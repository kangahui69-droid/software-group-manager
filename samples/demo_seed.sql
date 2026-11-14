-- Fictional sample data for isolated development and tests only.
-- Do not import this file into production.
-- Load sql/software_group.sql first to create the schema.

INSERT INTO `dictionary` (`code`, `name`, `type`, `sort_order`, `status`, `description`)
VALUES
  ('DEMO_TODO', '演示待办', 'DEMO_STATUS', 1, 1, '仅供隔离测试'),
  ('DEMO_DONE', '演示完成', 'DEMO_STATUS', 2, 1, '仅供隔离测试');
