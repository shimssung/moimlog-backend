package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자-역할 매핑 엔티티 클래스
 * 사용자와 역할 간의 다대다 관계를 관리하는 중간 테이블
 */
@Entity
@Table(name = "user_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
} 