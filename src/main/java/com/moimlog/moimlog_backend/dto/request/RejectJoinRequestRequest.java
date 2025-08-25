package com.moimlog.moimlog_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 모임 참여신청 거절 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectJoinRequestRequest {
    
    @NotBlank(message = "거절 사유는 필수입니다.")
    @Size(max = 500, message = "거절 사유는 500자를 초과할 수 없습니다.")
    private String reason;
}
