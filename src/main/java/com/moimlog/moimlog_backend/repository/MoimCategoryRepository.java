package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.MoimCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MoimCategoryRepository extends JpaRepository<MoimCategory, Long> {
    
    /**
     * 이름으로 카테고리 찾기
     */
    Optional<MoimCategory> findByName(String name);
    
    /**
     * 모든 카테고리 조회
     */
    @Query("SELECT mc FROM MoimCategory mc ORDER BY mc.name")
    List<MoimCategory> findAllOrderByName();
    
    /**
     * 활성 카테고리 조회 (모임 수 포함)
     */
    @Query("SELECT mc FROM MoimCategory mc WHERE mc.isActive = true ORDER BY mc.name")
    List<MoimCategory> findActiveCategoriesWithMoimCount();
} 