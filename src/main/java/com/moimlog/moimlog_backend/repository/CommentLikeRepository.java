package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 댓글 좋아요 레포지토리
 */
@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    
    /**
     * 댓글 ID로 좋아요 목록 조회
     */
    List<CommentLike> findByCommentId(Long commentId);
    
    /**
     * 사용자 ID로 좋아요 목록 조회
     */
    List<CommentLike> findByUserId(Long userId);
    
    /**
     * 댓글과 사용자로 좋아요 조회
     */
    Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);
    
    /**
     * 댓글과 사용자로 좋아요 존재 여부 확인
     */
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);
    
    /**
     * 댓글 ID로 좋아요 수 조회
     */
    long countByCommentId(Long commentId);
    
    /**
     * 사용자 ID로 좋아요 수 조회
     */
    long countByUserId(Long userId);
    
    /**
     * 댓글 ID로 좋아요 삭제
     */
    void deleteByCommentId(Long commentId);
    
    /**
     * 사용자 ID로 좋아요 삭제
     */
    void deleteByUserId(Long userId);
    
    /**
     * 특정 댓글의 특정 사용자 좋아요 삭제
     */
    void deleteByCommentIdAndUserId(Long commentId, Long userId);
    
    /**
     * 댓글 ID로 좋아요 목록 조회 (페이징)
     */
    @Query("SELECT cl FROM CommentLike cl WHERE cl.comment.id = :commentId ORDER BY cl.createdAt DESC")
    List<CommentLike> findByCommentIdOrderByCreatedAtDesc(@Param("commentId") Long commentId);
    
    /**
     * 사용자 ID로 좋아요 목록 조회 (페이징)
     */
    @Query("SELECT cl FROM CommentLike cl WHERE cl.user.id = :userId ORDER BY cl.createdAt DESC")
    List<CommentLike> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}
