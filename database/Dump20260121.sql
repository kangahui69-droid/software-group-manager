CREATE DATABASE  IF NOT EXISTS `software_group` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `software_group`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: software_group
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
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
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `max_participants` int DEFAULT NULL,
  `organizer` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('upcoming','ongoing','completed','canceled') COLLATE utf8mb4_unicode_ci DEFAULT 'upcoming',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity`
--

LOCK TABLES `activity` WRITE;
/*!40000 ALTER TABLE `activity` DISABLE KEYS */;
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
  `status` enum('registered','attended','absent') COLLATE utf8mb4_unicode_ci DEFAULT 'registered',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
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
/*!40000 ALTER TABLE `activity_participant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `award`
--

DROP TABLE IF EXISTS `award`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `award` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `level` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '濂栭」绾у埆',
  `competition` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `year` int NOT NULL,
  `result` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `certificate_url` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_cleaned` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `competition_time` date DEFAULT NULL COMMENT '姣旇禌鏃堕棿',
  `competition_location` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '姣旇禌鍦扮偣',
  `competition_level` int DEFAULT NULL,
  `competition_year` int DEFAULT NULL COMMENT '姣旇禌骞村害',
  `competition_session` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '姣旇禌鐣屽埆',
  `award_type` int DEFAULT NULL,
  `award_category` int DEFAULT NULL,
  `award_level` int DEFAULT NULL,
  `team_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍥㈤槦鍚嶇О',
  `award_status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '瀹℃牳鐘舵?',
  `created_by` int DEFAULT NULL COMMENT '鍒涘缓鑰匢D',
  `approved_by` int DEFAULT NULL COMMENT '瀹℃牳浜篒D',
  `approved_at` datetime DEFAULT NULL COMMENT '瀹℃牳鏃堕棿',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remark` text COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `created_by` (`created_by`),
  KEY `approved_by` (`approved_by`),
  KEY `fk_competition_level` (`competition_level`),
  KEY `fk_award_type` (`award_type`),
  KEY `fk_award_category` (`award_category`),
  KEY `fk_award_award_level` (`award_level`),
  CONSTRAINT `award_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `award_ibfk_2` FOREIGN KEY (`approved_by`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_award_award_level` FOREIGN KEY (`award_level`) REFERENCES `dictionary` (`id`),
  CONSTRAINT `fk_award_category` FOREIGN KEY (`award_category`) REFERENCES `dictionary` (`id`),
  CONSTRAINT `fk_award_type` FOREIGN KEY (`award_type`) REFERENCES `dictionary` (`id`),
  CONSTRAINT `fk_competition_level` FOREIGN KEY (`competition_level`) REFERENCES `dictionary` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `award`
--

