# 🚀 MoimLog 백엔드 개발 요구사항

## 📋 프로젝트 개요

**프로젝트명**: MoimLog (모임로그)  
**프로젝트 타입**: 모임 관리 플랫폼  
**프론트엔드**: Next.js (React)  
**백엔드**: Java + Spring Boot  
**데이터베이스**: MySQL

---

## 🏗️ 시스템 아키텍처

### 전체 구조

```
Frontend (Next.js) ←→ Backend (Spring Boot) ←→ Database (MySQL)
```

### 주요 기능

1. **사용자 관리** - 회원가입, 로그인, 소셜 로그인
2. **모임 관리** - 모임 생성, 수정, 삭제, 검색
3. **게시판** - 공지사항, 자유게시판, 사진게시판
4. **일정 관리** - 모임 일정, 작업, 마감일
5. **채팅** - 실시간 채팅
6. **멤버 관리** - 멤버 초대, 역할 관리
7. **알림** - 실시간 알림 시스템

---

## 👥 사용자 관리 (User Management)

### 사용자 정보 구조

```json
{
  "id": "UUID",
  "email": "user@example.com",
  "password": "hashed_password",
  "name": "홍길동",
  "nickname": "길동이",
  "profileImage": "https://...",
  "bio": "자기소개",
  "phone": "010-1234-5678",
  "birthDate": "1990-01-01",
  "gender": "male|female|other",
  "isActive": true,
  "isVerified": false,
  "lastLoginAt": "2024-03-18T10:30:00",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-03-18T10:30:00"
}
```

### 소셜 로그인 지원

- **Google OAuth2**
- **Kakao OAuth2**
- **Naver OAuth2**

### 사용자 역할 (Roles)

- **USER**: 일반 사용자
- **ADMIN**: 관리자
- **MODERATOR**: 모임 운영자

---

## 🏠 모임 관리 (Moim Management)

### 모임 카테고리 테이블

```sql
CREATE TABLE moim_categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    label VARCHAR(50) NOT NULL,
    description TEXT,
    color VARCHAR(7) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 기본 카테고리 데이터 삽입
INSERT INTO moim_categories (id, name, label, description, color) VALUES
('cat-001', 'book', '독서', '책과 관련된 모임', '#3b82f6'),
('cat-002', 'movie', '영화', '영화 감상 및 토론 모임', '#ef4444'),
('cat-003', 'music', '음악', '음악 감상 및 연주 모임', '#8b5cf6'),
('cat-004', 'sports', '스포츠', '운동 및 스포츠 모임', '#10b981'),
('cat-005', 'game', '게임', '게임 관련 모임', '#f59e0b'),
('cat-006', 'other', '기타', '기타 다양한 모임', '#6b7280');
```

### 모임 카테고리 Java Enum

```java
public enum MoimCategory {
    BOOK("book", "독서", "책과 관련된 모임", "#3b82f6"),
    MOVIE("movie", "영화", "영화 감상 및 토론 모임", "#ef4444"),
    MUSIC("music", "음악", "음악 감상 및 연주 모임", "#8b5cf6"),
    SPORTS("sports", "스포츠", "운동 및 스포츠 모임", "#10b981"),
    GAME("game", "게임", "게임 관련 모임", "#f59e0b"),
    OTHER("other", "기타", "기타 다양한 모임", "#6b7280");

    private final String code;
    private final String label;
    private final String description;
    private final String color;
}
```

### 모임 정보 구조

```json
{
  "id": "UUID",
  "title": "북클럽: 시크릿 가든",
  "description": "함께 읽고 토론하는 독서 모임입니다...",
  "categoryId": "cat-001",
  "category": {
    "id": "cat-001",
    "name": "book",
    "label": "독서",
    "description": "책과 관련된 모임",
    "color": "#3b82f6"
  },
  "tags": ["독서", "토론", "문학"],
  "thumbnail": "https://...",
  "maxMembers": 18,
  "currentMembers": 12,
  "isPrivate": false,
  "isActive": true,
  "onlineType": "online|offline|hybrid",
  "location": "서울시 강남구",
  "locationDetail": "상세 주소",
  "createdBy": "user_id",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-03-18T10:30:00"
}
```

### 모임 멤버 구조

