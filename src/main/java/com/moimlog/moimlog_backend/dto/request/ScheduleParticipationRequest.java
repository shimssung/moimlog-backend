package com.moimlog.moimlog_backend.dto.request;

import com.moimlog.moimlog_backend.entity.ScheduleParticipant;
import lombok.*;

import jakarta.validation.constraints.NotNull;

/**
 * 일정 참석 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleParticipationRequest {
    
    @NotNull(message = "참석 상태는 필수입니다.")
    private ScheduleParticipant.Status status;
}
