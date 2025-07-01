package com.moimlog.moimlog_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정 클래스
 * 
 * 애플리케이션의 보안 설정을 담당
 * - 비밀번호 암호화 방식 설정 (BCrypt)
 * - URL 접근 권한 설정
 * - CSRF 보호 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 비밀번호 암호화를 위한 BCrypt 인코더 빈 등록
     * 
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security 필터 체인 설정
     * 
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain
     * @throws Exception 설정 오류 시
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 보호 비활성화 (개발 단계에서만)
            .csrf(csrf -> csrf.disable())
            
            // URL별 접근 권한 설정
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/test/**").permitAll()  // 테스트 API는 모든 사용자 접근 가능
                .requestMatchers("/moimlog/admin/**").hasRole("ADMIN")  // 관리자 API는 ADMIN 역할 필요
                .anyRequest().authenticated()  // 나머지는 인증된 사용자만 접근 가능
            )
            
            // HTTP Basic 인증 활성화
            .httpBasic(httpBasic -> {});
        
        return http.build();
    }
} 