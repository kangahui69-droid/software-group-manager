-- 问题反馈表
CREATE TABLE IF NOT EXISTS `problem_report` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '问题ID',
    `title` VARCHAR(200) NOT NULL COMMENT '问题标题',
    `content` TEXT NOT NULL COMMENT '问题描述',
    `reporter_name` VARCHAR(100) DEFAULT NULL COMMENT '报告者姓名（游客可填）',
    `reporter_contact` VARCHAR(100) DEFAULT NULL COMMENT '联系方式（游客可填）',
    `reporter_type` ENUM('GUEST', 'MEMBER', 'ADMIN') NOT NULL DEFAULT 'GUEST' COMMENT '报告者类型',
    `user_id` INT DEFAULT NULL COMMENT '关联用户ID（如果是成员/管理员）',
    `category` ENUM('VERIFIED', 'UNVERIFIED', 'INVALID') NOT NULL DEFAULT 'UNVERIFIED' COMMENT '问题分类：VERIFIED-属实，UNVERIFIED-待验证，INVALID-不属实',
    `status` ENUM('PENDING', 'SOLVING', 'SOLVED', 'UNSOLVED') DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理，SOLVING-正在解决，SOLVED-已解决，UNSOLVED-未解决（仅属实问题有状态）',
    `admin_comment` TEXT DEFAULT NULL COMMENT '管理员备注/回复',
    `handled_by` INT DEFAULT NULL COMMENT '处理人ID',
    `handled_at` DATETIME DEFAULT NULL COMMENT '处理时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_category` (`category`),
    INDEX `idx_status` (`status`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题反馈表';