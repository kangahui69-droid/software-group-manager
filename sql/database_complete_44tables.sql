-- ============================================
-- 黄山学院软件小组管理系统 - 完整数据库脚本
-- 版本: v3.0 (44张表完整版)
-- 日期: 2026-04-13
-- 描述: 包含所有功能的完整数据库结构
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET @OLD_SQL_MODE=@@SQL_MODE;
SET SQL_MODE='NO_AUTO_VALUE_ON_ZERO';

-- ============================================
-- 第一部分：用户与权限相关表
-- ============================================

-- ------------------------------------------
-- 1. 用户表
-- ------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名/学号',
  `password` varchar(100) NOT NULL COMMENT '密码(DES加密)',
  `name` varchar(50) NOT NULL DEFAULT 'Unknown' COMMENT '姓名',
  `email` varchar(100) NOT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `role` enum('ADMIN','MEMBER','TEACHER','GUEST') NOT NULL DEFAULT 'MEMBER' COMMENT '角色',
  `status` int DEFAULT '1' COMMENT '状态:1-激活,0-禁用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 默认管理员账号: admin / 123456 (DES加密)
INSERT INTO `user` VALUES (1,'admin','RerBXm1xrJqSBMIE9v69ZQ==','管理员','admin@hsu.edu.cn','13800138032','ADMIN',1,'2025-12-21 14:17:28','2026-02-18 01:43:20');

-- ------------------------------------------
-- 2. 管理员档案表
-- ------------------------------------------
DROP TABLE IF EXISTS `admin_profile`;
CREATE TABLE `admin_profile` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `title` varchar(50) DEFAULT NULL,
  `department` varchar(100) DEFAULT NULL,
  `education` varchar(50) DEFAULT NULL,
  `research_area` varchar(200) DEFAULT NULL,
  `bio` text,
  `status` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `avatar_file_id` int DEFAULT NULL COMMENT '头像文件ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `fk_admin_avatar` (`avatar_file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员档案表';

