# 高校软件小组管理系统 - 数据库开发文档

## 1. 数据库结构概述

### 1.1 核心表结构

| 表名 | 说明 | 状态 |
|------|------|------|
| user | 用户表，存储用户基本信息和登录凭证 | ✅ 已实现 |
| member_profile | 成员档案表，存储成员详细信息 | ✅ 已实现 |
| admin_profile | 管理员档案表，存储管理员详细信息 | ✅ 已实现 |
| news | 新闻表，存储新闻和通知 | ✅ 已实现 |
| recruit_application | 招新申请表，存储招新报名信息 | ✅ 已实现 |
| activity | 活动表，存储活动信息 | ✅ 已实现 |
| activity_participant | 活动参与者表，存储活动报名信息 | ✅ 已实现 |
| award | 奖项表，存储奖项信息 | ✅ 已实现 |
| award_member | 奖项成员关联表，存储奖项与成员的多对多关系 | ✅ 已实现 |
| award_image | 奖项图片表，存储奖项相关图片 | ✅ 已实现 |
| project | 项目表，存储项目信息 | ✅ 已实现 |
| project_member | 项目成员表，存储项目与成员的关联关系 | ✅ 已实现 |
| project_member_application | 项目成员申请表，存储成员申请加入项目的信息 | ✅ 已实现 |
| project_plan | 项目计划表，存储项目计划 | ✅ 已实现 |
| project_progress | 项目进度表，存储项目进度 | ✅ 已实现 |
| project_file | 项目文件表，存储项目相关文件 | ✅ 已实现 |
| project_image | 项目图片表，存储项目相关图片 | ✅ 已实现 |
| project_label | 项目标签表，存储项目标签 | ✅ 已实现 |
| project_history | 项目历史表，存储项目变更历史 | ✅ 已实现 |
| file_storage | 文件存储表，统一管理文件元数据 | ✅ 已实现 |
| operation_log | 操作日志表，记录系统操作 | ✅ 已实现 |
| dictionary | 字典表，存储系统常量和枚举值 | ✅ 已实现 |

### 1.2 数据类型规范

| 数据类型 | 使用场景 | 示例 |
|---------|---------|------|
| int | 整数类型，用于ID、数量等 | `id`, `max_participants` |
| varchar | 字符串类型，用于名称、描述等 | `name`, `description` |
| text | 长文本类型，用于详细描述 | `bio`, `introduction` |
| datetime | 日期时间类型，用于时间戳 | `created_at`, `updated_at` |
| date | 日期类型，用于日期 | `birthday`, `competition_time` |
| decimal | 小数类型，用于金额 | `budget` |
| enum | 枚举类型，用于状态、类型等 | `role`, `status` |
| tinyint | 布尔类型，用于开关、状态 | `deleted`, `status` |

## 2. 数据库配置管理

### 2.1 配置文件结构

系统使用配置文件管理数据库连接信息，配置文件位于 `config/` 目录下：

- `config.properties.template` - 配置文件模板，包含所有配置项的默认值
- `config.local.properties` - 本地开发配置文件，包含实际的数据库连接信息
- `config.test.properties` - 测试环境配置文件
- `config.prod.properties` - 生产环境配置文件

### 2.2 配置文件使用

1. **首次拉取项目后**：
   - 复制 `config/config.properties.template` 为 `config/config.local.properties`
   - 修改 `config.local.properties` 中的数据库配置为实际值

2. **配置项说明**：
   ```properties
   # 数据库URL
   db.url=jdbc:mysql://localhost:3306/software_group?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
   
   # 数据库用户名
   db.username=root
   
   # 数据库密码
   db.password=your_password_here
   ```

3. **配置加载机制**：
   - 系统使用 `SimpleConfig` 类加载配置文件
   - 优先加载 `config.local.properties`
   - 如果不存在，则加载 `config.properties.template`
   - 提供默认值作为备用

### 2.3 安全注意事项

- **不要将包含真实密码的配置文件提交到Git**：
  - `config.local.properties`、`config.test.properties`、`config.prod.properties` 已添加到 `.gitignore`
  - 仅提交 `config.properties.template` 模板文件

- **生产环境配置**：
  - 生产环境应使用独立的配置文件
  - 密码应使用强密码
  - 数据库用户应使用最小权限原则

## 3. 数据库开发规范

### 3.1 代码规范

1. **数据库连接**：
   - 使用 `DBUtil.getConnection()` 获取数据库连接
   - 使用后务必关闭连接，避免资源泄漏
   - 示例：
     ```java
     Connection conn = null;
     try {
         conn = DBUtil.getConnection();
         // 数据库操作
     } catch (SQLException e) {
         e.printStackTrace();
     } finally {
         DBUtil.closeConnection(conn);
     }
     ```

2. **SQL语句**：
   - 使用参数化查询，避免SQL注入
   - 保持SQL语句简洁明了
   - 对于复杂查询，考虑使用视图或存储过程

3. **事务管理**：
   - 对于需要原子操作的业务逻辑，使用事务
   - 示例：
     ```java
     Connection conn = null;
     try {
         conn = DBUtil.getConnection();
         conn.setAutoCommit(false);
         // 多个数据库操作
         conn.commit();
     } catch (SQLException e) {
         if (conn != null) {
             try {
                 conn.rollback();
             } catch (SQLException ex) {
                 ex.printStackTrace();
             }
         }
         e.printStackTrace();
     } finally {
         if (conn != null) {
             try {
                 conn.setAutoCommit(true);
             } catch (SQLException e) {
                 e.printStackTrace();
             }
             DBUtil.closeConnection(conn);
         }
     }
     ```

### 3.2 命名规范

1. **表名**：
   - 使用小写字母
   - 单词之间用下划线分隔
   - 示例：`user`, `activity_participant`

