-- ============================================
-- AI助手及奖项功能完整迁移脚本 v2.0
-- 日期: 2026-04-12
-- 描述: 包含所有AI功能和奖项功能的数据库迁移
-- ============================================

-- ============================================
-- Part 1: AI助手相关表
-- ============================================

-- AI对话记录表
CREATE TABLE IF NOT EXISTS `ai_conversation` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `user_id` INT DEFAULT 0 COMMENT '用户ID，0表示游客',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID，唯一标识',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话记录表';

-- AI消息表
CREATE TABLE IF NOT EXISTS `ai_message` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `conversation_id` INT NOT NULL COMMENT '会话ID，外键',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：user/assistant/system',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_conversation_id` (`conversation_id`),
    FOREIGN KEY (`conversation_id`) REFERENCES `ai_conversation`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI消息表';

-- AI知识库表
CREATE TABLE IF NOT EXISTS `ai_knowledge_base` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类，如：奖项申报、活动报名',
    `question` VARCHAR(500) NOT NULL COMMENT '问题',
    `answer` TEXT NOT NULL COMMENT '答案',
    `keywords` VARCHAR(255) DEFAULT NULL COMMENT '关键词，用于匹配',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_category` (`category`),
    INDEX `idx_keywords` (`keywords`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI知识库表';

-- AI提问统计表
CREATE TABLE IF NOT EXISTS `ai_faq_statistics` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `question_hash` VARCHAR(64) UNIQUE COMMENT '问题哈希值',
    `normalized_question` VARCHAR(500) COMMENT '标准化问题',
    `query_count` INT DEFAULT 1 COMMENT '查询次数',
    `avg_rating` DECIMAL(3,2) DEFAULT NULL COMMENT '平均评分',
    `last_query_at` DATETIME DEFAULT NULL COMMENT '最后查询时间',
    `suggested_faq` TINYINT DEFAULT 0 COMMENT '是否建议FAQ：1-是，0-否',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_query_count` (`query_count`),
    INDEX `idx_suggested` (`suggested_faq`),
    INDEX `idx_last_query` (`last_query_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI提问统计表';

-- ============================================
-- Part 2: 群聊文件支持
-- ============================================

ALTER TABLE `group_message`
ADD COLUMN IF NOT EXISTS `message_type` VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型：TEXT-文本，FILE-文件',
ADD COLUMN IF NOT EXISTS `file_id` INT DEFAULT NULL COMMENT '文件ID，对应file_storage表',
ADD COLUMN IF NOT EXISTS `file_name` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
ADD COLUMN IF NOT EXISTS `file_size` BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
ADD COLUMN IF NOT EXISTS `file_type` VARCHAR(100) DEFAULT NULL COMMENT '文件MIME类型',
ADD COLUMN IF NOT EXISTS `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件存储路径';

-- ============================================
-- Part 3: 奖项表必要字段检查与添加
-- ============================================

-- 检查award表是否存在必要字段，如果没有则添加
-- 注意：以下ALTER语句需要在award表已有基本结构后执行
-- ALTER TABLE `award` ADD COLUMN IF NOT EXISTS `user_id` INT DEFAULT NULL COMMENT '用户ID';
-- ALTER TABLE `award` ADD COLUMN IF NOT EXISTS `created_by` INT DEFAULT NULL COMMENT '创建人ID';
-- ALTER TABLE `award` ADD COLUMN IF NOT EXISTS `year` INT DEFAULT NULL COMMENT '年份';

-- ============================================
-- Part 4: 清理干扰AI ACTION系统的FAQ
-- ============================================

-- 删除与ACTION功能重复的FAQ（按关键词匹配）
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%发布新闻%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%如何发布新闻%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%创建项目%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%如何创建项目%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%提交奖项%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%如何提交奖项%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%申报奖项%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%申请奖项%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%新闻列表%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%查看新闻%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%动态%' AND category LIKE '%游客%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%活动列表%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%查看活动%';

-- 删除与ACTION功能重复的FAQ（按问题内容匹配）
DELETE FROM ai_knowledge_base WHERE question LIKE '%奖项%' OR keywords LIKE '%奖项%';
DELETE FROM ai_knowledge_base WHERE question LIKE '%项目%' OR keywords LIKE '%项目%';

-- 禁用访客指南中可能被误匹配的FAQ
UPDATE ai_knowledge_base SET status = 0 WHERE category = '游客指南' AND keywords LIKE '%新闻%';
UPDATE ai_knowledge_base SET status = 0 WHERE category = '游客指南' AND keywords LIKE '%活动%';

-- ============================================
-- 执行说明
-- ============================================
-- 1. 执行此脚本前请备份数据库
-- 2. 确保已创建基本数据库结构（user、activity、project等表）
-- 3. 执行: mysql -u root -p software_group < sql/migration_ai_full_20260412.sql
-- 或在MySQL客户端中: source sql/migration_ai_full_20260412.sql
-- ============================================
