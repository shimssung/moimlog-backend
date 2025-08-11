package com.moimlog.moimlog_backend.dto.request;

import lombok.*;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * 모임 생성 요청 DTO
 * 프론트엔드에서 모임 생성 시 전송하는 데이터
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMoimRequest {
    
    @NotBlank(message = "모임 제목을 입력해주세요.")
    @Size(min = 1, max = 200, message = "모임 제목은 1-200자로 입력해주세요.")
    private String title;
    
    @NotBlank(message = "모임 설명을 입력해주세요.")
    @Size(min = 1, max = 2000, message = "모임 설명은 1-2000자로 입력해주세요.")
    private String description;
    
    @NotNull(message = "카테고리를 선택해주세요.")
    @Min(value = 1, message = "올바른 카테고리를 선택해주세요.")
    @Max(value = 10, message = "올바른 카테고리를 선택해주세요.")
    private Long categoryId;
    
    @NotNull(message = "최대 인원을 입력해주세요.")
    @Min(value = 2, message = "최대 인원은 2명 이상으로 설정해주세요.")
    @Max(value = 100, message = "최대 인원은 100명 이하로 설정해주세요.")
    private Integer maxMembers;
    
    @Size(max = 10, message = "태그는 최대 10개까지 입력 가능합니다.")
    private List<@Size(min = 1, max = 20, message = "각 태그는 1-20자로 입력해주세요.") String> tags;
    
    private String thumbnail;  // Base64 이미지 또는 URL 허용 (온보딩과 동일)
    
    @Builder.Default
    private Boolean isPrivate = false;
    
    @NotNull(message = "모임 형태를 선택해주세요.")
    @Pattern(regexp = "^(online|offline|hybrid)$", message = "올바른 모임 형태를 선택해주세요.")
    private String onlineType;
    
    @Size(max = 500, message = "활동 지역은 500자 이하로 입력해주세요.")
    private String location;
    
    @Size(max = 500, message = "상세 주소는 500자 이하로 입력해주세요.")
    private String locationDetail;
    
    /**
     * 온라인 타입에 따른 지역 필수 여부 검증
     */
    @AssertTrue(message = "오프라인 또는 하이브리드 모임의 경우 활동 지역을 입력해주세요.")
    public boolean isLocationRequiredForOffline() {
        if ("online".equals(onlineType)) {
            return true; // 온라인 모임은 지역 불필요
        }
        // 오프라인 또는 하이브리드 모임은 지역 필수
        return location != null && !location.trim().isEmpty();
    }
    
    /**
     * 태그 정제 (중복 제거, 공백 제거)
     */
    public List<String> getCleanedTags() {
        if (tags == null) {
            return List.of();
        }
        return tags.stream()
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .toList();
    }
    
    /**
     * 제목 정제 (앞뒤 공백 제거)
     */
    public String getCleanedTitle() {
        return title != null ? title.trim() : "";
    }
    
    /**
     * 설명 정제 (앞뒤 공백 제거)
     */
    public String getCleanedDescription() {
        return description != null ? description.trim() : "";
    }
    
    /**
     * 지역 정제 (앞뒤 공백 제거)
     */
    public String getCleanedLocation() {
        return location != null ? location.trim() : "";
    }
    
    /**
     * 상세 주소 정제 (앞뒤 공백 제거)
     */
    public String getCleanedLocationDetail() {
        return locationDetail != null ? locationDetail.trim() : "";
    }
}
