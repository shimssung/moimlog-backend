package com.moimlog.moimlog_backend.config;

import com.moimlog.moimlog_backend.repository.UserRepository;
import com.moimlog.moimlog_backend.util.JwtUtil;
import com.moimlog.moimlog_backend.service.OAuth2Service;
import com.moimlog.moimlog_backend.entity.User;
import com.moimlog.moimlog_backend.dto.response.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.util.Arrays;

/**
 * Spring Security 설정 클래스
 * 
 * 애플리케이션의 보안 설정을 담당
 * - 비밀번호 암호화 방식 설정 (BCrypt)
 * - URL 접근 권한 설정
 * - CSRF 보호 설정
 * - CORS 설정
 */
@Configuration
@EnableWebSecurity // Spring Security 활성화
@Slf4j

// final 필드들을 매개변수로 받는 생성자를 자동 생성
// Spring의 의존성 주입(DI)을 위한 편의 기능
public class SecurityConfig {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final OAuth2Service oAuth2Service;
    private final AuthenticationEntryPoint customAuthEntryPoint;
    private final AccessDeniedHandler customAccessDeniedHandler;
    
    @Autowired
    public SecurityConfig(UserRepository userRepository, JwtUtil jwtUtil, OAuth2Service oAuth2Service,
                          AuthenticationEntryPoint customAuthEntryPoint,
                          AccessDeniedHandler customAccessDeniedHandler) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.oAuth2Service = oAuth2Service;
        this.customAuthEntryPoint = customAuthEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    /**
     * 비밀번호 암호화를 위한 BCrypt 인코더 빈 등록
     * 
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * UserDetailsService 빈 등록
     * 
     * @return UserDetailsService 인스턴스
     */
    @Bean
    UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmailAndIsActiveTrue(username)
                // 사용자가 있으면 User 반환, 없으면 예외 발생
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }
    
    /**
     * OAuth2UserService 빈 등록
     * 
     * @return DefaultOAuth2UserService 인스턴스
     */
    @Bean
    DefaultOAuth2UserService oAuth2UserService() {
        return new DefaultOAuth2UserService();
    }

    /**
     * Spring Security 필터 체인 설정
     * 
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain
     * @throws Exception 설정 오류 시
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 보호 비활성화 (개발 단계에서만)
            .csrf(csrf -> csrf.disable())
            
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 세션 관리 설정
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // URL별 접근 권한 설정
            .authorizeHttpRequests(authz -> authz
                // 공개 API (인증 불필요)
                .requestMatchers("/auth/signup").permitAll()
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/auth/check-email").permitAll()
                .requestMatchers("/auth/send-verification").permitAll()
                .requestMatchers("/auth/verify-email").permitAll()
                .requestMatchers("/auth/forgot-password").permitAll() // 비밀번호 찾기
                .requestMatchers("/auth/verify-reset-code").permitAll() // 비밀번호 재설정 인증 코드 검증
                .requestMatchers("/auth/reset-password").permitAll() // 비밀번호 재설정
                .requestMatchers("/auth/check-nickname").permitAll() // 온보딩 중 닉네임 중복 체크
                .requestMatchers("/auth/refresh").permitAll() // 토큰 갱신 (인증 불필요)
                .requestMatchers("/h2-console/**").permitAll() // H2 콘솔 (개발용)
                .requestMatchers("/error").permitAll()
                
                // OAuth2 관련 URL (인증 불필요)
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/oauth2/authorization/**").permitAll()
                
                // 인증이 필요한 API
                .requestMatchers("/auth/me").authenticated()
                .requestMatchers("/auth/profile").authenticated()
                .requestMatchers("/auth/onboarding").authenticated() // 온보딩은 인증 필요
                .requestMatchers("/auth/onboarding/status").authenticated()
                .requestMatchers("/auth/user-categories").authenticated()
                .requestMatchers("/auth/moim-categories").authenticated()
                .requestMatchers("/auth/logout").authenticated()
                .requestMatchers("/auth/upload-profile-image").authenticated()
                
                // 관리자 API
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // 나머지는 인증된 사용자만 접근 가능
                .anyRequest().authenticated()
            )
            
            // OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService())
                )
                .successHandler((request, response, authentication) -> {
                    // OAuth2 로그인 성공 시 프론트엔드로 리다이렉트
                    if (authentication.getPrincipal() instanceof OAuth2User) {
                        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                        
                        // OAuth2Service를 통해 사용자 처리
                        User user = oAuth2Service.processGoogleUser(oauth2User.getAttributes());
                        LoginResponse loginResponse = oAuth2Service.createLoginResponse(user);
                        
                        // 로그 추가
                        log.info("OAuth2 로그인 성공 - 사용자: {}, 온보딩완료: {}, 리다이렉트URL: {}", 
                            user.getEmail(), user.getIsOnboardingCompleted(), 
                            "http://localhost:3000/oauth2-callback?token=" + loginResponse.getAccessToken().substring(0, 20) + "...");
                        
                        // 프론트엔드로 리다이렉트 (토큰을 URL 파라미터로 전달)
                        String redirectUrl = "http://localhost:3000/oauth2-callback?token=" + loginResponse.getAccessToken();
                        response.sendRedirect(redirectUrl);
                    } else {
                        // 오류 시 프론트엔드로 리다이렉트 (오류 파라미터 포함)
                        String redirectUrl = "http://localhost:3000/oauth2-callback?error=oauth2_user_not_found";
                        response.sendRedirect(redirectUrl);
                    }
                })
                .failureHandler((request, response, exception) -> {
                    // OAuth2 로그인 실패 시 프론트엔드로 리다이렉트
                    String redirectUrl = "http://localhost:3000/oauth2-callback?error=oauth2_login_failed&message=" + 
                        java.net.URLEncoder.encode(exception.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
                    response.sendRedirect(redirectUrl);
                })
            )
            
            // HTTP Basic 인증 비활성화 (JWT 사용 예정)
            .httpBasic(httpBasic -> httpBasic.disable())
            
            // JWT 필터 추가
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService()), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(customAuthEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            );
        
        return http.build();
    }

    /**
     * CORS 설정
     * 
     * @return CorsConfigurationSource
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 특정 origin만 허용 (보안 강화)
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true); // 쿠키 전송 허용 (보안 강화)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 