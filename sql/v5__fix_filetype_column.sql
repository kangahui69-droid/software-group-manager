-- 修复群聊文件上传bug：file_type字段长度不足
-- MIME类型如 application/vnd.openxmlformats-officedocument.wordprocessingml.document 超过50字符

ALTER TABLE file_storage MODIFY COLUMN file_type VARCHAR(200) DEFAULT NULL COMMENT '文件MIME类型';