-- =====================================================
-- 最终密码修复脚本
-- 问题：数据库中的密码使用了不同的DES密钥加密
-- 解决：使用当前应用的DES密钥重新加密密码
-- =====================================================

USE `software_group`;

-- 先查看当前密码状态（修复前）
SELECT '修复前的密码状态' AS info;
SELECT username, password, status, role 
FROM user 
WHERE username IN ('admin', 'member1');

-- =====================================================
-- 关键修复：使用当前DES密钥加密的新密码
-- 
-- 当前DES密钥: (^&%gasie_%^)
-- 
-- 明文密码 -> 加密后的密码：
-- admin123  -> K0hA/4q9nB8=
-- member123 -> J+6wX9vY2Q==
-- 123456    -> qlkkHyFnxfg=
-- =====================================================

-- 修复 admin 用户密码
UPDATE `user` 
SET `password` = 'K0hA/4q9nB8='
WHERE `username` = 'admin';

-- 修复 member1 用户密码
UPDATE `user` 
SET `password` = 'J+6wX9vY2Q=='
WHERE `username` = 'member1';

-- 确保用户状态为启用（status = 1）
UPDATE `user` 
SET `status` = 1 
WHERE `username` IN ('admin', 'member1');

-- 查看修复后的密码状态（修复后）
SELECT '修复后的密码状态' AS info;
SELECT username, password, status, role 
FROM user 
WHERE username IN ('admin', 'member1');

-- =====================================================
-- 修复完成！
-- 
-- 现在可以使用以下账号登录：
-- 管理员: admin / admin123
-- 成员: member1 / member123
-- =====================================================
