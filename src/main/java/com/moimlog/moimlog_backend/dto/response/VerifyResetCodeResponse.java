package com.moimlog.moimlog_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 재설정용 인증 코드 검증 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyResetCodeResponse {
    
    private boolean success;
    private String message;
    private String email;
    private boolean verified;
    
    public static VerifyResetCodeResponse success(String email) {
        return VerifyResetCodeResponse.builder()
                .success(true)
                .message("인증 코드가 확인되었습니다. 새 비밀번호를 입력해주세요.")
                .email(email)
                .verified(true)
                .build();
    }
    
    public static VerifyResetCodeResponse failure(String message) {
        return VerifyResetCodeResponse.builder()
                .success(false)
                .message(message)
                .verified(false)
                .build();
    }
} 