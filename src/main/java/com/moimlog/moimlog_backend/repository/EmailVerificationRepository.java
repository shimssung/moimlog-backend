package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 이메일 인증 리포지토리
 * 이메일 인증 관련 데이터베이스 작업을 처리
 */
@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    
    /**
     * 이메일로 최신 인증 정보 조회
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email ORDER BY ev.createdAt DESC")
    List<EmailVerification> findByEmailOrderByCreatedAtDesc(@Param("email") String email);
    
    /**
     * 이메일로 미인증된 인증 정보 조회
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email AND ev.isVerified = false ORDER BY ev.createdAt DESC")
    List<EmailVerification> findUnverifiedByEmail(@Param("email") String email);
    
    /**
     * 이메일로 최신 미인증 인증 정보 조회
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email AND ev.isVerified = false ORDER BY ev.createdAt DESC")
    Optional<EmailVerification> findLatestUnverifiedByEmail(@Param("email") String email);
    
    /**
     * 이메일로 최신 인증 완료된 인증 정보 조회
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email AND ev.isVerified = true ORDER BY ev.createdAt DESC")
    Optional<EmailVerification> findLatestVerifiedByEmail(@Param("email") String email);
    
    /**
     * 만료된 인증 정보 삭제
     */
    @Query("DELETE FROM EmailVerification ev WHERE ev.expiresAt < :now")
    void deleteExpiredVerifications(@Param("now") LocalDateTime now);
    
    /**
     * 이메일로 모든 인증 정보 삭제
     */
    void deleteByEmail(String email);
} 