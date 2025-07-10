package com.moimlog.moimlog_backend.controller;

import com.moimlog.moimlog_backend.dto.request.LoginRequest;
import com.moimlog.moimlog_backend.dto.request.SignupRequest;
import com.moimlog.moimlog_backend.dto.request.EmailVerificationRequest;
import com.moimlog.moimlog_backend.dto.request.SendVerificationRequest;
import com.moimlog.moimlog_backend.dto.response.LoginResponse;
import com.moimlog.moimlog_backend.dto.response.SignupResponse;
import com.moimlog.moimlog_backend.dto.response.EmailVerificationResponse;
import com.moimlog.moimlog_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 컨트롤러
 * 회원가입, 로그인, 로그아웃 등의 인증 관련 API를 처리
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // CORS 설정
public class AuthController {
    
    private final UserService userService;
    
    /**
     * 회원가입 API
     * @param signupRequest 회원가입 요청 정보
     * @return 회원가입 결과
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest signupRequest) {
        log.info("회원가입 API 호출: {}", signupRequest.getEmail());
        
        SignupResponse response = userService.signup(signupRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 로그인 API
     * @param loginRequest 로그인 요청 정보
     * @return 로그인 결과
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("로그인 API 호출: {}", loginRequest.getEmail());
        
        LoginResponse response = userService.login(loginRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 이메일 중복 확인 API
     * @param email 확인할 이메일
     * @return 중복 여부
     */
    @GetMapping("/check-email")
    public ResponseEntity<Object> checkEmailDuplicate(@RequestParam String email) {
        log.info("이메일 중복 확인 API 호출: {}", email);
        
        boolean isDuplicate = userService.isEmailDuplicate(email);
        
        return ResponseEntity.ok()
                .body(new EmailCheckResponse(email, isDuplicate));
    }
    
    /**
     * 이메일 인증 코드 발송 API
     * @param request 인증 코드 발송 요청 정보
     * @return 발송 결과
     */
    @PostMapping("/send-verification")
    public ResponseEntity<Object> sendVerificationCode(@RequestBody SendVerificationRequest request) {
        log.info("이메일 인증 코드 발송 API 호출: {}", request.getEmail());
        
        // 이메일 중복 확인
        if (userService.isEmailDuplicate(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new EmailVerificationResponse(false, "이미 가입된 이메일입니다.", request.getEmail(), false));
        }
        
        boolean success = userService.sendVerificationCode(request.getEmail());
        
        if (success) {
            return ResponseEntity.ok()
                    .body(new EmailVerificationResponse(true, "인증 코드가 발송되었습니다.", request.getEmail(), false));
        } else {
            return ResponseEntity.badRequest()
                    .body(new EmailVerificationResponse(false, "인증 코드 발송에 실패했습니다.", request.getEmail(), false));
        }
    }
    
    /**
     * 이메일 인증 코드 검증 API
     * @param request 인증 요청 정보
     * @return 인증 결과
     */
    @PostMapping("/verify-email")
    public ResponseEntity<EmailVerificationResponse> verifyEmail(@RequestBody EmailVerificationRequest request) {
        log.info("이메일 인증 API 호출: {}", request.getEmail());
        
        EmailVerificationResponse response = userService.verifyEmail(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 이메일 중복 확인 응답 DTO
     * 직접 반환 시 비밀번호 노출 위험 있음
     * 따라서 이메일 중복 확인 응답 DTO를 별도로 생성하여 반환
     * 이메일 중복 확인에만 사용되는 간단한 DTO
     */
    public static class EmailCheckResponse {
        private String email;
        private boolean duplicate;
        
        public EmailCheckResponse(String email, boolean duplicate) {
            this.email = email;
            this.duplicate = duplicate;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public boolean isDuplicate() {
            return duplicate;
        }
        
        public void setDuplicate(boolean duplicate) {
            this.duplicate = duplicate;
        }
    }
}