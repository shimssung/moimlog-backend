package com.moimlog.moimlog_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 온보딩 응답 DTO
 * 온보딩 처리 결과를 반환하기 위한 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingResponse {
    
    /**
     * 성공 여부
     */
    private boolean success;
    
    /**
     * 응답 메시지
     */
    private String message;
    
    /**
     * 사용자 ID
     */
    private Long userId;
    
    /**
     * 닉네임
     */
    private String nickname;
    
    /**
     * 온보딩 완료 여부
     */
    private boolean isOnboardingCompleted;
    
    /**
     * 성공 응답 생성
     */
    public static OnboardingResponse success(Long userId, String nickname) {
        return OnboardingResponse.builder()
                .success(true)
                .message("온보딩이 완료되었습니다.")
                .userId(userId)
                .nickname(nickname)
                .isOnboardingCompleted(true)
                .build();
    }
    
    /**
     * 실패 응답 생성
     */
    public static OnboardingResponse failure(String message) {
        return OnboardingResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
} 