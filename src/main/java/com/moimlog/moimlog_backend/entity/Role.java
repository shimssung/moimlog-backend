package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;

/**
 * 역할 엔티티 클래스
 * 시스템에서 사용할 수 있는 역할들을 정의
 */
@Entity
@Table(name = "roles")
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 20)
    private RoleType name;
    
    @Column(name = "description", length = 100)
    private String description;
    
    // 기본 생성자
    public Role() {
    }
    
    // 모든 필드를 매개변수로 받는 생성자
    public Role(Long id, RoleType name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    // Getter 메서드들
    public Long getId() {
        return id;
    }
    
    public RoleType getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    // Setter 메서드들
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setName(RoleType name) {
        this.name = name;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public enum RoleType {
        ADMIN,        // 관리자
        USER,         // 일반 사용자
        MODERATOR     // 모임 운영자
    }
} 