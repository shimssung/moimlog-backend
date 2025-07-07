package com.moimlog.moimlog_backend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 위한 유틸리티 클래스
 */
@Component
@Slf4j
public class JwtUtil {
    
    // JWT 시크릿 키
    @Value("${jwt.secret}")
    private String secret;
    
    // Access Token 만료 시간
    @Value("${jwt.expiration}")
    private Long expiration;
    
    // Refresh Token 만료 시간
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;
    
    /**
     * JWT 시크릿 키 생성
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    /**
     * Access Token 생성
     */
    public String generateAccessToken(String email, Long userId) {
        return generateToken(email, userId, expiration);
    }
    
    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(String email, Long userId) {
        return generateToken(email, userId, refreshExpiration);
    }
    
    /**
     * JWT 토큰 생성
     */
    private String generateToken(String email, Long userId, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 토큰에서 이메일 추출
     */
    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }
    
    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        return getClaimsFromToken(token).get("userId", Long.class);
    }
    
    /**
     * 토큰에서 Claims 추출
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 토큰 만료 시간 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
} 