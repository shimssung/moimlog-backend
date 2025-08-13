package com.moimlog.moimlog_backend.controller;

import com.moimlog.moimlog_backend.dto.common.ApiResponse;
import com.moimlog.moimlog_backend.dto.request.CreateMoimRequest;
import com.moimlog.moimlog_backend.dto.response.CreateMoimResponse;
import com.moimlog.moimlog_backend.service.MoimService;
import com.moimlog.moimlog_backend.util.JwtUtil;
import com.moimlog.moimlog_backend.entity.MoimCategory;
import com.moimlog.moimlog_backend.repository.MoimCategoryRepository;
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
    private final JwtUtil jwtUtil;
    private final MoimCategoryRepository moimCategoryRepository;
    
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
     * 모임 카테고리 목록 조회 API
     * GET /moims/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<MoimCategory>>> getMoimCategories() {
        try {
            List<MoimCategory> categories = moimCategoryRepository.findAllOrderByName();
            
            return ResponseEntity.ok(ApiResponse.<List<MoimCategory>>builder()
                    .success(true)
                    .message("모임 카테고리 조회 성공")
                    .data(categories)
                    .build());
                    
        } catch (Exception e) {
            log.error("모임 카테고리 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<MoimCategory>>builder()
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
}
