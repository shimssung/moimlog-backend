# MoimLog Backend API 문서

## 🚀 기본 정보

- **Base URL**: `http://localhost:8080/moimlog`
- **Content-Type**: `application/json`
- **인증 방식**: JWT Bearer Token

## 🔐 인증 API

### 1. 회원가입

**POST** `/auth/signup`

**Request Body:**
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

**Response (성공 - 201):**
```json
{
  "success": true,
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "nickname": "길동이",
  "message": "회원가입이 성공적으로 완료되었습니다."
}
```

**Response (실패 - 400):**
```json
{
  "success": false,
  "message": "이미 가입된 이메일입니다."
}
```

### 2. 로그인

**POST** `/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (성공 - 200):**
```json
{
  "success": true,
  "message": "로그인이 성공적으로 완료되었습니다.",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "nickname": "길동이"
}
```

**Response (실패 - 400):**
```json
{
  "success": false,
  "message": "이메일 또는 비밀번호가 올바르지 않습니다."
}
```

### 3. 이메일 중복 확인

**GET** `/auth/check-email?email=user@example.com`

**Response (200):**
```json
{
  "email": "user@example.com",
  "duplicate": false
}
```

## 🔧 프론트엔드 연동 가이드

### CORS 설정

백엔드에서 이미 CORS가 설정되어 있습니다:
- 허용된 Origin: `http://localhost:3000`, `http://localhost:3001`, `http://127.0.0.1:3000`
- 허용된 Methods: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`
- 허용된 Headers: `*`
- Credentials: `true`

### JWT 토큰 사용법

1. **로그인 후 토큰 저장:**
```javascript
const response = await fetch('/moimlog/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    email: 'user@example.com',
    password: 'password123'
  })
});

const data = await response.json();
if (data.success) {
  // 토큰을 로컬 스토리지나 세션 스토리지에 저장
  localStorage.setItem('accessToken', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
}
```

2. **인증이 필요한 API 호출:**
```javascript
const token = localStorage.getItem('accessToken');
const response = await fetch('/moimlog/api/protected-endpoint', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  }
});
```

### 에러 처리

모든 API는 일관된 에러 응답 형식을 사용합니다:

```json
{
  "success": false,
  "message": "에러 메시지",
  "errorCode": "ERROR_CODE" // 선택적
}
```

## 📝 예제 코드

### React/Next.js 예제

```javascript
// API 클라이언트 설정
const API_BASE_URL = 'http://localhost:8080/moimlog';

class ApiClient {
  static async request(endpoint, options = {}) {
    const token = localStorage.getItem('accessToken');
    
    const config = {
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
        ...options.headers,
      },
      ...options,
    };

    const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || 'API 요청 실패');
    }

    return data;
  }

  // 회원가입
  static async signup(userData) {
    return this.request('/auth/signup', {
      method: 'POST',
      body: JSON.stringify(userData),
    });
  }

  // 로그인
  static async login(credentials) {
    return this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
  }

  // 이메일 중복 확인
  static async checkEmail(email) {
    return this.request(`/auth/check-email?email=${encodeURIComponent(email)}`);
  }
}

// 사용 예제
try {
  // 회원가입
  const signupResult = await ApiClient.signup({
    email: 'test@example.com',
    password: 'password123',
    name: '테스트 사용자',
    nickname: '테스트'
  });

  // 로그인
  const loginResult = await ApiClient.login({
    email: 'test@example.com',
    password: 'password123'
  });

  // 이메일 중복 확인
  const emailCheck = await ApiClient.checkEmail('test@example.com');
  
} catch (error) {
  console.error('API 오류:', error.message);
}
```

## 🔒 보안 고려사항

1. **토큰 보안:**
   - Access Token은 클라이언트 메모리에 저장
   - Refresh Token은 HttpOnly 쿠키에 저장 권장
   - 토큰 만료 시 자동 갱신 로직 구현

2. **HTTPS 사용:**
   - 프로덕션 환경에서는 반드시 HTTPS 사용
   - CORS 설정에서 HTTPS 도메인만 허용

3. **입력 검증:**
   - 클라이언트와 서버 모두에서 입력 검증 수행
   - SQL Injection, XSS 공격 방지

## 🚀 배포 정보

- **개발 환경**: `http://localhost:8080/moimlog`
- **프로덕션 환경**: 배포 후 URL 업데이트 필요
- **데이터베이스**: Railway MySQL 사용

---

**이 문서는 MoimLog 백엔드 API의 기본적인 사용법을 설명합니다. 추가 API가 개발되면 이 문서를 업데이트하세요.** 