package com.moimlog.moimlog_backend.dto.response;

import com.moimlog.moimlog_backend.entity.Post;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 목록 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostListResponse {
    
    private List<PostSummary> posts;
    private PaginationInfo pagination;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PostSummary {
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
        private List<PostImageSummary> images;
        
        // 엔티티를 DTO로 변환하는 정적 메서드
        public static PostSummary fromEntity(Post post) {
            return PostSummary.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent().length() > 100 ? 
                            post.getContent().substring(0, 100) + "..." : 
                            post.getContent())
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
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PostImageSummary {
        private Long id;
        private String imageUrl;
        private Integer imageOrder;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginationInfo {
        private Integer currentPage;
        private Integer totalPages;
        private Long totalElements;
        private Integer size;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }
}
