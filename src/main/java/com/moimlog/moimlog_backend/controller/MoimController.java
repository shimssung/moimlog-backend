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
}
