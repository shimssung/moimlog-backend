package com.moimlog.moimlog_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moimlog.moimlog_backend.dto.request.SignupRequest;
import com.moimlog.moimlog_backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("회원가입 API 테스트 - 성공")
    void signup_Success() throws Exception {
        // given
        SignupRequest signupRequest = SignupRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("테스트 사용자")
                .nickname("테스트닉네임")
                .phone("010-1234-5678")
                .bio("테스트 소개글")
                .build();

        when(userService.signup(any(SignupRequest.class)))
                .thenReturn(com.moimlog.moimlog_backend.dto.response.SignupResponse.success(1L, "test@example.com", "테스트 사용자", "테스트닉네임"));

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("테스트 사용자"))
                .andExpect(jsonPath("$.nickname").value("테스트닉네임"))
                .andExpect(jsonPath("$.message").value("회원가입이 성공적으로 완료되었습니다."));
    }

    @Test
    @DisplayName("회원가입 API 테스트 - 실패 (중복 이메일)")
    void signup_Failure_DuplicateEmail() throws Exception {
        // given
        SignupRequest signupRequest = SignupRequest.builder()
                .email("existing@example.com")
                .password("password123")
                .name("테스트 사용자")
                .nickname("테스트닉네임")
                .build();

        when(userService.signup(any(SignupRequest.class)))
                .thenReturn(com.moimlog.moimlog_backend.dto.response.SignupResponse.failure("이미 가입된 이메일입니다."));

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));
    }

    @Test
    @DisplayName("이메일 중복 확인 API 테스트 - 중복되지 않은 이메일")
    void checkEmailDuplicate_False() throws Exception {
        // given
        String email = "new@example.com";
        when(userService.isEmailDuplicate(email)).thenReturn(false);

        // when & then
        mockMvc.perform(get("/auth/check-email")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.duplicate").value(false));
    }

    @Test
    @DisplayName("이메일 중복 확인 API 테스트 - 중복된 이메일")
    void checkEmailDuplicate_True() throws Exception {
        // given
        String email = "existing@example.com";
        when(userService.isEmailDuplicate(email)).thenReturn(true);

        // when & then
        mockMvc.perform(get("/auth/check-email")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.duplicate").value(true));
    }

    @Test
    @DisplayName("회원가입 API 테스트 - 잘못된 요청 형식")
    void signup_InvalidRequest() throws Exception {
        // given
        String invalidJson = "{\"email\": \"invalid-email\", \"password\": \"\"}";

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
} 