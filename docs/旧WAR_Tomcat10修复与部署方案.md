# 旧 WAR 部署到 Tomcat 10.1 与 Jakarta 迁移遗留修复方案

> 本文与 [`Windows Server生产部署完整手册.md`](./Windows%20Server生产部署完整手册.md) 平级：手册负责整栈部署流程，本文负责**旧单体 WAR（compose 服务 `app`）**这一条支线的诊断、修复与验证。

> 下面关于 `SessionRedisFilter` 和 Redis 中 `session:*` 键的记录属于当时的历史诊断与测试。OPS-05 已移除该过滤器；出现 Redis 键只证明旧实现写入，不证明跨实例恢复会话。当前旧 WAR 仅支持单实例 Tomcat 会话。
> 手册 §18 的进度断点、以及阶段 A 抓到的实际报错，都回写到本文。

## 进度断点

| 阶段 | 内容 | 状态 |
|------|------|------|
| 阶段 0 | 把方案落成仓库文档（本文） | ✅ 已完成 |
| 阶段 A | 部署试跑，抓 `app` 的失败证据 | ✅ 已完成（证据见 A5） |
| 阶段 B | 修复 5 个文件 + 122 处 JSP taglib URI | ✅ 已完成（见 §B 实施记录） |
| 阶段 C | 重新部署验证 | ✅ 已完成（证据见 §C2） |

---

## 1. 背景

项目是"新旧双栈并存"结构：

- 根 `pom.xml` 是 9 个 Spring Boot 3.2.5 微服务的父工程（本地已逐个在 Docker 里跑通）。
- `pom-war.xml` 单独打旧单体 WAR，由 `Dockerfile.tomcat` 容器化为 compose 服务 `app`，nginx 做统一入口。

部署目标是 Windows Server。因该机的 Docker EE 只能跑 Windows 容器，整套 Linux 镜像改为在 **Hyper-V 的 Ubuntu VM** 里跑 docker compose（手册 §18）。当前状态：**VM 里 Ubuntu 已装好，Docker 尚未安装，还没有任何部署动作。**

### 1.1 为什么必须先修旧 WAR

旧 WAR 的 Java 源码在 **2026-08-02（commit `4ad3474`）** 做过一次 jakarta 迁移：

- 316 处 `javax.servlet` → `jakarta.servlet`
- Java 11 → 17，Servlet 4.0 → 5.0，Tomcat 9 → 10.1

但那次**只改了 Java 源码和根 `pom.xml`，WAR 的运行环境三处没跟上**，导致 `app` 容器必然起不来：

| # | 位置 | 问题 |
|---|------|------|
| 1 | `Dockerfile.tomcat:2` | 仍是 `FROM tomcat:9-jdk11`。Tomcat 9 只提供 `javax.servlet`，而源码是 `jakarta.servlet`（实测 249 处 import，0 处 javax） |
| 2 | `pom-war.xml:45-49` | 仍是 `javax.servlet:jstl:1.2` |
| 3 | `src/main/webapp/**/*.jsp` | 86 个文件、122 处 taglib URI 仍是 `http://java.sun.com/jsp/jstl/*`；Tomcat 10.1 + JSTL 3.0 不再注册旧 URI |

### 1.2 同源的第二类问题：占位符没有运行期解析

`Config.java` **完全没有 `${VAR:-default}` 运行期解析**——只做字面的 `properties.getProperty(key, default)`。因此所有占位符必须在**构建期**被替换干净。

而 `Dockerfile.tomcat` 的 sed 清单只有 14 个变量，**漏了 `REDIS_*`、`RABBITMQ_*`、`AI_API_SECRET`**：

- 这些会以字面量形式进入部署配置，例如 `${REDIS_PASSWORD:-redis123}` 被 `RedisUtil:35` 当作密码去 `AUTH`，必然失败。
- 且 `SessionRedisFilter` 挂在 `/*` 上，**Redis 在请求路径上**，影响所有页面访问。
- 同一段 sed 还因为 `upload.baseDir=${UPLOAD_BASE_DIR:-${user.dir}/localstorage}` 的嵌套花括号，把默认值截断成 `.../localstorage/localstorage}`。

### 1.3 执行顺序

**已确认的顺序：先把部署推进、试跑一遍，再回来修这些问题。**

因此本方案分三段：**A 部署试跑 → B 修复 → C 重部署验证**。阶段 A 预期会看到 `sg_app` 失败——这正是用来坐实 1.1 诊断的证据，**不是返工**。