-- ------------------------------------------
-- 3. 成员档案表
-- ------------------------------------------
DROP TABLE IF EXISTS `member_profile`;
CREATE TABLE `member_profile` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `student_id` VARCHAR(20) NOT NULL COMMENT '学号',
  `major` VARCHAR(100) NOT NULL COMMENT '专业',
  `grade` VARCHAR(20) NOT NULL COMMENT '年级',
  `birthday` DATE DEFAULT NULL,
  `gender` ENUM('male','female','other') DEFAULT 'other',
  `introduction` TEXT COMMENT '个人简介',
  `skills` TEXT COMMENT '技能特长',
  `github` VARCHAR(100) DEFAULT NULL,
  `blog` VARCHAR(100) DEFAULT NULL,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `avatar_file_id` INT DEFAULT NULL COMMENT '头像文件ID',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `fk_member_avatar` (`avatar_file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成员档案表';

-- ------------------------------------------
-- 4. 招新申请表
-- ------------------------------------------
DROP TABLE IF EXISTS `recruit_application`;
CREATE TABLE `recruit_application` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `student_id` VARCHAR(50) DEFAULT NULL COMMENT '学号',
  `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
  `grade` VARCHAR(20) DEFAULT NULL COMMENT '年级',
  `phone` VARCHAR(20) NOT NULL COMMENT '电话',
  `email` VARCHAR(100) DEFAULT NULL,
  `reason` TEXT COMMENT '申请理由',
  `status` TINYINT(1) NOT NULL DEFAULT '1' COMMENT '状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='招新申请表';

-- ============================================
-- 第二部分：活动相关表
-- ============================================

-- ------------------------------------------
-- 5. 活动表
-- ------------------------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '活动名称',
  `description` text COMMENT '活动描述',
  `activity_type` varchar(50) DEFAULT NULL COMMENT '活动类型',
  `activity_start_time` datetime DEFAULT NULL COMMENT '活动开始时间',
  `activity_end_time` datetime DEFAULT NULL COMMENT '活动结束时间',
  `location` varchar(100) DEFAULT NULL COMMENT '活动地点',
  `organizers` varchar(255) DEFAULT NULL COMMENT '组织人',
  `contact_info` varchar(100) DEFAULT NULL COMMENT '联系方式',
  `registration_start_time` datetime DEFAULT NULL COMMENT '报名开始时间',
  `registration_end_time` datetime DEFAULT NULL COMMENT '报名截止时间',
  `max_participants` int DEFAULT NULL COMMENT '最大参与人数',
  `status` enum('upcoming','ongoing','completed','canceled') DEFAULT 'upcoming',
  `approval_status` enum('pending','approved','rejected') DEFAULT 'approved',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- ------------------------------------------
-- 6. 活动参与表
-- ------------------------------------------
DROP TABLE IF EXISTS `activity_participant`;
CREATE TABLE `activity_participant` (
  `activity_id` int NOT NULL,
  `user_id` int NOT NULL,
  `status` enum('pending','confirmed','rejected','expired') DEFAULT 'pending',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL,
  `notes` text,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`activity_id`,`user_id`),
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动参与表';

-- ------------------------------------------
-- 7. 活动报名表 (独立于activity_participant)
-- ------------------------------------------
DROP TABLE IF EXISTS `registration`;
CREATE TABLE `registration` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `activity_id` INT NOT NULL COMMENT '活动ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `status` ENUM('pending','confirmed','rejected','cancelled') DEFAULT 'pending' COMMENT '报名状态',
  `registration_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `notes` TEXT COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';

-- ============================================
-- 第三部分：群聊相关表
-- ============================================

-- ------------------------------------------
-- 8. 活动群组表
-- ------------------------------------------
DROP TABLE IF EXISTS `activity_group`;
CREATE TABLE `activity_group` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `group_name` VARCHAR(100) NOT NULL COMMENT '群名称',
    `group_owner_id` INT NOT NULL COMMENT '群主ID',
    `activity_id` INT DEFAULT NULL COMMENT '关联活动ID',
    `is_muted` TINYINT DEFAULT 0 COMMENT '是否禁言：0-否，1-是',
    `mute_reason` VARCHAR(255) DEFAULT NULL COMMENT '禁言原因',
    `muted_until` DATETIME DEFAULT NULL COMMENT '禁言截止时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`group_owner_id`),
    KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动群组表';

-- ------------------------------------------
-- 9. 群组成员表
-- ------------------------------------------
DROP TABLE IF EXISTS `group_member`;
CREATE TABLE `group_member` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `group_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `role` VARCHAR(20) DEFAULT 'MEMBER' COMMENT '角色：OWNER-群主，MEMBER-成员',
    `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `muted` TINYINT DEFAULT 0 COMMENT '是否被禁言',
    `muted_until` DATETIME DEFAULT NULL COMMENT '禁言截止时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_user` (`group_id`,`user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组成员表';

-- ------------------------------------------
-- 10. 群消息表（包含文件支持）
-- ------------------------------------------
DROP TABLE IF EXISTS `group_message`;
CREATE TABLE `group_message` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `group_id` INT NOT NULL COMMENT '群ID',
    `sender_id` INT NOT NULL COMMENT '发送者ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `sent_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `status` VARCHAR(20) DEFAULT 'normal' COMMENT '消息状态 normal-正常 pending-待审核 blocked-已屏蔽',
    `message_type` VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型：TEXT-文本消息，FILE-文件消息',
    `file_id` INT DEFAULT NULL COMMENT '文件ID',
    `file_name` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小',
    `file_type` VARCHAR(100) DEFAULT NULL COMMENT '文件MIME类型',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件存储路径',
    PRIMARY KEY (`id`),
    KEY `idx_group_id` (`group_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_sent_at` (`sent_at`),
    KEY `idx_message_type` (`message_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群消息表';

-- ------------------------------------------
-- 11. 用户_群关系表
-- ------------------------------------------
DROP TABLE IF EXISTS `user_group`;
CREATE TABLE `user_group` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `group_id` INT NOT NULL COMMENT '群ID',
    `joined_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_group` (`user_id`,`group_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户_群关系表';

-- ============================================
-- 第四部分：AI助手相关表
-- ============================================

-- ------------------------------------------
-- 12. AI对话记录表
-- ------------------------------------------
DROP TABLE IF EXISTS `ai_conversation`;
CREATE TABLE `ai_conversation` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT DEFAULT 0 COMMENT '用户ID，0表示游客',
  `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID，唯一标识',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_session_id` (`session_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话记录表';

-- ------------------------------------------
-- 13. AI对话日志表
-- ------------------------------------------
DROP TABLE IF EXISTS `ai_conversation_log`;
CREATE TABLE `ai_conversation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` VARCHAR(64) DEFAULT NULL,
  `user_id` INT DEFAULT NULL,
  `user_role` VARCHAR(20) DEFAULT NULL,
  `question` TEXT NOT NULL,
  `ai_answer` TEXT,
  `source` VARCHAR(20) DEFAULT NULL COMMENT '回答来源：faq/llm',
  `reference_id` INT DEFAULT NULL,
  `rating` TINYINT DEFAULT NULL COMMENT '用户评分1-5',
  `is_validated` TINYINT DEFAULT '0' COMMENT '是否已验证：1-是，0-否',
  `validated_by` INT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_session_id` (`session_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话日志表';

-- ------------------------------------------
-- 14. AI知识库表
-- ------------------------------------------
DROP TABLE IF EXISTS `ai_knowledge_base`;
CREATE TABLE `ai_knowledge_base` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
  `question` VARCHAR(500) NOT NULL,
  `answer` TEXT NOT NULL,
  `keywords` VARCHAR(255) DEFAULT NULL,
  `status` TINYINT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_category` (`category`),
  INDEX `idx_keywords` (`keywords`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI知识库表';

-- 初始化AI知识库数据
INSERT INTO `ai_knowledge_base` (`category`, `question`, `answer`, `keywords`, `status`) VALUES
('游客指南', '如何登录系统？', '访问系统首页，点击右上角"登录"按钮，输入学号和密码即可登录。新成员需要管理员审核通过后才能获得账号。', '登录,登录系统,如何登录', 1),
('游客指南', '如何注册账号？', '系统不开放自主注册。新成员需联系管理员进行招新报名，由管理员审核通过后自动创建账号。', '注册,注册账号,新成员', 1),
('游客指南', '可以浏览哪些信息？', '游客可以浏览首页新闻列表、查看活动信息、浏览获奖新闻等公开内容。', '浏览,查看新闻,查看活动', 1),
('成员-学习', '如何开始学习签到？', '点击导航"学习中心"，在规定时间（6:00-22:00）内点击"开始学习"按钮进行签到。', '开始学习,签到,学习签到', 1),
('成员-学习', '学习时间有什么规则？', '学习时间为6:00-22:00。18:00前签到为正常，18:00-19:00为迟到，19:00后为严重迟到。22:00系统自动结束学习。', '学习规则,签到规则', 1);

-- ------------------------------------------
-- 15. AI FAQ知识表（独立于ai_knowledge_base）
-- ------------------------------------------
DROP TABLE IF EXISTS `ai_faq_knowledge`;
CREATE TABLE `ai_faq_knowledge` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'FAQ ID',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
  `question` VARCHAR(500) NOT NULL COMMENT '问题',
  `answer` TEXT NOT NULL COMMENT '答案',
  `keywords` VARCHAR(255) DEFAULT NULL COMMENT '关键词',
  `target_role` VARCHAR(20) DEFAULT 'ALL' COMMENT '目标角色：ADMIN/MEMBER/GUEST/ALL',
  `priority` INT DEFAULT '1' COMMENT '优先级1-5',
  `view_count` INT DEFAULT '0' COMMENT '查看次数',
  `useful_count` INT DEFAULT '0' COMMENT '点赞次数',
  `status` TINYINT DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `verified` TINYINT DEFAULT '0' COMMENT '是否已验证：1-是，0-否',
  `verified_at` DATETIME DEFAULT NULL COMMENT '验证时间',
  `verified_by` INT DEFAULT NULL COMMENT '验证人ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_target_role` (`target_role`),
  KEY `idx_status` (`status`),
  KEY `idx_verified` (`verified`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI FAQ知识表';

-- ------------------------------------------
-- 16. AI消息表
-- ------------------------------------------
DROP TABLE IF EXISTS `ai_message`;
CREATE TABLE `ai_message` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `conversation_id` INT NOT NULL,
  `role` VARCHAR(20) NOT NULL COMMENT '角色：user/assistant/system',
  `content` TEXT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI消息表';

-- ------------------------------------------
-- 17. AI提问统计表
-- ------------------------------------------
DROP TABLE IF EXISTS `ai_faq_statistics`;
CREATE TABLE `ai_faq_statistics` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `question_hash` VARCHAR(64) UNIQUE COMMENT '问题哈希值',
  `normalized_question` VARCHAR(500),
  `query_count` INT DEFAULT 1,
  `avg_rating` DECIMAL(3,2),
  `last_query_at` DATETIME,
  `suggested_faq` TINYINT DEFAULT 0 COMMENT '是否建议作为FAQ：1-是，0-否',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_query_count` (`query_count`),
  INDEX `idx_suggested` (`suggested_faq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI提问统计表';

-- ============================================
-- 第五部分：考勤相关表
-- ============================================

-- ------------------------------------------
-- 18. 考勤表
-- ------------------------------------------
DROP TABLE IF EXISTS `attendance`;
CREATE TABLE `attendance` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `attendance_date` date NOT NULL COMMENT '签到日期',
  `check_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `check_out_time` datetime DEFAULT NULL COMMENT '签退时间',
  `check_in_status` varchar(20) DEFAULT 'NONE' COMMENT '签到状态',
  `check_out_status` varchar(20) DEFAULT 'NONE' COMMENT '签退状态',
  `work_duration` int DEFAULT NULL COMMENT '工作时长（分钟）',
  `location` varchar(200) DEFAULT NULL COMMENT '签到地点',
  `device_info` varchar(200) DEFAULT NULL COMMENT '设备信息',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`,`attendance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤表';

-- ------------------------------------------
-- 19. 考勤配置表
-- ------------------------------------------
DROP TABLE IF EXISTS `attendance_config`;
CREATE TABLE `attendance_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `config_key` varchar(50) NOT NULL,
  `config_value` text,
  `description` varchar(200) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤配置表';

-- ------------------------------------------
-- 20. 考勤补签表
-- ------------------------------------------
DROP TABLE IF EXISTS `attendance_makeup`;
CREATE TABLE `attendance_makeup` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `date` date NOT NULL,
  `type` varchar(20) NOT NULL COMMENT '补签类型：check_in/check_out',
  `reason` varchar(500) NOT NULL,
  `status` varchar(20) DEFAULT 'pending',
  `approved_by` int DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤补签表';

-- ------------------------------------------
-- 21. 学习会话表
-- ------------------------------------------
DROP TABLE IF EXISTS `study_session`;
CREATE TABLE `study_session` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `session_name` VARCHAR(100) NOT NULL COMMENT '会话名称',
  `subject` VARCHAR(50) DEFAULT NULL COMMENT '学习科目',
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME DEFAULT NULL,
  `duration` INT DEFAULT NULL COMMENT '学习时长（分钟）',
  `content` TEXT COMMENT '学习内容',
  `notes` TEXT COMMENT '笔记',
  `status` VARCHAR(20) DEFAULT 'ongoing' COMMENT '状态：ongoing/completed/canceled',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习会话表';

-- ============================================
-- 第六部分：奖项相关表
-- ============================================

-- ------------------------------------------
-- 22. 奖项表
-- ------------------------------------------
DROP TABLE IF EXISTS `award`;
CREATE TABLE `award` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '奖项ID',
  `name` VARCHAR(100) NOT NULL COMMENT '奖项名称/比赛名称',
  `competition` VARCHAR(200) DEFAULT NULL COMMENT '获奖赛事/项目',
  `year` INT DEFAULT NULL COMMENT '年份',
  `competition_time` DATE DEFAULT NULL COMMENT '比赛时间',
  `competition_location` VARCHAR(200) DEFAULT NULL COMMENT '比赛地点',
  `competition_session` VARCHAR(50) DEFAULT NULL COMMENT '比赛届别',
  `award_type` INT DEFAULT NULL COMMENT '奖项类型：5-个人，6-团队',
  `award_category` INT DEFAULT NULL COMMENT '奖项类别：7-项目，8-经典算法，9-人工智能，10-文档类，11-其他',
  `award_level` INT DEFAULT NULL COMMENT '奖项等级：21-一等奖，22-二等奖，23-三等奖，24-优胜奖，25-参与奖',
  `competition_level` INT DEFAULT NULL COMMENT '比赛等级：1-省级，2-国家级，3-地区级，4-其他',
  `team_name` VARCHAR(100) DEFAULT NULL COMMENT '团队名称',
  `award_status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '奖项状态：PENDING/APPROVED/REJECTED',
  `user_id` INT DEFAULT NULL COMMENT '用户ID',
  `created_by` INT DEFAULT NULL COMMENT '创建人ID',
  `approved_by` INT DEFAULT NULL COMMENT '审核人ID',
  `approved_at` DATETIME DEFAULT NULL COMMENT '审核时间',
  `description` TEXT COMMENT '详细描述',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`award_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖项表';

-- ------------------------------------------
-- 23. 奖项图片表
-- ------------------------------------------
DROP TABLE IF EXISTS `award_image`;
CREATE TABLE `award_image` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `award_id` INT NOT NULL COMMENT '奖项ID',
  `member_id` INT DEFAULT NULL COMMENT '奖项成员ID',
  `is_main` TINYINT NOT NULL DEFAULT '0' COMMENT '是否团队主持',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `file_storage_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_award_id` (`award_id`),
  KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖项图片关系表';

-- ------------------------------------------
-- 24. 奖项成员表
-- ------------------------------------------
DROP TABLE IF EXISTS `award_member`;
CREATE TABLE `award_member` (
  `award_id` INT NOT NULL,
  `member_id` INT NOT NULL,
  PRIMARY KEY (`award_id`,`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖项成员表';

-- ============================================
-- 第七部分：项目相关表
-- ============================================

-- ------------------------------------------
-- 25. 项目表
-- ------------------------------------------
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '项目名称',
  `description` TEXT COMMENT '项目描述',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '项目分类',
  `year` INT DEFAULT NULL COMMENT '年份',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态:pending/approved/rejected/completed',
  `leader_id` INT DEFAULT NULL COMMENT '负责人ID',
  `admin_id` INT DEFAULT NULL COMMENT '管理员ID',
  `expected_start_date` DATE DEFAULT NULL COMMENT '预计开始日期',
  `expected_end_date` DATE DEFAULT NULL COMMENT '预计结束日期',
  `actual_start_date` DATE DEFAULT NULL COMMENT '实际开始日期',
  `actual_end_date` DATE DEFAULT NULL COMMENT '实际结束日期',
  `repo_url` VARCHAR(500) DEFAULT NULL COMMENT '仓库地址',
  `doc_url` VARCHAR(500) DEFAULT NULL COMMENT '文档地址',
  `budget` DECIMAL(10,2) DEFAULT NULL COMMENT '预算',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_leader_id` (`leader_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- ------------------------------------------
-- 26. 项目文件表
-- ------------------------------------------
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目文件表';

-- ------------------------------------------
-- 27. 项目历史表
-- ------------------------------------------
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目历史表';

-- ------------------------------------------
-- 28. 项目图片表
-- ------------------------------------------
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目图片表';

-- ------------------------------------------
-- 29. 项目标签表
-- ------------------------------------------
DROP TABLE IF EXISTS `project_label`;
CREATE TABLE `project_label` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `label_code` VARCHAR(50) NOT NULL,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_label` (`project_id`,`label_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目标签表';

-- ------------------------------------------
-- 30. 项目成员表
-- ------------------------------------------
DROP TABLE IF EXISTS `project_member`;
CREATE TABLE `project_member` (
  `project_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `role` VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (`project_id`,`user_id`),
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员表';

-- ------------------------------------------
-- 31. 项目成员申请表
-- ------------------------------------------
DROP TABLE IF EXISTS `project_member_application`;
CREATE TABLE `project_member_application` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/CONFIRMED/REJECTED',
  `applied_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `handled_at` TIMESTAMP NULL DEFAULT NULL,
  `handled_by` INT DEFAULT NULL,
  `reason` TEXT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`,`user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员申请表';

-- ------------------------------------------
-- 32. 项目计划表
-- ------------------------------------------
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目计划表';

-- ------------------------------------------
-- 33. 项目进度表
-- ------------------------------------------
DROP TABLE IF EXISTS `project_progress`;
CREATE TABLE `project_progress` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `plan_id` INT DEFAULT NULL,
  `title` VARCHAR(200) NOT NULL,
  `description` TEXT,
  `completion_rate` INT DEFAULT '0' COMMENT '0-100',
  `created_by` INT NOT NULL,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_pg_project` (`project_id`),
  KEY `fk_pg_plan` (`plan_id`),
  KEY `fk_pg_creator` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目进度表';

-- ============================================
-- 第八部分：字典与文件存储
-- ============================================

-- ------------------------------------------
-- 34. 字典表
-- ------------------------------------------
DROP TABLE IF EXISTS `dictionary`;
CREATE TABLE `dictionary` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '代码',
  `name` varchar(100) NOT NULL COMMENT '名称',
  `type` varchar(50) NOT NULL COMMENT '类型',
  `sort_order` int DEFAULT 0,
  `status` tinyint DEFAULT 1,
  `description` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_type` (`code`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典表';

-- 初始化字典数据
INSERT INTO `dictionary` (`code`, `name`, `type`, `sort_order`) VALUES
('LEVEL_PROVINCIAL','省级','COMPETITION_LEVEL',1),
('LEVEL_NATIONAL','国家级','COMPETITION_LEVEL',2),
('LEVEL_REGIONAL','地区级别','COMPETITION_LEVEL',3),
('LEVEL_OTHER','其他级别','COMPETITION_LEVEL',4),
('TYPE_INDIVIDUAL','个人','AWARD_TYPE',1),
('TYPE_TEAM','团队','AWARD_TYPE',2),
('CATEGORY_PROJECT','项目','AWARD_CATEGORY',1),
('CATEGORY_ALGORITHM','经典算法','AWARD_CATEGORY',2),
('CATEGORY_AI','人工智能','AWARD_CATEGORY',3),
('CATEGORY_DOCUMENT','文档类','AWARD_CATEGORY',4),
('CATEGORY_OTHER','其他','AWARD_CATEGORY',5),
('ROLE_ADMIN','管理员','USER_ROLE',1),
('ROLE_MEMBER','成员','USER_ROLE',2),
('ROLE_STUDENT','学生','USER_ROLE',3),
('PENDING','待审核','AWARD_STATUS',1),
('APPROVED','已审核','AWARD_STATUS',2),
('REJECTED','已拒绝','AWARD_STATUS',3),
('DRAFT','草稿','NEWS_STATUS',1),
('PUBLISHED','已发布','NEWS_STATUS',2),
('ARCHIVED','已归档','NEWS_STATUS',3),
('LEVEL_FIRST','一等奖','AWARD_LEVEL',1),
('LEVEL_SECOND','二等奖','AWARD_LEVEL',2),
('LEVEL_THIRD','三等奖','AWARD_LEVEL',3),
('LEVEL_EXCELLENT','优胜奖','AWARD_LEVEL',4),
('LEVEL_PARTICIPATION','参与奖','AWARD_LEVEL',5),
('LECTURE','讲座','ACTIVITY_TYPE',1),
('COMPETITION','竞赛','ACTIVITY_TYPE',2),
('WORKSHOP','工作坊','ACTIVITY_TYPE',3),
('SEMINAR','研讨会','ACTIVITY_TYPE',4),
('TEA_PARTY','茶话会','ACTIVITY_TYPE',5);

-- ------------------------------------------
-- 35. 文件存储表
-- ------------------------------------------
DROP TABLE IF EXISTS `file_storage`;
CREATE TABLE `file_storage` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `create_by` INT NOT NULL,
  `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `stored_name` VARCHAR(255) NOT NULL COMMENT '存储文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
  `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件MIME类型',
  `file_size` BIGINT DEFAULT NULL COMMENT '文件大小',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` TINYINT DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件存储表';

-- ============================================
-- 第九部分：新闻与日志
-- ============================================

-- ------------------------------------------
-- 36. 新闻表
-- ------------------------------------------
DROP TABLE IF EXISTS `news`;
CREATE TABLE `news` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `type` ENUM('award','activity','notice','tech','recruit') NOT NULL DEFAULT 'notice' COMMENT '类型',
  `content_path` VARCHAR(255) NOT NULL COMMENT '内容路径',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT '摘要',
  `author_id` INT DEFAULT NULL,
  `status` TINYINT DEFAULT 1 COMMENT '状态',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `author_id` (`author_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新闻表';

-- ------------------------------------------
-- 37. 操作日志表
-- ------------------------------------------
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
  KEY `idx_operation` (`operation`),
  KEY `idx_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================
-- 第十部分：问题反馈
-- ============================================

-- ------------------------------------------
-- 38. 问题反馈表
-- ------------------------------------------
DROP TABLE IF EXISTS `problem_report`;
CREATE TABLE `problem_report` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  `title` VARCHAR(200) NOT NULL COMMENT '问题标题',
  `content` TEXT NOT NULL COMMENT '问题描述',
  `reporter_name` VARCHAR(100) DEFAULT NULL COMMENT '报告者姓名',
  `reporter_contact` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
  `reporter_type` ENUM('GUEST','MEMBER','ADMIN') NOT NULL DEFAULT 'GUEST' COMMENT '报告者类型',
  `user_id` INT DEFAULT NULL COMMENT '关联用户ID',
  `category` ENUM('VERIFIED','UNVERIFIED','INVALID') NOT NULL DEFAULT 'UNVERIFIED' COMMENT '问题分类',
  `status` ENUM('PENDING','SOLVING','SOLVED','UNSOLVED') DEFAULT 'PENDING' COMMENT '状态',
  `admin_comment` TEXT DEFAULT NULL COMMENT '管理员备注',
  `handled_by` INT DEFAULT NULL COMMENT '处理人ID',
  `handled_at` DATETIME DEFAULT NULL COMMENT '处理时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题反馈表';

-- ------------------------------------------
-- 39. 问题管理表（独立表）
-- ------------------------------------------
DROP TABLE IF EXISTS `problem_management`;
CREATE TABLE `problem_management` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  `title` VARCHAR(200) NOT NULL COMMENT '问题标题',
  `content` TEXT NOT NULL COMMENT '问题描述',
  `reporter_name` VARCHAR(100) DEFAULT NULL COMMENT '报告者姓名',
  `reporter_contact` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题管理表';

-- ============================================
-- 第十一部分：简历相关表
-- ============================================

-- ------------------------------------------
-- 40. 简历主表
-- ------------------------------------------
DROP TABLE IF EXISTS `resumes`;
CREATE TABLE `resumes` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '简历ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `resume_name` VARCHAR(100) DEFAULT '我的简历' COMMENT '简历名称',
  `template_style` VARCHAR(50) DEFAULT 'default' COMMENT '模板风格',
  `summary` TEXT COMMENT '个人简介',
  `career_objective` VARCHAR(500) DEFAULT NULL COMMENT '求职意向',
  `phone` VARCHAR(20) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `wechat` VARCHAR(50) DEFAULT NULL,
  `github_url` VARCHAR(200) DEFAULT NULL,
  `blog_url` VARCHAR(200) DEFAULT NULL,
  `photo_url` VARCHAR(500) DEFAULT NULL,
  `is_default` TINYINT DEFAULT '0' COMMENT '是否默认',
  `status` TINYINT DEFAULT '1' COMMENT '状态：0-草稿，1-已发布',
  `view_count` INT DEFAULT '0' COMMENT '浏览次数',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历主表';

-- ------------------------------------------
-- 41. 简历-获奖情况关联表
-- ------------------------------------------
DROP TABLE IF EXISTS `resume_awards`;
CREATE TABLE `resume_awards` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `resume_id` INT NOT NULL COMMENT '简历ID',
  `award_id` INT DEFAULT NULL,
  `award_name` VARCHAR(200) NOT NULL COMMENT '奖项名称',
  `competition_name` VARCHAR(200) DEFAULT NULL COMMENT '比赛名称',
  `award_level` VARCHAR(50) DEFAULT NULL COMMENT '奖项等级',
  `award_date` DATE DEFAULT NULL COMMENT '获奖时间',
  `award_org` VARCHAR(200) DEFAULT NULL COMMENT '颁奖机构',
  `description` TEXT COMMENT '获奖描述',
  `is_from_system` TINYINT DEFAULT '0' COMMENT '是否来自系统',
  `display_order` INT DEFAULT '0' COMMENT '显示顺序',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`),
  KEY `idx_award_id` (`award_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历-获奖情况关联表';

-- ------------------------------------------
-- 42. 简历-教育经历表
-- ------------------------------------------
DROP TABLE IF EXISTS `resume_educations`;
CREATE TABLE `resume_educations` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '教育经历ID',
  `resume_id` INT NOT NULL COMMENT '简历ID',
  `school_name` VARCHAR(200) NOT NULL COMMENT '学校名称',
  `major` VARCHAR(100) DEFAULT NULL COMMENT '专业名称',
  `degree` VARCHAR(50) DEFAULT NULL COMMENT '学历/学位',
  `start_date` DATE DEFAULT NULL COMMENT '入学时间',
  `end_date` DATE DEFAULT NULL COMMENT '毕业时间',
  `is_current` TINYINT DEFAULT '0' COMMENT '是否在读',
  `description` TEXT COMMENT '在校经历描述',
  `display_order` INT DEFAULT '0' COMMENT '显示顺序',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历-教育经历表';

-- ------------------------------------------
-- 43. 简历-项目经历表
-- ------------------------------------------
DROP TABLE IF EXISTS `resume_projects`;
CREATE TABLE `resume_projects` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `resume_id` INT NOT NULL COMMENT '简历ID',
  `project_name` VARCHAR(200) NOT NULL COMMENT '项目名称',
  `role` VARCHAR(100) DEFAULT NULL COMMENT '担任角色',
  `team_size` INT DEFAULT NULL COMMENT '团队规模',
  `start_date` DATE DEFAULT NULL COMMENT '开始时间',
  `end_date` DATE DEFAULT NULL COMMENT '结束时间',
  `is_current` TINYINT DEFAULT '0' COMMENT '是否进行中',
  `description` TEXT COMMENT '项目描述',
  `responsibilities` TEXT COMMENT '个人职责',
  `technologies` VARCHAR(500) DEFAULT NULL COMMENT '使用技术',
  `project_url` VARCHAR(500) DEFAULT NULL COMMENT '项目链接',
  `achievements` TEXT COMMENT '项目成果',
  `display_order` INT DEFAULT '0' COMMENT '显示顺序',
  `is_from_system` TINYINT DEFAULT '0' COMMENT '是否来自系统',
  `system_project_id` INT DEFAULT NULL COMMENT '关联的系统项目ID',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历-项目经历表';

-- ------------------------------------------
-- 44. 简历-技能特长表
-- ------------------------------------------
DROP TABLE IF EXISTS `resume_skills`;
CREATE TABLE `resume_skills` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '技能ID',
  `resume_id` INT NOT NULL COMMENT '简历ID',
  `skill_name` VARCHAR(100) NOT NULL COMMENT '技能名称',
  `proficiency` VARCHAR(20) DEFAULT 'intermediate' COMMENT '熟练程度',
  `proficiency_score` INT DEFAULT NULL COMMENT '熟练度分数',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '技能分类',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '技能描述',
  `display_order` INT DEFAULT '0' COMMENT '显示顺序',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历-技能特长表';

-- ------------------------------------------
-- 45. 简历投递记录表
-- ------------------------------------------
DROP TABLE IF EXISTS `resume_submissions`;
CREATE TABLE `resume_submissions` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '投递记录ID',
  `resume_id` INT NOT NULL COMMENT '简历ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `target_type` VARCHAR(50) DEFAULT NULL COMMENT '投递对象类型',
  `target_id` INT DEFAULT NULL COMMENT '投递对象ID',
  `target_name` VARCHAR(200) DEFAULT NULL COMMENT '投递对象名称',
  `status` TINYINT DEFAULT '0' COMMENT '投递状态',
  `submit_message` TEXT COMMENT '投递附言',
  `feedback` TEXT COMMENT '反馈信息',
  `submitted_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `viewed_at` TIMESTAMP NULL DEFAULT NULL,
  `replied_at` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target` (`target_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历投递记录表';

-- ============================================
-- 恢复设置
-- ============================================
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_MODE=@OLD_SQL_MODE;

-- ============================================
-- 执行完成
-- ============================================
-- 表总数: 45张
-- 默认管理员账号: admin / 123456 (DES加密后的密码)
