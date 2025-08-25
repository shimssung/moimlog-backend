package com.moimlog.moimlog_backend.controller;

import com.moimlog.moimlog_backend.dto.common.ApiResponse;
import com.moimlog.moimlog_backend.dto.request.CreateMoimRequest;
import com.moimlog.moimlog_backend.dto.request.CreatePostRequest;
import com.moimlog.moimlog_backend.dto.request.CreateCommentRequest;
import com.moimlog.moimlog_backend.dto.request.CreateScheduleRequest;
import com.moimlog.moimlog_backend.dto.request.ScheduleParticipationRequest;
import com.moimlog.moimlog_backend.dto.request.CreateJoinRequestRequest;
import com.moimlog.moimlog_backend.dto.request.ApproveJoinRequestRequest;
import com.moimlog.moimlog_backend.dto.request.RejectJoinRequestRequest;
import com.moimlog.moimlog_backend.dto.response.CreateMoimResponse;
import com.moimlog.moimlog_backend.dto.response.PostResponse;
import com.moimlog.moimlog_backend.dto.response.PostListResponse;
import com.moimlog.moimlog_backend.service.MoimService;
import com.moimlog.moimlog_backend.service.PostService;
import com.moimlog.moimlog_backend.service.ScheduleService;
import com.moimlog.moimlog_backend.service.MoimJoinRequestService;
import com.moimlog.moimlog_backend.util.JwtUtil;
import com.moimlog.moimlog_backend.entity.MoimCategory;
import com.moimlog.moimlog_backend.entity.Moim;
import com.moimlog.moimlog_backend.entity.MoimMember;
import com.moimlog.moimlog_backend.entity.Post;
import com.moimlog.moimlog_backend.entity.Schedule;
import com.moimlog.moimlog_backend.repository.MoimCategoryRepository;
import com.moimlog.moimlog_backend.repository.MoimRepository;
import com.moimlog.moimlog_backend.repository.MoimMemberRepository;
import com.moimlog.moimlog_backend.repository.PostRepository;
import com.moimlog.moimlog_backend.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.moimlog.moimlog_backend.entity.Moim;
import com.moimlog.moimlog_backend.entity.MoimMember;

