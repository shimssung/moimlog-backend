## 🗄️ 데이터베이스 설계

```sql
-- ========================================
-- MoimLog 전체 스키마 (AUTO_INCREMENT)
-- ========================================

-- 1️⃣ 사용자 관련 테이블
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    name VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    profile_image TEXT,
    bio TEXT,
    phone VARCHAR(20),
    birth_date DATE,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP,
    is_onboarding_completed BOOLEAN DEFAULT FALSE,
    oauth_provider VARCHAR(20),
    oauth_provider_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name ENUM('ADMIN', 'USER', 'MODERATOR') NOT NULL UNIQUE,
    description VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_role (user_id, role_id)
);

-- 2️⃣ 이메일 인증 관련 테이블
CREATE TABLE email_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    verification_code VARCHAR(6) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP
);

-- 3️⃣ 관심사 관련 테이블
CREATE TABLE interests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 4️⃣ 모임 카테고리
CREATE TABLE moim_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    label VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    color VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5️⃣ 사용자 관심사 (Interest 매핑)
CREATE TABLE user_interests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    interest_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (interest_id) REFERENCES interests(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_interest (user_id, interest_id)
);

-- 6️⃣ 사용자 모임 카테고리 (MoimCategory 매핑) - 별도 테이블로 분리
CREATE TABLE user_moim_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES moim_categories(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_category (user_id, category_id)
);

-- 7️⃣ 모임 관련
CREATE TABLE moims (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category_id BIGINT,
    tags JSON,
    thumbnail VARCHAR(500),
    max_members INT DEFAULT 0,
    current_members INT DEFAULT 0,
    is_private BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    online_type ENUM('online', 'offline', 'hybrid') DEFAULT 'offline',
    location VARCHAR(500),
    location_detail TEXT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES moim_categories(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE moim_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    moim_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role ENUM('admin', 'moderator', 'member') DEFAULT 'member',
    status ENUM('active', 'pending', 'banned') DEFAULT 'active',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_active_at TIMESTAMP,
    FOREIGN KEY (moim_id) REFERENCES moims(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_moim_user (moim_id, user_id)
);

CREATE TABLE moim_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    moim_id BIGINT NOT NULL,
    allow_anonymous_posts BOOLEAN DEFAULT FALSE,
    require_approval_for_posts BOOLEAN DEFAULT FALSE,
    notification_new_message BOOLEAN DEFAULT TRUE,
    notification_new_post BOOLEAN DEFAULT TRUE,
    notification_new_event BOOLEAN DEFAULT TRUE,
    notification_member_join BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (moim_id) REFERENCES moims(id) ON DELETE CASCADE,
    UNIQUE KEY unique_moim_settings (moim_id)
);

CREATE TABLE user_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    moim_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (moim_id) REFERENCES moims(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_moim (user_id, moim_id)
);

-- 8️⃣ 게시판 관련
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    moim_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    type ENUM('notice', 'free', 'photo', 'event') DEFAULT 'free',
    is_pinned BOOLEAN DEFAULT FALSE,
    is_anonymous BOOLEAN DEFAULT FALSE,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (moim_id) REFERENCES moims(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE post_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    image_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    parent_id BIGINT,
    content TEXT NOT NULL,
    is_anonymous BOOLEAN DEFAULT FALSE,
    like_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
);

CREATE TABLE post_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_post_user (post_id, user_id)
);

CREATE TABLE comment_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_comment_user (comment_id, user_id)
);

-- 9️⃣ 일정 관련
CREATE TABLE schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    moim_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    start_date DATETIME NOT NULL,
    end_date DATETIME,
    location VARCHAR(500),
    location_detail TEXT,
    max_attendees INT,
    type ENUM('meeting', 'task', 'deadline', 'event') DEFAULT 'meeting',
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_rule VARCHAR(200),
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (moim_id) REFERENCES moims(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE schedule_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status ENUM('attending', 'not_attending', 'maybe', 'pending') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_schedule_user (schedule_id, user_id)
);

-- 🔟 채팅 관련
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    moim_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    message_type ENUM('text', 'image', 'file', 'system') DEFAULT 'text',
    file_url VARCHAR(500),
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (moim_id) REFERENCES moims(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE chat_read_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    moim_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    last_read_message_id BIGINT,
    last_read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (moim_id) REFERENCES moims(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (last_read_message_id) REFERENCES chat_messages(id) ON DELETE SET NULL,
    UNIQUE KEY unique_moim_user (moim_id, user_id)
);

-- 1️⃣1️⃣ 알림
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type ENUM('schedule_reminder', 'new_post', 'new_comment', 'moim_update', 'system') NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    related_id BIGINT,
    related_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 1️⃣2️⃣ 파일 업로드
CREATE TABLE file_uploads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    moim_id BIGINT,
    post_id BIGINT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size INT NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    mime_type VARCHAR(100),
    upload_type ENUM('profile', 'moim_thumbnail', 'post_image', 'chat_file') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (moim_id) REFERENCES moims(id) ON DELETE SET NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE SET NULL
);

-- 1️⃣3️⃣ 사용자 활동 로그
CREATE TABLE user_activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    moim_id BIGINT,
    activity_type ENUM('login', 'logout', 'moim_join', 'moim_leave', 'post_create', 'comment_create', 'schedule_create', 'file_upload') NOT NULL,
    activity_data JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (moim_id) REFERENCES moims(id) ON DELETE SET NULL
);

-- 1️⃣4️⃣ 관리자 기능
CREATE TABLE reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    reported_user_id BIGINT,
    reported_moim_id BIGINT,
    reported_post_id BIGINT,
    reported_comment_id BIGINT,
    report_type ENUM('user', 'moim', 'post', 'comment') NOT NULL,
    reason ENUM('spam', 'inappropriate', 'harassment', 'violence', 'other') NOT NULL,
    description TEXT,
    status ENUM('pending', 'investigating', 'resolved', 'dismissed') DEFAULT 'pending',
    admin_notes TEXT,
    resolved_by BIGINT,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reported_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (reported_moim_id) REFERENCES moims(id) ON DELETE SET NULL,
    FOREIGN KEY (reported_post_id) REFERENCES posts(id) ON DELETE SET NULL,
    FOREIGN KEY (reported_comment_id) REFERENCES comments(id) ON DELETE SET NULL,
    FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE admin_actions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    action_type ENUM('user_ban', 'user_unban', 'moim_suspend', 'moim_restore', 'post_delete', 'comment_delete') NOT NULL,
    target_id BIGINT,
    target_type VARCHAR(50),
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ========================================
-- 기본 데이터 삽입
-- ========================================

-- 모임 카테고리 데이터 (10개)
INSERT INTO moim_categories (name, label, description, color) VALUES
('운동/스포츠', '운동/스포츠', '다양한 운동과 스포츠 활동', '#10b981'),
('게임', '게임', '온라인/오프라인 게임 모임', '#f59e0b'),
('독서/스터디', '독서/스터디', '책 읽기와 공부 모임', '#3b82f6'),
('음악', '음악', '음악 감상과 연주 활동', '#8b5cf6'),
('여행', '여행', '국내외 여행 모임', '#06b6d4'),
('요리/베이킹', '요리/베이킹', '요리와 베이킹 활동', '#ef4444'),
('영화/드라마', '영화/드라마', '영화와 드라마 감상', '#ec4899'),
('예술/문화', '예술/문화', '예술과 문화 활동', '#a855f7'),
('IT/기술', 'IT/기술', 'IT와 기술 관련 모임', '#6366f1'),
('기타', '기타', '기타 다양한 모임', '#6b7280');

-- 관심사 데이터 (모임 카테고리와 동일한 데이터로 초기화)
INSERT INTO interests (name, description) VALUES
('운동/스포츠', '다양한 운동과 스포츠 활동'),
('게임', '온라인/오프라인 게임'),
('독서/스터디', '책 읽기와 공부'),
('음악', '음악 감상과 연주'),
('여행', '국내외 여행'),
('요리/베이킹', '요리와 베이킹'),
('영화/드라마', '영화와 드라마 감상'),
('예술/문화', '예술과 문화 활동'),
('IT/기술', 'IT와 기술'),
('기타', '기타 다양한 관심사');

-- 역할 데이터
INSERT INTO roles (name, description) VALUES
('ADMIN', '시스템 관리자'),
('USER', '일반 사용자'),
('MODERATOR', '모임 운영자');

-- ========================================
-- 인덱스
-- ========================================
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_name ON users(name);
CREATE INDEX idx_users_nickname ON users(nickname);
CREATE INDEX idx_users_is_onboarding_completed ON users(is_onboarding_completed);
CREATE INDEX idx_users_oauth_provider ON users(oauth_provider);
CREATE INDEX idx_users_oauth_provider_id ON users(oauth_provider_id);
CREATE INDEX idx_email_verifications_email ON email_verifications(email);
CREATE INDEX idx_email_verifications_verification_code ON email_verifications(verification_code);
CREATE INDEX idx_interests_name ON interests(name);
CREATE INDEX idx_interests_is_active ON interests(is_active);
CREATE INDEX idx_user_interests_user_id ON user_interests(user_id);
CREATE INDEX idx_user_interests_interest_id ON user_interests(interest_id);
CREATE INDEX idx_user_moim_categories_user_id ON user_moim_categories(user_id);
CREATE INDEX idx_user_moim_categories_category_id ON user_moim_categories(category_id);
CREATE INDEX idx_moim_categories_name ON moim_categories(name);
CREATE INDEX idx_moim_categories_is_active ON moim_categories(is_active);
CREATE INDEX idx_moims_category_id ON moims(category_id);
CREATE INDEX idx_moims_created_by ON moims(created_by);
CREATE INDEX idx_moims_online_type ON moims(online_type);
CREATE INDEX idx_moims_is_active ON moims(is_active);
CREATE INDEX idx_posts_moim_id ON posts(moim_id);
CREATE INDEX idx_posts_author_id ON posts(author_id);
CREATE INDEX idx_posts_type ON posts(type);
CREATE INDEX idx_posts_created_at ON posts(created_at);
CREATE INDEX idx_comments_post_id ON comments(post_id);
CREATE INDEX idx_comments_author_id ON comments(author_id);
CREATE INDEX idx_schedules_moim_id ON schedules(moim_id);
CREATE INDEX idx_schedules_start_date ON schedules(start_date);
CREATE INDEX idx_chat_messages_moim_id ON chat_messages(moim_id);
CREATE INDEX idx_chat_messages_created_at ON chat_messages(created_at);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_file_uploads_user_id ON file_uploads(user_id);
CREATE INDEX idx_user_activity_logs_user_id ON user_activity_logs(user_id);
CREATE INDEX idx_user_activity_logs_created_at ON user_activity_logs(created_at);

-- ========================================
-- 뷰
-- ========================================
CREATE VIEW moim_details AS
SELECT
    m.*,
    mc.name as category_name,
    mc.label as category_label,
    mc.color as category_color,
    u.name as creator_name,
    u.profile_image as creator_image
FROM moims m
LEFT JOIN moim_categories mc ON m.category_id = mc.id
LEFT JOIN users u ON m.created_by = u.id;

CREATE VIEW user_interests_details AS
SELECT
    ui.*,
    u.name as user_name,
    u.email as user_email,
    i.name as interest_name,
    i.description as interest_description
FROM user_interests ui
JOIN users u ON ui.user_id = u.id
JOIN interests i ON ui.interest_id = i.id;

CREATE VIEW user_moim_categories_details AS
SELECT
    umc.*,
    u.name as user_name,
    u.email as user_email,
    mc.name as category_name,
    mc.label as category_label,
    mc.color as category_color
FROM user_moim_categories umc
JOIN users u ON umc.user_id = u.id
JOIN moim_categories mc ON umc.category_id = mc.id;

CREATE VIEW moim_member_details AS
SELECT
    mm.*,
    u.name,
    u.email,
    u.profile_image,
    u.last_login_at
FROM moim_members mm
JOIN users u ON mm.user_id = u.id;

CREATE VIEW post_details AS
SELECT
    p.*,
    u.name as author_name,
    u.profile_image as author_image,
    m.title as moim_title
FROM posts p
JOIN users u ON p.author_id = u.id
JOIN moims m ON p.moim_id = m.id;
```
