-- Schema-only baseline. Never commit production data to this repository.
-- MySQL dump 10.13  Distrib 5.7.42, for Win64 (x86_64)
--
-- Host: localhost    Database: software_group
-- ------------------------------------------------------
-- Server version	5.7.42-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `activity`
--

DROP TABLE IF EXISTS `activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `activity_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动类型（字典代码）',
  `activity_start_time` datetime DEFAULT NULL COMMENT '活动开始时间',
  `activity_end_time` datetime DEFAULT NULL COMMENT '活动结束时间',
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `organizers` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组织人（多个用逗号分隔）',
  `contact_info` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系方式',
  `registration_start_time` datetime DEFAULT NULL COMMENT '报名开始时间',
  `registration_end_time` datetime DEFAULT NULL COMMENT '报名截止时间',
  `max_participants` int(11) DEFAULT NULL,
  `status` enum('upcoming','ongoing','completed','canceled') COLLATE utf8mb4_unicode_ci DEFAULT 'upcoming',
  `approval_status` enum('pending','approved','rejected') COLLATE utf8mb4_unicode_ci DEFAULT 'approved',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(4) DEFAULT '0',
  `creator_id` int(11) DEFAULT NULL COMMENT '娲诲姩鍒涘缓鑰呯敤鎴稩D',
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `activity_group`
--

DROP TABLE IF EXISTS `activity_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `activity_id` int(11) DEFAULT NULL COMMENT '鍏宠仈鐨勬椿鍔↖D',
  `group_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '缇ゅ悕绉',
  `group_owner_id` int(11) NOT NULL COMMENT '缇や富鐢ㄦ埛ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_muted` tinyint(1) DEFAULT '0' COMMENT '鏄?惁鍏ㄤ綋绂佽█ 0-鍚?1-鏄',
  `muted_until` datetime DEFAULT NULL COMMENT '绂佽█鎴??鏃堕棿',
  `mute_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '绂佽█鍘熷洜',
  PRIMARY KEY (`id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_group_owner_id` (`group_owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `activity_participant`
--

