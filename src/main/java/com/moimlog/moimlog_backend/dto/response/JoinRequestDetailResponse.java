package com.moimlog.moimlog_backend.dto.response;

import com.moimlog.moimlog_backend.entity.MoimJoinRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 모임 참여신청 상세 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestDetailResponse {
    
    private Long id;
    private Long moimId;
    private String moimTitle;
    private String moimCategory;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userProfileImage;
    private String userBio;
    private String message;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private Long processedBy;
    private String processedByName;
    private String rejectReason;
    
    // 정적 팩토리 메서드
    public static JoinRequestDetailResponse from(MoimJoinRequest joinRequest) {
        return JoinRequestDetailResponse.builder()
                .id(joinRequest.getId())
                .moimId(joinRequest.getMoim().getId())
                .moimTitle(joinRequest.getMoim().getTitle())
                .moimCategory(joinRequest.getMoim().getCategory() != null ? joinRequest.getMoim().getCategory().getName() : null)
                .userId(joinRequest.getUser().getId())
                .userName(joinRequest.getUser().getName())
                .userEmail(joinRequest.getUser().getEmail())
                .userProfileImage(joinRequest.getUser().getProfileImage())
                .userBio(joinRequest.getUser().getBio())
                .message(joinRequest.getMessage())
                .status(joinRequest.getStatus().name())
                .requestedAt(joinRequest.getRequestedAt())
                .processedAt(joinRequest.getProcessedAt())
                .processedBy(joinRequest.getProcessedBy() != null ? joinRequest.getProcessedBy().getId() : null)
                .processedByName(joinRequest.getProcessedBy() != null ? joinRequest.getProcessedBy().getName() : null)
                .rejectReason(joinRequest.getRejectReason())
                .build();
    }
}
