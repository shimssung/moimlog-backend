# 🚀 MoimLog 백엔드 개발 요구사항

## 📋 프로젝트 개요

**프로젝트명**: MoimLog (모임로그)  
**프로젝트 타입**: 모임 관리 플랫폼  
**프론트엔드**: Next.js (React)  
**백엔드**: Java + Spring Boot  
**데이터베이스**: MySQL (Railway)  
**파일 저장소**: AWS S3

**현재 개발 상태**: ✅ Phase 1 완료, 🚀 Phase 2 준비 중

---

## 🏗️ 시스템 아키텍처

### 전체 구조

```
Frontend (Next.js) ←→ Backend (Spring Boot) ←→ Database (MySQL - Railway)
                                    ↓
                              AWS S3 (파일 저장)
```

### 주요 기능

1. **사용자 관리** - 회원가입, 로그인, 온보딩 ✅
2. **모임 관리** - 모임 생성, 수정, 삭제, 검색 🚧
3. **게시판** - 공지사항, 자유게시판, 사진게시판 🚧
4. **일정 관리** - 모임 일정, 작업, 마감일 🚧
5. **채팅** - 실시간 채팅 🚧
6. **멤버 관리** - 멤버 초대, 역할 관리 🚧
7. **알림** - 실시간 알림 시스템 🚧

---

## 👥 사용자 관리 (User Management) ✅

### 사용자 정보 구조

```json
{
  "id": 1,
  "email": "user@example.com",
  "password": "hashed_password",
  "name": "홍길동",
  "nickname": "길동이",
  "profileImage": "https://moimlog-bucket.s3.ap-southeast-2.amazonaws.com/profile-images/...",
  "bio": "자기소개",
  "phone": "010-1234-5678",
  "birthDate": "1990-01-01",
  "gender": "MALE|FEMALE|OTHER",
  "isActive": true,
  "isVerified": false,
  "isOnboardingCompleted": true,
  "lastLoginAt": "2025-07-01T10:30:00",
  "createdAt": "2025-06-01T00:00:00",
  "updatedAt": "2025-07-01T10:30:00"
}
```

### 온보딩 프로세스 ✅

1. **회원가입** → 기본 정보 입력 ✅
2. **로그인** → JWT 토큰 발급 ✅
3. **온보딩** → 프로필 설정, 모임 카테고리 선택 ✅
4. **완료** → 메인 페이지로 이동 ✅

### 사용자 역할 (Roles)

- **USER**: 일반 사용자 ✅
- **ADMIN**: 관리자 🚧
- **MODERATOR**: 모임 운영자 🚧

---

## 🏷️ 모임 카테고리 시스템 ✅

### 카테고리 정보 구조

```json
{
  "id": 1,
  "name": "운동/스포츠",
  "label": "운동/스포츠",
  "description": "다양한 운동과 스포츠 활동",
  "color": "#10b981",
  "createdAt": "2025-07-12T09:00:00"
}
```

### 기본 모임 카테고리 (10개) ✅

1. **운동/스포츠** - 다양한 운동과 스포츠 활동 ✅
2. **게임** - 온라인/오프라인 게임 모임 ✅
3. **독서/스터디** - 책 읽기와 공부 모임 ✅
4. **음악** - 음악 감상과 연주 활동 ✅
5. **여행** - 국내외 여행 모임 ✅
6. **요리/베이킹** - 요리와 베이킹 활동 ✅
7. **영화/드라마** - 영화와 드라마 감상 ✅
8. **예술/문화** - 예술과 문화 활동 ✅
9. **IT/기술** - IT와 기술 관련 모임 ✅
10. **기타** - 기타 다양한 모임 ✅

---

## 🎯 관심사 시스템 ✅

### 관심사 정보 구조

```json
{
  "id": 1,
  "name": "운동/스포츠",
  "description": "다양한 운동과 스포츠 활동",
  "isActive": true,
  "createdAt": "2025-07-12T09:00:00",
  "updatedAt": "2025-07-12T09:00:00"
}
```

### 기본 관심사 (10개) ✅

모임 카테고리와 동일한 10개 관심사 제공 ✅

---

## 🔐 인증 및 보안 ✅

