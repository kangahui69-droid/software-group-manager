-- ============================================
-- 黄山学院软件小组管理系统 - 群聊功能完整SQL脚本
-- 版本: v5_group_chat_full
-- 功能: 完整的群聊功能（包含禁言、文件消息等）
-- 适用场景: 全新部署或现有数据库升级
-- 执行前请先备份数据库
-- ============================================

-- 请在执行前选中正确的数据库：
-- USE software_group;

-- ============================================
-- 第一部分：删除旧表（如果存在）
-- 注意：执行前请确保已备份数据！
-- ============================================
-- 如果是全新部署，可以跳过删除步骤
-- 如果是升级，请先备份数据后再执行

-- DROP TABLE IF EXISTS group_message;
-- DROP TABLE IF EXISTS group_member;
-- DROP TABLE IF EXISTS user_group;
-- DROP TABLE IF EXISTS activity_group;

-- ============================================
-- 第二部分：创建活动群表
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

-- ============================================
-- 第三部分：创建群成员表
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

-- ============================================
-- 第四部分：创建群消息表
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

-- ============================================
-- 第五部分：创建用户_群关系表
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
-- 第六部分：添加外键约束（可选）
-- 如果不需要外键约束可以跳过此部分
-- ============================================
-- ALTER TABLE activity_group 
-- ADD CONSTRAINT fk_group_activity FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE SET NULL;
-- 
-- ALTER TABLE activity_group 
-- ADD CONSTRAINT fk_group_owner FOREIGN KEY (group_owner_id) REFERENCES user(id);
-- 
-- ALTER TABLE group_member 
-- ADD CONSTRAINT fk_member_group FOREIGN KEY (group_id) REFERENCES activity_group(id) ON DELETE CASCADE;
-- 
-- ALTER TABLE group_member 
-- ADD CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;
-- 
-- ALTER TABLE group_message 
-- ADD CONSTRAINT fk_message_group FOREIGN KEY (group_id) REFERENCES activity_group(id) ON DELETE CASCADE;
-- 
-- ALTER TABLE group_message 
-- ADD CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES user(id) ON DELETE CASCADE;
-- 
-- ALTER TABLE group_message
-- ADD CONSTRAINT fk_message_file FOREIGN KEY (file_id) REFERENCES file_storage(id) ON DELETE SET NULL;
-- 
-- ALTER TABLE user_group 
-- ADD CONSTRAINT fk_ug_group FOREIGN KEY (group_id) REFERENCES activity_group(id) ON DELETE CASCADE;
-- 
-- ALTER TABLE user_group 
-- ADD CONSTRAINT fk_ug_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;

-- ============================================
-- 验证表结构
-- ============================================
SHOW TABLES;
DESCRIBE activity_group;
DESCRIBE group_member;
DESCRIBE group_message;
DESCRIBE user_group;