2. **字段名**：
   - 使用小写字母
   - 单词之间用下划线分隔
   - 示例：`user_id`, `created_at`

3. **索引名**：
   - 主键索引：`PRIMARY KEY`
   - 唯一索引：`uk_表名_字段名`
   - 普通索引：`idx_表名_字段名`
   - 外键索引：`fk_表名_关联表名`

4. **SQL变量**：
   - 使用驼峰命名法
   - 示例：`userId`, `startTime`

### 3.3 性能优化

1. **索引优化**：
   - 为经常查询的字段添加索引
   - 为外键字段添加索引
   - 避免在索引列上使用函数

2. **查询优化**：
   - 只查询必要的字段
   - 使用 `LIMIT` 限制结果集大小
   - 避免使用 `SELECT *`

3. **批量操作**：
   - 对于大量数据操作，使用批量处理
   - 示例：使用 `PreparedStatement` 的批量执行

4. **连接池**：
   - 考虑使用连接池管理数据库连接
   - 合理设置连接池大小

## 4. 数据库部署与维护

### 4.1 环境要求

- **MySQL**：8.0+
- **JDBC驱动**：mysql-connector-java-8.0.28+
- **Java**：1.8+

### 4.2 数据库初始化

1. **创建数据库**：
   ```sql
   CREATE DATABASE software_group CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **导入数据**：
   - 使用 `database/` 目录下的SQL备份文件
   - 推荐使用最新的备份文件，如 `Dump20260307.sql`

3. **配置连接**：
   - 修改 `config/config.local.properties` 中的数据库连接信息
   - 确保数据库服务正在运行

### 4.3 数据库维护

1. **定期备份**：
   - 定期备份数据库，推荐使用 `mysqldump` 工具
   - 备份文件存储在 `database/` 目录下

2. **日志管理**：
   - 监控数据库错误日志
   - 定期清理 `operation_log` 表，避免数据过大

3. **性能监控**：
   - 监控数据库连接数
   - 监控慢查询日志
   - 定期优化表结构和索引

## 5. 常见问题与解决方案

### 5.1 连接问题

**症状**：无法连接到数据库
**解决方案**：
- 检查数据库服务是否正在运行
- 检查数据库连接配置是否正确
- 检查网络连接是否正常
- 检查数据库用户权限是否正确

### 5.2 权限问题

**症状**：执行SQL语句时出现权限错误
**解决方案**：
- 确保数据库用户拥有足够的权限
- 生产环境应使用最小权限原则
- 避免使用 `root` 用户直接连接

### 5.3 性能问题

**症状**：查询速度慢
**解决方案**：
- 为查询字段添加索引
- 优化SQL语句
- 考虑使用缓存
- 增加数据库服务器资源

### 5.4 数据一致性问题

**症状**：数据不一致或丢失
**解决方案**：
- 使用事务确保操作的原子性
- 定期备份数据库
- 实现数据验证机制

## 6. 辅助开发软件使用指南

### 6.1 TRAE 开发指南

1. **项目拉取**：
   - 拉取项目后，系统会自动识别配置文件结构
   - 提示创建 `config.local.properties` 文件

2. **数据库配置**：
   - TRAE 会自动读取 `config.properties.template` 作为模板
   - 提供交互式界面填写数据库连接信息
   - 自动生成 `config.local.properties` 文件

3. **开发辅助**：
   - TRAE 会根据数据库结构生成 DAO 层代码
   - 提供 SQL 语句编写辅助
   - 自动检查 SQL 语句语法

### 6.2 OpenCode 开发指南

1. **项目初始化**：
   - OpenCode 会自动检测项目结构
   - 识别数据库配置文件
   - 提示配置数据库连接信息

2. **代码生成**：
   - 根据数据库表结构生成实体类
   - 生成 DAO 层代码
   - 生成数据库操作示例代码

3. **部署辅助**：
   - 提供数据库迁移脚本生成
   - 提供数据库备份和恢复功能

### 6.3 ClaudeCode 开发指南

1. **项目分析**：
   - ClaudeCode 会分析项目结构和数据库配置
   - 识别数据库依赖关系
   - 生成数据库架构图

2. **代码辅助**：
   - 提供 SQL 语句优化建议
   - 生成数据库操作代码
   - 提供数据库性能优化建议

3. **测试辅助**：
   - 生成数据库测试用例
   - 提供数据填充脚本
   - 模拟数据库操作场景

## 7. 版本控制

### 7.1 配置文件管理

- **提交到Git**：
  - `config.properties.template` - 模板文件，必须提交
  - `.gitignore` - 确保敏感配置文件不被提交

- **不提交到Git**：
  - `config.local.properties` - 本地开发配置
  - `config.test.properties` - 测试环境配置
  - `config.prod.properties` - 生产环境配置

### 7.2 数据库结构变更

- **变更记录**：
  - 记录数据库结构变更
  - 生成变更脚本
  - 确保变更可回滚

- **版本管理**：
  - 数据库备份文件按日期命名
  - 保留多个版本的备份文件
  - 记录重要变更的说明

## 8. 总结

本文档提供了高校软件小组管理系统的数据库开发规范和指南，包括：

- 数据库结构概述
- 配置管理方法
- 开发规范和最佳实践
- 部署和维护指南
- 常见问题解决方案
- 辅助开发软件使用指南

遵循本指南，开发者可以：
- 快速理解数据库结构
- 正确配置数据库连接
- 规范开发数据库相关代码
- 有效维护数据库系统
- 利用辅助开发软件提高开发效率

同时，辅助开发软件（如TRAE、OpenCode、ClaudeCode）可以通过阅读本文档，理解项目的数据库结构和配置管理方式，从而提供更准确、更规范的开发辅助。