package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 사용자-역할 매핑 리포지토리 인터페이스
 * 
 * 사용자-역할 매핑 엔티티에 대한 데이터베이스 작업을 담당
 * - 기본 CRUD 작업 (JpaRepository 상속)
 * - 사용자 ID로 해당 사용자의 모든 역할 조회
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    /**
     * 사용자 ID로 해당 사용자의 모든 역할 조회
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 역할 목록
     */
    List<UserRole> findByUserId(String userId);
} 