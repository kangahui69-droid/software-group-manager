-- ============================================
-- 黄山学院软件小组管理系统 - 数据库一键构建脚本
-- 版本: v5_full_build
-- 适用场景: 全新部署
-- 执行前请先备份数据库！
-- ============================================

-- 请在执行前选中正确的数据库：
-- USE software_group;

-- ============================================
-- 数据库配置（根据实际情况修改）
-- ============================================
-- 数据库名: software_group
-- 用户名: root
-- 密码: kang2005.
-- 编码: utf8mb4

-- ============================================
-- 第一部分：创建数据库（如果不存在）
-- ============================================
CREATE DATABASE IF NOT EXISTS software_group DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE software_group;

-- ============================================
-- 第二部分：用户表
-- ============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` enum('MEMBER','ADMIN','TEACHER') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'MEMBER',
  `status` int DEFAULT '1' COMMENT '1-启用，0-禁用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入默认管理员账号 (密码: admin123)
INSERT INTO `user` VALUES (1,'admin','RerBXm1xrJqSBMIE9v69ZQ==','管理员','admin@hsu.com','13800138000','ADMIN',1,'2026-01-01 00:00:00','2026-01-01 00:00:00');

-- ============================================
-- 第三部分：活动表
-- ============================================
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `activity_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动类型（字典代码）',
  `activity_start_time` datetime DEFAULT NULL COMMENT '活动开始时间',
  `activity_end_time` datetime DEFAULT NULL COMMENT '活动结束时间',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `organizers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组织人（多个用逗号分隔）',
  `contact_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系方式',
  `registration_start_time` datetime DEFAULT NULL COMMENT '报名开始时间',
  `registration_end_time` datetime DEFAULT NULL COMMENT '报名截止时间',
  `max_participants` int DEFAULT NULL,
  `status` enum('upcoming','ongoing','completed','canceled') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'upcoming',
  `approval_status` enum('pending','approved','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'approved',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- ============================================
-- 第四部分：活动报名表
-- ============================================
DROP TABLE IF EXISTS `registration`;
CREATE TABLE `registration` (
  `id` int NOT NULL AUTO_INCREMENT,
  `activity_id` int NOT NULL,
  `user_id` int NOT NULL,
  `status` enum('pending','confirmed','rejected','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `registration_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';

-- ============================================
-- 第五部分：群聊基础表
-- ============================================
DROP TABLE IF EXISTS `activity_group`;
CREATE TABLE `activity_group` (
  `id` int NOT NULL AUTO_INCREMENT,
  `activity_id` int DEFAULT NULL COMMENT '关联的活动ID',
  `group_name` varchar(100) NOT NULL COMMENT '群名称',
  `group_owner_id` int NOT NULL COMMENT '群主用户ID',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_muted` tinyint(1) DEFAULT '0' COMMENT '是否全体禁言 0-否 1-是',
  `muted_until` datetime DEFAULT NULL COMMENT '禁言截止时间',
  `mute_reason` varchar(255) DEFAULT NULL COMMENT '禁言原因',
  PRIMARY KEY (`id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_group_owner_id` (`group_owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动群表';

DROP TABLE IF EXISTS `group_member`;
CREATE TABLE `group_member` (
  `id` int NOT NULL AUTO_INCREMENT,
  `group_id` int NOT NULL COMMENT '群ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `role` enum('OWNER','MEMBER') DEFAULT 'MEMBER' COMMENT '角色：群主/成员',
  `joined_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `muted` tinyint(1) DEFAULT '0' COMMENT '是否被禁言 0-否 1-是',
  `muted_until` datetime DEFAULT NULL COMMENT '禁言截止时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群成员表';

DROP TABLE IF EXISTS `group_message`;
CREATE TABLE `group_message` (
  `id` int NOT NULL AUTO_INCREMENT,
  `group_id` int NOT NULL COMMENT '群ID',
  `sender_id` int NOT NULL COMMENT '发送者ID',
  `content` text NOT NULL COMMENT '消息内容',
  `sent_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(20) DEFAULT 'normal' COMMENT '消息状态 normal-正常 pending-待审核 blocked-已屏蔽',
  `message_type` varchar(20) DEFAULT 'TEXT' COMMENT '消息类型：TEXT-文本消息，FILE-文件消息',
  `file_id` int DEFAULT NULL COMMENT '文件ID（文件消息时使用）',
  `file_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(100) DEFAULT NULL COMMENT '文件MIME类型',
  `file_path` varchar(500) DEFAULT NULL COMMENT '文件存储路径',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_sent_at` (`sent_at`),
  KEY `idx_message_type` (`message_type`),
  KEY `idx_file_id` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群消息表';

DROP TABLE IF EXISTS `user_group`;
CREATE TABLE `user_group` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户ID',
  `group_id` int NOT NULL COMMENT '群ID',
  `joined_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_group` (`user_id`,`group_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户_群关系表';

-- ============================================
-- 第六部分：其他功能表（按需启用）
-- ============================================

-- 文件存储表（群聊文件功能需要）
DROP TABLE IF EXISTS `file_storage`;
CREATE TABLE `file_storage` (
  `id` int NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '存储路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(100) DEFAULT NULL COMMENT 'MIME类型',
  `uploaded_by` int DEFAULT NULL COMMENT '上传者ID',
  `uploaded_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `category` varchar(50) DEFAULT NULL COMMENT '文件分类',
  PRIMARY KEY (`id`),
  KEY `idx_uploaded_by` (`uploaded_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件存储表';

-- 问题反馈表
DROP TABLE IF EXISTS `problem_report`;
CREATE TABLE `problem_report` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  `title` varchar(200) NOT NULL COMMENT '问题标题',
  `content` text NOT NULL COMMENT '问题描述',
  `reporter_name` varchar(100) DEFAULT NULL COMMENT '报告者姓名（游客可填）',
  `reporter_contact` varchar(100) DEFAULT NULL COMMENT '联系方式（游客可填）',
  `reporter_type` enum('GUEST','MEMBER','ADMIN') NOT NULL DEFAULT 'GUEST' COMMENT '报告者类型',
  `user_id` int DEFAULT NULL COMMENT '关联用户ID（如果是成员/管理员）',
  `category` enum('VERIFIED','UNVERIFIED','INVALID') NOT NULL DEFAULT 'UNVERIFIED' COMMENT '问题分类：VERIFIED-属实，UNVERIFIED-待验证，INVALID-不属实',
  `status` enum('PENDING','SOLVING','SOLVED','UNSOLVED') DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理，SOLVING-正在解决，SOLVED-已解决，UNSOLVED-未解决',
  `admin_comment` text DEFAULT NULL COMMENT '管理员备注/回复',
  `handled_by` int DEFAULT NULL COMMENT '处理人ID',
  `handled_at` datetime DEFAULT NULL COMMENT '处理时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题反馈表';

-- 学习会话表
DROP TABLE IF EXISTS `study_session`;
CREATE TABLE `study_session` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户ID',
  `subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学习科目',
  `topic` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学习主题',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` int DEFAULT NULL COMMENT '学习时长（分钟）',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '笔记',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习会话表';

-- 打卡表
DROP TABLE IF EXISTS `attendance`;
CREATE TABLE `attendance` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户ID',
  `check_in_time` datetime NOT NULL COMMENT '打卡时间',
  `subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '打卡科目',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '打卡地点',
  `photo_url` varchar(500) DEFAULT NULL COMMENT '打卡照片URL',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `status` enum('pending','approved','rejected') DEFAULT 'pending' COMMENT '审核状态',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_check_in_time` (`check_in_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡表';

-- ============================================
-- 完成提示
-- ============================================
SELECT '========================================' AS '';
SELECT '数据库构建完成！' AS 'status';
SELECT '========================================' AS '';
SELECT '默认管理员账号信息：' AS 'info';
SELECT '用户名: admin' AS 'admin';
SELECT '密码: admin123 (原始密码，建议修改)' AS 'password';
SELECT '========================================' AS '';
