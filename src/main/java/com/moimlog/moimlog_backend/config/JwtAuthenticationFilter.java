package com.moimlog.moimlog_backend.config;

import com.moimlog.moimlog_backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * 요청에서 JWT 토큰을 추출하고 검증하여 인증 정보를 설정
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        log.debug("JWT 필터 체크 - 요청 경로: {}", path);

        boolean shouldNotFilter =  path.contains("/auth/signup") ||
                                    path.contains("/auth/login") ||
                                    path.contains("/auth/check-email") ||
                                    path.contains("/auth/send-verification") ||
                                    path.contains("/auth/verify-email") ||
                                    path.contains("/auth/forgot-password") ||
                                    path.contains("/auth/verify-reset-code") ||
                                    path.contains("/auth/reset-password") ||
                                    path.contains("/auth/check-nickname") ||
                                    path.contains("/auth/refresh") ||
                                    path.contains("/h2-console") ||
                                    path.contains("/error");

        log.debug("JWT 필터 적용 여부: {}", !shouldNotFilter);
        return shouldNotFilter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 요청에서 JWT 토큰 추출
            String token = extractTokenFromRequest(request);
            
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // 토큰에서 사용자 정보 추출
                String email = jwtUtil.getEmailFromToken(token);
                
                // UserDetails 로드
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                
                // 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                
                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("JWT 인증 성공: {}", email);
            }
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 요청에서 JWT 토큰 추출
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
} 