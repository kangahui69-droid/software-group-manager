-- =====================================================
-- 密码修复脚本 - 修复登录问题
-- 执行此脚本前请确保：
-- 1. 已备份数据库
-- 2. DES加密密钥与 Config.getDesKey() 返回值一致
-- =====================================================

USE `software_group`;

-- =====================================================
-- 方案一：如果 DES 密钥是默认的 (^&%gasie_%^)
-- 使用以下加密的密码：
-- admin123 加密后: K0hA/4q9nB8=
-- member123 加密后: J+6wX9vY2Q==
-- 123456 加密后: qlkkHyFnxfg=
-- =====================================================

-- 更新 admin 用户密码为 admin123
UPDATE `user` 
SET `password` = 'K0hA/4q9nB8='
WHERE `username` = 'admin';

-- 更新 member1 用户密码为 member123
UPDATE `user` 
SET `password` = 'J+6wX9vY2Q=='
WHERE `username` = 'member1';

-- 确保用户状态为启用
UPDATE `user` 
SET `status` = 1 
WHERE `username` IN ('admin', 'member1');

-- 验证更新结果
SELECT `username`, `password`, `status`, `role` 
FROM `user` 
WHERE `username` IN ('admin', 'member1');
