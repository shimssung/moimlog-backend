package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 일정 레포지토리 인터페이스
 * 일정 관련 데이터베이스 작업을 담당
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    /**
     * 모임 ID로 일정 목록 조회 (페이지네이션)
     */
    Page<Schedule> findByMoimIdOrderByStartDateAsc(Long moimId, Pageable pageable);
    
    /**
     * 모임 ID와 일정 타입으로 일정 목록 조회
     */
    Page<Schedule> findByMoimIdAndTypeOrderByStartDateAsc(Long moimId, Schedule.ScheduleType type, Pageable pageable);
    
    /**
     * 모임 ID와 날짜 범위로 일정 목록 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.moim.id = :moimId " +
           "AND s.startDate BETWEEN :startDate AND :endDate " +
           "ORDER BY s.startDate ASC")
    List<Schedule> findByMoimIdAndDateRange(
            @Param("moimId") Long moimId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 모임 ID와 일정 타입과 날짜 범위로 일정 목록 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.moim.id = :moimId " +
           "AND s.type = :type " +
           "AND s.startDate BETWEEN :startDate AND :endDate " +
           "ORDER BY s.startDate ASC")
    List<Schedule> findByMoimIdAndTypeAndDateRange(
            @Param("moimId") Long moimId,
            @Param("type") Schedule.ScheduleType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 모임 ID로 진행 중인 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.moim.id = :moimId " +
           "AND s.startDate <= :now " +
           "AND (s.endDate IS NULL OR s.endDate >= :now) " +
           "ORDER BY s.startDate ASC")
    List<Schedule> findOngoingSchedulesByMoimId(
            @Param("moimId") Long moimId,
            @Param("now") LocalDateTime now);
    
    /**
     * 모임 ID로 예정된 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.moim.id = :moimId " +
           "AND s.startDate > :now " +
           "ORDER BY s.startDate ASC")
    List<Schedule> findUpcomingSchedulesByMoimId(
            @Param("moimId") Long moimId,
            @Param("now") LocalDateTime now);
    
    /**
     * 모임 ID로 지난 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.moim.id = :moimId " +
           "AND s.startDate < :now " +
           "ORDER BY s.startDate DESC")
    List<Schedule> findPastSchedulesByMoimId(
            @Param("moimId") Long moimId,
            @Param("now") LocalDateTime now);
    
    /**
     * 모임 ID로 일정 수 조회
     */
    long countByMoimId(Long moimId);
    
    /**
     * 모임 ID와 일정 타입으로 일정 수 조회
     */
    long countByMoimIdAndType(Long moimId, Schedule.ScheduleType type);
    
    /**
     * 작성자 ID로 일정 목록 조회
     */
    Page<Schedule> findByCreatedByIdOrderByStartDateDesc(Long createdById, Pageable pageable);
    
    /**
     * 모임 ID와 작성자 ID로 일정 목록 조회
     */
    Page<Schedule> findByMoimIdAndCreatedByIdOrderByStartDateDesc(Long moimId, Long createdById, Pageable pageable);
}
