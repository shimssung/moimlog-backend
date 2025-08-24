package com.moimlog.moimlog_backend.dto.request;

import lombok.*;

/**
 * 알림 설정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class NotificationSettingsRequest {
    
    private Boolean notificationEmail;
    private Boolean notificationPush;
    private Boolean notificationSchedule;
    private Boolean notificationComment;
}
