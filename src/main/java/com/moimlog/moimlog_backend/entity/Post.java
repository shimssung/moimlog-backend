package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 게시글 엔티티 클래스
 * 모임 내에서 작성되는 게시글을 저장하는 테이블
 */
@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id", nullable = false)
    private Moim moim;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private PostType type = PostType.FREE;
    
    @Column(name = "is_pinned", nullable = false)
    @Builder.Default
    private Boolean isPinned = false;
    
    @Column(name = "is_anonymous", nullable = false)
    @Builder.Default
    private Boolean isAnonymous = false;
    
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;
    
    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Integer likeCount = 0;
    
    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private Integer commentCount = 0;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 연관관계 매핑
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostImage> images = new ArrayList<>();
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostLike> likes = new ArrayList<>();
    
    // 게시글 타입 enum
    public enum PostType {
        NOTICE, FREE, PHOTO, EVENT
    }
    
    // JPA 생명주기 메서드
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 비즈니스 메서드
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    public void incrementLikeCount() {
        this.likeCount++;
    }
    
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
    
    public void incrementCommentCount() {
        this.commentCount++;
    }
    
    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
    
    public void addImage(PostImage image) {
        this.images.add(image);
        image.setPost(this);
    }
    
    public void removeImage(PostImage image) {
        this.images.remove(image);
        image.setPost(null);
    }
    
    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setPost(this);
        incrementCommentCount();
    }
    
    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        comment.setPost(null);
        decrementCommentCount();
    }
    
    public void addLike(PostLike like) {
        this.likes.add(like);
        like.setPost(this);
        incrementLikeCount();
    }
    
    public void removeLike(PostLike like) {
        this.likes.remove(like);
        like.setPost(null);
        decrementLikeCount();
    }
}
