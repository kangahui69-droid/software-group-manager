-- ============================================
-- 高校软件小组管理系统 - 初始化SQL脚本
-- 执行顺序：按文件顺序依次执行
-- ============================================

-- ============================================
-- 1. 完整数据库备份
-- ============================================
-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: software_group
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
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
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity`
--

LOCK TABLES `activity` WRITE;
/*!40000 ALTER TABLE `activity` DISABLE KEYS */;
INSERT INTO `activity` VALUES (6,'Java������������','��������','TEA_PARTY','2026-04-01 10:00:00','2026-04-01 12:00:00','�µص�','������֯��','new@test.com','2026-03-15 09:00:00','2026-03-31 23:59:00',100,'upcoming','approved','2026-02-19 14:32:34','2026-02-24 12:50:38',0),(7,'春季茶话会','轻松交流活动','TEA_PARTY','2026-03-05 15:00:00','2026-03-05 17:00:00','学生活动中心','李四,王五','lisi@example.com','2026-02-10 09:00:00','2026-03-04 23:59:00',30,'upcoming','approved','2026-02-19 14:32:34','2026-02-19 14:32:34',0),(8,'人工智能工作坊','AI实践工作坊','WORKSHOP','2026-03-10 09:00:00','2026-03-10 12:00:00','实验室C201','赵六','zhaoliu@example.com','2026-02-20 09:00:00','2026-03-09 23:59:00',20,'upcoming','approved','2026-02-19 14:32:34','2026-02-19 14:32:34',0),(9,'已过期活动','报名已截止','SEMINAR','2026-01-15 14:00:00','2026-01-15 16:00:00','会议室B201','孙七','sunqi@example.com','2026-01-01 09:00:00','2026-01-14 23:59:00',25,'completed','approved','2026-02-19 14:32:34','2026-02-19 14:32:34',0),(11,'满员测试活动','测试人数限制','LECTURE','2026-04-10 14:00:00','2026-04-10 16:00:00','教室B101','测试组织','test@test.com','2026-02-20 09:00:00','2026-03-20 23:59:00',1,'upcoming','approved','2026-02-21 11:04:42','2026-02-21 11:04:42',0),(15,'���Ի2026B','����','SEMINAR','2026-02-07 09:00:00','2026-02-07 11:00:00','�ص�',NULL,NULL,'2026-02-22 18:44:26','2026-02-06 09:00:00',20,'upcoming','approved','2026-02-22 10:44:25','2026-02-22 10:44:25',0),(16,'�����»','��������','LECTURE','2026-02-07 09:00:00','2026-02-07 11:00:00','���Եص�',NULL,NULL,'2026-02-22 19:00:47','2026-02-06 09:00:00',10,'upcoming','approved','2026-02-22 11:00:47','2026-02-22 11:00:47',0),(17,'�������ջ','����','LECTURE','2026-02-28 09:00:00','2026-02-28 11:00:00','�ص�',NULL,NULL,'2026-02-22 19:01:39','2026-02-27 09:00:00',10,'upcoming','approved','2026-02-22 11:01:39','2026-02-22 11:01:39',0),(18,'tesat','','','2026-02-28 09:00:00','2026-02-28 11:00:00','',NULL,NULL,'2026-02-22 19:02:00','2026-02-27 09:00:00',0,'upcoming','approved','2026-02-22 11:03:00','2026-02-22 11:03:00',0),(19,'testactivity','test','LECTURE','2026-03-15 10:00:00','2026-03-15 12:00:00','test','tester','test@test.com','2026-03-01 09:00:00','2026-03-14 23:59:00',50,'upcoming','approved','2026-02-24 13:28:18','2026-02-24 13:28:18',0),(20,'修电脑','培养学生对硬件的了解','OTHER','2026-03-14 09:00:00','2026-03-14 11:00:00','6305','康阿辉','','2026-03-10 22:02:00','2026-03-13 23:59:00',20,'upcoming','approved','2026-03-10 14:03:20','2026-03-10 14:03:20',0);
/*!40000 ALTER TABLE `activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_participant`
--

DROP TABLE IF EXISTS `activity_participant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_participant` (
  `activity_id` int NOT NULL,
  `user_id` int NOT NULL,
  `status` enum('pending','confirmed','rejected','expired') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL,
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`activity_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `activity_participant_ibfk_1` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`) ON DELETE CASCADE,
  CONSTRAINT `activity_participant_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_participant`
--

LOCK TABLES `activity_participant` WRITE;
/*!40000 ALTER TABLE `activity_participant` DISABLE KEYS */;
INSERT INTO `activity_participant` VALUES (6,2,'confirmed','2026-02-21 13:01:33','2026-02-21 13:28:49',NULL,0),(7,2,'confirmed','2026-02-21 10:39:40','2026-02-21 11:07:10',NULL,0),(7,3,'confirmed','2026-02-21 11:06:45','2026-02-21 11:07:10',NULL,0),(8,2,'confirmed','2026-02-21 13:35:26','2026-02-21 13:35:40',NULL,0),(9,2,'pending','2026-02-19 14:36:09',NULL,NULL,0),(11,2,'confirmed','2026-02-21 11:04:49','2026-02-21 11:05:59',NULL,0),(17,2,'pending','2026-02-24 13:13:11',NULL,NULL,0),(18,2,'pending','2026-02-24 12:34:31',NULL,NULL,0),(20,27,'confirmed','2026-03-10 14:04:26','2026-03-10 14:18:06',NULL,0);
/*!40000 ALTER TABLE `activity_participant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_profile`
--

DROP TABLE IF EXISTS `admin_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_profile` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `title` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `department` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `education` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `research_area` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bio` text COLLATE utf8mb4_unicode_ci,
  `status` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `avatar_file_id` int DEFAULT NULL COMMENT '头像文件ID，关联file_storage表',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `fk_admin_avatar` (`avatar_file_id`),
  CONSTRAINT `admin_profile_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_admin_avatar` FOREIGN KEY (`avatar_file_id`) REFERENCES `file_storage` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_profile`
--

LOCK TABLES `admin_profile` WRITE;
/*!40000 ALTER TABLE `admin_profile` DISABLE KEYS */;
INSERT INTO `admin_profile` VALUES (1,1,'讲师2','信息工程学院2','博士研究生','SAAS，网络通信2','这个一个简单的简历2',1,'2026-01-25 21:21:00','2026-02-23 10:52:41',32);
/*!40000 ALTER TABLE `admin_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_conversation`
--

DROP TABLE IF EXISTS `ai_conversation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_conversation` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `user_id` int DEFAULT '0' COMMENT '鐢ㄦ埛ID锛?琛ㄧず娓稿?',
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '浼氳瘽ID锛屽敮涓?爣璇',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI瀵硅瘽璁板綍琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_conversation`
--

