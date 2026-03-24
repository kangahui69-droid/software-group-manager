-- 签到签退功能数据库脚本
-- 执行前请先备份数据库！

-- 签到记录表
CREATE TABLE IF NOT EXISTS attendance (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    attendance_date DATE NOT NULL COMMENT '签到日期',
    check_in_time DATETIME COMMENT '签到时间',
    check_out_time DATETIME COMMENT '签退时间',
    check_in_status VARCHAR(20) DEFAULT 'NONE' COMMENT '签到状态：NONE-未签到、NORMAL-正常、LATE-迟到、LEAVE-请假',
    check_out_status VARCHAR(20) DEFAULT 'NONE' COMMENT '签退状态：NONE-未签退、NORMAL-正常、EARLY-早退、LEAVE-请假',
    work_duration INT COMMENT '工作时长（分钟）',
    location VARCHAR(200) COMMENT '签到地点',
    device_info VARCHAR(200) COMMENT '设备信息',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',

    UNIQUE KEY uk_user_date (user_id, attendance_date),
    INDEX idx_user_id (user_id),
    INDEX idx_attendance_date (attendance_date),

    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- 考勤配置表
CREATE TABLE IF NOT EXISTS attendance_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(50) NOT NULL UNIQUE COMMENT '配置键',
    config_value VARCHAR(200) COMMENT '配置值',
    description VARCHAR(500) COMMENT '说明',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤配置表';

-- 初始化考勤配置数据
INSERT INTO attendance_config (config_key, config_value, description) VALUES
('work_start_time', '09:00', '上班开始时间'),
('work_end_time', '18:00', '下班结束时间'),
('late_threshold', '09:30', '迟到阈值（超过此时间算迟到）'),
('early_leave_threshold', '17:30', '早退阈值（早于此时间算早退）')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- 补签申请表（预留第二阶段使用）
CREATE TABLE IF NOT EXISTS attendance_makeup (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    attendance_date DATE NOT NULL,
    make_up_type VARCHAR(20) COMMENT '补签类型：CHECK_IN-补签到、CHECK_OUT-补签退',
    apply_reason VARCHAR(500) COMMENT '申请原因',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待审核、APPROVED-通过、REJECTED-拒绝',
    approve_by INT COMMENT '审批人ID',
    approve_time DATETIME,
    approve_remark VARCHAR(500),

    UNIQUE KEY uk_user_date_type (user_id, attendance_date, make_up_type),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utfmb4 COMMENT='补签申请表';
