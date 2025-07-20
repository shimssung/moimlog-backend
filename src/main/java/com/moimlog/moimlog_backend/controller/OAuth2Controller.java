package com.moimlog.moimlog_backend.controller;

import com.moimlog.moimlog_backend.dto.response.LoginResponse;
import com.moimlog.moimlog_backend.entity.User;
import com.moimlog.moimlog_backend.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;
import java.util.Map;

/**
 * OAuth2 소셜 로그인 컨트롤러
 * Google, Kakao, Naver 소셜 로그인을 처리
 */
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OAuth2Controller {
    
    private final OAuth2Service oAuth2Service;
    
    /**
     * Google OAuth2 로그인 콜백
     */
    @GetMapping("/google/callback")
    public ResponseEntity<LoginResponse> googleCallback(HttpServletRequest request) {
        log.info("Google OAuth2 콜백 호출");
        
        try {
            // OAuth2 인증 정보를 SecurityContext에서 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
                log.error("OAuth2 인증 정보를 찾을 수 없습니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(LoginResponse.failure("OAuth2 인증 정보를 찾을 수 없습니다."));
            }
            
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            log.info("Google OAuth2 콜백 호출 - 사용자: {}", oauth2User.getName());
            Map<String, Object> attributes = oauth2User.getAttributes();
            User user = oAuth2Service.processGoogleUser(attributes);
            LoginResponse response = oAuth2Service.createLoginResponse(user);
            
            // Refresh Token을 HttpOnly 쿠키로 설정
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                    .httpOnly(true)
                    .secure(false) // 개발환경에서는 false, 프로덕션에서는 true
                    .path("/")
                    .maxAge(Duration.ofDays(30))
                    .build();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(response);
                    
        } catch (Exception e) {
            log.error("Google OAuth2 로그인 처리 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.failure("Google 로그인 처리 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * Kakao OAuth2 로그인 콜백
     */
    @GetMapping("/kakao/callback")
    public ResponseEntity<LoginResponse> kakaoCallback(HttpServletRequest request) {
        log.info("Kakao OAuth2 콜백 호출");
        
        try {
            // OAuth2 인증 정보를 SecurityContext에서 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
                log.error("OAuth2 인증 정보를 찾을 수 없습니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(LoginResponse.failure("OAuth2 인증 정보를 찾을 수 없습니다."));
            }
            
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oauth2User.getAttributes();
            User user = oAuth2Service.processKakaoUser(attributes);
            LoginResponse response = oAuth2Service.createLoginResponse(user);
            
            // Refresh Token을 HttpOnly 쿠키로 설정
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                    .httpOnly(true)
                    .secure(false) // 개발환경에서는 false, 프로덕션에서는 true
                    .path("/")
                    .maxAge(Duration.ofDays(30))
                    .build();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(response);
                    
        } catch (Exception e) {
            log.error("Kakao OAuth2 로그인 처리 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.failure("Kakao 로그인 처리 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * Naver OAuth2 로그인 콜백
     */
    @GetMapping("/naver/callback")
    public ResponseEntity<LoginResponse> naverCallback(HttpServletRequest request) {
        log.info("Naver OAuth2 콜백 호출");
        
        try {
            // OAuth2 인증 정보를 SecurityContext에서 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
                log.error("OAuth2 인증 정보를 찾을 수 없습니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(LoginResponse.failure("OAuth2 인증 정보를 찾을 수 없습니다."));
            }
            
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oauth2User.getAttributes();
            User user = oAuth2Service.processNaverUser(attributes);
            LoginResponse response = oAuth2Service.createLoginResponse(user);
            
            // Refresh Token을 HttpOnly 쿠키로 설정
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                    .httpOnly(true)
                    .secure(false) // 개발환경에서는 false, 프로덕션에서는 true
                    .path("/")
                    .maxAge(Duration.ofDays(30))
                    .build();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(response);
                    
        } catch (Exception e) {
            log.error("Naver OAuth2 로그인 처리 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.failure("Naver 로그인 처리 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 소셜 로그인 URL 제공
     */
    @GetMapping("/urls")
    public ResponseEntity<Map<String, String>> getOAuth2Urls() {
        String baseUrl = "http://localhost:8080/moimlog";
        
        Map<String, String> urls = Map.of(
            "google", baseUrl + "/oauth2/authorization/google",
            "kakao", baseUrl + "/oauth2/authorization/kakao",
            "naver", baseUrl + "/oauth2/authorization/naver"
        );
        
        return ResponseEntity.ok(urls);
    }
} 