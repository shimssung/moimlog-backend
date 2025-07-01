package com.moimlog.moimlog_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 사용자 엔티티 클래스
 * 사용자의 기본 정보를 저장하는 테이블
 */
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(name = "password", length = 255)
    private String password;
    
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    
    @Column(name = "nickname", length = 50)
    private String nickname;
    
    @Column(name = "profile_image", length = 500)
    private String profileImage;
    
    @Column(name = "bio", length = 500)
    private String bio;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "birth_date")
    private LocalDateTime birthDate;
    
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
    
    // 기본 생성자
    public User() {
    }
    
    // 모든 필드를 매개변수로 받는 생성자
    public User(String id, String email, String password, String name, String nickname,
                String profileImage, String bio, String phone, LocalDateTime birthDate,
                Gender gender, Boolean isActive, Boolean isVerified, LocalDateTime lastLoginAt,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.bio = bio;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
        this.isActive = isActive;
        this.isVerified = isVerified;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getter 메서드들
    public String getId() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getName() {
        return name;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getProfileImage() {
        return profileImage;
    }
    
    public String getBio() {
        return bio;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public LocalDateTime getBirthDate() {
        return birthDate;
    }
    
    public Gender getGender() {
        return gender;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public Boolean getIsVerified() {
        return isVerified;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // Setter 메서드들
    public void setId(String id) {
        this.id = id;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public void setBirthDate(LocalDateTime birthDate) {
        this.birthDate = birthDate;
    }
    
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum Gender {
        MALE, FEMALE, OTHER
    }
} 