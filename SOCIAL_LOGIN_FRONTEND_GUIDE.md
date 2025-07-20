# 🚀 MoimLog 소셜 로그인 프론트엔드 구현 가이드

## 📋 개요

MoimLog 백엔드에서 Google OAuth2 소셜 로그인이 구현되었습니다. 프론트엔드에서는 OAuth2 Authorization Code Flow를 사용하여 소셜 로그인을 구현해야 합니다.

---

## 🔗 API 엔드포인트

### **1. 소셜 로그인 URL 조회**

```http
GET /moimlog/oauth2/urls
```

**응답:**

```json
{
  "google": "http://localhost:8080/moimlog/oauth2/authorization/google",
  "kakao": "http://localhost:8080/moimlog/oauth2/authorization/kakao",
  "naver": "http://localhost:8080/moimlog/oauth2/authorization/naver"
}
```

### **2. 소셜 로그인 콜백**

백엔드에서 자동으로 처리되므로 프론트엔드에서 별도 구현 불필요

---

## 🎯 구현 방법

### **Option 1: 리다이렉트 방식 (추천)**

#### **1. 소셜 로그인 버튼 컴포넌트**

```jsx
// components/SocialLoginButton.jsx
import React from "react";

const SocialLoginButton = ({ provider, children, className }) => {
  const handleSocialLogin = async () => {
    try {
      // 백엔드에서 소셜 로그인 URL 조회
      const response = await fetch("http://localhost:8080/moimlog/oauth2/urls");
      const urls = await response.json();

      // 해당 플랫폼의 OAuth2 URL로 리다이렉트
      window.location.href = urls[provider];
    } catch (error) {
      console.error("소셜 로그인 URL 조회 실패:", error);
      alert("소셜 로그인을 시작할 수 없습니다.");
    }
  };

  return (
    <button onClick={handleSocialLogin} className={className}>
      {children}
    </button>
  );
};

export default SocialLoginButton;
```

#### **2. 소셜 로그인 페이지에서 사용**

```jsx
// pages/login.jsx
import React from "react";
import SocialLoginButton from "../components/SocialLoginButton";

const LoginPage = () => {
  return (
    <div className="login-container">
      <h1>MoimLog 로그인</h1>

      {/* 기존 이메일/비밀번호 로그인 폼 */}
      <form className="login-form">{/* ... 기존 로그인 폼 ... */}</form>

      <div className="divider">
        <span>또는</span>
      </div>

      {/* 소셜 로그인 버튼들 */}
      <div className="social-login-buttons">
        <SocialLoginButton provider="google" className="google-login-btn">
          <img src="/icons/google.svg" alt="Google" />
          Google로 로그인
        </SocialLoginButton>

        {/* 카카오, 네이버는 나중에 추가 */}
        {/* 
        <SocialLoginButton 
          provider="kakao"
          className="kakao-login-btn"
        >
          <img src="/icons/kakao.svg" alt="Kakao" />
          카카오로 로그인
        </SocialLoginButton>
        
        <SocialLoginButton 
          provider="naver"
          className="naver-login-btn"
        >
          <img src="/icons/naver.svg" alt="Naver" />
          네이버로 로그인
        </SocialLoginButton>
        */}
      </div>
    </div>
  );
};

export default LoginPage;
```

#### **3. 콜백 처리 페이지**

```jsx
// pages/oauth2-callback.jsx
import React, { useEffect, useState } from "react";
import { useRouter } from "next/router";

const OAuth2CallbackPage = () => {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const handleCallback = async () => {
      try {
        // URL 파라미터에서 토큰 확인
        const urlParams = new URLSearchParams(window.location.search);
        const token = urlParams.get("token");

        if (token) {
          // 토큰을 로컬 스토리지에 저장
          localStorage.setItem("accessToken", token);

          // 사용자 정보를 상태에 저장 (선택사항)
          // const userInfo = JSON.parse(urlParams.get('user') || '{}');
          // localStorage.setItem('userInfo', JSON.stringify(userInfo));

          // 메인 페이지로 리다이렉트
          router.push("/");
        } else {
          setError("로그인에 실패했습니다.");
        }
      } catch (error) {
        console.error("OAuth2 콜백 처리 중 오류:", error);
        setError("로그인 처리 중 오류가 발생했습니다.");
      } finally {
        setIsLoading(false);
      }
    };

    handleCallback();
  }, [router]);

  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>로그인 처리 중...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container">
        <h2>로그인 실패</h2>
        <p>{error}</p>
        <button onClick={() => router.push("/login")}>
          로그인 페이지로 돌아가기
        </button>
      </div>
    );
  }

  return null;
};

export default OAuth2CallbackPage;
```

### **Option 2: 팝업 방식**

```jsx
// components/SocialLoginPopup.jsx
import React from "react";

const SocialLoginPopup = ({
  provider,
  children,
  className,
  onSuccess,
  onError,
}) => {
  const handleSocialLogin = async () => {
    try {
      const response = await fetch("http://localhost:8080/moimlog/oauth2/urls");
      const urls = await response.json();

      // 팝업 창 열기
      const popup = window.open(
        urls[provider],
        "socialLogin",
        "width=500,height=600,scrollbars=yes,resizable=yes"
      );

      // 팝업에서 메시지 수신
      const handleMessage = (event: MessageEvent) => {
        if (event.data.type === "SOCIAL_LOGIN_SUCCESS") {
          const { accessToken, user } = event.data;
          onSuccess(accessToken, user);
          popup?.close();
          window.removeEventListener("message", handleMessage);
        } else if (event.data.type === "SOCIAL_LOGIN_ERROR") {
          onError(event.data.error);
          popup?.close();
          window.removeEventListener("message", handleMessage);
        }
      };

      window.addEventListener("message", handleMessage);
    } catch (error) {
      console.error("소셜 로그인 URL 조회 실패:", error);
      onError("소셜 로그인을 시작할 수 없습니다.");
    }
  };

  return (
    <button onClick={handleSocialLogin} className={className}>
      {children}
    </button>
  );
};

export default SocialLoginPopup;
```

