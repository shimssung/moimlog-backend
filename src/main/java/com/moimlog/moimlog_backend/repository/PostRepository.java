package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 게시글 레포지토리 인터페이스
 * 게시글 관련 데이터베이스 작업을 담당
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    /**
     * 모임 ID로 게시글 목록 조회 (페이지네이션)
     */
    Page<Post> findByMoimIdOrderByIsPinnedDescCreatedAtDesc(Long moimId, Pageable pageable);
    
    /**
     * 모임 ID와 게시글 타입으로 게시글 목록 조회
     */
    Page<Post> findByMoimIdAndTypeOrderByIsPinnedDescCreatedAtDesc(
            Long moimId, Post.PostType type, Pageable pageable);
    
    /**
     * 모임 ID와 검색어로 게시글 검색
     */
    @Query("SELECT p FROM Post p WHERE p.moim.id = :moimId " +
           "AND (p.title LIKE %:search% OR p.content LIKE %:search%) " +
           "ORDER BY p.isPinned DESC, p.createdAt DESC")
    Page<Post> findByMoimIdAndSearchKeyword(
            @Param("moimId") Long moimId, 
            @Param("search") String search, 
            Pageable pageable);
    
    /**
     * 모임 ID와 게시글 타입과 검색어로 게시글 검색
     */
    @Query("SELECT p FROM Post p WHERE p.moim.id = :moimId " +
           "AND p.type = :type " +
           "AND (p.title LIKE %:search% OR p.content LIKE %:search%) " +
           "ORDER BY p.isPinned DESC, p.createdAt DESC")
    Page<Post> findByMoimIdAndTypeAndSearchKeyword(
            @Param("moimId") Long moimId, 
            @Param("type") Post.PostType type, 
            @Param("search") String search, 
            Pageable pageable);
    
    /**
     * 모임 ID로 공지사항만 조회
     */
    List<Post> findByMoimIdAndTypeOrderByIsPinnedDescCreatedAtDesc(Long moimId, Post.PostType type);
    
    /**
     * 모임 ID로 게시글 수 조회
     */
    long countByMoimId(Long moimId);
    
    /**
     * 모임 ID와 게시글 타입으로 게시글 수 조회
     */
    long countByMoimIdAndType(Long moimId, Post.PostType type);
    
    /**
     * 작성자 ID로 게시글 목록 조회
     */
    Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);
    
    /**
     * 모임 ID와 작성자 ID로 게시글 목록 조회
     */
    Page<Post> findByMoimIdAndAuthorIdOrderByCreatedAtDesc(Long moimId, Long authorId, Pageable pageable);
    
    /**
     * 게시글 조회수 증가
     */
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);
    
    /**
     * 모임 ID로 인기 게시글 조회 (좋아요 순)
     */
    @Query("SELECT p FROM Post p WHERE p.moim.id = :moimId ORDER BY p.likeCount DESC, p.createdAt DESC")
    Page<Post> findPopularPostsByMoimId(@Param("moimId") Long moimId, Pageable pageable);
    
    /**
     * 모임 ID로 제목순 게시글 조회
     */
    @Query("SELECT p FROM Post p WHERE p.moim.id = :moimId ORDER BY p.title ASC")
    Page<Post> findPostsByMoimIdOrderByTitle(@Param("moimId") Long moimId, Pageable pageable);
}
