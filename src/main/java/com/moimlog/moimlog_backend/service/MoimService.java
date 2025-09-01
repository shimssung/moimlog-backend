package com.moimlog.moimlog_backend.service;

import com.moimlog.moimlog_backend.dto.request.CreateMoimRequest;
import com.moimlog.moimlog_backend.dto.response.CreateMoimResponse;
import com.moimlog.moimlog_backend.entity.*;
import com.moimlog.moimlog_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
    private final UserFavoriteRepository userFavoriteRepository;
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
    
    /**
     * 내가 만든 모임 목록 조회 (페이지네이션 지원)
     */
    @Transactional(readOnly = true)
    public Page<Moim> getMyCreatedMoims(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return moimRepository.findByCreatedBy(user, pageable);
    }
    
    /**
     * 내가 참여한 모임 목록 조회 (페이지네이션 지원)
     */
    @Transactional(readOnly = true)
    public Page<MoimMember> getMyJoinedMoims(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "joinedAt"));
        return moimMemberRepository.findByUserId(user.getId(), pageable);
    }
    
    // ========================================
    // 🎯 모임 상세 페이지 관련 메서드
    // ========================================
    
    /**
     * 모임 참여
     */
    public MoimMember joinMoim(Long moimId, Long userId) {
        // 모임 존재 확인
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
        
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        // 이미 모임 멤버인지 확인
        if (moimMemberRepository.existsByMoimIdAndUserId(moimId, userId)) {
            throw new IllegalArgumentException("이미 참여 중인 모임입니다.");
        }
        
        // 모임 정원 확인
        if (moim.getMaxMembers() > 0 && moim.getCurrentMembers() >= moim.getMaxMembers()) {
            throw new IllegalArgumentException("모임 정원이 가득 찼습니다.");
        }
        
        // 모임 멤버 생성
        MoimMember member = MoimMember.createMoimMember(moim, user, MoimMember.Role.MEMBER);
        MoimMember savedMember = moimMemberRepository.save(member);
        
        // 모임 현재 멤버 수 증가
        moim.setCurrentMembers(moim.getCurrentMembers() + 1);
        moimRepository.save(moim);
        
        log.info("모임 참여 성공: moimId={}, userId={}", moimId, userId);
        
        return savedMember;
    }
    
    /**
     * 모임 탈퇴
     */
    public void leaveMoim(Long moimId, Long userId) {
        // 모임 멤버 조회
        MoimMember member = moimMemberRepository.findByMoimIdAndUserId(moimId, userId)
                .orElseThrow(() -> new IllegalArgumentException("모임 멤버가 아닙니다."));
        
        // 모임 생성자는 탈퇴할 수 없음
        Moim moim = member.getMoim();
        if (moim.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("모임 생성자는 탈퇴할 수 없습니다.");
        }
        
        // 모임 멤버 삭제
        moimMemberRepository.delete(member);
        
        // 모임 현재 멤버 수 감소
        moim.setCurrentMembers(moim.getCurrentMembers() - 1);
        moimRepository.save(moim);
        
        log.info("모임 탈퇴 성공: moimId={}, userId={}", moimId, userId);
    }
    
    /**
     * 모임 멤버 목록 조회
     */
    public Map<String, Object> getMoimMembers(Long moimId) {
        // 모임 존재 확인
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
        
        // 모임 멤버 목록 조회
        List<MoimMember> memberList = moimMemberRepository.findByMoimIdOrderByRoleAscJoinedAtAsc(moimId);
        
        // 멤버 정보 변환
        List<Map<String, Object>> memberResponses = memberList.stream()
                .map(this::convertToMemberResponse)
                .collect(Collectors.toList());
        
        // 통계 정보 생성
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalMembers", memberList.size());
        statistics.put("adminCount", (int) memberList.stream().filter(m -> m.getRole() == MoimMember.Role.ADMIN).count());
        statistics.put("moderatorCount", (int) memberList.stream().filter(m -> m.getRole() == MoimMember.Role.MODERATOR).count());
        statistics.put("memberCount", (int) memberList.stream().filter(m -> m.getRole() == MoimMember.Role.MEMBER).count());
        
        Map<String, Object> response = new HashMap<>();
        response.put("members", memberResponses);
        response.put("statistics", statistics);
        
        return response;
    }
    
    /**
     * 멤버 정보를 응답 DTO로 변환
     */
    private Map<String, Object> convertToMemberResponse(MoimMember member) {
        Map<String, Object> response = new HashMap<>();
        User user = member.getUser();
        
        response.put("id", member.getId());
        response.put("moimId", member.getMoim().getId());
        response.put("userId", user.getId());
        response.put("role", member.getRole());
        response.put("status", member.getStatus());
        response.put("joinedAt", member.getJoinedAt());
        response.put("lastActiveAt", member.getLastActiveAt());
        response.put("userName", user.getName());
        response.put("userEmail", user.getEmail());
        response.put("userProfileImage", user.getProfileImage());
        
        // 게시글 수와 댓글 수는 추후 구현 (PostService 연동 필요)
        response.put("postCount", 0);
        response.put("commentCount", 0);
        
        return response;
    }
    
    /**
     * 페이지네이션 정보 생성
     */
    private Map<String, Object> createPaginationInfo(Page<?> page) {
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", page.getNumber() + 1);
        pagination.put("totalPages", page.getTotalPages());
        pagination.put("totalCount", page.getTotalElements());
        pagination.put("hasNext", page.hasNext());
        pagination.put("hasPrev", page.hasPrevious());
        return pagination;
    }
    
    // ========================================
    // 🎯 모임 목록 조회 관련 메서드
    // ========================================
    
    /**
     * 전체 모임 목록 조회 (검색, 필터링, 정렬 지원)
     */
    public Map<String, Object> getMoimList(Long userId, int page, int limit, String category, 
                                          String onlineType, String location, String search, 
                                          String sortBy, Integer minMembers, Integer maxMembers, 
                                          Boolean isPrivate) {
        
        // 페이지네이션 설정
        Pageable pageable = PageRequest.of(page - 1, limit, createSortBy(sortBy));
        
        // 카테고리 이름을 ID로 변환
        Long categoryId = null;
        if (category != null && !category.trim().isEmpty()) {
            try {
                categoryId = Long.parseLong(category);
            } catch (NumberFormatException e) {
                // 카테고리 이름으로 검색하는 경우는 별도 처리 필요
                log.warn("카테고리 이름으로 검색하는 기능은 아직 구현되지 않았습니다: {}", category);
            }
        }
        
        // 모임 목록 조회
        Page<Moim> moimPage = moimRepository.findMoimsWithFilters(
                categoryId, onlineType, location, search, minMembers, maxMembers, isPrivate, pageable);
        
        // 사용자별 정보 추가 (멤버 여부, 즐겨찾기 여부)
        List<Map<String, Object>> moimResponses = moimPage.getContent().stream()
                .map(moim -> convertToMoimListResponse(moim, userId))
                .collect(Collectors.toList());
        
        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("moims", moimResponses);
        response.put("pagination", createPaginationInfo(moimPage));
        response.put("filters", createFiltersInfo(category, onlineType, location, search, minMembers, maxMembers, isPrivate));
        
        return response;
    }
    
    /**
     * 인기 모임 목록 조회
     */
    public Map<String, Object> getPopularMoims(String category, int limit) {
        List<Moim> popularMoims;
        
        if (category != null && !category.trim().isEmpty()) {
            try {
                Long categoryId = Long.parseLong(category);
                popularMoims = moimRepository.findPopularMoimsByCategory(categoryId, PageRequest.of(0, limit));
            } catch (NumberFormatException e) {
                // 카테고리 이름으로 검색하는 경우는 별도 처리 필요
                log.warn("카테고리 이름으로 검색하는 기능은 아직 구현되지 않았습니다: {}", category);
                popularMoims = moimRepository.findPopularMoims(limit);
            }
        } else {
            popularMoims = moimRepository.findPopularMoims(limit);
        }
        
        List<Map<String, Object>> moimResponses = popularMoims.stream()
                .map(moim -> convertToMoimListResponse(moim, null))
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("moims", moimResponses);
        response.put("totalCount", moimResponses.size());
        
        return response;
    }
    
    /**
     * 최신 모임 목록 조회
     */
    public Map<String, Object> getLatestMoims(String category, int limit) {
        List<Moim> latestMoims;
        
        if (category != null && !category.trim().isEmpty()) {
            try {
                Long categoryId = Long.parseLong(category);
                latestMoims = moimRepository.findLatestMoimsByCategory(categoryId, PageRequest.of(0, limit));
            } catch (NumberFormatException e) {
                // 카테고리 이름으로 검색하는 기능은 별도 처리 필요
                log.warn("카테고리 이름으로 검색하는 기능은 아직 구현되지 않았습니다: {}", category);
                latestMoims = moimRepository.findLatestMoims(limit);
            }
        } else {
            latestMoims = moimRepository.findLatestMoims(limit);
        }
        
        List<Map<String, Object>> moimResponses = latestMoims.stream()
                .map(moim -> convertToMoimListResponse(moim, null))
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("moims", moimResponses);
        response.put("totalCount", moimResponses.size());
        
        return response;
    }
    
    /**
     * 모임 카테고리 목록 조회 (모임 수 포함)
     */
    public List<Map<String, Object>> getMoimCategories() {
        List<MoimCategory> categories = moimCategoryRepository.findActiveCategoriesWithMoimCount();
        
        return categories.stream()
                .map(category -> {
                    Map<String, Object> response = convertToCategoryResponse(category);
                    // 각 카테고리별 모임 수 조회
                    Long moimCount = moimRepository.countByCategoryIdAndIsActiveTrue(category.getId());
                    response.put("moimCount", moimCount);
                    return response;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 정렬 기준에 따른 Sort 객체 생성
     */
    private Sort createSortBy(String sortBy) {
        if (sortBy == null) {
            sortBy = "latest";
        }
        
        switch (sortBy.toLowerCase()) {
            case "popular":
                return Sort.by(Sort.Direction.DESC, "currentMembers", "createdAt");
            case "name":
                return Sort.by(Sort.Direction.ASC, "title");
            case "category":
                return Sort.by(Sort.Direction.ASC, "category.name", "createdAt");
            case "location":
                return Sort.by(Sort.Direction.ASC, "location", "createdAt");
            case "latest":
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }
    
    /**
     * 모임 목록 응답용 DTO 변환
     */
    private Map<String, Object> convertToMoimListResponse(Moim moim, Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("id", moim.getId());
        response.put("title", moim.getTitle());
        response.put("description", moim.getDescription());
        response.put("categoryId", moim.getCategory() != null ? moim.getCategory().getId() : null);
        response.put("categoryName", moim.getCategory() != null ? moim.getCategory().getName() : null);
        response.put("categoryLabel", moim.getCategory() != null ? moim.getCategory().getLabel() : null);
        response.put("categoryColor", moim.getCategory() != null ? moim.getCategory().getColor() : null);
        response.put("tags", moim.getTagsAsList());
        response.put("thumbnail", moim.getThumbnail());
        response.put("maxMembers", moim.getMaxMembers());
        response.put("currentMembers", moim.getCurrentMembers());
        response.put("isPrivate", moim.getIsPrivate());
        response.put("isActive", moim.getIsActive());
        response.put("onlineType", moim.getOnlineType());
        response.put("location", moim.getLocation());
        response.put("locationDetail", moim.getLocationDetail());
        response.put("createdBy", moim.getCreatedBy().getNickname() != null ? moim.getCreatedBy().getNickname() : moim.getCreatedBy().getName());
        response.put("creatorName", moim.getCreatedBy().getName());
        response.put("creatorProfileImage", moim.getCreatedBy().getProfileImage());
        response.put("createdAt", moim.getCreatedAt());
        response.put("updatedAt", moim.getUpdatedAt());
        
        // 사용자별 정보 (로그인한 경우만)
        if (userId != null) {
            response.put("isMember", moimMemberRepository.existsByMoimIdAndUserId(moim.getId(), userId));
            response.put("isFavorite", userFavoriteRepository.existsByUserIdAndMoimId(userId, moim.getId()));
            
            // 모임 멤버인 경우 역할 정보
            Optional<MoimMember> member = moimMemberRepository.findByMoimIdAndUserId(moim.getId(), userId);
            response.put("userRole", member.map(MoimMember::getRole).orElse(null));
        } else {
            response.put("isMember", false);
            response.put("isFavorite", false);
            response.put("userRole", null);
        }
        
        return response;
    }
    
    /**
     * 카테고리 응답용 DTO 변환
     */
    private Map<String, Object> convertToCategoryResponse(MoimCategory category) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("id", category.getId());
        response.put("name", category.getName());
        response.put("label", category.getLabel());
        response.put("description", category.getDescription());
        response.put("color", category.getColor());
        response.put("isActive", category.getIsActive());
        
        return response;
    }
    
    /**
     * 필터 정보 생성
     */
    private Map<String, Object> createFiltersInfo(String category, String onlineType, String location, 
                                                 String search, Integer minMembers, Integer maxMembers, Boolean isPrivate) {
        Map<String, Object> filters = new HashMap<>();
        
        Map<String, Object> appliedFilters = new HashMap<>();
        if (category != null) appliedFilters.put("category", category);
        if (onlineType != null) appliedFilters.put("onlineType", onlineType);
        if (location != null) appliedFilters.put("location", location);
        if (search != null) appliedFilters.put("search", search);
        if (minMembers != null) appliedFilters.put("minMembers", minMembers);
        if (maxMembers != null) appliedFilters.put("maxMembers", maxMembers);
        if (isPrivate != null) appliedFilters.put("isPrivate", isPrivate);
        
        filters.put("appliedFilters", appliedFilters);
        
        // 사용 가능한 카테고리 목록 (모임 수 포함)
        filters.put("availableCategories", getMoimCategories());
        
        return filters;
    }
    
}
