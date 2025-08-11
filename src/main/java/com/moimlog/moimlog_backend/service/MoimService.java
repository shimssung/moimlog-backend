package com.moimlog.moimlog_backend.service;

import com.moimlog.moimlog_backend.dto.request.CreateMoimRequest;
import com.moimlog.moimlog_backend.dto.response.CreateMoimResponse;
import com.moimlog.moimlog_backend.entity.*;
import com.moimlog.moimlog_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 모임 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MoimService {
    
    private final MoimRepository moimRepository;
    private final MoimCategoryRepository moimCategoryRepository;
    private final MoimMemberRepository moimMemberRepository;
    private final MoimSettingsRepository moimSettingsRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    
    /**
     * 모임 생성
     * @param request 모임 생성 요청
     * @param userId 생성자 사용자 ID
     * @return 생성된 모임 정보
     */
    public CreateMoimResponse createMoim(CreateMoimRequest request, Long userId) {
        log.info("모임 생성 시작: title={}, userId={}", request.getTitle(), userId);
        
        try {
            // 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            // 카테고리 조회
            MoimCategory category = moimCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
            
            // 온라인 타입 변환
            Moim.OnlineType onlineType = convertToOnlineType(request.getOnlineType());
            
            // 썸네일 이미지 처리 (Base64 → S3 업로드)
            String thumbnailUrl = null;
            if (request.getThumbnail() != null && !request.getThumbnail().trim().isEmpty()) {
                try {
                    if (request.getThumbnail().startsWith("data:image/")) {
                        // Base64 이미지를 S3에 업로드
                        String fileExtension = getFileExtensionFromBase64(request.getThumbnail());
                        thumbnailUrl = s3Service.uploadBase64Image(request.getThumbnail(), fileExtension);
                        log.info("썸네일 S3 업로드 완료: {}", thumbnailUrl);
                    } else if (request.getThumbnail().startsWith("http")) {
                        // 이미 URL인 경우 그대로 사용
                        thumbnailUrl = request.getThumbnail();
                    }
                } catch (Exception e) {
                    log.error("썸네일 업로드 실패: {}", e.getMessage(), e);
                    // 썸네일 업로드 실패 시에도 모임 생성은 계속 진행
                }
            }
            
            // 모임 생성
            Moim moim = Moim.createMoim(
                    request.getCleanedTitle(),
                    request.getCleanedDescription(),
                    category,
                    request.getCleanedTags(),
                    thumbnailUrl,  // S3 URL 또는 null
                    request.getMaxMembers(),
                    request.getIsPrivate(),
                    onlineType,
                    request.getCleanedLocation(),
                    request.getCleanedLocationDetail(),
                    user
            );
            
            // 모임 저장
            Moim savedMoim = moimRepository.save(moim);
            
            // 모임 생성자를 admin 역할로 추가
            MoimMember member = MoimMember.createMoimMember(
                    savedMoim, user, MoimMember.Role.ADMIN);
            moimMemberRepository.save(member);
            
            // 모임 기본 설정 생성
            MoimSettings settings = MoimSettings.createDefaultSettings(savedMoim);
            moimSettingsRepository.save(settings);
            
            log.info("모임 생성 완료: moimId={}, title={}", savedMoim.getId(), savedMoim.getTitle());
            
            // 응답 생성
            return CreateMoimResponse.success(
                    savedMoim.getId(),
                    savedMoim.getTitle(),
                    savedMoim.getDescription(),
                    savedMoim.getCategory().getId(),
                    CreateMoimResponse.CategoryInfo.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .label(category.getLabel())
                            .color(category.getColor())
                            .build(),
                    savedMoim.getMaxMembers(),
                    savedMoim.getCurrentMembers(),
                    savedMoim.getTagsAsList(),
                    savedMoim.getThumbnail(),
                    savedMoim.getIsPrivate(),
                    savedMoim.getOnlineType().name().toLowerCase(),
                    savedMoim.getLocation(),
                    savedMoim.getLocationDetail(),
                    CreateMoimResponse.CreatorInfo.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .profileImage(user.getProfileImage())
                            .build(),
                    savedMoim.getCreatedAt(),
                    savedMoim.getUpdatedAt()
            );
            
        } catch (Exception e) {
            log.error("모임 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("모임 생성에 실패했습니다.", e);
        }
    }
    
    /**
     * 온라인 타입 문자열을 enum으로 변환
     */
    private Moim.OnlineType convertToOnlineType(String onlineType) {
        return switch (onlineType.toLowerCase()) {
            case "online" -> Moim.OnlineType.ONLINE;
            case "offline" -> Moim.OnlineType.OFFLINE;
            case "hybrid" -> Moim.OnlineType.HYBRID;
            default -> throw new IllegalArgumentException("올바르지 않은 온라인 타입입니다: " + onlineType);
        };
    }
    
    /**
     * Base64 이미지에서 파일 확장자 추출
     */
    private String getFileExtensionFromBase64(String base64Image) {
        if (base64Image == null || !base64Image.contains(",")) {
            return ".jpg"; // 기본값
        }
        
        String header = base64Image.split(",")[0];
        if (header.contains("image/jpeg") || header.contains("image/jpg")) {
            return ".jpg";
        } else if (header.contains("image/png")) {
            return ".png";
        } else if (header.contains("image/gif")) {
            return ".gif";
        } else if (header.contains("image/webp")) {
            return ".webp";
        } else {
            return ".jpg"; // 기본값
        }
    }
    
    /**
     * 모임 ID로 모임 조회
     */
    @Transactional(readOnly = true)
    public Moim getMoimById(Long moimId) {
        return moimRepository.findById(moimId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
    }
    
    /**
     * 사용자가 생성한 모임 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Moim> getMoimsByCreator(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return moimRepository.findByCreatedBy(user);
    }
    
    /**
     * 사용자가 참여 중인 모임 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Moim> getMoimsByMember(Long userId) {
        List<MoimMember> memberships = moimMemberRepository.findByUserId(userId);
        return memberships.stream()
                .map(MoimMember::getMoim)
                .toList();
    }
    
    /**
     * 모임 참여 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isUserMemberOfMoim(Long moimId, Long userId) {
        return moimMemberRepository.existsByMoimIdAndUserId(moimId, userId);
    }
    
    /**
     * 모임 관리자 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isUserAdminOfMoim(Long moimId, Long userId) {
        return moimMemberRepository.existsByMoimIdAndUserIdAndRole(moimId, userId, MoimMember.Role.ADMIN);
    }
    
    /**
     * 모임 모더레이터 이상 권한 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isUserModeratorOrHigherOfMoim(Long moimId, Long userId) {
        return moimMemberRepository.isModeratorOrHigher(moimId, userId);
    }
}
