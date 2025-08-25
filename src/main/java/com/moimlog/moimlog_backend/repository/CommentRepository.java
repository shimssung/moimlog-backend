package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 댓글 레포지토리 인터페이스
 * 댓글 관련 데이터베이스 작업을 담당
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * 게시글 ID로 댓글 목록 조회 (페이지네이션)
     */
    Page<Comment> findByPostIdOrderByCreatedAtAsc(Long postId, Pageable pageable);
    
    /**
     * 게시글 ID로 댓글 목록 조회 (계층 구조)
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findTopLevelCommentsByPostId(@Param("postId") Long postId);
    
    /**
     * 부모 댓글 ID로 답글 목록 조회
     */
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);
    
    /**
     * 게시글 ID로 댓글 수 조회
     */
    long countByPostId(Long postId);
    
    /**
     * 작성자 ID로 댓글 목록 조회
     */
    Page<Comment> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);
    
    /**
     * 게시글 ID와 작성자 ID로 댓글 목록 조회
     */
    Page<Comment> findByPostIdAndAuthorIdOrderByCreatedAtDesc(Long postId, Long authorId, Pageable pageable);
    
    /**
     * 부모 댓글 ID로 답글 수 조회
     */
    long countByParentId(Long parentId);
    
    /**
     * 게시글 ID로 모든 댓글 삭제 (게시글 삭제 시)
     */
    void deleteByPostId(Long postId);
}