### JWT 토큰 구조

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user_id",
    "email": "user@example.com",
    "roles": ["USER"],
    "iat": 1647600000,
    "exp": 1647686400
  }
}
```

### 보안 요구사항 ✅

- **비밀번호 암호화**: BCrypt 사용 ✅
- **JWT 토큰**: Access Token + Refresh Token ✅
- **Refresh Token**: HttpOnly 쿠키로 관리 ✅
- **CORS 설정**: 프론트엔드 도메인 허용 ✅
- **API 인증**: 모든 API에 JWT 토큰 검증 ✅
- **공개 엔드포인트**: JWT 필터에서 제외 ✅

---

## 📊 API 엔드포인트 설계

### 인증 관련 ✅

```
POST   /auth/signup                    # 회원가입 ✅
POST   /auth/login                     # 로그인 ✅
POST   /auth/refresh                   # 토큰 갱신 ✅
POST   /auth/logout                    # 로그아웃 ✅
GET    /auth/me                        # 내 정보 조회 ✅
PUT    /auth/profile                   # 프로필 수정 ✅
GET    /auth/check-email              # 이메일 중복 확인 ✅
POST   /auth/send-verification        # 이메일 인증 코드 발송 ✅
POST   /auth/verify-email             # 이메일 인증 코드 검증 ✅
POST   /auth/forgot-password          # 비밀번호 찾기 ✅
POST   /auth/verify-reset-code        # 비밀번호 재설정 인증 코드 검증 ✅
POST   /auth/reset-password           # 비밀번호 재설정 ✅
POST   /auth/upload-profile-image     # 프로필 이미지 업로드 ✅
```

### 온보딩 관련 ✅

```
POST   /auth/onboarding               # 온보딩 처리 ✅
GET    /auth/onboarding/status        # 온보딩 완료 여부 확인 ✅
GET    /auth/check-nickname           # 닉네임 중복 확인 ✅
```

### 모임 카테고리 관련 ✅

```
GET    /auth/moim-categories          # 전체 모임 카테고리 목록 ✅
GET    /auth/user-categories          # 사용자 모임 카테고리 목록 ✅
```

### 관심사 관련 ✅

```
GET    /auth/interests                # 전체 관심사 목록 🚧
GET    /auth/user-interests           # 사용자 관심사 목록 🚧
POST   /auth/user-interests           # 사용자 관심사 설정 🚧
```

### 소셜 로그인 (예정)

```
GET    /auth/oauth2/google            # Google 로그인 🚧
GET    /auth/oauth2/kakao             # Kakao 로그인 🚧
GET    /auth/oauth2/naver             # Naver 로그인 🚧
```

### 모임 관련 (Phase 2 예정)

```
GET    /moims                         # 모임 목록 조회 🚧
POST   /moims                         # 모임 생성 🚧
GET    /moims/{id}                    # 모임 상세 조회 🚧
PUT    /moims/{id}                    # 모임 수정 🚧
DELETE /moims/{id}                    # 모임 삭제 🚧
POST   /moims/{id}/join               # 모임 가입 🚧
DELETE /moims/{id}/leave              # 모임 탈퇴 🚧
```

### 게시판 관련 (Phase 2 예정)

```
GET    /moims/{id}/posts              # 게시글 목록 🚧
POST   /moims/{id}/posts              # 게시글 작성 🚧
GET    /posts/{id}                    # 게시글 상세 🚧
PUT    /posts/{id}                    # 게시글 수정 🚧
DELETE /posts/{id}                    # 게시글 삭제 🚧
POST   /posts/{id}/likes              # 좋아요 🚧
DELETE /posts/{id}/likes              # 좋아요 취소 🚧
```

### 댓글 관련 (Phase 2 예정)

```
GET    /posts/{id}/comments           # 댓글 목록 🚧
POST   /posts/{id}/comments           # 댓글 작성 🚧
PUT    /comments/{id}                 # 댓글 수정 🚧
DELETE /comments/{id}                 # 댓글 삭제 🚧
```

### 일정 관련 (Phase 3 예정)

```
GET    /moims/{id}/schedules          # 일정 목록 🚧
POST   /moims/{id}/schedules          # 일정 생성 🚧
GET    /schedules/{id}                # 일정 상세 🚧
PUT    /schedules/{id}                # 일정 수정 🚧
DELETE /schedules/{id}                # 일정 삭제 🚧
POST   /schedules/{id}/attend         # 일정 참석 🚧
DELETE /schedules/{id}/attend         # 일정 참석 취소 🚧
```

### 멤버 관련 (Phase 2 예정)

```
GET    /moims/{id}/members            # 멤버 목록 🚧
PUT    /moims/{id}/members/{userId}/role # 역할 변경 🚧
DELETE /moims/{id}/members/{userId}   # 멤버 제거 🚧
```

### 알림 관련 (Phase 3 예정)

```
GET    /notifications                 # 알림 목록 🚧
PUT    /notifications/{id}/read       # 읽음 처리 🚧
PUT    /notifications/read-all        # 모두 읽음 처리 🚧
DELETE /notifications/{id}            # 알림 삭제 🚧
```

---

## 🔧 기술 스택

### Backend ✅

- **Java 17** ✅
- **Spring Boot 3.5.3** ✅
- **Spring Security** ✅
- **Spring Data JPA** ✅
- **Spring Boot Mail** ✅
- **Spring Boot Validation** ✅
- **Spring OAuth2 Client** 🚧
- **MySQL 8.0 (Railway)** ✅
- **JWT (jjwt 0.11.5)** ✅
- **AWS SDK for Java** ✅
- **Lombok** ✅

### 개발 도구

- **Gradle** ✅
- **IntelliJ IDEA / Eclipse** ✅
- **Postman / Insomnia** ✅
- **MySQL Workbench** ✅

### 배포

- **Railway** - 백엔드 배포 ✅
- **Vercel** - 프론트엔드 배포 🚧
- **AWS S3** - 파일 저장소 ✅
- **Railway MySQL** - 데이터베이스 ✅

---

## 📋 개발 우선순위

### Phase 1: 기본 인프라 ✅ 완료

1. **프로젝트 설정** - Spring Boot 프로젝트 생성 ✅
2. **데이터베이스 설계** - 테이블 생성 및 인덱스 설정 ✅
3. **사용자 인증** - JWT 기반 인증 시스템 ✅
4. **이메일 인증** - 이메일 인증 시스템 ✅
5. **온보딩 시스템** - 사용자 온보딩 프로세스 ✅
6. **모임 카테고리** - 카테고리 관리 시스템 ✅
7. **관심사 시스템** - 관심사 관리 시스템 ✅
8. **AWS S3 연동** - 파일 업로드 시스템 ✅
9. **프로필 관리** - 사용자 프로필 조회/수정 ✅
10. **전역 예외 처리** - 일관된 에러 응답 ✅
11. **Spring Security 설정** - JWT 필터 및 권한 관리 ✅
12. **비밀번호 재설정** - 이메일 기반 비밀번호 재설정 ✅

### Phase 2: 핵심 기능 (다음 단계)

1. **모임 관리** - CRUD 기능 🚧
2. **멤버 관리** - 가입, 탈퇴, 역할 관리 🚧
3. **게시판** - 공지사항, 자유게시판, 사진게시판 🚧
4. **댓글 시스템** - 댓글 CRUD, 좋아요 🚧

### Phase 3: 고급 기능 (예정)

1. **일정 관리** - 일정 CRUD, 참석 관리 🚧
2. **채팅 시스템** - WebSocket 기반 실시간 채팅 🚧
3. **알림 시스템** - 실시간 알림 🚧
4. **소셜 로그인** - Google, Kakao, Naver OAuth2 🚧

### Phase 4: 관리 기능 (예정)

1. **관리자 기능** - 사용자 관리, 신고 처리 🚧
2. **검색 기능** - 모임, 게시글 검색 🚧
3. **통계 기능** - 사용자, 모임 통계 🚧
4. **성능 최적화** - 캐싱, 인덱스 최적화 🚧

---

# 폴더 구조 ✅

src/main/java/com/moimlog/moimlog_backend/
├── config/ # 설정 클래스들 ✅
│ ├── SecurityConfig.java # Spring Security 설정 ✅
│ ├── JwtAuthenticationFilter.java # JWT 인증 필터 ✅
│ ├── AwsConfig.java # AWS 설정 ✅
│ ├── AwsS3Config.java # AWS S3 설정 ✅
│ └── JwtConfig.java # JWT 설정 ✅
├── controller/ # REST API 컨트롤러 ✅
│ └── AuthController.java # 인증 관련 API ✅
├── service/ # 비즈니스 로직 ✅
│ ├── UserService.java # 사용자 서비스 ✅
│ ├── EmailService.java # 이메일 서비스 ✅
│ └── S3Service.java # S3 파일 업로드 서비스 ✅
├── repository/ # 데이터 접근 계층 ✅
│ ├── UserRepository.java # 사용자 리포지토리 ✅
│ ├── EmailVerificationRepository.java # 이메일 인증 리포지토리 ✅
│ ├── MoimCategoryRepository.java # 모임 카테고리 리포지토리 ✅
│ ├── InterestRepository.java # 관심사 리포지토리 ✅
│ ├── UserInterestRepository.java # 사용자-관심사 리포지토리 ✅
│ ├── UserMoimCategoryRepository.java # 사용자-모임카테고리 리포지토리 ✅
│ ├── RoleRepository.java # 역할 리포지토리 ✅
│ └── UserRoleRepository.java # 사용자-역할 리포지토리 ✅
├── entity/ # JPA 엔티티 ✅
│ ├── User.java # 사용자 엔티티 ✅
│ ├── EmailVerification.java # 이메일 인증 엔티티 ✅
│ ├── MoimCategory.java # 모임 카테고리 엔티티 ✅
│ ├── Interest.java # 관심사 엔티티 ✅
│ ├── UserInterest.java # 사용자-관심사 엔티티 ✅
│ ├── UserMoimCategory.java # 사용자-모임카테고리 엔티티 ✅
│ ├── Role.java # 역할 엔티티 ✅
│ └── UserRole.java # 사용자-역할 엔티티 ✅
├── dto/ # 데이터 전송 객체 ✅
│ ├── request/ # 요청 DTO ✅
│ │ ├── LoginRequest.java # 로그인 요청 ✅
│ │ ├── SignupRequest.java # 회원가입 요청 ✅
│ │ ├── OnboardingRequest.java # 온보딩 요청 ✅
│ │ ├── UpdateProfileRequest.java # 프로필 수정 요청 ✅
│ │ ├── EmailVerificationRequest.java # 이메일 인증 요청 ✅
│ │ ├── SendVerificationRequest.java # 인증 코드 발송 요청 ✅
│ │ ├── ForgotPasswordRequest.java # 비밀번호 찾기 요청 ✅
│ │ ├── ResetPasswordRequest.java # 비밀번호 재설정 요청 ✅
│ │ └── VerifyResetCodeRequest.java # 비밀번호 재설정 인증 코드 검증 요청 ✅
│ ├── response/ # 응답 DTO ✅
│ │ ├── LoginResponse.java # 로그인 응답 ✅
│ │ ├── SignupResponse.java # 회원가입 응답 ✅
│ │ ├── OnboardingResponse.java # 온보딩 응답 ✅
│ │ ├── UserProfileResponse.java # 프로필 조회 응답 ✅
│ │ ├── EmailVerificationResponse.java # 이메일 인증 응답 ✅
│ │ ├── ForgotPasswordResponse.java # 비밀번호 찾기 응답 ✅
│ │ ├── ResetPasswordResponse.java # 비밀번호 재설정 응답 ✅
│ │ └── VerifyResetCodeResponse.java # 비밀번호 재설정 인증 코드 검증 응답 ✅
│ └── common/ # 공통 DTO ✅
│ └── ApiResponse.java # 공통 API 응답 ✅
├── exception/ # 예외 처리 ✅
│ └── GlobalExceptionHandler.java # 전역 예외 처리 ✅
└── util/ # 유틸리티 클래스 ✅
└── JwtUtil.java # JWT 유틸리티 ✅

---

## 📝 참고사항

### 프론트엔드 연동 ✅

- **CORS 설정** 필수 ✅
- **JWT 토큰** 헤더에 포함 ✅
- **파일 업로드** Base64 인코딩 및 MultipartFile 지원 ✅
- **AWS S3** 이미지 URL 반환 ✅
- **컨텍스트 패스** `/moimlog` 설정 ✅

### 보안 고려사항 ✅

- **SQL Injection** 방지 ✅
- **XSS** 방지 ✅
- **CSRF** 방지 ✅
- **Rate Limiting** 적용 🚧
- **JWT 토큰** 보안 강화 ✅
- **공개 엔드포인트** JWT 필터 제외 ✅

### 성능 고려사항

- **페이징** 처리 🚧
- **캐싱** 전략 🚧
- **인덱스** 최적화 ✅
- **N+1 문제** 해결 🚧
- **AWS S3** CDN 활용 🚧

### 온보딩 플로우 ✅

1. **회원가입** → 기본 정보 입력 ✅
2. **로그인** → JWT 토큰 발급 ✅
3. **온보딩 상태 확인** → `/auth/onboarding/status` ✅
4. **온보딩 진행** → 프로필 설정, 모임 카테고리 선택 ✅
5. **온보딩 완료** → 메인 페이지로 이동 ✅

### 데이터베이스 구조 ✅

- **users**: 사용자 기본 정보 ✅
- **email_verifications**: 이메일 인증 정보 ✅
- **interests**: 관심사 정보 ✅
- **user_interests**: 사용자-관심사 매핑 ✅
- **moim_categories**: 모임 카테고리 정보 ✅
- **user_moim_categories**: 사용자-모임카테고리 매핑 ✅
- **roles**: 역할 정보 ✅
- **user_roles**: 사용자-역할 매핑 ✅

### 현재 구현된 주요 기능 ✅

- ✅ JWT 기반 인증 시스템
- ✅ 이메일 인증 시스템
- ✅ 비밀번호 재설정 시스템
- ✅ 사용자 온보딩 프로세스
- ✅ 모임 카테고리 관리
- ✅ 관심사 관리 시스템
- ✅ AWS S3 파일 업로드
- ✅ 프로필 관리
- ✅ Spring Security 설정
- ✅ 전역 예외 처리
- ✅ 컨텍스트 패스 설정
- ✅ Railway MySQL 연동

---

**Phase 1 완료! 🎉 이제 Phase 2 (모임 관리) 개발을 시작할 준비가 되었습니다! 🚀**
