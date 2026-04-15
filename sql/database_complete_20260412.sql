-- ============================================
-- 黄山学院软件小组管理系统 - 完整数据库脚本
-- 版本: v2.0
-- 日期: 2026-04-12
-- 描述: 包含所有功能的完整数据库结构
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
-- 2. 活动表
-- ------------------------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '活动名称',
  `description` text COMMENT '活动描述',
  `activity_type` varchar(50) DEFAULT NULL COMMENT '活动类型（字典代码）',
  `activity_start_time` datetime DEFAULT NULL COMMENT '活动开始时间',
  `activity_end_time` datetime DEFAULT NULL COMMENT '活动结束时间',
  `location` varchar(100) DEFAULT NULL COMMENT '活动地点',
  `organizers` varchar(255) DEFAULT NULL COMMENT '组织人（多个用逗号分隔）',
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
-- 3. 活动参与表
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
-- 4. 管理员档案表
-- ------------------------------------------
DROP TABLE IF EXISTS `admin_profile`;
CREATE TABLE `admin_profile` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `title` varchar(50) DEFAULT NULL,
  `department` varchar(100) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员档案表';

-- ------------------------------------------
-- 5. AI对话记录表
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
-- 6. AI对话日志表
-- ------------------------------------------
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
  INDEX `idx_session_id` (`session_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话日志表';

-- ------------------------------------------
-- 7. AI知识库表
-- ------------------------------------------
DROP TABLE IF EXISTS `ai_knowledge_base`;
CREATE TABLE `ai_knowledge_base` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `category` VARCHAR(50) DEFAULT NULL,
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
-- 8. AI消息表
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
-- 9. AI提问统计表
-- ------------------------------------------
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
  INDEX `idx_query_count` (`query_count`),
  INDEX `idx_suggested` (`suggested_faq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI提问统计表';

-- ------------------------------------------
-- 10. 考勤表
-- ------------------------------------------
DROP TABLE IF EXISTS `attendance`;
CREATE TABLE `attendance` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `date` date NOT NULL,
  `sign_in_time` datetime DEFAULT NULL,
  `sign_out_time` datetime DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL COMMENT '状态',
  `notes` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`,`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤表';

-- ------------------------------------------
-- 11. 考勤配置表
-- ------------------------------------------
DROP TABLE IF EXISTS `attendance_config`;
CREATE TABLE `attendance_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `config_key` varchar(50) NOT NULL,
  `config_value` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤配置表';

-- ------------------------------------------
-- 12. 考勤补签表
-- ------------------------------------------
DROP TABLE IF EXISTS `attendance_makeup`;
CREATE TABLE `attendance_makeup` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `date` date NOT NULL,
  `sign_in_time` datetime NOT NULL,
  `sign_out_time` datetime DEFAULT NULL,
  `reason` text,
  `status` varchar(20) DEFAULT 'pending',
  `reviewed_by` int DEFAULT NULL,
  `reviewed_at` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤补签表';

-- ------------------------------------------
-- 13. 奖项表
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
-- 14. 奖项图片表
-- ------------------------------------------
DROP TABLE IF EXISTS `award_image`;
CREATE TABLE `award_image` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `award_id` INT NOT NULL COMMENT '奖项ID',
  `file_id` INT DEFAULT NULL COMMENT '文件ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_award_id` (`award_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖项图片表';

-- ------------------------------------------
-- 15. 奖项成员表
-- ------------------------------------------
DROP TABLE IF EXISTS `award_member`;
CREATE TABLE `award_member` (
  `award_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  PRIMARY KEY (`award_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖项成员表';

-- ------------------------------------------
-- 16. 字典表
-- ------------------------------------------
DROP TABLE IF EXISTS `dictionary`;
CREATE TABLE `dictionary` (
  `id` int NOT NULL AUTO_INCREMENT,
  `category` varchar(50) NOT NULL COMMENT '分类',
  `code` varchar(20) NOT NULL COMMENT '代码',
  `name` varchar(100) NOT NULL COMMENT '名称',
  `sort_order` int DEFAULT 0,
  `is_active` tinyint DEFAULT 1,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`category`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=70 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典表';

-- 初始化字典数据
INSERT INTO `dictionary` (`category`, `code`, `name`, `sort_order`) VALUES
('ACTIVITY_TYPE', 'LECTURE', '讲座', 1),
('ACTIVITY_TYPE', 'COMPETITION', '竞赛', 2),
('ACTIVITY_TYPE', 'WORKSHOP', '工作坊', 3),
('ACTIVITY_TYPE', 'SEMINAR', '研讨会', 4),
('ACTIVITY_TYPE', 'TEA_PARTY', '茶话会', 5),
('AWARD_TYPE', '5', '个人', 1),
('AWARD_TYPE', '6', '团队', 2),
('AWARD_CATEGORY', '7', '项目', 1),
('AWARD_CATEGORY', '8', '经典算法', 2),
('AWARD_CATEGORY', '9', '人工智能', 3),
('AWARD_CATEGORY', '10', '文档类', 4),
('AWARD_CATEGORY', '11', '其他', 5),
('AWARD_LEVEL', '21', '一等奖', 1),
('AWARD_LEVEL', '22', '二等奖', 2),
('AWARD_LEVEL', '23', '三等奖', 3),
('AWARD_LEVEL', '24', '优胜奖', 4),
('AWARD_LEVEL', '25', '参与奖', 5),
('COMPETITION_LEVEL', '1', '省级', 1),
('COMPETITION_LEVEL', '2', '国家级', 2),
('COMPETITION_LEVEL', '3', '地区级', 3),
('COMPETITION_LEVEL', '4', '其他', 4);

-- ------------------------------------------
-- 17. 文件存储表
-- ------------------------------------------
DROP TABLE IF EXISTS `file_storage`;
CREATE TABLE `file_storage` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `original_name` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
  `stored_name` VARCHAR(255) DEFAULT NULL COMMENT '存储文件名',
  `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
  `file_type` VARCHAR(100) DEFAULT NULL COMMENT '文件MIME类型',
  `file_size` BIGINT DEFAULT NULL COMMENT '文件大小',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
  `create_by` INT DEFAULT NULL COMMENT '上传用户ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件存储表';

-- ------------------------------------------
-- 18. 成员档案表
-- ------------------------------------------
DROP TABLE IF EXISTS `member_profile`;
CREATE TABLE `member_profile` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `avatar_file_id` INT DEFAULT NULL,
  `bio` TEXT,
  `skills` TEXT,
  `github_url` VARCHAR(255) DEFAULT NULL,
  `blog_url` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成员档案表';

-- ------------------------------------------
-- 19. 新闻表
-- ------------------------------------------
DROP TABLE IF EXISTS `news`;
CREATE TABLE `news` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `summary` TEXT COMMENT '摘要',
  `content` TEXT COMMENT '内容',
  `type` VARCHAR(20) DEFAULT 'activity' COMMENT '类型:activity/notice/tech/award/recruit',
  `status` TINYINT DEFAULT 0 COMMENT '状态:0-待审核,1-已发布',
  `author_id` INT DEFAULT NULL,
  `published_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新闻表';

-- ------------------------------------------
-- 20. 操作日志表
-- ------------------------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `action` VARCHAR(50) NOT NULL,
  `target_type` VARCHAR(50) DEFAULT NULL,
  `target_id` INT DEFAULT NULL,
  `details` TEXT,
  `ip_address` VARCHAR(50) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ------------------------------------------
-- 21. 项目表
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
  `repo_url` VARCHAR(500) DEFAULT NULL COMMENT '仓库地址',
  `budget` DECIMAL(10,2) DEFAULT NULL COMMENT '预算',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_leader_id` (`leader_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- ------------------------------------------
-- 22. 项目文件表
-- ------------------------------------------
DROP TABLE IF EXISTS `project_file`;
CREATE TABLE `project_file` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `file_id` INT DEFAULT NULL,
  `file_name` VARCHAR(255) DEFAULT NULL,
  `file_type` VARCHAR(50) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目文件表';

-- ------------------------------------------
-- 23. 项目历史表
-- ------------------------------------------
DROP TABLE IF EXISTS `project_history`;
CREATE TABLE `project_history` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `action` VARCHAR(50) NOT NULL,
  `details` TEXT,
  `created_by` INT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目历史表';

-- ------------------------------------------
-- 24. 项目图片表
-- ------------------------------------------
DROP TABLE IF EXISTS `project_image`;
CREATE TABLE `project_image` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `file_id` INT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目图片表';

-- ------------------------------------------
-- 25. 项目标签表
-- ------------------------------------------
DROP TABLE IF EXISTS `project_label`;
CREATE TABLE `project_label` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `label` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目标签表';

-- ------------------------------------------
-- 26. 项目成员表
-- ------------------------------------------
DROP TABLE IF EXISTS `project_member`;
CREATE TABLE `project_member` (
  `project_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `role` VARCHAR(20) DEFAULT 'member',
  `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`project_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员表';

-- ------------------------------------------
-- 27. 项目成员申请表
-- ------------------------------------------
DROP TABLE IF EXISTS `project_member_application`;
CREATE TABLE `project_member_application` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `status` VARCHAR(20) DEFAULT 'pending',
  `applied_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员申请表';

-- ------------------------------------------
-- 28. 项目计划表
-- ------------------------------------------
DROP TABLE IF EXISTS `project_plan`;
CREATE TABLE `project_plan` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `description` TEXT,
  `deadline` DATE DEFAULT NULL,
  `status` VARCHAR(20) DEFAULT 'pending',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目计划表';

-- ------------------------------------------
-- 29. 项目进度表
-- ------------------------------------------
DROP TABLE IF EXISTS `project_progress`;
CREATE TABLE `project_progress` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `content` TEXT NOT NULL,
  `progress` INT DEFAULT 0 COMMENT '进度百分比',
  `created_by` INT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目进度表';

-- ------------------------------------------
-- 30. 招新申请表
-- ------------------------------------------
DROP TABLE IF EXISTS `recruit_application`;
CREATE TABLE `recruit_application` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL COMMENT '学号/用户名',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `email` VARCHAR(100) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态',
  `processed_by` INT DEFAULT NULL,
  `processed_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='招新申请表';

-- ------------------------------------------
-- 31. 问题反馈表
-- ------------------------------------------
DROP TABLE IF EXISTS `problem_report`;
CREATE TABLE `problem_report` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT NOT NULL,
  `reporter_name` VARCHAR(100) DEFAULT NULL,
  `reporter_contact` VARCHAR(100) DEFAULT NULL,
  `reporter_type` ENUM('GUEST','MEMBER','ADMIN') DEFAULT 'GUEST',
  `user_id` INT DEFAULT NULL,
  `category` ENUM('VERIFIED','UNVERIFIED','INVALID') DEFAULT 'UNVERIFIED',
  `status` ENUM('PENDING','SOLVING','SOLVED','UNSOLVED') DEFAULT 'PENDING',
  `admin_comment` TEXT,
  `handled_by` INT DEFAULT NULL,
  `handled_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题反馈表';

-- ------------------------------------------
-- 32. 学习会话表
-- ------------------------------------------
DROP TABLE IF EXISTS `study_session`;
CREATE TABLE `study_session` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `session_name` VARCHAR(100) NOT NULL,
  `subject` VARCHAR(50) DEFAULT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME DEFAULT NULL,
  `duration` INT DEFAULT NULL COMMENT '学习时长（分钟）',
  `content` TEXT,
  `notes` TEXT,
  `status` VARCHAR(20) DEFAULT 'ongoing',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习会话表';

-- ============================================
-- 以下是群聊相关表（包含文件支持）
-- ============================================

-- ------------------------------------------
-- 33. 活动群组表
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
-- 34. 群组成员表
-- ------------------------------------------
DROP TABLE IF EXISTS `group_member`;
CREATE TABLE `group_member` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `group_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `role` VARCHAR(20) DEFAULT 'MEMBER' COMMENT '角色：OWNER-群主，MEMBER-成员',
    `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_user` (`group_id`,`user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组成员表';

-- ------------------------------------------
-- 35. 群消息表（包含文件支持）
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
    `file_id` INT DEFAULT NULL COMMENT '文件ID（文件消息时使用）',
    `file_name` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
    `file_type` VARCHAR(100) DEFAULT NULL COMMENT '文件MIME类型',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件存储路径',
    PRIMARY KEY (`id`),
    KEY `idx_group_id` (`group_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_sent_at` (`sent_at`),
    KEY `idx_message_type` (`message_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群消息表';

-- ------------------------------------------
-- 36. 用户_群关系表
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
-- 恢复外键检查
-- ============================================
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 执行完成
-- ============================================
-- 默认管理员账号: admin / 123456 (DES加密后的密码)
-- 如需重置管理员密码，可使用: UPDATE user SET password='qlkkHyFnxfg=' WHERE username='admin';
