# MIG-03 AI 会话持久化修复验证报告

日期：2026-11-15。范围：`ai-service` 本地代码；未操作生产 VM。

## 复核结论

生产代码中的 `AIConversationDAOImpl` 和 `AIMessageDAOImpl` 均使用进程内 `ConcurrentHashMap`，并作为 `@Repository` 默认启用。容器重启会同时丢失会话和消息，多实例也不共享。原 `AIService` 对已找到的会话检查用户归属，但历史查询会捕获所有 DAO 异常并返回 `null`，掩盖数据库故障。

## 本次修复

- 新增会话与消息的 Spring JDBC 实现，默认 Profile 使用 MySQL；内存实现限定于 `test` Profile。新会话和消息读取数据库自增 ID，历史按创建时间和 ID 排序。
- 为 `ai_conversation.session_id` 增加唯一索引；跨实例同时创建时，后到的实例重新读取记录并再次核对归属，不会把别人的会话当作自己的。提供现有库的手动迁移脚本，先查重再建索引。
- 历史读取与清空明确校验会话 ID 和当前用户；其他用户读取、清空或占用同一会话返回 403。历史查询不再吞掉数据库异常。清空会话和聊天写入使用事务。

## 验证结果

在本机临时 MySQL 8 的 `mig03_test` 库中导入无业务数据结构基线，并验证唯一索引存在。集成测试清理数据前强制确认数据库名为 `mig03_test`。

执行 `mvn -o -pl ai-service -am test -q`：**AI 服务 119 项、公共模块 15 项，共 134 项通过，0 失败、0 错误、0 跳过**。其中 3 项真实 MySQL 集成测试覆盖新服务对象读取既有会话和消息、用户隔离、唯一约束和事务回滚；2 项 Profile 测试确认默认 JDBC、`test` 内存。执行 `mvn -o -pl ai-service -am -DskipTests package -q` 通过，生成服务 JAR。

## 生产实施前提与另外发现的问题

1. 先备份现有数据库，并执行 `SELECT session_id, COUNT(*) FROM ai_conversation GROUP BY session_id HAVING COUNT(*) > 1`。重复值须查明归属并处理，之后手动执行 [MIG-03 唯一索引迁移](../migrations/manual/MIG-03_ai_conversation_unique_session.sql)。不能直接部署依赖唯一索引的代码而跳过迁移。
2. 旧容器的内存会话与消息无法从 MySQL 恢复；切换时应告知用户旧会话已失效，并做生产读写、重启和越权回归。本次的“重启”测试是重新创建服务和 DAO 对象、复用同一 MySQL 数据库，未重启生产容器。
3. 另外核实：`AIKnowledgeBaseDAOImpl`、`AIFaqStatisticsDAOImpl` 和 `AIMessageStatusDAOImpl` 仍在默认生产 Profile 使用 `ConcurrentHashMap`。它们不是本次会话/消息范围，数据持久化风险仍在，应单独确认后修复。

结论：**MIG-03 的会话与消息 DAO 已在本地代码中改为默认 MySQL 持久化；生产迁移和部署尚未执行。**
