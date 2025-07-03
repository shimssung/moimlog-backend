package com.moimlog.moimlog_backend.dto.request;

import lombok.*;

/**
 * 회원가입 요청 DTO
 */
@Getter // 모든 필드에 대한 getter 메서드 생성
@Setter // 모든 필드에 대한 setter 메서드 생성
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 포함한 생성자
@Builder // 빌더 패턴 사용
@ToString // 객체 문자열 표현
public class SignupRequest {
    
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String phone;
    private String bio;
} 