DROP TABLE IF EXISTS `activity_participant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_participant` (
  `activity_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `status` enum('pending','confirmed','rejected','expired') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL,
  `notes` text COLLATE utf8mb4_unicode_ci,
  `deleted` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`activity_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `activity_participant_ibfk_1` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`) ON DELETE CASCADE,
  CONSTRAINT `activity_participant_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `admin_profile`
--

DROP TABLE IF EXISTS `admin_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_profile` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `title` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `department` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `education` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `research_area` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bio` text COLLATE utf8mb4_unicode_ci,
  `status` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `avatar_file_id` int(11) DEFAULT NULL COMMENT '头像文件ID，关联file_storage表',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `fk_admin_avatar` (`avatar_file_id`),
  CONSTRAINT `admin_profile_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_admin_avatar` FOREIGN KEY (`avatar_file_id`) REFERENCES `file_storage` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `ai_conversation`
--

DROP TABLE IF EXISTS `ai_conversation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_conversation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `user_id` int(11) DEFAULT '0' COMMENT '鐢ㄦ埛ID锛?琛ㄧず娓稿?',
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '浼氳瘽ID锛屽敮涓?爣璇',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI瀵硅瘽璁板綍琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `ai_conversation_log`
--

DROP TABLE IF EXISTS `ai_conversation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_conversation_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '鏃ュ織ID',
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '浼氳瘽ID',
  `user_id` int(11) DEFAULT NULL COMMENT '鐢ㄦ埛ID',
  `user_role` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鐢ㄦ埛瑙掕壊',
  `question` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鐢ㄦ埛鎻愰棶',
  `ai_answer` text COLLATE utf8mb4_unicode_ci COMMENT 'AI鍥炵瓟',
  `source` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍥炵瓟鏉ユ簮锛歠aq/llm',
  `reference_id` int(11) DEFAULT NULL COMMENT '鍏宠仈FAQ ID',
  `rating` tinyint(4) DEFAULT NULL COMMENT '鐢ㄦ埛璇勫垎1-5',
  `is_validated` tinyint(4) DEFAULT '0' COMMENT '鏄?惁宸查獙璇侊細1-鏄?紝0-鍚',
  `validated_by` int(11) DEFAULT NULL COMMENT '楠岃瘉浜篒D',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_role` (`user_role`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_source` (`source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI瀵硅瘽鏃ュ織琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `ai_faq_knowledge`
--

DROP TABLE IF EXISTS `ai_faq_knowledge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_faq_knowledge` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'FAQ ID',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '闂??鍒嗙被',
  `question` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '闂??',
  `answer` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '绛旀?',
  `keywords` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍏抽敭璇嶏紝鐢ㄤ簬鍖归厤',
  `target_role` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ALL' COMMENT '鐩?爣瑙掕壊锛欰DMIN/MEMBER/GUEST/ALL',
  `priority` int(11) DEFAULT '1' COMMENT '浼樺厛绾?-5',
  `view_count` int(11) DEFAULT '0' COMMENT '鏌ョ湅娆℃暟',
  `useful_count` int(11) DEFAULT '0' COMMENT '鐐硅禐娆℃暟',
  `status` tinyint(4) DEFAULT '1' COMMENT '鐘舵?锛?-鍚?敤锛?-绂佺敤',
  `verified` tinyint(4) DEFAULT '0' COMMENT '鏄?惁宸查獙璇侊細1-鏄?紝0-鍚',
  `verified_at` datetime DEFAULT NULL COMMENT '楠岃瘉鏃堕棿',
  `verified_by` int(11) DEFAULT NULL COMMENT '楠岃瘉浜篒D',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_target_role` (`target_role`),
  KEY `idx_status` (`status`),
  KEY `idx_verified` (`verified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FAQ鐭ヨ瘑琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `ai_faq_statistics`
--

DROP TABLE IF EXISTS `ai_faq_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_faq_statistics` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `question_hash` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '闂??鍝堝笇鍊',
  `normalized_question` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鏍囧噯鍖栭棶棰',
  `query_count` int(11) DEFAULT '1' COMMENT '鏌ヨ?娆℃暟',
  `avg_rating` decimal(3,2) DEFAULT NULL COMMENT '骞冲潎璇勫垎',
  `last_query_at` datetime DEFAULT NULL COMMENT '鏈?悗鏌ヨ?鏃堕棿',
  `suggested_faq` tinyint(4) DEFAULT '0' COMMENT '鏄?惁寤鸿?FAQ锛?-鏄?紝0-鍚',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `question_hash` (`question_hash`),
  KEY `idx_query_count` (`query_count`),
  KEY `idx_suggested` (`suggested_faq`),
  KEY `idx_last_query` (`last_query_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI鎻愰棶缁熻?琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `ai_knowledge_base`
--

DROP TABLE IF EXISTS `ai_knowledge_base`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_knowledge_base` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍒嗙被锛屽?锛氬?椤圭敵鎶ャ?娲诲姩鎶ュ悕',
  `question` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '闂??',
  `answer` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '绛旀?',
  `keywords` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍏抽敭璇嶏紝鐢ㄤ簬鍖归厤',
  `status` tinyint(4) DEFAULT '1' COMMENT '鐘舵?锛?-鍚?敤锛?-绂佺敤',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_keywords` (`keywords`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI鐭ヨ瘑搴撹〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `ai_message`
--

DROP TABLE IF EXISTS `ai_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `conversation_id` int(11) NOT NULL COMMENT '浼氳瘽ID锛屽?閿',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瑙掕壊锛歶ser/assistant/system',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '娑堟伅鍐呭?',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`),
  CONSTRAINT `ai_message_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `ai_conversation` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI娑堟伅琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `attendance`
--

DROP TABLE IF EXISTS `attendance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `attendance_date` date NOT NULL COMMENT '签到日期',
  `check_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `check_out_time` datetime DEFAULT NULL COMMENT '签退时间',
  `check_in_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'NONE' COMMENT '签到状态',
  `check_out_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'NONE' COMMENT '签退状态',
  `work_duration` int(11) DEFAULT NULL COMMENT '工作时长（分钟）',
  `location` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '签到地点',
  `device_info` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备信息',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`,`attendance_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_attendance_date` (`attendance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `attendance_config`
--

DROP TABLE IF EXISTS `attendance_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text COLLATE utf8mb4_unicode_ci COMMENT '配置值',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `attendance_makeup`
--

DROP TABLE IF EXISTS `attendance_makeup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance_makeup` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '补签ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `date` date NOT NULL COMMENT '补签日期',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '补签类型：check_in/check_out',
  `reason` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '补签原因',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT '审批状态',
  `approved_by` int(11) DEFAULT NULL COMMENT '审批人ID',
  `approved_at` datetime DEFAULT NULL COMMENT '审批时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='补签申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `award`
--

DROP TABLE IF EXISTS `award`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `award` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `competition` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '比赛名称',
  `year` int(11) NOT NULL COMMENT '获奖年份',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间\n',
  `competition_time` date DEFAULT NULL COMMENT '比赛时间\n',
  `competition_location` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '比赛地点\n',
  `competition_session` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '比赛界别',
  `award_type` int(11) DEFAULT NULL COMMENT '奖项类型（个人，团队）',
  `award_category` int(11) DEFAULT NULL COMMENT '奖项类别（算法，项目等）',
  `team_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '团队名称',
  `award_status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '状态',
  `created_by` int(11) DEFAULT NULL COMMENT '创建人\n',
  `approved_by` int(11) DEFAULT NULL COMMENT '通过人',
  `approved_at` datetime DEFAULT NULL COMMENT '通过时间\n',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间\n',
  `award_level` int(11) DEFAULT NULL COMMENT '奖项等级，关联dictionary表',
  `competition_level` int(11) DEFAULT NULL COMMENT '比赛等级，关联dictionary表',
  `deleted` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `created_by` (`created_by`),
  KEY `approved_by` (`approved_by`),
  KEY `fk_award_type` (`award_type`),
  KEY `fk_award_category` (`award_category`),
  KEY `fk_award_level` (`award_level`),
  KEY `fk_competition_level` (`competition_level`),
  CONSTRAINT `award_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `award_ibfk_2` FOREIGN KEY (`approved_by`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_award_category` FOREIGN KEY (`award_category`) REFERENCES `dictionary` (`id`),
  CONSTRAINT `fk_award_level` FOREIGN KEY (`award_level`) REFERENCES `dictionary` (`id`),
  CONSTRAINT `fk_award_type` FOREIGN KEY (`award_type`) REFERENCES `dictionary` (`id`),
  CONSTRAINT `fk_competition_level` FOREIGN KEY (`competition_level`) REFERENCES `dictionary` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `award_image`
--

DROP TABLE IF EXISTS `award_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `award_image` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `award_id` int(11) NOT NULL COMMENT '奖项ID号',
  `member_id` int(11) DEFAULT NULL COMMENT '奖项成员ID （似乎无法保存一个奖项多个id），可以算作上传图片的人。',
  `is_main` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否团队主持\n',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `file_storage_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_award_id` (`award_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `fk_award_image_file_storage` (`file_storage_id`),
  CONSTRAINT `award_image_ibfk_1` FOREIGN KEY (`award_id`) REFERENCES `award` (`id`) ON DELETE CASCADE,
  CONSTRAINT `award_image_ibfk_2` FOREIGN KEY (`member_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_award_image_file_storage` FOREIGN KEY (`file_storage_id`) REFERENCES `file_storage` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖项图片关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `award_member`
--

DROP TABLE IF EXISTS `award_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `award_member` (
  `award_id` int(11) NOT NULL,
  `member_id` int(11) NOT NULL,
  PRIMARY KEY (`award_id`,`member_id`),
  KEY `member_id` (`member_id`),
  CONSTRAINT `award_member_ibfk_1` FOREIGN KEY (`award_id`) REFERENCES `award` (`id`) ON DELETE CASCADE,
  CONSTRAINT `award_member_ibfk_2` FOREIGN KEY (`member_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `dictionary`
--

DROP TABLE IF EXISTS `dictionary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dictionary` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort_order` int(11) DEFAULT '0',
  `status` tinyint(4) DEFAULT '1',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_type` (`code`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `file_storage`
--

DROP TABLE IF EXISTS `file_storage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `file_storage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `create_by` int(11) NOT NULL,
  `original_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `stored_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_path` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_type` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_size` bigint(20) DEFAULT NULL,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint(4) DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_create_by` (`create_by`),
  CONSTRAINT `file_storage_fk_create_by` FOREIGN KEY (`create_by`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `group_member`
--

DROP TABLE IF EXISTS `group_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_member` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_id` int(11) NOT NULL COMMENT '缇?D',
  `user_id` int(11) NOT NULL COMMENT '鐢ㄦ埛ID',
  `role` enum('OWNER','MEMBER') COLLATE utf8mb4_unicode_ci DEFAULT 'MEMBER' COMMENT '瑙掕壊锛氱兢涓?鎴愬憳',
  `joined_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `muted` tinyint(1) DEFAULT '0' COMMENT '鏄?惁琚??瑷?0-鍚?1-鏄',
  `muted_until` datetime DEFAULT NULL COMMENT '绂佽█鎴??鏃堕棿',
  `last_read_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `group_message`
--

DROP TABLE IF EXISTS `group_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_id` int(11) NOT NULL COMMENT '缇?D',
  `sender_id` int(11) NOT NULL COMMENT '鍙戦?鑰匢D',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '娑堟伅鍐呭?',
  `sent_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'normal' COMMENT '娑堟伅鐘舵? normal-姝ｅ父 pending-寰呭?鏍?blocked-宸插睆钄',
  `message_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'TEXT' COMMENT '娑堟伅绫诲瀷锛歍EXT-鏂囨湰娑堟伅锛孎ILE-鏂囦欢娑堟伅',
  `file_id` int(11) DEFAULT NULL COMMENT '鏂囦欢ID锛堟枃浠舵秷鎭?椂浣跨敤锛',
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍘熷?鏂囦欢鍚',
  `file_size` bigint(20) DEFAULT NULL COMMENT '鏂囦欢澶у皬锛堝瓧鑺傦級',
  `file_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鏂囦欢MIME绫诲瀷',
  `file_path` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鏂囦欢瀛樺偍璺?緞',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_sent_at` (`sent_at`),
  KEY `idx_message_type` (`message_type`),
  KEY `idx_file_id` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `member_profile`
--

DROP TABLE IF EXISTS `member_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_profile` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `student_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `major` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `grade` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `birthday` date DEFAULT NULL,
  `gender` enum('male','female','other') COLLATE utf8mb4_unicode_ci DEFAULT 'other',
  `introduction` text COLLATE utf8mb4_unicode_ci,
  `skills` text COLLATE utf8mb4_unicode_ci,
  `github` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `blog` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `avatar_file_id` int(11) DEFAULT NULL COMMENT '头像文件ID，关联file_storage表',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `fk_member_avatar` (`avatar_file_id`),
  CONSTRAINT `fk_member_avatar` FOREIGN KEY (`avatar_file_id`) REFERENCES `file_storage` (`id`) ON DELETE SET NULL,
  CONSTRAINT `member_profile_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `news`
--

DROP TABLE IF EXISTS `news`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `news` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '新闻标题\n',
  `type` enum('award','activity','notice') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '新闻类型\n',
  `content_path` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '新闻地址，html文件所在地\n',
  `summary` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '新闻摘要\n',
  `author_id` int(11) DEFAULT NULL COMMENT '作者ID',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '审核状态\n',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `activity_id` int(11) DEFAULT NULL COMMENT '鍏宠仈鐨勬椿鍔↖D',
  PRIMARY KEY (`id`),
  KEY `author_id` (`author_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_news_activity_id` (`activity_id`),
  CONSTRAINT `news_ibfk_1` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鏂伴椈琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `operation_log`
--

DROP TABLE IF EXISTS `operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `operation_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `operation` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `module` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `ip_address` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_agent` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation` (`operation`),
  KEY `idx_module` (`module`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `problem_management`
--

DROP TABLE IF EXISTS `problem_management`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `problem_management` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '闂??ID',
  `report_id` int(11) NOT NULL COMMENT '鍏宠仈鐨勯棶棰樺弽棣圛D',
  `category` enum('VERIFIED','INVALID','UNVERIFIED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNVERIFIED' COMMENT '鍒嗙被锛歏ERIFIED-灞炲疄锛孖NVALID-涓嶅睘瀹烇紝UNVERIFIED-寰呯‘璁',
  `status` enum('PENDING','SOLVING','SOLVED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '澶勭悊鐘舵?锛歅ENDING-寰呬慨鏀癸紝SOLVING-姝ｅ湪淇?敼锛孲OLVED-宸蹭慨鏀癸紙浠呭睘瀹為棶棰樻湁鐘舵?锛',
  `admin_comment` text COLLATE utf8mb4_unicode_ci COMMENT '绠＄悊鍛樺?娉?鍥炲?',
  `handled_by` int(11) DEFAULT NULL COMMENT '澶勭悊浜篒D',
  `handled_at` datetime DEFAULT NULL COMMENT '澶勭悊鏃堕棿',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_report_id` (`report_id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闂??绠＄悊琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `problem_report`
--

DROP TABLE IF EXISTS `problem_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `problem_report` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '闂??ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '闂??鏍囬?',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '闂??鎻忚堪',
  `reporter_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鎶ュ憡鑰呭?鍚嶏紙娓稿?鍙?～锛',
  `reporter_contact` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鑱旂郴鏂瑰紡锛堟父瀹㈠彲濉?級',
  `reporter_type` enum('GUEST','MEMBER','ADMIN') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GUEST' COMMENT '鎶ュ憡鑰呯被鍨',
  `user_id` int(11) DEFAULT NULL COMMENT '鍏宠仈鐢ㄦ埛ID锛堝?鏋滄槸鎴愬憳/绠＄悊鍛橈級',
  `category` enum('VERIFIED','UNVERIFIED','INVALID') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNVERIFIED' COMMENT '闂??鍒嗙被锛歏ERIFIED-灞炲疄锛孶NVERIFIED-寰呴獙璇侊紝INVALID-涓嶅睘瀹',
  `status` enum('PENDING','SOLVING','SOLVED','UNSOLVED') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '鐘舵?锛歅ENDING-寰呭?鐞嗭紝SOLVING-姝ｅ湪瑙ｅ喅锛孲OLVED-宸茶В鍐筹紝UNSOLVED-鏈?В鍐筹紙浠呭睘瀹為棶棰樻湁鐘舵?锛',
  `admin_comment` text COLLATE utf8mb4_unicode_ci COMMENT '绠＄悊鍛樺?娉?鍥炲?',
  `handled_by` int(11) DEFAULT NULL COMMENT '澶勭悊浜篒D',
  `handled_at` datetime DEFAULT NULL COMMENT '澶勭悊鏃堕棿',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闂??鍙嶉?琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `project` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `year` int(11) NOT NULL,
  `expected_start_date` date DEFAULT NULL,
  `expected_end_date` date DEFAULT NULL,
  `actual_start_date` date DEFAULT NULL,
  `actual_end_date` date DEFAULT NULL,
  `status` enum('pending','approved','in_progress','completed','canceled','rejected') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `approved_by` int(11) DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `admin_id` int(11) DEFAULT NULL,
  `leader_id` int(11) NOT NULL,
  `budget` decimal(10,2) DEFAULT NULL,
  `repo_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `doc_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `leader_id` (`leader_id`),
  KEY `fk_project_admin` (`admin_id`),
  CONSTRAINT `fk_project_admin` FOREIGN KEY (`admin_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `project_ibfk_1` FOREIGN KEY (`leader_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `project_file`
--

DROP TABLE IF EXISTS `project_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_file` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL,
  `file_id` int(11) NOT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int(11) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_pf_project` (`project_id`),
  KEY `fk_pf_file` (`file_id`),
  CONSTRAINT `fk_pf_file` FOREIGN KEY (`file_id`) REFERENCES `file_storage` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pf_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `project_history`
--

DROP TABLE IF EXISTS `project_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL,
  `operation_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `operator_id` int(11) NOT NULL,
  `operator_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `old_value` text COLLATE utf8mb4_unicode_ci,
  `new_value` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_ph_project` (`project_id`),
  KEY `fk_ph_operator` (`operator_id`),
  CONSTRAINT `fk_ph_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ph_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `project_image`
--

DROP TABLE IF EXISTS `project_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_image` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL,
  `file_id` int(11) NOT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int(11) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_pimg_project` (`project_id`),
  KEY `fk_pimg_file` (`file_id`),
  CONSTRAINT `fk_pimg_file` FOREIGN KEY (`file_id`) REFERENCES `file_storage` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pimg_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `project_label`
--

DROP TABLE IF EXISTS `project_label`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_label` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL,
  `label_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_label` (`project_id`,`label_code`),
  CONSTRAINT `fk_pl_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `project_member`
--

DROP TABLE IF EXISTS `project_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_member` (
  `project_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `role` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`project_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `project_member_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `project_member_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `project_member_application`
--

DROP TABLE IF EXISTS `project_member_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_member_application` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT 'PENDING/CONFIRMED/REJECTED',
  `applied_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `handled_at` timestamp NULL DEFAULT NULL,
  `handled_by` int(11) DEFAULT NULL,
  `reason` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`,`user_id`,`status`),
  KEY `fk_pma_user` (`user_id`),
  KEY `fk_pma_handler` (`handled_by`),
  CONSTRAINT `fk_pma_handler` FOREIGN KEY (`handled_by`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_pma_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pma_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `project_plan`
--

DROP TABLE IF EXISTS `project_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_plan` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `order_index` int(11) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_pp_project` (`project_id`),
  CONSTRAINT `fk_pp_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `project_progress`
--

DROP TABLE IF EXISTS `project_progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_progress` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL,
  `plan_id` int(11) DEFAULT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `completion_rate` int(11) DEFAULT '0' COMMENT '0-100',
  `created_by` int(11) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_pg_project` (`project_id`),
  KEY `fk_pg_plan` (`plan_id`),
  KEY `fk_pg_creator` (`created_by`),
  CONSTRAINT `fk_pg_creator` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pg_plan` FOREIGN KEY (`plan_id`) REFERENCES `project_plan` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_pg_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `recruit_application`
--

DROP TABLE IF EXISTS `recruit_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `recruit_application` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `student_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `major` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `grade` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reason` text COLLATE utf8mb4_unicode_ci,
  `status` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `resume_awards`
--

DROP TABLE IF EXISTS `resume_awards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `resume_awards` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `resume_id` int(11) NOT NULL COMMENT '简历ID（关联resumes表）',
  `award_id` int(11) DEFAULT NULL,
  `award_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '奖项名称',
  `competition_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '比赛/活动名称',
  `award_level` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '奖项等级：特等奖/一等奖/二等奖/三等奖/优秀奖等',
  `award_date` date DEFAULT NULL COMMENT '获奖时间',
  `award_org` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '颁奖机构',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '获奖描述',
  `is_from_system` tinyint(4) DEFAULT '0' COMMENT '是否来自系统奖项：0-手动添加，1-关联系统奖项',
  `display_order` int(11) DEFAULT '0' COMMENT '显示顺序',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`),
  KEY `idx_award_id` (`award_id`),
  KEY `idx_display_order` (`display_order`),
  CONSTRAINT `resume_awards_ibfk_1` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `resume_awards_ibfk_2` FOREIGN KEY (`award_id`) REFERENCES `award` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历-获奖情况关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `resume_educations`
--

DROP TABLE IF EXISTS `resume_educations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `resume_educations` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '教育经历ID',
  `resume_id` int(11) NOT NULL COMMENT '简历ID（关联resumes表）',
  `school_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学校名称',
  `major` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '专业名称',
  `degree` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学历/学位：高中/大专/本科/硕士/博士',
  `start_date` date DEFAULT NULL COMMENT '入学时间',
  `end_date` date DEFAULT NULL COMMENT '毕业时间',
  `is_current` tinyint(4) DEFAULT '0' COMMENT '是否在读：0-已毕业，1-在读',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '在校经历描述（主修课程、GPA、获奖情况等）',
  `display_order` int(11) DEFAULT '0' COMMENT '显示顺序',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`),
  KEY `idx_display_order` (`display_order`),
  CONSTRAINT `resume_educations_ibfk_1` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历-教育经历表';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `resume_projects`
--

DROP TABLE IF EXISTS `resume_projects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `resume_projects` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `resume_id` int(11) NOT NULL COMMENT '简历ID（关联resumes表）',
  `project_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  `role` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '担任角色：项目负责人/核心开发/参与开发等',
  `team_size` int(11) DEFAULT NULL COMMENT '团队规模',
  `start_date` date DEFAULT NULL COMMENT '开始时间',
  `end_date` date DEFAULT NULL COMMENT '结束时间',
  `is_current` tinyint(4) DEFAULT '0' COMMENT '是否进行中：0-已完成，1-进行中',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '项目描述',
  `responsibilities` text COLLATE utf8mb4_unicode_ci COMMENT '个人职责',
  `technologies` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '使用技术（逗号分隔）',
  `project_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '项目链接/演示地址',
  `achievements` text COLLATE utf8mb4_unicode_ci COMMENT '项目成果',
  `display_order` int(11) DEFAULT '0' COMMENT '显示顺序',
  `is_from_system` tinyint(4) DEFAULT '0' COMMENT '是否来自系统项目模块：0-手动添加，1-关联系统项目',
  `system_project_id` int(11) DEFAULT NULL COMMENT '关联的系统项目ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`),
  KEY `idx_is_current` (`is_current`),
  KEY `idx_display_order` (`display_order`),
  CONSTRAINT `resume_projects_ibfk_1` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历-项目经历表';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `resume_skills`
--

DROP TABLE IF EXISTS `resume_skills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `resume_skills` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '技能ID',
  `resume_id` int(11) NOT NULL COMMENT '简历ID（关联resumes表）',
  `skill_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能名称',
  `proficiency` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'intermediate' COMMENT '熟练程度：beginner/elementary/intermediate/advanced/expert',
  `proficiency_score` int(11) DEFAULT NULL COMMENT '熟练度分数：1-100',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '技能分类：编程语言/开发框架/数据库/工具/语言/其他',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '技能描述',
  `display_order` int(11) DEFAULT '0' COMMENT '显示顺序',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`),
  KEY `idx_category` (`category`),
  KEY `idx_display_order` (`display_order`),
  CONSTRAINT `resume_skills_ibfk_1` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历-技能特长表';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `resume_submissions`
--

DROP TABLE IF EXISTS `resume_submissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `resume_submissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '投递记录ID',
  `resume_id` int(11) NOT NULL COMMENT '简历ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `target_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '投递对象类型：activity-活动/项目，recruit-招新，job-职位',
  `target_id` int(11) DEFAULT NULL COMMENT '投递对象ID',
  `target_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '投递对象名称',
  `status` tinyint(4) DEFAULT '0' COMMENT '投递状态：0-已投递，1-已查看，2-已通过，3-未通过',
  `submit_message` text COLLATE utf8mb4_unicode_ci COMMENT '投递附言',
  `feedback` text COLLATE utf8mb4_unicode_ci COMMENT '反馈信息',
  `submitted_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
  `viewed_at` timestamp NULL DEFAULT NULL COMMENT '查看时间',
  `replied_at` timestamp NULL DEFAULT NULL COMMENT '回复时间',
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target` (`target_type`,`target_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `resume_submissions_ibfk_1` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `resume_submissions_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历投递记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `resumes`
--

DROP TABLE IF EXISTS `resumes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `resumes` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '简历ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID（关联users表）',
  `resume_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '我的简历' COMMENT '简历名称',
  `template_style` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'default' COMMENT '简历模板风格：default/minimal/academic/creative',
  `summary` text COLLATE utf8mb4_unicode_ci COMMENT '个人简介/自我评价',
  `career_objective` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '求职意向',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系邮箱',
  `wechat` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信号',
  `github_url` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'GitHub主页',
  `blog_url` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '技术博客地址',
  `photo_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '个人照片URL',
  `is_default` tinyint(4) DEFAULT '0' COMMENT '是否为默认简历：0-否，1-是',
  `status` tinyint(4) DEFAULT '1' COMMENT '简历状态：0-草稿，1-已发布，2-已隐藏',
  `view_count` int(11) DEFAULT '0' COMMENT '浏览次数',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '软删除标志：0-正常，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_default` (`is_default`),
  CONSTRAINT `resumes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `study_session`
--

DROP TABLE IF EXISTS `study_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `study_session` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `session_date` date DEFAULT NULL COMMENT '学习日期',
  `check_in_time` datetime DEFAULT NULL COMMENT '开始时间',
  `check_out_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` int(11) DEFAULT NULL COMMENT '学习时长（分钟）',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '状态：ACTIVE-进行中，COMPLETED-已完成',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_date` (`session_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习时段记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Unknown',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` enum('ADMIN','MEMBER','TEACHER','GUEST') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MEMBER',
  `status` int(11) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `must_change_password` tinyint(4) DEFAULT '0' COMMENT '是否必须修改密码：1-是，0-否',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

--
-- Table structure for table `user_group`
--

DROP TABLE IF EXISTS `user_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '鐢ㄦ埛ID',
  `group_id` int(11) NOT NULL COMMENT '缇?D',
  `joined_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_group` (`user_id`,`group_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

--
-- Table structure for table `ai_message_status` (阶段二中间件引入 - AI消息异步处理)
--

DROP TABLE IF EXISTS `ai_message_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_message_status` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message_id` varchar(64) NOT NULL COMMENT '消息唯一ID',
  `user_id` int(11) DEFAULT NULL COMMENT '用户ID',
  `session_id` varchar(128) DEFAULT NULL COMMENT '会话ID',
  `user_message` text COMMENT '用户消息',
  `ai_response` text COMMENT 'AI回复',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/processing/completed/failed',
  `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_id` (`message_id`),
  KEY `idx_user_session` (`user_id`, `session_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI消息状态表';
/*!40101 SET character_set_client = @saved_cs_client */;

-- Dump completed on 2026-07-11  8:54:46
