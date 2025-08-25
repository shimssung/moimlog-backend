package com.moimlog.moimlog_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

/**
 * 모임 참여신청 승인 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveJoinRequestRequest {
    
    @Size(max = 500, message = "환영 메시지는 500자를 초과할 수 없습니다.")
    private String message;
}
