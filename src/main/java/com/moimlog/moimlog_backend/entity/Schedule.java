package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 일정 엔티티 클래스
 * 모임의 일정 정보를 저장하는 테이블
 */
@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id", nullable = false)
    private Moim moim;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "location", length = 500)
    private String location;
    
    @Column(name = "location_detail", columnDefinition = "TEXT")
    private String locationDetail;
    
    @Column(name = "max_attendees")
    private Integer maxAttendees;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private ScheduleType type = ScheduleType.MEETING;
    
    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;
    
    @Column(name = "recurrence_rule", length = 200)
    private String recurrenceRule;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 연관관계 매핑
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ScheduleParticipant> participants = new ArrayList<>();
    
    // 일정 타입 enum
    public enum ScheduleType {
        MEETING, TASK, DEADLINE, EVENT
    }
    
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
    
    // 비즈니스 메서드
    public void addParticipant(ScheduleParticipant participant) {
        this.participants.add(participant);
        participant.setSchedule(this);
    }
    
    public void removeParticipant(ScheduleParticipant participant) {
        this.participants.remove(participant);
        participant.setSchedule(null);
    }
    
    public boolean isFull() {
        if (maxAttendees == null || maxAttendees == 0) {
            return false;
        }
        long attendingCount = participants.stream()
                .filter(p -> p.getStatus() == ScheduleParticipant.Status.ATTENDING)
                .count();
        return attendingCount >= maxAttendees;
    }
    
    public boolean isPast() {
        return LocalDateTime.now().isAfter(startDate);
    }
    
    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startDate) && (endDate == null || now.isBefore(endDate));
    }
}
