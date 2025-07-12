package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.UserMoimCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMoimCategoryRepository extends JpaRepository<UserMoimCategory, Long> {
    
    /**
     * 사용자 ID로 카테고리 이름 목록 조회
     */
    @Query("SELECT mc.name FROM UserMoimCategory umc " +
           "JOIN umc.moimCategory mc " +
           "WHERE umc.user.id = :userId")
    List<String> findCategoryNamesByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자 ID로 사용자-카테고리 연결 삭제
     */
    void deleteByUserId(Long userId);
} 