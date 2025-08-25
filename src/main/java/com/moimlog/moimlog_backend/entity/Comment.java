package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 댓글 엔티티 클래스
 * 게시글에 달리는 댓글을 저장하는 테이블
 */
@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "is_anonymous", nullable = false)
    @Builder.Default
    private Boolean isAnonymous = false;
    
    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Integer likeCount = 0;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 연관관계 매핑
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> replies = new ArrayList<>();
    
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommentLike> likes = new ArrayList<>();
    
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
    public void incrementLikeCount() {
        this.likeCount++;
    }
    
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
    
    public void addReply(Comment reply) {
        this.replies.add(reply);
        reply.setParent(this);
    }
    
    public void removeReply(Comment reply) {
        this.replies.remove(reply);
        reply.setParent(null);
    }
    
    public void addLike(CommentLike like) {
        this.likes.add(like);
        like.setComment(this);
        incrementLikeCount();
    }
    
    public void removeLike(CommentLike like) {
        this.likes.remove(like);
        like.setComment(null);
        decrementLikeCount();
    }
    
    public boolean isReply() {
        return this.parent != null;
    }
    
    public boolean hasReplies() {
        return !this.replies.isEmpty();
    }
}
