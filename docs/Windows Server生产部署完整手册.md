# Windows Server 生产部署完整手册（Hyper-V Ubuntu VM）

> **当前唯一部署路线**：Windows Server 宿主机 → Hyper-V 中的 Ubuntu VM `sg-linux` → VM 内 Docker Engine / Docker Compose。Windows 主机只负责虚拟化、文件中转和异机备份，不在 Windows Docker 中运行本项目的 Linux 镜像。
>
> **文档与版本核对**：2026-11-18；最后一次核对的 GitHub `Version-2` 基线为 `1befb72ee49199c7b8681f55aa929707fb9dc339`。本地已新增尚未推送的主题提交，仍有必要文档和 QA-04 工具待整理；实际本地 HEAD 以 `git rev-parse HEAD` 为准。最近一次已记录的生产容器基线为 2026-11-09；当前候选版本尚未部署，生产运行的 Git SHA 和本节发布步骤尚未在线验证。执行发布时须重新记录实际 VM IP、Git SHA、镜像 ID 和验收时间。
>
> 旧 Windows 主机 Docker、DockerCli 切换、Windows 防火墙端口表、`C:\software-group` 作为容器项目目录，以及早期 VM 安装过程已移入[历史附录](历史附录-Windows主机Docker与早期VM部署.md)。第十九章保存后续生产排查过程和专项补充，其中 2026-09/10 的“下一步”与旧 HTTP 命令是历史记录；当前操作从本页的步骤和专项验收文档进入。

## 当前架构、目录和访问入口

| 位置 | 用途 | 当前应使用的路径或规则 |
|---|---|---|
| Windows Server 宿主机 | 运行 Hyper-V、保存异机备份、必要时中转制品 | `C:\software-group` 可作受控中转；`C:\software-group\backup` 保存异机备份。两者都不是 Docker 项目目录或 Linux 容器挂载源 |
| Ubuntu VM `sg-linux` | 唯一运行 Docker Engine/Compose 的主机 | `/opt/software-group` 为 Compose 项目目录；私有 `.env` 位于该目录且权限应为 `600` |
| Ubuntu VM 私有目录 | HTTPS 证书和 CA 私钥 | `/opt/software-group-private`；证书挂载路径由私有 `.env` 的 `TLS_CERT_DIR` 指定，私钥不进 Git |
| Docker 命名卷 | MySQL、Redis、MinIO、旧 WAR 上传文件等持久化数据 | 由 VM 内 Docker 管理；不要把 Windows `C:\` 路径写成 Linux bind mount |
| 对外入口 | 目标版本仅 Nginx 发布宿主端口 | 80 跳转到 443，业务使用受信任内部 CA 的 HTTPS；内部容器仅走 Docker 网络 |

2026-11-10 从 Windows 宿主机实测可连接的 VM 地址曾为 `192.168.65.33`，更早的 `192.168.0.15` 已不可达。两者都不能作为固定地址；每次部署前在 VM 核对实际 IP，并通过静态地址或 DHCP 保留稳定下来。目标版本的 HTTPS、端口收敛、Nacos 认证、数据库独立账号尚未在生产切换；此前的 `docker compose ps` 输出不能证明这些改动已上线。

## 发布前核对与顺序

1. 在 VM 中核对当前版本、运行容器和私有配置，不把 `docker compose config` 的解析结果打印到终端或日志。若 VM 不是 Git 工作区，记录制品来源的提交 SHA 与镜像 ID。
2. 在 VM 完成 MySQL `software_group` 与 `nacos_config`、MinIO、`.env` 和 Compose/Nginx 配置备份；将备份复制到 Windows 宿主机并核对 SHA-256。备份含个人数据和密钥，只放在受控目录。
3. 已有生产库先按[业务数据库 Flyway 迁移说明](业务数据库Flyway迁移说明.md)完成结构比对、显式 baseline，再按[数据库运行账号切换说明](数据库运行账号切换说明.md)准备独立账号。全新环境另需初始化 `nacos_config`；第十九章 §19.4 保存了历史过程，执行前仍须核对当前 SQL 和配置。
4. 按[OPS-03 HTTPS 部署与验收](OPS-03-内网IP-HTTPS部署与验收.md)准备证书及客户端信任，再按[OPS-02 Nacos 认证切换与验收](OPS-02-Nacos认证切换与验收.md)和[OPS-01 端口收敛与防火墙验收](OPS-01端口收敛与防火墙验收.md)安排发布窗口。不要直接把本地 Compose 套到未 baseline 的旧库。
5. 候选版本发布后按 §19.12 采集整栈验收，再进行登录、项目、日志、上传下载及微服务实际业务读写验收。页面可达或容器 `healthy` 不能代替这些检查。

发布版本的源码提交、容器镜像 ID/digest 与 Flyway 迁移版本须按 [QA-04 版本基线与发布追踪](QA-04版本基线与发布追踪.md) 留证。当前生产镜像来源尚未核实，不得将本地 HEAD 直接标记为生产版本；发布构建前先在干净的 `Version-2` 工作区运行 `scripts/qa04_preflight_release.sh`。

以下只读命令均在 **Ubuntu VM** 的 shell 执行；不要在 Windows PowerShell 中运行：

```bash
cd /opt/software-group
pwd
git rev-parse HEAD
docker compose ps
docker compose config --quiet
df -h /opt/software-group
```

## Portainer 与监控边界

当前仓库的 `docker-compose.yml` 没有 Portainer 服务，也没有 `docker-compose.ops.yml`；不能照历史附录中的 Windows Compose 示例启动它。若以后单独批准在 VM 部署 Portainer，Docker socket 的 Linux 路径是 `/var/run/docker.sock:/var/run/docker.sock`，Portainer 数据应使用 Linux Docker 命名卷挂到 `/data`。历史示例的 `//./pipe/docker_engine` 仅属于 Windows Docker，不适用于 Ubuntu VM。Docker socket 可控制宿主 Docker，管理入口必须限制在受控运维网络；本手册不将 9443/9001 视为当前已开放端口。

可选监控方案以仓库现有 `monitoring` profile 和[OPS-04 健康检查与业务合成验收](OPS-04-健康检查与业务合成验收.md)为准；历史附录中的 `docker-compose.monitoring.yml`、`docker-compose.logging.yml` 和 Portainer 文件并不存在于当前仓库。

## 备份位置与命令差异

下列示例只在 **Ubuntu VM** 执行，先用上面的 `df -h` 核对可用空间，再生成新的受限备份目录。任一步返回非零即停止备份和后续发布，排查后重做；示例中的 `set -e` 会在失败时退出当前 shell。`docker exec` 不加 `-t`，保证 SQL 重定向和归档流不经过伪终端；MySQL 密码从容器环境读取，不写入终端参数。MinIO 的 `docker cp ... -` 直接输出 tar 流，无须临时拉取 Alpine 镜像。生产对象存储正在写入时须安排静默窗口或采用一致性快照，并通过隔离恢复演练验证备份。

