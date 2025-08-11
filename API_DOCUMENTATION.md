# MoimLog Backend API 문서

## 📋 개요

MoimLog 백엔드 API 문서입니다. 모든 API는 `/auth` 경로를 기본으로 합니다.

**현재 구현 상태**: ✅ Phase 1 완료 (기본 인프라), ✅ Phase 2 일부 완료 (모임 생성)
**서버 URL**: `http://localhost:8080/moimlog`
**데이터베이스**: AWS RDS MySQL

---

## 🔐 인증 관련 API

### 1. 회원가입

- **URL**: `POST /auth/signup`
- **설명**: 새로운 사용자 회원가입
- **요청 본문**:

```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동",
  "nickname": "길동이",
  "phone": "010-1234-5678",
  "bio": "자기소개"
}
```

- **응답**:

```json
{
  "success": true,
  "message": "회원가입이 성공적으로 완료되었습니다.",
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "nickname": "길동이"
}
```

### 2. 로그인

- **URL**: `POST /auth/login`
- **설명**: 사용자 로그인
- **요청 본문**:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

- **응답**:

```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "nickname": "길동이",
  "isOnboardingCompleted": false
}
```

### 3. 토큰 갱신

- **URL**: `POST /auth/refresh`
- **설명**: 액세스 토큰 갱신 (HttpOnly 쿠키의 refreshToken 사용)
- **요청 헤더**: 쿠키에서 refreshToken 자동 전송
- **응답**:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 4. 로그아웃

- **URL**: `POST /auth/logout`
- **설명**: 사용자 로그아웃
- **요청 헤더**:
  ```
  Authorization: Bearer {accessToken}
  ```
- **응답**:

```json
{
  "success": true,
  "message": "로그아웃이 완료되었습니다."
}
```

### 5. 내 정보 조회

- **URL**: `GET /auth/me`
- **설명**: 현재 로그인한 사용자 정보 조회
- **요청 헤더**:
  ```
  Authorization: Bearer {accessToken}
  ```
- **응답**:

```json
{
  "success": true,
  "userId": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "nickname": "길동이",
  "profileImage": "https://moimlog-bucket.s3.ap-southeast-2.amazonaws.com/profile-images/...",
  "bio": "자기소개",
  "phone": "010-1234-5678",
  "birthDate": "1990-01-01",
  "gender": "MALE",
  "isOnboardingCompleted": true
}
```

---

## 📧 이메일 인증 관련 API

### 1. 이메일 중복 확인

- **URL**: `GET /auth/check-email?email=user@example.com`
- **설명**: 이메일 중복 확인
- **응답**:

```json
{
  "email": "user@example.com",
  "duplicate": false
}
```

### 2. 이메일 인증 코드 발송

- **URL**: `POST /auth/send-verification`
- **설명**: 이메일 인증 코드 발송
- **요청 본문**:

```json
{
  "email": "user@example.com"
}
```

- **응답**:

```json
{
  "success": true,
  "message": "인증 코드가 발송되었습니다.",
  "email": "user@example.com",
  "isVerified": false
}
```

### 3. 이메일 인증 코드 검증

- **URL**: `POST /auth/verify-email`
- **설명**: 이메일 인증 코드 검증
- **요청 본문**:

```json
{
  "email": "user@example.com",
  "verificationCode": "123456"
}
```

- **응답**:

```json
{
  "success": true,
  "message": "이메일 인증이 완료되었습니다.",
  "email": "user@example.com",
  "isVerified": true
}
```

---

## 🔑 비밀번호 재설정 관련 API

### 1. 비밀번호 찾기

- **URL**: `POST /auth/forgot-password`
- **설명**: 비밀번호 재설정 이메일 발송
- **요청 본문**:

```json
{
  "email": "user@example.com"
}
```

- **응답**:

```json
{
  "success": true,
  "message": "비밀번호 재설정 이메일이 발송되었습니다.",
  "email": "user@example.com"
}
```

### 2. 비밀번호 재설정 인증 코드 검증

- **URL**: `POST /auth/verify-reset-code`
- **설명**: 비밀번호 재설정 인증 코드 검증
- **요청 본문**:

```json
{
  "email": "user@example.com",
  "verificationCode": "123456"
}
```

