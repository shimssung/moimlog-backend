package com.moimlog.moimlog_backend.config;

import com.moimlog.moimlog_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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

// final 필드들을 매개변수로 받는 생성자를 자동 생성
// Spring의 의존성 주입(DI)을 위한 편의 기능
@RequiredArgsConstructor 
public class SecurityConfig {

    private final UserRepository userRepository;

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
     * UserDetailsService 빈 등록
     * 
     * @return UserDetailsService 인스턴스
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmailAndIsActiveTrue(username)
                // 사용자가 있으면 User 반환, 없으면 예외 발생
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
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
                .requestMatchers("/auth/check-email").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // H2 콘솔 (개발용)
                .requestMatchers("/error").permitAll()
                
                // 관리자 API
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // 나머지는 인증된 사용자만 접근 가능
                .anyRequest().authenticated()
            )
            
            // HTTP Basic 인증 비활성화 (JWT 사용 예정)
            .httpBasic(httpBasic -> httpBasic.disable());
        
        return http.build();
    }

    /**
     * CORS 설정
     * 
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 