-- =====================================================
-- 快速密码修复脚本
-- 说明：当前数据库中的密码与当前 DES 密钥不匹配
-- 执行此脚本将重置密码为：admin123 / member123
-- =====================================================

USE `software_group`;

-- 重置 admin 用户密码为 admin123
-- 使用当前 DES 密钥加密后的值
UPDATE `user` 
SET `password` = 'K0hA/4q9nB8='
WHERE `username` = 'admin';

-- 重置 member1 用户密码为 member123  
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
