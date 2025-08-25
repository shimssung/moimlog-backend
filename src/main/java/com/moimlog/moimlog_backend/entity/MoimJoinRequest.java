package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 모임 참여신청 엔티티 클래스
 * 사용자가 모임에 참여를 신청하고 운영자가 승인/거절하는 정보를 저장
 */
@Entity
@Table(name = "moim_join_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoimJoinRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id", nullable = false)
    private Moim moim;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;
    
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;
    
    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;
    
    // 상태 열거형
    public enum Status {
        PENDING,    // 승인 대기
        APPROVED,   // 승인 완료
        REJECTED    // 거절됨
    }
    
    // JPA 생명주기 메서드
    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }
    
    // 참여신청 생성용 정적 팩토리 메서드
    public static MoimJoinRequest createJoinRequest(Moim moim, User user, String message) {
        return MoimJoinRequest.builder()
                .moim(moim)
                .user(user)
                .message(message)
                .status(Status.PENDING)
                .build();
    }
    
    // 승인 처리
    public void approve(User admin) {
        this.status = Status.APPROVED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = admin;
    }
    
    // 거절 처리
    public void reject(User admin, String reason) {
        this.status = Status.REJECTED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = admin;
        this.rejectReason = reason;
    }
    
    // 처리 가능한 상태인지 확인
    public boolean canBeProcessed() {
        return this.status == Status.PENDING;
    }
    
    // 승인된 상태인지 확인
    public boolean isApproved() {
        return this.status == Status.APPROVED;
    }
    
    // 거절된 상태인지 확인
    public boolean isRejected() {
        return this.status == Status.REJECTED;
    }
    
    // 대기 중인 상태인지 확인
    public boolean isPending() {
        return this.status == Status.PENDING;
    }
}