- **응답**:

```json
{
  "success": true,
  "message": "인증 코드가 확인되었습니다.",
  "email": "user@example.com"
}
```

### 3. 비밀번호 재설정

- **URL**: `POST /auth/reset-password`
- **설명**: 새 비밀번호로 변경
- **요청 본문**:

```json
{
  "email": "user@example.com",
  "verificationCode": "123456",
  "newPassword": "newpassword123"
}
```

- **응답**:

```json
{
  "success": true,
  "message": "비밀번호가 성공적으로 변경되었습니다.",
  "email": "user@example.com"
}
```

---

## 🎯 온보딩 관련 API

### 1. 온보딩 처리

- **URL**: `POST /auth/onboarding`
- **설명**: 사용자 온보딩 정보 저장
- **요청 헤더**:
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- **요청 본문**:

```json
{
  "nickname": "길동이",
  "bio": "안녕하세요! 새로운 사람들과 만나고 대화하는 것을 좋아합니다.",
  "moimCategories": ["운동/스포츠", "독서/스터디", "여행"],
  "profileImage": "data:image/jpeg;base64,..."
}
```

- **응답**:

```json
{
  "success": true,
  "message": "온보딩이 완료되었습니다.",
  "userId": 1,
  "nickname": "길동이",
  "isOnboardingCompleted": true
}
```

### 2. 온보딩 완료 여부 확인

- **URL**: `GET /auth/onboarding/status`
- **설명**: 사용자의 온보딩 완료 여부 확인
- **요청 헤더**:
  ```
  Authorization: Bearer {accessToken}
  ```
- **응답**:

```json
{
  "isCompleted": false
}
```

### 3. 닉네임 중복 확인

- **URL**: `GET /auth/check-nickname?nickname=길동이`
- **설명**: 닉네임 중복 확인
- **응답 (사용 가능한 경우)**:

```json
{
  "nickname": "길동이",
  "duplicate": false
}
```

- **응답 (중복된 경우)**:

```json
{
  "nickname": "길동이",
  "duplicate": true
}
```

### 4. 사용자 모임 카테고리 조회

- **URL**: `GET /auth/user-categories`
- **설명**: 현재 사용자의 모임 카테고리 목록 조회
- **요청 헤더**:
  ```
  Authorization: Bearer {accessToken}
  ```
- **응답**:

```json
{
  "categories": ["운동/스포츠", "독서/스터디", "여행"]
}
```

### 5. 전체 모임 카테고리 목록 조회

- **URL**: `GET /auth/moim-categories`
- **설명**: 시스템에 등록된 모든 모임 카테고리 목록 조회
- **요청 헤더**:
  ```
  Authorization: Bearer {accessToken}
  ```
- **응답**:

```json
{
  "categories": [
    {
      "id": 1,
      "name": "운동/스포츠",
      "label": "운동/스포츠",
      "description": "다양한 운동과 스포츠 활동",
      "color": "#10b981",
      "createdAt": "2025-07-12T09:00:00"
    },
    {
      "id": 2,
      "name": "게임",
      "label": "게임",
      "description": "온라인/오프라인 게임 모임",
      "color": "#f59e0b",
      "createdAt": "2025-07-12T09:00:00"
    },
    {
      "id": 3,
      "name": "독서/스터디",
      "label": "독서/스터디",
      "description": "책 읽기와 공부 모임",
      "color": "#3b82f6",
      "createdAt": "2025-07-12T09:00:00"
    },
    {
      "id": 4,
      "name": "음악",
      "label": "음악",
      "description": "음악 감상과 연주 활동",
      "color": "#8b5cf6",
      "createdAt": "2025-07-12T09:00:00"
    },
    {
      "id": 5,
      "name": "여행",
      "label": "여행",
      "description": "국내외 여행 모임",
      "color": "#06b6d4",
      "createdAt": "2025-07-12T09:00:00"
    },
    {
      "id": 6,
      "name": "요리/베이킹",
      "label": "요리/베이킹",
      "description": "요리와 베이킹 활동",
      "color": "#ef4444",
      "createdAt": "2025-07-12T09:00:00"
    },
    {
      "id": 7,
      "name": "영화/드라마",
      "label": "영화/드라마",
      "description": "영화와 드라마 감상",
      "color": "#ec4899",
      "createdAt": "2025-07-12T09:00:00"
    },
    {
      "id": 8,
      "name": "예술/문화",
      "label": "예술/문화",
      "description": "예술과 문화 활동",
      "color": "#a855f7",
      "createdAt": "2025-07-12T09:00:00"
    },
    {
      "id": 9,
      "name": "IT/기술",
      "label": "IT/기술",
      "description": "IT와 기술 관련 모임",
      "color": "#6366f1",
      "createdAt": "2025-07-12T09:00:00"
    },
    {
      "id": 10,
      "name": "기타",
      "label": "기타",
      "description": "기타 다양한 모임",
      "color": "#6b7280",
      "createdAt": "2025-07-12T09:00:00"
    }
  ]
}
```

