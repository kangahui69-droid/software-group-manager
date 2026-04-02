-- 活动群聊功能：添加活动创建者字段
-- 执行前请先备份数据库
-- 请确保已选中正确的数据库

-- 1. 添加活动创建者字段
ALTER TABLE activity 
ADD COLUMN creator_id INT DEFAULT NULL COMMENT '活动创建者用户ID' AFTER approval_status;

-- 添加索引
ALTER TABLE activity 
ADD INDEX idx_creator_id (creator_id),
ADD CONSTRAINT fk_activity_creator FOREIGN KEY (creator_id) REFERENCES user(id) ON DELETE SET NULL;

-- 2. 查看表结构确认
DESCRIBE activity;
