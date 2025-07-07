package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;

/**
 * 사용자-역할 매핑 엔티티 클래스
 * 사용자와 역할 간의 다대다 관계를 관리하는 중간 테이블
 */
@Entity
@Table(name = "user_roles")
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
    
    // 기본 생성자
    public UserRole() {
    }
    
    // 모든 필드를 매개변수로 받는 생성자
    public UserRole(Long id, User user, Role role) {
        this.id = id;
        this.user = user;
        this.role = role;
    }
    
    // Getter 메서드들
    public Long getId() {
        return id;
    }
    
    public User getUser() {
        return user;
    }
    
    public Role getRole() {
        return role;
    }
    
    // Setter 메서드들
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
} 