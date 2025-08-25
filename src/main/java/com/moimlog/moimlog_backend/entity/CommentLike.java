package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 댓글 좋아요 엔티티 클래스
 * 댓글에 대한 사용자의 좋아요 정보를 저장하는 테이블
 */
@Entity
@Table(name = "comment_likes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentLike {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // JPA 생명주기 메서드
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