```bash
cd /opt/software-group
umask 077
set -e
set -o pipefail
backup_dir="/opt/software-group/backup/pre-release-$(date +%Y%m%d_%H%M%S)"
mkdir -p "$backup_dir"
docker exec sg_mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysqldump -uroot --single-transaction --quick --routines --triggers --events --set-gtid-purged=OFF --databases software_group nacos_config' > "$backup_dir/mysql.sql"
test -s "$backup_dir/mysql.sql"
docker cp sg_minio:/data - | gzip > "$backup_dir/minio-data.tar.gz"
gzip -t "$backup_dir/minio-data.tar.gz"
tar -tzf "$backup_dir/minio-data.tar.gz" > /dev/null
tar -czf "$backup_dir/deployment-config.tar.gz" .env docker-compose.yml Dockerfile.tomcat nginx/nginx.conf
(cd "$backup_dir" && sha256sum mysql.sql minio-data.tar.gz deployment-config.tar.gz > SHA256SUMS)
```

`backup_dir` 变量仅在上述 VM shell 中有效。复制到 Windows 时，用 **Windows Server PowerShell** 新建接收目录并传输该次备份的实际文件夹名；不要把 VM 的 `/opt/...` 路径当作 Windows 本地路径，也不要把 `C:\software-group\backup` 绑定到 VM 容器：

```powershell
New-Item -ItemType Directory -Force -Path 'C:\software-group\backup' | Out-Null
scp -r 'ubuntu@<VM_IP>:/opt/software-group/backup/<实际备份目录名>' 'C:\software-group\backup\'
Get-FileHash -Algorithm SHA256 -LiteralPath 'C:\software-group\backup\<实际备份目录名>\mysql.sql'
```

将 Windows 的 `Get-FileHash` 与 VM 备份目录里的 `SHA256SUMS` 对照；MySQL、MinIO 和配置文件均要核对。2026-11-10 的整包异机校验记录见[修复执行计划的生产基线](项目问题汇总与整体修复执行计划.md)，不能当作本次发布的新备份。恢复操作先在隔离环境演练，不在运行中的生产库直接导入。旧 Windows Docker 命令中的 `docker run -v C:\...`、PowerShell `$(Get-Date)` 和 Linux `tar` 混用方式已归档，不作为当前命令。

## 发布后验收与历史记录

在 VM 上执行[步骤 6 验收采集脚本](../scripts/collect-ops07-acceptance.sh)，保存带时间、版本、退出码和原始日志的证据；具体前置条件和当前待执行状态见 §19.12。生产 VM 尚未应用本地修复时，不把此处目标端口、证书或新脚本写成已经验收。

---

> 以下第十九章保留 2026-09 至 2026-10 的部署过程与 2026-11 的专项补充。历史操作与当时的 IP、密码变量、端口、容器状态应以当时日期解读；执行新发布请从上方当前路线开始。

## 十九、微服务部署进度与后续步骤（追加：2026-09-28）

> **历史断点（2026-10-02）**：当时步骤 2 已完成，下一步是构建 8 个微服务；这不是 2026-11-18 的部署进度。
> 步骤 2 长期卡在 nacos 起不来，根因是**内嵌 Derby 在 Hyper-V 慢盘上首次建库 >30s，越过 Derby 默认 30s 登录超时**；
> 已改为 **nacos 持久化切 MySQL**（详见 §19.4 步骤 2 的「✅ 已解决」与「步骤 2 收尾改动」），nacos 现为 `(healthy)` + `200`。
> **当时的下一步**：按 §19.4 步骤 3 构建 8 个微服务镜像（`COMPOSE_PARALLEL_LIMIT=2`，最慢一步，首次 20~40 分钟）。
>
> 追加日期：2026-09-28。本节保存当时的排查与部署顺序；当前路线和真实待验收状态以上方入口及 §19.7–§19.12 为准。
> 详细修复过程与逐条证据记录在 `docs/旧WAR_Tomcat10修复与部署方案.md`（同目录）。
> 本节结构：19.1 环境事实 → 19.2 已完成 → 19.3 已知待办 → 19.4 接下来（步骤 0-6）→ 19.5 风险 → 19.6 快速定位。

### 19.1 环境事实（2026-09-28 实测）

| 项 | 值 |
|---|---|
| 部署执行环境 | Windows Server 主机上的 **Hyper-V Ubuntu VM**，VM 名 `sg-linux` |
| VM IP | `192.168.0.15` |
| 项目目录 | VM 内 `/opt/software-group` |
| 传输链路 | 开发机 → 向日葵 → Windows Server `C:\software-group\` → scp → VM |
| 当前访问入口 | `http://192.168.0.15:8080/software-group/...`（旧 WAR 直连，**不含 nginx**） |

### 19.2 已完成（截至 2026-09-28）

#### ① 旧 WAR 的 jakarta 迁移遗留 —— 已全部修复并验证跑通

背景：2026-08-02 那次 jakarta 迁移（commit `4ad3474`）只改了 Java 源码和根 `pom.xml`，WAR 的**运行环境三处没跟上**，导致 `app` 容器必然起不来。

| 处 | 修复前 | 修复后 |
|---|---|---|
| `Dockerfile.tomcat` | `FROM tomcat:9-jdk11` | `FROM tomcat:10.1-jdk17-temurin` |
| `pom-war.xml` | `javax.servlet:jstl:1.2` | `jakarta.servlet.jsp.jstl:*-api:3.0.2` + `org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.1` |
| 86 个 JSP / 122 处 taglib URI | `http://java.sun.com/jsp/jstl/*` | `jakarta.tags.core` / `jakarta.tags.fmt` |

同批还修掉（原计划阶段 B）：

| 文件 | 改动 |
|---|---|
| `src/main/java/config/Config.java` | 新增**运行期** `${VAR:-default}` 解析，兜住此前没被替换的 `REDIS_*` / `RABBITMQ_*` / `AI_API_SECRET` 等占位符 |
| `docker-compose.yml` | `app` 的 `build.args` 改到 `environment:`（配合上一条）；`DB_PASSWORD` 变量名统一为 `MYSQL_ROOT_PASSWORD` |
| `.dockerignore` | `target/` → `target/*`，让 WAR 能进构建上下文 |
| `src/main/webapp/WEB-INF/web.xml` | 描述符升到 jakarta `web-app_6_0.xsd` + `version="6.0"` |

**验证结果（阶段 C 通过，2026-09-23）**：容器内 `Starting Servlet engine: [Apache Tomcat/10.1.60]`；历史测试账号登录后 **302 → /index.jsp**；旧实现写入 Redis 的 `session:*` 键（`dbsize=1`，不代表能跨实例恢复）；DB **45 张表**齐全；Windows Server 浏览器可访问 `192.168.0.15:8080`。历史凭据已弃用，生产凭据不得写入手册。OPS-05 本地修复已移除旧过滤器，生产尚未部署；新版本旧 WAR 的 Session 由单实例 Tomcat 管理。

#### ② 当时排查过的两个「非缺陷」（记下来，避免以后重复排查）

