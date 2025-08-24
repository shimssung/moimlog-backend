package com.moimlog.moimlog_backend.controller;

import com.moimlog.moimlog_backend.dto.request.LoginRequest;
import com.moimlog.moimlog_backend.dto.request.SignupRequest;
import com.moimlog.moimlog_backend.dto.request.EmailVerificationRequest;
import com.moimlog.moimlog_backend.dto.request.SendVerificationRequest;
import com.moimlog.moimlog_backend.dto.request.UpdateProfileRequest;
import com.moimlog.moimlog_backend.dto.request.OnboardingRequest;
import com.moimlog.moimlog_backend.dto.request.ForgotPasswordRequest;
import com.moimlog.moimlog_backend.dto.request.ResetPasswordRequest;
import com.moimlog.moimlog_backend.dto.request.VerifyResetCodeRequest;

import com.moimlog.moimlog_backend.dto.request.NotificationSettingsRequest;
import com.moimlog.moimlog_backend.dto.response.LoginResponse;
import com.moimlog.moimlog_backend.dto.response.SignupResponse;
import com.moimlog.moimlog_backend.dto.response.EmailVerificationResponse;
import com.moimlog.moimlog_backend.dto.response.UserProfileResponse;
import com.moimlog.moimlog_backend.dto.response.OnboardingResponse;
import com.moimlog.moimlog_backend.dto.response.ForgotPasswordResponse;
import com.moimlog.moimlog_backend.dto.response.ResetPasswordResponse;
import com.moimlog.moimlog_backend.dto.response.VerifyResetCodeResponse;

