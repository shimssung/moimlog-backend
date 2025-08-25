package com.moimlog.moimlog_backend.controller;

import com.moimlog.moimlog_backend.dto.common.ApiResponse;
import com.moimlog.moimlog_backend.dto.request.ApproveJoinRequestRequest;
import com.moimlog.moimlog_backend.dto.request.CreateJoinRequestRequest;
import com.moimlog.moimlog_backend.dto.request.RejectJoinRequestRequest;
import com.moimlog.moimlog_backend.dto.response.JoinRequestDetailResponse;
import com.moimlog.moimlog_backend.dto.response.JoinRequestStatsResponse;
import com.moimlog.moimlog_backend.service.MoimJoinRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 모임 참여신청 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/moims/{moimId}/join-requests")
@RequiredArgsConstructor
@Slf4j
public class MoimJoinRequestController {
    
    private final MoimJoinRequestService moimJoinRequestService;
    
    /**
     * 모임 참여신청 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createJoinRequest(
            @PathVariable Long moimId,
            @Valid @RequestBody CreateJoinRequestRequest request,
            Authentication authentication) {
        
        try {
            Long userId = Long.parseLong(authentication.getName());
            log.info("모임 참여신청 생성 요청: moimId={}, userId={}", moimId, userId);
            
            var result = moimJoinRequestService.createJoinRequest(moimId, userId, request);
            
            if (result == null) {
                // 공개 모임 자동 참여
                return ResponseEntity.ok(ApiResponse.success("공개 모임에 자동으로 참여되었습니다.", null));
            } else {
                // 비공개 모임 참여신청
                return ResponseEntity.ok(ApiResponse.success("참여신청이 전송되었습니다.", result));
            }
            
        } catch (IllegalArgumentException e) {
            log.warn("모임 참여신청 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage(), "BAD_REQUEST"));
        } catch (Exception e) {
            log.error("모임 참여신청 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(ApiResponse.failure("참여신청 생성 중 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
        }
    }
    
    /**
     * 모임 참여신청 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getJoinRequests(
            @PathVariable Long moimId,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int limit,
            Authentication authentication) {
        
        try {
            Long userId = Long.parseLong(authentication.getName());
            log.info("모임 참여신청 목록 조회 요청: moimId={}, userId={}, status={}, page={}, limit={}", 
                    moimId, userId, status, page, limit);
            
            // TODO: 권한 검증 (모임 ADMIN/MODERATOR만 조회 가능)
            var result = moimJoinRequestService.getJoinRequests(moimId, status, page, limit);
            
            return ResponseEntity.ok(ApiResponse.success("참여신청 목록을 조회했습니다.", result));
            
        } catch (IllegalArgumentException e) {
            log.warn("모임 참여신청 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage(), "BAD_REQUEST"));
        } catch (Exception e) {
            log.error("모임 참여신청 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(ApiResponse.failure("참여신청 목록 조회 중 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
        }
    }
    
    /**
     * 모임 참여신청 상세 조회
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<?>> getJoinRequestDetail(
            @PathVariable Long moimId,
            @PathVariable Long requestId,
            Authentication authentication) {
        
        try {
            Long userId = Long.parseLong(authentication.getName());
            log.info("모임 참여신청 상세 조회 요청: moimId={}, requestId={}, userId={}", moimId, requestId, userId);
            
            // TODO: 권한 검증 (모임 ADMIN/MODERATOR만 조회 가능)
            JoinRequestDetailResponse result = moimJoinRequestService.getJoinRequestDetail(moimId, requestId);
            
            return ResponseEntity.ok(ApiResponse.success("참여신청 상세 정보를 조회했습니다.", result));
            
        } catch (IllegalArgumentException e) {
            log.warn("모임 참여신청 상세 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage(), "BAD_REQUEST"));
        } catch (Exception e) {
            log.error("모임 참여신청 상세 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(ApiResponse.failure("참여신청 상세 조회 중 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
        }
    }
    
    /**
     * 모임 참여신청 승인
     */
    @PostMapping("/{requestId}/approve")
    public ResponseEntity<ApiResponse<?>> approveJoinRequest(
            @PathVariable Long moimId,
            @PathVariable Long requestId,
            @Valid @RequestBody ApproveJoinRequestRequest request,
            Authentication authentication) {
        
        try {
            Long adminId = Long.parseLong(authentication.getName());
            log.info("모임 참여신청 승인 요청: moimId={}, requestId={}, adminId={}", moimId, requestId, adminId);
            
            // TODO: 권한 검증 (모임 ADMIN/MODERATOR만 승인 가능)
            var result = moimJoinRequestService.approveJoinRequest(moimId, requestId, adminId, request);
            
            return ResponseEntity.ok(ApiResponse.success("참여신청이 승인되었습니다.", result));
            
        } catch (IllegalArgumentException e) {
            log.warn("모임 참여신청 승인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage(), "BAD_REQUEST"));
        } catch (Exception e) {
            log.error("모임 참여신청 승인 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(ApiResponse.failure("참여신청 승인 중 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
        }
    }
    
    /**
     * 모임 참여신청 거절
     */
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ApiResponse<?>> rejectJoinRequest(
            @PathVariable Long moimId,
            @PathVariable Long requestId,
            @Valid @RequestBody RejectJoinRequestRequest request,
            Authentication authentication) {
        
        try {
            Long adminId = Long.parseLong(authentication.getName());
            log.info("모임 참여신청 거절 요청: moimId={}, requestId={}, adminId={}", moimId, requestId, adminId);
            
            // TODO: 권한 검증 (모임 ADMIN/MODERATOR만 거절 가능)
            var result = moimJoinRequestService.rejectJoinRequest(moimId, requestId, adminId, request);
            
            return ResponseEntity.ok(ApiResponse.success("참여신청이 거절되었습니다.", result));
            
        } catch (IllegalArgumentException e) {
            log.warn("모임 참여신청 거절 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage(), "BAD_REQUEST"));
        } catch (Exception e) {
            log.error("모임 참여신청 거절 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(ApiResponse.failure("참여신청 거절 중 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
        }
    }
    
    /**
     * 모임 참여신청 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<?>> getJoinRequestStats(
            @PathVariable Long moimId,
            Authentication authentication) {
        
        try {
            Long userId = Long.parseLong(authentication.getName());
            log.info("모임 참여신청 통계 조회 요청: moimId={}, userId={}", moimId, userId);
            
            // TODO: 권한 검증 (모임 ADMIN/MODERATOR만 조회 가능)
            JoinRequestStatsResponse result = moimJoinRequestService.getJoinRequestStats(moimId);
            
            return ResponseEntity.ok(ApiResponse.success("참여신청 통계를 조회했습니다.", result));
            
        } catch (IllegalArgumentException e) {
            log.warn("모임 참여신청 통계 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage(), "BAD_REQUEST"));
        } catch (Exception e) {
            log.error("모임 참여신청 통계 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(ApiResponse.failure("참여신청 통계 조회 중 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
        }
    }
    
    /**
     * 사용자의 모임 참여신청 상태 조회
     */
    @GetMapping("/my-status")
    public ResponseEntity<ApiResponse<?>> getMyJoinRequestStatus(
            @PathVariable Long moimId,
            Authentication authentication) {
        
        try {
            Long userId = Long.parseLong(authentication.getName());
            log.info("내 참여신청 상태 조회 요청: moimId={}, userId={}", moimId, userId);
            
            String status = moimJoinRequestService.getUserJoinRequestStatus(moimId, userId);
            
            return ResponseEntity.ok(ApiResponse.success("참여신청 상태를 조회했습니다.", status));
            
        } catch (Exception e) {
            log.error("참여신청 상태 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(ApiResponse.failure("참여신청 상태 조회 중 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
        }
    }
}
