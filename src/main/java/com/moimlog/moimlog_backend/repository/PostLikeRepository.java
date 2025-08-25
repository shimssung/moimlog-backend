package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 게시글 좋아요 레포지토리 인터페이스
 * 게시글 좋아요 관련 데이터베이스 작업을 담당
 */
@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    
    /**
     * 게시글 ID와 사용자 ID로 좋아요 조회
     */
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);
    
    /**
     * 게시글 ID로 좋아요 목록 조회
     */
    List<PostLike> findByPostId(Long postId);
    
    /**
     * 사용자 ID로 좋아요한 게시글 목록 조회
     */
    List<PostLike> findByUserId(Long userId);
    
    /**
     * 게시글 ID로 좋아요 수 조회
     */
    long countByPostId(Long postId);
    
    /**
     * 사용자 ID로 좋아요한 게시글 수 조회
     */
    long countByUserId(Long userId);
    
    /**
     * 게시글 ID로 모든 좋아요 삭제 (게시글 삭제 시)
     */
    void deleteByPostId(Long postId);
    
    /**
     * 사용자 ID로 모든 좋아요 삭제 (사용자 삭제 시)
     */
    void deleteByUserId(Long userId);
    
    /**
     * 게시글 ID와 사용자 ID로 좋아요 삭제
     */
    void deleteByPostIdAndUserId(Long postId, Long userId);
    
    /**
     * 게시글에 좋아요를 눌렀는지 확인
     */
    boolean existsByPostIdAndUserId(Long postId, Long userId);
}
