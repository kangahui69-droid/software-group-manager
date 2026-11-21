# OPS-04：健康检查、指标采集与业务合成验收

## 再次确认

修复前 `nginx/nginx.conf` 的 `/health` 固定返回 200，Compose 对 Nginx 的健康检查也只访问该路径。旧 WAR、MySQL 或 Redis 故障时，Nginx 仍可显示健康。`/microservices/health` 仅报告路由策略，响应中的 `businessValidated:false` 已说明它不是业务就绪检查。八个微服务虽有 Actuator，但此前没有 Prometheus 指标端点或直接采集配置。

## 本地修复

- `GET/HEAD /health`：仅表示 Nginx 进程能处理请求，不代表业务可用。
- `GET/HEAD /ready`：只允许 Nginx 容器内回环地址访问，代理到旧 WAR 的 `/internal/ready`。该 Servlet 对 MySQL 执行 `SELECT 1`，对 Redis 执行 `PING`；任一依赖不可用返回 503，Nginx 上游不可用返回 502/504。Compose 的 Nginx 健康检查改为访问 `/ready`。旧 WAR 的内部探针路径从公开 Nginx 路由返回 404。
- 当前 API 路由仍指向旧 WAR，因此 Nginx readiness 只检查实际承载业务的旧 WAR 及其必要依赖；八个微服务由各自容器健康检查和 Prometheus 独立监控。以后切流到微服务时应重新评估 readiness 依赖集合。
- 八个微服务新增 `micrometer-registry-prometheus` 并开放 `/actuator/prometheus`。可选 `monitoring` Compose profile 的 Prometheus 从 Docker 内网直接抓取八个目标，**不发布 9090 到宿主机**。配置位于 `monitoring/prometheus.yml`。
- `scripts/verify-gateway-business.sh` 从 HTTPS Nginx 入口读取登录页面及 CSRF token，使用专用低权限账号登录，再读取“我的项目”API 并验证业务响应。脚本不打印密码、Cookie 或响应数据，业务层只读；登录本身会产生 Session 和操作日志。

## 生产发布与验收

生产 VM 目前未应用 OPS-01 至 OPS-04 的本地配置，且 OPS-03 证书尚未准备。必须先完成 4.6 数据库迁移、OPS-02 Nacos 账号、OPS-03 证书和客户端信任，再按发布窗口更新 WAR、八个微服务和 Nginx；本项不要求现在停止或重启服务器。

发布后在 VM 内验证：

```bash
docker compose config --quiet
docker exec sg_nginx nginx -t
docker exec sg_nginx wget --no-check-certificate -qO- https://127.0.0.1/health
docker exec sg_nginx wget --no-check-certificate -qO- https://127.0.0.1/ready
docker compose ps nginx
```

预期 `/health` 为 `OK`，`/ready` 为 `READY`，Nginx 容器显示 healthy。来自 VM 外部的 `https://<VM_IP>/ready` 应返回 403，`https://<VM_IP>/software-group/internal/ready` 应返回 404；这些响应不能暴露数据库和 Redis 细节。在隔离演练环境中让旧 WAR 或依赖不可用，确认 `/health` 保持 200，而 `/ready` 返回非 2xx、Nginx 健康状态转为 unhealthy；恢复后状态应回到 healthy。生产环境不通过停服务做此演练。

如需启动内部指标采集，在发布窗口中执行：

```bash
docker compose --profile monitoring up -d prometheus
docker exec sg_prometheus promtool check config /etc/prometheus/prometheus.yml
docker exec sg_prometheus promtool query instant http://127.0.0.1:9090 'up{job="software-group-microservices"}'
```

等待至少一个抓取周期（默认 30 秒），预期八个目标的 `up` 都为 1。任何目标为 0，应检查对应容器的 `/actuator/prometheus`、网络和应用日志。Prometheus 本身没有宿主端口；远程查看应使用受控运维通道，不能为了方便把 9090 直接发布到外网。告警通知需接入现有运维告警平台，不能仅把 `up` 指标视为已送达的告警。

从已信任 OPS-03 内部 CA 的客户端执行关键业务合成检查。先准备一个**无管理权限、已完成首次改密**的专用账号，不要把账号密码写进仓库：

```bash
read -r -s SMOKE_PASSWORD < /secure/path/smoke-password
export SMOKE_PASSWORD
BASE_URL=https://<VM_IP> CURL_CA_BUNDLE=/secure/path/ca.crt SMOKE_USERNAME=<专用账号> \
  bash scripts/verify-gateway-business.sh
unset SMOKE_PASSWORD
```

预期脚本报告 `Gateway business smoke PASS`。它覆盖 TLS 入口、登录 CSRF、Session Cookie 和项目只读查询。可在运维监控中定时执行，并对非零退出报警；先验证告警确实送达。运行频率需考虑登录操作日志与 Session 数量。

## 本地验证边界

本地可验证 Java 单元测试、八服务构建与测试、Compose 解析和监控配置结构。由于本机 Docker 守护进程未启动且生产 VM 未授权访问，Nginx `nginx -t`、Prometheus `promtool`、真实依赖故障演练及外部 HTTPS 合成检查只能在 CI 或生产发布验收时完成，不能把本地配置通过视为线上已生效。
