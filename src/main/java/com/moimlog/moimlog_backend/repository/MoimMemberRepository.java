package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.Moim;
import com.moimlog.moimlog_backend.entity.MoimMember;
import com.moimlog.moimlog_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 모임 멤버 데이터 접근을 위한 Repository 인터페이스
 */
@Repository
public interface MoimMemberRepository extends JpaRepository<MoimMember, Long> {
    
    /**
     * 모임 ID로 멤버 목록 조회
     */
    List<MoimMember> findByMoimId(Long moimId);
    
    /**
     * 사용자 ID로 참여 중인 모임 멤버 목록 조회
     */
    List<MoimMember> findByUserId(Long userId);
    
    /**
     * 모임과 사용자로 멤버 조회
     */
    Optional<MoimMember> findByMoimIdAndUserId(Long moimId, Long userId);
    
    /**
     * 모임에 특정 사용자가 멤버로 존재하는지 확인
     */
    boolean existsByMoimIdAndUserId(Long moimId, Long userId);
    
    /**
     * 모임의 관리자 목록 조회
     */
    @Query("SELECT mm FROM MoimMember mm WHERE mm.moim.id = :moimId AND mm.role = 'ADMIN'")
    List<MoimMember> findAdminsByMoimId(@Param("moimId") Long moimId);
    
    /**
     * 사용자가 특정 역할을 가진 모임 멤버인지 확인
     */
    @Query("SELECT COUNT(mm) > 0 FROM MoimMember mm WHERE mm.moim.id = :moimId AND mm.user.id = :userId AND mm.role = :role")
    boolean existsByMoimIdAndUserIdAndRole(@Param("moimId") Long moimId, 
                                          @Param("userId") Long userId, 
                                          @Param("role") MoimMember.Role role);
    
    /**
     * 사용자가 모임의 관리자 또는 모더레이터인지 확인
     */
    @Query("SELECT COUNT(mm) > 0 FROM MoimMember mm WHERE mm.moim.id = :moimId AND mm.user.id = :userId AND mm.role IN ('ADMIN', 'MODERATOR')")
    boolean isModeratorOrHigher(@Param("moimId") Long moimId, @Param("userId") Long userId);
    
    /**
     * 모임의 멤버 상태별 목록 조회
     */
    List<MoimMember> findByMoimIdAndStatus(Long moimId, MoimMember.Status status);
    
    /**
     * 특정 모임에서 사용자 삭제
     */
    void deleteByMoimIdAndUserId(Long moimId, Long userId);
    
    /**
     * 모임 ID로 모든 멤버 삭제
     */
    void deleteByMoimId(Long moimId);
    
    /**
     * 사용자 ID로 모임 멤버십 조회 (페이지네이션 지원)
     */
    Page<MoimMember> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 모임 ID로 멤버 목록 조회 (역할 순, 가입일 순)
     */
    List<MoimMember> findByMoimIdOrderByRoleAscJoinedAtAsc(Long moimId);
}
