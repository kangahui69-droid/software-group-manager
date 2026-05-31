-- ============================================
-- AI助手功能优化迁移脚本 v1.0
-- 日期: 2026-04-11
-- 描述: 优化AI助手功能，移除干扰ACTION系统的FAQ
-- ============================================

-- 1. 删除干扰AI助手ACTION系统的FAQ记录
-- 这些FAQ会拦截用户的正常查询，导致ACTION无法正确执行

-- 查看当前FAQ
-- SELECT id, category, question, keywords FROM ai_knowledge_base WHERE status = 1;

-- 删除与ACTION功能重复的FAQ（按关键词匹配）
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%发布新闻%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%如何发布新闻%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%创建项目%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%如何创建项目%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%提交奖项%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%如何提交奖项%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%申报奖项%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%申请奖项%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%新闻列表%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%查看新闻%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%动态%' AND category LIKE '%游客%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%活动列表%';
DELETE FROM ai_knowledge_base WHERE keywords LIKE '%查看活动%';

-- 2. 删除所有包含"奖项"的FAQ（奖项功能未完善）
DELETE FROM ai_knowledge_base WHERE question LIKE '%奖项%' OR keywords LIKE '%奖项%';

-- 3. 删除所有包含"项目"的FAQ（项目功能未完善）
DELETE FROM ai_knowledge_base WHERE question LIKE '%项目%' OR keywords LIKE '%项目%';

-- 4. 禁用访客指南中可能被误匹配的FAQ
UPDATE ai_knowledge_base SET status = 0 WHERE category = '游客指南' AND keywords LIKE '%新闻%';
UPDATE ai_knowledge_base SET status = 0 WHERE category = '游客指南' AND keywords LIKE '%活动%';

-- ============================================
-- 执行说明
-- ============================================
-- 1. 执行此脚本前请备份数据库
-- 2. 执行: mysql -u root -p software_group < sql/migration_ai_20260411.sql
-- 或在MySQL客户端中: source sql/migration_ai_20260411.sql
-- ============================================
