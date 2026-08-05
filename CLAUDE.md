# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

高校软件小组管理系统 (University Software Group Management System) — a Maven WAR project for managing a university software group's activities, members, projects, awards, recruitment, attendance, group chat, and an AI assistant. Stack: **JSP + Servlet 4.0 (javax.*) + raw JDBC + MySQL 8 + Bootstrap 5/Tabler + Maven**, targeting **Java 11** (source/target) and **Tomcat 9**.

- `groupId/artifactId/version`: `software.group/software-group/1.0.0`, final WAR name `software-group`
- Context path: `/software-group`, default port 8080
- Default admin: `admin` / `admin123`; member: `member1` / `member123`
- **Project constitution (项目宪法)**: `RULES.md` — 8 mandatory rules. Read it before writing code.

## Refactoring Phases

| Phase | Status | Description |
|-------|--------|-------------|
| P0 | ✅ Completed | Infrastructure: HikariCP, TransactionTemplate, Result, BaseApiServlet, AuthFilter extension |
| P1 | ✅ Completed | Core Service layer: Activity, User, File, Project, Award, AI Services |
| P2 | ✅ Completed | Core REST API layer: 5 API Servlets + 303 tests |
| P3 | ✅ Completed | Group/Attendance/Recruit/Resume Services + APIs |
| P4 | ✅ Completed | News/Problem/Member Services + APIs |
| P5 | ✅ Completed | Study/Log Services + APIs (15 Services, 14 API Servlets total) |

See `docs/服务分层与API化完整计划.md` for detailed progress.

## Build & Test Commands

```bash
mvn clean compile                          # compile only
mvn clean package                          # produces target/software-group.war
mvn test                                   # unit + DAO tests (*Test.java), ~1s
mvn test -Dtest=ClassName                  # run a single test class
mvn test -Dtest=ClassName#methodName       # run a single test method
mvn test -Dgroups=fast                     # @FastTest only (milliseconds)
mvn verify                                 # unit + integration tests (*IT.java, Embedded Tomcat), ~5s

# API layer tests only
mvn test -Dtest=*ApiServletTest,ApiServletRegistrationTest
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
├── config/       # Config.java — single config entry point
├── dao/          # 31 DAOs (one per table), raw JDBC via DBUtil.getConnection()
├── dto/          # Data Transfer Objects (AwardDTO, ProjectDTO, etc.)
├── filter/       # CharacterEncodingFilter → AuthFilter → LoggingFilter → SecurityFilter (web.xml order)
├── listener/     # StudySessionListener, GroupMuteListener
├── model/        # POJOs (~35 entities)
├── service/      # 15 production services: Activity, User, File, Project, Award, AI, Group, Attendance, Recruit, Resume, News, Problem, Member, Study, Log
├── servlet/      # ~31 Servlets + 14 API Servlets (API Servlet registration via web.xml only, not @WebServlet)
└── util/         # DBUtil (HikariCP), DESUtil, FileUtil, AuthHelper, TransactionTemplate, Result, etc.
src/main/webapp/  # Web root (migrated from old WebContent/ — do NOT use WebContent/)
├── WEB-INF/web.xml
├── admin/ member/  # Role entry pages
├── jsp/            # JSP views (admin/, member/, award/, ai/, common/)
├── css/ js/ images/
└── index.jsp, login.jsp, problem-report.jsp
src/main/resources/
├── config.properties          # Committed template (placeholders only)
└── config.local.properties    # Gitignored — real passwords/keys go here
src/test/java/                 # Same flat package layout (support/util/dao/servlet)
sql/software_group.sql         # Authoritative schema (44 tables)
bin/convert_mysql_to_h2.py     # H2 schema regeneration script
docs/
├── requirements.md            # V2.0 requirements
├── development.md             # Dev guide — TDD workflow, coding conventions
└── 服务分层与API化完整计划.md  # P0-P5 refactoring plan (all phases completed)
```

### Key Design Decisions

