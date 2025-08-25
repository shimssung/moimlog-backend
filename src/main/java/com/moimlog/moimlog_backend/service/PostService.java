package com.moimlog.moimlog_backend.service;

import com.moimlog.moimlog_backend.dto.request.CreateCommentRequest;
import com.moimlog.moimlog_backend.dto.request.CreatePostRequest;
import com.moimlog.moimlog_backend.dto.response.PostListResponse;
import com.moimlog.moimlog_backend.dto.response.PostResponse;
import com.moimlog.moimlog_backend.entity.*;
import com.moimlog.moimlog_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 게시글 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostService {
    
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final MoimRepository moimRepository;
    private final UserRepository userRepository;
    
    /**
     * 게시글 목록 조회
     */
    public PostListResponse getPosts(Long moimId, int page, int size, String type, String search, String sort) {
        // 페이지네이션 설정
        Pageable pageable = createPageable(page, size, sort);
        
        // 게시글 조회
        Page<Post> postPage;
        if (type != null && !type.equals("all")) {
            Post.PostType postType = Post.PostType.valueOf(type.toUpperCase());
            if (search != null && !search.trim().isEmpty()) {
                postPage = postRepository.findByMoimIdAndTypeAndSearchKeyword(moimId, postType, search, pageable);
            } else {
                postPage = postRepository.findByMoimIdAndTypeOrderByIsPinnedDescCreatedAtDesc(moimId, postType, pageable);
            }
        } else {
            if (search != null && !search.trim().isEmpty()) {
                postPage = postRepository.findByMoimIdAndSearchKeyword(moimId, search, pageable);
            } else {
                postPage = postRepository.findByMoimIdOrderByIsPinnedDescCreatedAtDesc(moimId, pageable);
            }
        }
        
        // DTO 변환
        List<PostListResponse.PostSummary> posts = postPage.getContent().stream()
                .map(PostListResponse.PostSummary::fromEntity)
                .collect(Collectors.toList());
        
        // 페이지네이션 정보 생성
        PostListResponse.PaginationInfo pagination = PostListResponse.PaginationInfo.builder()
                .currentPage(page)
                .totalPages(postPage.getTotalPages())
                .totalElements(postPage.getTotalElements())
                .size(size)
                .hasNext(postPage.hasNext())
                .hasPrevious(postPage.hasPrevious())
                .build();
        
        return PostListResponse.builder()
                .posts(posts)
                .pagination(pagination)
                .build();
    }
    
    /**
     * 게시글 상세 조회
     */
    public PostResponse getPostDetail(Long postId, Long moimId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        
        // 모임 ID 검증
        if (!post.getMoim().getId().equals(moimId)) {
            throw new IllegalArgumentException("잘못된 모임 ID입니다.");
        }
        
        // 조회수 증가
        post.incrementViewCount();
        postRepository.save(post);
        
        // DTO 변환
        PostResponse response = PostResponse.fromEntity(post);
        
        // 이미지 정보 추가
        List<PostResponse.PostImageResponse> images = post.getImages().stream()
                .map(image -> PostResponse.PostImageResponse.builder()
                        .id(image.getId())
                        .imageUrl(image.getImageUrl())
                        .imageOrder(image.getImageOrder())
                        .createdAt(image.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        response.setImages(images);
        
        // 댓글 정보 추가
        List<PostResponse.CommentResponse> comments = commentRepository.findTopLevelCommentsByPostId(postId)
                .stream()
                .map(this::convertToCommentResponse)
                .collect(Collectors.toList());
        response.setComments(comments);
        
        return response;
    }
    
    /**
     * 게시글 작성
     */
    public Post createPost(Long moimId, Long userId, CreatePostRequest request) {
        // 모임 존재 확인
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
        
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        // 게시글 생성
        Post post = Post.builder()
                .moim(moim)
                .author(user)
                .title(request.getTitle())
                .content(request.getContent())
                .type(request.getType())
                .isAnonymous(request.getIsAnonymous())
                .build();
        
        // 이미지 추가
        if (request.getImages() != null) {
            for (CreatePostRequest.PostImageRequest imageRequest : request.getImages()) {
                PostImage image = PostImage.builder()
                        .imageUrl(imageRequest.getImageUrl())
                        .imageOrder(imageRequest.getImageOrder())
                        .build();
                post.addImage(image);
            }
        }
        
        Post savedPost = postRepository.save(post);
        
        // 모임의 게시글 수 증가
        moim.setCurrentMembers(moim.getCurrentMembers() + 1);
        moimRepository.save(moim);
        
        return savedPost;
    }
    
    /**
     * 댓글 작성
     */
    public Map<String, Object> createComment(Long postId, Long userId, CreateCommentRequest request) {
        // 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        // 부모 댓글 확인 (대댓글인 경우)
        Comment parentComment = null;
        if (request.getParentId() != null) {
            parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부모 댓글입니다."));
            
            // 부모 댓글이 같은 게시글에 속하는지 확인
            if (!parentComment.getPost().getId().equals(postId)) {
                throw new IllegalArgumentException("잘못된 부모 댓글입니다.");
            }
        }
        
        // 댓글 생성
        Comment comment = Comment.builder()
                .post(post)
                .author(user)
                .parent(parentComment)
                .content(request.getContent())
                .isAnonymous(request.getIsAnonymous())
                .build();
        
        Comment savedComment = commentRepository.save(comment);
        
        // 게시글에 댓글 추가
        post.addComment(savedComment);
        postRepository.save(post);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedComment.getId());
        response.put("content", savedComment.getContent());
        response.put("authorId", savedComment.getAuthor().getId());
        response.put("authorName", savedComment.getIsAnonymous() ? "익명" : savedComment.getAuthor().getName());
        response.put("isAnonymous", savedComment.getIsAnonymous());
        response.put("createdAt", savedComment.getCreatedAt());
        response.put("parentId", savedComment.getParent() != null ? savedComment.getParent().getId() : null);
        
        return response;
    }
    
    /**
     * 게시글 좋아요 토글
     */
    public Map<String, Object> togglePostLike(Long postId, Long userId) {
        // 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        // 이미 좋아요를 눌렀는지 확인
        PostLike existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId)
                .orElse(null);
        
        Map<String, Object> response = new HashMap<>();
        
        if (existingLike != null) {
            // 좋아요 취소
            postLikeRepository.delete(existingLike);
            post.decrementLikeCount();
            postRepository.save(post);
            
            response.put("liked", false);
            response.put("message", "좋아요를 취소했습니다.");
        } else {
            // 좋아요 추가
            PostLike newLike = PostLike.builder()
                    .post(post)
                    .user(user)
                    .build();
            postLikeRepository.save(newLike);
            post.incrementLikeCount();
            postRepository.save(post);
            
            response.put("liked", true);
            response.put("message", "좋아요를 눌렀습니다.");
        }
        
        response.put("likeCount", post.getLikeCount());
        
        return response;
    }
    
    /**
     * 댓글 좋아요 토글
     */
    public Map<String, Object> toggleCommentLike(Long commentId, Long userId) {
        // 댓글 존재 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        // 이미 좋아요를 눌렀는지 확인
        CommentLike existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId)
                .orElse(null);
        
        Map<String, Object> response = new HashMap<>();
        
        if (existingLike != null) {
            // 좋아요 취소
            commentLikeRepository.delete(existingLike);
            comment.decrementLikeCount();
            commentRepository.save(comment);
            
            response.put("liked", false);
            response.put("message", "좋아요를 취소했습니다.");
        } else {
            // 좋아요 추가
            CommentLike newLike = CommentLike.builder()
                    .comment(comment)
                    .user(user)
                    .build();
            commentLikeRepository.save(newLike);
            comment.incrementLikeCount();
            commentRepository.save(comment);
            
            response.put("liked", true);
            response.put("message", "좋아요를 눌렀습니다.");
        }
        
        response.put("likeCount", comment.getLikeCount());
        
        return response;
    }
    
    /**
     * 페이지네이션 설정 생성
     */
    private Pageable createPageable(int page, int size, String sort) {
        Sort sortObj;
        switch (sort.toLowerCase()) {
            case "popular":
                sortObj = Sort.by(Sort.Direction.DESC, "likeCount", "createdAt");
                break;
            case "title":
                sortObj = Sort.by(Sort.Direction.ASC, "title");
                break;
            case "latest":
            default:
                sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
        }
        
        return PageRequest.of(page - 1, size, sortObj);
    }
    
    /**
     * 댓글을 응답 DTO로 변환
     */
    private PostResponse.CommentResponse convertToCommentResponse(Comment comment) {
        PostResponse.CommentResponse response = PostResponse.CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getIsAnonymous() ? "익명" : comment.getAuthor().getName())
                .authorProfileImage(comment.getIsAnonymous() ? null : comment.getAuthor().getProfileImage())
                .isAnonymous(comment.getIsAnonymous())
                .likeCount(comment.getLikeCount())
                .createdAt(comment.getCreatedAt())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .build();
        
        // 답글 추가
        if (comment.hasReplies()) {
            List<PostResponse.CommentResponse> replies = comment.getReplies().stream()
                    .map(this::convertToCommentResponse)
                    .collect(Collectors.toList());
            response.setReplies(replies);
        }
        
        return response;
    }
}