## 2. 已确认的技术选型

- **JSTL 3.0 + 改写 122 处 URI**（与 Tomcat 10.1 / Servlet 6.0 / JSP 3.1 官方配套）。
- **占位符改为 `Config.java` 运行期解析**，同时把 `Dockerfile.tomcat` 里的 COPY/sed/envsubst/重打包整块删掉。
- **先部署试跑，后修复。**

---

## 阶段 A：部署试跑

**目标**：验通 VM/中间件层，并抓到 `app` 的失败证据。

### A0. 本地准备（在 `E:\java\software_group_AI`）

打 WAR：

```bash
mvn -f pom-war.xml clean package -DskipTests
# → 产出 target/software-group.war
```

- **必须带 `-DskipTests`**：实测直接 `verify` 会因 **2478 个测试里 61 failures + 47 errors** 中断（报错集中在 Mockito：`getRequestDispatcher` 返回 null、`InvalidUseOfMatchers`，疑似本机只有 JDK 23 而 Mockito 4.11 不支持所致）。测试失败归因列为阶段 B 的待查项，不阻塞部署。
- `target/software-group.war` 当前不存在，而 `Dockerfile.tomcat:27` 要 COPY 它——不先打出来，`app` 连镜像都构建不出。

备好 `.env`（放服务器 `/opt/software-group/.env`）。除手册 §5.1 模板外**必须包含**：

- `MYSQL_ROOT_PASSWORD` 与 `MYSQL_PASSWORD` **同值**（`docker-compose.yml:558` 的 `app` 用的变量名与微服务不同，见 B5）
- `AI_API_KEY=<config.local.properties 里那个 sk-cp- 开头的真 key>` —— 不填的话构建期会被替换成空串，AI 助手直接不可用

### A1. VM 内安装 Docker（[历史附录 §18.4 段 B](历史附录-Windows主机Docker与早期VM部署.md)）

按手册命令安装 `docker.io` + `docker-compose-plugin`，`usermod -aG docker $USER` 后重新登录。校园网拉镜像慢时再配国内 registry 镜像。

### A2. 文件进 VM（[历史附录 §18.4 段 C](历史附录-Windows主机Docker与早期VM部署.md)）

目标目录 `/opt/software-group`。除仓库代码外，**必须补上 3 个 git 不追踪的文件**：

- `target/software-group.war`
- `src/main/resources/config.local.properties`（占位符格式）
- `.env`

### A3. 起栈

```bash
cd /opt/software-group
docker compose up -d
docker compose ps
```

### A4. 抓证据（本阶段的关键产出）

```bash
docker compose logs app | head -60
```

- **预期**：`sg_app` 报 jakarta 相关错误（`NoClassDefFoundError` / `The absolute uri ... cannot be resolved` / `Error listenerStart`），与 1.1 的诊断一致 → 进入阶段 B。
- **反例处理**：若 `app` 竟然正常起来，说明诊断有误，**立即停下来复核**：重点看 `web.xml` 是否真被 Tomcat 10.1 接受了 4.0 描述符、以及实际运行的是哪个镜像。

### A5. 实测结果（阶段 A 证据）

中间件层全部健康（`sg_mysql` / `sg_redis` / `sg_rabbitmq` 均 `Up (healthy)`，MySQL 首次 init 已过），`sg_app` 容器 `Up`（`catalina.sh run` 进程存活），但 webapp 部署失败：

```
23-Sep-2026 03:13:32.773 SEVERE [main] org.apache.catalina.core.StandardContext.startInternal One or more listeners failed to start. Full details will be found in the appropriate container log file
23-Sep-2026 03:13:32.773 SEVERE [main] org.apache.catalina.core.StandardContext.startInternal Context [/software-group] startup failed due to previous errors
```

**与 1.1 的诊断一致 → 阶段 A 达成**（A4 预期就是 `app` 失败，这不是返工）。

容器内 `logs/localhost.*.log` 抓到的根因（**实测确认**，2026-09-23）：

```
at org.apache.catalina.core.StandardContext.listenerStart(StandardContext.java:4154)
at org.apache.catalina.core.StandardContext.startInternal(StandardContext.java:4679)
...
at org.apache.catalina.startup.HostConfig.deployWAR(HostConfig.java:1012)
Caused by: java.lang.ClassNotFoundException: jakarta.servlet.ServletContextListener
    at org.apache.catalina.loader.WebappClassLoaderBase.loadClass(WebappClassLoaderBase.java:1441)
SEVERE [main] StandardContext.listenerStart Skipped installing application listeners due to previous error(s)
```


