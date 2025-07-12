package com.moimlog.moimlog_backend.dto.response;

import com.moimlog.moimlog_backend.entity.User;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 프로필 조회 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserProfileResponse {
    
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String profileImage;
    private String bio;
    private String phone;
    private LocalDate birthDate;
    private User.Gender gender;
    private boolean isVerified;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    
    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .bio(user.getBio())
                .phone(user.getPhone())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .isVerified(user.getIsVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
} 