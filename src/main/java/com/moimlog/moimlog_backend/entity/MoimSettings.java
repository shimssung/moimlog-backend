package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 모임 설정 엔티티 클래스
 * 모임의 다양한 설정 정보를 저장하는 테이블
 */
@Entity
@Table(name = "moim_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoimSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id", nullable = false, unique = true)
    private Moim moim;
    
    @Column(name = "allow_anonymous_posts", nullable = false)
    @Builder.Default
    private Boolean allowAnonymousPosts = false;
    
    @Column(name = "require_approval_for_posts", nullable = false)
    @Builder.Default
    private Boolean requireApprovalForPosts = false;
    
    @Column(name = "notification_new_message", nullable = false)
    @Builder.Default
    private Boolean notificationNewMessage = true;
    
    @Column(name = "notification_new_post", nullable = false)
    @Builder.Default
    private Boolean notificationNewPost = true;
    
    @Column(name = "notification_new_event", nullable = false)
    @Builder.Default
    private Boolean notificationNewEvent = true;
    
    @Column(name = "notification_member_join", nullable = false)
    @Builder.Default
    private Boolean notificationMemberJoin = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // JPA 생명주기 메서드
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 정적 팩토리 메서드
    public static MoimSettings createDefaultSettings(Moim moim) {
        return MoimSettings.builder()
                .moim(moim)
                .allowAnonymousPosts(false)
                .requireApprovalForPosts(false)
                .notificationNewMessage(true)
                .notificationNewPost(true)
                .notificationNewEvent(true)
                .notificationMemberJoin(true)
                .build();
    }
    
    // 익명 게시글 허용 여부 변경
    public void toggleAnonymousPosts() {
        this.allowAnonymousPosts = !this.allowAnonymousPosts;
    }
    
    // 게시글 승인 필요 여부 변경
    public void togglePostApproval() {
        this.requireApprovalForPosts = !this.requireApprovalForPosts;
    }
    
    // 알림 설정 변경
    public void updateNotificationSettings(Boolean newMessage, Boolean newPost, 
                                        Boolean newEvent, Boolean memberJoin) {
        if (newMessage != null) this.notificationNewMessage = newMessage;
        if (newPost != null) this.notificationNewPost = newPost;
        if (newEvent != null) this.notificationNewEvent = newEvent;
        if (memberJoin != null) this.notificationMemberJoin = memberJoin;
    }
    
    // 모든 알림 설정 토글
    public void toggleAllNotifications() {
        boolean newValue = !this.notificationNewMessage;
        this.notificationNewMessage = newValue;
        this.notificationNewPost = newValue;
        this.notificationNewEvent = newValue;
        this.notificationMemberJoin = newValue;
    }
    
    @Override
    public String toString() {
        return "MoimSettings{" +
                "id=" + id +
                ", moim=" + (moim != null ? moim.getTitle() : "null") +
                ", allowAnonymousPosts=" + allowAnonymousPosts +
                ", requireApprovalForPosts=" + requireApprovalForPosts +
                ", notificationNewMessage=" + notificationNewMessage +
                ", notificationNewPost=" + notificationNewPost +
                ", notificationNewEvent=" + notificationNewEvent +
                ", notificationMemberJoin=" + notificationMemberJoin +
                '}';
    }
}
