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

-- ============================================
-- 组队申请表
-- 说明：同一用户对同一帖子只能有一条申请记录（唯一约束）。
--       若被拒绝后需重新申请，由应用层删除旧记录或更新状态后重新插入。
-- ============================================
CREATE TABLE IF NOT EXISTS team_applications (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_post_id    BIGINT      NOT NULL,
    applicant_id    BIGINT      NOT NULL,
    message         VARCHAR(500)            COMMENT '申请留言',
    status          TINYINT     NOT NULL DEFAULT 0 COMMENT '0=待审核 1=已通过 2=已拒绝',
    created_at      DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    DEFAULT CURRENT_TIMESTAMP
);

-- 唯一约束：同一用户对同一帖子只能有一条申请
CREATE UNIQUE INDEX IF NOT EXISTS uk_team_post_applicant ON team_applications (team_post_id, applicant_id);
-- 查询索引：帖子作者查看所有申请
CREATE INDEX IF NOT EXISTS idx_ta_team_post ON team_applications (team_post_id, status);
-- 查询索引：申请人查看自己的申请
CREATE INDEX IF NOT EXISTS idx_ta_applicant ON team_applications (applicant_id);

-- ============================================
-- 消息通知表
-- ============================================
CREATE TABLE IF NOT EXISTS notifications (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT      NOT NULL              COMMENT '接收用户 ID',
    type            VARCHAR(30) NOT NULL              COMMENT '通知类型：system=系统通知 application=组队申请通知 comment=评论通知',
    title           VARCHAR(200) NOT NULL             COMMENT '通知标题',
    content         TEXT                              COMMENT '通知内容',
    is_read         TINYINT     NOT NULL DEFAULT 0    COMMENT '0=未读 1=已读',
    related_id      BIGINT                            COMMENT '关联业务 ID（如申请ID、评论ID）',
    related_type    VARCHAR(30)                       COMMENT '关联业务类型（如 team_application、comment）',
    created_at      DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    DEFAULT CURRENT_TIMESTAMP
);

-- 查询索引：按用户查询未读通知（最常用场景）
CREATE INDEX IF NOT EXISTS idx_notif_user_read ON notifications (user_id, is_read);
-- 查询索引：按时间倒序
CREATE INDEX IF NOT EXISTS idx_notif_user_time ON notifications (user_id, created_at);
-- 查询索引：按关联业务跳转
CREATE INDEX IF NOT EXISTS idx_notif_related ON notifications (related_type, related_id);

-- ============================================
-- 评论表
-- ============================================
CREATE TABLE IF NOT EXISTS comments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_post_id    BIGINT      NOT NULL              COMMENT '所属组队帖 ID',
    user_id         BIGINT      NOT NULL              COMMENT '评论用户 ID',
    parent_id       BIGINT                            COMMENT '父评论 ID（NULL 表示顶级评论）',
    content         TEXT        NOT NULL              COMMENT '评论内容',
    like_count      INT         NOT NULL DEFAULT 0    COMMENT '点赞数',
    created_at      DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    DEFAULT CURRENT_TIMESTAMP
);

-- 查询索引：按帖子查评论列表（最常用场景）
CREATE INDEX IF NOT EXISTS idx_comment_post ON comments (team_post_id, created_at);
-- 查询索引：按父评论查回复
CREATE INDEX IF NOT EXISTS idx_comment_parent ON comments (parent_id);
-- 查询索引：按用户查评论
CREATE INDEX IF NOT EXISTS idx_comment_user ON comments (user_id);