/**
 * 모임 관련 API를 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/moims")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MoimController {
    
    private final MoimService moimService;
    private final PostService postService;
    private final ScheduleService scheduleService;
    private final MoimJoinRequestService moimJoinRequestService;
    private final JwtUtil jwtUtil;
    private final MoimCategoryRepository moimCategoryRepository;
    private final MoimRepository moimRepository;
    private final MoimMemberRepository moimMemberRepository;
    private final PostRepository postRepository;
    private final ScheduleRepository scheduleRepository;
    
    /**
     * 모임 생성 API
     * POST /api/v1/moims
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateMoimResponse>> createMoim(
            @Valid @RequestBody CreateMoimRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        
        log.info("모임 생성 API 호출: title={}", request.getTitle());
        log.info("전송된 데이터 상세: {}", request);
        log.info("=== 필드별 상세 데이터 ===");
        log.info("title: '{}'", request.getTitle());
        log.info("description: '{}'", request.getDescription());
        log.info("categoryId: {}", request.getCategoryId());
        log.info("maxMembers: {}", request.getMaxMembers());
        log.info("tags: {}", request.getTags());
        log.info("thumbnail: '{}'", request.getThumbnail());
        log.info("isPrivate: {}", request.getIsPrivate());
        log.info("onlineType: '{}'", request.getOnlineType());
        log.info("location: '{}'", request.getLocation());
        log.info("locationDetail: '{}'", request.getLocationDetail());
        log.info("========================");
        
        // 유효성 검사 오류 확인
        if (bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            List<String> errorMessages = fieldErrors.stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            
            log.warn("모임 생성 유효성 검사 실패: {}", errorMessages);
            
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<CreateMoimResponse>builder()
                            .success(false)
                            .message("입력 정보가 올바르지 않습니다.")
                            .errors(errorMessages)
                            .build());
        }
        
        try {
            // JWT 토큰에서 사용자 ID 추출
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 모임 생성
            CreateMoimResponse response = moimService.createMoim(request, userId);
            
            log.info("모임 생성 성공: moimId={}", response.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<CreateMoimResponse>builder()
                            .success(true)
                            .message("모임이 성공적으로 생성되었습니다.")
                            .data(response)
                            .build());
            
        } catch (IllegalArgumentException e) {
            log.warn("모임 생성 실패 (잘못된 요청): {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<CreateMoimResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
            
        } catch (Exception e) {
            log.error("모임 생성 중 서버 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<CreateMoimResponse>builder()
                            .success(false)
                            .message("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * HTTP 요청에서 JWT 토큰을 추출하는 메서드
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new IllegalArgumentException("유효한 인증 토큰이 없습니다.");
    }
    
    /**
     * HTTP 요청에서 JWT 토큰을 추출하는 메서드 (토큰이 없어도 null 반환)
     */
    private String extractTokenFromRequestOptional(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * 전체 모임 목록 조회 (검색, 필터링, 정렬 지원)
     * GET /moims
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMoimList(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String onlineType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            @RequestParam(required = false) Integer minMembers,
            @RequestParam(required = false) Integer maxMembers,
            @RequestParam(required = false, defaultValue = "false") Boolean isPrivate,
            HttpServletRequest httpRequest) {
        
        try {
            // 파라미터 검증
            if (page < 1 || page > 1000) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("페이지 번호는 1-1000 사이여야 합니다.")
                                .build());
            }
            
            if (limit < 1 || limit > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("페이지당 모임 수는 1-100 사이여야 합니다.")
                                .build());
            }
            
            // 정렬 기준 검증
            String[] allowedSortBy = {"latest", "popular", "name", "category", "location"};
            if (!Arrays.asList(allowedSortBy).contains(sortBy.toLowerCase())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("잘못된 정렬 기준입니다.")
                                .build());
            }
            
            // 사용자 ID 추출 (로그인한 경우만)
            Long userId = null;
            try {
                String token = extractTokenFromRequestOptional(httpRequest);
                if (token != null) {
                    userId = jwtUtil.getUserIdFromToken(token);
                }
            } catch (Exception e) {
                // 토큰이 유효하지 않은 경우 비로그인 사용자로 처리
                log.debug("유효하지 않은 토큰으로 인한 비로그인 사용자 모임 목록 조회");
            }
            
            // 모임 목록 조회
            Map<String, Object> response = moimService.getMoimList(
                    userId, page, limit, category, onlineType, location, 
                    search, sortBy, minMembers, maxMembers, isPrivate);
            
            // 모임 목록의 이미지 URL을 프록시 URL로 변환
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> moims = (List<Map<String, Object>>) response.get("moims");
            if (moims != null) {
                for (Map<String, Object> moim : moims) {
                    convertMoimListUrlsToProxy(moim);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("모임 목록을 성공적으로 조회했습니다.")
                    .data(response)
                    .build());
                    
        } catch (Exception e) {
            log.error("모임 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("모임 목록 조회 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * 인기 모임 목록 조회
     * GET /moims/popular
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPopularMoims(
            @RequestParam(required = false, defaultValue = "6") int limit,
            @RequestParam(required = false) String category) {
        
        try {
            if (limit < 1 || limit > 50) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("조회할 모임 수는 1-50 사이여야 합니다.")
                                .build());
            }
            
            Map<String, Object> response = moimService.getPopularMoims(category, limit);
            
            // 모임 목록의 이미지 URL을 프록시 URL로 변환
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> moims = (List<Map<String, Object>>) response.get("moims");
            if (moims != null) {
                for (Map<String, Object> moim : moims) {
                    convertMoimListUrlsToProxy(moim);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("인기 모임 목록을 성공적으로 조회했습니다.")
                    .data(response)
                    .build());
                    
        } catch (Exception e) {
            log.error("인기 모임 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("인기 모임 목록 조회 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * 최신 모임 목록 조회
     * GET /moims/latest
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLatestMoims(
            @RequestParam(required = false, defaultValue = "6") int limit,
            @RequestParam(required = false) String category) {
        
        try {
            if (limit < 1 || limit > 50) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("조회할 모임 수는 1-50 사이여야 합니다.")
                                .build());
            }
            
            Map<String, Object> response = moimService.getLatestMoims(category, limit);
            
            // 모임 목록의 이미지 URL을 프록시 URL로 변환
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> moims = (List<Map<String, Object>>) response.get("moims");
            if (moims != null) {
                for (Map<String, Object> moim : moims) {
                    convertMoimListUrlsToProxy(moim);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("최신 모임 목록을 성공적으로 조회했습니다.")
                    .data(response)
                    .build());
                    
        } catch (Exception e) {
            log.error("최신 모임 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("최신 모임 목록 조회 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * 모임 카테고리 목록 조회 API (모임 수 포함)
     * GET /moims/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMoimCategories() {
        try {
            List<Map<String, Object>> categories = moimService.getMoimCategories();
            
            return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                    .success(true)
                    .message("모임 카테고리 조회 성공")
                    .data(categories)
                    .build());
                    
        } catch (Exception e) {
            log.error("모임 카테고리 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<Map<String, Object>>>builder()
                            .success(false)
                            .message("카테고리 조회에 실패했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * 모임 생성 API 헬스체크
     * GET /api/v1/moims/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Moim API is running");
    }
    
    /**
     * 내가 만든 모임 목록 조회
     * GET /moims/my-created
     */
    @GetMapping("/my-created")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyCreatedMoims(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            Page<Moim> moimPage = moimService.getMyCreatedMoims(userId, page - 1, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("moims", moimPage.getContent().stream()
                    .map(this::convertToMoimResponse)
                    .collect(Collectors.toList()));
            response.put("pagination", createPaginationInfo(moimPage));
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("내가 만든 모임 목록 조회 성공")
                    .data(response)
                    .build());
        } catch (Exception e) {
            log.error("내가 만든 모임 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("모임 목록 조회 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * 내가 참여한 모임 목록 조회
     * GET /moims/my-joined
     */
    @GetMapping("/my-joined")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyJoinedMoims(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            Page<MoimMember> memberPage = moimService.getMyJoinedMoims(userId, page - 1, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("moims", memberPage.getContent().stream()
                    .map(this::convertToJoinedMoimResponse)
                    .collect(Collectors.toList()));
            response.put("pagination", createPaginationInfo(memberPage));
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("내가 참여한 모임 목록 조회 성공")
                    .data(response)
                    .build());
        } catch (Exception e) {
            log.error("내가 참여한 모임 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("모임 목록 조회 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * 페이지네이션 정보 생성
     */
    private Map<String, Object> createPaginationInfo(Page<?> page) {
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current_page", page.getNumber() + 1);
        pagination.put("total_pages", page.getTotalPages());
        pagination.put("total_elements", page.getTotalElements());
        pagination.put("size", page.getSize());
        return pagination;
    }
    
    /**
     * 모임 정보를 응답 형식으로 변환
     */
    private Map<String, Object> convertToMoimResponse(Moim moim) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", moim.getId());
        response.put("title", moim.getTitle());
        response.put("description", moim.getDescription());
        response.put("category_id", moim.getCategory() != null ? moim.getCategory().getId() : null);
        response.put("category_name", moim.getCategory() != null ? moim.getCategory().getName() : null);
        response.put("category_label", moim.getCategory() != null ? moim.getCategory().getLabel() : null);
        
        // 썸네일을 백엔드 프록시 URL로 변환
        response.put("thumbnail", convertS3UrlToProxyUrl(moim.getThumbnail()));
        
        response.put("max_members", moim.getMaxMembers());
        response.put("current_members", moim.getCurrentMembers());
        response.put("is_private", moim.getIsPrivate());
        response.put("online_type", moim.getOnlineType());
        response.put("location", moim.getLocation());
        response.put("location_detail", moim.getLocationDetail());
        response.put("tags", moim.getTags());
        response.put("created_at", moim.getCreatedAt());
        response.put("updated_at", moim.getUpdatedAt());
        
        return response;
    }
    
    /**
     * S3 URL을 백엔드 프록시 URL로 변환
     * @param s3Url S3 URL
     * @return 프록시 URL 또는 null
     */
    private String convertS3UrlToProxyUrl(String s3Url) {
        if (s3Url == null || s3Url.isEmpty()) {
            return null;
        }
        
        // 디버깅을 위한 로그 추가
        log.info("원본 S3 URL: {}", s3Url);
        
        // S3 URL에서 파일명 추출
        String[] urlParts = s3Url.split("/");
        String fileName = urlParts[urlParts.length - 1];
        
        log.info("추출된 파일명: {}", fileName);
        
        // 백엔드 프록시 URL로 변환 (모임 썸네일용)
        String proxyUrl = "http://localhost:8080/moimlog/auth/moim-thumbnail/" + fileName;
        log.info("생성된 프록시 URL: {}", proxyUrl);
        
        return proxyUrl;
    }
    
    /**
     * 참여한 모임 정보를 응답 형식으로 변환
     */
    private Map<String, Object> convertToJoinedMoimResponse(MoimMember member) {
        Map<String, Object> response = new HashMap<>();
        Moim moim = member.getMoim();
        
        response.put("id", moim.getId());
        response.put("title", moim.getTitle());
        response.put("description", moim.getDescription());
        response.put("category_id", moim.getCategory() != null ? moim.getCategory().getId() : null);
        response.put("category_name", moim.getCategory() != null ? moim.getCategory().getName() : null);
        response.put("category_label", moim.getCategory() != null ? moim.getCategory().getLabel() : null);
        
        // 썸네일을 백엔드 프록시 URL로 변환
        response.put("thumbnail", convertS3UrlToProxyUrl(moim.getThumbnail()));
        
        response.put("max_members", moim.getMaxMembers());
        response.put("current_members", moim.getCurrentMembers());
        response.put("is_private", moim.getIsPrivate());
        response.put("online_type", moim.getOnlineType());
        response.put("location", moim.getLocation());
        response.put("location_detail", moim.getLocationDetail());
        response.put("tags", moim.getTags());
        response.put("role", member.getRole());
        response.put("status", member.getStatus());
        response.put("joined_at", member.getJoinedAt());
        response.put("created_at", moim.getCreatedAt());
        response.put("updated_at", moim.getUpdatedAt());
        
        return response;
    }
    
    /**
     * 모임 목록 응답에서 S3 URL을 프록시 URL로 변환
     */
    private void convertMoimListUrlsToProxy(Map<String, Object> moimResponse) {
        // 썸네일 URL 변환
        if (moimResponse.containsKey("thumbnail")) {
            moimResponse.put("thumbnail", convertS3UrlToProxyUrl((String) moimResponse.get("thumbnail")));
        }
        
        // 생성자 프로필 이미지 URL 변환
        if (moimResponse.containsKey("creatorProfileImage")) {
            moimResponse.put("creatorProfileImage", convertS3UrlToProxyUrl((String) moimResponse.get("creatorProfileImage")));
        }
    }
    
    // ========================================
    // 🎯 1단계: 모임 상세 페이지 기본 구조
    // ========================================
    
    /**
     * 모임 상세 정보 조회
     * GET /moims/{moimId}
     */
    @GetMapping("/{moimId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMoimDetail(
            @PathVariable Long moimId,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 모임 정보 조회
            Moim moim = moimRepository.findById(moimId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
            
            // 현재 사용자가 모임 멤버인지 확인
            MoimMember member = moimMemberRepository.findByMoimIdAndUserId(moimId, userId)
                    .orElse(null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", moim.getId());
            response.put("title", moim.getTitle());
            response.put("description", moim.getDescription());
            response.put("categoryId", moim.getCategory() != null ? moim.getCategory().getId() : null);
            response.put("categoryName", moim.getCategory() != null ? moim.getCategory().getName() : null);
            response.put("categoryLabel", moim.getCategory() != null ? moim.getCategory().getLabel() : null);
            response.put("categoryColor", moim.getCategory() != null ? moim.getCategory().getColor() : null);
            response.put("tags", moim.getTags());
            response.put("thumbnail", convertS3UrlToProxyUrl(moim.getThumbnail()));
            response.put("maxMembers", moim.getMaxMembers());
            response.put("currentMembers", moim.getCurrentMembers());
            response.put("isPrivate", moim.getIsPrivate());
            response.put("isActive", moim.getIsActive());
            response.put("onlineType", moim.getOnlineType());
            response.put("location", moim.getLocation());
            response.put("locationDetail", moim.getLocationDetail());
            response.put("createdBy", moim.getCreatedBy().getId());
            response.put("creatorName", moim.getCreatedBy().getName());
            response.put("creatorProfileImage", convertS3UrlToProxyUrl(moim.getCreatedBy().getProfileImage()));
            response.put("createdAt", moim.getCreatedAt());
            response.put("updatedAt", moim.getUpdatedAt());
            response.put("isMember", member != null);
            response.put("userRole", member != null ? member.getRole() : null);
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("모임 상세 정보 조회 성공")
                    .data(response)
                    .build());
                    
        } catch (IllegalArgumentException e) {
            log.warn("모임 상세 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("모임 상세 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("모임 상세 정보 조회 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * 모임 참여
     * POST /moims/{moimId}/join
     */
    @PostMapping("/{moimId}/join")
    public ResponseEntity<ApiResponse<Map<String, Object>>> joinMoim(
            @PathVariable Long moimId,
            @RequestBody(required = false) Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 이미 모임 멤버인지 확인
            if (moimMemberRepository.existsByMoimIdAndUserId(moimId, userId)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("이미 참여 중인 모임입니다.")
                                .build());
            }
            
            // 모임 참여 처리
            MoimMember member = moimService.joinMoim(moimId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("moimId", moimId);
            response.put("userId", userId);
            response.put("role", member.getRole());
            response.put("status", member.getStatus());
            response.put("joinedAt", member.getJoinedAt());
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("모임에 성공적으로 참여했습니다.")
                    .data(response)
                    .build());
                    
        } catch (Exception e) {
            log.error("모임 참여 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("모임 참여 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * 모임 탈퇴
     * DELETE /moims/{moimId}/join
     */
    @DeleteMapping("/{moimId}/join")
    public ResponseEntity<ApiResponse<Map<String, Object>>> leaveMoim(
            @PathVariable Long moimId,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 모임 탈퇴 처리
            moimService.leaveMoim(moimId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("moimId", moimId);
            response.put("userId", userId);
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("모임에서 탈퇴했습니다.")
                    .data(response)
                    .build());
                    
        } catch (Exception e) {
            log.error("모임 탈퇴 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("모임 탈퇴 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    // ========================================
    // 🎯 2단계: 게시판 기능 구현
    // ========================================
    
    /**
     * 게시글 목록 조회
     * GET /moims/{moimId}/posts
     */
    @GetMapping("/{moimId}/posts")
    public ResponseEntity<ApiResponse<PostListResponse>> getPosts(
            @PathVariable Long moimId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "latest") String sort,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 모임 멤버인지 확인
            if (!moimMemberRepository.existsByMoimIdAndUserId(moimId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<PostListResponse>builder()
                                .success(false)
                                .message("모임 멤버만 접근할 수 있습니다.")
                                .build());
            }
            
            // 게시글 목록 조회
            PostListResponse response = postService.getPosts(moimId, page, size, type, search, sort);
            
            return ResponseEntity.ok(ApiResponse.<PostListResponse>builder()
                    .success(true)
                    .message("게시글 목록 조회 성공")
                    .data(response)
                    .build());
                    
        } catch (Exception e) {
            log.error("게시글 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<PostListResponse>builder()
                            .success(false)
                            .message("게시글 목록 조회 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * 게시글 상세 조회
     * GET /moims/{moimId}/posts/{postId}
     */
    @GetMapping("/{moimId}/posts/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostDetail(
            @PathVariable Long moimId,
            @PathVariable Long postId,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 모임 멤버인지 확인
            if (!moimMemberRepository.existsByMoimIdAndUserId(moimId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<PostResponse>builder()
                                .success(false)
                                .message("모임 멤버만 접근할 수 있습니다.")
                                .build());
            }
            
            // 게시글 상세 조회
            PostResponse response = postService.getPostDetail(postId, moimId);
            
            return ResponseEntity.ok(ApiResponse.<PostResponse>builder()
                    .success(true)
                    .message("게시글 상세 조회 성공")
                    .data(response)
                    .build());
                    
        } catch (Exception e) {
            log.error("게시글 상세 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<PostResponse>builder()
                            .success(false)
                            .message("게시글 상세 조회 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * 게시글 작성
     * POST /moims/{moimId}/posts
     */
    @PostMapping("/{moimId}/posts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPost(
            @PathVariable Long moimId,
            @Valid @RequestBody CreatePostRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 모임 멤버인지 확인
            if (!moimMemberRepository.existsByMoimIdAndUserId(moimId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("모임 멤버만 게시글을 작성할 수 있습니다.")
                                .build());
            }
            
            // 유효성 검사 오류 확인
            if (bindingResult.hasErrors()) {
                List<FieldError> fieldErrors = bindingResult.getFieldErrors();
                List<String> errorMessages = fieldErrors.stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.toList());
                
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("입력 정보가 올바르지 않습니다.")
                                .errors(errorMessages)
                                .build());
            }
            
            // 게시글 작성
            Post post = postService.createPost(moimId, userId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", post.getId());
            response.put("title", post.getTitle());
            response.put("content", post.getContent());
            response.put("type", post.getType());
            response.put("isAnonymous", post.getIsAnonymous());
            response.put("authorId", post.getAuthor().getId());
            response.put("createdAt", post.getCreatedAt());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(true)
                            .message("게시글이 성공적으로 작성되었습니다.")
                            .data(response)
                            .build());
                    
        } catch (Exception e) {
            log.error("게시글 작성 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("게시글 작성 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * 댓글 작성
     * POST /moims/{moimId}/posts/{postId}/comments
     */
    @PostMapping("/{moimId}/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createComment(
            @PathVariable Long moimId,
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 모임 멤버인지 확인
            if (!moimMemberRepository.existsByMoimIdAndUserId(moimId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("모임 멤버만 댓글을 작성할 수 있습니다.")
                                .build());
            }
            
            // 유효성 검사 오류 확인
            if (bindingResult.hasErrors()) {
                List<FieldError> fieldErrors = bindingResult.getFieldErrors();
                List<String> errorMessages = fieldErrors.stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.toList());
                
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("입력 정보가 올바르지 않습니다.")
                                .errors(errorMessages)
                                .build());
            }
            
            // 댓글 작성
            Map<String, Object> response = postService.createComment(postId, userId, request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(true)
                            .message("댓글이 성공적으로 작성되었습니다.")
                            .data(response)
                            .build());
                    
        } catch (Exception e) {
            log.error("댓글 작성 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("댓글 작성 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * 게시글 좋아요/취소
     * POST /moims/{moimId}/posts/{postId}/like
     */
    @PostMapping("/{moimId}/posts/{postId}/like")
    public ResponseEntity<ApiResponse<Map<String, Object>>> togglePostLike(
            @PathVariable Long moimId,
            @PathVariable Long postId,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 모임 멤버인지 확인
            if (!moimMemberRepository.existsByMoimIdAndUserId(moimId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("모임 멤버만 접근할 수 있습니다.")
                                .build());
            }
            
            // 좋아요 토글
            Map<String, Object> response = postService.togglePostLike(postId, userId);
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message(response.get("liked").equals(true) ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다.")
                    .data(response)
                    .build());
                    
        } catch (Exception e) {
            log.error("게시글 좋아요 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("좋아요 처리 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * 댓글 좋아요 토글
     * POST /moims/{moimId}/posts/{postId}/comments/{commentId}/like
     */
    @PostMapping("/{moimId}/posts/{postId}/comments/{commentId}/like")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleCommentLike(
            @PathVariable Long moimId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 모임 멤버인지 확인
            if (!moimMemberRepository.existsByMoimIdAndUserId(moimId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("모임 멤버만 접근할 수 있습니다.")
                                .build());
            }
            
            // 댓글 좋아요 토글
            Map<String, Object> response = postService.toggleCommentLike(commentId, userId);
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message(response.get("liked").equals(true) ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다.")
                    .data(response)
                    .build());
                    
        } catch (Exception e) {
            log.error("댓글 좋아요 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("댓글 좋아요 처리 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    // ========================================
    // 🎯 3단계: 일정 + 멤버 관리
    // ========================================
    
    /**
     * 일정 목록 조회
     * GET /moims/{moimId}/schedules
     */
    @GetMapping("/{moimId}/schedules")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSchedules(
            @PathVariable Long moimId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String type,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 모임 멤버인지 확인
            if (!moimMemberRepository.existsByMoimIdAndUserId(moimId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("모임 멤버만 접근할 수 있습니다.")
                                .build());
            }
            
            // 일정 목록 조회
            Map<String, Object> response = scheduleService.getSchedules(moimId, startDate, endDate, type);
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("일정 목록 조회 성공")
                    .data(response)
                    .build());
                    
        } catch (Exception e) {
            log.error("일정 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("일정 목록 조회 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * 일정 등록
     * POST /moims/{moimId}/schedules
     */
    @PostMapping("/{moimId}/schedules")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSchedule(
            @PathVariable Long moimId,
            @Valid @RequestBody CreateScheduleRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 모임 멤버인지 확인
            if (!moimMemberRepository.existsByMoimIdAndUserId(moimId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("모임 멤버만 일정을 등록할 수 있습니다.")
                                .build());
            }
            
            // 유효성 검사 오류 확인
            if (bindingResult.hasErrors()) {
                List<FieldError> fieldErrors = bindingResult.getFieldErrors();
                List<String> errorMessages = fieldErrors.stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.toList());
                
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("입력 정보가 올바르지 않습니다.")
                                .errors(errorMessages)
                                .build());
            }
            
            // 일정 등록
            Schedule schedule = scheduleService.createSchedule(moimId, userId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", schedule.getId());
            response.put("title", schedule.getTitle());
            response.put("startDate", schedule.getStartDate());
            response.put("createdAt", schedule.getCreatedAt());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(true)
                            .message("일정이 성공적으로 등록되었습니다.")
                            .data(response)
                            .build());
                    
        } catch (Exception e) {
            log.error("일정 등록 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("일정 등록 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * 일정 참석/불참
     * POST /moims/{moimId}/schedules/{scheduleId}/participate
     */
    @PostMapping("/{moimId}/schedules/{scheduleId}/participate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> participateSchedule(
            @PathVariable Long moimId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleParticipationRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 모임 멤버인지 확인
            if (!moimMemberRepository.existsByMoimIdAndUserId(moimId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("모임 멤버만 접근할 수 있습니다.")
                                .build());
            }
            
            // 유효성 검사 오류 확인
            if (bindingResult.hasErrors()) {
                List<FieldError> fieldErrors = bindingResult.getFieldErrors();
                List<String> errorMessages = fieldErrors.stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.toList());
                
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("입력 정보가 올바르지 않습니다.")
                                .errors(errorMessages)
                                .build());
            }
            
            // 일정 참석 처리
            Map<String, Object> response = scheduleService.participateSchedule(scheduleId, userId, request.getStatus());
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("일정 참석 상태가 업데이트되었습니다.")
                    .data(response)
                    .build());
                    
        } catch (Exception e) {
            log.error("일정 참석 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("일정 참석 처리 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    /**
     * 멤버 목록 조회
     * GET /moims/{moimId}/members
     */
    @GetMapping("/{moimId}/members")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMembers(
            @PathVariable Long moimId,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 모임 멤버인지 확인
            if (!moimMemberRepository.existsByMoimIdAndUserId(moimId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("모임 멤버만 접근할 수 있습니다.")
                                .build());
            }
            
            // 멤버 목록 조회
            Map<String, Object> response = moimService.getMoimMembers(moimId);
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("멤버 목록 조회 성공")
                    .data(response)
                    .build());
                    
        } catch (Exception e) {
            log.error("멤버 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("멤버 목록 조회 중 오류가 발생했습니다.")
                            .errorCode("INTERNAL_SERVER_ERROR")
                            .build());
        }
    }
    
    // ========================================
    // 🎯 모임 참여신청 관련 API
    // ========================================
    
    /**
     * 모임 참여신청 생성
     * POST /moims/{moimId}/join-requests
     */
    @PostMapping("/{moimId}/join-requests")
    public ResponseEntity<ApiResponse<?>> createJoinRequest(
            @PathVariable Long moimId,
            @Valid @RequestBody CreateJoinRequestRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 유효성 검사 오류 확인
            if (bindingResult.hasErrors()) {
                List<FieldError> fieldErrors = bindingResult.getFieldErrors();
                List<String> errorMessages = fieldErrors.stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.toList());
                
                return ResponseEntity.badRequest()
                        .body(ApiResponse.builder()
                                .success(false)
                                .message("입력 정보가 올바르지 않습니다.")
                                .errors(errorMessages)
                                .build());
            }
            
            var result = moimJoinRequestService.createJoinRequest(moimId, userId, request);
            
            if (result == null) {
                // 공개 모임 자동 참여
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("공개 모임에 자동으로 참여되었습니다.")
                        .build());
            } else {
                // 비공개 모임 참여신청
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("참여신청이 전송되었습니다.")
                        .data(result)
                        .build());
            }
            
        } catch (IllegalArgumentException e) {
            log.warn("모임 참여신청 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("모임 참여신청 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("참여신청 생성 중 오류가 발생했습니다.")
                            .build());
        }
    }
    
    /**
     * 모임 참여신청 목록 조회 (운영자 전용)
     * GET /moims/{moimId}/join-requests
     */
    @GetMapping("/{moimId}/join-requests")
    public ResponseEntity<ApiResponse<?>> getJoinRequests(
            @PathVariable Long moimId,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int limit,
            HttpServletRequest httpRequest) {
        
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // TODO: 권한 검증 (모임 ADMIN/MODERATOR만 조회 가능)
            var result = moimJoinRequestService.getJoinRequests(moimId, status, page, limit);
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("참여신청 목록을 조회했습니다.")
                    .data(result)
                    .build());
            
        } catch (IllegalArgumentException e) {
            log.warn("모임 참여신청 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("모임 참여신청 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("참여신청 목록 조회 중 오류가 발생했습니다.")
                            .build());
        }
    }
    
    /**
     * 모임 참여신청 상세 조회 (운영자 전용)
     * GET /moims/{moimId}/join-requests/{requestId}
     */
    @GetMapping("/{moimId}/join-requests/{requestId}")
    public ResponseEntity<ApiResponse<?>> getJoinRequestDetail(
            @PathVariable Long moimId,
            @PathVariable Long requestId,
            HttpServletRequest httpRequest) {
        
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // TODO: 권한 검증 (모임 ADMIN/MODERATOR만 조회 가능)
            var result = moimJoinRequestService.getJoinRequestDetail(moimId, requestId);
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("참여신청 상세 정보를 조회했습니다.")
                    .data(result)
                    .build());
            
        } catch (IllegalArgumentException e) {
            log.warn("모임 참여신청 상세 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("모임 참여신청 상세 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("참여신청 상세 조회 중 오류가 발생했습니다.")
                            .build());
        }
    }
    
    /**
     * 모임 참여신청 승인 (운영자 전용)
     * POST /moims/{moimId}/join-requests/{requestId}/approve
     */
    @PostMapping("/{moimId}/join-requests/{requestId}/approve")
    public ResponseEntity<ApiResponse<?>> approveJoinRequest(
            @PathVariable Long moimId,
            @PathVariable Long requestId,
            @Valid @RequestBody ApproveJoinRequestRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long adminId = jwtUtil.getUserIdFromToken(token);
            
            // 유효성 검사 오류 확인
            if (bindingResult.hasErrors()) {
                List<FieldError> fieldErrors = bindingResult.getFieldErrors();
                List<String> errorMessages = fieldErrors.stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.toList());
                
                return ResponseEntity.badRequest()
                        .body(ApiResponse.builder()
                                .success(false)
                                .message("입력 정보가 올바르지 않습니다.")
                                .errors(errorMessages)
                                .build());
            }
            
            // TODO: 권한 검증 (모임 ADMIN/MODERATOR만 승인 가능)
            var result = moimJoinRequestService.approveJoinRequest(moimId, requestId, adminId, request);
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("참여신청이 승인되었습니다.")
                    .data(result)
                    .build());
            
        } catch (IllegalArgumentException e) {
            log.warn("모임 참여신청 승인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("모임 참여신청 승인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("참여신청 승인 중 오류가 발생했습니다.")
                            .build());
        }
    }
    
    /**
     * 모임 참여신청 거절 (운영자 전용)
     * POST /moims/{moimId}/join-requests/{requestId}/reject
     */
    @PostMapping("/{moimId}/join-requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<?>> rejectJoinRequest(
            @PathVariable Long moimId,
            @PathVariable Long requestId,
            @Valid @RequestBody RejectJoinRequestRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long adminId = jwtUtil.getUserIdFromToken(token);
            
            // 유효성 검사 오류 확인
            if (bindingResult.hasErrors()) {
                List<FieldError> fieldErrors = bindingResult.getFieldErrors();
                List<String> errorMessages = fieldErrors.stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.toList());
                
                return ResponseEntity.badRequest()
                        .body(ApiResponse.builder()
                                .success(false)
                                .message("입력 정보가 올바르지 않습니다.")
                                .errors(errorMessages)
                                .build());
                }
            
            // TODO: 권한 검증 (모임 ADMIN/MODERATOR만 거절 가능)
            var result = moimJoinRequestService.rejectJoinRequest(moimId, requestId, adminId, request);
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("참여신청이 거절되었습니다.")
                    .data(result)
                    .build());
            
        } catch (IllegalArgumentException e) {
            log.warn("모임 참여신청 거절 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("모임 참여신청 거절 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("참여신청 거절 중 오류가 발생했습니다.")
                            .build());
        }
    }
    
    /**
     * 모임 참여신청 통계 조회 (운영자 전용)
     * GET /moims/{moimId}/join-requests/stats
     */
    @GetMapping("/{moimId}/join-requests/stats")
    public ResponseEntity<ApiResponse<?>> getJoinRequestStats(
            @PathVariable Long moimId,
            HttpServletRequest httpRequest) {
        
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // TODO: 권한 검증 (모임 ADMIN/MODERATOR만 조회 가능)
            var result = moimJoinRequestService.getJoinRequestStats(moimId);
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("참여신청 통계를 조회했습니다.")
                    .data(result)
                    .build());
            
        } catch (IllegalArgumentException e) {
            log.warn("모임 참여신청 통계 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("모임 참여신청 통계 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("참여신청 통계 조회 중 오류가 발생했습니다.")
                            .build());
        }
    }
    
    /**
     * 내 참여신청 상태 조회
     * GET /moims/{moimId}/join-requests/my-status
     */
    @GetMapping("/{moimId}/join-requests/my-status")
    public ResponseEntity<ApiResponse<?>> getMyJoinRequestStatus(
            @PathVariable Long moimId,
            HttpServletRequest httpRequest) {
        
        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            String status = moimJoinRequestService.getUserJoinRequestStatus(moimId, userId);
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("참여신청 상태를 조회했습니다.")
                    .data(status)
                    .build());
            
        } catch (Exception e) {
            log.error("참여신청 상태 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("참여신청 상태 조회 중 오류가 발생했습니다.")
                            .build());
        }
    }
}
