-- ============================================================
-- 微服务数据库用户及权限配置
-- 对应计划文档：阶段三_微服务拆分实施计划 - 5.2 数据库策略
-- 使用方式：先由管理员通过受控方式创建服务账号，再手动执行本文件；不放入自动初始化目录
-- ============================================================

-- ============================================================
-- 前置条件：使用独立的随机密码创建以下服务用户（本文件不包含密码）
-- ============================================================

-- file-service：只访问文件相关表

-- user-service：访问用户、成员档案、管理员档案相关表

-- activity-service：访问活动、考勤、学习相关表

-- project-award-service：访问项目、奖项相关表

-- content-service：访问群组、新闻相关表

-- hr-service：访问招聘、简历相关表

-- monitor-service：访问问题反馈、操作日志相关表

-- ai-service：访问 AI 相关表

-- ============================================================
-- 第二步：file-service 权限（只访问文件存储表）
-- ============================================================
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.file_storage TO 'file_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.file_storage TO 'file_svc'@'localhost';

-- ============================================================
-- 第三步：user-service 权限（用户、成员档案、管理员档案）
-- ============================================================
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.user TO 'user_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.user TO 'user_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.member_profile TO 'user_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.member_profile TO 'user_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.admin_profile TO 'user_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.admin_profile TO 'user_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.user_group TO 'user_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.user_group TO 'user_svc'@'localhost';
-- user-service 需要查询活动创建者信息（只读）
GRANT SELECT ON software_group.activity TO 'user_svc'@'%';
GRANT SELECT ON software_group.activity TO 'user_svc'@'localhost';
-- user-service 需要查询项目成员信息（只读）
GRANT SELECT ON software_group.project_member TO 'user_svc'@'%';
GRANT SELECT ON software_group.project_member TO 'user_svc'@'localhost';

-- ============================================================
-- 第四步：activity-service 权限（活动、考勤、学习）
-- ============================================================
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.activity TO 'activity_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.activity TO 'activity_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.activity_participant TO 'activity_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.activity_participant TO 'activity_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.activity_group TO 'activity_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.activity_group TO 'activity_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.attendance TO 'activity_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.attendance TO 'activity_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.attendance_makeup TO 'activity_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.attendance_makeup TO 'activity_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.attendance_config TO 'activity_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.attendance_config TO 'activity_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.study_session TO 'activity_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.study_session TO 'activity_svc'@'localhost';
-- activity-service 需要查询用户信息（只读，用于获取活动创建者姓名）
GRANT SELECT ON software_group.user TO 'activity_svc'@'%';
GRANT SELECT ON software_group.user TO 'activity_svc'@'localhost';
GRANT SELECT ON software_group.member_profile TO 'activity_svc'@'%';
GRANT SELECT ON software_group.member_profile TO 'activity_svc'@'localhost';

-- ============================================================
-- 第五步：project-award-service 权限（项目、奖项）
-- ============================================================
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project TO 'project_award_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project TO 'project_award_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_file TO 'project_award_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_file TO 'project_award_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_image TO 'project_award_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_image TO 'project_award_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_label TO 'project_award_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_label TO 'project_award_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_plan TO 'project_award_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_plan TO 'project_award_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_progress TO 'project_award_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_progress TO 'project_award_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_history TO 'project_award_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_history TO 'project_award_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_member TO 'project_award_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_member TO 'project_award_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_member_application TO 'project_award_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_member_application TO 'project_award_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.award TO 'project_award_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.award TO 'project_award_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.award_image TO 'project_award_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.award_image TO 'project_award_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.award_member TO 'project_award_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.award_member TO 'project_award_svc'@'localhost';
-- project-award-service 需要查询用户信息（只读）
GRANT SELECT ON software_group.user TO 'project_award_svc'@'%';
GRANT SELECT ON software_group.user TO 'project_award_svc'@'localhost';

-- ============================================================
-- 第六步：content-service 权限（群组、新闻）
-- ============================================================
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.user_group TO 'content_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.user_group TO 'content_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.group_member TO 'content_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.group_member TO 'content_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.group_message TO 'content_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.group_message TO 'content_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.news TO 'content_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.news TO 'content_svc'@'localhost';
-- content-service 需要查询用户信息（只读）
GRANT SELECT ON software_group.user TO 'content_svc'@'%';
GRANT SELECT ON software_group.user TO 'content_svc'@'localhost';
-- content-service 需要访问 file_storage（上传群组/新闻图片）
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.file_storage TO 'content_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.file_storage TO 'content_svc'@'localhost';

