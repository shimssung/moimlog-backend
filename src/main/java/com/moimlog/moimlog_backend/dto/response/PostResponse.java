package com.moimlog.moimlog_backend.dto.response;

import com.moimlog.moimlog_backend.entity.Post;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    
    private Long id;
    private String title;
    private String content;
    private Post.PostType type;
    private Boolean isPinned;
    private Boolean isAnonymous;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Long authorId;
    private String authorName;
    private String authorProfileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PostImageResponse> images;
    private List<CommentResponse> comments;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PostImageResponse {
        private Long id;
        private String imageUrl;
        private Integer imageOrder;
        private LocalDateTime createdAt;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommentResponse {
        private Long id;
        private String content;
        private Long authorId;
        private String authorName;
        private String authorProfileImage;
        private Boolean isAnonymous;
        private Integer likeCount;
        private LocalDateTime createdAt;
        private Long parentId;
        private List<CommentResponse> replies;
    }
    
    // 엔티티를 DTO로 변환하는 정적 메서드
    public static PostResponse fromEntity(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .type(post.getType())
                .isPinned(post.getIsPinned())
                .isAnonymous(post.getIsAnonymous())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .authorId(post.getAuthor().getId())
                .authorName(post.getIsAnonymous() ? "익명" : post.getAuthor().getName())
                .authorProfileImage(post.getIsAnonymous() ? null : post.getAuthor().getProfileImage())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
