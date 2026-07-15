# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

高校软件小组管理系统 (University Software Group Management System) — a Maven WAR project for managing a university software group's activities, members, projects, awards, recruitment, attendance, group chat, and an AI assistant. Stack: **JSP + Servlet 4.0 (javax.*) + raw JDBC + MySQL 8 + Bootstrap 5/Tabler + Maven**, targeting **Java 8** and **Tomcat 9**.

- `groupId/artifactId/version`: `software.group/software-group/1.0.0`, final WAR name `software-group`
- Context path: `/software-group`, default port 8080
- Default admin: `admin` / `admin123`; member: `member1` / `member123`
- **Project constitution**: `RULES.md` — 8 mandatory rules (CSS variables, async 4-states, null-safety, no class components, tests required, naming conventions, no `any`, typed Props). Read it before writing code.
- **Refactoring in progress**: see `docs/服务分层与API化重构计划.md` — phased plan (P0 infra → P1 Service layer → P2 REST API → P3 MCP/Agent). Follow TDD for all new code.

## Build & Test Commands

```bash
mvn clean compile                          # compile only
mvn clean package                          # produces target/software-group.war
mvn test                                   # unit + DAO tests (*Test.java), ~1s
mvn test -Dtest=ClassName                  # run a single test class
mvn test -Dgroups=fast                     # @FastTest only (milliseconds)
mvn verify                                 # unit + integration tests (*IT.java, Embedded Tomcat), ~5s
```

**There is no Tomcat/Jetty Maven plugin** — `mvn tomcat7:run` will fail. Deploy the WAR to an external Tomcat 9 instance. First-run setup: copy `src/main/resources/config.properties` → `src/main/resources/config.local.properties` and set the real DB password / AI key.

### Test Infrastructure
- `src/test/java/support/` — test helpers: `@FastTest`/`@SlowTest`/`@IntegrationTest` meta-annotations, `H2Database` (init/reset 44 tables), `EmbeddedTomcat` + `TomcatTestBase` (random port, login helper), `HttpRequest` (lightweight HTTP client)
- `src/test/resources/h2-schema.sql` — **all 44 tables**, auto-generated from `sql/software_group.sql`. After modifying production schema, run `python bin/convert_mysql_to_h2.py` to regenerate, then update `H2Database.reset()` tables array if tables were added/dropped.
- `src/test/resources/config.local.properties` — H2 in-memory DB config; uses `NON_KEYWORDS` for reserved words (critical: do NOT add `SET` to NON_KEYWORDS — it breaks UPDATE parsing).
- H2 base fixtures: admin (id=1, `admin`/`admin123`, ADMIN) and member1 (id=2, `member1`/`admin123`, MEMBER); business fixtures built per-test-class.
- Surefire runs `*Test.java`, Failsafe runs `*IT.java` in `verify` phase.

## Architecture

### Layout (Maven standard, flat packages — no `software.group` prefix)
```
src/main/java/
├── config/       # Config.java — single config entry point (see below)
├── dao/          # One DAO per table, raw JDBC via DBUtil.getConnection()
├── filter/       # CharacterEncodingFilter → AuthFilter → LoggingFilter → SecurityFilter (web.xml order)
├── listener/     # StudySessionListener, GroupMuteListener (start schedulers on context init)
├── model/        # POJOs (~35 entities)
├── service/      # AIService (only production service); EnhancedIntentRecognizer + ConversationContextManager are ORPHANED/DEAD
├── servlet/      # ~31 Servlets; mixed registration (web.xml + @WebServlet — check both)
└── util/         # DBUtil, DESUtil, FileUtil, AuthHelper, CSRFTokenUtil, HtmlSanitizer, AIClientUtil, etc.
src/main/webapp/  # Web root (migrated from old WebContent/ — do NOT use WebContent/)
├── WEB-INF/web.xml
├── admin/ member/  # Role entry pages
├── jsp/            # All JSP views; jsp/common/ has layout_top.jsp + layout_bottom.jsp templates
├── css/ js/ images/
└── index.jsp, login.jsp, problem-report.jsp
src/main/resources/
├── config.properties          # Committed template (placeholders only)
└── config.local.properties    # Gitignored — real passwords/keys go here
src/test/java/                 # Same flat package layout as src/main/java (support/util/dao/servlet)
sql/software_group.sql         # Authoritative schema (44 tables); older per-feature .sql files are deprecated
bin/convert_mysql_to_h2.py     # H2 schema regeneration script
localstorage/                  # Uploaded files (EXTERNAL to webapp, gitignored; created at runtime)
docs/
├── requirements.md            # V2.0 requirements (38KB)
├── development.md             # Dev guide (63KB) — includes TDD workflow, coding conventions
└── 服务分层与API化重构计划.md    # P0-P3 refactoring plan
```

