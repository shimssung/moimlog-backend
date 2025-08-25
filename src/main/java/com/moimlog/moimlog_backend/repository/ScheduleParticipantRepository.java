package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.ScheduleParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 일정 참가자 레포지토리 인터페이스
 * 일정 참가자 관련 데이터베이스 작업을 담당
 */
@Repository
public interface ScheduleParticipantRepository extends JpaRepository<ScheduleParticipant, Long> {
    
    /**
     * 일정 ID와 사용자 ID로 참가자 조회
     */
    Optional<ScheduleParticipant> findByScheduleIdAndUserId(Long scheduleId, Long userId);
    
    /**
     * 일정 ID로 참가자 목록 조회
     */
    List<ScheduleParticipant> findByScheduleId(Long scheduleId);
    
    /**
     * 사용자 ID로 참가한 일정 목록 조회
     */
    List<ScheduleParticipant> findByUserId(Long userId);
    
    /**
     * 일정 ID와 참가 상태로 참가자 목록 조회
     */
    List<ScheduleParticipant> findByScheduleIdAndStatus(Long scheduleId, ScheduleParticipant.Status status);
    
    /**
     * 일정 ID로 참석하는 참가자 수 조회
     */
    long countByScheduleIdAndStatus(Long scheduleId, ScheduleParticipant.Status status);
    
    /**
     * 사용자 ID로 참석하는 일정 수 조회
     */
    long countByUserIdAndStatus(Long userId, ScheduleParticipant.Status status);
    
    /**
     * 일정 ID로 모든 참가자 삭제 (일정 삭제 시)
     */
    void deleteByScheduleId(Long scheduleId);
    
    /**
     * 사용자 ID로 모든 참가자 삭제 (사용자 삭제 시)
     */
    void deleteByUserId(Long userId);
    
    /**
     * 일정 ID와 사용자 ID로 참가자 삭제
     */
    void deleteByScheduleIdAndUserId(Long scheduleId, Long userId);
    
    /**
     * 일정에 참가자가 있는지 확인
     */
    boolean existsByScheduleIdAndUserId(Long scheduleId, Long userId);
}
