-- ============================================
-- 从旧版WAR包升级到最新版数据库的增量脚本
-- 版本: upgrade_from_old_war
-- 适用场景: 已有旧版数据库，需要升级
-- 执行前请先备份数据库！
-- ============================================

USE software_group;

-- ============================================
-- 第一部分：群聊禁言功能（activity_group表）
-- ============================================
ALTER TABLE activity_group ADD COLUMN is_muted TINYINT(1) DEFAULT 0 COMMENT '是否全体禁言 0-否 1-是';
ALTER TABLE activity_group ADD COLUMN muted_until DATETIME DEFAULT NULL COMMENT '禁言截止时间';
ALTER TABLE activity_group ADD COLUMN mute_reason VARCHAR(255) DEFAULT NULL COMMENT '禁言原因';

-- ============================================
-- 第二部分：群成员禁言（group_member表）
-- ============================================
ALTER TABLE group_member ADD COLUMN muted TINYINT(1) DEFAULT 0 COMMENT '是否被禁言 0-否 1-是';
ALTER TABLE group_member ADD COLUMN muted_until DATETIME DEFAULT NULL COMMENT '禁言截止时间';

-- ============================================
-- 第三部分：群消息扩展（group_message表）
-- ============================================
ALTER TABLE group_message ADD COLUMN status VARCHAR(20) DEFAULT 'normal' COMMENT '消息状态 normal-正常 pending-待审核 blocked-已屏蔽';
ALTER TABLE group_message ADD COLUMN message_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型：TEXT-文本消息，FILE-文件消息';
ALTER TABLE group_message ADD COLUMN file_id INT DEFAULT NULL COMMENT '文件ID（文件消息时使用）';
ALTER TABLE group_message ADD COLUMN file_name VARCHAR(255) DEFAULT NULL COMMENT '原始文件名';
ALTER TABLE group_message ADD COLUMN file_size BIGINT DEFAULT NULL COMMENT '文件大小（字节）';
ALTER TABLE group_message ADD COLUMN file_type VARCHAR(100) DEFAULT NULL COMMENT '文件MIME类型';
ALTER TABLE group_message ADD COLUMN file_path VARCHAR(500) DEFAULT NULL COMMENT '文件存储路径';

-- 添加索引
ALTER TABLE group_message ADD INDEX idx_message_type (message_type);
ALTER TABLE group_message ADD INDEX idx_file_id (file_id);

-- ============================================
-- 第四部分：创建群聊相关表（如果不存在）
-- ============================================

-- 创建活动群表（如果不存在）
CREATE TABLE IF NOT EXISTS activity_group (
    id INT NOT NULL AUTO_INCREMENT,
    activity_id INT DEFAULT NULL COMMENT '关联的活动ID',
    group_name VARCHAR(100) NOT NULL COMMENT '群名称',
    group_owner_id INT NOT NULL COMMENT '群主用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_muted TINYINT(1) DEFAULT 0 COMMENT '是否全体禁言 0-否 1-是',
    muted_until DATETIME DEFAULT NULL COMMENT '禁言截止时间',
    mute_reason VARCHAR(255) DEFAULT NULL COMMENT '禁言原因',
    PRIMARY KEY (id),
    KEY idx_activity_id (activity_id),
    KEY idx_group_owner_id (group_owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建群成员表（如果不存在）
CREATE TABLE IF NOT EXISTS group_member (
    id INT NOT NULL AUTO_INCREMENT,
    group_id INT NOT NULL COMMENT '群ID',
    user_id INT NOT NULL COMMENT '用户ID',
    role ENUM('OWNER', 'MEMBER') DEFAULT 'MEMBER' COMMENT '角色：群主/成员',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    muted TINYINT(1) DEFAULT 0 COMMENT '是否被禁言 0-否 1-是',
    muted_until DATETIME DEFAULT NULL COMMENT '禁言截止时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_user (group_id, user_id),
    KEY idx_user_id (user_id),
    KEY idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建群消息表（如果不存在）
CREATE TABLE IF NOT EXISTS group_message (
    id INT NOT NULL AUTO_INCREMENT,
    group_id INT NOT NULL COMMENT '群ID',
    sender_id INT NOT NULL COMMENT '发送者ID',
    content TEXT NOT NULL COMMENT '消息内容',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'normal' COMMENT '消息状态 normal-正常 pending-待审核 blocked-已屏蔽',
    message_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型：TEXT-文本消息，FILE-文件消息',
    file_id INT DEFAULT NULL COMMENT '文件ID（文件消息时使用）',
    file_name VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    file_size BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
    file_type VARCHAR(100) DEFAULT NULL COMMENT '文件MIME类型',
    file_path VARCHAR(500) DEFAULT NULL COMMENT '文件存储路径',
    PRIMARY KEY (id),
    KEY idx_group_id (group_id),
    KEY idx_sender_id (sender_id),
    KEY idx_sent_at (sent_at),
    KEY idx_message_type (message_type),
    KEY idx_file_id (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户_群关系表（如果不存在）
CREATE TABLE IF NOT EXISTS user_group (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT '用户ID',
    group_id INT NOT NULL COMMENT '群ID',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_group (user_id, group_id),
    KEY idx_user_id (user_id),
    KEY idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 验证表结构
-- ============================================
DESCRIBE activity_group;
DESCRIBE group_member;
DESCRIBE group_message;
DESCRIBE user_group;

SELECT '升级完成！' AS status;
