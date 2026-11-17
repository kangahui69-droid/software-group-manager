# 业务数据库 Flyway 迁移说明

## 范围与版本

`db-migrations/sql/V1__baseline_schema.sql` 是当前 `software_group` 的脱敏、无业务数据结构快照，含 45 张业务表。它与 `sql/software_group.sql` 内容一致；V1 发布后不可修改。后续结构变更按 `V2__description.sql`、`V3__description.sql` 递增，禁止直接修改 V1 或只在生产手工执行 SQL。既有 `migrations/` 下的手工脚本只供核对旧库状态，不会被 Flyway 再次执行；已包含在 V1 中的变更尤其不能重复执行。

Flyway 11.19.1 只管理 `software_group`。`nacos_config` 是独立数据库，继续按 Nacos 自身的建库流程处理，不能把 Nacos SQL 放入 `db-migrations/sql`。DB-03 使用的授权脚本是 `migrations/20261117_db03_runtime_grants.sql`，它不属于业务结构迁移；旧的 `migrations/20261114_db_user_grants.sql` 已停用。

## 全新数据库

MySQL 容器只创建空的 `software_group` 库，不再从 `/docker-entrypoint-initdb.d` 执行结构脚本。`docker compose up -d` 会先等 MySQL 健康，再运行一次 `db-migrate`；它成功后，旧 WAR 和八个微服务才会启动。Flyway 创建结构并在 `flyway_schema_history` 记录 V1。不要把 V1 再手工导入同一空库。

全新环境仍需单独创建和初始化 `nacos_config`，再启动 Nacos；见《Windows Server生产部署完整手册》§19.4。Nacos 的 12 张表不属于 Flyway 业务库。完整环境第一次启动前应先完成该独立步骤。

## 已有生产库首次接入（先备份和核对）

本仓库没有生产库实时状态，不能自动为它建立 baseline。正式发布前先核对当前数据库备份已恢复验证，并在同一 MySQL 版本的隔离副本上演练。对照 V1 核查全部 45 张表、字段类型/空值/默认值、索引与约束；尤其确认 `user.password` 为 `VARCHAR(255)`、项目分页索引、`operation_log` 的结果/耗时/关联字段和 `proxy_ip`、`ai_conversation.uk_ai_conversation_session_id` 都已存在。`information_schema` 的表数/列数检查只能初筛，不能代替结构逐项比较。缺少任何变更时，先按原手工脚本及其前置数据检查补齐，切勿直接 baseline。

只有结构与 V1 一致后，在生产 VM 的 `/opt/software-group` 中显式记录 V1；此操作会写入 `flyway_schema_history`，但不重建业务表：

```bash
docker compose run --rm --no-deps db-migrate mvn -B -ntp -f /migrations/pom.xml -Dflyway.baselineVersion=1 flyway:baseline
docker compose run --rm --no-deps db-migrate mvn -B -ntp -f /migrations/pom.xml flyway:validate
```

随后才发布包含本次 Compose 改动的版本。先运行 `docker compose run --rm --no-deps db-migrate` 并确认其退出码为 0、`flyway_schema_history` 为成功的 V1，再启动或重建应用服务。既有容器不能在结构未经核对时直接用新 Compose 重建。全程不删除 MySQL 数据卷。

## 日常新增迁移

每次只新增一个不可变的 `V数字__说明.sql`，在空库和现有库副本上分别执行 `migrate`、`validate`；需要数据回填时编写可审查的独立迁移，注明锁表和回滚方案。已发布文件不改写。生产每次发布新版应用前，显式运行 `docker compose run --rm --no-deps db-migrate` 并检查退出码，再运行 `docker compose up -d`。不要仅依赖先前已成功退出的 `db-migrate` 容器；挂载目录中新增 V2 文件本身未必触发 Compose 重建该容器。迁移失败时停止应用发布，保留备份并按 Flyway 历史表和数据库实际结构排查；不要启用 `baselineOnMigrate` 或直接运行 `repair`/`clean`。

本地或 CI 使用同一 Maven 模块，连接参数通过 `FLYWAY_URL`、`FLYWAY_USER`、`FLYWAY_PASSWORD` 环境变量提供；例如：

```bash
mvn -B -ntp -f db-migrations/pom.xml flyway:migrate flyway:validate
```

CI 的 `.github/workflows/db-migrations.yml` 覆盖空库创建、非空无历史库拒绝自动迁移、显式 baseline 和临时 V2 升级。生产 VM 的实际 baseline 与发布仍需要单独验收。
