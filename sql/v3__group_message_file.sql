-- ============================================
-- 群聊文件传送功能 - 数据库脚本
-- 执行前请先备份数据库
-- ============================================

-- 请在执行前选中正确的数据库：
-- USE software_group;

-- ============================================
-- 扩展群消息表，添加文件相关字段
-- ============================================

-- 添加消息类型字段：TEXT(文本消息), FILE(文件消息)
ALTER TABLE group_message ADD COLUMN message_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型：TEXT-文本消息，FILE-文件消息' AFTER content;

-- 添加文件ID字段（关联file_storage表）
ALTER TABLE group_message ADD COLUMN file_id INT DEFAULT NULL COMMENT '文件ID（文件消息时使用）' AFTER message_type;

-- 添加文件名（冗余存储，避免关联查询）
ALTER TABLE group_message ADD COLUMN file_name VARCHAR(255) DEFAULT NULL COMMENT '原始文件名' AFTER file_id;

-- 添加文件大小
ALTER TABLE group_message ADD COLUMN file_size BIGINT DEFAULT NULL COMMENT '文件大小（字节）' AFTER file_name;

-- 添加文件类型（MIME type）
ALTER TABLE group_message ADD COLUMN file_type VARCHAR(100) DEFAULT NULL COMMENT '文件MIME类型' AFTER file_size;

-- 添加文件存储路径（冗余存储）
ALTER TABLE group_message ADD COLUMN file_path VARCHAR(500) DEFAULT NULL COMMENT '文件存储路径' AFTER file_type;

-- 添加索引提升查询效率
ALTER TABLE group_message ADD INDEX idx_message_type (message_type);
ALTER TABLE group_message ADD INDEX idx_file_id (file_id);

-- ============================================
-- 验证表结构
-- ============================================
DESCRIBE group_message;
