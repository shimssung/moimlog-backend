package com.moimlog.moimlog_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 설정을 위한 Configuration 클래스
 * application.properties의 jwt.* 설정을 바인딩
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    /**
     * JWT 시크릿 키
     */
    private String secret;
    
    /**
     * Access Token 만료 시간 (밀리초)
     */
    private Long accessTokenExpiration;
    
    /**
     * Refresh Token 만료 시간 (밀리초)
     */
    private Long refreshTokenExpiration;
    
    // application.properties의 하이픈 형식과 매핑
    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
    
    public void setAccessTokenExpiration(Long accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }
    
    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
    
    public void setRefreshTokenExpiration(Long refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
} 