---

## 👤 프로필 관련 API

### 1. 프로필 수정

- **URL**: `PUT /auth/profile`
- **설명**: 사용자 프로필 정보 수정
- **요청 헤더**:
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- **요청 본문**:

```json
{
  "name": "홍길동",
  "nickname": "길동이",
  "profileImage": "data:image/jpeg;base64,...",
  "bio": "자기소개",
  "phone": "010-1234-5678",
  "birthDate": "1990-01-01",
  "gender": "MALE"
}
```

- **응답**:

```json
{
  "success": true,
  "userId": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "nickname": "길동이",
  "profileImage": "https://moimlog-bucket.s3.ap-southeast-2.amazonaws.com/profile-images/...",
  "bio": "자기소개",
  "phone": "010-1234-5678",
  "birthDate": "1990-01-01",
  "gender": "MALE",
  "isOnboardingCompleted": true
}
```

### 2. 프로필 이미지 업로드

- **URL**: `POST /auth/upload-profile-image`
- **설명**: 프로필 이미지 파일 업로드
- **요청 헤더**:
  ```
  Authorization: Bearer {accessToken}
  Content-Type: multipart/form-data
  ```
- **요청 파라미터**:
  - `file`: 이미지 파일 (MultipartFile)
- **응답**:

```json
{
  "success": true,
  "imageUrl": "https://moimlog-bucket.s3.ap-southeast-2.amazonaws.com/profile-images/..."
}
```

---

## 🔧 기술적 세부사항

### 인증 방식

- JWT (JSON Web Token) 사용
- Access Token: 1시간 만료 (메모리에만 저장)
- Refresh Token: 7일 만료 (HttpOnly 쿠키)
- 보안 강화: XSS/CSRF 공격 방지를 위해 Access Token을 쿠키에서 제거

### AWS S3 이미지 업로드

- 프로필 이미지는 AWS S3에 업로드
- 모임 썸네일 이미지는 AWS S3에 업로드
- Base64 인코딩된 이미지 지원
- 업로드된 이미지 URL 반환

### 에러 응답 형식

```json
{
  "success": false,
  "message": "에러 메시지",
  "errorCode": "ERROR_CODE"
}
```

### HTTP 상태 코드

- `200`: 성공
- `201`: 생성 성공 (회원가입)
- `400`: 잘못된 요청
- `401`: 인증 실패
- `403`: 권한 없음
- `404`: 리소스 없음
- `500`: 서버 오류

### CORS 설정

- 개발 환경: `http://localhost:3000`, `http://127.0.0.1:3000` 허용
- 프로덕션 환경에서 추가 설정 필요

### 컨텍스트 패스

- 서버 컨텍스트 패스: `/moimlog`
- 프론트엔드 요청 시: `http://localhost:8080/moimlog/auth/...`

---

## 📝 참고사항

1. **토큰 사용**: 인증이 필요한 API 호출 시 `Authorization: Bearer {accessToken}` 헤더 포함
2. **이미지 업로드**: Base64 인코딩된 문자열 지원
3. **모임 카테고리**: 10가지 기본 모임 카테고리 제공
4. **온보딩**: 로그인 후 온보딩 완료 여부에 따라 리다이렉트 처리 필요
5. **데이터베이스**: `user_moim_categories` 테이블을 사용하여 사용자-카테고리 매핑 관리
6. **보안**: JWT 필터에서 공개 엔드포인트는 인증 검증 건너뛰기

