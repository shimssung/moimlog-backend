package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 모임 엔티티 클래스
 * 모임의 기본 정보를 저장하는 테이블
 */
@Entity
@Table(name = "moims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Moim {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private MoimCategory category;
    
    @Column(name = "tags", columnDefinition = "JSON")
    private String tags; // JSON 형태로 저장
    
    @Column(name = "thumbnail", length = 500)
    private String thumbnail;
    
    @Column(name = "max_members", nullable = false)
    private Integer maxMembers;
    
    @Column(name = "current_members", nullable = false)
    @Builder.Default
    private Integer currentMembers = 1;
    
    @Column(name = "is_private", nullable = false)
    @Builder.Default
    private Boolean isPrivate = false;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "online_type", nullable = false)
    private OnlineType onlineType;
    
    @Column(name = "location", length = 500)
    private String location;
    
    @Column(name = "location_detail", columnDefinition = "TEXT")
    private String locationDetail;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 온라인 타입 enum
    public enum OnlineType {
        ONLINE, OFFLINE, HYBRID
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
    
    // 정적 팩토리 메서드
    public static Moim createMoim(String title, String description, MoimCategory category,
                                 List<String> tags, String thumbnail, Integer maxMembers,
                                 Boolean isPrivate, OnlineType onlineType, String location,
                                 String locationDetail, User createdBy) {
        return Moim.builder()
                .title(title)
                .description(description)
                .category(category)
                .tags(convertTagsToJson(tags))
                .thumbnail(thumbnail)
                .maxMembers(maxMembers)
                .currentMembers(1)
                .isPrivate(isPrivate != null ? isPrivate : false)
                .onlineType(onlineType)
                .location(location)
                .locationDetail(locationDetail)
                .createdBy(createdBy)
                .isActive(true)
                .build();
    }
    
    // 태그 리스트를 JSON 문자열로 변환
    private static String convertTagsToJson(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        // 간단한 JSON 배열 형태로 변환
        return "[" + tags.stream()
                .map(tag -> "\"" + tag.replace("\"", "\\\"") + "\"")
                .reduce((a, b) -> a + "," + b)
                .orElse("") + "]";
    }
    
    // JSON 문자열을 태그 리스트로 변환
    public List<String> getTagsAsList() {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        // 간단한 파싱 (실제로는 Jackson ObjectMapper 사용 권장)
        String cleanTags = tags.replaceAll("[\\[\\]\"]", "");
        if (cleanTags.isEmpty()) {
            return List.of();
        }
        return List.of(cleanTags.split(","));
    }
    
    // 모임 멤버 수 증가
    public void incrementCurrentMembers() {
        this.currentMembers++;
    }
    
    // 모임 멤버 수 감소
    public void decrementCurrentMembers() {
        if (this.currentMembers > 0) {
            this.currentMembers--;
        }
    }
    
    // 모임이 가득 찼는지 확인
    public boolean isFull() {
        return this.currentMembers >= this.maxMembers;
    }
    
    // 모임에 참여 가능한지 확인
    public boolean canJoin() {
        return this.isActive && !this.isFull();
    }
    
    @Override
    public String toString() {
        return "Moim{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", category=" + (category != null ? category.getName() : "null") +
                ", maxMembers=" + maxMembers +
                ", currentMembers=" + currentMembers +
                ", isPrivate=" + isPrivate +
                ", onlineType=" + onlineType +
                ", location='" + location + '\'' +
                ", createdBy=" + (createdBy != null ? createdBy.getName() : "null") +
                ", createdAt=" + createdAt +
                '}';
    }
}
