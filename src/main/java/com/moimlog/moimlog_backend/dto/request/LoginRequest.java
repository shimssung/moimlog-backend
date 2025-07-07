package com.moimlog.moimlog_backend.dto.request;

import lombok.*;

/**
 * 로그인 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LoginRequest {
    
    private String email;
    private String password;
} 