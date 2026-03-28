-- 问题管理表（管理员使用）
CREATE TABLE IF NOT EXISTS `problem_management` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '问题ID',
    `report_id` INT NOT NULL COMMENT '关联的问题反馈ID',
    `category` ENUM('VERIFIED', 'INVALID', 'UNVERIFIED') NOT NULL DEFAULT 'UNVERIFIED' COMMENT '分类：VERIFIED-属实，INVALID-不属实，UNVERIFIED-待确认',
    `status` ENUM('PENDING', 'SOLVING', 'SOLVED') NOT NULL DEFAULT 'PENDING' COMMENT '处理状态：PENDING-待修改，SOLVING-正在修改，SOLVED-已修改（仅属实问题有状态）',
    `admin_comment` TEXT DEFAULT NULL COMMENT '管理员备注/回复',
    `handled_by` INT DEFAULT NULL COMMENT '处理人ID',
    `handled_at` DATETIME DEFAULT NULL COMMENT '处理时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_report_id` (`report_id`),
    INDEX `idx_category` (`category`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题管理表';
