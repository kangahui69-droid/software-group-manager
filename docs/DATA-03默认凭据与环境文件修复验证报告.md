# DATA-03 默认凭据与环境文件修复验证报告

日期：2026-11-14

## 复核结果

问题存在。原 Compose 对 MySQL、Redis、RabbitMQ、MinIO 和 DES 使用固定值回退；JWT 已经是非空必填。`.gitignore` 不匹配根目录或子目录的 `.env`，旧 `config/docker.env.example` 带有可直接复制的默认密码。自动执行的 `sql/db_users.sql` 还会创建使用固定口令的微服务数据库账号。多个微服务的 `application.yml` 以及旧 WAR 默认配置也带固定密码。验证脚本和部署手册包含明文默认口令示例，冒烟测试还会输出 JWT 片段。

## 本地修复

1. Compose 对 `MYSQL_ROOT_PASSWORD`、`REDIS_PASSWORD`、`RABBITMQ_USER`、`RABBITMQ_PASSWORD`、`MINIO_ACCESS_KEY`、`MINIO_SECRET_KEY`、`JWT_SECRET`、`DES_KEY`、`AI_API_KEY` 采用非空必填插值。只在缺失必需值时输出变量名，不输出值。
2. `.gitignore` 与 `.dockerignore` 排除私有环境文件；根目录 `.env.example` 仅列空变量名。删除 `sql/db_users.sql`，将保留现有权限及表名修正的 `migrations/20261114_db_user_grants.sql` 放在 MySQL 自动初始化目录之外，且不含 `CREATE USER` 或 `IDENTIFIED BY`。`scripts/check-deployment-secrets.sh` 只读检查私有 `.env` 为普通文件、权限 `600`，并执行 `docker compose config --quiet`。
3. 微服务生产配置移除数据库、Redis、MinIO、旧 DES 的固定密码回退；旧 WAR 的生产模式优先读取必需的环境变量，未提供时抛出只含变量名的错误。Redis 健康检查改用 `REDISCLI_AUTH`，避免 `redis-cli -a` 的命令行警告与参数泄漏。
4. 现用部署手册和验证脚本删除固定密码示例；E2E 冒烟测试要求专用测试账号变量，且不输出 Token。历史设计文档中的旧示例并非生产配置来源，仍需在后续文档治理中清理。

## 本地验证

| 检查 | 结果 |
| --- | --- |
| Compose 缺失变量 | 九个必需变量逐一置空，`docker compose config --quiet` 均以非零退出 |
| Compose 完整测试变量 | 仅使用测试值，`docker compose config --quiet` 退出码为 0 |
| `.env` 忽略规则 | 根目录、子目录及 `config/*.env` 被忽略，`.env.example` 可跟踪 |
| MySQL 自动初始化目录 | 不再包含固定服务账号口令的 `db_users.sql`；权限迁移文件位于目录之外且不含密码 |
| WAR 配置与密码相关测试 | `ConfigTest`、`PasswordHasherTest`、`UserDAOTest` 通过；生产模式临时进程在缺少 `DB_PASSWORD` 时以退出码 2 拒绝，提供测试值时退出码 0 |
| 用户服务、文件服务测试 | 用户服务 45 项、文件服务 21 项，均为 0 失败、0 错误 |
| 八个受影响微服务打包 | 通过；未连接生产服务 |
| 多模块全量测试 | 在 `monitor-service` 的业务用例中 8 失败、1 错误后停止；`ai-service` 单独运行 114 项中 1 项缓存用例失败。涉及的失败类未修改，需另行排查；不能记为全量通过。 |
| Linux 预检脚本语法 | Git Bash `bash -n` 通过；未在本机模拟 Linux `chmod 600` 的真实权限行为 |

本机未运行 Docker 容器、未连接 VM、未修改生产 `.env` 或容器。

## 生产执行顺序

1. 在 Ubuntu VM `/opt/software-group` 检查现有 `.env` 是否存在，妥善备份；不要用空模板覆盖现有文件。将其权限设为 `600`，通过 `scripts/check-deployment-secrets.sh` 验证。不要运行会打印解析后密钥的 `docker compose config`；仅用 `config --quiet`。
2. 逐项盘点现有服务的实际凭据。对持久化的 MySQL 与 RabbitMQ，修改 `.env` **不会**自动修改数据库账户或消息队列账户；先在服务内完成受控轮换，再同步应用配置。若历史 `db_users.sql` 已执行，还需轮换其中创建的所有服务账号；迁移文件只赋权，不创建账号。Redis 与 MinIO 的切换要安排服务级维护窗口，并验证所有客户端连接。不要把新密码放进命令参数、终端录屏、日志或工单正文。
3. 更新全部服务使用的 JWT 密钥并重新部署相关容器，旧令牌将失效；核对登录、跨服务授权和注销。旧 DES 密钥必须保持原值，直到 DATA-02 的旧格式账户全部迁移并移除兼容代码，过早更换会导致旧账户无法验证。
4. 分批发布并验收 MySQL、Redis、RabbitMQ、MinIO 和应用连接；核对健康状态及真实业务读写。确认旧凭据已无法使用，然后清理受控备份中的旧密钥副本。全程保持其他未变更容器运行。

**当前状态：本地代码与配置已修复，生产凭据轮换和 VM 权限核验未执行。** Nacos 关闭认证另列为 OPS-02，不在本问题内宣称解决。
