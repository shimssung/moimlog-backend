package com.moimlog.moimlog_backend.dto.response;

import lombok.*;

/**
 * 로그인 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LoginResponse {
    
    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String email;
    private String name;
    private String nickname;
    private Boolean isOnboardingCompleted;
    
    /**
     * 로그인 성공 시 응답 생성
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @param userId 사용자 ID
     * @param email 이메일
     * @param name 이름 
     * @param nickname 닉네임
     * @param isOnboardingCompleted 온보딩 완료 여부
     * @return 로그인 성공 응답
     */
    public static LoginResponse success(String accessToken, String refreshToken, Long userId, String email, String name, String nickname, Boolean isOnboardingCompleted) {
        return LoginResponse.builder()
                .success(true)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userId)
                .email(email)
                .name(name)
                .nickname(nickname)
                .isOnboardingCompleted(isOnboardingCompleted)
                .build();
    }
    
    /**
     * 로그인 실패 시 응답 생성
     * @param message 실패 메시지
     * @return 로그인 실패 응답
     */
    public static LoginResponse failure(String message) {
        return LoginResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
} 