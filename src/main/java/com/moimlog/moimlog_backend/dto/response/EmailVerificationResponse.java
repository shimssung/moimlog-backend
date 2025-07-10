package com.moimlog.moimlog_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 이메일 인증 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationResponse {
    
    private boolean success;
    private String message;
    private String email;
    private boolean verified;
    
    public static EmailVerificationResponse success(String email) {
        return EmailVerificationResponse.builder()
                .success(true)
                .message("이메일 인증이 완료되었습니다.")
                .email(email)
                .verified(true)
                .build();
    }
    
    public static EmailVerificationResponse failure(String message) {
        return EmailVerificationResponse.builder()
                .success(false)
                .message(message)
                .verified(false)
                .build();
    }
} 