根因已按代码收窄到 1.1 的第 1 条：`web.xml:365-372` 注册的 `listener.StudySessionListener` / `listener.GroupMuteListener` 都 `implements jakarta.servlet.ServletContextListener`，而镜像基底仍是 `tomcat:9-jdk11`（B1 未修）只提供 `javax.servlet.*`，且 `jakarta.servlet:jakarta.servlet-api` 是 `provided` 作用域、不进 `WEB-INF/lib` —— 监听器类加载即 `NoClassDefFoundError`，Tomcat 在 `listenerStart()` 阶段中止，`filterStart()`（`SessionRedisFilter`）根本没轮到执行。

**排除项**：两个监听器的 `contextInitialized` 只 `new Timer` + 调度到 22:00，启动期不碰 DB/Redis；`SessionRedisFilter.init()` 是空实现（`RedisUtil` 靠静态初始化，首次请求才触发）。故本次失败**不含**占位符/Redis 因素——B4 的靶子是 B1 修好之后才会暴露的第二道坎。

---

## 阶段 B：修复

改动集中在 5 个文件 + 一批 JSP。

| # | 文件 | 改动 |
|---|------|------|
| B1 | `Dockerfile.tomcat` | `FROM tomcat:9-jdk11` → `FROM tomcat:10.1-jdk17-temurin`（官方 10.1 tag 是 Ubuntu 基底，`apt-get` 仍可用）。**删掉** `COPY src/main/resources/config.local.properties /tmp/config.template` + 14 条 sed + envsubst + unzip/rezip 整块（B4 之后不再需要）；保留 `rm -rf webapps/*`、`COPY target/software-group.war`、`CATALINA_OPTS`、`EXPOSE 8080`、`CMD catalina.sh run` |
| B2 | `pom-war.xml` | 删 `javax.servlet:jstl:1.2` 与 `jstl.version` 属性；加 `jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api:3.0.2` 与 `org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.1`（compile 作用域）；`servlet-api.version` 5.0.0 → 6.0.0 以对齐 `tomcat-embed 10.1.20` |
| B3 | `src/main/webapp/**/*.jsp`（86 个文件、122 处） | `http://java.sun.com/jsp/jstl/core` → `jakarta.tags.core`；`.../fmt` → `jakarta.tags.fmt`。两种书写顺序（`uri=` 在 `prefix=` 前后）和 5 处行首缩进都要覆盖；改完用 grep 断言零残留 |
| B4 | `src/main/java/config/Config.java` | 新增私有 `resolve(String)`，在 `getProperty(key)` / `getProperty(key, default)` 两个重载里调用：正则匹配 `\$\{([A-Za-z_]\w*):-([^}]*)\}`，env 非空则用 env，否则用 group 2，并再解析一次以展开默认值里的嵌套 `${user.dir}`。这样 `REDIS_*`/`RABBITMQ_*`/`AI_API_SECRET` 一并解决，`upload.baseDir` 的截断隐患也消失 |
| B5 | `docker-compose.yml` | L558 `DB_PASSWORD: ${MYSQL_PASSWORD:-root123}` → `${MYSQL_ROOT_PASSWORD:-root123}`（`mysql` 服务只定义了 `MYSQL_ROOT_PASSWORD`，且仓库里没有 `.env` 可依赖）；`app` 的 `build.args`（`DB_*`/`AI_*`/`DES_KEY`/`HIKARI_*`/`SESSION_TIMEOUT`/`UPLOAD_BASE_DIR` + `REDIS_*`/`RABBITMQ_*`/`AI_API_SECRET`）改到 `environment:`，因为 B4 之后是运行期读 env |
| B6 | `.dockerignore` | `target/` → `target/*`（父目录被整体排除时，`!target/*.war` 无法再纳入子项，WAR 是否进构建上下文属未定义行为） |
| B7 | `src/main/webapp/WEB-INF/web.xml` | 非阻塞项：Tomcat 10.1 能接受 `version="4.0"` 的 `xmlns.jcp.org` 描述符（已确认不抛错），但顺手把开头 4 行升到 `https://jakarta.ee/xml/ns/jakartaee` + `web-app_6_0.xsd` + `version="6.0"`；469 行其余内容不动 |