**Package layout is flat** — no `software.group.*` parent package. Test classes must mirror this (e.g. `src/test/java/dao/UserDAOTest.java`, not `src/test/java/software/group/dao/...`). This also lets tests access package-private helpers.

**No DI container** — Servlets instantiate DAOs directly as fields. Service layer uses constructors for testability (P1 completed) without introducing Spring.

**Test-prod DB switching via Config** — `DBUtil.getConnection()` reads driver/url/user/password from `Config` on every call (not cached in static final), so tests can switch to H2 by having `config.local.properties` on the test classpath. Production code is unaffected.

### REST API Layer (P2-P5)

All API Servlets extend `BaseApiServlet` and return unified JSON: `{"code":0,"message":"ok","data":...}`

| Servlet | Path | Purpose |
|---------|------|---------|
| ActivityApiServlet | `/api/activities/*` | CRUD, registration, approval |
| UserApiServlet | `/api/*` | Auth, profile, avatar |
| FileApiServlet | `/api/files/*` | Upload, download, view, delete |
| ProjectApiServlet | `/api/projects/*` | Full project lifecycle |
| AwardApiServlet | `/api/awards/*` | Submission, approval, statistics |
| GroupApiServlet | `/api/groups/*` | Group chat, members, messages |
| AttendanceApiServlet | `/api/attendance/*` | Check-in/out, stats, makeup |
| RecruitApiServlet | `/api/recruit/*` | Application, approval |
| ResumeApiServlet | `/api/resumes/*` | Resume CRUD, education, skills, projects, awards |
| NewsApiServlet | `/api/news/*` | News CRUD, publish/unpublish |
| ProblemApiServlet | `/api/problems/*` | Problem reporting and management |
| MemberApiServlet | `/api/members/*` | Member management, profile |
| StudyApiServlet | `/api/study/*` | Study session tracking, statistics |
| LogApiServlet | `/api/logs/*` | Operation log query |

### Filter Chain (order defined in web.xml)
1. **CharacterEncodingFilter** (`/*`) — forces UTF-8
2. **AuthFilter** — protects `/member/*`, `/admin/*`, `/api/*`, etc.; returns 401 JSON for API, redirects for JSP
3. **LoggingFilter** (`/*`) — logs POST/PUT/DELETE to `operation_log` table
4. **SecurityFilter** (`/*`) — XSS sanitizer (Jsoup). **CSRF NOT enforced**

### Authentication
- Passwords DES-encrypted (`util/DESUtil`, key from config).
- Session attributes: `user`, `username`, `role` (GUEST/MEMBER/ADMIN), `memberProfile`/`adminProfile`. 30min timeout.
- Use `util.AuthHelper` (`getCurrentUser`, `checkAdmin`, `isAdmin`) for programmatic checks.

### File Storage
- **Uploads at `${user.dir}/localstorage/`** (outside webapp, survives `mvn clean`).
- Use `util.FileUtil`: `getCategoryDir("images/avatar")`, `resolvePhysicalPath("/localstorage/...")`.
- DB stores logical paths like `/localstorage/images/avatar/<file>`.
- `FileStorageServlet` (`/file`) is the unified access point.

### AI Assistant Module
- `AIServlet` (`/ai`, `/ai/*`) routes by `action`: `chat`, `send`, `sendStream` (SSE, unused), `execute`, `history`, `statistics`, `init`, `clear`.
- **[ACTION] flow**: LLM emits `[ACTION]actionType|k1=v1|k2=v2`; frontend POSTs to `/ai?action=execute` → `AIService.executeAction()` dispatches to DAOs.
- Providers: minimax/volcengine/wenxin/qwen/openai (switched via `ai.provider`).
- **Dead code**: `service/EnhancedIntentRecognizer`, `service/ConversationContextManager` — do NOT use.

