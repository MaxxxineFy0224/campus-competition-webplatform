-- ============================================
-- 校园竞赛组队平台 - 建表脚本
-- H2 数据库（兼容 MySQL 语法）
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    avatar        VARCHAR(255),
    school        VARCHAR(100),
    major         VARCHAR(100),
    grade         VARCHAR(20),
    bio           VARCHAR(500),
    skills        VARCHAR(500),
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 竞赛表
CREATE TABLE IF NOT EXISTS competitions (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(100) NOT NULL,
    category      VARCHAR(50)  NOT NULL,
    level         VARCHAR(20),
    organizer     VARCHAR(100),
    deadline      DATE         NOT NULL,
    event_date    DATE,
    location      VARCHAR(100),
    min_team_size INT,
    max_team_size INT,
    description   TEXT,
    website       VARCHAR(255),
    prize         VARCHAR(200),
    image_url     VARCHAR(500),
    status        TINYINT DEFAULT 0,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 组队帖表
CREATE TABLE IF NOT EXISTS team_posts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id  BIGINT       NOT NULL,
    author_id       BIGINT       NOT NULL,
    title           VARCHAR(100),
    description     TEXT         NOT NULL,
    required_skills VARCHAR(500),
    contact         VARCHAR(30)  NOT NULL,
    deadline        DATE         NOT NULL,
    need_count      INT DEFAULT 1,
    current_count   INT DEFAULT 1,
    status          TINYINT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 收藏表
CREATE TABLE IF NOT EXISTS favorites (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    item_id     BIGINT      NOT NULL,
    item_type   VARCHAR(20) NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 联合唯一索引：同一用户不能重复收藏同一对象
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_item ON favorites (user_id, item_id, item_type);