---

## 🏠 모임 관련 API

### 1. 모임 생성

- **URL**: `POST /moims`
- **설명**: 새로운 모임 생성
- **요청 헤더**:
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- **요청 본문**:

```json
{
  "title": "축구 모임",
  "description": "매주 토요일 축구하는 모임입니다.",
  "categoryId": 1,
  "maxMembers": 20,
  "tags": ["축구", "운동", "토요일"],
  "thumbnail": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD...",
  "isPrivate": false,
  "onlineType": "offline",
  "location": "전주시 평화동",
  "locationDetail": "전주초등학교 운동장"
}
```

- **응답**:

```json
{
  "success": true,
  "message": "모임이 성공적으로 생성되었습니다.",
  "data": {
    "id": 1,
    "title": "축구 모임",
    "description": "매주 토요일 축구하는 모임입니다.",
    "categoryId": 1,
    "category": {
      "id": 1,
      "name": "운동/스포츠",
      "label": "운동/스포츠",
      "color": "#FF6B6B"
    },
    "maxMembers": 20,
    "currentMembers": 1,
    "tags": ["축구", "운동", "토요일"],
    "thumbnail": "https://moimlog-bucket.s3.ap-southeast-2.amazonaws.com/profile-images/...",
    "isPrivate": false,
    "onlineType": "offline",
    "location": "전주시 평화동",
    "locationDetail": "전주초등학교 운동장",
    "createdBy": {
      "id": 3,
      "name": "사용자명",
      "profileImage": "프로필이미지URL"
    },
    "createdAt": "2025-08-11T22:02:06.392",
    "updatedAt": "2025-08-11T22:02:06.392"
  }
}
```

### 2. 모임 카테고리 목록 조회

- **URL**: `GET /moims/categories`
- **설명**: 모임 생성 시 선택할 수 있는 카테고리 목록 조회
- **요청 헤더**:
  ```
  Authorization: Bearer {accessToken}
  ```
- **응답**:

```json
{
  "success": true,
  "message": "모임 카테고리 조회 성공",
  "data": [
    {
      "id": 1,
      "name": "운동/스포츠",
      "label": "운동/스포츠",
      "color": "#FF6B6B"
    },
    {
      "id": 2,
      "name": "게임",
      "label": "게임",
      "color": "#4ECDC4"
    },
    {
      "id": 3,
      "name": "독서/학습",
      "label": "독서/학습",
      "color": "#45B7D1"
    },
    {
      "id": 4,
      "name": "음악",
      "label": "음악",
      "color": "#96CEB4"
    },
    {
      "id": 5,
      "name": "영화/드라마",
      "label": "영화/드라마",
      "color": "#FFEAA7"
    },
    {
      "id": 6,
      "name": "요리/베이킹",
      "label": "요리/베이킹",
      "color": "#DDA0DD"
    },
    {
      "id": 7,
      "name": "여행",
      "label": "여행",
      "color": "#98D8C8"
    },
    {
      "id": 8,
      "name": "프로그래밍/IT",
      "label": "프로그래밍/IT",
      "color": "#F7DC6F"
    },
    {
      "id": 9,
      "name": "예술/문화",
      "label": "예술/문화",
      "color": "#BB8FCE"
    },
    {
      "id": 10,
      "name": "기타",
      "label": "기타",
      "color": "#85C1E9"
    }
  ]
}
```

### 3. 모임 API 헬스체크

- **URL**: `GET /moims/health`
- **설명**: 모임 API 서버 상태 확인
- **응답**:

```
Moim API is running
```

---

## 🚧 향후 개발 예정 API

### 모임 관련 (Phase 2)

- ✅ 모임 생성, 조회
- 모임 수정, 삭제
- 모임 가입, 탈퇴
- 모임 멤버 관리

### 게시판 관련 (Phase 2)

- 게시글 작성, 수정, 삭제, 조회
- 댓글 시스템
- 좋아요 기능

### 일정 관리 (Phase 3)

- 일정 생성, 수정, 삭제
- 일정 참석 관리

### 채팅 시스템 (Phase 3)

- 실시간 채팅
- WebSocket 연동

### 알림 시스템 (Phase 3)

- 실시간 알림
- 푸시 알림
