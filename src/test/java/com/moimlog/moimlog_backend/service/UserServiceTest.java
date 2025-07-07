package com.moimlog.moimlog_backend.service;

import com.moimlog.moimlog_backend.dto.request.SignupRequest;
import com.moimlog.moimlog_backend.dto.response.SignupResponse;
import com.moimlog.moimlog_backend.entity.User;
import com.moimlog.moimlog_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private SignupRequest signupRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        signupRequest = SignupRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("테스트 사용자")
                .nickname("테스트닉네임")
                .phone("010-1234-5678")
                .bio("테스트 소개글")
                .build();

        mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .name("테스트 사용자")
                .nickname("테스트닉네임")
                .phone("010-1234-5678")
                .bio("테스트 소개글")
                .isActive(true)
                .isVerified(false)
                .build();
    }

    @Test
    @DisplayName("정상적인 회원가입 테스트")
    void signup_Success() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // when
        SignupResponse response = userService.signup(signupRequest);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isEqualTo("테스트 사용자");
        assertThat(response.getNickname()).isEqualTo("테스트닉네임");
        assertThat(response.getMessage()).isEqualTo("회원가입이 성공적으로 완료되었습니다.");
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입 시도 시 실패 테스트")
    void signup_Failure_DuplicateEmail() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // when
        SignupResponse response = userService.signup(signupRequest);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("이미 가입된 이메일입니다.");
    }

    @Test
    @DisplayName("이메일 중복 확인 테스트 - 중복되지 않은 이메일")
    void isEmailDuplicate_False() {
        // given
        String email = "new@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // when
        boolean result = userService.isEmailDuplicate(email);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("이메일 중복 확인 테스트 - 중복된 이메일")
    void isEmailDuplicate_True() {
        // given
        String email = "existing@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // when
        boolean result = userService.isEmailDuplicate(email);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이메일로 사용자 조회 테스트 - 존재하는 사용자")
    void findByEmail_Success() {
        // given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(mockUser));

        // when
        User result = userService.findByEmail(email);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("이메일로 사용자 조회 테스트 - 존재하지 않는 사용자")
    void findByEmail_NotFound() {
        // given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.empty());

        // when
        User result = userService.findByEmail(email);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("ID로 사용자 조회 테스트 - 존재하는 사용자")
    void findById_Success() {
        // given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(mockUser));

        // when
        User result = userService.findById(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("ID로 사용자 조회 테스트 - 존재하지 않는 사용자")
    void findById_NotFound() {
        // given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // when
        User result = userService.findById(userId);

        // then
        assertThat(result).isNull();
    }
} 