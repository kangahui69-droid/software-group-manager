-- 添加群聊禁言字段
ALTER TABLE activity_group ADD COLUMN is_muted TINYINT(1) DEFAULT 0 COMMENT '是否全体禁言 0-否 1-是';
ALTER TABLE activity_group ADD COLUMN muted_until DATETIME DEFAULT NULL COMMENT '禁言截止时间';
ALTER TABLE activity_group ADD COLUMN mute_reason VARCHAR(255) DEFAULT NULL COMMENT '禁言原因';

-- 添加群聊消息审核状态字段（可选，用于监控）
ALTER TABLE group_message ADD COLUMN status VARCHAR(20) DEFAULT 'normal' COMMENT '消息状态 normal-正常 pending-待审核 blocked-已屏蔽';