import com.moimlog.moimlog_backend.entity.MoimCategory;
import com.moimlog.moimlog_backend.service.UserService;
import com.moimlog.moimlog_backend.service.S3Service;
import com.amazonaws.services.s3.AmazonS3;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;

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
    private final Optional<S3Service> s3Service;
    private final AmazonS3 amazonS3;
    
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
            // Refresh Token만 HttpOnly 쿠키로 설정 (accessToken은 메모리에만 저장)
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                .httpOnly(true)
                .secure(false) // 개발환경에서는 false
                .sameSite("Lax")
                .maxAge(Duration.ofDays(7)) // 7일
                .path("/")
                .build();
            
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
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
     * 비밀번호 찾기 API
     * @param request 비밀번호 찾기 요청 정보
     * @return 처리 결과
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        log.info("비밀번호 찾기 API 호출: {}", request.getEmail());
        
        ForgotPasswordResponse response = userService.forgotPassword(request.getEmail());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 비밀번호 재설정 API
     * @param request 비밀번호 재설정 요청 정보
     * @return 처리 결과
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        log.info("비밀번호 재설정 API 호출: {}", request.getEmail());
        
        ResetPasswordResponse response = userService.resetPassword(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 비밀번호 재설정 인증 코드 검증 API
     * @param request 인증 코드 검증 요청 정보
     * @return 검증 결과
     */
    @PostMapping("/verify-reset-code")
    public ResponseEntity<VerifyResetCodeResponse> verifyResetCode(@RequestBody VerifyResetCodeRequest request) {
        log.info("비밀번호 재설정 인증 코드 검증 API 호출: {}", request.getEmail());
        VerifyResetCodeResponse response = userService.verifyResetCode(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 내 프로필 조회 API
     * @return 내 프로필 정보
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        log.info("내 프로필 조회 API 호출");
        
        UserProfileResponse response = userService.getMyProfile();
        log.info("프로필 응답 - 사용자: {}, 온보딩완료: {}", 
            response.getEmail(), response.isOnboardingCompleted());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 프로필 수정 API
     * @param request 프로필 수정 요청 정보
     * @return 수정된 프로필 정보
     */
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        log.info("프로필 수정 API 호출");
        
        UserProfileResponse response = userService.updateProfile(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 온보딩 처리 API
     * @param request 온보딩 요청 정보
     * @return 온보딩 결과
     */
    @PostMapping("/onboarding")
    public ResponseEntity<OnboardingResponse> processOnboarding(@RequestBody OnboardingRequest request) {
        log.info("온보딩 처리 API 호출");
        
        OnboardingResponse response = userService.processOnboarding(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 온보딩 완료 여부 확인 API
     * @return 온보딩 완료 여부
     */
    @GetMapping("/onboarding/status")
    public ResponseEntity<Object> checkOnboardingStatus() {
        log.info("온보딩 상태 확인 API 호출");
        
        boolean isCompleted = userService.isOnboardingCompleted();
        
        return ResponseEntity.ok()
                .body(new OnboardingStatusResponse(isCompleted));
    }
    

    
    /**
     * 알림 설정 업데이트 API
     * @param request 알림 설정 요청
     * @return 알림 설정 업데이트 결과
     */
    @PutMapping("/notification-settings")
    public ResponseEntity<Map<String, Object>> updateNotificationSettings(@RequestBody NotificationSettingsRequest request) {
        log.info("알림 설정 업데이트 API 호출");
        
        try {
            Map<String, Object> result = userService.updateNotificationSettings(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("알림 설정 업데이트 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", "알림 설정 업데이트에 실패했습니다.",
                    "error", e.getMessage()
                ));
        }
    }
    

    
    /**
     * 사용자 모임 카테고리 목록 조회 API
     * @return 모임 카테고리 이름 목록
     */
    @GetMapping("/user-categories")
    public ResponseEntity<Object> getUserCategories() {
        log.info("사용자 모임 카테고리 조회 API 호출");
        
        List<String> categories = userService.getUserCategories();
        
        return ResponseEntity.ok()
                .body(new UserCategoriesResponse(categories));
    }
    
    /**
     * 모든 모임 카테고리 목록 조회 API
     * @return 모임 카테고리 목록
     */
    @GetMapping("/moim-categories")
    public ResponseEntity<Object> getAllCategories() {
        log.info("전체 모임 카테고리 조회 API 호출");
        List<MoimCategory> categories = userService.getAllCategories();
        System.out.println("카테고리 목록: " + categories);
        return ResponseEntity.ok()
                .body(new AllCategoriesResponse(categories));
    }
    
    /**
     * 닉네임 중복 확인 API
     * @param nickname 확인할 닉네임
     * @return 중복 여부
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Object> checkNicknameDuplicate(@RequestParam String nickname) {
        log.info("닉네임 중복 확인 API 호출: {}", nickname);
        
        boolean isDuplicate = userService.isNicknameDuplicate(nickname);
        
        return ResponseEntity.ok()
                .body(new NicknameCheckResponse(nickname, isDuplicate));
    }
    
    /**
     * 토큰 갱신 API
     * @param request HTTP 요청 객체
     * @return 새로운 액세스 토큰
     */
    @PostMapping("/refresh")
    public ResponseEntity<Object> refreshToken(HttpServletRequest request) {
        log.info("토큰 갱신 API 호출");
        
        // HttpOnly 쿠키에서 리프레시 토큰 추출
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    log.debug("리프레시 토큰 발견: {}", refreshToken.substring(0, Math.min(20, refreshToken.length())) + "...");
                    break;
                }
            }
        }
        
        if (refreshToken == null) {
            log.warn("리프레시 토큰이 쿠키에 없습니다");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "리프레시 토큰이 없습니다"));
        }
        
        try {
            String newAccessToken = userService.refreshAccessToken(refreshToken);
            log.info("토큰 갱신 성공");
            
            // accessToken은 메모리에만 저장하므로 쿠키 설정 안함
            return ResponseEntity.ok()
                    .body(new TokenRefreshResponse(newAccessToken));
        } catch (Exception e) {
            log.error("토큰 갱신 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "토큰 갱신에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 로그아웃 API
     * @param request HTTP 요청 객체
     * @return 로그아웃 결과
     */
    @PostMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        log.info("로그아웃 API 호출");
        
        try {
            userService.logout();
            
            // refreshToken 쿠키만 삭제 (accessToken은 메모리에만 저장됨)
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // 개발환경에서는 false
                .sameSite("Lax")
                .maxAge(0) // 즉시 만료
                .path("/")
                .build();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(new LogoutResponse(true, "로그아웃이 완료되었습니다."));
        } catch (Exception e) {
            log.error("로그아웃 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new LogoutResponse(false, "로그아웃 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 프로필 이미지 업로드 API
     * @param file 업로드할 이미지 파일
     * @return 업로드된 이미지의 S3 URL
     */
    @PostMapping("/upload-profile-image")
    public ResponseEntity<Object> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        log.info("프로필 이미지 업로드 API 호출");
        
        if (s3Service.isPresent()) {
            try {
                String imageUrl = s3Service.get().upload(file);
                return ResponseEntity.ok().body(new ProfileImageUploadResponse(true, imageUrl));
            } catch (IOException e) {
                log.error("S3 업로드 실패", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ProfileImageUploadResponse(false, null));
            }
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ProfileImageUploadResponse(false, "S3 서비스가 비활성화되어 있습니다."));
        }
    }
    
    /**
     * 프로필 이미지 프록시 - S3 이미지를 백엔드를 통해 제공
     */
    @GetMapping("/profile-image/{imageKey}")
    public ResponseEntity<ByteArrayResource> getProfileImage(@PathVariable String imageKey) {
        try {
            // S3에서 이미지 다운로드
            S3Object s3Object = amazonS3.getObject("moimlog-bucket", "profile-images/" + imageKey);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            
            // 이미지 데이터를 바이트 배열로 변환
            byte[] imageData = inputStream.readAllBytes();
            inputStream.close();
            
            // 응답 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // 또는 적절한 이미지 타입
            headers.setContentLength(imageData.length);
            headers.setCacheControl("public, max-age=31536000"); // 1년 캐시
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new ByteArrayResource(imageData));
                    
        } catch (Exception e) {
            log.error("프로필 이미지 로드 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 모임 썸네일 프록시 - S3 이미지를 백엔드를 통해 제공
     */
    @GetMapping("/moim-thumbnail/{imageKey}")
    public ResponseEntity<ByteArrayResource> getMoimThumbnail(@PathVariable String imageKey) {
        try {
            // S3에서 이미지 다운로드 (profile-images 폴더에서 가져옴)
            S3Object s3Object = amazonS3.getObject("moimlog-bucket", "profile-images/" + imageKey);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            
            // 이미지 데이터를 바이트 배열로 변환
            byte[] imageData = inputStream.readAllBytes();
            inputStream.close();
            
            // 응답 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // 또는 적절한 이미지 타입
            headers.setContentLength(imageData.length);
            headers.setCacheControl("public, max-age=31536000"); // 1년 캐시
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new ByteArrayResource(imageData));
                    
        } catch (Exception e) {
            log.error("모임 썸네일 로드 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
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
    
    /**
     * 온보딩 상태 응답 DTO
     */
    public static class OnboardingStatusResponse {
        private boolean isCompleted;
        
        public OnboardingStatusResponse(boolean isCompleted) {
            this.isCompleted = isCompleted;
        }
        
        public boolean isCompleted() {
            return isCompleted;
        }
        
        public void setCompleted(boolean completed) {
            isCompleted = completed;
        }
    }
    
    /**
     * 사용자 모임 카테고리 응답 DTO
     */
    public static class UserCategoriesResponse {
        private List<String> categories;
        
        public UserCategoriesResponse(List<String> categories) {
            this.categories = categories;
        }
        
        public List<String> getCategories() {
            return categories;
        }
        
        public void setCategories(List<String> categories) {
            this.categories = categories;
        }
    }
    
    /**
     * 전체 모임 카테고리 응답 DTO
     */
    public static class AllCategoriesResponse {
        private List<MoimCategory> categories;
        
        public AllCategoriesResponse(List<MoimCategory> categories) {
            this.categories = categories;
        }
        
        public List<MoimCategory> getCategories() {
            return categories;
        }
        
        public void setCategories(List<MoimCategory> categories) {
            this.categories = categories;
        }
    }
    
    /**
     * 닉네임 중복 확인 응답 DTO
     */
    public static class NicknameCheckResponse {
        private String nickname;
        private boolean duplicate;
        
        public NicknameCheckResponse(String nickname, boolean duplicate) {
            this.nickname = nickname;
            this.duplicate = duplicate;
        }
        
        public String getNickname() {
            return nickname;
        }
        
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
        
        public boolean isDuplicate() {
            return duplicate;
        }
        
        public void setDuplicate(boolean duplicate) {
            this.duplicate = duplicate;
        }
    }
    
    /**
     * 토큰 갱신 응답 DTO
     */
    public static class TokenRefreshResponse {
        private String accessToken;
        
        public TokenRefreshResponse(String accessToken) {
            this.accessToken = accessToken;
        }
        
        public String getAccessToken() {
            return accessToken;
        }
        
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
    
    /**
     * 로그아웃 응답 DTO
     */
    public static class LogoutResponse {
        private boolean success;
        private String message;
        
        public LogoutResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * 프로필 이미지 업로드 응답 DTO
     */
    public static class ProfileImageUploadResponse {
        private boolean success;
        private String imageUrl;

        public ProfileImageUploadResponse(boolean success, String imageUrl) {
            this.success = success;
            this.imageUrl = imageUrl;
        }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
}