package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 관심사 Repository 인터페이스
 * 관심사 관련 데이터베이스 작업을 처리
 */
@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {
    
    /**
     * 활성화된 관심사 목록 조회
     * @return 활성화된 관심사 목록
     */
    @Query("SELECT i FROM Interest i WHERE i.isActive = true ORDER BY i.name")
    List<Interest> findAllActive();
    
    /**
     * 이름으로 관심사 조회
     * @param name 관심사 이름
     * @return 관심사 정보 (Optional)
     */
    Optional<Interest> findByNameAndIsActiveTrue(String name);
    
    /**
     * 이름으로 관심사 존재 여부 확인
     * @param name 관심사 이름
     * @return 존재 여부
     */
    boolean existsByNameAndIsActiveTrue(String name);
} 