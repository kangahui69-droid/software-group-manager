-- DB-03: runtime grants only. Accounts/passwords are created by provision-db-users.sh.
-- This file is not a Flyway migration. It must run after the business schema exists.
-- No account receives DDL, GRANT OPTION, mysql.*, or flyway_schema_history access.

GRANT SELECT, INSERT, UPDATE ON software_group.file_storage TO 'sg_file_svc'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.user TO 'sg_user_svc'@'%';
GRANT SELECT, INSERT, UPDATE ON software_group.member_profile TO 'sg_user_svc'@'%';
GRANT SELECT, INSERT, UPDATE ON software_group.admin_profile TO 'sg_user_svc'@'%';

GRANT SELECT, INSERT, UPDATE ON software_group.activity TO 'sg_activity_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.activity_participant TO 'sg_activity_svc'@'%';
GRANT SELECT, INSERT, UPDATE ON software_group.attendance TO 'sg_activity_svc'@'%';
GRANT SELECT, INSERT, UPDATE ON software_group.attendance_makeup TO 'sg_activity_svc'@'%';
GRANT SELECT, INSERT, UPDATE ON software_group.study_session TO 'sg_activity_svc'@'%';
GRANT SELECT ON software_group.user TO 'sg_activity_svc'@'%';
GRANT SELECT ON software_group.member_profile TO 'sg_activity_svc'@'%';

GRANT SELECT, INSERT, UPDATE ON software_group.project TO 'sg_project_award_svc'@'%';
GRANT SELECT, INSERT, UPDATE ON software_group.award TO 'sg_project_award_svc'@'%';
GRANT SELECT, INSERT, DELETE ON software_group.award_image TO 'sg_project_award_svc'@'%';
GRANT SELECT, INSERT ON software_group.award_member TO 'sg_project_award_svc'@'%';
GRANT SELECT, INSERT, UPDATE ON software_group.project_member TO 'sg_project_award_svc'@'%';
GRANT SELECT, INSERT, UPDATE ON software_group.project_member_application TO 'sg_project_award_svc'@'%';
GRANT SELECT, INSERT ON software_group.project_history TO 'sg_project_award_svc'@'%';
GRANT SELECT, INSERT ON software_group.project_plan TO 'sg_project_award_svc'@'%';
GRANT SELECT, INSERT ON software_group.project_progress TO 'sg_project_award_svc'@'%';
GRANT SELECT ON software_group.project_label TO 'sg_project_award_svc'@'%';
GRANT SELECT ON software_group.dictionary TO 'sg_project_award_svc'@'%';
GRANT SELECT ON software_group.file_storage TO 'sg_project_award_svc'@'%';
GRANT SELECT ON software_group.user TO 'sg_project_award_svc'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.recruit_application TO 'sg_hr_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resumes TO 'sg_hr_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resume_educations TO 'sg_hr_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resume_skills TO 'sg_hr_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resume_projects TO 'sg_hr_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resume_awards TO 'sg_hr_svc'@'%';
GRANT SELECT, INSERT ON software_group.member_profile TO 'sg_hr_svc'@'%';
GRANT SELECT, INSERT ON software_group.user TO 'sg_hr_svc'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.problem_report TO 'sg_monitor_svc'@'%';
GRANT SELECT ON software_group.operation_log TO 'sg_monitor_svc'@'%';
GRANT SELECT ON software_group.user TO 'sg_monitor_svc'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.activity_group TO 'sg_content_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.group_member TO 'sg_content_svc'@'%';
GRANT SELECT, INSERT, DELETE ON software_group.group_message TO 'sg_content_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.user_group TO 'sg_content_svc'@'%';
GRANT SELECT, INSERT, UPDATE ON software_group.news TO 'sg_content_svc'@'%';
GRANT SELECT ON software_group.file_storage TO 'sg_content_svc'@'%';
GRANT SELECT ON software_group.member_profile TO 'sg_content_svc'@'%';
GRANT SELECT ON software_group.user TO 'sg_content_svc'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_conversation TO 'sg_ai_svc'@'%';
GRANT SELECT, INSERT, UPDATE ON software_group.ai_faq_statistics TO 'sg_ai_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_knowledge_base TO 'sg_ai_svc'@'%';
GRANT SELECT, INSERT, DELETE ON software_group.ai_message TO 'sg_ai_svc'@'%';
GRANT SELECT, INSERT, UPDATE ON software_group.ai_message_status TO 'sg_ai_svc'@'%';

-- The legacy WAR still spans modules. Grant DML only on its business tables.
-- It cannot write Flyway history, Nacos tables, user accounts, or schema objects.
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.activity TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.activity_group TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.activity_participant TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.admin_profile TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_conversation TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_faq_statistics TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_knowledge_base TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_message TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.ai_message_status TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.attendance TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.attendance_makeup TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.award TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.award_image TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.award_member TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.dictionary TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.file_storage TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.group_member TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.group_message TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.member_profile TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.news TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.operation_log TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.problem_management TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.problem_report TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_file TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_history TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_image TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_label TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_member TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_member_application TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_plan TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.project_progress TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.recruit_application TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resume_awards TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resume_educations TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resume_projects TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resume_skills TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.resumes TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.study_session TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.user TO 'sg_app_svc'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON software_group.user_group TO 'sg_app_svc'@'%';

-- Nacos schema is managed separately from software_group/Flyway.
GRANT SELECT, INSERT, UPDATE, DELETE ON nacos_config.* TO 'sg_nacos_svc'@'%';
