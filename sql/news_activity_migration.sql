-- 为news表添加activity_id字段，用于关联活动新闻
ALTER TABLE news 
ADD COLUMN activity_id INT DEFAULT NULL COMMENT '关联的活动ID' AFTER author_id;

-- 添加外键约束（如果需要）
-- ALTER TABLE news 
-- ADD CONSTRAINT fk_news_activity FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE SET NULL;

-- 创建索引以提高查询效率
CREATE INDEX idx_news_activity_id ON news(activity_id);