### Key Design Decisions

**Package layout is flat** — no `software.group.*` parent package. Test classes must mirror this (e.g. `src/test/java/dao/UserDAOTest.java`, not `src/test/java/software/group/dao/...`). This also lets tests access package-private helpers.

**No DI container** — Servlets instantiate DAOs directly as fields. Service-layer refactoring (P1) will use constructors for testability without introducing Spring.

**Test-prod DB switching via Config** — `DBUtil.getConnection()` reads driver/url/user/password from `Config` on every call (not cached in static final), so tests can switch to H2 by having `config.local.properties` on the test classpath. Production code is unaffected.

### Filter Chain (order defined in web.xml)
1. **CharacterEncodingFilter** (`/*`) — forces UTF-8
2. **AuthFilter** (`/member/*`, `/admin/*`, `/activity`, `/news`, `/project`, `/award`, `/recruit`, `/study`, `/ai`, `/group`) — whitelists public paths (news list/detail, recruit apply, `/ai`, `/problem`, index); redirects to `/login.jsp` if no session; enforces ADMIN for `/admin/*`. **Does NOT cover `/attendance/*`** — AttendanceServlet does manual checks.
3. **LoggingFilter** (`/*`) — logs POST/PUT/DELETE to `operation_log` table
4. **SecurityFilter** (`/*`) — wraps request in XSS sanitizer (Jsoup). **CSRF is NOT enforced** — `CSRFTokenUtil` mints tokens but nothing validates them.

### Authentication
- Passwords DES-encrypted (`util/DESUtil`, key `(^&%gasie_%^` from config).
- Session attributes: `user`, `username`, `role` (GUEST/MEMBER/ADMIN), `memberProfile`/`adminProfile`. Session timeout 30min.
- Use `util.AuthHelper` (`getCurrentUser`, `checkAdmin`, `checkMember`, `isAdmin`) for programmatic auth checks.

### File Storage (refactored — no longer in webapp)
- **Uploads live at `${user.dir}/localstorage/`** (outside webapp; `mvn clean` does NOT wipe them).
- Use `util.FileUtil`: `getCategoryDir("images/avatar")` (write), `resolvePhysicalPath("/localstorage/...")` (read).
- DB stores logical paths like `/localstorage/images/avatar/<file>`.
- `FileStorageServlet` (`/file`, 100MB) is the unified access point: `?action=view&id=X` (public), `?action=download`, `?action=upload&category=X`, `?action=list`, `?action=delete`. Has legacy fallback to `getRealPath()` for old records.
- Legacy `getServletContext().getRealPath()` calls remain in NewsServlet, ProjectServlet, GroupServlet, FileDownloadServlet — migrate to FileUtil when touching those.

### AI Assistant Module ([ACTION] mechanism)
- `AIServlet` (`/ai`, `/ai/*`) routes by `action`: `chat`, `send` (non-streaming JSON — what UI uses), `sendStream` (SSE, defined but unused), `execute`, `history`, `statistics`, `init`, `clear`.
- **[ACTION] flow**: LLM emits `[ACTION]actionType|k1=v1|k2=v2`; frontend extracts it and POSTs to `/ai?action=execute` → `AIService.executeAction()` dispatches via switch to DAOs.
- Role-specific prompts for GUEST/MEMBER/ADMIN; providers switched via `ai.provider` (minimax/volcengine/wenxin/qwen/openai). Empty `ai.api.key` returns canned help text (no network call).
- **Do NOT use** `service/EnhancedIntentRecognizer` or `service/ConversationContextManager` — dead code. Intent matching is inline in `AIService.buildOperationGuide` (Chinese keyword matcher).

