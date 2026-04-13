-- ============================================
-- 黄山学院软件小组管理系统 - 完整数据库脚本
-- 版本: v4.0 (45张表完整版)
-- 日期: 2026-04-13
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 用户与权限相关表 (4张)
-- ============================================

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL COMMENT '用户名/学号',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `name` VARCHAR(50) NOT NULL DEFAULT 'Unknown' COMMENT '姓名',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `role` ENUM('ADMIN','MEMBER','TEACHER','GUEST') NOT NULL DEFAULT 'MEMBER',
  `status` INT DEFAULT '1',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `user` VALUES (1,'admin','RerBXm1xrJqSBMIE9v69ZQ==','管理员','admin@hsu.edu.cn','13800138032','ADMIN',1,'2025-12-21 14:17:28','2026-02-18 01:43:20');

DROP TABLE IF EXISTS `admin_profile`;
CREATE TABLE `admin_profile` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `title` VARCHAR(50) DEFAULT NULL,
  `department` VARCHAR(100) DEFAULT NULL,
  `education` VARCHAR(50) DEFAULT NULL,
  `research_area` VARCHAR(200) DEFAULT NULL,
  `bio` TEXT,
  `status` TINYINT(1) NOT NULL DEFAULT '1',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `avatar_file_id` INT DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `fk_admin_avatar` (`avatar_file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `member_profile`;
CREATE TABLE `member_profile` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `student_id` VARCHAR(20) NOT NULL,
  `major` VARCHAR(100) NOT NULL,
  `grade` VARCHAR(20) NOT NULL,
  `birthday` DATE DEFAULT NULL,
  `gender` ENUM('male','female','other') DEFAULT 'other',
  `introduction` TEXT,
  `skills` TEXT,
  `github` VARCHAR(100) DEFAULT NULL,
  `blog` VARCHAR(100) DEFAULT NULL,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `avatar_file_id` INT DEFAULT NULL,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `fk_member_avatar` (`avatar_file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `recruit_application`;
CREATE TABLE `recruit_application` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `student_id` VARCHAR(50) DEFAULT NULL,
  `major` VARCHAR(100) DEFAULT NULL,
  `grade` VARCHAR(20) DEFAULT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `reason` TEXT,
  `status` TINYINT(1) NOT NULL DEFAULT '1',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 活动相关表 (3张)
-- ============================================

DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `description` TEXT,
  `activity_type` VARCHAR(50) DEFAULT NULL,
  `activity_start_time` DATETIME DEFAULT NULL,
  `activity_end_time` DATETIME DEFAULT NULL,
  `location` VARCHAR(100) DEFAULT NULL,
  `organizers` VARCHAR(255) DEFAULT NULL,
  `contact_info` VARCHAR(100) DEFAULT NULL,
  `registration_start_time` DATETIME DEFAULT NULL,
  `registration_end_time` DATETIME DEFAULT NULL,
  `max_participants` INT DEFAULT NULL,
  `status` ENUM('upcoming','ongoing','completed','canceled') DEFAULT 'upcoming',
  `approval_status` ENUM('pending','approved','rejected') DEFAULT 'approved',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `activity_participant`;
CREATE TABLE `activity_participant` (
  `activity_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `status` ENUM('pending','confirmed','rejected','expired') DEFAULT 'pending',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT NULL,
  `notes` TEXT,
  `deleted` TINYINT DEFAULT '0',
  PRIMARY KEY (`activity_id`,`user_id`),
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `registration`;
CREATE TABLE `registration` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `activity_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `status` ENUM('pending','confirmed','rejected','cancelled') DEFAULT 'pending',
  `registration_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `notes` TEXT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';

-- ============================================
-- 群聊相关表 (4张)
-- ============================================

DROP TABLE IF EXISTS `activity_group`;
CREATE TABLE `activity_group` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `group_name` VARCHAR(100) NOT NULL,
  `group_owner_id` INT NOT NULL,
  `activity_id` INT DEFAULT NULL,
  `is_muted` TINYINT DEFAULT 0,
  `mute_reason` VARCHAR(255) DEFAULT NULL,
  `muted_until` DATETIME DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`group_owner_id`),
  KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `group_member`;
CREATE TABLE `group_member` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `group_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `role` VARCHAR(20) DEFAULT 'MEMBER',
  `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `muted` TINYINT DEFAULT 0,
  `muted_until` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `group_message`;
CREATE TABLE `group_message` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `group_id` INT NOT NULL,
  `sender_id` INT NOT NULL,
  `content` TEXT NOT NULL,
  `sent_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `status` VARCHAR(20) DEFAULT 'normal',
  `message_type` VARCHAR(20) DEFAULT 'TEXT',
  `file_id` INT DEFAULT NULL,
  `file_name` VARCHAR(255) DEFAULT NULL,
  `file_size` BIGINT DEFAULT NULL,
  `file_type` VARCHAR(100) DEFAULT NULL,
  `file_path` VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_sent_at` (`sent_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `user_group`;
CREATE TABLE `user_group` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `group_id` INT NOT NULL,
  `joined_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_group` (`user_id`,`group_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- AI助手相关表 (6张)
-- ============================================

DROP TABLE IF EXISTS `ai_conversation`;
CREATE TABLE `ai_conversation` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT DEFAULT 0,
  `session_id` VARCHAR(64) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_session_id` (`session_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `ai_conversation_log`;
CREATE TABLE `ai_conversation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` VARCHAR(64) DEFAULT NULL,
  `user_id` INT DEFAULT NULL,
  `user_role` VARCHAR(20) DEFAULT NULL,
  `question` TEXT NOT NULL,
  `ai_answer` TEXT,
  `source` VARCHAR(20) DEFAULT NULL,
  `reference_id` INT DEFAULT NULL,
  `rating` TINYINT DEFAULT NULL,
  `is_validated` TINYINT DEFAULT '0',
  `validated_by` INT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `ai_knowledge_base`;
CREATE TABLE `ai_knowledge_base` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `category` VARCHAR(50) DEFAULT NULL,
  `question` VARCHAR(500) NOT NULL,
  `answer` TEXT NOT NULL,
  `keywords` VARCHAR(255) DEFAULT NULL,
  `status` TINYINT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `ai_knowledge_base` (`category`, `question`, `answer`, `keywords`, `status`) VALUES
('游客指南', '如何登录系统？', '访问系统首页，点击右上角"登录"按钮，输入学号和密码即可登录。', '登录,登录系统', 1),
('成员-学习', '如何开始学习签到？', '点击导航"学习中心"，在规定时间（6:00-22:00）内点击"开始学习"按钮进行签到。', '开始学习,签到', 1);

DROP TABLE IF EXISTS `ai_faq_knowledge`;
CREATE TABLE `ai_faq_knowledge` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `category` VARCHAR(50) DEFAULT NULL,
  `question` VARCHAR(500) NOT NULL,
  `answer` TEXT NOT NULL,
  `keywords` VARCHAR(255) DEFAULT NULL,
  `target_role` VARCHAR(20) DEFAULT 'ALL',
  `priority` INT DEFAULT '1',
  `view_count` INT DEFAULT '0',
  `useful_count` INT DEFAULT '0',
  `status` TINYINT DEFAULT '1',
  `verified` TINYINT DEFAULT '0',
  `verified_at` DATETIME DEFAULT NULL,
  `verified_by` INT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_target_role` (`target_role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `ai_message`;
CREATE TABLE `ai_message` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `conversation_id` INT NOT NULL,
  `role` VARCHAR(20) NOT NULL,
  `content` TEXT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `ai_faq_statistics`;
CREATE TABLE `ai_faq_statistics` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `question_hash` VARCHAR(64) UNIQUE,
  `normalized_question` VARCHAR(500),
  `query_count` INT DEFAULT 1,
  `avg_rating` DECIMAL(3,2),
  `last_query_at` DATETIME,
  `suggested_faq` TINYINT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_query_count` (`query_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 考勤相关表 (4张)
-- ============================================

DROP TABLE IF EXISTS `attendance`;
CREATE TABLE `attendance` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `attendance_date` DATE NOT NULL,
  `check_in_time` DATETIME DEFAULT NULL,
  `check_out_time` DATETIME DEFAULT NULL,
  `check_in_status` VARCHAR(20) DEFAULT 'NONE',
  `check_out_status` VARCHAR(20) DEFAULT 'NONE',
  `work_duration` INT DEFAULT NULL,
  `location` VARCHAR(200) DEFAULT NULL,
  `device_info` VARCHAR(200) DEFAULT NULL,
  `remark` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`,`attendance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `attendance_config`;
CREATE TABLE `attendance_config` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(50) NOT NULL,
  `config_value` TEXT,
  `description` VARCHAR(200) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `attendance_makeup`;
CREATE TABLE `attendance_makeup` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `date` DATE NOT NULL,
  `type` VARCHAR(20) NOT NULL,
  `reason` VARCHAR(500) NOT NULL,
  `status` VARCHAR(20) DEFAULT 'pending',
  `approved_by` INT DEFAULT NULL,
  `approved_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `study_session`;
CREATE TABLE `study_session` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `session_name` VARCHAR(100) NOT NULL,
  `subject` VARCHAR(50) DEFAULT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME DEFAULT NULL,
  `duration` INT DEFAULT NULL,
  `content` TEXT,
  `notes` TEXT,
  `status` VARCHAR(20) DEFAULT 'ongoing',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 奖项相关表 (3张)
-- ============================================

DROP TABLE IF EXISTS `award`;
CREATE TABLE `award` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `competition` VARCHAR(200) DEFAULT NULL,
  `year` INT DEFAULT NULL,
  `competition_time` DATE DEFAULT NULL,
  `competition_location` VARCHAR(200) DEFAULT NULL,
  `competition_session` VARCHAR(50) DEFAULT NULL,
  `award_type` INT DEFAULT NULL,
  `award_category` INT DEFAULT NULL,
  `award_level` INT DEFAULT NULL,
  `competition_level` INT DEFAULT NULL,
  `team_name` VARCHAR(100) DEFAULT NULL,
  `award_status` VARCHAR(20) DEFAULT 'PENDING',
  `user_id` INT DEFAULT NULL,
  `created_by` INT DEFAULT NULL,
  `approved_by` INT DEFAULT NULL,
  `approved_at` DATETIME DEFAULT NULL,
  `description` TEXT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`award_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `award_image`;
CREATE TABLE `award_image` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `award_id` INT NOT NULL,
  `member_id` INT DEFAULT NULL,
  `is_main` TINYINT NOT NULL DEFAULT '0',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `file_storage_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_award_id` (`award_id`),
  KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `award_member`;
CREATE TABLE `award_member` (
  `award_id` INT NOT NULL,
  `member_id` INT NOT NULL,
  PRIMARY KEY (`award_id`,`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 项目相关表 (9张)
-- ============================================

DROP TABLE IF EXISTS `project`;
CREATE TABLE `project` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `description` TEXT,
  `category` VARCHAR(50) DEFAULT NULL,
  `year` INT DEFAULT NULL,
  `status` VARCHAR(20) DEFAULT 'pending',
  `leader_id` INT DEFAULT NULL,
  `admin_id` INT DEFAULT NULL,
  `expected_start_date` DATE DEFAULT NULL,
  `expected_end_date` DATE DEFAULT NULL,
  `actual_start_date` DATE DEFAULT NULL,
  `actual_end_date` DATE DEFAULT NULL,
  `repo_url` VARCHAR(500) DEFAULT NULL,
  `doc_url` VARCHAR(500) DEFAULT NULL,
  `budget` DECIMAL(10,2) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_leader_id` (`leader_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `project_file`;
CREATE TABLE `project_file` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `file_id` INT NOT NULL,
  `description` VARCHAR(200) DEFAULT NULL,
  `file_type` VARCHAR(50) DEFAULT NULL,
  `sort_order` INT DEFAULT '0',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_pf_project` (`project_id`),
  KEY `fk_pf_file` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `project_history`;
CREATE TABLE `project_history` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `operation_type` VARCHAR(50) NOT NULL,
  `operator_id` INT NOT NULL,
  `operator_name` VARCHAR(100) DEFAULT NULL,
  `description` TEXT,
  `old_value` TEXT,
  `new_value` TEXT,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_ph_project` (`project_id`),
  KEY `fk_ph_operator` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `project_image`;
CREATE TABLE `project_image` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `file_id` INT NOT NULL,
  `description` VARCHAR(200) DEFAULT NULL,
  `sort_order` INT DEFAULT '0',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_pimg_project` (`project_id`),
  KEY `fk_pimg_file` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `project_label`;
CREATE TABLE `project_label` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `label_code` VARCHAR(50) NOT NULL,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_label` (`project_id`,`label_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `project_member`;
CREATE TABLE `project_member` (
  `project_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `role` VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (`project_id`,`user_id`),
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `project_member_application`;
CREATE TABLE `project_member_application` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `status` VARCHAR(20) DEFAULT 'PENDING',
  `applied_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `handled_at` TIMESTAMP NULL DEFAULT NULL,
  `handled_by` INT DEFAULT NULL,
  `reason` TEXT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`,`user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `project_plan`;
CREATE TABLE `project_plan` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `description` TEXT,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `order_index` INT DEFAULT '0',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_pp_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `project_progress`;
CREATE TABLE `project_progress` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `plan_id` INT DEFAULT NULL,
  `title` VARCHAR(200) NOT NULL,
  `description` TEXT,
  `completion_rate` INT DEFAULT '0',
  `created_by` INT NOT NULL,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_pg_project` (`project_id`),
  KEY `fk_pg_plan` (`plan_id`),
  KEY `fk_pg_creator` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 字典与文件存储 (2张)
-- ============================================

DROP TABLE IF EXISTS `dictionary`;
CREATE TABLE `dictionary` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `type` VARCHAR(50) NOT NULL,
  `sort_order` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `description` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_type` (`code`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `dictionary` (`code`, `name`, `type`, `sort_order`) VALUES
('LEVEL_PROVINCIAL','省级','COMPETITION_LEVEL',1),
('LEVEL_NATIONAL','国家级','COMPETITION_LEVEL',2),
('TYPE_INDIVIDUAL','个人','AWARD_TYPE',1),
('TYPE_TEAM','团队','AWARD_TYPE',2),
('CATEGORY_PROJECT','项目','AWARD_CATEGORY',1),
('CATEGORY_ALGORITHM','经典算法','AWARD_CATEGORY',2),
('CATEGORY_AI','人工智能','AWARD_CATEGORY',3),
('PENDING','待审核','AWARD_STATUS',1),
('APPROVED','已审核','AWARD_STATUS',2),
('REJECTED','已拒绝','AWARD_STATUS',3);

DROP TABLE IF EXISTS `file_storage`;
CREATE TABLE `file_storage` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `create_by` INT NOT NULL,
  `original_name` VARCHAR(255) NOT NULL,
  `stored_name` VARCHAR(255) NOT NULL,
  `file_path` VARCHAR(500) NOT NULL,
  `file_type` VARCHAR(50) DEFAULT NULL,
  `file_size` BIGINT DEFAULT NULL,
  `category` VARCHAR(50) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` TINYINT DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 新闻与日志 (2张)
-- ============================================

DROP TABLE IF EXISTS `news`;
CREATE TABLE `news` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `type` ENUM('award','activity','notice','tech','recruit') NOT NULL DEFAULT 'notice',
  `content_path` VARCHAR(255) NOT NULL,
  `summary` VARCHAR(500) DEFAULT NULL,
  `author_id` INT DEFAULT NULL,
  `status` TINYINT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `author_id` (`author_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` INT DEFAULT NULL,
  `username` VARCHAR(50) DEFAULT NULL,
  `operation` VARCHAR(100) NOT NULL,
  `module` VARCHAR(50) DEFAULT NULL,
  `description` TEXT,
  `ip_address` VARCHAR(50) DEFAULT NULL,
  `user_agent` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation` (`operation`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 问题反馈 (2张)
-- ============================================

DROP TABLE IF EXISTS `problem_report`;
CREATE TABLE `problem_report` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT NOT NULL,
  `reporter_name` VARCHAR(100) DEFAULT NULL,
  `reporter_contact` VARCHAR(100) DEFAULT NULL,
  `reporter_type` ENUM('GUEST','MEMBER','ADMIN') NOT NULL DEFAULT 'GUEST',
  `user_id` INT DEFAULT NULL,
  `category` ENUM('VERIFIED','UNVERIFIED','INVALID') NOT NULL DEFAULT 'UNVERIFIED',
  `status` ENUM('PENDING','SOLVING','SOLVED','UNSOLVED') DEFAULT 'PENDING',
  `admin_comment` TEXT DEFAULT NULL,
  `handled_by` INT DEFAULT NULL,
  `handled_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `problem_management`;
CREATE TABLE `problem_management` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT NOT NULL,
  `reporter_name` VARCHAR(100) DEFAULT NULL,
  `reporter_contact` VARCHAR(100) DEFAULT NULL,
  `reporter_type` ENUM('GUEST','MEMBER','ADMIN') NOT NULL DEFAULT 'GUEST',
  `user_id` INT DEFAULT NULL,
  `category` ENUM('VERIFIED','UNVERIFIED','INVALID') NOT NULL DEFAULT 'UNVERIFIED',
  `status` ENUM('PENDING','SOLVING','SOLVED','UNSOLVED') DEFAULT 'PENDING',
  `admin_comment` TEXT DEFAULT NULL,
  `handled_by` INT DEFAULT NULL,
  `handled_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 简历相关表 (6张)
-- ============================================

DROP TABLE IF EXISTS `resumes`;
CREATE TABLE `resumes` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `resume_name` VARCHAR(100) DEFAULT '我的简历',
  `template_style` VARCHAR(50) DEFAULT 'default',
  `summary` TEXT,
  `career_objective` VARCHAR(500) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `wechat` VARCHAR(50) DEFAULT NULL,
  `github_url` VARCHAR(200) DEFAULT NULL,
  `blog_url` VARCHAR(200) DEFAULT NULL,
  `photo_url` VARCHAR(500) DEFAULT NULL,
  `is_default` TINYINT DEFAULT '0',
  `status` TINYINT DEFAULT '1',
  `view_count` INT DEFAULT '0',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `resume_awards`;
CREATE TABLE `resume_awards` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `resume_id` INT NOT NULL,
  `award_id` INT DEFAULT NULL,
  `award_name` VARCHAR(200) NOT NULL,
  `competition_name` VARCHAR(200) DEFAULT NULL,
  `award_level` VARCHAR(50) DEFAULT NULL,
  `award_date` DATE DEFAULT NULL,
  `award_org` VARCHAR(200) DEFAULT NULL,
  `description` TEXT,
  `is_from_system` TINYINT DEFAULT '0',
  `display_order` INT DEFAULT '0',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`),
  KEY `idx_award_id` (`award_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `resume_educations`;
CREATE TABLE `resume_educations` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `resume_id` INT NOT NULL,
  `school_name` VARCHAR(200) NOT NULL,
  `major` VARCHAR(100) DEFAULT NULL,
  `degree` VARCHAR(50) DEFAULT NULL,
  `start_date` DATE DEFAULT NULL,
  `end_date` DATE DEFAULT NULL,
  `is_current` TINYINT DEFAULT '0',
  `description` TEXT,
  `display_order` INT DEFAULT '0',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `resume_projects`;
CREATE TABLE `resume_projects` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `resume_id` INT NOT NULL,
  `project_name` VARCHAR(200) NOT NULL,
  `role` VARCHAR(100) DEFAULT NULL,
  `team_size` INT DEFAULT NULL,
  `start_date` DATE DEFAULT NULL,
  `end_date` DATE DEFAULT NULL,
  `is_current` TINYINT DEFAULT '0',
  `description` TEXT,
  `responsibilities` TEXT,
  `technologies` VARCHAR(500) DEFAULT NULL,
  `project_url` VARCHAR(500) DEFAULT NULL,
  `achievements` TEXT,
  `display_order` INT DEFAULT '0',
  `is_from_system` TINYINT DEFAULT '0',
  `system_project_id` INT DEFAULT NULL,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `resume_skills`;
CREATE TABLE `resume_skills` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `resume_id` INT NOT NULL,
  `skill_name` VARCHAR(100) NOT NULL,
  `proficiency` VARCHAR(20) DEFAULT 'intermediate',
  `proficiency_score` INT DEFAULT NULL,
  `category` VARCHAR(50) DEFAULT NULL,
  `description` VARCHAR(500) DEFAULT NULL,
  `display_order` INT DEFAULT '0',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `resume_submissions`;
CREATE TABLE `resume_submissions` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `resume_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `target_type` VARCHAR(50) DEFAULT NULL,
  `target_id` INT DEFAULT NULL,
  `target_name` VARCHAR(200) DEFAULT NULL,
  `status` TINYINT DEFAULT '0',
  `submit_message` TEXT,
  `feedback` TEXT,
  `submitted_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `viewed_at` TIMESTAMP NULL DEFAULT NULL,
  `replied_at` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 恢复设置
-- ============================================
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 执行完成，共45张表
-- 默认管理员: admin / 123456
-- ============================================
