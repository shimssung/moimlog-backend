package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자-모임카테고리 연결 엔티티 클래스
 * 사용자와 모임 카테고리의 다대다 관계를 관리하는 테이블
 */
@Entity
@Table(name = "user_interests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMoimCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private MoimCategory moimCategory;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // JPA 생명주기 메서드
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // 정적 팩토리 메서드
    public static UserMoimCategory createUserMoimCategory(User user, MoimCategory moimCategory) {
        return UserMoimCategory.builder()
                .user(user)
                .moimCategory(moimCategory)
                .build();
    }
} 