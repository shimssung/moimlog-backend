package com.moimlog.moimlog_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 재설정 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordResponse {
    
    private boolean success;
    private String message;
    private String email;
    
    public static ResetPasswordResponse success(String email) {
        return ResetPasswordResponse.builder()
                .success(true)
                .message("비밀번호가 성공적으로 변경되었습니다.")
                .email(email)
                .build();
    }
    
    public static ResetPasswordResponse failure(String message) {
        return ResetPasswordResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
} 