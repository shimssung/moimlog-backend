package com.moimlog.moimlog_backend.service;

import com.moimlog.moimlog_backend.dto.request.LoginRequest;
import com.moimlog.moimlog_backend.dto.request.SignupRequest;
import com.moimlog.moimlog_backend.dto.response.LoginResponse;
import com.moimlog.moimlog_backend.dto.response.SignupResponse;
import com.moimlog.moimlog_backend.entity.User;
import com.moimlog.moimlog_backend.repository.UserRepository;
import com.moimlog.moimlog_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 서비스 클래스
 * 사용자 관련 비즈니스 로직을 처리
 */
@Service
@RequiredArgsConstructor // 생성자 자동 생성
@Slf4j // 로깅 기능 제공
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * 회원가입 처리
     * @param signupRequest 회원가입 요청 정보
     * @return 회원가입 결과
     */
    public SignupResponse signup(SignupRequest signupRequest) {
        log.info("회원가입 요청: {}", signupRequest.getEmail());
        
        try {
            // 이메일 중복 확인
            if (userRepository.existsByEmail(signupRequest.getEmail())) {
                log.warn("이미 존재하는 이메일: {}", signupRequest.getEmail());
                return SignupResponse.failure("이미 가입된 이메일입니다.");
            }
            
            // 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
            
            // 사용자 생성
            User user = User.createUser(
                signupRequest.getEmail(),
                encodedPassword,
                signupRequest.getName(),
                signupRequest.getNickname()
            );
            
            // 추가 정보 설정
            if (signupRequest.getPhone() != null) {
                user.setPhone(signupRequest.getPhone());
            }
            if (signupRequest.getBio() != null) {
                user.setBio(signupRequest.getBio());
            }
            
            // 사용자 저장
            User savedUser = userRepository.save(user);
            
            log.info("회원가입 성공: {}", savedUser.getEmail());
            
            return SignupResponse.success(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getNickname()
            );
            
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생: {}", e.getMessage(), e);
            return SignupResponse.failure("회원가입 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 이메일로 사용자 조회
     * @param email 조회할 이메일
     * @return 사용자 정보 (Optional)
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }
    
    /**
     * 사용자 ID로 조회
     * @param id 사용자 ID
     * @return 사용자 정보 (Optional)
     */
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElse(null);
    }
    
    /**
     * 이메일 중복 확인
     * @param email 확인할 이메일
     * @return 중복 여부
     */
    @Transactional(readOnly = true)
    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * 로그인 처리
     * @param loginRequest 로그인 요청 정보
     * @return 로그인 결과
     */
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("로그인 요청: {}", loginRequest.getEmail());
        
        try {
            // 사용자 조회
            User user = userRepository.findByEmailAndIsActiveTrue(loginRequest.getEmail())
                    .orElse(null);
            
            if (user == null) {
                log.warn("존재하지 않는 사용자: {}", loginRequest.getEmail());
                return LoginResponse.failure("이메일 또는 비밀번호가 올바르지 않습니다.");
            }
            
            // 비밀번호 검증
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                log.warn("비밀번호 불일치: {}", loginRequest.getEmail());
                return LoginResponse.failure("이메일 또는 비밀번호가 올바르지 않습니다.");
            }
            
            // JWT 토큰 생성
            String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getId());
            
            // 마지막 로그인 시간 업데이트
            user.setLastLoginAt(java.time.LocalDateTime.now());
            userRepository.save(user);
            
            log.info("로그인 성공: {}", user.getEmail());
            
            return LoginResponse.success(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname()
            );
            
        } catch (Exception e) {
            log.error("로그인 중 오류 발생: {}", e.getMessage(), e);
            return LoginResponse.failure("로그인 중 오류가 발생했습니다.");
        }
    }
} 