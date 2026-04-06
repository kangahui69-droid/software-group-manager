-- ============================================
-- 黄山学院软件小组管理系统 - 数据库迁移脚本
-- 版本: v4_group_chat_admin_and_file
-- 功能: 群聊管理功能 + 群聊文件传送功能
-- 执行前请先备份数据库
-- ============================================

-- 请在执行前选中正确的数据库：
-- USE software_group;

-- ============================================
-- 第一部分：群聊禁言管理功能
-- ============================================

-- 添加群聊禁言字段
ALTER TABLE activity_group ADD COLUMN is_muted TINYINT(1) DEFAULT 0 COMMENT '是否全体禁言 0-否 1-是';
ALTER TABLE activity_group ADD COLUMN muted_until DATETIME DEFAULT NULL COMMENT '禁言截止时间';
ALTER TABLE activity_group ADD COLUMN mute_reason VARCHAR(255) DEFAULT NULL COMMENT '禁言原因';

-- ============================================
-- 第二部分：群聊消息扩展功能
-- ============================================

-- 添加消息审核状态字段
ALTER TABLE group_message ADD COLUMN status VARCHAR(20) DEFAULT 'normal' COMMENT '消息状态 normal-正常 pending-待审核 blocked-已屏蔽';

-- 添加消息类型字段：TEXT(文本消息), FILE(文件消息)
ALTER TABLE group_message ADD COLUMN message_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型：TEXT-文本消息，FILE-文件消息' AFTER content;

-- 添加文件ID字段（关联file_storage表）
ALTER TABLE group_message ADD COLUMN file_id INT DEFAULT NULL COMMENT '文件ID（文件消息时使用）' AFTER message_type;

-- 添加文件名（冗余存储，避免关联查询）
ALTER TABLE group_message ADD COLUMN file_name VARCHAR(255) DEFAULT NULL COMMENT '原始文件名' AFTER file_id;

-- 添加文件大小
ALTER TABLE group_message ADD COLUMN file_size BIGINT DEFAULT NULL COMMENT '文件大小（字节）' AFTER file_name;

-- 添加文件类型（MIME type）
ALTER TABLE group_message ADD COLUMN file_type VARCHAR(100) DEFAULT NULL COMMENT '文件MIME类型' AFTER file_type;

-- 添加文件存储路径（冗余存储）
ALTER TABLE group_message ADD COLUMN file_path VARCHAR(500) DEFAULT NULL COMMENT '文件存储路径' AFTER file_type;

-- 添加索引提升查询效率
ALTER TABLE group_message ADD INDEX idx_message_type (message_type);
ALTER TABLE group_message ADD INDEX idx_file_id (file_id);

-- ============================================
-- 验证表结构
-- ============================================
DESCRIBE activity_group;
DESCRIBE group_message;