### DAO Pattern
- One DAO per table; `DBUtil.getConnection()` → `PreparedStatement`/`ResultSet` with `?` placeholders.
- Inserts use `prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)` + `getGeneratedKeys()`.
- Dynamic queries: `StringBuilder "WHERE 1=1"` + `List<Object> params`.
- **DBUtil is NOT using HikariCP** (stub only) — every call opens/closes a raw `DriverManager` connection. HikariCP enablement is a P0 infrastructure task.
- ActivityDAO already has a Connection-parameter overload pattern that will be rolled out to all DAOs for transaction support.

### JSP Layout
- `jsp/common/layout_top.jsp` — Tabler 1.4.0 CDN + Bootstrap Icons + `css/design-system.css`, navbar + role sidebar (`admin_sidebar.jsp`/`member_sidebar.jsp`). Pages pass `param.title`/`param.active`.
- `jsp/common/layout_bottom.jsp` — footer + Tabler JS.
- Avatars: `/file?action=view&id=${memberProfile.avatarFileId}&t=<timestamp>`.
- JSTL core (`prefix="c"`), JSTL 1.2.

### Configuration
Single entry point: `config.Config.java` (static block loads properties). Priority:
1. `config.local.properties` (classpath root, gitignored)
2. `config.properties` (classpath root, committed; fills absent keys via `putIfAbsent`)

Helpers: `getProperty(key[,default])`, `getDesKey()`, `getMaxFileSize()`, `getMaxRequestSize()`, `getSessionTimeout()`, `getFileStorageBaseDir()`, `reloadConfig()` (test hook).

## Known Gotchas

1. **No Tomcat Maven plugin** — `mvn tomcat7:run` is wrong. Use `mvn package` + external Tomcat 9.
2. **DBUtil is NOT using HikariCP** — jar is present but unused; `getPoolStatus()`/`closeDataSource()` are no-ops.
3. **CSRF tokens minted but never validated server-side.** Don't rely on CSRF protection; SecurityFilter only does XSS.
4. **EnhancedIntentRecognizer / ConversationContextManager are dead code.** Don't wire them in.
5. **SSE endpoint exists (`action=sendStream`) but UI doesn't use it.** Chat uses `send`.
6. **File-path duality** — legacy servlets still use `getRealPath()`. New code must use FileUtil.
7. **Mixed servlet/filter registration** across web.xml AND `@WebServlet`/`@WebFilter`. Always check both.
8. **`src/main/resources/config.local.properties` contains real credentials** (gitignored). Never print or commit it.
9. **UserDAO logs plaintext/encrypted passwords to stdout** during login.
10. **AuthFilter does NOT cover `/attendance/*` or `/study/*`** — manual AuthHelper checks. To be fixed in P0.
11. **DES is weak crypto** with hardcoded default key `(^&%gasie_%^)`; changing breaks existing password hashes.
12. **NON_KEYWORDS must NOT include SQL syntax keywords** (especially `SET`) — only identifier/table/column names, otherwise UPDATE/INSERT parsing breaks.
13. **Regenerate H2 schema after any production DDL change**: modify `sql/software_group.sql` → run `python bin/convert_mysql_to_h2.py` → add new tables to `H2Database.reset()` tables array → `mvn verify`.
14. **Package layout is flat** — test classes in `src/test/java/{util,dao,servlet,support}/` (no `software.group` prefix).
15. **Latent production bugs discovered by tests (not yet fixed)**: `ActivityParticipantDAO.getParticipantStatus` reads column `participant_status` but selects `status`; `AwardDAO.findApproved` orders by `competition_year` which doesn't exist (column is `year`). Fix when working on those modules.

## Database
- Schema: `sql/software_group.sql` (44 tables, utf8mb4_unicode_ci). This is the authoritative source.
- Driver: `com.mysql.cj.j.Driver` (MySQL Connector/J 8.0.28).
- Key tables: `user`, `member_profile`, `admin_profile`, `activity` + `activity_participant` + `activity_group`, `award` + `award_image` + `award_member`, `project` + `project_file/image/label/plan/progress/history/member/member_application`, `news`, `recruit_application`, `problem_report`/`problem_management`, `file_storage` (central upload metadata), `group_message`/`group_member` (group chat), `attendance`/`attendance_makeup`/`attendance_config`, `study_session`, `operation_log`, `dictionary`, `resumes` + `resume_*`, AI: `ai_conversation`/`ai_message`/`ai_knowledge_base`/`ai_faq_knowledge`/`ai_faq_statistics`, `user_group` (group membership cross-ref).
