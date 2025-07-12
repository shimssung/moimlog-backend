package com.moimlog.moimlog_backend.repository;

import com.moimlog.moimlog_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * 사용자 리포지토리 인터페이스
 * 
 * 사용자 엔티티에 대한 데이터베이스 작업을 담당
 * - 기본 CRUD 작업 (JpaRepository 상속)
 * - 이메일로 사용자 조회
 * - 이메일 중복 확인
 * - Spring Security를 위한 활성 사용자 조회
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 이메일로 사용자 조회
     * @param email 조회할 이메일
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 이메일 중복 확인
     * @param email 확인할 이메일
     * @return 중복 여부 (true: 중복, false: 중복 아님)
     */
    boolean existsByEmail(String email);
    
    /**
     * 활성화된 사용자만 조회 (Spring Security용)
     * @param email 조회할 이메일
     * @return 활성화된 사용자 정보 (Optional)
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);
    
    /**
     * 닉네임 중복 확인
     * @param nickname 확인할 닉네임
     * @return 중복 여부 (true: 중복, false: 중복 아님)
     */
    boolean existsByNickname(String nickname);
} 