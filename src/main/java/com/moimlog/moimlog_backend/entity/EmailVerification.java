package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 이메일 인증 엔티티 클래스
 * 이메일 인증 코드와 관련 정보를 저장
 */
@Entity
@Table(name = "email_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "email", nullable = false, length = 255)
    private String email;
    
    @Column(name = "verification_code", nullable = false, length = 6)
    private String verificationCode;
    
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    // JPA 생명주기 메서드
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // 인증 코드 생성용 정적 팩토리 메서드
    public static EmailVerification createVerification(String email, String verificationCode) {
        return EmailVerification.builder()
                .email(email)
                .verificationCode(verificationCode)
                .isVerified(false)
                .expiresAt(LocalDateTime.now().plusMinutes(10)) // 10분 후 만료
                .build();
    }
    
    // 인증 코드 검증
    public boolean isValidCode(String code) {
        return verificationCode.equals(code) && 
               !isVerified && 
               LocalDateTime.now().isBefore(expiresAt);
    }
    
    // 인증 완료 처리
    public void verify() {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }
} 