package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.UserFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 즐겨찾기 레포지토리
 */
@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {
    
    /**
     * 사용자 ID로 즐겨찾기 목록 조회
     */
    List<UserFavorite> findByUserId(Long userId);
    
    /**
     * 사용자 ID로 즐겨찾기 목록 조회 (페이징)
     */
    Page<UserFavorite> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 모임 ID로 즐겨찾기 목록 조회
     */
    List<UserFavorite> findByMoimId(Long moimId);
    
    /**
     * 사용자와 모임으로 즐겨찾기 조회
     */
    Optional<UserFavorite> findByUserIdAndMoimId(Long userId, Long moimId);
    
    /**
     * 사용자와 모임으로 즐겨찾기 존재 여부 확인
     */
    boolean existsByUserIdAndMoimId(Long userId, Long moimId);
    
    /**
     * 사용자 ID로 즐겨찾기 수 조회
     */
    long countByUserId(Long userId);
    
    /**
     * 모임 ID로 즐겨찾기 수 조회
     */
    long countByMoimId(Long moimId);
    
    /**
     * 사용자 ID로 즐겨찾기 삭제
     */
    void deleteByUserId(Long userId);
    
    /**
     * 모임 ID로 즐겨찾기 삭제
     */
    void deleteByMoimId(Long moimId);
    
    /**
     * 특정 사용자의 특정 모임 즐겨찾기 삭제
     */
    void deleteByUserIdAndMoimId(Long userId, Long moimId);
    
    /**
     * 사용자 ID로 즐겨찾기 목록 조회 (최신순)
     */
    @Query("SELECT uf FROM UserFavorite uf WHERE uf.user.id = :userId ORDER BY uf.createdAt DESC")
    List<UserFavorite> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    /**
     * 모임 ID로 즐겨찾기 목록 조회 (최신순)
     */
    @Query("SELECT uf FROM UserFavorite uf WHERE uf.moim.id = :moimId ORDER BY uf.createdAt DESC")
    List<UserFavorite> findByMoimIdOrderByCreatedAtDesc(@Param("moimId") Long moimId);
}