-- ============================================================
-- 第七步：hr-service 权限（招聘、简历）
-- ============================================================
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.recruit_application TO 'hr_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.recruit_application TO 'hr_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resumes TO 'hr_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resumes TO 'hr_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resume_educations TO 'hr_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resume_educations TO 'hr_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resume_skills TO 'hr_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resume_skills TO 'hr_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resume_projects TO 'hr_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resume_projects TO 'hr_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resume_awards TO 'hr_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resume_awards TO 'hr_svc'@'localhost';
-- hr-service 需要查询成员档案（只读）
GRANT SELECT ON software_group.member_profile TO 'hr_svc'@'%';
GRANT SELECT ON software_group.member_profile TO 'hr_svc'@'localhost';
-- hr-service 需要查询用户信息（只读）
GRANT SELECT ON software_group.user TO 'hr_svc'@'%';
GRANT SELECT ON software_group.user TO 'hr_svc'@'localhost';

-- ============================================================
-- 第八步：monitor-service 权限（问题反馈、操作日志）
-- ============================================================
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.problem_report TO 'monitor_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.problem_report TO 'monitor_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.operation_log TO 'monitor_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.operation_log TO 'monitor_svc'@'localhost';
-- monitor-service 需要查询用户信息（只读）
GRANT SELECT ON software_group.user TO 'monitor_svc'@'%';
GRANT SELECT ON software_group.user TO 'monitor_svc'@'localhost';

-- ============================================================
-- 第九步：ai-service 权限（AI 对话、知识库、FAQ）
-- ============================================================
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_conversation TO 'ai_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_conversation TO 'ai_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_message TO 'ai_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_message TO 'ai_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_message_status TO 'ai_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_message_status TO 'ai_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_knowledge_base TO 'ai_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_knowledge_base TO 'ai_svc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_faq_statistics TO 'ai_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_faq_statistics TO 'ai_svc'@'localhost';
-- AI 服务需要通过 Feign 获取业务数据（只读用户、活动、项目等）
-- 注意：这些是业务数据，只读通过 Feign 调用，不直接访问
GRANT SELECT ON software_group.user TO 'ai_svc'@'%';
GRANT SELECT ON software_group.user TO 'ai_svc'@'localhost';
GRANT SELECT ON software_group.member_profile TO 'ai_svc'@'%';
GRANT SELECT ON software_group.member_profile TO 'ai_svc'@'localhost';
GRANT SELECT ON software_group.activity TO 'ai_svc'@'%';
GRANT SELECT ON software_group.activity TO 'ai_svc'@'localhost';
GRANT SELECT ON software_group.project TO 'ai_svc'@'%';
GRANT SELECT ON software_group.project TO 'ai_svc'@'localhost';
GRANT SELECT ON software_group.award TO 'ai_svc'@'%';
GRANT SELECT ON software_group.award TO 'ai_svc'@'localhost';

-- ============================================================
-- 第十步：刷新权限
-- ============================================================
FLUSH PRIVILEGES;

-- ============================================================
-- 验证脚本：查看各用户的权限
-- ============================================================
-- SELECT user, host FROM mysql.user WHERE user LIKE '%_svc%';
-- SHOW GRANTS FOR 'file_svc'@'%';
-- SHOW GRANTS FOR 'user_svc'@'%';
-- SHOW GRANTS FOR 'activity_svc'@'%';
-- SHOW GRANTS FOR 'project_award_svc'@'%';
-- SHOW GRANTS FOR 'content_svc'@'%';
-- SHOW GRANTS FOR 'hr_svc'@'%';
-- SHOW GRANTS FOR 'monitor_svc'@'%';
-- SHOW GRANTS FOR 'ai_svc'@'%';

-- ============================================================
-- 回滚脚本（如果需要删除这些用户）
-- ============================================================
-- DROP USER IF EXISTS 'file_svc'@'%';
-- DROP USER IF EXISTS 'file_svc'@'localhost';
-- DROP USER IF EXISTS 'user_svc'@'%';
-- DROP USER IF EXISTS 'user_svc'@'localhost';
-- DROP USER IF EXISTS 'activity_svc'@'%';
-- DROP USER IF EXISTS 'activity_svc'@'localhost';
-- DROP USER IF EXISTS 'project_award_svc'@'%';
-- DROP USER IF EXISTS 'project_award_svc'@'localhost';
-- DROP USER IF EXISTS 'content_svc'@'%';
-- DROP USER IF EXISTS 'content_svc'@'localhost';
-- DROP USER IF EXISTS 'hr_svc'@'%';
-- DROP USER IF EXISTS 'hr_svc'@'localhost';
-- DROP USER IF EXISTS 'monitor_svc'@'%';
-- DROP USER IF EXISTS 'monitor_svc'@'localhost';
-- DROP USER IF EXISTS 'ai_svc'@'%';
-- DROP USER IF EXISTS 'ai_svc'@'localhost';
-- FLUSH PRIVILEGES;
