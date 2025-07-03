package com.moimlog.moimlog_backend.controller;

import com.moimlog.moimlog_backend.dto.request.SignupRequest;
import com.moimlog.moimlog_backend.dto.response.SignupResponse;
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
@CrossOrigin(origins = "*")
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