```json
{
  "id": "UUID",
  "moimId": "moim_id",
  "userId": "user_id",
  "role": "admin|moderator|member",
  "status": "active|pending|banned",
  "joinedAt": "2024-01-15T00:00:00",
  "lastActiveAt": "2024-03-18T10:30:00"
}
```

---

## 📝 게시판 (Board)

### 게시글 타입

```java
public enum PostType {
    NOTICE("공지사항"),
    FREE("자유게시판"),
    PHOTO("사진게시판");
}
```

### 게시글 구조

```json
{
  "id": "UUID",
  "moimId": "moim_id",
  "authorId": "user_id",
  "title": "다음 모임 준비물 안내",
  "content": "다음 모임에서는...",
  "type": "notice",
  "isPinned": true,
  "likes": 5,
  "comments": 3,
  "createdAt": "2024-03-18T10:00:00",
  "updatedAt": "2024-03-18T10:00:00"
}
```

### 게시글 이미지

```json
{
  "id": "UUID",
  "postId": "post_id",
  "imageUrl": "https://...",
  "order": 1,
  "createdAt": "2024-03-18T10:00:00"
}
```

### 댓글 구조

```json
{
  "id": "UUID",
  "postId": "post_id",
  "authorId": "user_id",
  "content": "댓글 내용",
  "parentId": "parent_comment_id", // 대댓글용
  "likes": 2,
  "createdAt": "2024-03-18T10:00:00",
  "updatedAt": "2024-03-18T10:00:00"
}
```

---

## 📅 일정 관리 (Schedule)

### 일정 타입

```java
public enum ScheduleType {
    MEETING("모임"),
    TASK("작업"),
    DEADLINE("마감일");
}
```

### 일정 구조

```json
{
  "id": "UUID",
  "moimId": "moim_id",
  "title": "북클럽 정기모임",
  "description": "시크릿 가든 3-4장 토론",
  "type": "meeting",
  "date": "2024-03-20T14:00:00",
  "location": "중앙 도서관 3층 세미나실",
  "maxAttendees": 12,
  "createdBy": "user_id",
  "createdAt": "2024-03-18T10:00:00",
  "updatedAt": "2024-03-18T10:00:00"
}
```

### 일정 참석자

```json
{
  "id": "UUID",
  "scheduleId": "schedule_id",
  "userId": "user_id",
  "status": "attending|maybe|declined",
  "createdAt": "2024-03-18T10:00:00"
}
```

---

## 💬 채팅 (Chat)

### 채팅 메시지 구조

```json
{
  "id": "UUID",
  "moimId": "moim_id",
  "authorId": "user_id",
  "content": "메시지 내용",
  "messageType": "text|image|file",
  "fileUrl": "https://...", // 이미지/파일인 경우
  "createdAt": "2024-03-18T10:30:00"
}
```

### 채팅 읽음 상태

```json
{
  "id": "UUID",
  "messageId": "message_id",
  "userId": "user_id",
  "readAt": "2024-03-18T10:35:00"
}
```

---

## 🔔 알림 (Notification)

### 알림 타입

```java
public enum NotificationType {
    MOIM_JOIN("새로운 멤버 가입"),
    MOIM_SCHEDULE("모임 일정 알림"),
    MOIM_COMMENT("새로운 댓글"),
    MOIM_UPDATE("모임 정보 업데이트");
}
```

### 알림 구조

```json
{
  "id": "UUID",
  "userId": "user_id",
  "type": "moim_join",
  "title": "새로운 멤버 가입",
  "message": "김철수님이 시크릿 가든 북클럽에 가입했습니다.",
  "isRead": false,
  "relatedId": "related_entity_id", // 관련 엔티티 ID
  "createdAt": "2024-03-15T14:30:00"
}
```

---

## 🔐 인증 및 보안

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

### 보안 요구사항

- **비밀번호 암호화**: BCrypt 사용
- **JWT 토큰**: Access Token + Refresh Token
- **CORS 설정**: 프론트엔드 도메인 허용
- **API 인증**: 모든 API에 JWT 토큰 검증

---

## 📊 API 엔드포인트 설계

### 인증 관련

```
POST   /moimlog/auth/signup          # 회원가입
POST   /moimlog/auth/login           # 로그인
POST   /moimlog/auth/refresh         # 토큰 갱신
POST   /moimlog/auth/logout          # 로그아웃
GET    /moimlog/auth/me              # 내 정보 조회
PUT    /moimlog/auth/profile         # 프로필 수정
```