LOCK TABLES `award` WRITE;
/*!40000 ALTER TABLE `award` DISABLE KEYS */;
INSERT INTO `award` VALUES (9,'22-特啊他','22','特啊他',2026,'22',NULL,0,'2026-01-13 08:23:49','2026-01-02','ga',2,2026,'213',5,8,22,'gasdg ','APPROVED',2,1,'2026-01-20 20:14:56','2026-01-20 20:14:56'),(10,'21-特啊他gasdgads','21','特啊他gasdgads',2026,'21',NULL,0,'2026-01-20 12:15:38','2026-01-01','dsf',2,2026,'12',6,9,21,'asga','REJECTED',2,1,'2026-01-20 20:15:58','2026-01-20 20:15:58');
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
  `award_id` int NOT NULL COMMENT '濂栭」ID',
  `member_id` int DEFAULT NULL COMMENT '鎴愬憳ID(涓汉濂栭」蹇呭～锛屽洟闃熷椤瑰彲閫�)',
  `is_main` tinyint NOT NULL DEFAULT '0' COMMENT '鏄惁涓轰富鍥撅細1-鏄紝0-鍚�',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `file_storage_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_award_id` (`award_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `fk_award_image_file_storage` (`file_storage_id`),
  CONSTRAINT `award_image_ibfk_1` FOREIGN KEY (`award_id`) REFERENCES `award` (`id`) ON DELETE CASCADE,
  CONSTRAINT `award_image_ibfk_2` FOREIGN KEY (`member_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_award_image_file_storage` FOREIGN KEY (`file_storage_id`) REFERENCES `file_storage` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='濂栭」鍥剧墖鍏宠仈琛�';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `award_image`
--

LOCK TABLES `award_image` WRITE;
/*!40000 ALTER TABLE `award_image` DISABLE KEYS */;
INSERT INTO `award_image` VALUES (9,9,2,0,'2026-01-13 16:23:49',1),(10,10,2,0,'2026-01-20 20:15:39',2);
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
INSERT INTO `award_member` VALUES (9,2),(10,2);
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
  `code` varchar(50) NOT NULL,
  `name` varchar(100) NOT NULL,
  `type` varchar(50) NOT NULL,
  `sort_order` int DEFAULT '0',
  `status` tinyint DEFAULT '1',
  `description` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_type` (`code`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dictionary`
--

LOCK TABLES `dictionary` WRITE;
/*!40000 ALTER TABLE `dictionary` DISABLE KEYS */;
INSERT INTO `dictionary` VALUES (1,'LEVEL_NATIONAL','国家级','COMPETITION_LEVEL',1,1,'国家级比赛','2026-01-13 14:44:23','2026-01-13 14:44:23'),(2,'LEVEL_REGIONAL','地区级别','COMPETITION_LEVEL',2,1,'地区级比赛','2026-01-13 14:44:23','2026-01-13 14:44:23'),(3,'LEVEL_PROVINCIAL','省级','COMPETITION_LEVEL',3,1,'省级比赛','2026-01-13 14:44:23','2026-01-13 14:44:23'),(4,'LEVEL_OTHER','其他级别','COMPETITION_LEVEL',4,1,'其他级别的比赛','2026-01-13 14:44:23','2026-01-13 14:44:23'),(5,'TYPE_INDIVIDUAL','个人','AWARD_TYPE',1,1,'个人奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(6,'TYPE_TEAM','团队','AWARD_TYPE',2,1,'团队奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(7,'CATEGORY_PROJECT','项目','AWARD_CATEGORY',1,1,'项目类奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(8,'CATEGORY_ALGORITHM','经典算法','AWARD_CATEGORY',2,1,'算法类奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(9,'CATEGORY_AI','人工智能','AWARD_CATEGORY',3,1,'人工智能类奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(10,'CATEGORY_DOCUMENT','文档类','AWARD_CATEGORY',4,1,'文档类奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(11,'CATEGORY_OTHER','其他','AWARD_CATEGORY',5,1,'其他类别的奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(12,'ROLE_ADMIN','管理员','USER_ROLE',1,1,'系统管理员','2026-01-13 14:44:23','2026-01-13 14:44:23'),(13,'ROLE_MEMBER','成员','USER_ROLE',2,1,'团队成员','2026-01-13 14:44:23','2026-01-13 14:44:23'),(14,'ROLE_STUDENT','学生','USER_ROLE',3,1,'普通学生','2026-01-13 14:44:23','2026-01-13 14:44:23'),(15,'PENDING','待审核','AWARD_STATUS',1,1,'待管理员审核的奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(16,'APPROVED','已审核','AWARD_STATUS',2,1,'管理员已审核通过的奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(17,'REJECTED','已拒绝','AWARD_STATUS',3,1,'管理员已拒绝的奖项','2026-01-13 14:44:23','2026-01-13 14:44:23'),(18,'DRAFT','草稿','NEWS_STATUS',1,1,'未发布的新闻草稿','2026-01-13 14:44:23','2026-01-13 14:44:23'),(19,'PUBLISHED','已发布','NEWS_STATUS',2,1,'已发布的新闻','2026-01-13 14:44:23','2026-01-13 14:44:23'),(20,'ARCHIVED','已归档','NEWS_STATUS',3,1,'已归档的新闻','2026-01-13 14:44:23','2026-01-13 14:44:23'),(21,'LEVEL_FIRST','一等奖','AWARD_LEVEL',1,1,'一等奖','2026-01-13 14:58:39','2026-01-13 14:58:39'),(22,'LEVEL_SECOND','二等奖','AWARD_LEVEL',2,1,'二等奖','2026-01-13 14:58:39','2026-01-13 14:58:39'),(23,'LEVEL_THIRD','三等奖','AWARD_LEVEL',3,1,'三等奖','2026-01-13 14:58:39','2026-01-13 14:58:39'),(24,'LEVEL_EXCELLENT','优胜奖','AWARD_LEVEL',4,1,'优胜奖','2026-01-13 14:58:39','2026-01-13 14:58:39'),(25,'LEVEL_PARTICIPATION','参与奖','AWARD_LEVEL',5,1,'参与奖','2026-01-13 14:58:39','2026-01-13 14:58:39');
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
  `original_name` varchar(255) NOT NULL,
  `stored_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_type` varchar(50) DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `category` varchar(50) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_create_by` (`create_by`),
  CONSTRAINT `file_storage_fk_create_by` FOREIGN KEY (`create_by`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_storage`
--

LOCK TABLES `file_storage` WRITE;
/*!40000 ALTER TABLE `file_storage` DISABLE KEYS */;
INSERT INTO `file_storage` VALUES (1,2,'无标题.png','1768292629855_无标题.png','/localstorage/images/award/1768292629855_无标题.png','image/png',334591,'award_image','2026-01-13 16:43:40'),(2,2,'无标题.png','1768911339149_无标题.png','/localstorage/images/award/1768911339149_无标题.png','image/png',334591,'award_image','2026-01-20 20:15:39');
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
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `student_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `major` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `grade` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `birthday` date DEFAULT NULL,
  `gender` enum('male','female','other') COLLATE utf8mb4_unicode_ci DEFAULT 'other',
  `introduction` text COLLATE utf8mb4_unicode_ci,
  `skills` text COLLATE utf8mb4_unicode_ci,
  `avatar` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `github` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `blog` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `member_profile_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member_profile`
--

LOCK TABLES `member_profile` WRITE;
/*!40000 ALTER TABLE `member_profile` DISABLE KEYS */;
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
  `title` varchar(200) NOT NULL COMMENT '鏍囬?',
  `type` enum('award','activity','notice') NOT NULL COMMENT '绫诲瀷锛歛ward-鑾峰?鏂伴椈锛宎ctivity-娲诲姩鏂伴椈锛宯otice-閫氱煡鍏?憡',
  `content_path` varchar(255) NOT NULL COMMENT 'HTML鍐呭?鏂囦欢鐩稿?璺?緞',
  `summary` varchar(500) DEFAULT NULL COMMENT '鎽樿?',
  `author_id` int DEFAULT NULL COMMENT '鍙戝竷鑰匢D',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '鐘舵?锛?-鍙戝竷锛?-涓嬬嚎',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `author_id` (`author_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `news_ibfk_1` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鏂伴椈琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `news`
--

LOCK TABLES `news` WRITE;
/*!40000 ALTER TABLE `news` DISABLE KEYS */;
INSERT INTO `news` VALUES (1,'tesat','notice','localstorage/news/notice/1768192494445_notice.html','asgfa',1,1,'2026-01-12 12:34:54','2026-01-12 12:34:54'),(2,'sgasdgads','award','localstorage/news/award/1768192512844_award.html','adsgadsgads',1,1,'2026-01-12 12:35:12','2026-01-12 12:35:12'),(3,'asdas','notice','localstorage/news/notice/1768192524931_notice.html','asfhafh',1,1,'2026-01-12 12:35:24','2026-01-12 12:35:24'),(4,'asdhash','activity','localstorage/news/activity/1768192532845_activity.html','afhhad',1,1,'2026-01-12 12:35:32','2026-01-12 12:35:32'),(5,'asgash噶啥','notice','localstorage/news/notice/1768215581939_notice.html','asdfhafsh',1,1,'2026-01-12 18:59:41','2026-01-12 19:50:31');
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
  `username` varchar(50) DEFAULT NULL,
  `operation` varchar(100) NOT NULL,
  `module` varchar(50) DEFAULT NULL,
  `description` text,
  `ip_address` varchar(50) DEFAULT NULL,
  `user_agent` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation` (`operation`),
  KEY `idx_module` (`module`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=241 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operation_log`
--

LOCK TABLES `operation_log` WRITE;
/*!40000 ALTER TABLE `operation_log` DISABLE KEYS */;
INSERT INTO `operation_log` VALUES (1,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 21:26:58'),(2,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 21:36:18'),(3,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 21:36:30'),(4,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 21:38:00'),(5,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 21:49:41'),(6,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 21:52:58'),(7,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 21:53:22'),(8,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 21:57:05'),(9,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 22:01:44'),(10,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 22:08:15'),(11,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 22:30:27'),(12,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 22:34:33'),(13,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 22:53:36'),(14,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 22:54:57'),(15,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-11 22:57:44'),(16,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 11:45:34'),(17,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 11:48:37'),(18,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 11:51:19'),(19,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:26:03'),(20,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:26:29'),(21,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:27:21'),(22,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:31:15'),(23,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:31:29'),(24,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:33:38'),(25,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:33:47'),(26,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:33:54'),(27,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:34:46'),(28,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:34:54'),(29,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:35:12'),(30,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:35:24'),(31,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:35:32'),(32,1,'admin','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 12:36:05'),(33,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 13:49:18'),(34,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 13:53:07'),(35,1,'admin','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 13:56:32'),(36,1,'admin','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 13:57:22'),(37,1,'admin','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 13:58:09'),(38,1,'admin','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 13:58:33'),(39,1,'admin','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:01:04'),(40,1,'admin','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:04:02'),(41,1,'admin','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:05:21'),(42,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:09:16'),(43,1,'admin','POST','管理员','POST /Software_group_Web_exploded/admin/recruit/manage','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:11:31'),(44,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:16:12'),(45,1,'admin','POST','管理员','POST /Software_group_Web_exploded/admin/recruit/manage','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:16:46'),(46,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:20:45'),(47,1,'admin','POST','管理员','POST /Software_group_Web_exploded/admin/recruit/manage','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:21:08'),(48,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:26:01'),(49,1,'admin','POST','管理员','POST /Software_group_Web_exploded/admin/recruit/manage','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 14:26:16'),(50,NULL,'游客','POST','招新','POST /Software_group_Web_exploded/recruit/submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:12:41'),(51,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:13:01'),(52,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:22:25'),(53,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:36:48'),(54,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:39:15'),(55,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:41:50'),(56,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:46:23'),(57,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:50:16'),(58,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 15:52:26'),(59,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 18:48:21'),(60,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 18:58:03'),(61,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 18:58:14'),(62,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 18:58:30'),(63,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=create','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 18:59:41'),(64,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:06:38'),(65,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:07:06'),(66,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:08:36'),(67,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:08:43'),(68,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:10:31'),(69,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:10:37'),(70,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:10:44'),(71,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:10:58'),(72,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:11:03'),(73,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:12:12'),(74,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:12:20'),(75,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:13:28'),(76,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:13:41'),(77,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:14:58'),(78,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:15:06'),(79,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:16:37'),(80,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:16:43'),(81,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:18:32'),(82,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:18:48'),(83,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:20:43'),(84,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:20:56'),(85,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:22:55'),(86,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:23:15'),(87,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:24:39'),(88,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:24:47'),(89,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=%20update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:24:55'),(90,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:25:39'),(91,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:25:50'),(92,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:27:03'),(93,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:31:05'),(94,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:31:13'),(95,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:34:07'),(96,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:34:16'),(97,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:40:33'),(98,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:44:18'),(99,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:46:03'),(100,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news?action=delete&id=5','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:50:20'),(101,1,'admin','POST','新闻','POST /Software_group_Web_exploded/news','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 19:50:30'),(102,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 20:09:43'),(103,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 20:13:23'),(104,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 20:15:54'),(105,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 21:04:24'),(106,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 21:05:13'),(107,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 21:42:01'),(108,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 21:48:49'),(109,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:07:15'),(110,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:07:58'),(111,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:08:06'),(112,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:14:32'),(113,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:14:44'),(114,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:15:10'),(115,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:19:41'),(116,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:20:03'),(117,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:21:58'),(118,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-12 22:22:18'),(119,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 09:52:11'),(120,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 09:52:33'),(121,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 09:56:17'),(122,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 09:56:41'),(123,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 09:58:59'),(124,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 09:59:22'),(125,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:02:07'),(126,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:02:26'),(127,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:06:29'),(128,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:06:48'),(129,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:12:35'),(130,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:13:00'),(131,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:19:07'),(132,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:19:26'),(133,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:26:34'),(134,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:26:56'),(135,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:31:46'),(136,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:32:06'),(137,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:32:38'),(138,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:36:41'),(139,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:36:41'),(140,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:37:00'),(141,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:40:07'),(142,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:40:28'),(143,NULL,'游客','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:43:42'),(144,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:43:44'),(145,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:44:16'),(146,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 10:44:52'),(147,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:07:33'),(148,NULL,'游客','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:07:50'),(149,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:07:51'),(150,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:08:19'),(151,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:10:28'),(152,NULL,'游客','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:10:38'),(153,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:10:59'),(154,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:12:49'),(155,NULL,'游客','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:14:52'),(156,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:14:53'),(157,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:15:17'),(158,NULL,'游客','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:18:24'),(159,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:18:25'),(160,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:18:46'),(161,NULL,'游客','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:20:28'),(162,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:20:29'),(163,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:20:48'),(164,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:27:18'),(165,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:27:40'),(166,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:30:34'),(167,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:38:05'),(168,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:38:28'),(169,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:41:35'),(170,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:47:18'),(171,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 11:47:58'),(172,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 12:36:28'),(173,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 14:09:37'),(174,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 14:21:23'),(175,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 14:55:37'),(176,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:00:40'),(177,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:01:09'),(178,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:01:28'),(179,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:01:42'),(180,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:03:20'),(181,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:19:28'),(182,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:19:53'),(183,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:23:11'),(184,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:29:15'),(185,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:42:09'),(186,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:43:05'),(187,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:49:07'),(188,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:52:49'),(189,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','127.0.0.1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:52:50'),(190,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 15:58:28'),(191,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 16:07:30'),(192,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 16:07:30'),(193,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 16:08:01'),(194,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=update','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 16:08:16'),(195,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 16:12:47'),(196,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 16:23:28'),(197,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 16:23:49'),(198,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 16:58:29'),(199,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:00:27'),(200,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:03:15'),(201,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:09:24'),(202,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:12:21'),(203,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:18:03'),(204,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:23:55'),(205,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:27:06'),(206,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:29:28'),(207,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:29:46'),(208,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:30:43'),(209,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:33:52'),(210,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:41:18'),(211,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:48:18'),(212,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:50:18'),(213,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:53:24'),(214,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-13 17:56:55'),(215,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 20:44:31'),(216,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 20:45:04'),(217,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 20:45:09'),(218,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 20:57:54'),(219,1,'admin','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 20:58:02'),(220,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 20:58:16'),(221,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 21:02:03'),(222,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 21:11:42'),(223,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 21:15:44'),(224,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 21:15:58'),(225,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 21:16:17'),(226,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 21:19:24'),(227,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-18 21:36:58'),(228,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 20:10:18'),(229,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 20:14:45'),(230,1,'admin','POST','奖项','POST /Software_group_Web_exploded/award?action=approve','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 20:14:56'),(231,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 20:15:08'),(232,2,'member1','POST','奖项','POST /Software_group_Web_exploded/award?action=submit','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 20:15:38'),(233,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 20:15:51'),(234,1,'admin','POST','奖项','POST /Software_group_Web_exploded/award?action=reject','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 20:15:58'),(235,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 20:58:18'),(236,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 21:12:41'),(237,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 21:14:02'),(238,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 21:38:45'),(239,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 22:23:14'),(240,NULL,'游客','POST','其他','POST /Software_group_Web_exploded/login','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36','2026-01-20 22:24:20');
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
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `year` int NOT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `status` enum('pending','approved','in_progress','completed','canceled') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `leader_id` int NOT NULL,
  `budget` decimal(10,2) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `leader_id` (`leader_id`),
  CONSTRAINT `project_ibfk_1` FOREIGN KEY (`leader_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project`
--

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
/*!40000 ALTER TABLE `project` ENABLE KEYS */;
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
  `role` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
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
/*!40000 ALTER TABLE `project_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recruit_application`
--

DROP TABLE IF EXISTS `recruit_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recruit_application` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `student_id` varchar(50) DEFAULT NULL,
  `major` varchar(100) DEFAULT NULL,
  `grade` varchar(20) DEFAULT NULL,
  `phone` varchar(20) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `reason` text,
  `status` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recruit_application`
--

LOCK TABLES `recruit_application` WRITE;
/*!40000 ALTER TABLE `recruit_application` DISABLE KEYS */;
INSERT INTO `recruit_application` VALUES (1,'zhangsan ','12445','2346','2362346','18725533292','dfsgs@hsu.edu.cn','dsfg',1,'2026-01-12 14:26:16'),(2,'zhangsan ','12445','2346','2362346','18725533292','dfsgs@hsu.edu.cn','sdfa',1,'2026-01-12 15:12:41');
/*!40000 ALTER TABLE `recruit_application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` enum('ADMIN','MEMBER','TEACHER','GUEST') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MEMBER',
  `status` int DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','0192023a7bbd73250516f069df18b500','admin@example.com','13800138000','ADMIN',1,'2025-12-21 14:17:28','2026-01-11 13:44:57'),(2,'member1','0192023a7bbd73250516f069df18b500','member1@example.com','13800138001','MEMBER',1,'2025-12-21 14:17:28','2026-01-12 11:40:13');
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

-- Dump completed on 2026-01-21  7:32:38