**待查（不阻塞）**：那 108 个测试失败的真实归因——是 JDK 23 与 Mockito 4.11 不兼容，还是测试代码本身的 matcher 用法问题。

### B 实施记录（2026-09-23）

改动全部落在开发机，B1–B7 均已实施，`mvn -f pom-war.xml clean package -DskipTests` **BUILD SUCCESS**（31.9s），产物 `target/software-group.war`（14.76 MB）。

**与计划的三处偏差/修正：**

1. **B4 不能用正则**，原计划的正则 `\$\{([A-Za-z_]\w*):-([^}]*)\}` 会把 `upload.baseDir=${UPLOAD_BASE_DIR:-${user.dir}/localstorage}` 截断成 `${UPLOAD_BASE_DIR:-${user.dir}` —— 正是要消灭的那类 bug。改为**花括号配对扫描**：`resolve()` / `findClosingBrace()` / `expand()` 三个私有方法，递归展开嵌套默认值；不含 `:-` 的形式（如 `${user.dir}`）原样保留，继续由 `getFileStorageBaseDir()` 处理。`getProperty(key)` 现在委托给 `getProperty(key, null)`。
2. **B5 的事实修正**：`docker-compose.yml` 本来就把 `REDIS_*` / `RABBITMQ_*` 作为 `build.args` 传了，缺的是 `Dockerfile.tomcat` 里的 `ARG` 声明（未声明的构建参数不可用）；真正没传的只有 `AI_API_SECRET`。B1 删掉整个 `args` 块后，B5 把这些变量全量搬到 `app.environment`（22 个，与 `config.local.properties` 的占位符名逐一对齐）。同时确认：9 个微服务与 `mysql` 服务本来就用 `MYSQL_ROOT_PASSWORD`，只有 `app` 用 `MYSQL_PASSWORD` —— 现已对齐。
3. **B6 验证**：`target/` 整体排除时 `!target/*.war` 确实无法回纳入子项，改 `target/*` + `!target/*.war` 后 WAR 能被 `COPY` 到。

**B3 的意外结论**：解包 `jakarta.servlet.jsp.jstl-3.0.1.jar` 看 TLD，`c.tld` 同时注册了 `jakarta.tags.core` 与 `http://java.sun.com/jsp/jstl/core`（`fmt.tld` 同理）——**旧 URI 并未失效，122 处替换本可省略**。即 §4 那条"未验证项"现已验证。仍保留替换：官方推荐写法，且两种 URI 并存无冲突。

**WAR 内容断言（全部通过）：**

- `WEB-INF/lib/` 有 `jakarta.servlet.jsp.jstl-3.0.1.jar` + `jakarta.servlet.jsp.jstl-api-3.0.2.jar`，**无** `javax.servlet:jstl:1.2`
- `WEB-INF/classes/config.local.properties` 内 **22 个** `${...}` 占位符原样保留 → 说明确由运行期 `Config.resolve()` 负责展开（构建期不再替换）
- WAR 内 `login.jsp` 的 taglib URI 为 `jakarta.tags.core`

**副作用（好的）：** 占位符改成运行期解析后，AI API Key 之类只需 `docker compose up -d app` 重建容器，**不必重新 build 镜像**。

---

## 阶段 C：重新部署验证

重建 `app` 后重起，验证清单见下节。

### C1. 实施记录（2026-09-23，VM `sg-linux` / `192.168.0.15`）

镜像 `software-group-app`（本地构建，`FROM tomcat:10.1-jdk17-temurin`）已生效，容器内 `Starting Servlet engine: [Apache Tomcat/10.1.60]` 证明 B1 落地。

实际遇到的阻塞都与 WAR 无关，属于部署链本身（两条都会在**全新部署时复现**）：

1. **`sql/` 初始化顺序 bug**：`docker-entrypoint-initdb.d` 按**文件名字母序**执行，而仓库里 `db_users.sql` 排在 `software_group.sql` 之前；`db_users.sql` 含**表级 GRANT**（要求表已存在），于是第一条就报 `ERROR 1146 ... Table 'software_group.file_storage' doesn't exist`。mysql 的 entrypoint **遇错即中止整个初始化**，紧接着 `restart: always` 让容器带着非空 datadir 重启 → **初始化被永久跳过，表数为 0**。
   本次绕过方式：手工按"先 schema、后授权"的顺序导入 → 45 张表落地。
