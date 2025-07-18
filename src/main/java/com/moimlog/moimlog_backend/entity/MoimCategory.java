package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 모임 카테고리 엔티티 클래스
 * 모임의 카테고리를 저장하는 테이블
 */
@Entity
@Table(name = "moim_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoimCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(name = "label", nullable = false, length = 50)
    private String label;
    
    @Column(name = "description", length = 200)
    private String description;
    
    @Column(name = "color", length = 20)
    private String color;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // JPA 생명주기 메서드
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // 정적 팩토리 메서드
    public static MoimCategory createCategory(String name, String label, String description, String color) {
        return MoimCategory.builder()
                .name(name)
                .label(label)
                .description(description)
                .color(color)
                .isActive(true)
                .build();
    }

    @Override
    public String toString() {
        return "MoimCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", label='" + label + '\'' +
                ", description='" + description + '\'' +
                ", color='" + color + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
} 