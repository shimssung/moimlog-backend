package com.moimlog.moimlog_backend.service;

import com.moimlog.moimlog_backend.dto.request.LoginRequest;
import com.moimlog.moimlog_backend.dto.request.SignupRequest;
import com.moimlog.moimlog_backend.dto.request.EmailVerificationRequest;
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

import com.moimlog.moimlog_backend.entity.User;
import com.moimlog.moimlog_backend.entity.EmailVerification;
import com.moimlog.moimlog_backend.entity.MoimCategory;
import com.moimlog.moimlog_backend.entity.UserMoimCategory;
import com.moimlog.moimlog_backend.repository.UserRepository;
import com.moimlog.moimlog_backend.repository.EmailVerificationRepository;
import com.moimlog.moimlog_backend.repository.MoimCategoryRepository;
import com.moimlog.moimlog_backend.repository.UserMoimCategoryRepository;
import com.moimlog.moimlog_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

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
    private final EmailVerificationRepository emailVerificationRepository;
    private final MoimCategoryRepository moimCategoryRepository;
    private final UserMoimCategoryRepository userMoimCategoryRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final Optional<S3Service> s3Service;
    
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
                user.getNickname(),
                user.getIsOnboardingCompleted()
            );
            
        } catch (Exception e) {
            log.error("로그인 중 오류 발생: {}", e.getMessage(), e);
            return LoginResponse.failure("로그인 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 이메일 인증 코드 발송
     * @param email 인증 코드를 발송할 이메일
     * @return 발송 결과
     */
    public boolean sendVerificationCode(String email) {
        log.info("이메일 인증 코드 발송 요청: {}", email);
        
        try {
            // 기존 미인증 인증 코드 삭제
            emailVerificationRepository.deleteByEmail(email);
            
            // 6자리 인증 코드 생성
            String verificationCode = generateVerificationCode();
            
            // 인증 정보 저장
            EmailVerification verification = EmailVerification.createVerification(email, verificationCode);
            emailVerificationRepository.save(verification);
            
            // 이메일 발송
            emailService.sendVerificationEmail(email, verificationCode);
            
            log.info("이메일 인증 코드 발송 완료: {}", email);
            return true;
            
        } catch (Exception e) {
            log.error("이메일 인증 코드 발송 실패: {}", email, e);
            return false;
        }
    }
    
    /**
     * 이메일 인증 코드 검증
     * @param request 인증 요청 정보
     * @return 인증 결과
     */
    public EmailVerificationResponse verifyEmail(EmailVerificationRequest request) {
        log.info("이메일 인증 요청: {}", request.getEmail());
        
        try {
            // 최신 미인증 인증 정보 조회
            EmailVerification verification = emailVerificationRepository
                    .findLatestUnverifiedByEmail(request.getEmail())
                    .orElse(null);
            
            if (verification == null) {
                return EmailVerificationResponse.failure("인증 코드를 찾을 수 없습니다. 다시 발송해주세요.");
            }
            
            // 인증 코드 검증
            if (!verification.isValidCode(request.getVerificationCode())) {
                return EmailVerificationResponse.failure("인증 코드가 올바르지 않거나 만료되었습니다.");
            }
            
            // 인증 완료 처리
            verification.verify();
            emailVerificationRepository.save(verification);
            
            log.info("이메일 인증 완료: {}", request.getEmail());
            return EmailVerificationResponse.success(request.getEmail());
            
        } catch (Exception e) {
            log.error("이메일 인증 중 오류 발생: {}", e.getMessage(), e);
            return EmailVerificationResponse.failure("인증 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 이메일 인증 상태 확인
     * @param email 확인할 이메일
     * @return 인증 완료 여부
     */
    @Transactional(readOnly = true)
    public boolean isEmailVerified(String email) {
        return emailVerificationRepository.findLatestUnverifiedByEmail(email)
                .map(EmailVerification::getIsVerified)
                .orElse(false);
    }
    
    /**
     * 내 프로필 조회
     * @return 내 프로필 정보
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        // 현재 인증된 사용자 정보 가져오기
        String currentUserEmail = getCurrentUserEmail();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return UserProfileResponse.from(user);
    }
    
    /**
     * 프로필 수정
     * @param request 프로필 수정 요청
     * @return 수정된 프로필 정보
     */
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        // 현재 인증된 사용자 정보 가져오기
        String currentUserEmail = getCurrentUserEmail();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 프로필 정보 업데이트
        user.setName(request.getName());
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }
        if (request.getGender() != null && !request.getGender().trim().isEmpty()) {
            try {
                User.Gender gender = User.Gender.valueOf(request.getGender().toUpperCase());
                user.setGender(gender);
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 성별 값: {}", request.getGender());
                // 잘못된 값은 무시하고 기존 값 유지
            }
        }

        
        User updatedUser = userRepository.save(user);
        return UserProfileResponse.from(updatedUser);
    }
    
    /**
     * 현재 인증된 사용자의 이메일 가져오기
     * @return 현재 사용자 이메일
     */
    private String getCurrentUserEmail() {
        return org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }
    
    /**
     * 온보딩 처리
     * @param request 온보딩 요청 정보
     * @return 온보딩 결과
     */
    @Transactional
    public OnboardingResponse processOnboarding(OnboardingRequest request) {
        log.info("온보딩 요청 처리");
        
        try {
            // 현재 인증된 사용자 정보 가져오기
            String currentUserEmail = getCurrentUserEmail();
            User user = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            // 닉네임 중복 확인
            if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
                if (userRepository.existsByNickname(request.getNickname())) {
                    return OnboardingResponse.failure("이미 사용 중인 닉네임입니다.");
                }
            }
            
            // 사용자 정보 업데이트
            if (request.getNickname() != null) {
                user.setNickname(request.getNickname());
            }
            if (request.getBio() != null) {
                user.setBio(request.getBio());
            }
            if (request.getProfileImage() != null) {
                // S3에 이미지 업로드
                if (s3Service.isPresent()) {
                    try {
                        String s3Url = s3Service.get().uploadBase64Image(
                            request.getProfileImage(), ".jpg");
                        user.setProfileImage(s3Url);
                        log.info("프로필 이미지 S3 업로드 완료: {}", s3Url);
                    } catch (Exception e) {
                        log.error("프로필 이미지 업로드 실패: {}", e.getMessage());
                        return OnboardingResponse.failure("프로필 이미지 업로드에 실패했습니다.");
                    }
                } else {
                    // S3가 비활성화된 경우 Base64 그대로 저장
                    user.setProfileImage(request.getProfileImage());
                }
            }
            
            // 온보딩 완료 상태로 변경
            user.setIsOnboardingCompleted(true);
            
            // 사용자 정보 저장
            userRepository.save(user);
            
            // 기존 관심사 삭제
            userMoimCategoryRepository.deleteByUserId(user.getId());
            
            // 새로운 모임 카테고리 추가
            if (request.getMoimCategories() != null && !request.getMoimCategories().isEmpty()) {
                for (String categoryName : request.getMoimCategories()) {
                    MoimCategory category = moimCategoryRepository.findByName(categoryName)
                            .orElse(null);
                    
                    if (category != null) {
                        UserMoimCategory userCategory = UserMoimCategory.createUserMoimCategory(user, category);
                        userMoimCategoryRepository.save(userCategory);
                    }
                }
            }
            
            log.info("온보딩 완료: {}", user.getEmail());
            
            return OnboardingResponse.success(user.getId(), user.getNickname());
            
        } catch (Exception e) {
            log.error("온보딩 처리 중 오류 발생: {}", e.getMessage(), e);
            return OnboardingResponse.failure("온보딩 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 온보딩 완료 여부 확인
     * @return 온보딩 완료 여부
     */
    @Transactional(readOnly = true)
    public boolean isOnboardingCompleted() {
        String currentUserEmail = getCurrentUserEmail();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return user.getIsOnboardingCompleted();
    }
    
    /**
     * 사용자의 모임 카테고리 목록 조회
     * @return 모임 카테고리 이름 목록
     */
    @Transactional(readOnly = true)
    public List<String> getUserCategories() {
        String currentUserEmail = getCurrentUserEmail();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return userMoimCategoryRepository.findCategoryNamesByUserId(user.getId());
    }
    
    /**
     * 모든 모임 카테고리 목록 조회
     * @return 모임 카테고리 목록
     */
    @Transactional(readOnly = true)
    public List<MoimCategory> getAllCategories() {
        return moimCategoryRepository.findAllOrderByName();
    }
    
    /**
     * 닉네임 중복 확인
     * @param nickname 확인할 닉네임
     * @return 중복 여부
     */
    @Transactional(readOnly = true)
    public boolean isNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
    
    /**
     * 액세스 토큰 갱신
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰
     */
    public String refreshAccessToken(String refreshToken) {
        log.info("액세스 토큰 갱신 요청");
        
        try {
            // 리프레시 토큰 검증
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
            }
            
            // 토큰에서 사용자 정보 추출
            String email = jwtUtil.getEmailFromRefreshToken(refreshToken);
            Long userId = jwtUtil.getUserIdFromRefreshToken(refreshToken);
            
            // 사용자 존재 확인
            userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            // 새로운 액세스 토큰 생성
            String newAccessToken = jwtUtil.generateAccessToken(email, userId);
            
            log.info("액세스 토큰 갱신 완료: {}", email);
            return newAccessToken;
            
        } catch (Exception e) {
            log.error("액세스 토큰 갱신 실패: {}", e.getMessage(), e);
            throw new RuntimeException("토큰 갱신에 실패했습니다.");
        }
    }
    
    /**
     * 로그아웃 처리
     */
    public void logout() {
        log.info("로그아웃 요청");
        
        try {
            // 현재 사용자 정보 가져오기
            String currentUserEmail = getCurrentUserEmail();
            User user = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            // 로그아웃 시간 업데이트 (선택사항)
            user.setLastLoginAt(java.time.LocalDateTime.now());
            userRepository.save(user);
            
            log.info("로그아웃 완료: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("로그아웃 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 비밀번호 찾기 요청 처리
     * @param email 비밀번호를 찾을 이메일
     * @return 처리 결과
     */
    public ForgotPasswordResponse forgotPassword(String email) {
        log.info("비밀번호 찾기 요청: {}", email);
        
        try {
            // 사용자 존재 확인
            User user = userRepository.findByEmailAndIsActiveTrue(email)
                    .orElse(null);
            
            if (user == null) {
                log.warn("존재하지 않는 사용자: {}", email);
                return ForgotPasswordResponse.failure("해당 이메일로 가입된 계정을 찾을 수 없습니다.");
            }
            
            // 기존 비밀번호 재설정 인증 코드 삭제
            emailVerificationRepository.deleteByEmail(email);
            
            // 6자리 인증 코드 생성
            String verificationCode = generateVerificationCode();
            
            // 인증 정보 저장
            EmailVerification verification = EmailVerification.createVerification(email, verificationCode);
            emailVerificationRepository.save(verification);
            
            // 비밀번호 재설정 이메일 발송
            emailService.sendPasswordResetEmail(email, verificationCode);
            
            log.info("비밀번호 재설정 이메일 발송 완료: {}", email);
            return ForgotPasswordResponse.success(email);
            
        } catch (Exception e) {
            log.error("비밀번호 찾기 처리 실패: {}", email, e);
            return ForgotPasswordResponse.failure("비밀번호 재설정 이메일 발송에 실패했습니다.");
        }
    }
    
    /**
     * 비밀번호 재설정용 인증 코드 검증
     * @param request 인증 코드 검증 요청 정보
     * @return 검증 결과
     */
    public VerifyResetCodeResponse verifyResetCode(VerifyResetCodeRequest request) {
        log.info("비밀번호 재설정 인증 코드 검증 요청: {}", request.getEmail());
        
        try {
            // 사용자 존재 확인
            User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                    .orElse(null);
            
            if (user == null) {
                return VerifyResetCodeResponse.failure("해당 이메일로 가입된 계정을 찾을 수 없습니다.");
            }
            
            // 인증 코드 검증
            EmailVerification verification = emailVerificationRepository
                    .findLatestUnverifiedByEmail(request.getEmail())
                    .orElse(null);
            
            if (verification == null) {
                return VerifyResetCodeResponse.failure("인증 코드를 찾을 수 없습니다. 다시 발송해주세요.");
            }
            
            if (!verification.isValidCode(request.getVerificationCode())) {
                return VerifyResetCodeResponse.failure("인증 코드가 올바르지 않거나 만료되었습니다.");
            }
            
            // 인증 완료 처리 (비밀번호 재설정용)
            verification.verify();
            emailVerificationRepository.save(verification);
            
            log.info("비밀번호 재설정 인증 코드 검증 완료: {}", request.getEmail());
            return VerifyResetCodeResponse.success(request.getEmail());
            
        } catch (Exception e) {
            log.error("비밀번호 재설정 인증 코드 검증 중 오류 발생: {}", e.getMessage(), e);
            return VerifyResetCodeResponse.failure("인증 코드 검증 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 비밀번호 재설정 처리 (인증 완료 후)
     * @param request 비밀번호 재설정 요청 정보
     * @return 처리 결과
     */
    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        log.info("비밀번호 재설정 요청: {}", request.getEmail());
        
        try {
            // 비밀번호 확인 일치 검증
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResetPasswordResponse.failure("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            }
            
            // 사용자 존재 확인
            User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                    .orElse(null);
            
            if (user == null) {
                return ResetPasswordResponse.failure("해당 이메일로 가입된 계정을 찾을 수 없습니다.");
            }
            
            // 인증 완료 상태 확인
            EmailVerification verification = emailVerificationRepository
                    .findLatestVerifiedByEmail(request.getEmail())
                    .orElse(null);
            
            if (verification == null) {
                return ResetPasswordResponse.failure("인증이 완료되지 않았습니다. 인증 코드를 먼저 확인해주세요.");
            }
            
            // 비밀번호 업데이트
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(encodedPassword);
            userRepository.save(user);
            
            // 인증 정보 삭제
            emailVerificationRepository.deleteByEmail(request.getEmail());
            
            log.info("비밀번호 재설정 완료: {}", request.getEmail());
            return ResetPasswordResponse.success(request.getEmail());
            
        } catch (Exception e) {
            log.error("비밀번호 재설정 중 오류 발생: {}", e.getMessage(), e);
            return ResetPasswordResponse.failure("비밀번호 재설정 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 6자리 인증 코드 생성
     * @return 인증 코드
     */
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    

    
    /**
     * 알림 설정 업데이트
     * @param request 알림 설정 요청
     * @return 처리 결과
     */
    @Transactional
    public Map<String, Object> updateNotificationSettings(NotificationSettingsRequest request) {
        try {
            // 현재 인증된 사용자 정보 가져오기
            String currentUserEmail = getCurrentUserEmail();
            User user = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            // 알림 설정 업데이트
            if (request.getNotificationEmail() != null) {
                user.setNotificationEmail(request.getNotificationEmail());
            }
            if (request.getNotificationPush() != null) {
                user.setNotificationPush(request.getNotificationPush());
            }
            if (request.getNotificationSchedule() != null) {
                user.setNotificationSchedule(request.getNotificationSchedule());
            }
            if (request.getNotificationComment() != null) {
                user.setNotificationComment(request.getNotificationComment());
            }
            
            userRepository.save(user);
            
            log.info("알림 설정 업데이트 완료: {}", currentUserEmail);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "알림 설정이 저장되었습니다.");
            result.put("data", Map.of(
                "userId", user.getId(),
                "notificationEmail", user.getNotificationEmail(),
                "notificationPush", user.getNotificationPush(),
                "notificationSchedule", user.getNotificationSchedule(),
                "notificationComment", user.getNotificationComment(),
                "updatedAt", user.getUpdatedAt()
            ));
            
            return result;
            
        } catch (Exception e) {
            log.error("알림 설정 업데이트 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("알림 설정 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    

} 