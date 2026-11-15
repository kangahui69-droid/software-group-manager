# MIG-04 网关路由隔离与验证报告

> 本文保留首次本地修复时的验证记录。后续已完成本机 Nginx 运行测试、端口限制和生产入口只读核实，当前状态见 [4.3 剩余问题修复与本地验证报告](4.3剩余问题修复与本地验证报告.md)。

## 问题复核

原 `nginx/nginx.conf` 将 `files`、`users`、`members`、`activities`、`attendance`、`study`、`projects`、`awards`、`recruit`、`resumes`、`problems`、`logs`、`groups`、`news` 的 `/api/*` 请求直接发往微服务，并将 `/api/ai/*` 发往 AI 服务。此前 MIG-01～03 的修复仅在本地，生产容器仍是旧版本；AI 服务还有其他内存 DAO。因此该 P0 风险确实存在。

旧 WAR 部署在 `/software-group` 上下文。`web.xml` 或 Servlet 注解覆盖了上述域中除 `members` 外的 13 个域。`/api/members` 和 `/api/ai` 没有等价的旧 WAR 业务入口，不能假装回退成功。

## 本地处理

1. Nginx 将 `/api/` 默认转到 `/software-group/api/` 旧 WAR，保持其余旧页面路径；`/api/members`、`/api/ai` 及子路径直接返回 JSON 503，并加 `X-SG-Route` 标记。
2. `/microservices/health` 只说明代理可返回响应和当前路由策略，返回 `businessValidated:false`，不再伪称所有业务服务健康。
3. `scripts/verify-routes.sh` 只发 GET，请求 13 个回退域的根路径与带尾斜杠路径，核对路由标记和 HTTP 状态；404、5xx、连接失败均不算通过；另外核对两个禁用域和健康说明。
4. `scripts/verify-db-backed-services.sh` 只允许指向本机 `mig01_test`、`mig02_test`、`mig03_test` 库，调用已有的 MySQL 集成测试，作为数据写入后读取的独立发布门槛。

## 本次验证

- 静态路由策略测试：`python -m unittest -q scripts/test_mig04_route_policy.py`，7 项通过。
- Bash 语法检查：对两个脚本执行 `bash -n`，通过。
- `git diff --check`，通过；仅有 Git 的行尾转换提醒。
- 本机没有可用的 Nginx 运行环境，因此本次未执行真实 `nginx -t` 或经网关的 HTTP 联调；本次也未启动隔离 MySQL，未重新运行数据库集成测试。

## 上线前门槛与范围

1. 在候选配置对应的 Nginx 容器执行 `nginx -t`；失败不得 reload。保留旧配置以便快速恢复。
2. 经真实入口运行只读路由检查，并使用普通用户与管理员分别核对页面和 API 的登录、列表、上传、项目等契约。外部 JWT 客户端可能与旧 WAR 的会话认证不兼容，需逐一验收。
3. 在隔离数据库运行写入后查询测试；再用测试账号在目标环境检查关键业务链路。健康端点只能证明代理自身响应，不能替代这些检查。
4. 生产 VM、容器、数据库、远端分支本次均未改动。Docker Compose 中直接发布的微服务端口仍可能绕过 Nginx，本问题未调整这些端口映射，后续须单独核实入口暴露策略。
