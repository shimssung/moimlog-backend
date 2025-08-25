package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.Moim;
import com.moimlog.moimlog_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 모임 데이터 접근을 위한 Repository 인터페이스
 */
@Repository
public interface MoimRepository extends JpaRepository<Moim, Long> {
    
    /**
     * 제목으로 모임 검색 (부분 일치)
     */
    List<Moim> findByTitleContainingIgnoreCase(String title);
    
    /**
     * 카테고리로 모임 검색
     */
    List<Moim> findByCategoryId(Long categoryId);
    
    /**
     * 생성자로 모임 검색
     */
    List<Moim> findByCreatedBy(User createdBy);
    
    /**
     * 온라인 타입으로 모임 검색
     */
    List<Moim> findByOnlineType(Moim.OnlineType onlineType);
    
    /**
     * 비공개 모임 여부로 검색
     */
    List<Moim> findByIsPrivate(Boolean isPrivate);
    
    /**
     * 활성 상태로 모임 검색
     */
    List<Moim> findByIsActive(Boolean isActive);
    
    /**
     * 최대 인원 범위로 모임 검색
     */
    List<Moim> findByMaxMembersBetween(Integer minMembers, Integer maxMembers);
    
    /**
     * 현재 인원이 최대 인원보다 적은 모임 검색 (참여 가능한 모임)
     */
    @Query("SELECT m FROM Moim m WHERE m.currentMembers < m.maxMembers AND m.isActive = true")
    List<Moim> findAvailableMoims();
    
    /**
     * 특정 지역의 모임 검색
     */
    List<Moim> findByLocationContainingIgnoreCase(String location);
    
    /**
     * 태그가 포함된 모임 검색
     */
    @Query("SELECT m FROM Moim m WHERE m.tags LIKE %:tag%")
    List<Moim> findByTagContaining(@Param("tag") String tag);
    
    /**
     * 복합 조건으로 모임 검색 (페이징 지원)
     */
    @Query("SELECT m FROM Moim m WHERE " +
           "(:title IS NULL OR m.title LIKE %:title%) AND " +
           "(:categoryId IS NULL OR m.category.id = :categoryId) AND " +
           "(:onlineType IS NULL OR m.onlineType = :onlineType) AND " +
           "(:isPrivate IS NULL OR m.isPrivate = :isPrivate) AND " +
           "m.isActive = true")
    Page<Moim> findMoimsByConditions(
            @Param("title") String title,
            @Param("categoryId") Long categoryId,
            @Param("onlineType") Moim.OnlineType onlineType,
            @Param("isPrivate") Boolean isPrivate,
            Pageable pageable
    );
    
    /**
     * 사용자가 생성한 모임 수 조회
     */
    long countByCreatedBy(User createdBy);
    
    /**
     * 특정 카테고리의 활성 모임 수 조회
     */
    long countByCategoryIdAndIsActiveTrue(Long categoryId);
    
    /**
     * 제목 존재 여부 확인
     */
    boolean existsByTitle(String title);
    
    /**
     * 제목과 생성자로 모임 존재 여부 확인
     */
    boolean existsByTitleAndCreatedBy(String title, User createdBy);
    
    /**
     * 생성자로 모임 목록 조회 (페이지네이션 지원)
     */
    Page<Moim> findByCreatedBy(User createdBy, Pageable pageable);
    
    /**
     * 복합 필터링을 통한 모임 목록 조회
     */
    @Query("SELECT m FROM Moim m WHERE " +
           "(:categoryId IS NULL OR m.category.id = :categoryId) AND " +
           "(:onlineType IS NULL OR m.onlineType = :onlineType) AND " +
           "(:location IS NULL OR m.location LIKE %:location%) AND " +
           "(:minMembers IS NULL OR m.maxMembers >= :minMembers) AND " +
           "(:maxMembers IS NULL OR m.maxMembers <= :maxMembers) AND " +
           "(:isPrivate IS NULL OR m.isPrivate = :isPrivate) AND " +
           "(:search IS NULL OR (" +
           "  m.title LIKE %:search% OR " +
           "  m.description LIKE %:search% OR " +
           "  m.tags LIKE %:search%" +
           ")) AND " +
           "m.isActive = true")
    Page<Moim> findMoimsWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("onlineType") String onlineType,
            @Param("location") String location,
            @Param("search") String search,
            @Param("minMembers") Integer minMembers,
            @Param("maxMembers") Integer maxMembers,
            @Param("isPrivate") Boolean isPrivate,
            Pageable pageable
    );
    
    /**
     * 인기 모임 목록 조회 (멤버 수 기준)
     */
    @Query("SELECT m FROM Moim m WHERE m.isActive = true ORDER BY m.currentMembers DESC, m.createdAt DESC")
    List<Moim> findPopularMoims(Pageable pageable);
    
    /**
     * 카테고리별 인기 모임 목록 조회
     */
    @Query("SELECT m FROM Moim m WHERE m.isActive = true AND m.category.id = :categoryId " +
           "ORDER BY m.currentMembers DESC, m.createdAt DESC")
    List<Moim> findPopularMoimsByCategory(@Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * 최신 모임 목록 조회
     */
    @Query("SELECT m FROM Moim m WHERE m.isActive = true ORDER BY m.createdAt DESC")
    List<Moim> findLatestMoims(Pageable pageable);
    
    /**
     * 카테고리별 최신 모임 목록 조회
     */
    @Query("SELECT m FROM Moim m WHERE m.category.id = :categoryId " +
           "ORDER BY m.createdAt DESC")
    List<Moim> findLatestMoimsByCategory(@Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * 인기 모임 목록 조회 (제한된 수)
     */
    default List<Moim> findPopularMoims(int limit) {
        return findPopularMoims(PageRequest.of(0, limit));
    }
    
    /**
     * 카테고리별 인기 모임 목록 조회 (제한된 수)
     */
    default List<Moim> findPopularMoimsByCategory(String category, int limit) {
        try {
            Long categoryId = Long.parseLong(category);
            return findPopularMoimsByCategory(categoryId, PageRequest.of(0, limit));
        } catch (NumberFormatException e) {
            // 카테고리 이름으로 검색하는 경우는 별도 처리 필요
            return new ArrayList<>();
        }
    }
    
    /**
     * 최신 모임 목록 조회 (제한된 수)
     */
    default List<Moim> findLatestMoims(int limit) {
        return findLatestMoims(PageRequest.of(0, limit));
    }
    
    /**
     * 카테고리별 최신 모임 목록 조회 (제한된 수)
     */
    default List<Moim> findLatestMoimsByCategory(String category, int limit) {
        try {
            Long categoryId = Long.parseLong(category);
            return findLatestMoimsByCategory(categoryId, PageRequest.of(0, limit));
        } catch (NumberFormatException e) {
            // 카테고리 이름으로 검색하는 경우는 별도 처리 필요
            return new ArrayList<>();
        }
    }
}
