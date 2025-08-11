package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.MoimSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 모임 설정 데이터 접근을 위한 Repository 인터페이스
 */
@Repository
public interface MoimSettingsRepository extends JpaRepository<MoimSettings, Long> {
    
    /**
     * 모임 ID로 설정 조회
     */
    Optional<MoimSettings> findByMoimId(Long moimId);
    
    /**
     * 모임 ID로 설정 존재 여부 확인
     */
    boolean existsByMoimId(Long moimId);
    
    /**
     * 모임 ID로 설정 삭제
     */
    void deleteByMoimId(Long moimId);
}
