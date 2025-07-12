# MoimLog Backend API 문서

## 📋 개요

MoimLog 백엔드 API 문서입니다. 모든 API는 `/auth` 경로를 기본으로 합니다.

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
  "message": "회원가입이 완료되었습니다.",
  "userId": 1,
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
- **설명**: 액세스 토큰 갱신
- **요청 파라미터**: `refreshToken`
- **응답**:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 4. 로그아웃

- **URL**: `POST /auth/logout`
- **설명**: 사용자 로그아웃
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
- **요청 헤더**:
  ```
  Authorization: Bearer {accessToken}
  ```
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

---

## 🔧 기술적 세부사항

### 인증 방식

- JWT (JSON Web Token) 사용
- Access Token: 30분 만료
- Refresh Token: 7일 만료 (HttpOnly 쿠키)

### AWS S3 이미지 업로드

- 프로필 이미지는 AWS S3에 업로드
- Base64 인코딩된 이미지 지원
- 업로드된 이미지 URL 반환

### 에러 응답 형식

```json
{
  "success": false,
  "message": "에러 메시지"
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

- 모든 도메인 허용 (`*`)
- 개발 환경에서 테스트 가능

---

## 📝 참고사항

1. **토큰 사용**: 인증이 필요한 API 호출 시 `Authorization: Bearer {accessToken}` 헤더 포함
2. **이미지 업로드**: Base64 인코딩된 문자열로 전송, S3에 업로드 후 URL 반환
3. **모임 카테고리**: 10가지 기본 모임 카테고리 제공
4. **온보딩**: 로그인 후 온보딩 완료 여부에 따라 리다이렉트 처리 필요
5. **데이터베이스**: `user_interests` 테이블을 사용하여 사용자-카테고리 매핑 관리
