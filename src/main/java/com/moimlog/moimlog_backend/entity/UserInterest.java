package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자-관심사 연결 엔티티 클래스
 * 사용자와 관심사의 다대다 관계를 관리하는 테이블
 */
@Entity
@Table(name = "user_interests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInterest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // JPA 생명주기 메서드
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // 정적 팩토리 메서드
    public static UserInterest createUserInterest(User user, Interest interest) {
        return UserInterest.builder()
                .user(user)
                .interest(interest)
                .build();
    }
} 