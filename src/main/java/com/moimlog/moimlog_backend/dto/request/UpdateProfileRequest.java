package com.moimlog.moimlog_backend.dto.request;

import com.moimlog.moimlog_backend.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;

/**
 * 사용자 프로필 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UpdateProfileRequest {
    
    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 100, message = "이름은 2자 이상 100자 이하여야 합니다.")
    private String name;
    
    @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
    private String nickname;
    
    @Size(max = 500, message = "프로필 이미지 URL은 500자 이하여야 합니다.")
    private String profileImage;
    
    @Size(max = 1000, message = "자기소개는 1000자 이하여야 합니다.")
    private String bio;
    
    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
    private String phone;
    
    private LocalDate birthDate;
    
    @Pattern(regexp = "^(MALE|FEMALE|)$", message = "성별은 MALE, FEMALE 중 하나이거나 비워둘 수 있습니다.")
    private String gender;
    

} 