### 소셜 로그인

```
GET    /moimlog/auth/oauth2/google   # Google 로그인
GET    /moimlog/auth/oauth2/kakao    # Kakao 로그인
GET    /moimlog/auth/oauth2/naver    # Naver 로그인
```

### 카테고리 관련

```
GET    /moimlog/categories           # 카테고리 목록 조회
GET    /moimlog/categories/{id}      # 카테고리 상세 조회
POST   /moimlog/categories           # 카테고리 생성 (관리자)
PUT    /moimlog/categories/{id}      # 카테고리 수정 (관리자)
DELETE /moimlog/categories/{id}      # 카테고리 삭제 (관리자)
```

### 모임 관련

```
GET    /moimlog/moims                # 모임 목록 조회
POST   /moimlog/moims                # 모임 생성
GET    /moimlog/moims/{id}           # 모임 상세 조회
PUT    /moimlog/moims/{id}           # 모임 수정
DELETE /moimlog/moims/{id}           # 모임 삭제
POST   /moimlog/moims/{id}/join      # 모임 가입
DELETE /moimlog/moims/{id}/leave     # 모임 탈퇴
```

### 게시판 관련

```
GET    /moimlog/moims/{id}/posts     # 게시글 목록
POST   /moimlog/moims/{id}/posts     # 게시글 작성
GET    /moimlog/posts/{id}           # 게시글 상세
PUT    /moimlog/posts/{id}           # 게시글 수정
DELETE /moimlog/posts/{id}           # 게시글 삭제
POST   /moimlog/posts/{id}/likes     # 좋아요
DELETE /moimlog/posts/{id}/likes     # 좋아요 취소
```

### 댓글 관련

```
GET    /moimlog/posts/{id}/comments  # 댓글 목록
POST   /moimlog/posts/{id}/comments  # 댓글 작성
PUT    /moimlog/comments/{id}        # 댓글 수정
DELETE /moimlog/comments/{id}        # 댓글 삭제
```

### 일정 관련

```
GET    /moimlog/moims/{id}/schedules # 일정 목록
POST   /moimlog/moims/{id}/schedules # 일정 생성
GET    /moimlog/schedules/{id}       # 일정 상세
PUT    /moimlog/schedules/{id}       # 일정 수정
DELETE /moimlog/schedules/{id}       # 일정 삭제
POST   /moimlog/schedules/{id}/attend # 일정 참석
DELETE /moimlog/schedules/{id}/attend # 일정 참석 취소
```

### 멤버 관련

```
GET    /moimlog/moims/{id}/members   # 멤버 목록
PUT    /moimlog/moims/{id}/members/{userId}/role # 역할 변경
DELETE /moimlog/moims/{id}/members/{userId} # 멤버 제거
```

### 알림 관련

```
GET    /moimlog/notifications        # 알림 목록
PUT    /moimlog/notifications/{id}/read # 읽음 처리
PUT    /moimlog/notifications/read-all # 모두 읽음 처리
DELETE /moimlog/notifications/{id}   # 알림 삭제
```

---

## 🗄️ 데이터베이스 설계

### 주요 테이블

1. **users** - 사용자 정보
2. **social_logins** - 소셜 로그인 정보
3. **roles** - 사용자 역할
4. **user_roles** - 사용자-역할 매핑
5. **moim_categories** - 모임 카테고리
6. **moims** - 모임 정보
7. **moim_members** - 모임 멤버
8. **posts** - 게시글
9. **post_images** - 게시글 이미지
10. **comments** - 댓글
11. **post_likes** - 게시글 좋아요
12. **comment_likes** - 댓글 좋아요
13. **schedules** - 일정
14. **schedule_participants** - 일정 참석자
15. **chat_messages** - 채팅 메시지
16. **chat_read_status** - 채팅 읽음 상태
17. **notifications** - 알림
18. **reports** - 신고
19. **admin_actions** - 관리자 액션

### 모임 카테고리 테이블 스키마