2. **`db_users.sql` 与 schema 表名不同步**：授权语句引用了 5 个**单数**表名 `resume` / `resume_award` / `resume_education` / `resume_project` / `resume_skill`，而 `sql/software_group.sql`（45 张表）用的是**复数** `resumes` / `resume_awards` / `resume_educations` / `resume_projects` / `resume_skills`。mysql 在首条错误处中止 → **144 行之后的授权全部未生效**（`hr_svc` 简历部分、`monitor_svc`、`ai_svc` 全丢）。
   本地已修正这 5 处表名。**不影响单体 WAR**（`app` 以 `root` 连库），只影响 8 个微服务。

### C2. 阶段 C 实测结果（验收证据）

| # | 检查 | 结果 |
|---|------|------|
| 1 | `mvn -f pom-war.xml clean package -DskipTests` | BUILD SUCCESS，`target/software-group.war` = **14758225** 字节 |
| 2 | 容器内 `webapps/software-group.war` | **14758225** 字节，与本地同尺寸 |
| 3 | `Starting Servlet engine` | `[Apache Tomcat/10.1.60]` → **B1 生效** |
| 4 | 部署日志 | `Deployment of web application archive [.../software-group.war] has finished in [2,624] ms`；`Server startup in [2870] milliseconds` |
| 5 | HTTP 拓扑 | `/software-group/`、`login.jsp`、`index.jsp`、`css/design-system.css` 全 **200**；`/login.jsp`、`/` 全 **404**（无根 context，符合预期） |
| 6 | JSTL 改写 | 部署产物的 `login.jsp` 第 2 行为 `<%@ taglib uri="jakarta.tags.core" prefix="c" %>` → **B3 生效** |
| 7 | **登录 POST** | `POST /software-group/login`（`admin`/`admin123`）→ **302**，`Location: /software-group/index.jsp` |
| 8 | 错误密码对照 | 同接口 `password=wrongpass` → **200**（被拒回登录页） |
| 9 | 会话落 Redis | `session:88B0AF05...` 存在，`dbsize` = 1 → `RedisUtil` AUTH 与序列化正常 |
| 10 | `db_users.sql` 缺表报错 | 历史存量 **25** 条（均为 `activity_group`），集中在 08:30–08:55 空库窗口（`GroupMuteScheduler` 每分钟一次），导入 45 张表后停止；复查计数仍为 25（未增长），`activity_group` 已存在，全库 45 张表 |
| 11 | Windows Server 浏览器 | `http://192.168.0.15:8080/software-group/login.jsp` 可正常打开并登录 → **Windows Server(192.168.0.55) → VM(192.168.0.15):8080 网络可达** |
| 12 | 管理端入口 | `登录后 /admin/index.jsp` 正常。**初访曾报 403，属预期行为**：`AuthFilter.java:151` 对 `/admin/*` 只放行 `role=ADMIN`，当时浏览器会话是非管理员账号。库内核对：`admin`(id=1) `role=ADMIN`、`status=1`、`must_change_password=0`；全库仅 **1 个 ADMIN + 26 个 MEMBER**，无 `TEACHER`/`GUEST`。非缺陷，无需修 |

**结论：阶段 C 通过。** 旧单体 WAR 在 Tomcat 10.1 上跑通，B1–B7 全部生效。#7 这一条同时压到 DES 解密、HikariCP 连接池、`user` 表种子数据三条链路，是本阶段最硬的一关。

> 对本文 §3 清单的一处更正：清单第 6 条写"期望 302 跳 `/member/index` 或 `/admin/index`"，**实际跳转目标是 `/index.jsp`**（见 `src/main/java/servlet/LoginServlet.java:46`），清单描述有误。
> 另：`/` 返 404 说明**不存在 ROOT context**，日志里那条 `Deployment of web application directory [.../webapps/ROOT]` 与磁盘现状矛盾，但无功能影响，未再深究。

### C3. 阶段 C 暴露的待办（均未修）

按"先把部署推进完"的既定顺序，以下记录在案、暂不处理：

