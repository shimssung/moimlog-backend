package com.moimlog.moimlog_backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 모임 생성 응답 DTO
 * 모임 생성 성공 시 프론트엔드로 전송하는 데이터
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMoimResponse {
    
    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private CategoryInfo category;
    private Integer maxMembers;
    private Integer currentMembers;
    private List<String> tags;
    private String thumbnail;
    private Boolean isPrivate;
    private String onlineType;
    private String location;
    private String locationDetail;
    private CreatorInfo createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 카테고리 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String label;
        private String color;
    }
    
    /**
     * 생성자 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatorInfo {
        private Long id;
        private String name;
        private String profileImage;
    }
    
    /**
     * 성공 응답 생성
     */
    public static CreateMoimResponse success(Long id, String title, String description,
                                          Long categoryId, CategoryInfo category,
                                          Integer maxMembers, Integer currentMembers,
                                          List<String> tags, String thumbnail,
                                          Boolean isPrivate, String onlineType,
                                          String location, String locationDetail,
                                          CreatorInfo createdBy, LocalDateTime createdAt,
                                          LocalDateTime updatedAt) {
        return CreateMoimResponse.builder()
                .id(id)
                .title(title)
                .description(description)
                .categoryId(categoryId)
                .category(category)
                .maxMembers(maxMembers)
                .currentMembers(currentMembers)
                .tags(tags)
                .thumbnail(thumbnail)
                .isPrivate(isPrivate)
                .onlineType(onlineType)
                .location(location)
                .locationDetail(locationDetail)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
