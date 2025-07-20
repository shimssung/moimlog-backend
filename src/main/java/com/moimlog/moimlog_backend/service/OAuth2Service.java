package com.moimlog.moimlog_backend.service;

import com.moimlog.moimlog_backend.dto.response.LoginResponse;
import com.moimlog.moimlog_backend.entity.User;
import com.moimlog.moimlog_backend.repository.UserRepository;
import com.moimlog.moimlog_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * OAuth2 소셜 로그인 서비스
 * Google, Kakao, Naver 소셜 로그인을 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OAuth2Service {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    /**
     * OAuth2 사용자 정보 처리
     */
    public User processOAuth2User(String provider, String providerId, String email, String name) {
        log.info("OAuth2 로그인 시도: provider={}, email={}, name={}", provider, email, name);
        
        // 기존 사용자 조회
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            // 새로운 사용자 생성
            user = createNewUser(email, name, provider, providerId);
            log.info("새로운 OAuth2 사용자 생성: {}", email);
        } else {
            // 기존 사용자 정보 업데이트
            updateExistingUser(user, provider, providerId);
            log.info("기존 OAuth2 사용자 업데이트: {}", email);
        }
        
        // 마지막 로그인 시간 업데이트
        user.setLastLoginAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    /**
     * 새로운 사용자 생성
     */
    private User createNewUser(String email, String name, String provider, String providerId) {
        User user = User.createSocialUser(email, name, provider, providerId);
        return userRepository.save(user);
    }
    
    /**
     * 기존 사용자 정보 업데이트
     */
    private void updateExistingUser(User user, String provider, String providerId) {
        // 이미 소셜 로그인으로 연결된 경우
        if (user.isSocialLogin()) {
            log.info("이미 소셜 로그인으로 연결된 사용자: {}", user.getEmail());
            return;
        }
        
        // 이메일 회원가입 사용자를 소셜 로그인과 연결
        log.info("이메일 회원가입 사용자를 소셜 로그인과 연결: {}", user.getEmail());
        
        // 기존 온보딩 상태 저장
        Boolean originalOnboardingStatus = user.getIsOnboardingCompleted();
        
        // 소셜 로그인 정보 업데이트
        user.setOauthProvider(provider);
        user.setOauthProviderId(providerId);
        user.setIsVerified(true);
        
        // 기존 온보딩 상태 복원 (변경되지 않도록)
        user.setIsOnboardingCompleted(originalOnboardingStatus);
        
        log.info("기존 사용자의 온보딩 상태 유지: isOnboardingCompleted={}", user.getIsOnboardingCompleted());
    }
    
    /**
     * OAuth2 로그인 응답 생성
     */
    public LoginResponse createLoginResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getId());
        
        LoginResponse response = LoginResponse.success(
            accessToken,
            refreshToken,
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getNickname(),
            user.getIsOnboardingCompleted()
        );
        
        log.info("OAuth2 로그인 응답 생성 - 사용자: {}, 온보딩완료: {}, 소셜로그인: {}", 
            user.getEmail(), user.getIsOnboardingCompleted(), user.isSocialLogin());
        
        return response;
    }
    
    /**
     * Google OAuth2 사용자 정보 처리
     */
    public User processGoogleUser(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub");
        
        return processOAuth2User("google", providerId, email, name);
    }
    
    /**
     * Kakao OAuth2 사용자 정보 처리
     */
    public User processKakaoUser(Map<String, Object> attributes) {
        String providerId = attributes.get("id").toString();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        
        String email = (String) kakaoAccount.get("email");
        String name = (String) profile.get("nickname");
        
        return processOAuth2User("kakao", providerId, email, name);
    }
    
    /**
     * Naver OAuth2 사용자 정보 처리
     */
    public User processNaverUser(Map<String, Object> attributes) {
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        
        String email = (String) response.get("email");
        String name = (String) response.get("name");
        String providerId = (String) response.get("id");
        
        return processOAuth2User("naver", providerId, email, name);
    }
} 