package com.moimlog.moimlog_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 온보딩 요청 DTO
 * 사용자의 온보딩 정보를 받기 위한 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRequest {
    
    /**
     * 닉네임
     */
    private String nickname;
    
    /**
     * 자기소개 (선택사항)
     */
    private String bio;
    
    /**
     * 모임 카테고리 목록
     */
    private List<String> moimCategories;
    
    /**
     * 프로필 이미지 (Base64 인코딩된 문자열, 선택사항)
     */
    private String profileImage;
} 