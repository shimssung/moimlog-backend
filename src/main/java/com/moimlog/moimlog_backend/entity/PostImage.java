package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 게시글 이미지 엔티티 클래스
 * 게시글에 첨부된 이미지 정보를 저장하는 테이블
 */
@Entity
@Table(name = "post_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
    
    @Column(name = "image_order", nullable = false)
    @Builder.Default
    private Integer imageOrder = 0;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // JPA 생명주기 메서드
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
