package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 모임 멤버 엔티티 클래스
 * 모임에 참여한 사용자들의 정보를 저장하는 테이블
 */
@Entity
@Table(name = "moim_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoimMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id", nullable = false)
    private Moim moim;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.MEMBER;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;
    
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
    
    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    // 역할 enum
    public enum Role {
        ADMIN, MODERATOR, MEMBER
    }
    
    // 상태 enum
    public enum Status {
        ACTIVE, PENDING, BANNED
    }
    
    // JPA 생명주기 메서드
    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
        lastActiveAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastActiveAt = LocalDateTime.now();
    }
    
    // 정적 팩토리 메서드
    public static MoimMember createMoimMember(Moim moim, User user, Role role) {
        return MoimMember.builder()
                .moim(moim)
                .user(user)
                .role(role != null ? role : Role.MEMBER)
                .status(Status.ACTIVE)
                .build();
    }
    
    // 관리자 역할 확인
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }
    
    // 모더레이터 이상 역할 확인
    public boolean isModeratorOrHigher() {
        return this.role == Role.ADMIN || this.role == Role.MODERATOR;
    }
    
    // 활성 상태 확인
    public boolean isActive() {
        return this.status == Status.ACTIVE;
    }
    
    // 마지막 활동 시간 업데이트
    public void updateLastActiveTime() {
        this.lastActiveAt = LocalDateTime.now();
    }
    
    // 역할 변경
    public void changeRole(Role newRole) {
        this.role = newRole;
    }
    
    // 상태 변경
    public void changeStatus(Status newStatus) {
        this.status = newStatus;
    }
    
    @Override
    public String toString() {
        return "MoimMember{" +
                "id=" + id +
                ", moim=" + (moim != null ? moim.getTitle() : "null") +
                ", user=" + (user != null ? user.getName() : "null") +
                ", role=" + role +
                ", status=" + status +
                ", joinedAt=" + joinedAt +
                '}';
    }
}
