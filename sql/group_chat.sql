-- 群聊功能数据库表
-- 执行前请先备份数据库
-- 请在执行前选中正确的数据库

-- ============================================
-- 1. 活动群表
-- ============================================
CREATE TABLE IF NOT EXISTS activity_group (
    id INT NOT NULL AUTO_INCREMENT,
    activity_id INT DEFAULT NULL COMMENT '关联的活动ID',
    group_name VARCHAR(100) NOT NULL COMMENT '群名称',
    group_owner_id INT NOT NULL COMMENT '群主用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_activity_id (activity_id),
    KEY idx_group_owner_id (group_owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 2. 群成员表
-- ============================================
CREATE TABLE IF NOT EXISTS group_member (
    id INT NOT NULL AUTO_INCREMENT,
    group_id INT NOT NULL COMMENT '群ID',
    user_id INT NOT NULL COMMENT '用户ID',
    role ENUM('OWNER', 'MEMBER') DEFAULT 'MEMBER' COMMENT '角色：群主/成员',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_user (group_id, user_id),
    KEY idx_user_id (user_id),
    KEY idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 3. 站内消息表
-- ============================================
CREATE TABLE IF NOT EXISTS group_message (
    id INT NOT NULL AUTO_INCREMENT,
    group_id INT NOT NULL COMMENT '群ID',
    sender_id INT NOT NULL COMMENT '发送者ID',
    content TEXT NOT NULL COMMENT '消息内容',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_group_id (group_id),
    KEY idx_sender_id (sender_id),
    KEY idx_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 4. 用户_群关系表（用户参与的群列表）
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
-- 外键约束（如果需要可以取消注释）
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
-- ALTER TABLE user_group 
-- ADD CONSTRAINT fk_ug_group FOREIGN KEY (group_id) REFERENCES activity_group(id) ON DELETE CASCADE;
-- 
-- ALTER TABLE user_group 
-- ADD CONSTRAINT fk_ug_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;

-- 查看表结构
SHOW TABLES;
DESCRIBE activity_group;
DESCRIBE group_member;
DESCRIBE group_message;
DESCRIBE user_group;
