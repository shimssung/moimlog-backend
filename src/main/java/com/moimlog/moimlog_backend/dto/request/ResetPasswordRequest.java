package com.moimlog.moimlog_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 재설정 요청 DTO
 * 인증 코드 검증이 완료된 후 새 비밀번호를 설정하는 용도
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
             message = "비밀번호는 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String newPassword;
    
    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String confirmPassword;
} 