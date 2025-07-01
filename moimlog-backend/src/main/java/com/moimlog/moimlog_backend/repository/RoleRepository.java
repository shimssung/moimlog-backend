package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * 역할 리포지토리 인터페이스
 * 
 * 역할 엔티티에 대한 데이터베이스 작업을 담당
 * - 기본 CRUD 작업 (JpaRepository 상속)
 * - 역할 이름으로 역할 조회
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * 역할 이름으로 역할 조회
     * @param name 조회할 역할 이름 (USER, ADMIN, MODERATOR)
     * @return 역할 정보 (Optional)
     */
    Optional<Role> findByName(Role.RoleType name);
} 