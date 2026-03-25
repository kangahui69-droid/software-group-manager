-- 学习时段表
CREATE TABLE IF NOT EXISTS `study_session` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `session_date` DATE COMMENT '学习日期',
    `check_in_time` DATETIME COMMENT '开始时间',
    `check_out_time` DATETIME COMMENT '结束时间',
    `duration` INT COMMENT '学习时长（分钟）',
    `status` VARCHAR(20) COMMENT '状态：ACTIVE-进行中，COMPLETED-已完成',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_session_date` (`session_date`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习时段记录表';
