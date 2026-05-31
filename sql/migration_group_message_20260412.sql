-- Migration: Add file support columns to group_message table
-- Date: 2026-04-12
-- Description: Add message_type, file_id, file_name, file_size, file_type, file_path columns to support file sharing in group chat

ALTER TABLE group_message
ADD COLUMN message_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型：TEXT-文本，FILE-文件',
ADD COLUMN file_id INT DEFAULT NULL COMMENT '文件ID，对应file_storage表',
ADD COLUMN file_name VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
ADD COLUMN file_size BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
ADD COLUMN file_type VARCHAR(100) DEFAULT NULL COMMENT '文件MIME类型',
ADD COLUMN file_path VARCHAR(500) DEFAULT NULL COMMENT '文件存储路径';
