package com.moimlog.moimlog_backend.service;

import com.moimlog.moimlog_backend.dto.request.ApproveJoinRequestRequest;
import com.moimlog.moimlog_backend.dto.request.CreateJoinRequestRequest;
import com.moimlog.moimlog_backend.dto.request.RejectJoinRequestRequest;
import com.moimlog.moimlog_backend.dto.response.JoinRequestDetailResponse;
import com.moimlog.moimlog_backend.dto.response.JoinRequestResponse;
import com.moimlog.moimlog_backend.dto.response.JoinRequestStatsResponse;
import com.moimlog.moimlog_backend.entity.*;
import com.moimlog.moimlog_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 모임 참여신청 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MoimJoinRequestService {
    
    private final MoimJoinRequestRepository moimJoinRequestRepository;
    private final MoimRepository moimRepository;
    private final UserRepository userRepository;
    private final MoimMemberRepository moimMemberRepository;
    
    /**
     * 모임 참여신청 생성
     */
    public MoimJoinRequest createJoinRequest(Long moimId, Long userId, CreateJoinRequestRequest request) {
        log.info("모임 참여신청 생성 시작: moimId={}, userId={}", moimId, userId);
        
        // 모임 존재 확인
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
        
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        // 이미 모임 멤버인지 확인
        if (moimMemberRepository.existsByMoimIdAndUserId(moimId, userId)) {
            throw new IllegalArgumentException("이미 모임 멤버입니다.");
        }
        
        // 이미 참여신청이 있는지 확인
        if (moimJoinRequestRepository.existsByMoimIdAndUserId(moimId, userId)) {
            throw new IllegalArgumentException("이미 참여신청을 보냈습니다.");
        }
        
        // 모임이 비공개이고 승인이 필요한 경우
        if (moim.getIsPrivate()) {
            // 참여신청 생성
            MoimJoinRequest joinRequest = MoimJoinRequest.createJoinRequest(moim, user, request.getMessage());
            MoimJoinRequest savedRequest = moimJoinRequestRepository.save(joinRequest);
            
            log.info("모임 참여신청 생성 완료: requestId={}", savedRequest.getId());
            return savedRequest;
        } else {
            // 공개 모임인 경우 바로 멤버로 추가
            MoimMember member = MoimMember.createMoimMember(moim, user, MoimMember.Role.MEMBER);
            moimMemberRepository.save(member);
            
            // 모임 현재 멤버 수 증가
            moim.setCurrentMembers(moim.getCurrentMembers() + 1);
            moimRepository.save(moim);
            
            log.info("공개 모임 자동 참여 완료: moimId={}, userId={}", moimId, userId);
            return null;
        }
    }
    
    /**
     * 모임 참여신청 목록 조회
     */
    public Map<String, Object> getJoinRequests(Long moimId, String status, int page, int limit) {
        log.info("모임 참여신청 목록 조회: moimId={}, status={}, page={}, limit={}", moimId, status, page, limit);
        
        // 모임 존재 확인
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
        
        // 페이지네이션 설정
        Pageable pageable = PageRequest.of(page - 1, limit);
        
        Page<MoimJoinRequest> joinRequestPage;
        
        if ("ALL".equalsIgnoreCase(status) || status == null) {
            joinRequestPage = moimJoinRequestRepository.findByMoimId(moimId, pageable);
        } else {
            try {
                MoimJoinRequest.Status requestStatus = MoimJoinRequest.Status.valueOf(status.toUpperCase());
                joinRequestPage = moimJoinRequestRepository.findByMoimIdAndStatus(moimId, requestStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("잘못된 상태 값입니다: " + status);
            }
        }
        
        // DTO 변환
        List<JoinRequestResponse> joinRequests = joinRequestPage.getContent().stream()
                .map(JoinRequestResponse::from)
                .collect(Collectors.toList());
        
        // 페이지네이션 정보
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", page);
        pagination.put("totalPages", joinRequestPage.getTotalPages());
        pagination.put("totalItems", joinRequestPage.getTotalElements());
        pagination.put("itemsPerPage", limit);
        
        // 응답 구성
        Map<String, Object> response = new HashMap<>();
        response.put("joinRequests", joinRequests);
        response.put("pagination", pagination);
        
        return response;
    }
    
    /**
     * 모임 참여신청 상세 조회
     */
    public JoinRequestDetailResponse getJoinRequestDetail(Long moimId, Long requestId) {
        log.info("모임 참여신청 상세 조회: moimId={}, requestId={}", moimId, requestId);
        
        // 참여신청 조회
        MoimJoinRequest joinRequest = moimJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("참여신청을 찾을 수 없습니다."));
        
        // 모임 ID 검증
        if (!joinRequest.getMoim().getId().equals(moimId)) {
            throw new IllegalArgumentException("잘못된 모임 ID입니다.");
        }
        
        return JoinRequestDetailResponse.from(joinRequest);
    }
    
    /**
     * 모임 참여신청 승인
     */
    public Map<String, Object> approveJoinRequest(Long moimId, Long requestId, Long adminId, ApproveJoinRequestRequest request) {
        log.info("모임 참여신청 승인 시작: moimId={}, requestId={}, adminId={}", moimId, requestId, adminId);
        
        // 참여신청 조회
        MoimJoinRequest joinRequest = moimJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("참여신청을 찾을 수 없습니다."));
        
        // 모임 ID 검증
        if (!joinRequest.getMoim().getId().equals(moimId)) {
            throw new IllegalArgumentException("잘못된 모임 ID입니다.");
        }
        
        // 처리 가능한 상태인지 확인
        if (!joinRequest.canBeProcessed()) {
            throw new IllegalArgumentException("이미 처리된 참여신청입니다.");
        }
        
        // 관리자 권한 확인
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        // 모임 정원 확인
        Moim moim = joinRequest.getMoim();
        if (moim.getMaxMembers() > 0 && moim.getCurrentMembers() >= moim.getMaxMembers()) {
            throw new IllegalArgumentException("모임 정원이 가득 찼습니다.");
        }
        
        // 참여신청 승인 처리
        joinRequest.approve(admin);
        moimJoinRequestRepository.save(joinRequest);
        
        // 모임 멤버로 추가
        MoimMember member = MoimMember.createMoimMember(moim, joinRequest.getUser(), MoimMember.Role.MEMBER);
        member.setStatus(MoimMember.Status.ACTIVE);
        member.setApprovedBy(admin);
        member.setApprovedAt(joinRequest.getProcessedAt());
        
        MoimMember savedMember = moimMemberRepository.save(member);
        
        // 모임 현재 멤버 수 증가
        moim.setCurrentMembers(moim.getCurrentMembers() + 1);
        moimRepository.save(moim);
        
        log.info("모임 참여신청 승인 완료: requestId={}", requestId);
        
        // 응답 구성
        Map<String, Object> response = new HashMap<>();
        response.put("message", "참여신청이 승인되었습니다.");
        response.put("joinRequest", JoinRequestResponse.from(joinRequest));
        
        Map<String, Object> moimMember = new HashMap<>();
        moimMember.put("id", savedMember.getId());
        moimMember.put("moimId", savedMember.getMoim().getId());
        moimMember.put("userId", savedMember.getUser().getId());
        moimMember.put("role", savedMember.getRole().name());
        moimMember.put("status", savedMember.getStatus().name());
        moimMember.put("approvedBy", savedMember.getApprovedBy().getId());
        moimMember.put("approvedAt", savedMember.getApprovedAt());
        
        response.put("moimMember", moimMember);
        
        return response;
    }
    
    /**
     * 모임 참여신청 거절
     */
    public Map<String, Object> rejectJoinRequest(Long moimId, Long requestId, Long adminId, RejectJoinRequestRequest request) {
        log.info("모임 참여신청 거절 시작: moimId={}, requestId={}, adminId={}", moimId, requestId, adminId);
        
        // 참여신청 조회
        MoimJoinRequest joinRequest = moimJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("참여신청을 찾을 수 없습니다."));
        
        // 모임 ID 검증
        if (!joinRequest.getMoim().getId().equals(moimId)) {
            throw new IllegalArgumentException("잘못된 모임 ID입니다.");
        }
        
        // 처리 가능한 상태인지 확인
        if (!joinRequest.canBeProcessed()) {
            throw new IllegalArgumentException("이미 처리된 참여신청입니다.");
        }
        
        // 관리자 권한 확인
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        // 참여신청 거절 처리
        joinRequest.reject(admin, request.getReason());
        moimJoinRequestRepository.save(joinRequest);
        
        log.info("모임 참여신청 거절 완료: requestId={}", requestId);
        
        // 응답 구성
        Map<String, Object> response = new HashMap<>();
        response.put("message", "참여신청이 거절되었습니다.");
        response.put("joinRequest", JoinRequestResponse.from(joinRequest));
        
        return response;
    }
    
    /**
     * 모임 참여신청 통계 조회
     */
    public JoinRequestStatsResponse getJoinRequestStats(Long moimId) {
        log.info("모임 참여신청 통계 조회: moimId={}", moimId);
        
        // 모임 존재 확인
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
        
        // 통계 정보 조회
        long total = moimJoinRequestRepository.countByMoimId(moimId);
        long pending = moimJoinRequestRepository.countByMoimIdAndStatus(moimId, MoimJoinRequest.Status.PENDING);
        long approved = moimJoinRequestRepository.countByMoimIdAndStatus(moimId, MoimJoinRequest.Status.APPROVED);
        long rejected = moimJoinRequestRepository.countByMoimIdAndStatus(moimId, MoimJoinRequest.Status.REJECTED);
        
        // 최근 활동 조회
        List<MoimJoinRequest> recentProcessed = moimJoinRequestRepository.findProcessedRequestsByMoimId(moimId, PageRequest.of(0, 5));
        
        List<JoinRequestStatsResponse.RecentActivity> recentActivity = recentProcessed.stream()
                .map(request -> JoinRequestStatsResponse.RecentActivity.builder()
                        .type(request.getStatus().name())
                        .userName(request.getUser().getName())
                        .processedAt(request.getProcessedAt())
                        .build())
                .collect(Collectors.toList());
        
        // 통계 정보 구성
        JoinRequestStatsResponse.Stats stats = JoinRequestStatsResponse.Stats.builder()
                .total(total)
                .pending(pending)
                .approved(approved)
                .rejected(rejected)
                .build();
        
        return JoinRequestStatsResponse.builder()
                .stats(stats)
                .recentActivity(recentActivity)
                .build();
    }
    
    /**
     * 사용자의 모임 참여신청 상태 조회
     */
    public String getUserJoinRequestStatus(Long moimId, Long userId) {
        return moimJoinRequestRepository.findStatusByMoimIdAndUserId(moimId, userId)
                .map(Enum::name)
                .orElse(null);
    }
}
