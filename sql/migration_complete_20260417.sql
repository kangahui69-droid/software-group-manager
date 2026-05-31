-- ============================================
-- 黄山学院软件小组管理系统 - 数据库迁移脚本
-- 版本: v2.0 (增量迁移版)
-- 日期: 2026-04-17
-- 描述: 从旧版升级到最新版的完整增量迁移脚本
-- 兼容: MySQL 5.7+
-- 使用说明: 
--   1. 如果是全新数据库，请使用 database_45tables.sql
--   2. 如果是已存在数据库，执行此脚本即可
-- 执行前请先备份数据库！
-- ============================================

USE software_group;

-- ============================================
-- 第一部分：activity表 - 添加缺失的字段
-- ============================================

-- 添加活动创建者字段（creator_id）
-- 注意：如果字段已存在会报错，可忽略错误继续执行
ALTER TABLE activity ADD COLUMN creator_id INT DEFAULT NULL COMMENT '活动创建者用户ID';

-- 添加索引（如果索引已存在会报错，可忽略错误）
-- ALTER TABLE activity ADD INDEX idx_creator_id (creator_id);

-- ============================================
-- 第二部分：news表 - 添加activity_id字段
-- ============================================

-- 为news表添加activity_id字段
ALTER TABLE news ADD COLUMN activity_id INT DEFAULT NULL COMMENT '关联的活动ID';

-- 添加索引（如果索引已存在会报错，可忽略错误）
-- ALTER TABLE news ADD INDEX idx_news_activity_id (activity_id);

-- ============================================
-- 第三部分：创建活动群表（如果不存在）
-- ============================================
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

-- 为已存在的activity_group表添加缺失的字段（兼容旧版本）
-- 注意：如果字段已存在会报错，可忽略错误继续执行
ALTER TABLE activity_group ADD COLUMN is_muted TINYINT(1) DEFAULT 0 COMMENT '是否全体禁言';
ALTER TABLE activity_group ADD COLUMN muted_until DATETIME DEFAULT NULL COMMENT '禁言截止时间';
ALTER TABLE activity_group ADD COLUMN mute_reason VARCHAR(255) DEFAULT NULL COMMENT '禁言原因';

-- ============================================
-- 第四部分：创建群成员表（如果不存在）
-- ============================================
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

-- 为已存在的group_member表添加缺失的字段（兼容旧版本）
ALTER TABLE group_member ADD COLUMN muted TINYINT(1) DEFAULT 0 COMMENT '是否被禁言 0-否 1-是';
ALTER TABLE group_member ADD COLUMN muted_until DATETIME DEFAULT NULL COMMENT '禁言截止时间';
ALTER TABLE group_member ADD COLUMN last_read_at DATETIME DEFAULT NULL COMMENT '最后阅读时间';

-- ============================================
-- 第五部分：创建群消息表（如果不存在）
-- ============================================
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

-- 为已存在的group_message表添加缺失的字段（兼容旧版本）
ALTER TABLE group_message ADD COLUMN status VARCHAR(20) DEFAULT 'normal' COMMENT '消息状态';
ALTER TABLE group_message ADD COLUMN message_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型';
ALTER TABLE group_message ADD COLUMN file_id INT DEFAULT NULL COMMENT '文件ID';
ALTER TABLE group_message ADD COLUMN file_name VARCHAR(255) DEFAULT NULL COMMENT '文件名';
ALTER TABLE group_message ADD COLUMN file_size BIGINT DEFAULT NULL COMMENT '文件大小';
ALTER TABLE group_message ADD COLUMN file_type VARCHAR(100) DEFAULT NULL COMMENT '文件类型';
ALTER TABLE group_message ADD COLUMN file_path VARCHAR(500) DEFAULT NULL COMMENT '文件路径';

-- 添加索引（如果索引已存在会报错，可忽略错误）
-- ALTER TABLE group_message ADD INDEX idx_message_type (message_type);
-- ALTER TABLE group_message ADD INDEX idx_file_id (file_id);

-- ============================================
-- 第六部分：创建用户_群关系表（如果不存在）
-- ============================================
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
-- 第七部分：修复群聊文件上传bug
-- MIME类型如 application/vnd.openxmlformats-officedocument.wordprocessingml.document 超过50字符
-- ============================================

ALTER TABLE file_storage MODIFY COLUMN file_type VARCHAR(200) DEFAULT NULL COMMENT '文件MIME类型';

-- ============================================
-- 迁移完成
-- ============================================