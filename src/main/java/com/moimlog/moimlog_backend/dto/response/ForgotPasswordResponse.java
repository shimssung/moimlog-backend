package com.moimlog.moimlog_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 찾기 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponse {
    
    private boolean success;
    private String message;
    private String email;
    
    public static ForgotPasswordResponse success(String email) {
        return ForgotPasswordResponse.builder()
                .success(true)
                .message("비밀번호 재설정 이메일이 발송되었습니다.")
                .email(email)
                .build();
    }
    
    public static ForgotPasswordResponse failure(String message) {
        return ForgotPasswordResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
} 