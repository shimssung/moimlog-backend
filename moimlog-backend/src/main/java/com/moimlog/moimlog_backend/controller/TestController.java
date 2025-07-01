package com.moimlog.moimlog_backend.controller;

import com.moimlog.moimlog_backend.entity.Role;
import com.moimlog.moimlog_backend.entity.User;
import com.moimlog.moimlog_backend.entity.UserRole;
import com.moimlog.moimlog_backend.repository.RoleRepository;
import com.moimlog.moimlog_backend.repository.UserRepository;
import com.moimlog.moimlog_backend.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 테스트용 컨트롤러
 * 개발 및 테스트 목적으로 사용되는 API 엔드포인트들
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @PostMapping("/create-admin")
    public ResponseEntity<String> createAdminUser() {
        try {
            // 1. 관리자 역할이 있는지 확인하고 없으면 생성
            Role adminRole = roleRepository.findByName(Role.RoleType.ADMIN).orElse(null);
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setName(Role.RoleType.ADMIN);
                adminRole.setDescription("시스템 관리자");
                adminRole = roleRepository.save(adminRole);
            }

            // 2. 관리자 사용자 생성
            User adminUser = new User();
            adminUser.setEmail("admin@test.com");
            adminUser.setPassword("admin123");
            adminUser.setName("관리자");
            adminUser.setNickname("admin");
            adminUser = userRepository.save(adminUser);

            // 3. 사용자-역할 연결
            UserRole userRole = new UserRole();
            userRole.setUser(adminUser);
            userRole.setRole(adminRole);
            userRoleRepository.save(userRole);

            return ResponseEntity.ok("관리자 계정이 성공적으로 생성되었습니다. ID: " + adminUser.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("관리자 계정 생성 실패: " + e.getMessage());
        }
    }

    @GetMapping("/check-admin")
    public ResponseEntity<String> checkAdminUser() {
        try {
            User adminUser = userRepository.findByEmail("admin@test.com").orElse(null);
            if (adminUser != null) {
                return ResponseEntity.ok("관리자 계정이 존재합니다. ID: " + adminUser.getId());
            } else {
                return ResponseEntity.ok("관리자 계정이 존재하지 않습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("조회 실패: " + e.getMessage());
        }
    }
} 