- **`/software-group/admin/index.jsp` 报 403** —— 是 `AuthFilter` 的**正确行为**：未登录是 302，登录了但角色非 ADMIN 才是 403。用 `admin` 账号登录后正常。
- **日志里 25 条 `activity_group doesn't exist`** —— 发生在零表窗口期，由 `GroupMuteScheduler` 每 60 秒探测产生，属**历史错误**；现在计数不再增长，表也已存在。

#### ③ 当前容器实况

`docker ps` 只有 4 个容器，且 `docker ps -a` 输出与之**完全一致** → 其余服务**从未创建过**（不是"停了"）：

```
sg_app        software-group-app              Up 4 days
sg_rabbitmq   rabbitmq:3-management-alpine    Up 5 days (healthy)
sg_redis      redis:7-alpine                  Up 5 days (healthy)
sg_mysql      mysql:8                         Up 5 days (healthy)
```

镜像仓库里**中间件已齐**（`nacos/nacos-server:v2.2.3`、`bladex/sentinel-dashboard:latest`、`minio/minio:latest`、`nginx:alpine`），但 **8 个微服务一个镜像都没有**（compose 里全是 `build:`，必须现场构建）。构建所需的 `maven:3.9-eclipse-temurin-17` 与 `eclipse-temurin:17-jre-alpine` 两个基础镜像也**尚未拉取**。

> 待解疑点：本机镜像列表里**没有 `tomcat:10.1-*`**，但运行中的 `sg_app` 自报 Tomcat 10.1.60。下次可用 `docker history software-group-app --no-trunc | tail -5` 看基础层。

### 19.3 已知待办（均未修；不阻塞当前部署，但影响「可重复部署」）

| # | 问题 | 影响 |
|---|---|---|
| 1 | `sql/` 目录按**字母序**执行，而 `db_users.sql` 排在 `software_group.sql` **之前**，且含表级 `GRANT` → 首条即 `ERROR 1146`，**mysql entrypoint 中止整个初始化**；`restart: always` 带非空 datadir 重启后初始化**永久跳过、表数 0** | **全新部署必复现**；当前环境因是既有数据卷，没踩到 |
| 2 | `sql/db_users.sql` 引用了 5 个**单数**表名（`resume`/`resume_award`/`resume_education`/`resume_project`/`resume_skill`），schema 实际是**复数** | 授权不生效。但 compose 里 9 个 `DB_USER` **全是 `root`**，这些 `*_svc` 账号**根本没被使用**，故无实际影响 |
| 3 | `mysql` healthcheck 缺 `start_period` | 冷启动可能被误判不健康 |
| 4 | `mysql:8` 是浮动 tag（已漂到 8.4.11） | 不可复现 |
| 5 | `src/main/webapp/META-INF/context.xml` 里的 `<Context path="/"/>` | 死代码（Tomcat 忽略 WAR 内的 `path` 属性），可删 |
| 6 | `sql/software_group.sql` 混了坏 UTF-8（部分 COMMENT / 种子行） | 显示乱码 |
| 7 | 108 个测试失败（疑似本机 JDK 23 与 Mockito 4.11 不兼容，**未验证**） | 不阻塞部署 |
| 8 | **96 个改动尚未 commit** —— 阶段 B 的全部产出目前**只存在于工作区** | **风险最高**，一次误操作即全丢 |
| 9 | `.env` **不在 `.gitignore` 里** | 明文 `MYSQL_ROOT_PASSWORD` 有被误提交的风险 |

可回收空间：`docker rmi tomcat:9-jdk11`（652MB）+ `docker rmi nacos/nacos-server:v2.2.0`（1.63GB）≈ **2.28GB**。

### 19.4 历史部署过程：8 个微服务（步骤 0 → 6）

> 本节记录 2026-09/10 的命令和当时结果。当前版本已引入 Flyway、独立数据库账号、Nacos 认证、HTTPS 与端口收敛，不能直接复制本节的旧启动、密码、HTTP 或宿主端口命令；请先按文首当前发布顺序核对。

> **总前提：绝对不要执行 `docker compose down -v`。** 数据卷一删就会触发待办 #1，45 张表全没。

#### 执行进度（每完成一步在此打勾）