---

## 🎨 CSS 스타일링

### **소셜 로그인 버튼 스타일**

```css
/* styles/social-login.css */
.social-login-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

.social-login-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  font-weight: 500;
}

.social-login-btn:hover {
  background: #f8f9fa;
  border-color: #adb5bd;
}

.social-login-btn img {
  width: 20px;
  height: 20px;
}

/* Google 로그인 버튼 */
.google-login-btn {
  color: #5f6368;
}

.google-login-btn:hover {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 카카오 로그인 버튼 */
.kakao-login-btn {
  background: #fee500;
  border-color: #fee500;
  color: #000;
}

.kakao-login-btn:hover {
  background: #fdd800;
}

/* 네이버 로그인 버튼 */
.naver-login-btn {
  background: #03c75a;
  border-color: #03c75a;
  color: white;
}

.naver-login-btn:hover {
  background: #02b351;
}

/* 로딩 스피너 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  gap: 16px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 에러 컨테이너 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  gap: 16px;
  text-align: center;
}

.error-container h2 {
  color: #e74c3c;
  margin: 0;
}

.error-container button {
  padding: 8px 16px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.error-container button:hover {
  background: #2980b9;
}
```

---

## 🔧 환경 설정

### **1. Next.js 환경변수 설정**

```env
# .env.local
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/moimlog
```

### **2. API 클라이언트 설정**

```javascript
// lib/api.js
const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/moimlog";

export const apiClient = {
  async get(endpoint) {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      headers: {
        "Content-Type": "application/json",
      },
    });
    return response.json();
  },

  async post(endpoint, data) {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    });
    return response.json();
  },
};
```

### **3. 인증 상태 관리**

```javascript
// hooks/useAuth.js
import { useState, useEffect } from "react";

export const useAuth = () => {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      // 토큰이 있으면 사용자 정보 조회
      fetchUserInfo(token);
    } else {
      setIsLoading(false);
    }
  }, []);

  const fetchUserInfo = async (token) => {
    try {
      const response = await fetch("http://localhost:8080/moimlog/auth/me", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const userData = await response.json();
        setUser(userData);
      } else {
        // 토큰이 유효하지 않으면 제거
        localStorage.removeItem("accessToken");
      }
    } catch (error) {
      console.error("사용자 정보 조회 실패:", error);
      localStorage.removeItem("accessToken");
    } finally {
      setIsLoading(false);
    }
  };

  const login = (token, userData) => {
    localStorage.setItem("accessToken", token);
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem("accessToken");
    setUser(null);
  };

  return {
    user,
    isLoading,
    login,
    logout,
  };
};
```

---

## 🧪 테스트 방법

### **1. 개발 서버 실행**

```bash
npm run dev
# 또는
yarn dev
```

### **2. 브라우저에서 테스트**

```
1. http://localhost:3000/login 접속
2. "Google로 로그인" 버튼 클릭
3. Google 로그인 화면에서 로그인
4. 성공하면 메인 페이지로 리다이렉트
```

### **3. 개발자 도구에서 확인**

```
1. Application 탭 → Local Storage
2. accessToken이 저장되었는지 확인
3. Network 탭에서 API 호출 확인
```

---

## ⚠️ 주의사항

### **1. CORS 설정**

- 백엔드에서 `http://localhost:3000`을 허용하도록 설정됨
- 프로덕션에서는 실제 도메인으로 변경 필요

### **2. 보안**

- `accessToken`은 HttpOnly 쿠키로 관리됨
- 프론트엔드에서는 별도 토큰 저장 불필요

### **3. 에러 처리**

- 네트워크 오류, 인증 실패 등 다양한 에러 상황 처리
- 사용자에게 적절한 에러 메시지 표시

### **4. 로딩 상태**

- 소셜 로그인 진행 중 로딩 상태 표시
- 사용자 경험 향상을 위한 UI/UX 고려

---

## 📝 체크리스트

- [ ] 소셜 로그인 버튼 컴포넌트 구현
- [ ] OAuth2 콜백 처리 페이지 구현
- [ ] 인증 상태 관리 훅 구현
- [ ] CSS 스타일링 적용
- [ ] 에러 처리 및 로딩 상태 구현
- [ ] 테스트 및 디버깅 완료

---

## 🆘 문제 해결

### **자주 발생하는 문제들**

1. **CORS 오류**

   - 백엔드 CORS 설정 확인
   - 프론트엔드 도메인이 허용 목록에 포함되었는지 확인

2. **토큰 저장 실패**

   - 브라우저 개발자 도구에서 Local Storage 확인
   - 쿠키 설정 확인

3. **리다이렉트 실패**
   - Google Cloud Console에서 리다이렉트 URI 설정 확인
   - 백엔드 콜백 URL과 일치하는지 확인

---

**문의사항이 있으면 백엔드 개발자에게 연락하세요!** 📞
