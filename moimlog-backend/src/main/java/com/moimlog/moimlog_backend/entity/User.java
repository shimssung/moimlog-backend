package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 사용자 엔티티 클래스
 * 사용자의 기본 정보를 저장하는 테이블
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(name = "password", length = 255)
    private String password;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "nickname", length = 50)
    private String nickname;
    
    @Column(name = "profile_image", length = 500)
    private String profileImage;
    
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Spring Security UserDetails 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER")); // 사용자 권한 설정
    }
    

    /* Spring Security가 사용자 인증을 처리할 때 자동으로 호출하는 메서드들에는 
       isAccountNonExpired, isAccountNonLocked, isCredentialsNonExpired, isEnabled 메서드가 있음
       이 메서드들은 사용자 계정의 상태를 확인하는 데 사용됨
       
       isAccountNonExpired: 계정 만료 여부 확인
       isAccountNonLocked: 계정 잠금 여부 확인
       isCredentialsNonExpired: 자격 증명 만료 여부 확인
       isEnabled: 계정 활성화 여부 확인
    */ 

    // Spring Security는 반드시 이 메서드(getUsername)를 구현해야 함
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return isActive;
    }
    
    // JPA 생명주기 메서드
    @PrePersist // 엔티티가 생성될 때 실행
    protected void onCreate() {
        createdAt = LocalDateTime.now(); // 현재 시간을 사용자 생성 시간으로 설정
        updatedAt = LocalDateTime.now(); // 현재 시간을 사용자 업데이트 시간으로 설정
    }
    
    @PreUpdate // 엔티티가 업데이트될 때 실행
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(); // 현재 시간을 사용자 업데이트 시간으로 설정
    }
    
    // 회원가입용 정적 팩토리 메서드
    // 매번 회원가입 요청이 들어올 때마다 새로운 사용자 객체를 생성하는 대신 이 메서드를 사용하여 사용자 객체를 생성함
    // 실수방지, 기본값 자동 설정
    public static User createUser(String email, String password, String name, String nickname) {
        return User.builder()
                .email(email)
                .password(password)
                .name(name)
                .nickname(nickname != null ? nickname : name)
                .isActive(true)
                .isVerified(false)
                .build();
    }
    
    // 성별 enum
    public enum Gender {
        MALE, FEMALE, OTHER
    }
} 