| 步骤 | 状态 | 完成日期 | 结果摘要 |
|---|---|---|---|
| 步骤 0 确认源码与资源 | ✅ 已完成 | 2026-09-29 | 8 个 `*-service/` + `common/` + `nginx/` + `static/` 全在，14 项关键文件零 `MISS`；VM 内存上限由 8GB 提到 **12GB**；清掉 2 个僵尸镜像后磁盘可用 **≥15G** |
| 步骤 1 修 `sql/` 文件名顺序（可选） | ✅ 已完成 | 2026-09-29 | 10 处表名全改复数；`db_users.sql` → `zz_db_users.sql`（排序置后）；`verify-infrastructure.sh` 同步更新，md5 与本地复现值字节级一致 |
| 步骤 1.5 行尾规整（CRLF→LF） | ✅ 已完成 | 2026-09-29 | VM 整棵树是 CRLF（仓库是 LF，传输链路转的）。已修 `scripts/*.sh`（尺寸 6619/6313/7848 全对）与 **8 个 `*-service/Dockerfile`**（避免 `HEALTHCHECK ... \` 续行被 CR 打断而搞崩步骤 3）；`Dockerfile.tomcat` 本就是 LF。`.env` **实测 compose 会剥掉 CR**（文件 11 字节 → 容器内 10 字节），故 CRLF 无害，保持不动 |
| 步骤 2 起中间件层（nacos/sentinel/minio） | ✅ 已完成 | 2026-10-02 | sentinel ✅ 200、minio ✅ 200、**nacos ✅ `(healthy)` + 200**。nacos 长期卡死的根因＝**内嵌 Derby 在 Hyper-V 动态扩展 VHDX（慢盘）上首次建库 >30s，越过 Derby 默认 30s 登录超时**（`SQLTimeoutException: Login timeout exceeded`），建库失败留下缺 `service.properties` 的残破 `derby-data` → 崩溃循环。调超时参数 / 删目录 / 重建 volume 均无效，**最终改为 nacos 持久化切 MySQL 后一次起通**（改动见 §19.4「步骤 2 收尾改动」） |
| 步骤 3 构建 8 个微服务 | ✅ 已完成 | 2026-10-02 | 8 个镜像齐全（`docker images \| grep service \| wc -l` = **8**），SIZE **467–511MB**（=`eclipse-temurin:17-jre-alpine`≈180MB + Spring Boot fat jar），JAR 确已打入。本次 `docker compose build` 全命中缓存（`Building 8/8`、每条 `Built 0.0s`）——即**镜像已存在且与当前源码一致**，无须真编译 |
| 步骤 4 启动 8 个微服务 | ✅ 已完成 | 2026-10-07 | 8 个容器 `Up (healthy)`，Nacos 服务列表 **`count=8`**（`hr/user/content/file/monitor/ai/activity/project-award` 全齐）。三个坑逐一定性并修复：① healthcheck 误用 `curl`（镜像里只有 wget）→ 改 `wget -q --spider`+`127.0.0.1`；② **真根因：5 个服务 `spring.application.name` 只写在被忽略的 `bootstrap.yml`、运行期为空 → `NacosServiceRegistry` 打 `No service to register for nacos client...` 直接跳过注册**（**并不是 server-addr**）；③ 用 `docker-compose.override.yml` 补 `environment` 被本机 compose 拒（`additional properties 'environment' not allowed`）。最终改法：在 base `docker-compose.yml` 的 5 个服务各插一行 `SPRING_APPLICATION_NAME: <svc>-service`（详见下方「步骤 4 修复 A / B」） |
| 步骤 5 起 nginx | ✅ 已完成 | 2026-10-07 | `sg_nginx` `Up (healthy)`；四条 curl 全 200（`root`/`login`/`sg-login`/`sg-css`）。两个坑：① 探针 `localhost`→`127.0.0.1`（`nginx:alpine` 内 localhost 解析到 `::1`，而 `listen 80` 仅绑 IPv4）；② **旧站内链双前缀 404** —— 应用链接带 `${contextPath}`=`/software-group/...`，而 `location /` 把 `/` 映射成 `/software-group/` → 二次拼接成 `/software-group/software-group/...`（首页能开、CSS/JS/导航全 404）。修法：**新增独立前缀 location 原样透传** `location /software-group/ { proxy_pass http://tomcat_backend; ... }`（**rewrite 方向错、勿用**），并保留 `location /`（`e2e-smoke-test.sh` 要求 `/` 与 `/login.jsp`=200）。详见 §19.4「步骤 5」 |
| 步骤 6 验收（3 个脚本 + 2 个入口检查） | ⬜ 待执行 | — | 未收到完整生产输出；当前记录与采集方式见 §19.12 |

> 状态用：⬜ 待执行 / 🔵 执行中 / ✅ 已完成 / ❌ 失败待查

**端口与容器名速查**（8083 / 8088 空缺，是设计如此）

| 服务 | 容器名 | 宿主端口 |
|---|---|---|
| file-service | `sg_file_service` | 8081 |
| user-service | `sg_user_service` | 8082 |
| activity-service | `sg_activity_service` | 8084 |
| project-award-service | `sg_project_award_service` | 8085 |
| monitor-service | `sg_monitor_service` | 8086 |
| content-service | `sg_content_service` | 8087 |
| hr-service | `sg_hr_service` | 8089 |
| ai-service | `sg_ai_service` | 8094 |

#### 步骤 0｜确认源码与资源（只读，不改动任何东西）

```bash
cd /opt/software-group
ls -d */ | head -30
ls common/pom.xml user-service/Dockerfile nginx/nginx.conf static/css 2>&1
free -h && df -h /var/lib/docker && nproc
```

- **用途**：确认 8 个 `*-service/`、`common/`、根 `pom.xml`、`nginx/nginx.conf`、`static/{css,js,images}` 都在 VM 上；并核对内存 ≥8GB、`/var/lib/docker` 余量 >20GB。
- **这是目前最大的未知项 —— 源码若不在 VM 上，后续全部免谈。**

#### 步骤 1｜（可选，建议）修 `sql/` 文件名顺序

```bash
cd /opt/software-group/sql && mv db_users.sql zz_db_users.sql && ls
```

- **用途**：让 `software_group.sql` 排在前面（对应待办 #1）。
- 对**本次**部署**不阻塞**（数据卷已存在，初始化根本不会跑），但修了才算"可重复部署"。若 VM 上是 git 仓库用 `git mv`。

#### 步骤 2｜起中间件层

```bash
cd /opt/software-group
docker compose up -d nacos sentinel minio
sleep 60    # Nacos 单机模式首次启动慢，60 秒内显示 (health: starting) 属正常
curl -s -o /dev/null -w 'nacos=%{http_code}\n'    http://localhost:8848/nacos/
curl -s -o /dev/null -w 'sentinel=%{http_code}\n' http://localhost:8858/
curl -s -o /dev/null -w 'minio=%{http_code}\n'    http://localhost:9000/minio/health/live
```

- **预期**：三个都返回 `200`。
- **实测（2026-10-02）**：`sentinel` ✅ `200`、`minio` ✅ `200`、`nacos` ✅ `200`（nacos 已切 MySQL 持久化，见下方「✅ 步骤 2 已解决」）。

---

> ## ✅ 步骤 2 已解决（2026-10-02 记）
>
> **一句话**：nacos 起不来的根因是**内嵌 Derby 在慢盘上建库超时**，调参/删目录/重建 volume 全都治不了；**改成 MySQL 持久化后一次起通**，`(healthy)` + `200`。
> 下方"历史命令（M/N/O、P/Q）"仅作记录，**已不需要再执行**。

### 最终根因（2026-10-02 定性）

| 项 | 内容 |
|----|------|
| 现象 | `sg_nacos` `restart: always` 无限重启；`docker logs` 反复出现 `SQLTimeoutException: Login timeout exceeded`，随后 `... does not contain the expected 'service.properties' file` |
| 主因 | volume `software-group_nacos_data` 落在 **Hyper-V 动态扩展 VHDX** 上，磁盘慢；**内嵌 Derby 首次建库耗时 >35s，越过 Derby 默认 30s 登录超时** → 建库中途失败 |
| 次因（自续） | 失败留下**缺 `service.properties` 的残破 `derby-data`**，之后每次启动 Derby 都判定"上次建库没完成"而拒绝，崩溃循环不止 |
| 已排除 | 内存：`OOMKilled=false`；权限：镜像内进程实测 `uid=0(root)`，坏目录本身即 root 创建 |

### 试过但无效的路（**别重复**）

| 尝试 | 结果 |
|------|------|
| 删 `derby-data`（即旧 M/N/O） | 目录清了，但**下次启动仍在 30s 内剪不断**，重新生成残破目录 → 继续崩 |
| 重建 `software-group_nacos_data` volume（退路 A） | 同上，慢盘这个物理条件没变，复现 |
| `JAVA_OPT_EXT="-Dderby.loginTimeout=120 -Ddb.pool.config.connectionTimeout=60000 -Ddb.pool.config.initializationFailTimeout=60000"` | 参数确实进了 JVM（`grep -c` = 108），**内嵌 Derby 不认这个系统属性**，仍崩（且崩溃间隔反而缩到 2.2s） |

### 生效的解法：nacos 持久化切 MySQL

细节与改动清单见紧接着的 **`#### 步骤 2 收尾改动`**。要点：新建 `nacos_config` 库（12 张表）→ compose 里 nacos 加 `SPRING_DATASOURCE_PLATFORM=mysql` + 6 个 `MYSQL_SERVICE_*` → 加 `depends_on: mysql(service_healthy)`。**结果：`docker compose ps nacos` → `Up (healthy)`，`curl .../nacos/` → `nacos=200`。**

### 历史命令（M/N/O、P/Q —— 已作废，仅供追溯，不要重跑）

<details>
<summary>展开：2026-09-29 的 M/N/O 与 P/Q（Derby 路线的旧操作）</summary>

| 步 | 做了什么 | 命令 |
|----|----------|------|
| **M** | 摘 restart 策略停循环 | `docker update --restart=no sg_nacos` + `docker stop sg_nacos` |
| **N** | 查 volume 内目录 | `docker run --rm --entrypoint ls -v software-group_nacos_data:/data redis:7-alpine -la /data` |
| **O** | 删坏目录 | `docker run --rm --entrypoint rm -v software-group_nacos_data:/data redis:7-alpine -rf /data/derby-data` |
| **P** | 重新起 nacos 并等待 | `docker update --restart=always sg_nacos` + `docker compose up -d nacos` + `sleep 90` + `curl -s -o /dev/null -w 'nacos=%{http_code}\n' http://localhost:8848/nacos/` |
| **Q** | 复查四条 | `docker inspect sg_nacos \| grep -E '"(RestartCount\|Status)"'` + `docker compose exec nacos ls -la /home/nacos/data/derby-data`（期望 `RestartCount` 不再增长、`Status=running`、出现 `service.properties`、`nacos=200`） |

</details>

---

#### 步骤 2 收尾改动（2026-10-02，已完成，供"可重复部署"参考）

> 目的：把 nacos 从内嵌 Derby 切到 **MySQL 持久化**，彻底绕开慢盘建库超时。**新环境部署按本小节做一遍即可，不必再走上面的 Derby 老路。**

**① `docker-compose.yml` 里 nacos 服务的改动**

- **新增** 持久化环境变量（`environment:` 下）：

  ```yaml
        SPRING_DATASOURCE_PLATFORM: mysql
        MYSQL_SERVICE_HOST: mysql
        MYSQL_SERVICE_PORT: 3306
        MYSQL_SERVICE_DB_NAME: nacos_config
        MYSQL_SERVICE_USER: root
MYSQL_SERVICE_PASSWORD: ${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD is required}
        MYSQL_SERVICE_DB_PARAM: "characterEncoding=utf8&connectTimeout=3000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
  ```

- **新增** 启动门禁（nacos 段末尾）：

  ```yaml
        depends_on:
          mysql:
            condition: service_healthy
  ```

- **删除** 曾经的 `JAVA_OPT_EXT: "-Dderby.loginTimeout=120 ..."`（已验证无效，**别再加回来**）。

**② 建 `nacos_config` 库 + 建表**

- 先建空库（MySQL 已在跑）：

  ```bash
  docker exec sg_mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" \
      -e "CREATE DATABASE IF NOT EXISTS nacos_config DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
  ```

- 从 nacos 镜像里导出官方建表脚本（**12 张表**，无 `CREATE DATABASE`/`USE`，需先 `USE nacos_config` 再执行）：

  ```bash
  docker run --rm --entrypoint cat nacos/nacos-server:v2.2.3 \
      /home/nacos/conf/mysql-schema.sql > nacos-mysql-schema.sql   # 实测 10825 字节
  ```

- 导入：

  ```bash
  docker exec -i sg_mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" nacos_config \
      < nacos-mysql-schema.sql
  docker exec sg_mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" \
      -e "SHOW TABLES IN nacos_config;"        # 期望 12 行
  ```

  > ⚠️ **`nacos-mysql-schema.sql` 放在项目根目录，不要混入业务库初始化文件。** DB-02 起 Compose 不再将 `sql/software_group.sql` 挂载到 MySQL 初始化目录，业务库由独立 Flyway `db-migrate` 任务管理；Nacos 的 12 张表仍须按本节单独导入 `nacos_config`，不能导入 `software_group`。已有业务库首次使用新 Compose 前，先按《业务数据库Flyway迁移说明》核对结构并显式 baseline。

**③ 重建 nacos 容器并验证**

```bash
cd /opt/software-group
docker compose up -d nacos
sleep 60
docker compose ps nacos                                  # 期望 Up ... (healthy)
curl -s -o /dev/null -w 'nacos=%{http_code}\n' http://localhost:8848/nacos/   # 期望 nacos=200
```

**④ 附带说明**

- `software-group_nacos_data` 卷现在**已无实际作用**（配置存在 MySQL 里），可留可删，不必再折腾它。
- 备份/迁移只需带 **`mysql_data` 卷**，nacos 配置随 `nacos_config` 库一起走。

---

#### 步骤 3｜构建 8 个微服务（最慢的一步）

```bash
cd /opt/software-group
COMPOSE_PARALLEL_LIMIT=2 docker compose build \
    file-service user-service activity-service project-award-service \
    hr-service monitor-service content-service ai-service
```

- **用途**：生成 8 个镜像。每个 Dockerfile 内含 **3 次 Maven 构建**（根 pom → `common/` → 本服务），`common` 层有 Docker 缓存可复用。
- **为什么限并发**：VM 只有 **4 核 / 12GB**，而每个 Maven 构建进程约吃 0.8–1.5GB。默认并发会同时跑 4 个 → 叠加已在跑的服务，**极易 OOM**。`COMPOSE_PARALLEL_LIMIT=2` 是折中；若构建中内存吃紧（`free -h` 的 available 掉到 1GB 以下），改用 `COMPOSE_PARALLEL_LIMIT=1`。
- **预期**：8 行 `Successfully tagged software-group-<svc>:latest`。
- **风险**：① 首次需拉 `maven:3.9-eclipse-temurin-17` + `eclipse-temurin:17-jre-alpine` ≈1.5GB；② 容器内直连 Maven Central，**校园网可能慢或超时**，必要时挂国内镜像。
- **单独重试某个服务**：`docker compose build user-service`（不影响其它，失败后优先用这条重试）。

#### 步骤 4｜启动 8 个微服务

```bash
cd /opt/software-group
docker compose up -d file-service user-service activity-service project-award-service \
                     hr-service monitor-service content-service ai-service
curl -s "http://localhost:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=20" | python3 -m json.tool
curl -s -o /dev/null -w 'user-svc=%{http_code}\n' http://localhost:8082/actuator/health
docker compose logs --tail=40 user-service
```

- **预期**：Nacos 服务列表出现 8 个服务名；`user-svc=200`；日志有 `Tomcat started on port(s): 8082` 与 `Started UserServiceApplication`。
- **实测（2026-10-07）**：`up -d` 后 8 个容器都 `Up`，但**暴露出两个此前没发现的坑**（均属 compose/配置层，不是应用代码问题），逐个修完细节见下。

##### 步骤 4 修复 A：healthcheck 误用 `curl`（已修，8 个全 `(healthy)`）

- **现象**：8 个服务容器全部 `Up ... (unhealthy)`，但 `curl http://localhost:8082/actuator/health` 从宿主看是 **`{"status":"UP"}`**（`db`/`discoveryComposite`/`diskSpace`/`sentinel` 全 UP），日志也显示注册成功。
- **根因**：compose 里 8 个 java 服务的 `healthcheck.test` 写的是 `["CMD", "curl", "-f", "http://localhost:<port>/actuator/health"]`，但运行镜像 `eclipse-temurin:17-jre-alpine` **只有 busybox 的 `wget`、没有 `curl`**（`docker exec sg_user_service which curl` 无输出）；compose 的 `healthcheck.test` 会**覆盖** Dockerfile 里原本正确的 `wget -q --spider` HEALTHCHECK → 探针永远"命令不存在" → 恒 `unhealthy`。
- **口诀**：**"应用 curl 得到 UP、容器却 unhealthy" → 先查探针用的二进制在镜像里存不存在。**
- **修复（已应用在 VM 与仓库 `docker-compose.yml`）**：

  ```bash
  sed -i '/actuator.health/ s/"curl", "-f"/"wget", "-q", "--spider"/' docker-compose.yml
  sed -i '/actuator.health/ s/localhost/127.0.0.1/' docker-compose.yml
  ```

  （用 `127.0.0.1` 而非 `localhost`，避免容器内 `localhost` 解析到 `::1` 而 Tomcat 只听 IPv4。）改完 `docker compose up -d <8 svc>` 重建 → **8 个全部 `(healthy)`**。

##### 步骤 4 修复 B：5 个服务未注册 —— **真因是 `spring.application.name` 为空**（已解决，`count=8`）

- **现象**：`curl .../nacos/v1/ns/service/list` 只返回 **3 个**（`ai-service`、`content-service`、`project-award-service`），其余 5 个（file/user/activity/hr/monitor）不注册。
- **根因（2026-10-07 纠正；此前误判为 `server-addr`）**：**真正的阻塞是 `spring.application.name` 缺失**。这 5 个服务把 `spring.application.name` 只写在 `bootstrap.yml` 里，而它们**没有 `spring-cloud-starter-bootstrap`** → 整份 `bootstrap.yml` 被忽略 → 运行期 `spring.application.name` 为**空** → `NacosServiceRegistry.register()` 打印 `No service to register for nacos client...` 并**直接 return、跳过注册**。此时 `server-addr` 根本没被用到——所以补 `server-addr` 的环境变量**设了也白设**。
  - **日志实证**：`WARN c.a.n.c.r.NacosServiceRegistry - No service to register for nacos client...`
  - **相关性 100%**：`ai-service` 的 `spring.application.name` 在 `application.yml`（正常）；`content`/`project-award` 有 bootstrap starter，`bootstrap.yml` 生效（正常）。
  - 附带第二个问题：`application.yml` 把 nacos 配置挂在顶层 `alibaba:`（应为 `spring.cloud.nacos`），这段被忽略（但不是本次阻塞点）。
- **修复（最终生效，**不重建镜像**，改 base `docker-compose.yml`）**：给这 5 个服务各补一行 `SPRING_APPLICATION_NAME: <svc>-service`（`file/user/activity/hr/monitor` → `file-service`…，与已注册的 `ai-service` 命名一致）：

  ```bash
  for s in file user activity hr monitor
  do
  sed -i "/container_name: sg_${s}_service/,/NACOS_HOST: nacos/ s|^\( *\)\(NACOS_HOST: nacos\)|&\n\1SPRING_APPLICATION_NAME: ${s}-service|" docker-compose.yml
  done
  ```

  - 范围锚点＝每个服务独有的 `container_name: sg_<x>_service`；用 `&`+`\1` 复用（不重复长片段、不写死缩进）。
  - 校验：`grep -c SPRING_APPLICATION_NAME docker-compose.yml` → **5**。改完 `docker compose up -d <8 svc>` 重建这 5 个 → `curl .../service/list` → **`count=8`**。
  - **VM 与仓库 `docker-compose.yml` 均已改**（仓库由同一 loop 写入，`grep -c` = 5）。
- **走过但无效 / 被拒的路（别再走）**：
  - `SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR: nacos:8848`（8 处，`sed` 插入）：**没解决问题**——app 名先空，注册在更早处就 `return` 了。它现仍在 compose 里（无害多余，可留可删）。
  - `docker-compose.override.yml` 补 `environment`：被本机 compose 拒——`validating .../docker-compose.override.yml: services.monitor-service additional properties 'environment' not allowed`（override 里的 `environment` 被判成"不许出现的属性"，反常）。**override 路在本机走不通，改用 base 文件。**
  - ⚠️ **粘贴坑（2026-10-07）**：下发 sed 别在替换段**重复长片段**（会被整段吞 → `unterminated 's'`），缩进别写死、用 `\(...\)`+`\1` 反向引用。见记忆 `feedback_paste_mangling`。

##### ✅ 步骤 4 断点已解决（2026-10-07）

- 断点（"重建容器 + 复查 Nacos 注册"）已跑完：`docker compose up -d <8 svc>` 后 `curl .../service/list` → **`count=8`**，8 个服务名齐全。
- **关键教训**：卡住的真因是 `spring.application.name` 为空（见「修复 B」），**不是** server-addr。只盯着 `count=3` 会误判成"环境变量没生效"——**一定要抓启动日志里的 `c.a.n.c.r.NacosServiceRegistry` 那行**（`No service to register for nacos client...`）。
- **重启后提醒**：VM/容器重启后，`docker compose ps` 先确认 mysql/redis/rabbitmq/nacos/sentinel/minio 与 8 个服务都 `Up`；若某服务没起，`docker compose up -d` 一次即可（`restart: always` 通常会自动拉起）。


#### 步骤 5｜最后起 nginx

```bash
cd /opt/software-group
mkdir -p static/css static/js static/images
docker compose up -d nginx
```

- **用途**：nginx 是统一入口（宿主 `${NGINX_PORT:-80}:80`），`depends_on` 了 `app` + 全部 8 个服务，**必须最后起**（否则依赖未就绪）。
- **✅ 已完成（2026-10-07）**：`sg_nginx` `Up (healthy)`，四条 curl 全 200。
- **两个坑（务必照做，否则旧站经 `:80` 会 404）**：
  1. **探针恒 unhealthy**：healthcheck 用 `http://localhost/health`，`nginx:alpine` 内 `localhost` 解析到 `::1`，而 `listen 80;` 只绑 IPv4 → 改 `127.0.0.1`。
  2. **旧站内链双前缀 404**：应用链接由 `${pageContext.request.contextPath}` 生成为 `/software-group/...`，而 `location / { proxy_pass http://tomcat_backend/software-group/; }` 把 `/` 映射成 `/software-group/` → 二次拼接成 `/software-group/software-group/...` → **首页能开、CSS/JS/导航全 404**。修法＝**新增一个原样透传的前缀 location**（`rewrite` 走不通，别试 —— 见下方注）：
     ```nginx
     location /software-group/ {
         proxy_pass http://tomcat_backend;
         proxy_set_header Host $host;
         proxy_set_header X-Real-IP $remote_addr;
         proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
         proxy_set_header X-Forwarded-Proto $scheme;
         proxy_http_version 1.1;
         proxy_set_header Connection "";
     }
     ```
     并**保留** `location /`（`scripts/e2e-smoke-test.sh` 硬性要求 `/` 与 `/login.jsp`=200）。
     > 为什么不用 `rewrite`：① `rewrite ^/software-group/(.*)$ /$1 break;` 放在带 URI 的 `proxy_pass` 里得不到预期路径（实测 backend 收到错误路径 → 仍 404）；② 即便剥成功，`/software-group/css/x.css` → `/css/x.css` 会撞上 nginx 的 `location /css/` 静态 alias（空目录）→ 还是 404。
     > 另注：`location ~* \.(woff|woff2|ttf|eot|otf)$` 是**正则**、优先于前缀，会截走 `/software-group/**/*.woff2`（字体可能 404）；要覆盖就写 `location ^~ /software-group/`。
     > 改 `nginx.conf` 时的粘贴坑：`sed` 的 `&`/`\$`/`{}` 与 `python` 里的 `\n` 都会被工具改写 → 用 `python3 - <<'PY'` + `chr(10)` 拼行、`awk 'NR!=<n>'` 删行、改完 `docker compose up -d --force-recreate nginx`（见记忆 `deploy-windows-dockerfile-gotchas`）。
- **验收 curl**：
  ```bash
  curl -s -o /dev/null -w 'root=%{http_code}\n'     http://localhost/
  curl -s -o /dev/null -w 'login=%{http_code}\n'    http://localhost/login.jsp
  curl -s -o /dev/null -w 'sg-login=%{http_code}\n' http://localhost/software-group/login.jsp
  curl -s -o /dev/null -w 'sg-css=%{http_code}\n'   http://localhost/software-group/css/design-system.css
  ```
  四条都应为 `200`（`sg-css` 是关键，证明内链不再双前缀）。

#### 步骤 6｜验收

```bash
cd /opt/software-group
bash scripts/verify-infrastructure.sh   # 只能在整栈起齐后跑（会查 sg_nacos/sg_minio/sg_nginx）
bash scripts/verify-routes.sh
bash scripts/e2e-smoke-test.sh
docker exec sg_nginx wget -q --spider http://app:8080/software-group/login.jsp && echo 'app-direct=200'
curl -s -o /dev/null -w 'via-nginx=%{http_code}\n'  http://localhost/software-group/login.jsp
```

- **预期**：三个脚本全绿；容器内直连旧 WAR 和 Nginx 统一入口均返回 `200`（证明微服务上了、且旧站没被影响）。

> 2026-11-15 的 4.3 补充：`verify-routes.sh` 和 `e2e-smoke-test.sh` 现只验证网关安全回退、页面可达及明确禁用的接口，不能证明微服务业务已切流。部署 4.3 候选版本并完成数据库迁移后，另运行 `bash scripts/verify-mig43-production-readiness.sh`，再做登录、项目、奖项、新闻和群组的实际读写验收。OPS-01 后旧 WAR 的 8080 仅在 Docker 网络内可访问；直连验收从 Nginx 容器访问 `app:8080`。

> **当前步骤 6 状态见 §19.12。** 上面的 HTTP 80 命令保留为 2026-10 的部署历史；新候选版本完成 OPS-03 的 TLS 部署后，应使用受信任 CA 的 HTTPS 入口采集验收证据。历史的页面 200 不等于当前版本通过。

### 19.5 风险清单

| 风险 | 说明 |
|---|---|
| 源码不在 VM | 步骤 0 会暴露；**最大未知项** |
| 内存吃紧 | nacos 512m + sentinel + minio + 8×JVM(256–512m) + app/mysql/redis/rabbit，建议 VM ≥8GB；留意 `dmesg \| grep -i oom` |
| Maven 拉包慢/失败 | 校园网直连 Maven Central |
| 首次构建慢 | 8 服务 × 3 次 Maven 构建，首次约 20–40 分钟 |
| 误删数据卷 | `docker compose down -v` 会触发待办 #1，45 张表全没 |

### 19.6 快速定位（供下次读取本文件）

- **手册状态**：原一~十八章已移入[历史附录](历史附录-Windows主机Docker与早期VM部署.md)；本节 19.1–19.6 是历史排查记录，当前生产操作以文首路线和 19.7–19.12 专项补充为准。
- **VM 历史地址**：`sg-linux` 在 2026-09 曾使用 `192.168.0.15`，2026-11-10 从 Windows 宿主机实测可连接的地址为 `192.168.65.33`；当前 IP 须现场核对。项目目录为 `/opt/software-group`。
- **旧 WAR 历史状态**：2026-09 曾通过 `http://192.168.0.15:8080/software-group/login.jsp` 直连；当前目标版本仅由 Nginx HTTPS 对外提供入口，生产尚未切换。
- **进度一览（2026-10-07）**：步骤 0 ✅、步骤 1 ✅（sql 顺序+表名复数）、步骤 1.5 ✅（CRLF→LF）、步骤 2 ✅（nacos 切 MySQL）、步骤 3 ✅（8 个镜像 467–511MB）、**步骤 4 ✅（8 容器 `Up (healthy)`，Nacos `count=8`）**、**步骤 5 ✅（nginx `Up (healthy)`；`root`/`login`/`sg-login`/`sg-css` 四条 curl 全 200）**、**步骤 6 🔵 验收（下一动作）**。
- **🔴 下一条动作：步骤 6（整栈验收）** —— 跑 `scripts/verify-infrastructure.sh`、`verify-routes.sh`、`e2e-smoke-test.sh` 三个脚本 + 两个 curl（`app-direct` 直连 `:8080`、`via-nginx` 走 `:80`）都应为 200。
- **步骤 5 的两个坑（细节见 §19.4「步骤 5」）**：① 探针 `localhost`→`127.0.0.1`；② 旧站内链双前缀 404 → **新增 `location /software-group/ { proxy_pass http://tomcat_backend; ... }` 原样透传**（**rewrite 方向错、勿用**），保留 `location /`（`e2e-smoke-test.sh` 要 `/`、`/login.jsp`=200）。
- **步骤 4 的三个坑与真因（细节见 §19.4「步骤 4 修复 A / B」）**：
  - **A**＝healthcheck 误用镜像里没有的 `curl` → 改 `wget -q --spider` + `127.0.0.1`。
  - **B（真因）＝5 个服务 `spring.application.name` 为空**——只写在被忽略的 `bootstrap.yml`（它们无 `spring-cloud-starter-bootstrap`）→ `NacosServiceRegistry` 打 `No service to register for nacos client...` 直接跳过注册。**不是 server-addr！** 修法：base `docker-compose.yml` 里给这 5 个各加 `SPRING_APPLICATION_NAME: <svc>-service`。
  - **C**＝`docker-compose.override.yml` 补 `environment` 被本机 compose 拒（`additional properties 'environment' not allowed`）→ 改走 base 文件。
- **步骤 2 的历史**：Derby 慢盘建库超时的完整定性、试错与最终 MySQL 方案，见 §19.4 步骤 2 的「✅ 步骤 2 已解决」+「步骤 2 收尾改动」（**新环境照"收尾改动"做即可，别再走 Derby**）。
- **协作方式**：用户把 VM 终端输出存到桌面 `终端输出.txt` 再发我；**每次只给一到两条命令的输出**，所以诊断步骤要能独立判断。**粘贴易失真**（中文变 `⬢`、空格/分号丢、命令被改写），验证优先用纯 ASCII + 计数判定（详见记忆 `feedback_paste_mangling`）。
- **详细证据与修复过程**：`docs/旧WAR_Tomcat10修复与部署方案.md`。
- **微服务逐服务的命令与预期值**：`docs/阶段三_微服务拆分实施计划.md` §3.3–§3.8。

### 19.7 DB-03 运行账号切换补充（2026-11-17）

前文 19.3 中“9 个 `DB_USER` 全是 root”和 19.4 中 Nacos 使用 root 的记录是当时的部署历史。DB-03 的本地配置已改为 10 个独立运行账号，生产尚未切换。正式更新 VM 前，先完成 DB-02 生产库 baseline，再按 [数据库运行账号切换说明](数据库运行账号切换说明.md) 设置私有 `.env`、运行 `db-migrate` 与 `db-users-init` 并验收权限。不能再执行旧的 `migrations/20261114_db_user_grants.sql`。

### 19.8 OPS-01 端口收敛补充（2026-11-17）

当前本地 Compose 只把 Nginx 的 TCP 80/443 发布到 VM 宿主机，80 仅用于 HTTPS 跳转；MySQL、Redis、RabbitMQ、Nacos、Sentinel、MinIO、8 个微服务和旧 WAR 均只在 Docker 网络内通信。上文 19.3、19.4、19.6 中的宿主直连端口和旧 IP 是当时的部署记录，不再作为新版本的端口要求。上线前必须按 OPS-03 文档先准备私有证书和客户端信任。

生产 VM 仍在运行旧容器；本地改动不会自动关闭既有端口。正式切换须先完成 4.6 的生产库 baseline 和独立账号验收，再按 [OPS-01 端口收敛与防火墙验收](OPS-01端口收敛与防火墙验收.md) 核对现有绑定、备份配置、重建容器并从 VM 外验证端口。容器重建可能造成短暂连接中断，应在发布窗口执行，不使用 `docker compose down -v`。

### 19.9 OPS-02 Nacos 认证补充（本地已修复，生产待验收；2026-11-17）

当前本地 Compose 已为 Nacos 2.2.3 显式开启认证，要求私有 token/服务身份和八个微服务共用的专用客户端凭据。此前 `NACOS_AUTH_ENABLE=false`、`nacos/nacos` 的示例属于历史配置。生产 Nacos 尚未切换；账号创建、默认管理员改密、密钥生成、隔离演练及在线验证按 [OPS-02 Nacos 认证切换与验收](OPS-02-Nacos认证切换与验收.md) 执行。OPS-01 后 8848/9848 不再发布到 VM 宿主机。

### 19.10 OPS-03 HTTPS 补充（本地已修复，生产待验收；2026-11-17）

当前仅有内网 IP、没有证书。本地已配置 Nginx 443 TLS、80 跳转、Secure 会话 Cookie 和内部 CA 签发/续期脚本；**生产 VM 尚未套用此配置**。签发与信任步骤、到期告警和上线验收见 [OPS-03 内网 IP HTTPS 部署与验收](OPS-03-内网IP-HTTPS部署与验收.md)。内网 IP 的 HSTS 头不会被浏览器缓存；要获得 HSTS 保护，需使用域名。

### 19.11 OPS-04 健康检查补充（本地已修复，生产待验收；2026-11-17）

Nginx `/health` 现在只表示自身存活；容器健康状态改由 `/ready` 检查旧 WAR 与 MySQL/Redis。八个微服务提供内部 Prometheus 指标，可选 `monitoring` profile 在 Docker 网络内直接采集；线上仍需配置告警通道和 HTTPS 入口的业务合成检查。发布和验证步骤见 [OPS-04 健康检查与业务合成验收](OPS-04-健康检查与业务合成验收.md)。本地 Docker 未启动，生产 VM 未修改。

### 19.12 OPS-07 步骤 6 生产验收记录（2026-11-18；待现场执行）

**结论：步骤 6 尚未完成。** 截至本次文档核对，没有三项脚本和两项入口检查针对当前候选版本的完整生产输出、退出码、执行时间及 Git 版本。2026-10-07 的四条页面 `200` 和 2026-11-09 提供的 `docker compose ps`（16 个容器运行，其中 15 个显示 `healthy`，旧 WAR 为 `Up`）只能作为历史状态证据，不能代替当前步骤 6 验收。本地修复尚未发布到生产 VM。

| 验收项 | 已有证据 | 当前候选版本结果 | 退出码 / 时间 / 证据文件 |
|---|---|---|---|
| `verify-infrastructure.sh` | 未收到完整运行输出 | 待执行 | 待采集 |
| `verify-routes.sh` | 未收到完整运行输出 | 待执行 | 待采集 |
| `e2e-smoke-test.sh`（含容器内服务健康） | 未收到完整运行输出 | 待执行 | 待采集 |
| 旧 WAR 直连 `app:8080/software-group/login.jsp` | 2026-10 的旧部署曾可访问；非当前候选版本证据 | 待执行 | 待采集 |
| Nginx 统一入口 `/software-group/login.jsp` | 2026-10-07 历史页面检查为 `200`；非当前候选版本证据 | 待执行 | 待采集 |
| `verify-mig43-production-readiness.sh` | 4.3 候选版本和生产数据库迁移尚未验收 | 待满足前置条件后执行 | 待采集 |

完成 4.6 数据库 baseline 与账号验收、OPS-03 证书和客户端信任、候选版本发布后，在 **VM** 的 `/opt/software-group` 运行只读采集脚本。将 `<VM_IP>` 与 CA 路径替换成生产实际值；只有 4.3 候选版本已部署且数据库迁移完成时才设置 `MIG43_CANDIDATE_DEPLOYED=1`：

```bash
cd /opt/software-group
BASE_URL="https://<VM_IP>" CURL_CA_BUNDLE="/path/to/ca.crt" MIG43_CANDIDATE_DEPLOYED=1 \
  bash scripts/collect-ops07-acceptance.sh
```

脚本运行三项只读检查和两个入口检查，记录北京时间、Git SHA、容器镜像 ID、每项退出码及原始输出到 `backup/ops07-acceptance-<时间>/`。`summary.md` 中五个核心检查全部为“通过”，且条件适用的 4.3 预检通过后，将实际结果、时间、Git SHA 与证据目录写回上表，再把步骤 6 标记“已完成”。若任一项失败，保留原始日志并标记“失败待查”；不得把未执行项填为通过。脚本不覆盖登录、上传下载、业务读写或生产外部端口验收，这些按各专项验收文档继续执行。