```sql
CREATE TABLE moim_categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    label VARCHAR(50) NOT NULL,
    description TEXT,
    color VARCHAR(7) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 기본 카테고리 데이터 삽입
INSERT INTO moim_categories (id, name, label, description, color) VALUES
('cat-001', 'book', '독서', '책과 관련된 모임', '#3b82f6'),
('cat-002', 'movie', '영화', '영화 감상 및 토론 모임', '#ef4444'),
('cat-003', 'music', '음악', '음악 감상 및 연주 모임', '#8b5cf6'),
('cat-004', 'sports', '스포츠', '운동 및 스포츠 모임', '#10b981'),
('cat-005', 'game', '게임', '게임 관련 모임', '#f59e0b'),
('cat-006', 'other', '기타', '기타 다양한 모임', '#6b7280');
```

### 인덱스 설계

```sql
-- 사용자 관련
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_name ON users(name);

-- 카테고리 관련
CREATE INDEX idx_moim_categories_name ON moim_categories(name);

-- 모임 관련
CREATE INDEX idx_moims_category_id ON moims(category_id);
CREATE INDEX idx_moims_created_by ON moims(created_by);
CREATE INDEX idx_moim_members_moim_user ON moim_members(moim_id, user_id);

-- 게시글 관련
CREATE INDEX idx_posts_moim_created ON posts(moim_id, created_at);
CREATE INDEX idx_posts_author ON posts(author_id);
CREATE INDEX idx_comments_post_created ON comments(post_id, created_at);

-- 일정 관련
CREATE INDEX idx_schedules_moim_date ON schedules(moim_id, date);
CREATE INDEX idx_schedule_participants_schedule ON schedule_participants(schedule_id);

-- 채팅 관련
CREATE INDEX idx_chat_messages_moim_created ON chat_messages(moim_id, created_at);

-- 알림 관련
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
```

---

## 🔧 기술 스택

### Backend

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security**
- **Spring Data JPA**
- **Spring OAuth2 Client**
- **MySQL 8.0**
- **JWT (jjwt 0.11.5)**
- **Lombok**

### 개발 도구

- **Maven**
- **IntelliJ IDEA / Eclipse**
- **Postman / Insomnia**
- **MySQL Workbench**

### 배포

- **Render** - 백엔드 배포
- **Vercel** - 프론트엔드 배포
- **로컬 MySQL** - 데이터베이스

---

## 📋 개발 우선순위

### Phase 1: 기본 인프라

1. **프로젝트 설정** - Spring Boot 프로젝트 생성
2. **데이터베이스 설계** - 테이블 생성 및 인덱스 설정
3. **사용자 인증** - JWT 기반 인증 시스템
4. **소셜 로그인** - Google, Kakao, Naver OAuth2

### Phase 2: 핵심 기능

1. **카테고리 관리** - 카테고리 CRUD 기능
2. **모임 관리** - CRUD 기능
3. **멤버 관리** - 가입, 탈퇴, 역할 관리
4. **게시판** - 공지사항, 자유게시판, 사진게시판
5. **댓글 시스템** - 댓글 CRUD, 좋아요

### Phase 3: 고급 기능

1. **일정 관리** - 일정 CRUD, 참석 관리
2. **채팅 시스템** - WebSocket 기반 실시간 채팅
3. **알림 시스템** - 실시간 알림
4. **파일 업로드** - 이미지, 파일 업로드

### Phase 4: 관리 기능

1. **관리자 기능** - 사용자 관리, 신고 처리
2. **검색 기능** - 모임, 게시글 검색
3. **통계 기능** - 사용자, 모임 통계
4. **성능 최적화** - 캐싱, 인덱스 최적화

---

## 🚀 시작하기

### 1. 프로젝트 생성

```bash
# Spring Initializr 사용 또는 수동 생성
# https://start.spring.io/
```

### 2. 의존성 추가

```xml
<!-- pom.xml에 위의 의존성들 추가 -->
```

### 3. 설정 파일

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/moimlog
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### 4. 데이터베이스 생성

```sql
CREATE DATABASE moimlog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 📝 참고사항

### 프론트엔드 연동

- **CORS 설정** 필수
- **JWT 토큰** 헤더에 포함
- **파일 업로드** multipart/form-data 지원

### 보안 고려사항

- **SQL Injection** 방지
- **XSS** 방지
- **CSRF** 방지
- **Rate Limiting** 적용

### 성능 고려사항

- **페이징** 처리
- **캐싱** 전략
- **인덱스** 최적화
- **N+1 문제** 해결

---

**이 문서를 기반으로 MoimLog 백엔드 개발을 시작하세요! 🚀**
