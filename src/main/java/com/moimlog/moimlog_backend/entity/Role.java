package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 역할 엔티티 클래스
 * 시스템에서 사용할 수 있는 역할들을 정의
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 20)
    private RoleType name;
    
    @Column(name = "description", length = 100)
    private String description;
    
    public enum RoleType {
        ADMIN,        // 관리자
        USER,         // 일반 사용자
        MODERATOR     // 모임 운영자
    }
} 