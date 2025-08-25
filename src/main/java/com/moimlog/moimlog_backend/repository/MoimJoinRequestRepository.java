package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.MoimJoinRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 모임 참여신청 데이터 접근을 위한 Repository 인터페이스
 */
@Repository
public interface MoimJoinRequestRepository extends JpaRepository<MoimJoinRequest, Long> {
    
    /**
     * 모임 ID로 참여신청 목록 조회 (페이지네이션)
     */
    Page<MoimJoinRequest> findByMoimId(Long moimId, Pageable pageable);
    
    /**
     * 모임 ID와 상태로 참여신청 목록 조회 (페이지네이션)
     */
    Page<MoimJoinRequest> findByMoimIdAndStatus(Long moimId, MoimJoinRequest.Status status, Pageable pageable);
    
    /**
     * 사용자 ID로 참여신청 목록 조회
     */
    List<MoimJoinRequest> findByUserId(Long userId);
    
    /**
     * 모임과 사용자로 참여신청 조회
     */
    Optional<MoimJoinRequest> findByMoimIdAndUserId(Long moimId, Long userId);
    
    /**
     * 모임에 특정 사용자의 참여신청이 존재하는지 확인
     */
    boolean existsByMoimIdAndUserId(Long moimId, Long userId);
    
    /**
     * 모임의 대기 중인 참여신청 수 조회
     */
    long countByMoimIdAndStatus(Long moimId, MoimJoinRequest.Status status);
    
    /**
     * 모임의 전체 참여신청 수 조회
     */
    long countByMoimId(Long moimId);
    
    /**
     * 모임의 승인 대기 중인 참여신청 목록 조회 (최신순)
     */
    @Query("SELECT mjr FROM MoimJoinRequest mjr WHERE mjr.moim.id = :moimId AND mjr.status = 'PENDING' ORDER BY mjr.requestedAt ASC")
    List<MoimJoinRequest> findPendingRequestsByMoimId(@Param("moimId") Long moimId);
    
    /**
     * 모임의 최근 처리된 참여신청 목록 조회 (최신순)
     */
    @Query("SELECT mjr FROM MoimJoinRequest mjr WHERE mjr.moim.id = :moimId AND mjr.status IN ('APPROVED', 'REJECTED') ORDER BY mjr.processedAt DESC")
    List<MoimJoinRequest> findProcessedRequestsByMoimId(@Param("moimId") Long moimId, Pageable pageable);
    
    /**
     * 사용자의 특정 모임 참여신청 상태 조회
     */
    @Query("SELECT mjr.status FROM MoimJoinRequest mjr WHERE mjr.moim.id = :moimId AND mjr.user.id = :userId")
    Optional<MoimJoinRequest.Status> findStatusByMoimIdAndUserId(@Param("moimId") Long moimId, @Param("userId") Long userId);
    
    /**
     * 모임의 참여신청 통계 조회
     */
    @Query("SELECT mjr.status, COUNT(mjr) FROM MoimJoinRequest mjr WHERE mjr.moim.id = :moimId GROUP BY mjr.status")
    List<Object[]> getStatusCountsByMoimId(@Param("moimId") Long moimId);
}
