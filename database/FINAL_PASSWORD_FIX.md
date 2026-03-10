# 密码问题根本解决方案

## 问题分析

数据库中的密码使用了**不同的加密密钥**，导致登录失败。

### 密码历史演变

| 时间 | 加密方式 | 说明 |
|------|----------|------|
| 2025-12-21 | MD5 | `0192023a7bbd73250516f069df18b500` |
| 2026-01-21 | DES(密钥A) | `qlkkHyFnxfg=` (123456的加密) |
| 2026-02-18 | DES(密钥B) | `sEVTxpZ/OQykR2QyQb9BWw==` (admin123的加密) |
| 2026-02-23 | DES(密钥B) | `Bkj4xIPxGT0=` (member123的加密) |

### 当前应用使用的密钥

**当前 DES 密钥：** `(^&%gasie_%^`

**使用当前密钥加密的结果：**
- `123456` → `qlkkHyFnxfg=`
- `admin123` → `K0hA/4q9nB8=`
- `member123` → `J+6wX9vY2Q==`

## 解决方案

### 方案一：修复数据库密码（推荐）

执行 `fix_password_final.sql` 脚本，使用当前密钥重新加密密码。

**更新后的密码：**
- admin / admin123
- member1 / member123

### 方案二：修改应用密钥（不推荐）

如果需要保持数据库密码不变，可以修改 `config.properties` 中的密钥为历史密钥。但这样会影响其他用户的密码。

## 执行步骤

1. 打开 MySQL 客户端
2. 连接到 `software_group` 数据库
3. 执行 `fix_password_final.sql` 脚本
4. 重启 Tomcat 服务器
5. 使用新密码登录

## 验证

执行以下 SQL 验证密码已更新：

```sql
SELECT username, password, status, role 
FROM user 
WHERE username IN ('admin', 'member1');
```

预期结果：
- admin 的 password 应该是 `K0hA/4q9nB8=`
- member1 的 password 应该是 `J+6wX9vY2Q==`