### DAO Pattern
- One DAO per table; `DBUtil.getConnection()` → `PreparedStatement`/`ResultSet`.
- Inserts use `Statement.RETURN_GENERATED_KEYS`.
- Dynamic queries: `StringBuilder "WHERE 1=1"` + `List<Object> params`.
- **DBUtil uses HikariCP** — `getConnection()` from pool; `getPoolStatus()`/`closeDataSource()` functional.
- Transaction support: write methods have `Connection conn` overload for Service-layer transactions.

### JSP Layout
- `jsp/common/layout_top.jsp` — Tabler 1.4.0 CDN + Bootstrap Icons, navbar + role sidebar.
- `jsp/common/layout_bottom.jsp` — footer + Tabler JS.
- Avatars: `/file?action=view&id=${memberProfile.avatarFileId}&t=<timestamp>`.
- JSTL core (`prefix="c"`), JSTL 1.2.

### Configuration
Single entry point: `config.Config.java`. Priority:
1. `config.local.properties` (classpath root, gitignored — real credentials)
2. `config.properties` (committed template; fills absent keys via `putIfAbsent`)

Helpers: `getProperty(key[,default])`, `getDesKey()`, `getMaxFileSize()`, `getSessionTimeout()`, `getFileStorageBaseDir()`, `reloadConfig()` (test hook).

## Known Gotchas

1. **No Tomcat Maven plugin** — `mvn tomcat7:run` is wrong. Use `mvn package` + external Tomcat 9.
2. **CSRF tokens minted but never validated server-side.** SecurityFilter only does XSS, not CSRF.
3. **SSE endpoint exists (`action=sendStream`) but UI doesn't use it.** Chat uses `send`.
4. **File-path duality** — legacy servlets still use `getRealPath()`. New code must use FileUtil.
5. **Mixed servlet/filter registration** across web.xml AND `@WebServlet`/`@WebFilter`. Always check both.
6. **`config.local.properties` contains real credentials** (gitignored). Never print or commit it.
7. **UserDAO logs plaintext/encrypted passwords to stdout** during login.
8. **DES is weak crypto** with hardcoded default key `(^&%gasie_%^)`; changing breaks existing hashes.
9. **NON_KEYWORDS must NOT include SQL syntax keywords** (especially `SET`) — only identifier/table/column names.
10. **Regenerate H2 schema after DDL change**: `sql/software_group.sql` → `python bin/convert_mysql_to_h2.py` → add tables to `H2Database.reset()` → `mvn verify`.
11. **Package layout is flat** — test classes in `src/test/java/{util,dao,servlet,support}/` (no `software.group` prefix).
12. **API Servlet registration** — @WebServlet annotation doesn't work reliably in IDEA; use web.xml manual registration only.

## Project Constitution (RULES.md)

8 mandatory rules enforced in all code. Key points:

| Rule | Summary |
|------|---------|
| 1 | CSS variables for all visual tokens (no hardcoded colors/spacing/sizes) |
| 2 | Async operations must handle loading/empty/error/success four states |
| 3 | All array operations must null-check before access |
| 4 | Use function components + Hooks (no Class components) |
| 5 | Every component/Service method/DAO write needs unit tests |
| 6 | Naming: PascalCase components, camelCase variables, UPPER_SNAKE constants |
| 7 | No `any` type in TypeScript; no `Map<String, Object>` in Java |
| 8 | All Props must have explicit TypeScript interfaces |

## Database

- Schema: `sql/software_group.sql` (44 tables, utf8mb4_unicode_ci) — authoritative source
- Driver: `com.mysql.cj.j.Driver` (MySQL Connector/J 8.0.28)
- Connection pool: HikariCP (maximumPoolSize=20, minimumIdle=5)

Key tables: `user`, `member_profile`, `admin_profile`, `activity` + `activity_participant` + `activity_group`, `award` + `award_image` + `award_member`, `project` + `project_file/image/label/plan/progress/history/member/member_application`, `news`, `recruit_application`, `file_storage`, `group_message`/`group_member`, `attendance`, `study_session`, `operation_log`, `dictionary`, AI: `ai_conversation`/`ai_message`/`ai_knowledge_base`, `user_group`.
