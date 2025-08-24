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
    private boolean isOnboardingCompleted;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    private Boolean notificationEmail;
    private Boolean notificationPush;
    private Boolean notificationSchedule;
    private Boolean notificationComment;
    
    public static UserProfileResponse from(User user) {
        // S3 URL을 프록시 URL로 변환
        String proxyImageUrl = convertS3UrlToProxyUrl(user.getProfileImage());
        
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .profileImage(proxyImageUrl) // 프록시 URL 사용
                .bio(user.getBio())
                .phone(user.getPhone())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .isVerified(user.getIsVerified())
                .isOnboardingCompleted(user.getIsOnboardingCompleted())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())

                .notificationEmail(user.getNotificationEmail())
                .notificationPush(user.getNotificationPush())
                .notificationSchedule(user.getNotificationSchedule())
                .notificationComment(user.getNotificationComment())
                .build();
    }
    
    /**
     * S3 URL을 백엔드 프록시 URL로 변환
     * @param s3Url S3 URL
     * @return 프록시 URL 또는 null
     */
    private static String convertS3UrlToProxyUrl(String s3Url) {
        if (s3Url == null || s3Url.isEmpty()) {
            return null;
        }
        
        // S3 URL에서 파일명 추출
        String[] urlParts = s3Url.split("/");
        String fileName = urlParts[urlParts.length - 1];
        
        // 백엔드 프록시 URL로 변환
        return "http://localhost:8080/moimlog/auth/profile-image/" + fileName;
    }
} 