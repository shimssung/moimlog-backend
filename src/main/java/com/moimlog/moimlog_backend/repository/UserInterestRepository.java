package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 사용자-관심사 연결 Repository 인터페이스
 * 사용자와 관심사의 연결 관계를 관리
 */
@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    
    /**
     * 사용자의 관심사 목록 조회
     * @param userId 사용자 ID
     * @return 사용자의 관심사 목록
     */
    @Query("SELECT ui FROM UserInterest ui JOIN FETCH ui.interest WHERE ui.user.id = :userId")
    List<UserInterest> findByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자의 관심사 이름 목록 조회
     * @param userId 사용자 ID
     * @return 관심사 이름 목록
     */
    @Query("SELECT ui.interest.name FROM UserInterest ui WHERE ui.user.id = :userId")
    List<String> findInterestNamesByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자의 기존 관심사 삭제
     * @param userId 사용자 ID
     */
    void deleteByUserId(Long userId);
    
    /**
     * 사용자의 특정 관심사 존재 여부 확인
     * @param userId 사용자 ID
     * @param interestId 관심사 ID
     * @return 존재 여부
     */
    boolean existsByUserIdAndInterestId(Long userId, Long interestId);
} 