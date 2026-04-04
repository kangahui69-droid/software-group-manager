-- ============================================
-- 黄山学院软件小组管理系统 - 群聊功能数据库脚本
-- 执行前请先备份数据库
-- ============================================

-- 请在执行前选中正确的数据库：
-- USE software_group;

-- ============================================
-- 第一部分：活动表迁移（添加 creator_id 字段）
-- ============================================

-- 1. 检查并添加活动创建者字段
-- 注意：如果字段已存在，请手动删除或跳过此步
-- ALTER TABLE activity ADD COLUMN creator_id INT DEFAULT NULL COMMENT '活动创建者用户ID' AFTER approval_status;

-- ============================================
-- 第二部分：群聊功能数据表
-- ============================================

-- 2. 活动群表
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

-- 3. 群成员表
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

-- 4. 群消息表
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

-- 5. 用户_群关系表
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
-- 第三部分：查看表结构（可选）
-- ============================================

SHOW TABLES;
DESCRIBE activity_group;
DESCRIBE group_member;
DESCRIBE group_message;
DESCRIBE user_group;