LOCK TABLES `ai_conversation` WRITE;
/*!40000 ALTER TABLE `ai_conversation` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_conversation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_conversation_log`
--

DROP TABLE IF EXISTS `ai_conversation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_conversation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鏃ュ織ID',
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '浼氳瘽ID',
  `user_id` int DEFAULT NULL COMMENT '鐢ㄦ埛ID',
  `user_role` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鐢ㄦ埛瑙掕壊',
  `question` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鐢ㄦ埛鎻愰棶',
  `ai_answer` text COLLATE utf8mb4_unicode_ci COMMENT 'AI鍥炵瓟',
  `source` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍥炵瓟鏉ユ簮锛歠aq/llm',
  `reference_id` int DEFAULT NULL COMMENT '鍏宠仈FAQ ID',
  `rating` tinyint DEFAULT NULL COMMENT '鐢ㄦ埛璇勫垎1-5',
  `is_validated` tinyint DEFAULT '0' COMMENT '鏄?惁宸查獙璇侊細1-鏄?紝0-鍚',
  `validated_by` int DEFAULT NULL COMMENT '楠岃瘉浜篒D',
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
-- Dumping data for table `ai_conversation_log`
--

LOCK TABLES `ai_conversation_log` WRITE;
/*!40000 ALTER TABLE `ai_conversation_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_conversation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_faq_knowledge`
--

DROP TABLE IF EXISTS `ai_faq_knowledge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_faq_knowledge` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'FAQ ID',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '闂??鍒嗙被',
  `question` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '闂??',
  `answer` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '绛旀?',
  `keywords` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍏抽敭璇嶏紝鐢ㄤ簬鍖归厤',
  `target_role` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ALL' COMMENT '鐩?爣瑙掕壊锛欰DMIN/MEMBER/GUEST/ALL',
  `priority` int DEFAULT '1' COMMENT '浼樺厛绾?-5',
  `view_count` int DEFAULT '0' COMMENT '鏌ョ湅娆℃暟',
  `useful_count` int DEFAULT '0' COMMENT '鐐硅禐娆℃暟',
  `status` tinyint DEFAULT '1' COMMENT '鐘舵?锛?-鍚?敤锛?-绂佺敤',
  `verified` tinyint DEFAULT '0' COMMENT '鏄?惁宸查獙璇侊細1-鏄?紝0-鍚',
  `verified_at` datetime DEFAULT NULL COMMENT '楠岃瘉鏃堕棿',
  `verified_by` int DEFAULT NULL COMMENT '楠岃瘉浜篒D',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_target_role` (`target_role`),
  KEY `idx_status` (`status`),
  KEY `idx_verified` (`verified`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FAQ鐭ヨ瘑琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_faq_knowledge`
--

LOCK TABLES `ai_faq_knowledge` WRITE;
/*!40000 ALTER TABLE `ai_faq_knowledge` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_faq_knowledge` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_faq_statistics`
--

DROP TABLE IF EXISTS `ai_faq_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_faq_statistics` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `question_hash` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '闂??鍝堝笇鍊',
  `normalized_question` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鏍囧噯鍖栭棶棰',
  `query_count` int DEFAULT '1' COMMENT '鏌ヨ?娆℃暟',
  `avg_rating` decimal(3,2) DEFAULT NULL COMMENT '骞冲潎璇勫垎',
  `last_query_at` datetime DEFAULT NULL COMMENT '鏈?悗鏌ヨ?鏃堕棿',
  `suggested_faq` tinyint DEFAULT '0' COMMENT '鏄?惁寤鸿?FAQ锛?-鏄?紝0-鍚',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `question_hash` (`question_hash`),
  KEY `idx_query_count` (`query_count`),
  KEY `idx_suggested` (`suggested_faq`),
  KEY `idx_last_query` (`last_query_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI鎻愰棶缁熻?琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_faq_statistics`
--

LOCK TABLES `ai_faq_statistics` WRITE;
/*!40000 ALTER TABLE `ai_faq_statistics` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_faq_statistics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_knowledge_base`
--

DROP TABLE IF EXISTS `ai_knowledge_base`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_knowledge_base` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍒嗙被锛屽?锛氬?椤圭敵鎶ャ?娲诲姩鎶ュ悕',
  `question` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '闂??',
  `answer` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '绛旀?',
  `keywords` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍏抽敭璇嶏紝鐢ㄤ簬鍖归厤',
  `status` tinyint DEFAULT '1' COMMENT '鐘舵?锛?-鍚?敤锛?-绂佺敤',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_keywords` (`keywords`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI鐭ヨ瘑搴撹〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_knowledge_base`
--

LOCK TABLES `ai_knowledge_base` WRITE;
/*!40000 ALTER TABLE `ai_knowledge_base` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_knowledge_base` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_message`
--

DROP TABLE IF EXISTS `ai_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_message` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `conversation_id` int NOT NULL COMMENT '浼氳瘽ID锛屽?閿',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瑙掕壊锛歶ser/assistant/system',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '娑堟伅鍐呭?',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`),
  CONSTRAINT `ai_message_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `ai_conversation` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI娑堟伅琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_message`
--

LOCK TABLES `ai_message` WRITE;
/*!40000 ALTER TABLE `ai_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attendance`
--

DROP TABLE IF EXISTS `attendance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `attendance_date` date NOT NULL COMMENT '签到日期',
  `check_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `check_out_time` datetime DEFAULT NULL COMMENT '签退时间',
  `check_in_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'NONE' COMMENT '签到状态',
  `check_out_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'NONE' COMMENT '签退状态',
  `work_duration` int DEFAULT NULL COMMENT '工作时长（分钟）',
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
-- Dumping data for table `attendance`
--

LOCK TABLES `attendance` WRITE;
/*!40000 ALTER TABLE `attendance` DISABLE KEYS */;
/*!40000 ALTER TABLE `attendance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attendance_config`
--

DROP TABLE IF EXISTS `attendance_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
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
-- Dumping data for table `attendance_config`
--

LOCK TABLES `attendance_config` WRITE;
/*!40000 ALTER TABLE `attendance_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `attendance_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attendance_makeup`
--

DROP TABLE IF EXISTS `attendance_makeup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance_makeup` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '补签ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `date` date NOT NULL COMMENT '补签日期',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '补签类型：check_in/check_out',
  `reason` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '补签原因',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT '审批状态',
  `approved_by` int DEFAULT NULL COMMENT '审批人ID',
  `approved_at` datetime DEFAULT NULL COMMENT '审批时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='补签申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendance_makeup`
--

LOCK TABLES `attendance_makeup` WRITE;
/*!40000 ALTER TABLE `attendance_makeup` DISABLE KEYS */;
/*!40000 ALTER TABLE `attendance_makeup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `award`
--

DROP TABLE IF EXISTS `award`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `award` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `competition` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '比赛名称',
  `year` int NOT NULL COMMENT '获奖年份',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间\n',
  `competition_time` date DEFAULT NULL COMMENT '比赛时间\n',
  `competition_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '比赛地点\n',
  `competition_session` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '比赛界别',
  `award_type` int DEFAULT NULL COMMENT '奖项类型（个人，团队）',
  `award_category` int DEFAULT NULL COMMENT '奖项类别（算法，项目等）',
  `team_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '团队名称',
  `award_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '状态',
  `created_by` int DEFAULT NULL COMMENT '创建人\n',
  `approved_by` int DEFAULT NULL COMMENT '通过人',
  `approved_at` datetime DEFAULT NULL COMMENT '通过时间\n',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间\n',
  `award_level` int DEFAULT NULL COMMENT '奖项等级，关联dictionary表',
  `competition_level` int DEFAULT NULL COMMENT '比赛等级，关联dictionary表',
  `deleted` tinyint DEFAULT '0',
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `award`
--

LOCK TABLES `award` WRITE;
/*!40000 ALTER TABLE `award` DISABLE KEYS */;
INSERT INTO `award` VALUES (9,'22-特啊他','特啊他',2026,'2026-01-13 08:23:49','2026-01-02','ga','213',5,8,'gasdg ','APPROVED',2,1,'2026-01-20 20:14:56','2026-01-28 10:41:34',22,1,0),(10,'21-特啊他gasdgads','特啊他gasdgads',2026,'2026-01-20 12:15:38','2026-01-01','dsf','12',6,9,'asga','REJECTED',2,1,'2026-01-20 20:15:58','2026-01-28 10:41:34',21,1,0),(11,'蓝桥杯-23','蓝桥杯',2026,'2026-01-27 13:31:27','2026-01-01','南京','第十三届',6,8,'给萨达','APPROVED',2,1,'2026-01-27 21:31:53','2026-01-28 10:41:34',23,1,0),(12,'蓝桥杯-21','蓝桥杯',2026,'2026-01-27 13:36:43','2026-01-08','南京','第十三届',5,8,'','REJECTED',2,1,'2026-01-27 21:36:56','2026-01-28 10:41:34',21,2,0),(13,'蓝桥杯-22','蓝桥杯',2026,'2026-01-28 01:50:19','2026-01-08','南京','第十三届',5,8,'','APPROVED',2,NULL,NULL,'2026-02-18 10:16:14',22,1,0);
/*!40000 ALTER TABLE `award` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `award_image`
--

DROP TABLE IF EXISTS `award_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `award_image` (
  `id` int NOT NULL AUTO_INCREMENT,
  `award_id` int NOT NULL COMMENT '奖项ID号',
  `member_id` int DEFAULT NULL COMMENT '奖项成员ID （似乎无法保存一个奖项多个id），可以算作上传图片的人。',
  `is_main` tinyint NOT NULL DEFAULT '0' COMMENT '是否团队主持\n',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `file_storage_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_award_id` (`award_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `fk_award_image_file_storage` (`file_storage_id`),
  CONSTRAINT `award_image_ibfk_1` FOREIGN KEY (`award_id`) REFERENCES `award` (`id`) ON DELETE CASCADE,
  CONSTRAINT `award_image_ibfk_2` FOREIGN KEY (`member_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_award_image_file_storage` FOREIGN KEY (`file_storage_id`) REFERENCES `file_storage` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖项图片关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `award_image`
--

LOCK TABLES `award_image` WRITE;
/*!40000 ALTER TABLE `award_image` DISABLE KEYS */;
INSERT INTO `award_image` VALUES (9,9,2,0,'2026-01-13 16:23:49',1),(10,10,2,0,'2026-01-20 20:15:39',2),(11,11,2,0,'2026-01-27 21:31:28',7),(12,12,2,0,'2026-01-27 21:36:45',8),(13,13,2,0,'2026-01-28 09:50:22',9);
/*!40000 ALTER TABLE `award_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `award_member`
--

DROP TABLE IF EXISTS `award_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `award_member` (
  `award_id` int NOT NULL,
  `member_id` int NOT NULL,
  PRIMARY KEY (`award_id`,`member_id`),
  KEY `member_id` (`member_id`),
  CONSTRAINT `award_member_ibfk_1` FOREIGN KEY (`award_id`) REFERENCES `award` (`id`) ON DELETE CASCADE,
  CONSTRAINT `award_member_ibfk_2` FOREIGN KEY (`member_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `award_member`
--

LOCK TABLES `award_member` WRITE;
/*!40000 ALTER TABLE `award_member` DISABLE KEYS */;
INSERT INTO `award_member` VALUES (9,2),(10,2),(11,2),(12,2),(13,2);
/*!40000 ALTER TABLE `award_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dictionary`
--

DROP TABLE IF EXISTS `dictionary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dictionary` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort_order` int DEFAULT '0',
  `status` tinyint DEFAULT '1',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_type` (`code`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dictionary`
--

LOCK TABLES `dictionary` WRITE;
/*!40000 ALTER TABLE `dictionary` DISABLE KEYS */;
INSERT INTO `dictionary` VALUES (1,'LEVEL_PROVINCIAL','省级','COMPETITION_LEVEL',1,1,'省级比赛','2026-01-13 14:44:23','2026-01-13 14:44:23'),(2,'LEVEL_NATIONAL','国家级','COMPETITION_LEVEL',2,1,'国家级比赛','2026-01-13 14:44:23','2026-01-13 14:44:23'),(3,'LEVEL_REGIONAL','地区级别','COMPETITION_LEVEL',3,1,'地区级比赛','2026-01-13 14:44:23','2026-01-13 14:44:23'),(4,'LEVEL_OTHER','其他级别','COMPETITION_LEVEL',4,1,'其他级别的比赛','2026-01-13 14:44:23','2026-01-13 14:44:23'),(5,'TYPE_INDIVIDUAL','个人','AWARD_TYPE',1,1,'个人奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(6,'TYPE_TEAM','团队','AWARD_TYPE',2,1,'团队奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(7,'CATEGORY_PROJECT','项目','AWARD_CATEGORY',1,1,'项目类奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(8,'CATEGORY_ALGORITHM','经典算法','AWARD_CATEGORY',2,1,'算法类奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(9,'CATEGORY_AI','人工智能','AWARD_CATEGORY',3,1,'人工智能类奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(10,'CATEGORY_DOCUMENT','文档类','AWARD_CATEGORY',4,1,'文档类奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(11,'CATEGORY_OTHER','其他','AWARD_CATEGORY',5,1,'其他类别的奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(12,'ROLE_ADMIN','管理员','USER_ROLE',1,1,'系统管理员','2026-01-13 14:44:23','2026-01-13 14:44:23'),(13,'ROLE_MEMBER','成员','USER_ROLE',2,1,'团队成员','2026-01-13 14:44:23','2026-01-13 14:44:23'),(14,'ROLE_STUDENT','学生','USER_ROLE',3,1,'普通学生','2026-01-13 14:44:23','2026-01-13 14:44:23'),(15,'PENDING','待审核','AWARD_STATUS',1,1,'待管理员审核的奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(16,'APPROVED','已审核','AWARD_STATUS',2,1,'管理员已审核通过的奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(17,'REJECTED','已拒绝','AWARD_STATUS',3,1,'管理员已拒绝的奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(18,'DRAFT','草稿','NEWS_STATUS',1,1,'未发布的新闻草稿','2026-01-13 14:44:23','2026-01-13 14:44:23'),(19,'PUBLISHED','已发布','NEWS_STATUS',2,1,'已发布的新闻','2026-01-13 14:44:23','2026-01-13 14:44:23'),(20,'ARCHIVED','已归档','NEWS_STATUS',3,1,'已归档的新闻','2026-01-13 14:44:23','2026-01-13 14:44:23'),(21,'LEVEL_FIRST','一等奖','AWARD_LEVEL',1,1,'一等奖','2026-01-13 14:58:39','2026-01-13 14:58:39'),(22,'LEVEL_SECOND','二等奖','AWARD_LEVEL',2,1,'二等奖','2026-01-13 14:58:39','2026-01-13 14:58:39'),(23,'LEVEL_THIRD','三等奖','AWARD_LEVEL',3,1,'三等奖','2026-01-13 14:58:39','2026-01-13 14:58:39'),(24,'LEVEL_EXCELLENT','优胜奖','AWARD_LEVEL',4,1,'优胜奖','2026-01-13 14:58:39','2026-01-13 14:58:39'),(25,'LEVEL_PARTICIPATION','参与奖','AWARD_LEVEL',5,1,'参与奖','2026-01-13 14:58:39','2026-01-13 14:58:39'),(26,'LECTURE','讲座','ACTIVITY_TYPE',1,1,'专家讲座','2026-02-19 22:31:18','2026-02-19 22:31:18'),(27,'SEMINAR','讨论会','ACTIVITY_TYPE',2,1,'专题讨论会','2026-02-19 22:31:18','2026-02-19 22:31:18'),(28,'TEA_PARTY','茶话会','ACTIVITY_TYPE',3,1,'休闲交流活动','2026-02-19 22:31:18','2026-02-19 22:31:18'),(29,'PROJECT_INTRO','项目介绍','ACTIVITY_TYPE',4,1,'项目宣讲会','2026-02-19 22:31:18','2026-02-19 22:31:18'),(30,'WORKSHOP','工作坊','ACTIVITY_TYPE',5,1,'实践工作坊','2026-02-19 22:31:18','2026-02-19 22:31:18'),(31,'COMPETITION','竞赛','ACTIVITY_TYPE',6,1,'技术竞赛','2026-02-19 22:31:18','2026-02-19 22:31:18'),(32,'TRAINING','培训','ACTIVITY_TYPE',7,1,'技能培训','2026-02-19 22:31:18','2026-02-19 22:31:18'),(33,'TEAM_BUILDING','团建','ACTIVITY_TYPE',8,1,'团队建设活动','2026-02-19 22:31:18','2026-02-19 22:31:18'),(34,'OTHER','其他','ACTIVITY_TYPE',9,1,'其他类型活动','2026-02-19 22:31:18','2026-02-19 22:31:18'),(35,'WEB_DEV','WEB开发','PROJECT_TYPE',1,1,'Web前端/后端开发项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(36,'APP_DEV','APP开发','PROJECT_TYPE',2,1,'移动应用开发项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(37,'CS_DEV','CS开发','PROJECT_TYPE',3,1,'客户端/服务器开发项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(38,'PROTOCOL_DEV','协议开发','PROJECT_TYPE',4,1,'网络协议开发项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(39,'COMPREHENSIVE_DEV','综合开发','PROJECT_TYPE',5,1,'综合类开发项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(40,'HARMONY_DEV','鸿蒙开发','PROJECT_TYPE',6,1,'鸿蒙系统应用开发','2026-02-23 19:14:39','2026-02-23 19:14:39'),(41,'GAME_DEV','游戏开发','PROJECT_TYPE',7,1,'游戏开发项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(42,'AI_APP','人工智能应用','PROJECT_TYPE',8,1,'AI应用开发项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(43,'ALGORITHM_DESIGN','算法设计','PROJECT_TYPE',9,1,'算法设计与实现项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(44,'BEGINNER_FRIENDLY','适合新手','PROLABEL',1,1,'适合新手参与的项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(45,'CHALLENGING','有挑战','PROLABEL',2,1,'具有挑战性的项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(46,'INNOVATIVE','创新项目','PROLABEL',3,1,'创新类项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(47,'RESEARCH','科研类','PROLABEL',4,1,'科研类项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(48,'PRACTICAL','实战项目','PROLABEL',5,1,'实战落地项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(49,'OPEN_SOURCE','开源项目','PROLABEL',6,1,'开源项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(50,'TEAM_COLLABORATION','团队协作','PROLABEL',7,1,'注重团队协作的项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(51,'PENDING','申请审核中','PROJECT_STATUS',1,1,'项目申请待审批','2026-02-23 19:14:39','2026-02-23 19:14:39'),(52,'APPROVED','确认成立','PROJECT_STATUS',2,1,'项目已审批通过，正式成立','2026-02-23 19:14:39','2026-02-23 19:14:39'),(53,'IN_PROGRESS','进行中','PROJECT_STATUS',3,1,'项目已开始执行','2026-02-23 19:14:39','2026-02-23 19:14:39'),(54,'COMPLETED','已完成','PROJECT_STATUS',4,1,'项目已完成','2026-02-23 19:14:39','2026-02-23 19:14:39'),(55,'CANCELED','项目取消','PROJECT_STATUS',5,1,'项目被取消','2026-02-23 19:14:39','2026-02-23 19:14:39'),(56,'REJECTED','申请驳回','PROJECT_STATUS',6,1,'项目申请被驳回','2026-02-23 19:14:39','2026-02-23 19:14:39'),(57,'PAUSED','项目暂停','PROJECT_STATUS',7,1,'项目暂停执行','2026-02-23 19:14:39','2026-02-23 19:14:39'),(58,'PROJECT_APPLY','项目申请','PROJECT_HISTORY_OPERATION',1,1,'成员申请创建项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(59,'PROJECT_APPROVE','项目审批通过','PROJECT_HISTORY_OPERATION',2,1,'管理员审批通过项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(60,'PROJECT_REJECT','项目审批驳回','PROJECT_HISTORY_OPERATION',3,1,'管理员驳回项目申请','2026-02-23 19:14:39','2026-02-23 19:14:39'),(61,'PROJECT_TRANSFER','项目管理员转移','PROJECT_HISTORY_OPERATION',4,1,'项目管理员变更','2026-02-23 19:14:39','2026-02-23 19:14:39'),(62,'MEMBER_JOIN','成员加入','PROJECT_HISTORY_OPERATION',5,1,'成员加入项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(63,'MEMBER_APPLY','成员申请','PROJECT_HISTORY_OPERATION',6,1,'成员申请加入项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(64,'MEMBER_APPROVE','成员审批通过','PROJECT_HISTORY_OPERATION',7,1,'项目管理员批准加入申请','2026-02-23 19:14:39','2026-02-23 19:14:39'),(65,'MEMBER_REJECT','成员审批驳回','PROJECT_HISTORY_OPERATION',8,1,'项目管理员驳回加入申请','2026-02-23 19:14:39','2026-02-23 19:14:39'),(66,'MEMBER_LEAVE','成员离开','PROJECT_HISTORY_OPERATION',9,1,'成员离开项目','2026-02-23 19:14:39','2026-02-23 19:14:39'),(67,'PROJECT_INFO_UPDATE','项目信息修改','PROJECT_HISTORY_OPERATION',10,1,'项目信息被更新','2026-02-23 19:14:39','2026-02-23 19:14:39'),(68,'PROJECT_STATUS_CHANGE','项目状态变更','PROJECT_HISTORY_OPERATION',11,1,'项目状态发生变化','2026-02-23 19:14:39','2026-02-23 19:14:39'),(69,'PROJECT_LABEL_ADD','添加项目标签','PROJECT_HISTORY_OPERATION',12,1,'添加项目标签','2026-02-23 19:14:39','2026-02-23 19:14:39'),(70,'PROJECT_LABEL_REMOVE','移除项目标签','PROJECT_HISTORY_OPERATION',13,1,'移除项目标签','2026-02-23 19:14:39','2026-02-23 19:14:39');
/*!40000 ALTER TABLE `dictionary` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_storage`
--

DROP TABLE IF EXISTS `file_storage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `file_storage` (
  `id` int NOT NULL AUTO_INCREMENT,
  `create_by` int NOT NULL,
  `original_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `stored_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_path` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_create_by` (`create_by`),
  CONSTRAINT `file_storage_fk_create_by` FOREIGN KEY (`create_by`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_storage`
--

LOCK TABLES `file_storage` WRITE;
/*!40000 ALTER TABLE `file_storage` DISABLE KEYS */;
INSERT INTO `file_storage` VALUES (1,2,'无标题.png','1768292629855_无标题.png','/localstorage/images/award/1768292629855_无标题.png','image/png',334591,'award_image','2026-01-13 16:43:40',1),(2,2,'无标题.png','1768911339149_无标题.png','/localstorage/images/award/1768911339149_无标题.png','image/png',334591,'award_image','2026-01-20 20:15:39',1),(6,1,'avatar_1_1769427328806.png','avatar_1_1769427328806.png','/localstorage/images/avatar/avatar_1_1769427328806.png','image/png',181064,NULL,'2026-01-26 19:35:28',1),(7,2,'学习计划.png','1769520688167_学习计划.png','/localstorage/images/award/1769520688167_学习计划.png','image/png',181064,'award_image','2026-01-27 21:31:28',1),(8,2,'学习计划.png','1769521004817_学习计划.png','/localstorage/images/award/1769521004817_学习计划.png','image/png',181064,'award_image','2026-01-27 21:36:44',1),(9,2,'学习计划.png','1769565021366_学习计划.png','/localstorage/images/award/1769565021366_学习计划.png','image/png',181064,'award_image','2026-01-28 09:50:21',1),(10,2,'2.jpg','1771403876289_4fdfe3c617034e439736d1a010228fa8.jpg','images/avatar/1771403876289_4fdfe3c617034e439736d1a010228fa8.jpg','image/jpeg',216599,'avatar','2026-02-18 16:37:56',1),(11,2,'avatar_2_1771421266023.jpg','avatar_2_1771421266023.jpg','image/avatar/avatar_2_1771421266023.jpg','image/jpeg',216599,NULL,'2026-02-18 21:27:46',0),(12,1,'avatar_1_1771752503314.jpg','avatar_1_1771752503314.jpg','/localstorage/images/avatar/avatar_1_1771752503314.jpg','image/jpeg',216599,'avatar_image','2026-02-22 17:28:23',0),(13,1,'avatar_1_1771764163035.jpg','avatar_1_1771764163035.jpg','/localstorage/images/avatar/avatar_1_1771764163035.jpg','image/jpeg',216599,'avatar_image','2026-02-22 20:42:43',0),(14,1,'avatar_1_1771766460435.png','avatar_1_1771766460435.png','/localstorage/images/avatar/avatar_1_1771766460435.png','image/png',195459,'avatar_image','2026-02-22 21:21:00',0),(15,1,'avatar_1_1771766850819.jpg','avatar_1_1771766850819.jpg','/localstorage/images/avatar/avatar_1_1771766850819.jpg','image/jpeg',216599,'avatar_image','2026-02-22 21:27:30',0),(16,1,'avatar_1_1771767336882.png','avatar_1_1771767336882.png','/localstorage/images/avatar/avatar_1_1771767336882.png','image/png',195459,'avatar_image','2026-02-22 21:35:36',0),(17,1,'avatar_1_1771768467809.jpg','avatar_1_1771768467809.jpg','/localstorage/images/avatar/avatar_1_1771768467809.jpg','image/jpeg',216599,'avatar_image','2026-02-22 21:54:27',1),(18,1,'avatar_1_1771769015804.jpg','avatar_1_1771769015804.jpg','/localstorage/images/avatar/avatar_1_1771769015804.jpg','image/jpeg',216599,'avatar_image','2026-02-22 22:03:35',0),(19,1,'1.jpg','1771769112664_c7d470281caa4de082ab6b8ad807bd2d.jpg','images/avatar/1771769112664_c7d470281caa4de082ab6b8ad807bd2d.jpg','image/jpeg',216599,'avatar','2026-02-22 22:05:12',1),(20,2,'1.jpg','1771769150082_014181c87350403b91f02123a3638eec.jpg','images/avatar/1771769150082_014181c87350403b91f02123a3638eec.jpg','image/jpeg',216599,'avatar','2026-02-22 22:05:50',0),(21,1,'avatar_1_1771810057909.jpg','avatar_1_1771810057909.jpg','/localstorage/images/avatar/avatar_1_1771810057909.jpg','image/jpeg',216599,'avatar_image','2026-02-23 09:27:37',1),(22,1,'avatar_1_1771810102947.jpg','avatar_1_1771769015804.jpg','/localstorage/images/avatar/avatar_1_1771769015804.jpg','image/jpeg',216599,'avatar_image','2026-02-23 09:28:22',1),(23,1,'avatar_1_1771812593931.png','avatar_1_1771812593931.png','/localstorage/images/avatar/avatar_1_1771812593931.png','image/png',468567,'avatar_image','2026-02-23 10:09:53',0),(24,1,'avatar_1_1771812897100.jpg','avatar_1_1771812897100.jpg','/localstorage/images/avatar/avatar_1_1771812897100.jpg','image/jpeg',216599,'avatar_image','2026-02-23 10:14:57',0),(25,1,'avatar_1_1771813218950.png','avatar_1_1771813218950.png','/localstorage/images/avatar/avatar_1_1771813218950.png','image/png',34016,'avatar_image','2026-02-23 10:20:18',0),(26,1,'avatar_1_1771813378767.jpg','avatar_1_1771813378767.jpg','/localstorage/images/avatar/avatar_1_1771813378767.jpg','image/jpeg',216599,'avatar_image','2026-02-23 10:22:58',1),(27,1,'avatar_1_1771813808074.png','avatar_1_1771813808074.png','/localstorage/images/avatar/avatar_1_1771813808074.png','image/png',195459,'avatar_image','2026-02-23 10:30:08',0),(28,1,'avatar_1_1771814491335.jpg','avatar_1_1771814491335.jpg','/localstorage/images/avatar/avatar_1_1771814491335.jpg','image/jpeg',216599,'avatar_image','2026-02-23 10:41:31',0),(29,1,'avatar_1_1771814517029.jpg','avatar_1_1771814517029.jpg','/localstorage/images/avatar/avatar_1_1771814517029.jpg','image/jpeg',216599,'avatar_image','2026-02-23 10:41:57',0),(30,1,'avatar_1_1771814680333.jpg','avatar_1_1771814680333.jpg','/localstorage/images/avatar/avatar_1_1771814680333.jpg','image/jpeg',216599,'avatar_image','2026-02-23 10:44:40',0),(31,1,'avatar_1_1771814697384.png','avatar_1_1771814697384.png','/localstorage/images/avatar/avatar_1_1771814697384.png','image/png',195459,'avatar_image','2026-02-23 10:44:57',0),(32,1,'avatar_1_1771815161050.jpg','avatar_1_1771815161050.jpg','/localstorage/images/avatar/avatar_1_1771815161050.jpg','image/jpeg',216599,'avatar_image','2026-02-23 10:52:41',1),(33,2,'avatar_2_1771815232706.png','avatar_2_1771815232706.png','/localstorage/images/avatar/avatar_2_1771815232706.png','image/png',468567,'avatar','2026-02-23 10:53:52',0),(34,2,'avatar_2_1771815647892.png','avatar_2_1771815647892.png','/localstorage/images/avatar/avatar_2_1771815647892.png','image/png',195459,'avatar_image','2026-02-23 11:00:47',1);
/*!40000 ALTER TABLE `file_storage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member_profile`
--

DROP TABLE IF EXISTS `member_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_profile` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `student_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `birthday` date DEFAULT NULL,
  `gender` enum('male','female','other') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'other',
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `skills` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `github` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `blog` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `avatar_file_id` int DEFAULT NULL COMMENT '头像文件ID，关联file_storage表',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `fk_member_avatar` (`avatar_file_id`),
  CONSTRAINT `fk_member_avatar` FOREIGN KEY (`avatar_file_id`) REFERENCES `file_storage` (`id`) ON DELETE SET NULL,
  CONSTRAINT `member_profile_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member_profile`
--

LOCK TABLES `member_profile` WRITE;
/*!40000 ALTER TABLE `member_profile` DISABLE KEYS */;
INSERT INTO `member_profile` VALUES (7,2,'22222','NewMajor','2021','2026-02-04','other','NewBio',NULL,'newgithub54134','https://new.com32452','2026-02-23 03:00:48',34,'2026-02-18 13:22:06'),(8,3,'33333','计算机科学','2024',NULL,'other',NULL,NULL,NULL,NULL,'2026-02-21 11:05:33',NULL,'2026-02-21 11:05:33'),(9,27,'22406032001','计算机科学与技术','24级',NULL,'other',NULL,NULL,NULL,NULL,'2026-03-10 13:32:36',NULL,'2026-03-10 13:32:36'),(10,28,'22506031033','计算机科学与技术','大一',NULL,'other',NULL,NULL,NULL,NULL,'2026-03-10 13:45:53',NULL,'2026-03-10 13:45:53');
/*!40000 ALTER TABLE `member_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `news`
--

DROP TABLE IF EXISTS `news`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `news` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '新闻标题\n',
  `type` enum('award','activity','notice') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '新闻类型\n',
  `content_path` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '新闻地址，html文件所在地\n',
  `summary` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '新闻摘要\n',
  `author_id` int DEFAULT NULL COMMENT '作者ID',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '审核状态\n',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `author_id` (`author_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `news_ibfk_1` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鏂伴椈琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `news`
--

LOCK TABLES `news` WRITE;
/*!40000 ALTER TABLE `news` DISABLE KEYS */;
INSERT INTO `news` VALUES (1,'tesat','notice','localstorage/news/notice/1768192494445_notice.html','asgfa',1,1,'2026-01-12 12:34:54','2026-01-12 12:34:54'),(2,'sgasdgads','award','localstorage/news/award/1768192512844_award.html','adsgadsgads',1,1,'2026-01-12 12:35:12','2026-01-12 12:35:12'),(3,'asdas','notice','localstorage/news/notice/1768192524931_notice.html','asfhafh',1,1,'2026-01-12 12:35:24','2026-01-12 12:35:24'),(4,'asdhash','activity','localstorage/news/activity/1768192532845_activity.html','afhhad',1,1,'2026-01-12 12:35:32','2026-01-12 12:35:32'),(5,'asgash噶啥','notice','localstorage/news/notice/1768215581939_notice.html','asdfhafsh',1,1,'2026-01-12 18:59:41','2026-01-12 19:50:31'),(6,'gasghas','activity','localstorage/news/activity/1768978232798_activity.html','agsdgads',1,1,'2026-01-21 14:50:32','2026-01-21 14:50:32'),(7,'AI','notice','localstorage/news/notice/1773151013205_notice.html','现在AI的走向',1,1,'2026-03-10 21:56:53','2026-03-10 21:56:53');
/*!40000 ALTER TABLE `news` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `operation_log`
--

DROP TABLE IF EXISTS `operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `operation_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=1354 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operation_log`
--

LOCK TABLES `operation_log` WRITE;
/*!40000 ALTER TABLE `operation_log` DISABLE KEYS */;
INSERT INTO `operation_log` VALUES (1,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 21:26:58'),(2,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 21:36:18'),(3,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 21:36:30'),(4,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 21:38:00'),(5,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 21:49:41'),(6,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 21:52:58'),(7,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 21:53:22'),(8,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 21:57:05'),(9,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 22:01:44'),(10,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 22:08:15'),(11,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 22:30:27'),(12,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 22:34:33'),(13,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 22:53:36'),(14,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 22:54:57'),(15,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 22:57:44'),(16,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 11:45:34'),(17,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 11:48:37'),(18,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 11:51:19'),(19,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:26:03'),(20,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:26:29'),(21,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:27:21'),(22,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:31:15'),(23,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:31:29'),(24,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:33:38'),(25,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:33:47'),(26,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:33:54'),(27,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:34:46'),(28,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:34:54'),(29,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:35:12'),(30,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:35:24'),(31,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:35:32'),(32,1,'admin','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:36:05'),(33,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 13:49:18'),(34,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 13:53:07'),(35,1,'admin','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 13:56:32'),(36,1,'admin','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 13:57:22'),(37,1,'admin','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 13:58:09'),(38,1,'admin','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 13:58:33'),(39,1,'admin','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:01:04'),(40,1,'admin','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:04:02'),(41,1,'admin','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:05:21'),(42,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:09:16'),(43,1,'admin','POST','管理员','POST /Software_group_Web_exploded/admin/recruit/manage','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:11:31'),(44,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:16:12'),(45,1,'admin','POST','管理员','POST /Software_group_Web_exploded/admin/recruit/manage','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:16:46'),(46,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:20:45'),(47,1,'admin','POST','管理员','POST /Software_group_Web_exploded/admin/recruit/manage','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:21:08'),(48,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:26:01'),(49,1,'admin','POST','管理员','POST /Software_group_Web_exploded/admin/recruit/manage','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:26:16'),(50,NULL,'游客','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:12:41'),(51,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:13:01'),(52,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:22:25'),(53,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:36:48'),(54,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:39:15'),(55,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:41:50'),(56,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:46:23'),(57,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:50:16'),(58,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:52:26'),(59,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 18:48:21'),(60,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 18:58:03'),(61,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 18:58:14'),(62,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 18:58:30'),(63,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 18:59:41'),(64,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:06:38'),(65,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:07:06'),(66,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:08:36'),(67,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:08:43'),(68,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:10:31'),(69,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:10:37'),(70,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:10:44'),(71,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:10:58'),(72,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:11:03'),(73,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:12:12'),(74,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:12:20'),(75,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:13:28'),(76,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:13:41'),(77,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:14:58'),(78,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:15:06'),(79,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:16:37'),(80,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:16:43'),(81,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:18:32'),(82,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:18:48'),(83,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:20:43'),(84,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:20:56'),(85,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:22:55'),(86,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:23:15'),(87,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:24:39'),(88,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:24:47'),(89,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:24:55'),(90,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:25:39'),(91,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:25:50'),(92,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:27:03'),(93,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:31:05'),(94,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:31:13'),(95,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:34:07'),(96,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:34:16'),(97,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:40:33'),(98,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:44:18'),(99,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:46:03'),(100,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=delete&id=5','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:50:20'),(101,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:50:30'),(102,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 20:09:43'),(103,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 20:13:23'),(104,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 20:15:54'),(105,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 21:04:24'),(106,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 21:05:13'),(107,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 21:42:01'),(108,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 21:48:49'),(109,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:07:15'),(110,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:07:58'),(111,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:08:06'),(112,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:14:32'),(113,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:14:44'),(114,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:15:10'),(115,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:19:41'),(116,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:20:03'),(117,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:21:58'),(118,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:22:18'),(119,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 09:52:11'),(120,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 09:52:33'),(121,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 09:56:17'),(122,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 09:56:41'),(123,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 09:58:59'),(124,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 09:59:22'),(125,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:02:07'),(126,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:02:26'),(127,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:06:29'),(128,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:06:48'),(129,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:12:35'),(130,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:13:00'),(131,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:19:07'),(132,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:19:26'),(133,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:26:34'),(134,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:26:56'),(135,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:31:46'),(136,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:32:06'),(137,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:32:38'),(138,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:36:41'),(139,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:36:41'),(140,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:37:00'),(141,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:40:07'),(142,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:40:28'),(143,NULL,'游客','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:43:42'),(144,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:43:44'),(145,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:44:16'),(146,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:44:52'),(147,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:07:33'),(148,NULL,'游客','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:07:50'),(149,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:07:51'),(150,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:08:19'),(151,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:10:28'),(152,NULL,'游客','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:10:38'),(153,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:10:59'),(154,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:12:49'),(155,NULL,'游客','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:14:52'),(156,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:14:53'),(157,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:15:17'),(158,NULL,'游客','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:18:24'),(159,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:18:25'),(160,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:18:46'),(161,NULL,'游客','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:20:28'),(162,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:20:29'),(163,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:20:48'),(164,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:27:18'),(165,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:27:40'),(166,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:30:34'),(167,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:38:05'),(168,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:38:28'),(169,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:41:35'),(170,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:47:18'),(171,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:47:58'),(172,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 12:36:28'),(173,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 14:09:37'),(174,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 14:21:23'),(175,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 14:55:37'),(176,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:00:40'),(177,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:01:09'),(178,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:01:28'),(179,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:01:42'),(180,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:03:20'),(181,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:19:28'),(182,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:19:53'),(183,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:23:11'),(184,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:29:15'),(185,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:42:09'),(186,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:43:05'),(187,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:49:07'),(188,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:52:49'),(189,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:52:50'),(190,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:58:28'),(191,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 16:07:30'),(192,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 16:07:30'),(193,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 16:08:01'),(194,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 16:08:16'),(195,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 16:12:47'),(196,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 16:23:28'),(197,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 16:23:49'),(198,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 16:58:29'),(199,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:00:27'),(200,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:03:15'),(201,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:09:24'),(202,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:12:21'),(203,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:18:03'),(204,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:23:55'),(205,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:27:06'),(206,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:29:28'),(207,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:29:46'),(208,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:30:43'),(209,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:33:52'),(210,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:41:18'),(211,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:48:18'),(212,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:50:18'),(213,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:53:24'),(214,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:56:55'),(215,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 20:44:31'),(216,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 20:45:04'),(217,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 20:45:09'),(218,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 20:57:54'),(219,1,'admin','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 20:58:02'),(220,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 20:58:16'),(221,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 21:02:03'),(222,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 21:11:42'),(223,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 21:15:44'),(224,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 21:15:58'),(225,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 21:16:17'),(226,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 21:19:24'),(227,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 21:36:58'),(228,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 20:10:18'),(229,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 20:14:45'),(230,1,'admin','POST','奖项','POST /Software_group_Web_exploded/award?action=approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 20:14:56'),(231,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 20:15:08'),(232,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 20:15:38'),(233,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 20:15:51'),(234,1,'admin','POST','奖项','POST /Software_group_Web_exploded/award?action=reject','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 20:15:58'),(235,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 20:58:18'),(236,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 21:12:41'),(237,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 21:14:02'),(238,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 21:38:45'),(239,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 22:23:14'),(240,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 22:24:20'),(241,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 09:08:39'),(242,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 10:12:37'),(243,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 10:44:13'),(244,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 10:56:35'),(245,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 11:02:33'),(246,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 11:05:07'),(247,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 11:28:13'),(248,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:14:27'),(249,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:24:58'),(250,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:34:14'),(251,1,'admin','POST','其他','POST /SoftwareGroupManagement_Web_exploded/upload-avatar','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:35:23'),(252,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:37:48'),(253,1,'admin','POST','活动','POST /SoftwareGroupManagement_Web_exploded/activity','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:38:26'),(254,1,'admin','POST','项目','POST /SoftwareGroupManagement_Web_exploded/project','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:39:14'),(255,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:47:47'),(256,1,'admin','POST','项目','POST /SoftwareGroupManagement_Web_exploded/project','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:47:57'),(257,1,'admin','POST','活动','POST /SoftwareGroupManagement_Web_exploded/activity','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:48:24'),(258,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:50:19'),(259,1,'admin','POST','新闻','POST /SoftwareGroupManagement_Web_exploded/news','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:50:32'),(260,1,'admin','POST','活动','POST /SoftwareGroupManagement_Web_exploded/activity','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:50:53'),(261,1,'admin','POST','项目','POST /SoftwareGroupManagement_Web_exploded/project','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:51:29'),(262,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:55:02'),(263,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:56:01'),(264,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:56:08'),(265,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 14:59:41'),(266,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 15:00:15'),(267,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 15:01:04'),(268,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 15:01:10'),(269,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 15:04:44'),(270,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 15:06:34'),(271,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 15:10:51'),(272,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 15:10:57'),(273,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 15:39:07'),(274,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 15:39:32'),(275,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 15:41:59'),(276,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 15:42:04'),(277,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 15:46:36'),(278,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 15:46:58'),(279,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 15:57:15'),(280,1,'admin','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 15:57:16'),(281,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 15:57:23'),(282,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:01:43'),(283,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:02:44'),(284,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:07:29'),(285,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:07:34'),(286,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:12:05'),(287,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:12:15'),(288,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:12:26'),(289,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:12:38'),(290,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:12:50'),(291,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:15:12'),(292,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:15:15'),(293,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:15:22'),(294,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:15:27'),(295,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:36:23'),(296,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:36:31'),(297,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:39:09'),(298,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:39:19'),(299,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:39:43'),(300,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:39:59'),(301,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:40:02'),(302,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:40:24'),(303,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:40:42'),(304,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/member/','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:41:42'),(305,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:47:36'),(306,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/member/','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:47:41'),(307,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/member/','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:47:45'),(308,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/member/','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 16:48:13'),(309,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 17:05:55'),(310,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 17:05:57'),(311,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 17:12:33'),(312,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 17:13:06'),(313,1,'admin','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-21 17:31:38'),(314,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:05:20'),(315,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:06:54'),(316,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:10:24'),(317,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:14:07'),(318,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:17:02'),(319,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:29:37'),(320,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:33:33'),(321,1,'admin','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:33:38'),(322,1,'admin','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:33:43'),(323,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:36:29'),(324,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:39:11'),(325,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:40:57'),(326,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:42:08'),(327,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:42:35'),(328,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:45:33'),(329,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:50:05'),(330,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 08:57:29'),(331,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 09:38:07'),(332,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 09:44:57'),(333,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 09:49:23'),(334,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 09:52:14'),(335,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 09:57:20'),(336,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 09:57:31'),(337,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 10:03:53'),(338,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 10:10:31'),(339,NULL,'游客','POST','其他','POST /SoftwareGroupManagement_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 10:18:45'),(340,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 10:19:04'),(341,1,'admin','POST','管理员','POST /SoftwareGroupManagement_Web_exploded/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 OPR/126.0.0.0','2026-01-22 10:19:45'),(342,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-23 21:35:33'),(343,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-23 21:41:40'),(344,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-23 21:41:44'),(345,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-23 21:44:57'),(346,1,'admin','POST','管理员','POST /Software_group_Web_exploded/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-23 21:45:01'),(347,1,'admin','POST','管理员','POST /Software_group_Web_exploded/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-23 21:45:04'),(348,1,'admin','POST','管理员','POST /Software_group_Web_exploded/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-23 21:45:06'),(349,1,'admin','POST','管理员','POST /Software_group_Web_exploded/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-23 21:45:08'),(350,1,'admin','POST','管理员','POST /Software_group_Web_exploded/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-23 21:45:35'),(351,1,'admin','POST','管理员','POST /Software_group_Web_exploded/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-23 21:45:52'),(352,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-23 21:47:50'),(353,1,'admin','POST','管理员','POST /Software_group_Web_exploded/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-23 21:47:59'),(354,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 09:50:25'),(355,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 09:50:33'),(356,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 09:50:36'),(357,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 10:22:38'),(358,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 20:39:06'),(359,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 20:39:31'),(360,1,'admin','POST','项目','POST /Software_group_v2_Web_exploded2/project','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 20:47:36'),(361,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 20:53:50'),(362,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 20:54:34'),(363,1,'admin','POST','项目','POST /Software_group_v2_Web_exploded2/project','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 20:55:56'),(364,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:02:30'),(365,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:02:51'),(366,1,'admin','POST','活动','POST /Software_group_v2_Web_exploded2/activity','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:05:49'),(367,1,'admin','POST','项目','POST /Software_group_v2_Web_exploded2/project','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:06:04'),(368,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:11:36'),(369,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:12:03'),(370,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:14:13'),(371,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:14:46'),(372,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:16:57'),(373,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:17:24'),(374,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:18:35'),(375,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:20:48'),(376,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:21:00'),(377,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:23:09'),(378,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:28:54'),(379,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:29:03'),(380,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:32:31'),(381,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:32:35'),(382,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:32:39'),(383,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:33:09'),(384,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:55:55'),(385,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:56:01'),(386,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:56:40'),(387,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 21:58:32'),(388,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 22:08:22'),(389,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 22:08:33'),(390,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 22:15:17'),(391,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 22:16:23'),(392,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 22:21:44'),(393,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 22:22:22'),(394,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 22:22:58'),(395,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 22:29:27'),(396,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 22:29:53'),(397,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-25 22:30:21'),(398,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-26 19:06:54'),(399,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-26 19:07:23'),(400,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-26 19:19:16'),(401,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-26 19:22:08'),(402,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-26 19:23:57'),(403,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-26 19:24:22'),(404,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-26 19:24:34'),(405,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-26 19:28:26'),(406,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-26 19:28:48'),(407,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-26 19:29:26'),(408,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-26 19:35:10'),(409,1,'admin','POST','管理员','POST /Software_group_v2_Web_exploded2/admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-26 19:35:28'),(410,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-26 19:41:43'),(411,NULL,'游客','POST','其他','POST /Software_group_v2_Web_exploded2/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-26 21:35:30'),(412,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:02:33'),(413,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:03:13'),(414,2,'member1','POST','奖项','POST /award?action=submit','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:04:04'),(415,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:21:42'),(416,2,'member1','POST','奖项','POST /award?action=submit','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:22:06'),(417,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:23:52'),(418,2,'member1','POST','奖项','POST /award?action=submit','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:24:15'),(419,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:27:52'),(420,2,'member1','POST','奖项','POST /award?action=submit','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:28:12'),(421,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:31:04'),(422,2,'member1','POST','奖项','POST /award?action=submit','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:31:27'),(423,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:31:42'),(424,1,'admin','POST','奖项','POST /award?action=approve','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:31:53'),(425,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:36:06'),(426,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:36:15'),(427,2,'member1','POST','奖项','POST /award?action=submit','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:36:43'),(428,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:36:52'),(429,1,'admin','POST','奖项','POST /award?action=reject','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:36:56'),(430,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:37:05'),(431,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 21:48:23'),(432,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 22:04:54'),(433,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 22:27:54'),(434,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 22:44:15'),(435,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 22:51:03'),(436,2,'member1','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-27 22:51:23'),(437,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 09:08:41'),(438,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 09:19:48'),(439,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 09:46:02'),(440,2,'member1','POST','奖项','POST /award?action=submit','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 09:50:19'),(441,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 10:27:04'),(442,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 10:43:23'),(443,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 10:45:00'),(444,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 10:49:32'),(445,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 10:53:02'),(446,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 10:57:24'),(447,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 11:03:09'),(448,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 11:22:24'),(449,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 11:24:40'),(450,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 11:26:36'),(451,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 11:33:36'),(452,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 11:45:29'),(453,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 11:50:58'),(454,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 11:56:15'),(455,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 11:56:18'),(456,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-28 13:44:11'),(457,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-01-30 08:20:38'),(458,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-06 10:19:50'),(459,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-08 08:59:44'),(460,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 20:45:07'),(461,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 20:45:58'),(462,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 20:46:29'),(463,1,'admin','POST','管理员','POST /Software_group/admin/api/dashboard','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 20:46:29'),(464,1,'admin','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 20:46:57'),(465,1,'admin','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 20:47:48'),(466,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 20:47:53'),(467,NULL,'游客','POST','其他','POST /Software_group/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-15 20:48:43'),(468,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:23:25'),(469,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:41:01'),(470,1,'admin','POST','管理员','POST /Software_group/admin/api/dashboard','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:41:02'),(471,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:41:12'),(472,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:41:45'),(473,1,'admin','POST','管理员','POST /Software_group/admin/api/dashboard','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:41:46'),(474,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:41:46'),(475,2,'member1','POST','成员','POST /Software_group/member/index.jsp','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:41:47'),(476,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:41:54'),(477,1,'admin','POST','管理员','POST /Software_group/admin/api/dashboard','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:41:54'),(478,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:42:00'),(479,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:42:33'),(480,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:42:33'),(481,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:42:34'),(482,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:42:35'),(483,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:43:04'),(484,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:43:05'),(485,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:43:05'),(486,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:43:06'),(487,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:43:06'),(488,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:43:26'),(489,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:43:27'),(490,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:44:41'),(491,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:49:27'),(492,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:58:04'),(493,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 21:59:45'),(494,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:04:24'),(495,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:05:08'),(496,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:05:08'),(497,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:09:26'),(498,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:09:29'),(499,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:09:33'),(500,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:11:28'),(501,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:11:31'),(502,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:11:34'),(503,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:12:31'),(504,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:12:32'),(505,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:13:36'),(506,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:13:36'),(507,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:14:43'),(508,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:14:43'),(509,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:14:43'),(510,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:15:22'),(511,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:15:23'),(512,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:16:29'),(513,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:32:47'),(514,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:33:09'),(515,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:33:18'),(516,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:34:59'),(517,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:35:51'),(518,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:36:07'),(519,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:37:07'),(520,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:37:17'),(521,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:37:29'),(522,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:37:34'),(523,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:37:39'),(524,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-15 22:37:56'),(525,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 09:36:52'),(526,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 09:36:59'),(527,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 09:38:20'),(528,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 09:38:27'),(529,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 09:38:35'),(530,1,'admin','POST','管理员','POST /Software_group/admin/recruit/manage','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 09:38:35'),(531,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 09:38:42'),(532,1,'admin','POST','管理员','POST /Software_group/admin/recruit/manage','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 09:38:43'),(533,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 09:39:05'),(534,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 09:39:43'),(535,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 09:42:12'),(536,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 09:42:20'),(537,1,'admin','POST','管理员','POST /admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 09:43:20'),(538,1,'admin','POST','奖项','POST /award?action=approve','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 09:44:29'),(539,1,'admin','POST','奖项','POST /award?action=approve','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 09:44:33'),(540,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 09:47:44'),(541,1,'admin','POST','管理员','POST /Software_group/admin/recruit/reject','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 09:47:52'),(542,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 09:48:09'),(543,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 09:48:16'),(544,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 09:49:32'),(545,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 09:49:38'),(546,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 09:49:45'),(547,1,'admin','POST','奖项','POST /award?action=approve','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 09:49:52'),(548,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 09:55:54'),(549,1,'admin','POST','奖项','POST /Software_group/award?action=approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 09:57:38'),(550,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:00:54'),(551,1,'admin','POST','奖项','POST /Software_group/award?action=approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:02:36'),(552,1,'admin','POST','奖项','POST /Software_group/award?action=approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:05:38'),(553,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:05:56'),(554,1,'admin','POST','奖项','POST /Software_group/award?action=approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:06:04'),(555,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:07:11'),(556,1,'admin','POST','奖项','POST /Software_group/award?action=approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:07:12'),(557,1,'admin','POST','奖项','POST /Software_group/award?action=approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:07:20'),(558,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:08:46'),(559,1,'admin','POST','奖项','POST /Software_group/award?action=reject','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:09:06'),(560,1,'admin','POST','奖项','POST /Software_group/award?action=reject','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:09:31'),(561,1,'admin','POST','奖项','POST /Software_group/award?action=reject','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:09:47'),(562,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:10:45'),(563,1,'admin','POST','奖项','POST /Software_group/award?action=approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:10:46'),(564,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:11:56'),(565,1,'admin','POST','奖项','POST /Software_group/award?action=approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:11:57'),(566,1,'admin','POST','奖项','POST /Software_group/award?action=reject','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:13:10'),(567,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:13:37'),(568,1,'admin','POST','奖项','POST /Software_group/award?action=approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:13:45'),(569,1,'admin','POST','奖项','POST /Software_group/award?action=approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:14:49'),(570,1,'admin','POST','奖项','POST /Software_group/award?action=approveList','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:14:49'),(571,1,'admin','POST','奖项','POST /Software_group/award?action=approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:14:57'),(572,1,'admin','POST','奖项','POST /Software_group/award?action=approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:15:33'),(573,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:16:13'),(574,1,'admin','POST','奖项','POST /Software_group/award?action=approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:16:13'),(575,1,'admin','POST','奖项','POST /Software_group/award?action=approve','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:16:46'),(576,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:17:42'),(577,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 10:19:04'),(578,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 10:19:20'),(579,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:21:51'),(580,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:22:18'),(581,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:23:11'),(582,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:24:12'),(583,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:24:32'),(584,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:25:21'),(585,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 10:27:41'),(586,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:31:36'),(587,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:32:19'),(588,NULL,'游客','POST','其他','POST /login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:35:05'),(589,NULL,'游客','POST','其他','POST /login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:35:31'),(590,NULL,'游客','POST','其他','POST /login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:36:52'),(591,NULL,'游客','POST','其他','POST /login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:39:03'),(592,NULL,'游客','POST','其他','POST /login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:40:07'),(593,NULL,'游客','POST','其他','POST /login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:42:09'),(594,NULL,'游客','POST','其他','POST /login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:42:49'),(595,NULL,'游客','POST','其他','POST /login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 10:46:29'),(596,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 10:53:40'),(597,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 11:12:19'),(598,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 11:12:40'),(599,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 11:12:51'),(600,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 11:13:01'),(601,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 11:13:12'),(602,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 11:13:38'),(603,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 11:13:53'),(604,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 11:34:59'),(605,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 11:45:43'),(606,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 12:46:32'),(607,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 12:47:58'),(608,NULL,'游客','POST','其他','POST /Software_group/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 14:29:24'),(609,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 14:30:20'),(610,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 14:41:25'),(611,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 14:43:20'),(612,NULL,'游客','POST','其他','POST /Software_group/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 14:52:28'),(613,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 14:58:37'),(614,NULL,'游客','POST','其他','POST /Software_group/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 15:01:44'),(615,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 15:05:08'),(616,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 15:08:26'),(617,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 15:10:47'),(618,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 15:13:24'),(619,NULL,'游客','POST','其他','POST /Software_group/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 16:00:05'),(620,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:03:23'),(621,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:05:40'),(622,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:05:45'),(623,1,'admin','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:06:03'),(624,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:07:19'),(625,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:07:23'),(626,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 16:10:03'),(627,1,'admin','POST','管理员','POST /Software_group/admin/dashboard','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 16:10:03'),(628,1,'admin','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 16:10:41'),(629,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 16:10:56'),(630,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:11:54'),(631,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:12:25'),(632,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:12:35'),(633,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:13:23'),(634,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:14:15'),(635,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:16:17'),(636,1,'admin','POST','管理员','POST /Software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:16:37'),(637,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 16:20:53'),(638,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:33:51'),(639,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:34:50'),(640,2,'member1','POST','其他','POST /Software_group/upload-avatar','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:37:55'),(641,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:38:04'),(642,1,'admin','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 16:38:14'),(643,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 17:12:03'),(644,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 17:12:11'),(645,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 17:13:41'),(646,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 17:14:06'),(647,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 17:14:26'),(648,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 17:15:22'),(649,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 17:18:24'),(650,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 18:28:47'),(651,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 18:32:33'),(652,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 18:32:51'),(653,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 18:34:16'),(654,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 18:37:06'),(655,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 18:43:33'),(656,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 18:50:35'),(657,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 18:54:10'),(658,2,'member1','POST','成员','POST /member/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 19:07:12'),(659,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 19:11:51'),(660,2,'member1','POST','其他','POST /Software_group/profile','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 19:11:58'),(661,2,'member1','POST','成员','POST /Software_group/member/profile','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 19:12:13'),(662,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 19:12:34'),(663,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 19:13:15'),(664,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 19:16:00'),(665,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 19:16:09'),(666,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 19:19:01'),(667,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 19:19:11'),(668,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 19:21:05'),(669,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 19:21:14'),(670,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 19:23:20'),(671,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 20:59:57'),(672,2,'member1','POST','成员','POST /member/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-18 21:00:45'),(673,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:02:42'),(674,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:02:50'),(675,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:03:36'),(676,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:04:29'),(677,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:08:39'),(678,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:12:06'),(679,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:14:03'),(680,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:14:49'),(681,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:16:11'),(682,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:16:26'),(683,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:18:17'),(684,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:19:54'),(685,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:20:21'),(686,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:23:29'),(687,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:23:54'),(688,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:27:45'),(689,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:33:21'),(690,2,'member1','POST','成员','POST /Software_group/member/password','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:33:21'),(691,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:34:57'),(692,2,'member1','POST','成员','POST /Software_group/member/password','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:35:08'),(693,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:35:27'),(694,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:35:37'),(695,2,'member1','POST','成员','POST /Software_group/member/password','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:35:37'),(696,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 21:36:06'),(697,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:39:26'),(698,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 21:40:38'),(699,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:45:43'),(700,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:45:54'),(701,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 21:46:30'),(702,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:46:34'),(703,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:46:35'),(704,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:51:57'),(705,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:52:04'),(706,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:52:14'),(707,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:53:26'),(708,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:53:27'),(709,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:53:38'),(710,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:53:49'),(711,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:54:46'),(712,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:56:32'),(713,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:56:32'),(714,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:59:15'),(715,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:59:15'),(716,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:59:27'),(717,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:59:39'),(718,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 21:59:47'),(719,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 22:00:34'),(720,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 22:00:57'),(721,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 22:01:24'),(722,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 22:01:45'),(723,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-18 22:02:06'),(724,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 22:12:04'),(725,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 22:52:38'),(726,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-18 22:52:51'),(727,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-19 19:23:28'),(728,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:04:34'),(729,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:04:42'),(730,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:04:56'),(731,NULL,'游客','POST','招新','POST /Software_group/recruit/success','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:05:01'),(732,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:05:07'),(733,NULL,'游客','POST','招新','POST /Software_group/recruit/success','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:05:08'),(734,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:05:15'),(735,NULL,'游客','POST','招新','POST /Software_group/recruit/success','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:05:44'),(736,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:05:58'),(737,NULL,'游客','POST','招新','POST /Software_group/recruit/success','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:05:58'),(738,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:06:38'),(739,NULL,'游客','POST','招新','POST /Software_group/recruit/success','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:06:38'),(740,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:06:47'),(741,NULL,'游客','POST','招新','POST /Software_group/recruit/success','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:06:48'),(742,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:07:57'),(743,NULL,'游客','POST','招新','POST /Software_group/recruit/success','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:07:57'),(744,NULL,'游客','POST','招新','POST /Software_group/recruit/submit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:08:34'),(745,NULL,'游客','POST','招新','POST /Software_group/recruit/success','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:08:34'),(746,NULL,'游客','POST','招新','POST /Software_group/recruit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:08:58'),(747,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:09:16'),(748,1,'admin','POST','管理员','POST /Software_group/admin/recruit/edit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:09:35'),(749,1,'admin','POST','管理员','POST /Software_group/admin/recruit/edit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:09:47'),(750,1,'admin','POST','管理员','POST /Software_group/admin/recruit/edit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:09:55'),(751,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:10:34'),(752,1,'admin','POST','管理员','POST /Software_group/admin/recruit/edit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:10:41'),(753,1,'admin','POST','管理员','POST /Software_group/admin/recruit/edit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:11:47'),(754,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:12:23'),(755,1,'admin','POST','管理员','POST /Software_group/admin/recruit/edit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:12:24'),(756,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:12:41'),(757,1,'admin','POST','管理员','POST /Software_group/admin/recruit/edit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:12:41'),(758,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:13:07'),(759,1,'admin','POST','管理员','POST /Software_group/admin/recruit/edit','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:13:07'),(760,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:13:18'),(761,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:13:19'),(762,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:13:32'),(763,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:13:33'),(764,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:13:42'),(765,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:13:54'),(766,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:13:55'),(767,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:17:38'),(768,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:25:10'),(769,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:31:41'),(770,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-19 22:33:53'),(771,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:24:29'),(772,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:25:47'),(773,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:31:51'),(774,2,'member1','POST','活动','POST /Software_group/activity?action=register&activityId=6','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:32:13'),(775,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:37:16'),(776,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:38:58'),(777,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:39:40'),(778,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:39:40'),(779,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:39:52'),(780,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:39:52'),(781,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:39:57'),(782,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:39:57'),(783,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:40:21'),(784,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:40:21'),(785,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:40:41'),(786,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:40:57'),(787,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:40:57'),(788,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:41:02'),(789,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:41:03'),(790,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:41:07'),(791,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:41:08'),(792,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:41:34'),(793,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:41:34'),(794,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:41:45'),(795,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:41:45'),(796,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:41:51'),(797,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:45:27'),(798,2,'member1','POST','活动','POST /Software_group/activity?action=register&activityId=7','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:45:28'),(799,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:45:32'),(800,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:45:40'),(801,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:45:41'),(802,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:45:45'),(803,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:45:46'),(804,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:46:16'),(805,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:46:16'),(806,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:46:35'),(807,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:46:35'),(808,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:47:23'),(809,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:47:23'),(810,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:47:34'),(811,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:47:34'),(812,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:48:01'),(813,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:52:06'),(814,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:52:45'),(815,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:53:50'),(816,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:54:47'),(817,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:55:09'),(818,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:55:10'),(819,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:55:42'),(820,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:55:43'),(821,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:56:08'),(822,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:56:42'),(823,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:56:43'),(824,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 18:57:11'),(825,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:03:11'),(826,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:03:21'),(827,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:03:22'),(828,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:03:36'),(829,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:03:36'),(830,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:04:19'),(831,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:04:20'),(832,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:04:48'),(833,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:04:49'),(834,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:05:43'),(835,3,'12445','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:05:43'),(836,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:05:59'),(837,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:05:59'),(838,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:06:05'),(839,3,'12445','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:06:05'),(840,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:06:14'),(841,3,'12445','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:06:14'),(842,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:06:52'),(843,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:06:53'),(844,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:07:09'),(845,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:07:10'),(846,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:08:13'),(847,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 19:09:54'),(848,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-21 20:22:47'),(849,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-21 20:22:52'),(850,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 20:38:52'),(851,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 20:39:22'),(852,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 20:39:37'),(853,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 20:40:12'),(854,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 20:40:12'),(855,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 20:41:27'),(856,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-21 20:42:42'),(857,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-21 20:49:08'),(858,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 20:56:55'),(859,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 20:57:23'),(860,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 20:57:32'),(861,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 20:58:28'),(862,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-21 21:01:24'),(863,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-21 21:01:33'),(864,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-21 21:01:48'),(865,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-21 21:01:51'),(866,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-21 21:02:04'),(867,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 21:25:47'),(868,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 21:28:38'),(869,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 21:29:56'),(870,2,'member1','POST','活动','POST /Software_group/activity?action=register&activityId=10','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 21:30:38'),(871,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-21 21:34:51'),(872,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-21 21:35:17'),(873,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-21 21:35:26'),(874,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-21 21:35:34'),(875,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 22:02:17'),(876,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 22:02:36'),(877,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-21 22:22:25'),(878,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-21 22:40:49'),(879,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-22 17:20:49'),(880,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-22 17:22:01'),(881,24,'FINAL21771164883','POST','活动','POST /activity','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-22 17:22:16'),(882,24,'FINAL21771164883','POST','活动','POST /activity','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-22 17:22:19'),(883,24,'FINAL21771164883','POST','活动','POST /activity','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-22 17:22:31'),(884,24,'FINAL21771164883','POST','活动','POST /activity','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-22 17:22:42'),(885,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-22 17:25:21'),(886,1,'admin','POST','管理员','POST /admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-22 17:28:23'),(887,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 17:32:13'),(888,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 18:01:15'),(889,2,'member1','POST','活动','POST /Software_group/activity?action=register&activityId=11','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 18:01:35'),(890,2,'member1','POST','活动','POST /Software_group/activity?action=register&activityId=8','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 18:02:26'),(891,2,'member1','POST','活动','POST /Software_group/activity?action=register&activityId=7','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 18:02:43'),(892,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 18:02:49'),(893,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 18:04:48'),(894,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 18:04:57'),(895,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-22 18:31:05'),(896,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-22 18:31:59'),(897,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 18:39:37'),(898,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 18:39:38'),(899,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 18:41:05'),(900,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 18:41:06'),(901,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 18:41:51'),(902,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 18:41:52'),(903,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 18:43:41'),(904,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 18:43:42'),(905,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 18:44:25'),(906,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-22 18:52:24'),(907,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 19:00:46'),(908,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 19:01:38'),(909,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 19:01:39'),(910,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-22 19:02:36'),(911,1,'admin','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-22 19:03:00'),(912,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-22 20:38:19'),(913,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-22 20:42:42'),(914,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-22 21:09:39'),(915,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 21:15:19'),(916,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 21:15:51'),(917,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-22 21:20:58'),(918,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 21:24:56'),(919,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-22 21:27:18'),(920,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-22 21:27:30'),(921,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-22 21:35:25'),(922,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-22 21:35:36'),(923,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-22 21:54:17'),(924,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-22 21:54:27'),(925,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36','2026-02-22 22:03:35'),(926,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 22:04:59'),(927,1,'admin','POST','其他','POST /Software_group/upload-avatar','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 22:05:11'),(928,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 22:05:49'),(929,2,'member1','POST','其他','POST /Software_group/upload-avatar','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-22 22:05:50'),(930,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-23 09:27:27'),(931,1,'admin','POST','管理员','POST /admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-23 09:27:37'),(932,NULL,'游客','POST','其他','POST /login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-23 09:28:14'),(933,1,'admin','POST','管理员','POST /admin/api/profile/update','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-23 09:28:22'),(934,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 09:38:05'),(935,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 09:38:05'),(936,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-23 10:09:43'),(937,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-23 10:09:53'),(938,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:14:48'),(939,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:14:56'),(940,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:18:57'),(941,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:19:06'),(942,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:20:18'),(943,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-23 10:22:51'),(944,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-23 10:22:58'),(945,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:29:21'),(946,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:30:07'),(947,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:38:05'),(948,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:39:00'),(949,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 10:41:25'),(950,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 10:41:30'),(951,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 10:41:56'),(952,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:44:39'),(953,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:44:57'),(954,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:45:18'),(955,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:52:30'),(956,1,'admin','POST','管理员','POST /Software_group/admin/api/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:52:40'),(957,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:53:16'),(958,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:53:52'),(959,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:58:26'),(960,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 10:58:33'),(961,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 11:00:31'),(962,2,'member1','POST','成员','POST /Software_group/member/profile/update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 11:00:47'),(963,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 11:01:31'),(964,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 11:08:15'),(965,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 11:16:09'),(966,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 12:15:00'),(967,NULL,'游客','POST','其他','POST /Software_group/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-23 12:22:20'),(968,NULL,'游客','POST','其他','POST /Software_group/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0','2026-02-23 13:09:51'),(969,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 13:10:34'),(970,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 13:11:19'),(971,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 13:11:34'),(972,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 13:13:31'),(973,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 13:19:18'),(974,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 14:35:05'),(975,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 14:39:13'),(976,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 14:39:34'),(977,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 14:45:14'),(978,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-02-23 14:50:26'),(979,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 14:57:36'),(980,1,'admin','POST','管理员','POST /Software_group/admin/member/','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 15:04:49'),(981,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 15:05:27'),(982,2,'member1','POST','成员','POST /Software_group/member/password','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 15:31:09'),(983,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 15:31:16'),(984,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 15:31:21'),(985,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 15:34:15'),(986,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 15:34:42'),(987,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 16:16:21'),(988,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 16:16:50'),(989,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 16:16:59'),(990,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 16:17:12'),(991,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 16:28:48'),(992,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 16:29:02'),(993,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 16:29:17'),(994,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 16:29:58'),(995,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 16:30:28'),(996,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 16:30:42'),(997,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 16:32:00'),(998,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 16:32:00'),(999,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 16:42:21'),(1000,NULL,'游客','POST','其他','POST /Software_group/login','127.0.0.1','curl/8.17.0','2026-02-23 17:07:49'),(1001,NULL,'游客','POST','其他','POST /Software_group/login','127.0.0.1','curl/8.17.0','2026-02-23 17:13:17'),(1002,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 17:19:29'),(1003,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 17:20:49'),(1004,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 17:31:14'),(1005,NULL,'游客','POST','其他','POST /Software_group/login','127.0.0.1','curl/8.17.0','2026-02-23 17:47:17'),(1006,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 18:00:51'),(1007,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 18:02:49'),(1008,NULL,'游客','POST','其他','POST /Software_group/login','127.0.0.1','curl/8.17.0','2026-02-23 18:08:24'),(1009,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 18:09:00'),(1010,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 18:10:57'),(1011,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 19:57:52'),(1012,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:05:12'),(1013,NULL,'游客','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:05:25'),(1014,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:05:54'),(1015,NULL,'游客','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:06:05'),(1016,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:07:06'),(1017,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:07:40'),(1018,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:07:51'),(1019,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:08:13'),(1020,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:10:14'),(1021,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:10:41'),(1022,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 20:11:24'),(1023,NULL,'游客','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:14:44'),(1024,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:14:48'),(1025,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:14:57'),(1026,2,'member1','POST','项目','POST /Software_group/project/','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:15:30'),(1027,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:21:35'),(1028,2,'member1','POST','项目','POST /Software_group/project?action=create','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:21:54'),(1029,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:24:38'),(1030,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:24:47'),(1031,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:25:10'),(1032,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:27:51'),(1033,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:27:52'),(1034,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 20:37:16'),(1035,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:37:18'),(1036,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:37:18'),(1037,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:38:40'),(1038,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:40:55'),(1039,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:41:15'),(1040,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 20:42:12'),(1041,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:44:22'),(1042,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:45:07'),(1043,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:52:24'),(1044,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:52:56'),(1045,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 20:57:36'),(1046,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 20:58:57'),(1047,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 21:00:27'),(1048,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:06:41'),(1049,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:09:05'),(1050,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:09:43'),(1051,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:10:15'),(1052,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:11:40'),(1053,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 21:12:28'),(1054,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 21:12:38'),(1055,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:24:00'),(1056,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:24:09'),(1057,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:24:47'),(1058,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:25:06'),(1059,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:25:40'),(1060,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:25:54'),(1061,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:26:07'),(1062,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:27:15'),(1063,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:27:15'),(1064,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:29:02'),(1065,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:29:28'),(1066,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-23 21:29:55'),(1067,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:30:11'),(1068,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:31:52'),(1069,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:32:20'),(1070,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:32:28'),(1071,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:32:41'),(1072,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:32:53'),(1073,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:33:35'),(1074,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:33:46'),(1075,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:34:02'),(1076,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:34:16'),(1077,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:35:17'),(1078,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:35:50'),(1079,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:36:22'),(1080,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:36:22'),(1081,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:37:50'),(1082,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:38:06'),(1083,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:38:48'),(1084,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:38:48'),(1085,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:39:05'),(1086,2,'member1','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:39:05'),(1087,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:39:25'),(1088,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:40:02'),(1089,1,'admin','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:40:02'),(1090,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:41:15'),(1091,1,'admin','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:41:15'),(1092,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:41:51'),(1093,1,'admin','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:41:51'),(1094,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 21:52:41'),(1095,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 22:27:22'),(1096,NULL,'游客','POST','项目','POST /Software_group/project?action=create','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 22:28:13'),(1097,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 22:29:55'),(1098,NULL,'游客','POST','项目','POST /Software_group/project?action=create','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 22:30:17'),(1099,NULL,'游客','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 22:30:38'),(1100,NULL,'游客','POST','项目','POST /Software_group/project','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 22:35:03'),(1101,NULL,'游客','POST','项目','POST /Software_group/project?action=create','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 22:35:15'),(1102,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 22:36:26'),(1103,NULL,'游客','POST','项目','POST /Software_group/project?action=create','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 22:36:39'),(1104,NULL,'游客','POST','项目','POST /Software_group/project?action=create','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 22:37:09'),(1105,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 22:37:42'),(1106,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 22:38:13'),(1107,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-23 22:38:44'),(1108,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-24 15:14:29'),(1109,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-24 15:21:11'),(1110,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-24 15:21:13'),(1111,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-24 15:29:27'),(1112,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 15:37:21'),(1113,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 15:38:45'),(1114,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 15:39:41'),(1115,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 15:40:30'),(1116,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 15:40:52'),(1117,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-24 15:42:00'),(1118,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 18:55:34'),(1119,NULL,'游客','POST','项目','POST /Software_group/project?action=create','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 18:55:39'),(1120,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 18:55:48'),(1121,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 18:56:12'),(1122,2,'member1','POST','项目','POST /Software_group/project?action=create','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 18:56:24'),(1123,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 18:56:35'),(1124,1,'admin','POST','项目','POST /Software_group/project?action=update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 18:57:50'),(1125,1,'admin','POST','项目','POST /Software_group/project?action=update','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 18:59:05'),(1126,1,'admin','POST','项目','POST /Software_group/project?action=update&id=8','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 18:59:46'),(1127,1,'admin','POST','项目','POST /Software_group/project?action=update&id=8','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 19:00:44'),(1128,1,'admin','POST','项目','POST /Software_group/project?action=saveProject&id=8','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 19:01:53'),(1129,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 19:02:23'),(1130,1,'admin','POST','项目','POST /Software_group/project?action=saveProject&id=8','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 19:02:23'),(1131,1,'admin','POST','项目','POST /Software_group/project?action=saveProject&id=8','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 19:03:05'),(1132,1,'admin','POST','项目','POST /Software_group/project?action=saveProject&id=8','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 19:04:01'),(1133,1,'admin','POST','项目','POST /Software_group/project?action=update&id=8','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 19:04:31'),(1134,1,'admin','POST','项目','POST /Software_group/project?action=update&id=8','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 19:05:37'),(1135,2,'member1','POST','项目','POST /Software_group/project?action=create','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 19:05:54'),(1136,2,'member1','POST','项目','POST /Software_group/project?action=create','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 19:08:14'),(1137,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 19:08:59'),(1138,1,'admin','POST','项目','POST /Software_group/project?action=update&id=8','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 19:08:59'),(1139,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 19:09:51'),(1140,1,'admin','POST','项目','POST /Software_group/project?action=update&id=8','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 19:09:51'),(1141,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:26:24'),(1142,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:27:20'),(1143,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:27:58'),(1144,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:28:59'),(1145,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:30:44'),(1146,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:31:20'),(1147,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:32:04'),(1148,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:32:55'),(1149,2,'member1','POST','成员','POST /Software_group/member/index.jsp','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:32:55'),(1150,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:33:24'),(1151,1,'admin','POST','管理员','POST /Software_group/admin/dashboard','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:33:24'),(1152,1,'admin','POST','活动','POST /Software_group/activity?action=create','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:33:52'),(1153,2,'member1','POST','活动','POST /Software_group/activity?action=register&activityId=18','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:34:31'),(1154,1,'admin','POST','活动','POST /Software_group/activity?action=create','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:35:20'),(1155,1,'admin','POST','活动','POST /Software_group/activity?action=update&id=18','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:35:47'),(1156,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:49:54'),(1157,1,'admin','POST','管理员','POST /Software_group/admin/dashboard','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:49:55'),(1158,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:50:03'),(1159,1,'admin','POST','管理员','POST /Software_group/admin/dashboard','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:50:04'),(1160,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:50:14'),(1161,1,'admin','POST','活动','POST /Software_group/activity?action=update&id=1','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:50:24'),(1162,1,'admin','POST','活动','POST /Software_group/activity?action=update&id=6','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:50:38'),(1163,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-24 20:52:51'),(1164,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:56:51'),(1165,2,'member1','POST','成员','POST /Software_group/member/index.jsp','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:56:51'),(1166,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:57:34'),(1167,2,'member1','POST','成员','POST /Software_group/member/index.jsp','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:57:34'),(1168,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:58:21'),(1169,2,'member1','POST','成员','POST /Software_group/member/index.jsp','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:58:21'),(1170,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:59:48'),(1171,2,'member1','POST','成员','POST /Software_group/member/index.jsp','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 20:59:48'),(1172,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 21:02:48'),(1173,2,'member1','POST','成员','POST /Software_group/member/index.jsp','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 21:02:49'),(1174,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 21:03:56'),(1175,2,'member1','POST','成员','POST /Software_group/member/index.jsp','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 21:03:57'),(1176,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 21:05:18'),(1177,2,'member1','POST','成员','POST /Software_group/member/index.jsp','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 21:05:18'),(1178,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 21:06:42'),(1179,2,'member1','POST','成员','POST /Software_group/member/index.jsp','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 21:06:42'),(1180,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 21:09:06'),(1181,2,'member1','POST','成员','POST /Software_group/member/index.jsp','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 21:09:07'),(1182,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-24 21:10:45'),(1183,2,'member1','POST','活动','POST /Software_group/activity','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-24 21:13:11'),(1184,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-24 21:13:35'),(1185,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 21:27:10'),(1186,1,'admin','POST','活动','POST /Software_group/activity?action=create','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 21:28:17'),(1187,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','curl/8.17.0','2026-02-24 21:28:39'),(1188,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-24 21:29:15'),(1189,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-24 21:29:50'),(1190,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-02-24 21:37:15'),(1191,NULL,'游客','POST','其他','POST /Software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36','2026-03-07 16:03:13'),(1192,NULL,'游客','POST','其他','POST /Software_group/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:148.0) Gecko/20100101 Firefox/148.0','2026-03-07 16:03:43'),(1193,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 20:00:40'),(1194,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 21:30:17'),(1195,NULL,'游客','POST','招新','POST /software_group/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 21:31:54'),(1196,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 21:32:11'),(1197,1,'admin','POST','管理员','POST /software_group/admin/recruit/approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 21:32:36'),(1198,NULL,'游客','POST','其他','POST /software_group/login','192.168.63.33','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 21:42:50'),(1199,NULL,'游客','POST','其他','POST /software_group/login','192.168.63.33','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 21:44:36'),(1200,NULL,'游客','POST','招新','POST /software_group/recruit/submit','192.168.63.50','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36 NetType/WIFI MicroMessenger/7.0.20.1781(0x6700143B) WindowsWechat(0x63090a13) UnifiedPCWindowsWechat(0xf2541721) XWEB/18787 Flue','2026-03-10 21:45:27'),(1201,1,'admin','POST','管理员','POST /software_group/admin/recruit/approve','192.168.63.33','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 21:45:53'),(1202,NULL,'游客','POST','其他','POST /software_group/login','192.168.63.50','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36 NetType/WIFI MicroMessenger/7.0.20.1781(0x6700143B) WindowsWechat(0x63090a13) UnifiedPCWindowsWechat(0xf2541721) XWEB/18787 Flue','2026-03-10 21:46:58'),(1203,NULL,'游客','POST','其他','POST /software_group/login','192.168.63.33','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 21:51:47'),(1204,NULL,'游客','POST','其他','POST /software_group/login','192.168.63.33','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 21:55:08'),(1205,1,'admin','POST','新闻','POST /software_group/news','192.168.63.33','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 21:56:53'),(1206,NULL,'游客','POST','其他','POST /software_group/login','192.168.63.33','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 21:57:58'),(1207,27,'22406032001','POST','项目','POST /software_group/project?action=create','192.168.63.33','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 22:00:14'),(1208,NULL,'游客','POST','其他','POST /software_group/login','192.168.63.33','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 22:01:19'),(1209,1,'admin','POST','项目','POST /software_group/project','192.168.63.33','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 22:01:28'),(1210,1,'admin','POST','活动','POST /software_group/activity','192.168.63.33','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 22:03:20'),(1211,NULL,'游客','POST','其他','POST /software_group/login','192.168.63.33','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 22:03:52'),(1212,27,'22406032001','POST','活动','POST /software_group/activity','192.168.63.33','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 22:04:10'),(1213,27,'22406032001','POST','活动','POST /software_group/activity','192.168.63.33','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 22:04:26'),(1214,NULL,'游客','POST','其他','POST /software_group/login','192.168.63.33','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-10 22:04:38'),(1215,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-11 12:56:28'),(1216,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-11 13:55:01'),(1217,NULL,'游客','POST','其他','POST /software_group/login','192.168.63.13','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36 NetType/WIFI MicroMessenger/7.0.20.1781(0x6700143B) WindowsWechat(0x63090a13) UnifiedPCWindowsWechat(0xf2541739) XWEB/18955 Flue','2026-03-11 13:55:56'),(1218,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-11 13:56:54'),(1219,NULL,'游客','POST','其他','POST /software_group/login','192.168.63.13','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36 NetType/WIFI MicroMessenger/7.0.20.1781(0x6700143B) WindowsWechat(0x63090a13) UnifiedPCWindowsWechat(0xf2541739) XWEB/18955 Flue','2026-03-11 13:56:59'),(1220,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-11 14:25:47'),(1221,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-11 14:25:53'),(1222,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-11 14:26:06'),(1223,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-11 14:28:38'),(1224,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-11 17:39:22'),(1225,27,'22406032001','POST','成员','POST /software_group/member/password','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-11 17:45:37'),(1226,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-11 18:29:01'),(1227,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-11 18:30:54'),(1228,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-11 21:52:57'),(1229,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-11 22:03:13'),(1230,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 08:16:21'),(1231,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 09:30:39'),(1232,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 13:41:33'),(1233,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 15:13:18'),(1234,27,'22406032001','POST','其他','POST /software_group/resume','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 15:14:37'),(1235,27,'22406032001','POST','其他','POST /software_group/resume','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 15:23:21'),(1236,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 15:23:41'),(1237,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 15:28:26'),(1238,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 15:48:14'),(1239,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 15:58:52'),(1240,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 15:59:39'),(1241,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 19:00:06'),(1242,27,'22406032001','POST','其他','POST /software_group/resume','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 19:02:11'),(1243,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 19:02:42'),(1244,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 19:04:23'),(1245,27,'22406032001','POST','其他','POST /software_group/resume','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 19:04:56'),(1246,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 19:17:50'),(1247,27,'22406032001','POST','其他','POST /software_group/resume','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 19:18:06'),(1248,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 19:53:58'),(1249,27,'22406032001','POST','其他','POST /software_group/resume','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 19:54:07'),(1250,27,'22406032001','POST','其他','POST /software_group/resume','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 19:54:13'),(1251,27,'22406032001','POST','其他','POST /software_group/resume/education','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 19:56:21'),(1252,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 19:57:40'),(1253,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 19:58:22'),(1254,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 20:06:21'),(1255,27,'22406032001','POST','其他','POST /software_group/resume','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 20:06:38'),(1256,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 21:32:23'),(1257,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-12 22:03:41'),(1258,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 08:29:14'),(1259,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 08:41:53'),(1260,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 09:11:51'),(1261,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 09:21:22'),(1262,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 09:29:23'),(1263,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 09:39:48'),(1264,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 09:42:03'),(1265,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 09:54:21'),(1266,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 17:24:50'),(1267,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 19:33:50'),(1268,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 20:03:40'),(1269,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 20:04:32'),(1270,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 20:10:23'),(1271,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 21:26:20'),(1272,27,'22406032001','POST','项目','POST /software_group/resume/project','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 21:27:17'),(1273,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 21:49:15'),(1274,27,'22406032001','POST','其他','POST /software_group/resume','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 21:49:36'),(1275,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-13 21:50:44'),(1276,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-14 14:25:30'),(1277,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0','2026-03-15 13:06:59'),(1278,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 08:19:05'),(1279,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 09:28:17'),(1280,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 09:30:01'),(1281,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 09:53:37'),(1282,27,'22406032001','POST','奖项','POST /software_group/resume/award','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 09:54:44'),(1283,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 11:12:56'),(1284,27,'22406032001','POST','奖项','POST /software_group/resume/award','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 11:13:29'),(1285,27,'22406032001','POST','奖项','POST /software_group/resume/award','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 11:13:49'),(1286,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 20:36:41'),(1287,27,'22406032001','POST','奖项','POST /software_group/resume/award','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 20:37:02'),(1288,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 20:48:34'),(1289,27,'22406032001','POST','奖项','POST /software_group/resume/award','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 20:49:12'),(1290,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 20:58:25'),(1291,27,'22406032001','POST','奖项','POST /software_group/resume/award','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 20:58:39'),(1292,27,'22406032001','POST','奖项','POST /software_group/resume/award','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 20:59:44'),(1293,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 22:09:46'),(1294,27,'22406032001','POST','奖项','POST /software_group/resume/award','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 22:10:02'),(1295,27,'22406032001','POST','其他','POST /software_group/resume','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 22:10:20'),(1296,27,'22406032001','POST','奖项','POST /software_group/resume/award','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 22:10:32'),(1297,27,'22406032001','POST','奖项','POST /software_group/resume/award','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-16 22:10:54'),(1298,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 09:23:59'),(1299,27,'22406032001','POST','项目','POST /software_group/resume/project','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 09:24:31'),(1300,27,'22406032001','POST','奖项','POST /software_group/resume/award','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 09:24:55'),(1301,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 09:40:43'),(1302,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 09:54:49'),(1303,27,'22406032001','POST','其他','POST /software_group/resume/education','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 09:55:07'),(1304,27,'22406032001','POST','奖项','POST /software_group/resume/award','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 09:55:34'),(1305,27,'22406032001','POST','奖项','POST /software_group/resume/award','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 09:55:46'),(1306,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 09:56:28'),(1307,27,'22406032001','POST','奖项','POST /software_group/resume/award','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 09:56:59'),(1308,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 09:57:37'),(1309,27,'22406032001','POST','奖项','POST /software_group/resume/award','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 09:57:55'),(1310,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 10:53:02'),(1311,27,'22406032001','POST','奖项','POST /software_group/resume/award','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 10:53:27'),(1312,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 16:00:41'),(1313,27,'22406032001','POST','其他','POST /software_group/resume/skill','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 16:01:22'),(1314,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 16:34:39'),(1315,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 16:59:53'),(1316,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 20:49:23'),(1317,27,'22406032001','POST','其他','POST /software_group/resume','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 20:50:26'),(1318,NULL,'游客','POST','其他','POST /software_group/login','192.168.5.25','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 21:30:24'),(1319,NULL,'游客','POST','其他','POST /software_group/login','192.168.5.25','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 21:30:46'),(1320,NULL,'游客','POST','其他','POST /software_group/login','192.168.5.25','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 21:31:05'),(1321,NULL,'游客','POST','其他','POST /software_group/login','220.179.124.179','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 21:49:49'),(1322,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-17 22:18:43'),(1323,NULL,'游客','POST','招新','POST /software_group/recruit/submit','39.144.252.219, 120.240.95.112','Mozilla/5.0 (Linux; Android 16; V2359A) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.200 Mobile Safari/537.36 VivoBrowser/28.2.20.0','2026-03-17 22:19:31'),(1324,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-18 22:05:49'),(1325,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 08:40:01'),(1326,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 14:01:28'),(1327,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 14:49:04'),(1328,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 15:13:20'),(1329,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 15:28:18'),(1330,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 15:33:23'),(1331,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 15:33:34'),(1332,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 15:33:37'),(1333,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 15:33:51'),(1334,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 15:42:46'),(1335,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 16:21:00'),(1336,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 16:34:00'),(1337,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 16:34:35'),(1338,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 16:52:30'),(1339,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 18:57:45'),(1340,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 19:18:00'),(1341,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 19:21:10'),(1342,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 20:07:24'),(1343,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 20:09:51'),(1344,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 21:52:10'),(1345,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 22:22:43'),(1346,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-19 22:23:05'),(1347,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-20 14:02:09'),(1348,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-20 14:02:54'),(1349,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-20 14:03:38'),(1350,27,'22406032001','POST','成员','POST /software_group/member/password','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-20 14:04:02'),(1351,27,'22406032001','POST','成员','POST /software_group/member/password','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-20 14:04:22'),(1352,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-20 14:04:38'),(1353,NULL,'游客','POST','其他','POST /software_group/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0','2026-03-21 15:18:27');
/*!40000 ALTER TABLE `operation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `year` int NOT NULL,
  `expected_start_date` date DEFAULT NULL,
  `expected_end_date` date DEFAULT NULL,
  `actual_start_date` date DEFAULT NULL,
  `actual_end_date` date DEFAULT NULL,
  `status` enum('pending','approved','in_progress','completed','canceled','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `approved_by` int DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `admin_id` int DEFAULT NULL,
  `leader_id` int NOT NULL,
  `budget` decimal(10,2) DEFAULT NULL,
  `repo_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `doc_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `leader_id` (`leader_id`),
  KEY `fk_project_admin` (`admin_id`),
  CONSTRAINT `fk_project_admin` FOREIGN KEY (`admin_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `project_ibfk_1` FOREIGN KEY (`leader_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project`
--

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
INSERT INTO `project` VALUES (1,'gasdgas','gasdas','Web开发',2025,NULL,NULL,NULL,NULL,'pending',NULL,NULL,NULL,1,NULL,NULL,NULL,'2026-01-21 06:51:29','2026-01-21 06:51:29',0),(2,'发生的a\'s','as撒大噶是','移动应用',2026,NULL,NULL,NULL,NULL,'pending',NULL,NULL,NULL,1,NULL,NULL,NULL,'2026-01-25 12:47:37','2026-01-25 12:47:37',0),(3,'撒大噶','噶啥的噶啥噶哈哈啊','Web开发',2025,NULL,NULL,NULL,NULL,'pending',NULL,NULL,NULL,1,NULL,NULL,NULL,'2026-01-25 12:55:56','2026-01-25 12:55:56',0),(4,'撒大噶是规范和咖啡馆喝咖啡','哈啥a\'a\'ha\'d\'f','Web开发',2026,NULL,NULL,NULL,NULL,'pending',NULL,NULL,NULL,1,NULL,NULL,NULL,'2026-01-25 13:06:04','2026-01-25 13:06:04',0),(5,'TestProject444','testdesc','WEB_DEV',2026,NULL,NULL,NULL,NULL,'approved',1,'2026-02-23 20:47:13',2,2,NULL,NULL,NULL,'2026-02-23 12:41:15','2026-02-23 12:47:13',0),(6,'TestProjectWithDates','test with dates','WEB_DEV',2026,'2026-03-01','2026-12-31',NULL,NULL,'pending',NULL,NULL,2,2,10000.00,'https://github.com/test/test',NULL,'2026-02-23 12:44:22','2026-02-23 12:44:22',0),(7,'AdminProject','CreatedByAdmin','WEB_DEV',2026,NULL,NULL,NULL,NULL,'in_progress',NULL,NULL,NULL,1,NULL,NULL,NULL,'2026-02-23 13:40:02','2026-02-23 13:42:42',0),(8,'UpdatedAgain','Desc2','WEB_DEV',2022,'2022-02-01','2022-08-01','2022-03-01','2022-07-01','approved',NULL,NULL,2,2,5000.00,'https://github.com/test',NULL,'2026-02-24 10:56:25','2026-02-24 11:09:52',0),(9,'TestNewProject','New project test','WEB_DEV',2024,'2024-01-01','2024-12-31',NULL,NULL,'pending',NULL,NULL,2,2,10000.00,NULL,NULL,'2026-02-24 11:05:55','2026-02-24 11:05:55',0),(10,'TestProject2025','Test desc','WEB_DEV',2025,'2025-01-01','2025-12-31',NULL,NULL,'pending',NULL,NULL,2,2,NULL,NULL,NULL,'2026-02-24 11:08:14','2026-02-24 11:08:14',0),(11,'软件小组系统','开发一个关于软件小组的网页','CS_DEV',2026,'2026-03-10','2026-03-31',NULL,NULL,'approved',1,'2026-03-10 22:01:28',27,27,100.00,'https://edu.gitee.com/hsuniversity/repos/hsuniversity/softwaregroup/','https://edu.gitee.com/hsuniversity/repos/hsuniversity/softwaregroup/','2026-03-10 14:00:14','2026-03-10 14:01:28',0);
/*!40000 ALTER TABLE `project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_file`
--

DROP TABLE IF EXISTS `project_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_file` (
  `id` int NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `file_id` int NOT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_pf_project` (`project_id`),
  KEY `fk_pf_file` (`file_id`),
  CONSTRAINT `fk_pf_file` FOREIGN KEY (`file_id`) REFERENCES `file_storage` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pf_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_file`
--

LOCK TABLES `project_file` WRITE;
/*!40000 ALTER TABLE `project_file` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_history`
--

DROP TABLE IF EXISTS `project_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `operation_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `operator_id` int NOT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_history`
--

LOCK TABLES `project_history` WRITE;
/*!40000 ALTER TABLE `project_history` DISABLE KEYS */;
INSERT INTO `project_history` VALUES (1,5,'PROJECT_APPLY',2,'NewTest','成员申请创建项目',NULL,'TestProject444','2026-02-23 12:41:16'),(2,6,'PROJECT_APPLY',2,'NewTest','成员申请创建项目',NULL,'TestProjectWithDates','2026-02-23 12:44:23'),(3,5,'PROJECT_APPROVE',1,'张三2','项目审批通过','PENDING','APPROVED','2026-02-23 12:45:17'),(4,5,'PROJECT_APPROVE',1,'张三2','项目审批通过','PENDING','APPROVED','2026-02-23 12:47:13'),(5,5,'MEMBER_APPROVE',2,'NewTest','成员审批通过','PENDING','CONFIRMED','2026-02-23 13:06:42'),(6,5,'PROJECT_INFO_UPDATE',2,'NewTest','添加项目计划: ��һ�׶ο���',NULL,'��һ�׶ο���','2026-02-23 13:09:05'),(7,5,'PROJECT_INFO_UPDATE',2,'NewTest','添加项目进度: ����������',NULL,'���������� (0%)','2026-02-23 13:09:43'),(8,5,'PROJECT_LABEL_ADD',2,'NewTest','添加项目标签',NULL,'BEGINNER_FRIENDLY','2026-02-23 13:11:40'),(9,5,'MEMBER_APPROVE',2,'NewTest','成员审批通过','PENDING','CONFIRMED','2026-02-23 13:29:29'),(10,5,'MEMBER_REJECT',2,'NewTest','成员审批驳回: null','PENDING','REJECTED','2026-02-23 13:30:12'),(11,5,'PROJECT_LABEL_ADD',2,'NewTest','添加项目标签',NULL,'CHALLENGING','2026-02-23 13:37:50'),(12,5,'PROJECT_LABEL_REMOVE',2,'NewTest','移除项目标签','CHALLENGING',NULL,'2026-02-23 13:38:07'),(13,5,'PROJECT_INFO_UPDATE',2,'NewTest','添加项目计划: Phase1Dev',NULL,'Phase1Dev','2026-02-23 13:38:49'),(14,5,'PROJECT_INFO_UPDATE',2,'NewTest','添加项目进度: DesignComplete',NULL,'DesignComplete (50%)','2026-02-23 13:39:06'),(15,8,'PROJECT_APPLY',2,'NewTest','成员申请创建项目',NULL,'TestProjectWithDates','2026-02-24 10:56:25'),(16,8,'PROJECT_UPDATE',1,'张三2','修改项目信息','TestProjectWithDates','TestProjectWithDates','2026-02-24 10:57:50'),(17,8,'PROJECT_UPDATE',1,'张三2','修改项目信息','TestProjectWithDates','TestProjectWithDates','2026-02-24 10:59:05'),(18,8,'PROJECT_UPDATE',1,'张三2','修改项目信息','TestProjectWithDates','TestProjectWithDates','2026-02-24 10:59:46'),(19,8,'PROJECT_UPDATE',1,'张三2','修改项目信息','TestProjectWithDates','TestProjectWithDates','2026-02-24 11:00:45'),(20,8,'PROJECT_UPDATE',1,'张三2','修改项目信息','TestProjectWithDates','UpdatedTitle2','2026-02-24 11:04:32'),(21,8,'PROJECT_UPDATE',1,'张三2','修改项目信息','UpdatedTitle2','FinalTitle','2026-02-24 11:05:37'),(22,9,'PROJECT_APPLY',2,'NewTest','成员申请创建项目',NULL,'TestNewProject','2026-02-24 11:05:55'),(23,10,'PROJECT_APPLY',2,'NewTest','成员申请创建项目',NULL,'TestProject2025','2026-02-24 11:08:15'),(24,8,'PROJECT_UPDATE',1,'张三2','修改项目信息','FinalTitle','UpdatedTitleFinal','2026-02-24 11:09:00'),(25,8,'PROJECT_UPDATE',1,'张三2','修改项目信息','UpdatedTitleFinal','UpdatedAgain','2026-02-24 11:09:53'),(26,11,'PROJECT_APPLY',27,'康阿辉','成员申请创建项目',NULL,'软件小组系统','2026-03-10 14:00:14'),(27,11,'PROJECT_APPROVE',1,'张三2','项目审批通过','PENDING','APPROVED','2026-03-10 14:01:28');
/*!40000 ALTER TABLE `project_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_image`
--

DROP TABLE IF EXISTS `project_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_image` (
  `id` int NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `file_id` int NOT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_pimg_project` (`project_id`),
  KEY `fk_pimg_file` (`file_id`),
  CONSTRAINT `fk_pimg_file` FOREIGN KEY (`file_id`) REFERENCES `file_storage` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pimg_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_image`
--

LOCK TABLES `project_image` WRITE;
/*!40000 ALTER TABLE `project_image` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_label`
--

DROP TABLE IF EXISTS `project_label`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_label` (
  `id` int NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `label_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_label` (`project_id`,`label_code`),
  CONSTRAINT `fk_pl_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_label`
--

LOCK TABLES `project_label` WRITE;
/*!40000 ALTER TABLE `project_label` DISABLE KEYS */;
INSERT INTO `project_label` VALUES (1,5,'BEGINNER_FRIENDLY','2026-02-23 13:11:40');
/*!40000 ALTER TABLE `project_label` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_member`
--

DROP TABLE IF EXISTS `project_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_member` (
  `project_id` int NOT NULL,
  `user_id` int NOT NULL,
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`project_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `project_member_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `project_member_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_member`
--

LOCK TABLES `project_member` WRITE;
/*!40000 ALTER TABLE `project_member` DISABLE KEYS */;
INSERT INTO `project_member` VALUES (2,1,'负责人'),(3,1,'负责人'),(4,1,'负责人'),(5,2,'ADMIN'),(5,3,'MEMBER'),(5,22,'MEMBER'),(6,2,'ADMIN'),(8,2,'ADMIN'),(9,2,'ADMIN'),(10,2,'ADMIN'),(11,27,'ADMIN');
/*!40000 ALTER TABLE `project_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_member_application`
--

DROP TABLE IF EXISTS `project_member_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_member_application` (
  `id` int NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `user_id` int NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT 'PENDING/CONFIRMED/REJECTED',
  `applied_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `handled_at` timestamp NULL DEFAULT NULL,
  `handled_by` int DEFAULT NULL,
  `reason` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`,`user_id`,`status`),
  KEY `fk_pma_user` (`user_id`),
  KEY `fk_pma_handler` (`handled_by`),
  CONSTRAINT `fk_pma_handler` FOREIGN KEY (`handled_by`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_pma_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pma_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_member_application`
--

LOCK TABLES `project_member_application` WRITE;
/*!40000 ALTER TABLE `project_member_application` DISABLE KEYS */;
INSERT INTO `project_member_application` VALUES (1,5,3,'CONFIRMED','2026-02-23 13:00:07','2026-02-23 13:06:42',2,'我想加入这个项目学习'),(2,5,22,'CONFIRMED','2026-02-23 13:28:27','2026-02-23 13:29:29',2,'测试申请加入项目'),(3,5,23,'REJECTED','2026-02-23 13:30:04','2026-02-23 13:30:12',2,NULL),(4,5,2,'pending','2026-02-23 13:32:14',NULL,NULL,'测试我的申请记录页面'),(5,5,23,'pending','2026-02-23 13:36:36',NULL,NULL,'再次申请测试');
/*!40000 ALTER TABLE `project_member_application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_plan`
--

DROP TABLE IF EXISTS `project_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_plan` (
  `id` int NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `order_index` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_pp_project` (`project_id`),
  CONSTRAINT `fk_pp_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_plan`
--

LOCK TABLES `project_plan` WRITE;
/*!40000 ALTER TABLE `project_plan` DISABLE KEYS */;
INSERT INTO `project_plan` VALUES (1,5,'��һ�׶ο���','��ɻ�������','2026-03-01','2026-06-30',0,'2026-02-23 13:09:05','2026-02-23 13:09:05'),(2,5,'Phase1Dev','DevelopmentPhase','2026-03-01','2026-06-30',0,'2026-02-23 13:38:49','2026-02-23 13:38:49');
/*!40000 ALTER TABLE `project_plan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_progress`
--

DROP TABLE IF EXISTS `project_progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_progress` (
  `id` int NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `plan_id` int DEFAULT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `completion_rate` int DEFAULT '0' COMMENT '0-100',
  `created_by` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_pg_project` (`project_id`),
  KEY `fk_pg_plan` (`plan_id`),
  KEY `fk_pg_creator` (`created_by`),
  CONSTRAINT `fk_pg_creator` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pg_plan` FOREIGN KEY (`plan_id`) REFERENCES `project_plan` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_pg_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_progress`
--

LOCK TABLES `project_progress` WRITE;
/*!40000 ALTER TABLE `project_progress` DISABLE KEYS */;
INSERT INTO `project_progress` VALUES (1,5,1,'����������','�����������ĵ�',0,2,'2026-02-23 13:09:43','2026-02-23 13:09:43'),(2,5,2,'DesignComplete','DesignPhaseDone',50,2,'2026-02-23 13:39:05','2026-02-23 13:39:05');
/*!40000 ALTER TABLE `project_progress` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recruit_application`
--

DROP TABLE IF EXISTS `recruit_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recruit_application` (
  `id` int NOT NULL AUTO_INCREMENT,
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
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recruit_application`
--

LOCK TABLES `recruit_application` WRITE;
/*!40000 ALTER TABLE `recruit_application` DISABLE KEYS */;
INSERT INTO `recruit_application` VALUES (1,'lisi ','12445235','2346','2362346','18725533292','dfsgs_1@hsu.edu.cn','dsfg',2,'2026-01-12 14:26:16'),(2,'zhangsan ','12445','2346','2362346','18725533292','dfsgs@hsu.edu.cn','sdfa',2,'2026-01-12 15:12:41'),(3,'����ͬѧ','202402152143','�������','2024','13800138000','test202402152143@example.com','�Ȱ���̣�ϣ���������С��',2,'2026-02-15 21:43:26'),(4,'����ͬѧ','TEST1771163081','�������','2024','13800138000','testTEST1771163081@test.com','�Ȱ����',2,'2026-02-15 21:44:41'),(5,'测试已有','member1','软件工程','2024','13900000000','test@test.com','测试',2,'2026-02-15 21:47:33'),(6,'��ͬѧ','NEW1771163367','�������ѧ','2025','13900000001','newNEW1771163367@test.com','�Ȱ��������',1,'2026-02-15 21:49:27'),(7,'���ղ���','FINAL1771164264','�����','2025','13999999999','final@test.com','����',1,'2026-02-15 22:04:25'),(8,'·������','PATH1771164308','����','2025','13800000000','path@test.com','����',1,'2026-02-15 22:05:08'),(9,'�޸�����','TESTFIX1771164566','�������','2025','13800000000','testfix@test.com','�����޸�',1,'2026-02-15 22:09:27'),(10,'�ܾ�����','REJECTTEST1771164573','�����','2025','13900000000','reject@test.com','���Ծܾ�',2,'2026-02-15 22:09:35'),(11,'POST����','POSTTEST1771164688','�������','2025','13700000000','post@test.com','����',0,'2026-02-15 22:11:29'),(12,'���ղ���2','FINAL21771164883','�������','2025','13600000000','final2@test.com','���ղ���',2,'2026-02-15 22:14:43'),(13,'','123456','CS','2025','13800000000','test@test.com','test',1,'2026-02-19 22:04:49'),(14,'','123456','CS','2025','13800000000','test@test.com','test',1,'2026-02-19 22:05:00'),(15,'','123456','CS','2025','13800000000','test@test.com','test',1,'2026-02-19 22:05:08'),(16,'','123456','CS','2025','13800000000','test@test.com','test',1,'2026-02-19 22:05:43'),(17,'','123456','CS','2025','13800000000','test@test.com','test',1,'2026-02-19 22:05:58'),(18,'','123456','CS','2025','13800000000','test123456@test.com','test',1,'2026-02-19 22:06:38'),(19,'','123456','CS','2025','13800000000','test123456@test.com','test',1,'2026-02-19 22:06:47'),(20,'','123456','CS','2025','13800000000','test654321@test.com','test',1,'2026-02-19 22:07:57'),(21,'','TEST001','CS','2025','13800000000','test999@test.com','test',1,'2026-02-19 22:08:34'),(22,'','TEST002','CS','2025','13800000000','test002@test.com','test',1,'2026-02-19 22:09:35'),(23,'','TEST002','CS','2025','13800000000','test002@test.com','test',1,'2026-02-19 22:09:47'),(24,'','TEST002','CS','2025','13800000000','test002@test.com','test',1,'2026-02-19 22:09:56'),(25,'','TEST003','CS','2025','13800000000','test003@test.com','test',1,'2026-02-19 22:10:42'),(26,'','TEST004','CS','2025','13800000000','test004@test.com','test',1,'2026-02-19 22:11:47'),(27,'康阿辉','22406032001','计算机科学与技术','24级','18297382246','3508008752@QQ.COM','我已是该小组的老油条，一直没加入这个网页，现在申请加入网页',2,'2026-03-10 21:31:55'),(28,'刘鸣燕','22506031033','计算机科学与技术','大一','18055196731','','兴趣',2,'2026-03-10 21:45:27');
/*!40000 ALTER TABLE `recruit_application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resume_awards`
--

DROP TABLE IF EXISTS `resume_awards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resume_awards` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `resume_id` int NOT NULL COMMENT '简历ID（关联resumes表）',
  `award_id` int DEFAULT NULL,
  `award_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '奖项名称',
  `competition_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '比赛/活动名称',
  `award_level` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '奖项等级：特等奖/一等奖/二等奖/三等奖/优秀奖等',
  `award_date` date DEFAULT NULL COMMENT '获奖时间',
  `award_org` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '颁奖机构',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '获奖描述',
  `is_from_system` tinyint DEFAULT '0' COMMENT '是否来自系统奖项：0-手动添加，1-关联系统奖项',
  `display_order` int DEFAULT '0' COMMENT '显示顺序',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`),
  KEY `idx_award_id` (`award_id`),
  KEY `idx_display_order` (`display_order`),
  CONSTRAINT `resume_awards_ibfk_1` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `resume_awards_ibfk_2` FOREIGN KEY (`award_id`) REFERENCES `award` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历-获奖情况关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resume_awards`
--

LOCK TABLES `resume_awards` WRITE;
/*!40000 ALTER TABLE `resume_awards` DISABLE KEYS */;
INSERT INTO `resume_awards` VALUES (8,5,NULL,'蓝桥杯省一','蓝桥杯',NULL,'2026-03-20','黄山学院','',0,1,'2026-03-17 02:53:27');
/*!40000 ALTER TABLE `resume_awards` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resume_educations`
--

DROP TABLE IF EXISTS `resume_educations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resume_educations` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '教育经历ID',
  `resume_id` int NOT NULL COMMENT '简历ID（关联resumes表）',
  `school_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学校名称',
  `major` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '专业名称',
  `degree` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学历/学位：高中/大专/本科/硕士/博士',
  `start_date` date DEFAULT NULL COMMENT '入学时间',
  `end_date` date DEFAULT NULL COMMENT '毕业时间',
  `is_current` tinyint DEFAULT '0' COMMENT '是否在读：0-已毕业，1-在读',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '在校经历描述（主修课程、GPA、获奖情况等）',
  `display_order` int DEFAULT '0' COMMENT '显示顺序',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`),
  KEY `idx_display_order` (`display_order`),
  CONSTRAINT `resume_educations_ibfk_1` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历-教育经历表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resume_educations`
--

LOCK TABLES `resume_educations` WRITE;
/*!40000 ALTER TABLE `resume_educations` DISABLE KEYS */;
INSERT INTO `resume_educations` VALUES (2,5,'黄山学院','计算机科学与技术','本科','2026-03-13',NULL,1,'',0,'2026-03-17 01:55:07','2026-03-17 01:55:07');
/*!40000 ALTER TABLE `resume_educations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resume_projects`
--

DROP TABLE IF EXISTS `resume_projects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resume_projects` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `resume_id` int NOT NULL COMMENT '简历ID（关联resumes表）',
  `project_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  `role` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '担任角色：项目负责人/核心开发/参与开发等',
  `team_size` int DEFAULT NULL COMMENT '团队规模',
  `start_date` date DEFAULT NULL COMMENT '开始时间',
  `end_date` date DEFAULT NULL COMMENT '结束时间',
  `is_current` tinyint DEFAULT '0' COMMENT '是否进行中：0-已完成，1-进行中',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '项目描述',
  `responsibilities` text COLLATE utf8mb4_unicode_ci COMMENT '个人职责',
  `technologies` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '使用技术（逗号分隔）',
  `project_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '项目链接/演示地址',
  `achievements` text COLLATE utf8mb4_unicode_ci COMMENT '项目成果',
  `display_order` int DEFAULT '0' COMMENT '显示顺序',
  `is_from_system` tinyint DEFAULT '0' COMMENT '是否来自系统项目模块：0-手动添加，1-关联系统项目',
  `system_project_id` int DEFAULT NULL COMMENT '关联的系统项目ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`),
  KEY `idx_is_current` (`is_current`),
  KEY `idx_display_order` (`display_order`),
  CONSTRAINT `resume_projects_ibfk_1` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历-项目经历表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resume_projects`
--

LOCK TABLES `resume_projects` WRITE;
/*!40000 ALTER TABLE `resume_projects` DISABLE KEYS */;
INSERT INTO `resume_projects` VALUES (2,5,'电商','负责人',4,'2026-03-13','2026-03-15',0,'','','Java','http:/afsd','',0,0,NULL,'2026-03-17 01:24:31','2026-03-17 01:24:31');
/*!40000 ALTER TABLE `resume_projects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resume_skills`
--

DROP TABLE IF EXISTS `resume_skills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resume_skills` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '技能ID',
  `resume_id` int NOT NULL COMMENT '简历ID（关联resumes表）',
  `skill_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能名称',
  `proficiency` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'intermediate' COMMENT '熟练程度：beginner/elementary/intermediate/advanced/expert',
  `proficiency_score` int DEFAULT NULL COMMENT '熟练度分数：1-100',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '技能分类：编程语言/开发框架/数据库/工具/语言/其他',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '技能描述',
  `display_order` int DEFAULT '0' COMMENT '显示顺序',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`),
  KEY `idx_category` (`category`),
  KEY `idx_display_order` (`display_order`),
  CONSTRAINT `resume_skills_ibfk_1` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历-技能特长表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resume_skills`
--

LOCK TABLES `resume_skills` WRITE;
/*!40000 ALTER TABLE `resume_skills` DISABLE KEYS */;
INSERT INTO `resume_skills` VALUES (1,5,'java','intermediate',37,'编程语言','asfa',1,'2026-03-17 08:01:22','2026-03-17 08:01:22');
/*!40000 ALTER TABLE `resume_skills` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resume_submissions`
--

DROP TABLE IF EXISTS `resume_submissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resume_submissions` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '投递记录ID',
  `resume_id` int NOT NULL COMMENT '简历ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `target_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '投递对象类型：activity-活动/项目，recruit-招新，job-职位',
  `target_id` int DEFAULT NULL COMMENT '投递对象ID',
  `target_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '投递对象名称',
  `status` tinyint DEFAULT '0' COMMENT '投递状态：0-已投递，1-已查看，2-已通过，3-未通过',
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
-- Dumping data for table `resume_submissions`
--

LOCK TABLES `resume_submissions` WRITE;
/*!40000 ALTER TABLE `resume_submissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `resume_submissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resumes`
--

DROP TABLE IF EXISTS `resumes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resumes` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '简历ID',
  `user_id` int NOT NULL COMMENT '用户ID（关联users表）',
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
  `is_default` tinyint DEFAULT '0' COMMENT '是否为默认简历：0-否，1-是',
  `status` tinyint DEFAULT '1' COMMENT '简历状态：0-草稿，1-已发布，2-已隐藏',
  `view_count` int DEFAULT '0' COMMENT '浏览次数',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '软删除标志：0-正常，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_default` (`is_default`),
  CONSTRAINT `resumes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resumes`
--

LOCK TABLES `resumes` WRITE;
/*!40000 ALTER TABLE `resumes` DISABLE KEYS */;
INSERT INTO `resumes` VALUES (1,27,'Java开发工程师简历','creative','我是一个非常伟大的Java开发工程师','','18297382246','3508008752@QQ.COM','kang2005820','','',NULL,1,1,0,'2026-03-12 07:14:37','2026-03-12 13:30:24',0),(2,27,'Java开发工程师简历','default','非常牛逼的Java开发工程人员','java开发工程师','18297382246','3508008752@QQ.COM','kang2005820','http:/git','http:/puoke',NULL,1,1,0,'2026-03-12 07:23:21','2026-03-12 13:30:24',0),(3,27,'Java开发工程师简历','default','我是非常牛逼的Java开发人员','java开发工程师','18297382246','3508008752@QQ.COM','kang2005820','http:/git','http:/puoke',NULL,1,1,0,'2026-03-12 11:02:11','2026-03-12 13:33:10',1),(4,27,'Java开发工程师简历','default','asda','java开发工程师','18297382246','3508008752@QQ.COM','kang2005820','http:/git','http:/uoke',NULL,1,1,0,'2026-03-12 11:04:56','2026-03-12 13:30:24',0),(5,27,'Java开发工程师简历','default','asdfas','java开发工程师','18297382246','3508008752@QQ.COM','kang2005820','http:/git','http:/puoke',NULL,1,1,0,'2026-03-12 11:18:07','2026-03-16 14:10:20',0),(7,27,'Java开发工程师','default','啊手动阀','java开发工程师','18297382246','3508008752@QQ.COM','kang2005820','http:/git','http:/puoke',NULL,0,1,0,'2026-03-12 12:06:38','2026-03-12 13:30:24',0),(8,27,'C++游戏开发时','default','','java开发工程师','18297382246','3508008752@QQ.COM','kang2005820','http:/git','http:/puoke',NULL,0,1,0,'2026-03-17 12:50:26','2026-03-17 12:50:26',0);
/*!40000 ALTER TABLE `resumes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `study_session`
--

DROP TABLE IF EXISTS `study_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `study_session` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '学习会话ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `session_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话名称',
  `subject` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学习科目',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` int DEFAULT NULL COMMENT '学习时长（分钟）',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '学习内容',
  `notes` text COLLATE utf8mb4_unicode_ci COMMENT '笔记',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ongoing' COMMENT '状态：ongoing/completed/canceled',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习会话表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `study_session`
--

LOCK TABLES `study_session` WRITE;
/*!40000 ALTER TABLE `study_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `study_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Unknown',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` enum('ADMIN','MEMBER','TEACHER','GUEST') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MEMBER',
  `status` int DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','RerBXm1xrJqSBMIE9v69ZQ==','张三2','admin@hsu.com2','13800138032','ADMIN',1,'2025-12-21 14:17:28','2026-02-18 01:43:20'),(2,'member1','Bkj4xIPxGT0=','NewTest','newtest@test.com','222222222','MEMBER',1,'2025-12-21 14:17:28','2026-02-23 07:31:10'),(3,'12445','qlkkHyFnxfg=','测试用户2','dfsgs@hsu.edu.cn','18725533292','MEMBER',1,'2026-01-21 07:01:10','2026-02-21 11:05:37'),(22,'12445235','qlkkHyFnxfg=','Unknown','dfsgs_1@hsu.edu.cn','18725533292','MEMBER',1,'2026-01-21 08:40:42','2026-01-21 08:40:42'),(23,'testuser123','test','测试用户','test@test.com','13900000000','MEMBER',1,'2026-02-15 13:58:58','2026-02-15 13:58:58'),(24,'FINAL21771164883','qlkkHyFnxfg=','���ղ���2','final2@test.com','13600000000','MEMBER',1,'2026-02-18 01:36:59','2026-02-18 01:36:59'),(25,'REJECTTEST1771164573','qlkkHyFnxfg=','�ܾ�����','reject@test.com','13900000000','MEMBER',1,'2026-02-18 01:48:09','2026-02-18 01:48:09'),(26,'TEST1771163081','qlkkHyFnxfg=','����ͬѧ','testTEST1771163081@test.com','13800138000','MEMBER',0,'2026-02-18 08:16:37','2026-02-23 07:04:49'),(27,'22406032001','RerBXm1xrJqSBMIE9v69ZQ==','康阿辉','3508008752@QQ.COM','18297382246','MEMBER',1,'2026-03-10 13:32:36','2026-03-20 06:04:22'),(28,'22506031033','qlkkHyFnxfg=','刘鸣燕','','18055196731','MEMBER',1,'2026-03-10 13:45:53','2026-03-10 13:45:53');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-26 17:47:18

--
-- Table structure for table `problem_report`
--

DROP TABLE IF EXISTS `problem_report`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `problem_report` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  `title` varchar(200) NOT NULL COMMENT '问题标题',
  `content` text NOT NULL COMMENT '问题描述',
  `reporter_name` varchar(100) DEFAULT NULL COMMENT '报告者姓名（游客可填）',
  `reporter_contact` varchar(100) DEFAULT NULL COMMENT '联系方式（游客可填）',
  `reporter_type` enum('GUEST','MEMBER','ADMIN') NOT NULL DEFAULT 'GUEST' COMMENT '报告者类型',
  `user_id` int DEFAULT NULL COMMENT '关联用户ID（如果是成员/管理员）',
  `category` enum('VERIFIED','UNVERIFIED','INVALID') NOT NULL DEFAULT 'UNVERIFIED' COMMENT '问题分类：VERIFIED-属实，UNVERIFIED-待验证，INVALID-不属实',
  `status` enum('PENDING','SOLVING','SOLVED','UNSOLVED') DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理，SOLVING-正在解决，SOLVED-已解决，UNSOLVED-未解决（仅属实问题有状态）',
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
/*!40101 SET character_set_client = @saved_cs_client */;

-- ============================================
-- 2. 群聊功能
-- ============================================
-- 群聊功能数据库表
-- 执行前请先备份数据库
-- 请在执行前选中正确的数据库

-- ============================================
-- 1. 活动群表
-- ============================================
CREATE TABLE IF NOT EXISTS activity_group (
    id INT NOT NULL AUTO_INCREMENT,
    activity_id INT DEFAULT NULL COMMENT '关联的活动ID',
    group_name VARCHAR(100) NOT NULL COMMENT '群名称',
    group_owner_id INT NOT NULL COMMENT '群主用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_activity_id (activity_id),
    KEY idx_group_owner_id (group_owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 2. 群成员表
-- ============================================
CREATE TABLE IF NOT EXISTS group_member (
    id INT NOT NULL AUTO_INCREMENT,
    group_id INT NOT NULL COMMENT '群ID',
    user_id INT NOT NULL COMMENT '用户ID',
    role ENUM('OWNER', 'MEMBER') DEFAULT 'MEMBER' COMMENT '角色：群主/成员',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_user (group_id, user_id),
    KEY idx_user_id (user_id),
    KEY idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 3. 站内消息表
-- ============================================
CREATE TABLE IF NOT EXISTS group_message (
    id INT NOT NULL AUTO_INCREMENT,
    group_id INT NOT NULL COMMENT '群ID',
    sender_id INT NOT NULL COMMENT '发送者ID',
    content TEXT NOT NULL COMMENT '消息内容',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_group_id (group_id),
    KEY idx_sender_id (sender_id),
    KEY idx_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 4. 用户_群关系表（用户参与的群列表）
-- ============================================
CREATE TABLE IF NOT EXISTS user_group (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT '用户ID',
    group_id INT NOT NULL COMMENT '群ID',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_group (user_id, group_id),
    KEY idx_user_id (user_id),
    KEY idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 外键约束（如果需要可以取消注释）
-- ============================================
-- ALTER TABLE activity_group 
-- ADD CONSTRAINT fk_group_activity FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE SET NULL;
-- 
-- ALTER TABLE activity_group 
-- ADD CONSTRAINT fk_group_owner FOREIGN KEY (group_owner_id) REFERENCES user(id);
-- 
-- ALTER TABLE group_member 
-- ADD CONSTRAINT fk_member_group FOREIGN KEY (group_id) REFERENCES activity_group(id) ON DELETE CASCADE;
-- 
-- ALTER TABLE group_member 
-- ADD CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;
-- 
-- ALTER TABLE group_message 
-- ADD CONSTRAINT fk_message_group FOREIGN KEY (group_id) REFERENCES activity_group(id) ON DELETE CASCADE;
-- 
-- ALTER TABLE group_message 
-- ADD CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES user(id) ON DELETE CASCADE;
-- 
-- ALTER TABLE user_group 
-- ADD CONSTRAINT fk_ug_group FOREIGN KEY (group_id) REFERENCES activity_group(id) ON DELETE CASCADE;
-- 
-- ALTER TABLE user_group 
-- ADD CONSTRAINT fk_ug_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;

-- 查看表结构
SHOW TABLES;
DESCRIBE activity_group;
DESCRIBE group_member;
DESCRIBE group_message;
DESCRIBE user_group;

-- ============================================
-- 3. AI助手功能
-- ============================================
-- ============================================
-- AI助手功能数据库脚本
-- 高校软件小组管理系统
-- ============================================

-- 创建AI对话日志表（原有表）
CREATE TABLE IF NOT EXISTS `ai_conversation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `session_id` VARCHAR(64) DEFAULT NULL COMMENT '会话ID',
    `user_id` INT DEFAULT NULL COMMENT '用户ID',
    `user_role` VARCHAR(20) DEFAULT NULL COMMENT '用户角色',
    `question` TEXT NOT NULL COMMENT '用户提问',
    `ai_answer` TEXT COMMENT 'AI回答',
    `source` VARCHAR(20) DEFAULT NULL COMMENT '回答来源：faq/llm',
    `reference_id` INT DEFAULT NULL COMMENT '关联FAQ ID',
    `rating` TINYINT DEFAULT NULL COMMENT '用户评分1-5',
    `is_validated` TINYINT DEFAULT '0' COMMENT '是否已验证：1-是，0-否',
    `validated_by` INT DEFAULT NULL COMMENT '验证人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_user_role` (`user_role`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_source` (`source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话日志表';

-- 创建FAQ知识表（原有表）
CREATE TABLE IF NOT EXISTS `ai_faq_knowledge` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT 'FAQ ID',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '问题分类',
    `question` VARCHAR(500) NOT NULL COMMENT '问题',
    `answer` TEXT NOT NULL COMMENT '答案',
    `keywords` VARCHAR(255) DEFAULT NULL COMMENT '关键词，用于匹配',
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
    INDEX `idx_category` (`category`),
    INDEX `idx_target_role` (`target_role`),
    INDEX `idx_status` (`status`),
    INDEX `idx_verified` (`verified`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FAQ知识表';

-- 创建AI对话记录表
CREATE TABLE IF NOT EXISTS `ai_conversation` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `user_id` INT DEFAULT 0 COMMENT '用户ID，0表示游客',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID，唯一标识',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话记录表';

-- 创建AI消息表
CREATE TABLE IF NOT EXISTS `ai_message` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `conversation_id` INT NOT NULL COMMENT '会话ID，外键',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：user/assistant/system',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_conversation_id` (`conversation_id`),
    FOREIGN KEY (`conversation_id`) REFERENCES `ai_conversation`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI消息表';

-- 创建AI知识库表
CREATE TABLE IF NOT EXISTS `ai_knowledge_base` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类，如：奖项申报、活动报名',
    `question` VARCHAR(500) NOT NULL COMMENT '问题',
    `answer` TEXT NOT NULL COMMENT '答案',
    `keywords` VARCHAR(255) DEFAULT NULL COMMENT '关键词，用于匹配',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_category` (`category`),
    INDEX `idx_keywords` (`keywords`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI知识库表';

-- 创建AI提问统计表
CREATE TABLE IF NOT EXISTS `ai_faq_statistics` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `question_hash` VARCHAR(64) UNIQUE COMMENT '问题哈希值',
    `normalized_question` VARCHAR(500) COMMENT '标准化问题',
    `query_count` INT DEFAULT 1 COMMENT '查询次数',
    `avg_rating` DECIMAL(3,2) DEFAULT NULL COMMENT '平均评分',
    `last_query_at` DATETIME DEFAULT NULL COMMENT '最后查询时间',
    `suggested_faq` TINYINT DEFAULT 0 COMMENT '是否建议FAQ：1-是，0-否',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_query_count` (`query_count`),
    INDEX `idx_suggested` (`suggested_faq`),
    INDEX `idx_last_query` (`last_query_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI提问统计表';

-- ============================================
-- 初始化知识库数据
-- ============================================

INSERT INTO `ai_knowledge_base` (`category`, `question`, `answer`, `keywords`, `status`) VALUES

-- ==================== 游客可用功能 ====================
('游客指南', '如何登录系统？', '访问系统首页，点击右上角"登录"按钮，输入学号和密码即可登录。新成员需要管理员审核通过后才能获得账号。', '登录,登录系统,如何登录', 1),
('游客指南', '如何注册账号？', '系统不开放自主注册。新成员需联系管理员进行招新报名，由管理员审核通过后自动创建账号。', '注册,注册账号,新成员', 1),
('游客指南', '可以浏览哪些信息？', '游客可以浏览首页新闻列表、查看活动信息、浏览获奖新闻等公开内容。', '浏览,查看新闻,查看活动', 1),
('游客指南', '如何提交问题反馈？', '点击导航栏"问题反馈"，填写问题标题和详细内容后提交。管理员审核后会更新处理状态。', '问题反馈,提交问题,反馈问题', 1),
('游客指南', '如何报名参加活动？', '首先需要登录系统（联系管理员创建账号），然后访问活动列表，选择活动后点击"报名"按钮。', '报名活动,参加活动', 1),

-- ==================== 成员-个人信息 ====================
('成员-个人信息', '如何修改个人资料？', '登录后点击右上角头像进入"个人中心"，点击"编辑资料"按钮进行修改，保存后生效。', '修改资料,编辑资料,个人信息', 1),
('成员-个人信息', '如何修改登录密码？', '登录后点击右上角头像进入"个人中心"，点击"修改密码"，输入原密码和新密码后确认。', '修改密码,更改密码', 1),
('成员-个人信息', '个人中心有哪些功能？', '个人中心提供：个人信息查看/编辑、项目管理、活动管理、奖项管理、简历管理、学习记录查看等功能入口。', '个人中心,我的', 1),

-- ==================== 成员-活动 ====================
('成员-活动', '如何报名参加活动？', '点击导航"活动"，在活动列表中选择想参加的活动，点击"报名"按钮确认报名（需在报名时间内）。', '报名活动,参加活动,活动报名', 1),
('成员-活动', '如何查看我报名的活动？', '登录后进入"个人中心"，点击"我的活动"查看所有报名记录和处理状态。', '我的活动,查看报名', 1),
('成员-活动', '如何取消已报名的活动？', '在"我的活动"页面，找到要取消的活动，点击"取消报名"按钮。需在报名截止前操作。', '取消报名,取消活动', 1),

-- ==================== 成员-项目 ====================
('成员-项目', '如何创建新项目？', '进入"我的项目"页面，点击"创建项目"按钮，填写项目名称、描述、团队成员等信息后提交，等待管理员审核。', '创建项目,新建项目,项目创建', 1),
('成员-项目', '如何查看我的项目申请记录？', '登录后进入"个人中心"，点击"我的项目"查看所有项目及其审核状态。', '我的项目,项目记录', 1),
('成员-项目', '项目申请被拒绝后可以重新申请吗？', '可以，修改项目信息后可以重新提交申请。', '项目被拒,重新申请', 1),

-- ==================== 成员-奖项 ====================
('成员-奖项', '如何提交奖项申请？', '进入"我的奖项"页面，点击"提交奖项"按钮，填写奖项名称、获奖时间等信息，上传证书图片，提交后等待审核。', '提交奖项,申报奖项,奖项申报', 1),
('成员-奖项', '如何查看我的获奖记录？', '登录后进入"个人中心"，点击"我的奖项"查看所有获奖记录及审核状态。', '我的奖项,获奖记录', 1),
('成员-奖项', '奖项申请被拒绝后怎么办？', '可以在"我的奖项"页面重新编辑信息后再次提交申请。', '奖项被拒,重新申请', 1),

-- ==================== 成员-学习 ====================
('成员-学习', '如何开始学习签到？', '点击导航"学习中心"，在规定时间（6:00-22:00）内点击"开始学习"按钮进行签到。', '开始学习,签到,学习签到', 1),
('成员-学习', '学习时间有什么规则？', '学习时间为6:00-22:00。18:00前签到为正常，18:00-19:00为迟到，19:00后为严重迟到。22:00系统自动结束学习。', '学习规则,签到规则', 1),
('成员-学习', '如何签退结束学习？', '在学习中心点击"签退"按钮结束学习。21:30前签退为正常早退，21:30后为正常签退。', '签退,结束学习', 1),
('成员-学习', '忘记了签到怎么办？', '请在下次学习时注意签到时间，系统会自动记录。目前不支持补签。', '忘记签到,补签', 1),

-- ==================== 成员-问题反馈 ====================
('成员-问题反馈', '如何提交问题反馈？', '点击导航"问题反馈"，填写问题标题和详细描述后提交。管理员处理后可在列表查看状态。', '提交反馈,问题反馈,提交问题', 1),
('成员-问题反馈', '如何查看我提交的问题？', '在问题反馈页面可以看到所有历史反馈记录及处理状态（待确认/属实/不属实）。', '查看反馈,我的反馈', 1),

-- ==================== 管理员-新闻管理 ====================
('管理员-新闻管理', '如何发布新闻？', '点击"新闻管理"，点击"发布新闻"按钮，选择新闻类型（通知/获奖/活动），填写标题和内容，设置发布状态后保存。', '发布新闻,创建新闻', 1),
('管理员-新闻管理', '如何修改已发布的新闻？', '在新闻列表找到目标新闻，点击"编辑"按钮修改内容后保存。', '修改新闻,编辑新闻', 1),
('管理员-新闻管理', '如何删除新闻？', '在新闻列表找到目标新闻，点击"删除"按钮确认删除。注意：已关联活动的新闻无法删除。', '删除新闻', 1),

-- ==================== 管理员-活动管理 ====================
('管理员-活动管理', '如何创建新活动？', '点击"活动管理"，点击"创建活动"，填写活动名称、时间、地点，设置报名起止时间后保存。', '创建活动,新建活动', 1),
('管理员-活动管理', '如何查看活动报名情况？', '在活动列表点击"报名管理"按钮，可查看所有报名成员及审核状态。', '查看报名,报名情况', 1),
('管理员-活动管理', '如何删除活动？', '在活动列表找到目标活动，点击"删除"按钮。注意：进行中或已结束的活动无法删除。', '删除活动', 1),

-- ==================== 管理员-项目管理 ====================
('管理员-项目管理', '如何审核项目申请？', '在项目管理页面查看"待审核"状态的项目，点击"批准"通过或"编辑"查看详情后处理。', '审核项目,审批项目', 1),
('管理员-项目管理', '项目状态有哪几种？', '项目状态分为：待审核（黄色）、已通过（绿色）、已拒绝（红色）三种。', '项目状态', 1),

-- ==================== 管理员-奖项管理 ====================
('管理员-奖项管理', '如何审核奖项申请？', '在奖项管理页面查看待审核申请，点击"通过"或"拒绝"按钮处理。', '审核奖项,审批奖项', 1),
('管理员-奖项管理', '奖项状态有哪几种？', '奖项状态分为：待审核、已通过、已拒绝三种。', '奖项状态', 1),

-- ==================== 管理员-成员管理 ====================
('管理员-成员管理', '如何重置成员密码？', '在成员管理页面找到目标成员，点击"重置密码"按钮。系统会将密码重置为默认密码123456。', '重置密码,忘记密码', 1),

-- ==================== 管理员-招新管理 ====================
('管理员-招新管理', '如何处理招新报名？', '点击"招新管理"，查看待处理申请，点击"同意"为申请者创建账号（学号即用户名，默认密码123456），或点击"拒绝"驳回。', '处理报名,审核报名', 1),

-- ==================== 管理员-学习管理 ====================
('管理员-学习管理', '如何查看学习记录？', '点击"学习管理"，可按日期范围和成员筛选查看学习时段记录，包括开始时间、结束时间、学习时长等。', '查看学习,学习记录', 1),
('管理员-学习管理', '可以统计学习数据吗？', '可以，学习管理页面顶部显示总学习次数、已完成次数、总时长、平均时长等统计信息。', '学习统计', 1),

-- ==================== 管理员-问题管理 ====================
('管理员-问题管理', '如何处理问题反馈？', '点击"问题管理"，以三个标签页查看（属实/不属实/待确认），点击"查看"可更新分类、状态和处理备注。', '处理问题,审核问题', 1),
('管理员-问题管理', '如何删除问题反馈？', '在问题列表点击"删除"按钮，可移除不再需要的问题记录。', '删除问题', 1),

-- ==================== 账号通用 ====================
('账号问题', '忘记密码怎么办？', '联系管理员重置密码，重置后默认密码为123456，首次登录后请立即修改。', '忘记密码,密码找回', 1),
('账号问题', '默认登录密码是什么？', '新账号默认密码为123456，登录后请立即修改密码。', '默认密码', 1),
('账号问题', '账户被锁定怎么办？', '联系管理员解锁账户。', '账户锁定,账号锁定,解锁', 1),

-- ==================== 学习规则 ====================
('学习规则', '学习时间段是什么？', '系统学习时间为每日6:00-22:00。18:00系统会强制签退（如正在学习中）。', '学习时间,时段', 1),
('学习规则', '签到状态如何判定？', '6:00-18:00签到为正常；18:00-19:00为迟到；19:00之后为严重迟到。', '签到状态', 1),
('学习规则', '签退状态如何判定？', '21:30前签退为早退；21:30后签退为正常；22:00系统自动结束学习。', '签退状态', 1);

-- ============================================
-- 脚本执行完成
-- ============================================
-- 使用说明：
-- 1. 确保您使用的是MySQL 5.7+或MySQL 8.0+
-- 2. 执行此脚本前请备份数据库
-- 3. 执行：source path/to/ai_assistant.sql
-- 或：mysql -u root -p software_group < ai_assistant.sql
-- ============================================
-- ============================================
-- 4. 签到功能
-- ============================================
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

-- ============================================
-- 5. 问题反馈
-- ============================================
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
-- ============================================
-- 6. 问题管理
-- ============================================
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

-- ============================================
-- 7. 学习时段
-- ============================================
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

-- ============================================
-- 8. 群聊v2功能
-- ============================================
-- ============================================
-- 黄山学院软件小组管理系统 - 群聊功能数据库脚本
-- 执行前请先备份数据库
-- ============================================

-- 请在执行前选中正确的数据库：
-- USE software_group;

-- ============================================
-- 第一部分：活动表迁移（添加 creator_id 字段）
-- ============================================

-- 1. 检查并添加活动创建者字段
-- 注意：如果字段已存在，请手动删除或跳过此步
-- ALTER TABLE activity ADD COLUMN creator_id INT DEFAULT NULL COMMENT '活动创建者用户ID' AFTER approval_status;

-- ============================================
-- 第二部分：群聊功能数据表
-- ============================================

-- 2. 活动群表
CREATE TABLE IF NOT EXISTS activity_group (
    id INT NOT NULL AUTO_INCREMENT,
    activity_id INT DEFAULT NULL COMMENT '关联的活动ID',
    group_name VARCHAR(100) NOT NULL COMMENT '群名称',
    group_owner_id INT NOT NULL COMMENT '群主用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_activity_id (activity_id),
    KEY idx_group_owner_id (group_owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 群成员表
CREATE TABLE IF NOT EXISTS group_member (
    id INT NOT NULL AUTO_INCREMENT,
    group_id INT NOT NULL COMMENT '群ID',
    user_id INT NOT NULL COMMENT '用户ID',
    role ENUM('OWNER', 'MEMBER') DEFAULT 'MEMBER' COMMENT '角色：群主/成员',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_user (group_id, user_id),
    KEY idx_user_id (user_id),
    KEY idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 群消息表
CREATE TABLE IF NOT EXISTS group_message (
    id INT NOT NULL AUTO_INCREMENT,
    group_id INT NOT NULL COMMENT '群ID',
    sender_id INT NOT NULL COMMENT '发送者ID',
    content TEXT NOT NULL COMMENT '消息内容',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_group_id (group_id),
    KEY idx_sender_id (sender_id),
    KEY idx_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 用户_群关系表
CREATE TABLE IF NOT EXISTS user_group (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT '用户ID',
    group_id INT NOT NULL COMMENT '群ID',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_group (user_id, group_id),
    KEY idx_user_id (user_id),
    KEY idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 第三部分：查看表结构（可选）
-- ============================================

SHOW TABLES;
DESCRIBE activity_group;
DESCRIBE group_member;
DESCRIBE group_message;
DESCRIBE user_group;

-- ============================================
-- 9. 活动群聊迁移
-- ============================================
-- 活动群聊功能：添加活动创建者字段
-- 执行前请先备份数据库
-- 请确保已选中正确的数据库

-- 1. 添加活动创建者字段
ALTER TABLE activity 
ADD COLUMN creator_id INT DEFAULT NULL COMMENT '活动创建者用户ID' AFTER approval_status;

-- 添加索引
ALTER TABLE activity 
ADD INDEX idx_creator_id (creator_id),
ADD CONSTRAINT fk_activity_creator FOREIGN KEY (creator_id) REFERENCES user(id) ON DELETE SET NULL;

-- 2. 查看表结构确认
DESCRIBE activity;
