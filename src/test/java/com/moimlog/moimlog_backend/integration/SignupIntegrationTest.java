package com.moimlog.moimlog_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moimlog.moimlog_backend.dto.request.SignupRequest;
import com.moimlog.moimlog_backend.entity.User;
import com.moimlog.moimlog_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class SignupIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 통합 테스트 - 성공")
    void signup_Success() throws Exception {
        // given
        SignupRequest signupRequest = SignupRequest.builder()
                .email("integration@example.com")
                .password("password123")
                .name("통합테스트 사용자")
                .nickname("통합테스트닉네임")
                .phone("010-9876-5432")
                .bio("통합 테스트 소개글")
                .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.name").value("통합테스트 사용자"))
                .andExpect(jsonPath("$.nickname").value("통합테스트닉네임"))
                .andExpect(jsonPath("$.message").value("회원가입이 성공적으로 완료되었습니다."));

        // 데이터베이스에 실제로 저장되었는지 확인
        User savedUser = userRepository.findByEmail("integration@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("통합테스트 사용자");
        assertThat(savedUser.getNickname()).isEqualTo("통합테스트닉네임");
        assertThat(savedUser.getPhone()).isEqualTo("010-9876-5432");
        assertThat(savedUser.getBio()).isEqualTo("통합 테스트 소개글");
        assertThat(savedUser.getIsActive()).isTrue();
        assertThat(savedUser.getIsVerified()).isFalse();
    }

    @Test
    @DisplayName("회원가입 통합 테스트 - 중복 이메일 실패")
    void signup_Failure_DuplicateEmail() throws Exception {
        // given - 먼저 사용자 생성
        User existingUser = User.createUser(
                "duplicate@example.com",
                "encodedPassword",
                "기존 사용자",
                "기존닉네임"
        );
        userRepository.save(existingUser);

        // 중복 이메일로 회원가입 시도
        SignupRequest signupRequest = SignupRequest.builder()
                .email("duplicate@example.com")
                .password("password123")
                .name("새로운 사용자")
                .nickname("새로운닉네임")
                .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));

        // 기존 사용자는 그대로 유지되는지 확인
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("이메일 중복 확인 통합 테스트 - 중복되지 않은 이메일")
    void checkEmailDuplicate_False() throws Exception {
        // given
        String email = "new@example.com";

        // when & then
        mockMvc.perform(get("/auth/check-email")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.duplicate").value(false));
    }

    @Test
    @DisplayName("이메일 중복 확인 통합 테스트 - 중복된 이메일")
    void checkEmailDuplicate_True() throws Exception {
        // given
        String email = "existing@example.com";
        User existingUser = User.createUser(
                email,
                "encodedPassword",
                "기존 사용자",
                "기존닉네임"
        );
        userRepository.save(existingUser);

        // when & then
        mockMvc.perform(get("/auth/check-email")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.duplicate").value(true));
    }

    @Test
    @DisplayName("회원가입 통합 테스트 - 필수 필드 누락")
    void signup_Failure_MissingRequiredFields() throws Exception {
        // given
        SignupRequest signupRequest = SignupRequest.builder()
                .email("test@example.com")
                // password, name 누락
                .nickname("테스트닉네임")
                .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 통합 테스트 - 선택적 필드만 있는 경우")
    void signup_Success_OptionalFieldsOnly() throws Exception {
        // given
        SignupRequest signupRequest = SignupRequest.builder()
                .email("optional@example.com")
                .password("password123")
                .name("선택적필드 사용자")
                // nickname, phone, bio는 선택적 필드
                .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.email").value("optional@example.com"))
                .andExpect(jsonPath("$.name").value("선택적필드 사용자"));

        // 데이터베이스에 저장된 사용자 확인
        User savedUser = userRepository.findByEmail("optional@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getNickname()).isEqualTo("선택적필드 사용자"); // name과 동일하게 설정됨
        assertThat(savedUser.getPhone()).isNull();
        assertThat(savedUser.getBio()).isNull();
    }
} 