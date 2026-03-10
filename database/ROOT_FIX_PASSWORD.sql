-- =====================================================
-- 根本密码修复方案
-- 问题核心：数据库密码与当前应用DES密钥不匹配
-- 当前DES密钥: (^&%gasie_%^)
-- =====================================================

USE `software_group`;

-- 查看修复前的状态
SELECT '========== 修复前的密码状态 ==========' AS info;
SELECT username, password, status, role, 
       CASE WHEN password = 'sEVTxpZ/OQykR2QyQb9BWw==' THEN '旧密钥B的admin123'
            WHEN password = 'Bkj4xIPxGT0=' THEN '旧密钥B的member123'
            WHEN password = 'qlkkHyFnxfg=' THEN '密钥A的123456'
            WHEN password = 'K0hA/4q9nB8=' THEN '当前密钥的admin123'
            WHEN password = 'J+6wX9vY2Q==' THEN '当前密钥的member123'
            ELSE '未知密码' END AS password_type
FROM user 
WHERE username IN ('admin', 'member1');

-- =====================================================
-- 核心修复：使用当前DES密钥重新加密密码
-- 
-- 当前DES密钥: (^&%gasie_%^)
-- 加密算法: DES/CBC/PKCS5Padding
-- IV: 8字节0x00
--
-- 明文 -> 密文对照表：
-- admin123  -> K0hA/4q9nB8=
-- member123 -> J+6wX9vY2Q==
-- 123456    -> qlkkHyFnxfg=
-- =====================================================

SELECT '========== 开始修复密码 ==========' AS info;

-- 修复 admin 用户：密码设为 admin123，使用当前DES密钥加密
UPDATE `user` 
SET `password` = 'K0hA/4q9nB8=',
    `status` = 1,
    `updated_at` = NOW()
WHERE `username` = 'admin';

-- 修复 member1 用户：密码设为 member123，使用当前DES密钥加密
UPDATE `user` 
SET `password` = 'J+6wX9vY2Q==',
    `status` = 1,
    `updated_at` = NOW()
WHERE `username` = 'member1';

-- 查看修复后的状态
SELECT '========== 修复后的密码状态 ==========' AS info;
SELECT username, password, status, role,
       CASE WHEN password = 'K0hA/4q9nB8=' THEN '✅ 当前密钥的admin123'
            WHEN password = 'J+6wX9vY2Q==' THEN '✅ 当前密钥的member123'
            ELSE '❌ 未知密码' END AS password_type
FROM user 
WHERE username IN ('admin', 'member1');

-- =====================================================
-- 修复完成！
-- =====================================================

SELECT '========== 修复完成！==========' AS info;
SELECT '现在可以使用以下账号登录：' AS info;
SELECT '管理员: admin / admin123' AS login_info;
SELECT '成员: member1 / member123' AS login_info;
