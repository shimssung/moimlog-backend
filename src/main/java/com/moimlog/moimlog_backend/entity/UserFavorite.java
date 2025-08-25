package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자 즐겨찾기 엔티티 클래스
 * 사용자가 즐겨찾기한 모임 정보를 저장하는 테이블
 */
@Entity
@Table(name = "user_favorites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFavorite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id", nullable = false)
    private Moim moim;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // JPA 생명주기 메서드
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
