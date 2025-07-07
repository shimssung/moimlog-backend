package com.moimlog.moimlog_backend.dto.response;

import lombok.*;

/**
 * 회원가입 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SignupResponse {
    
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String message;
    private boolean success;
    
    public static SignupResponse success(Long id, String email, String name, String nickname) {
        return SignupResponse.builder()
                .id(id)
                .email(email)
                .name(name)
                .nickname(nickname)
                .message("회원가입이 성공적으로 완료되었습니다.")
                .success(true)
                .build();
    }
    
    public static SignupResponse failure(String message) {
        return SignupResponse.builder()
                .message(message)
                .success(false)
                .build();
    }
} 