- **`sql/` 文件名顺序**：需让 schema 排在授权脚本之前（如 `01-software_group.sql` / `02-db_users.sql`），并同步更新 `scripts/verify-infrastructure.sh:168,172,189` 与 `docs/阶段三_微服务拆分实施计划.md` 的引用。**不修则全新部署必复现 C1.1 的初始化中止。**
- **`mysql` healthcheck 缺 `start_period`**：当前仅 50s 宽限（10s × 5），首次启动很可能被判 unhealthy。
- **`sql/db_users.sql` 的 5 处表名**：本地已修，需覆盖到 VM（就地 `sed` 后重跑即可，不必再传文件）。
- **`mysql:8` 是浮空 tag**：现解析为 MySQL **8.4.11**，建议钉到 `mysql:8.0` 或 `8.4`。
- **`src/main/webapp/META-INF/context.xml`**：内容为 `<Context path="/"/>`；Tomcat 对 WAR 内 `context.xml` 的 `path` 属性**一律忽略**（context 路径由文件名推导），该文件是无效残留，可删。
- **`sql/software_group.sql` 编码混合**：部分 COMMENT 乱码、种子行含非法字节（MySQL 5.7.42 dump 遗留）。
- **108 个测试失败**归因未定（疑似本机 JDK 23 与 Mockito 4.11 不兼容）。

## 3. 验证清单

本地能做的先做（需启动 Docker Desktop、`docker info` 可用）：

1. `mvn -f pom-war.xml clean package -DskipTests` → 确认 `target/software-group.war` 存在。
2. `docker compose build --progress=plain app` → 证明 B1/B2/B6 生效（WAR 被 COPY 到、JSTL 依赖能解析）。
3. 只起必要服务：`docker compose up -d mysql redis rabbitmq app`（跳过 8 个微服务/nacos/sentinel/minio/nginx，先验单体）。
4. `docker compose logs app | head -60`。
   - 通过标志：`[Config] 已加载本地配置: config.local.properties`、`Server startup in`、`Deployment of web application archive ... software-group.war has finished`。
   - 失败信号：`NoClassDefFoundError: javax/servlet/jsp/...` 或 `The absolute uri ... cannot be resolved`（→ B2/B3 没生效）；`SEVERE: Error listenerStart`（→ B7）。
5. 登录页（这条专门验 JSTL）：
   - `curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/software-group/login.jsp` → `200`
   - `curl -s .../login.jsp | grep -c '\${'` → **0**（说明 EL/JSTL 真的渲染了而不是原样吐出）
6. 登录 POST：先读 `src/main/java/servlet/LoginServlet.java` 确认参数名与是否带 CSRF 字段，再 curl → 期望 302 跳 `/member/index` 或 `/admin/index`。
7. 验 B4/REDIS_PASSWORD：`docker compose exec redis redis-cli -a <REDIS_PASSWORD> --scan --pattern 'session*'` 能出键，且 `app` 日志无 Redis AUTH 错误。
8. 上 VM 后重复 1–7（VM 内用 `curl http://localhost:8080/software-group/login.jsp`，再从 Windows Server 浏览器开 `http://<vm-ip>:8080/software-group/login.jsp`）。

> 注意 `scripts/verify-infrastructure.sh` 会检查 `sg_nacos`/`sg_minio`/`sg_nginx`，**只在整栈起齐后跑**；它只查容器与端口，不 curl 应用本身。

## 4. 风险与未验证项

- **JSTL 3.0.1 的 TLD 是否同时注册旧 URI** 未能验证（编写时网络受限，读不到 jar 内 TLD）。若它确实兼容旧 URI，B3 的 122 处替换可省——但按已确认的选型仍执行 B3，代价是一次机械替换。
- **`tomcat:10.1-jdk17-temurin` 这个 tag 是否存在**未验证；若不存在退回 `10.1-jdk17`。
- 108 个测试失败的归因未定。
- B4 改动生产代码 `Config.java`：本地开发环境下 env 未设置，`resolve()` 会回落到默认值（如 `localhost`），行为与现状一致；测试类路径用的是 `src/test/resources/config.local.properties`（H2），不受影响。
- 阶段 A 会在修复前先跑一次，预期 `sg_app` 失败；这是刻意的诊断确认，不是返工。

## 5. 安全注意

- `src/main/resources/config.local.properties` 含真凭据（已被 gitignore），**不要打印、不要提交**。
- AI API Key（`sk-cp-` 开头）目前作为 `${AI_API_KEY:-...}` 的默认值内嵌在配置里，**会随 WAR 打进镜像层**；若镜像曾被推送或共享，需轮换该 key。
- DES 默认密钥 `(^&%gasie_%^)` 是弱加密，**修改会使已有密码哈希全部失效**。
