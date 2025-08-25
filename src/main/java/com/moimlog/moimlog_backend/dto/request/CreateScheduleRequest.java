package com.moimlog.moimlog_backend.dto.request;

import com.moimlog.moimlog_backend.entity.Schedule;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 일정 작성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateScheduleRequest {
    
    @NotBlank(message = "일정 제목은 필수입니다.")
    @Size(max = 200, message = "일정 제목은 200자를 초과할 수 없습니다.")
    private String title;
    
    private String description;
    
    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @Size(max = 500, message = "위치는 500자를 초과할 수 없습니다.")
    private String location;
    
    private String locationDetail;
    
    private Integer maxAttendees;
    
    @NotNull(message = "일정 타입은 필수입니다.")
    private Schedule.ScheduleType type;
    
    @Builder.Default
    private Boolean isRecurring = false;
    
    @Size(max = 200, message = "반복 규칙은 200자를 초과할 수 없습니다.")
    private String recurrenceRule;
}
