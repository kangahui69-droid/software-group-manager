# 高校软件小组管理系统 —— 技术开发文档

> 面向新加入开发成员的入门指南。读完本文档，你将能够：
> 1. 在本地搭建开发环境并启动项目
> 2. 理解项目整体架构与代码组织方式
> 3. 掌握新增功能、修复Bug的标准流程（TDD 测试驱动开发）
> 4. 了解各核心模块的实现细节与注意事项
>
> **技术栈提醒**：本项目严格使用 **JSP + Servlet + 原生JDBC**，严禁引入 Spring / Spring Boot / MyBatis / Hibernate / JPA / Lombok / Redis 等框架。测试框架使用 JUnit 5 + Mockito + AssertJ + H2 + 嵌入式Tomcat + Testcontainers，不违反上述约束。

---

## 目录

1. [快速开始：环境搭建与启动](#1-快速开始环境搭建与启动)
2. [整体架构概览](#2-整体架构概览)
3. [代码目录结构详解](#3-代码目录结构详解)
4. [核心基础设施](#4-核心基础设施)
5. [过滤器链与请求生命周期](#5-过滤器链与请求生命周期)
6. [模块开发指南](#6-模块开发指南)
7. [前端开发指南（JSP + Tabler）](#7-前端开发指南jsp--tabler)
8. [编码规范与约定](#8-编码规范与约定)
9. [数据库操作指南](#9-数据库操作指南)
10. [文件存储机制](#10-文件存储机制)
11. [AI助手模块实现说明](#11-ai助手模块实现说明)
12. [常见问题与调试技巧](#12-常见问题与调试技巧)
13. [测试框架与TDD开发流程](#13-测试框架与tdd开发流程)
14. [第二阶段开发预备知识](#14-第二阶段开发预备知识)

---

## 1. 快速开始：环境搭建与启动

### 1.1 环境要求

| 软件 | 版本要求 | 下载说明 |
|------|----------|----------|
| JDK | 1.8 及以上 | 推荐 Adoptium Temurin 8 |
| Maven | 3.6+ | 配置好国内镜像（阿里云） |
| MySQL | 8.0+ | 必须是 8.0 系列，驱动用 8.0.28 |
| Tomcat | 9.0.x | **不要用 Tomcat 10/11**（Jakarta包名不兼容） |
| IDE | IntelliJ IDEA（推荐）或 Eclipse | 装 Lombok 插件（虽然代码不使用，但IDE兼容用） |
| Git | 任意 | 代码版本管理 |

### 1.2 第一步：数据库初始化

```bash
# 1. 登录MySQL
mysql -u root -p

# 2. 创建数据库（utf8mb4支持emoji和中文生僻字）
CREATE DATABASE software_group DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. 导入建表脚本
USE software_group;
SOURCE /你的项目路径/sql/software_group.sql;

# 4. （可选）查看默认账号
SELECT username, password, role FROM user;
# 管理员：admin / admin123
# 测试成员：member1 / 123456
```

### 1.3 第二步：配置本地数据库连接

项目使用双配置文件机制：
- `src/main/resources/config.properties` —— 提交到Git的模板，含占位符
- `src/main/resources/config.local.properties` —— **本地配置（不提交Git）**，覆盖模板

复制模板并修改：

```properties
# src/main/resources/config.local.properties（你需要自己创建）
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/software_group?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
db.username=root
db.password=你的MySQL密码

# DES加密密钥（保持默认即可，不要改，改了历史密码全部失效）
des.key=(^&%gasie_%^

# 文件存储根目录（默认在项目启动目录下的localstorage，一般不需要改）
upload.baseDir=${user.dir}/localstorage
upload.maxFileSize=10485760
upload.maxRequestSize=20971520

# AI 配置（如果不需要AI功能可以不改，空key会返回兜底文本）
ai.provider=minimax
ai.api.key=your_api_key_here
ai.api.url=https://api.minimax.chat/v1/text/chatcompletion_v2
ai.model=abab6.5s-chat
ai.timeout=30000
ai.max_tokens=2048
```

> ⚠️ **重要**：`config.local.properties` 已经在 `.gitignore` 中，**永远不要把它提交到Git**，否则密码会泄露。

### 1.4 第三步：编译打包

```bash
# 在项目根目录执行（pom.xml所在目录）
mvn clean package -DskipTests
# 成功后会生成 target/software-group.war
```

如果是IDEA，直接在Maven面板双击 `package` 即可。

### 1.5 第四步：部署到Tomcat

**方式一：手动部署**
1. 把 `target/software-group.war` 复制到 Tomcat 的 `webapps/` 目录
2. 启动 Tomcat（`bin/startup.bat`）
3. 访问 `http://localhost:8080/software-group/`

**方式二：IDEA集成Tomcat（推荐开发用）**
1. Run → Edit Configurations → + → Tomcat Server → Local
2. Configure 选择你的Tomcat安装目录
3. 在 Deployment 标签页添加 Artifact → `software-group:war exploded`
4. Application context 填 `/software-group`
5. 点击 Debug 启动，支持热部署JSP和静态资源

**方式三：使用`mvn tomcat7:run`？**

❌ **不要用**。本项目没有配置Tomcat Maven插件，旧文档里有这个说法但已经失效。

### 1.6 验证是否启动成功

1. 访问 `http://localhost:8080/software-group/` 能看到首页
2. 访问 `http://localhost:8080/software-group/login.jsp` 能看到登录页
3. 用 admin / admin123 能登录后台
4. 项目根目录下会自动创建 `localstorage/` 文件夹存放上传文件

如果遇到问题先看 [第12节：常见问题与调试技巧](#12-常见问题与调试技巧)。

---

## 2. 整体架构概览

### 2.1 架构模式

本项目是经典的 **Java Web Model 2 (MVC)** 单体架构：

```
┌─────────────────────────────────────────────────────────────────┐
│                         浏览器 / 移动端                          │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP请求
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Tomcat Servlet容器                       │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    过滤器链 (Filter Chain)                 │  │
│  │  CharacterEncodingFilter → AuthFilter → LoggingFilter     │  │
│  │  → SecurityFilter (XSS清洗)                               │  │
│  └────────────────────────────┬──────────────────────────────┘  │
│                               ▼                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Servlet (控制器层)                      │  │
│  │  接收参数 → 调用DAO → 转发/重定向到JSP                    │  │
│  │  ⚠️ 大部分业务逻辑直接写在Servlet里（胖Servlet模式）      │  │
│  └────────────────────────────┬──────────────────────────────┘  │
│                               ▼                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │               DAO (数据访问层，原生JDBC)                   │  │
│  │  一个DAO对应一张表，使用PreparedStatement防止SQL注入      │  │
│  └────────────────────────────┬──────────────────────────────┘  │
│                               ▼                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    MySQL 8.0 数据库                        │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    JSP (视图层)                            │  │
│  │  使用JSTL标签 + Tabler UI组件，layout统一布局             │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 分层说明

| 层级 | 所在包 | 职责 | 数量 |
|------|--------|------|------|
| 配置层 | `config/` | 读取properties配置，单例入口 | 1个类 |
| 过滤器 | `filter/` | 编码、权限、日志、XSS | 4个类 |
| 监听器 | `listener/` | 启动定时任务（群聊禁言、自习） | 2个类 |
| Servlet层 | `servlet/` | 接收请求、参数校验、业务处理、响应 | 31个类 |
| Service层 | `service/` | **目前只有AIService**，其他模块业务逻辑在Servlet | 3个类 |
| DAO层 | `dao/` | 每个表一个DAO，纯JDBC操作 | 32个类 |
| Model层 | `model/` | POJO实体类，与数据库表对应 | 35个类 |
| 工具层 | `util/` | 通用工具：DB、文件、加密、认证、AI客户端等 | 12个类 |

> ⚠️ **架构现状提示**：项目目前是"胖Servlet"模式——除了AI模块有独立Service层，其他模块的业务逻辑都直接写在Servlet里。这是历史原因，第二阶段API化改造时会逐步抽离Service层。**写新功能时请遵循现有模式，不要强行引入Service层造成风格不统一**。

### 2.3 一次请求的完整流转

以"成员报名活动"为例，看看一个请求从头到尾走了哪些代码：

1. 浏览器点击"报名"按钮 → `POST /activity?action=register&id=5`
2. `CharacterEncodingFilter` 设置 request/response 为 UTF-8
3. `AuthFilter` 检查Session中是否有登录用户 → 有，放行
4. `LoggingFilter` 记录请求日志到 operation_log 表
5. `SecurityFilter` 对请求参数做XSS清洗
6. 请求到达 `ActivityServlet.doPost()`
7. 根据 `action=register` 进入 `registerActivity()` 方法：
   - 从Session获取当前用户
   - 校验活动是否存在、是否在报名时间、人数是否满、是否重复报名
   - 创建Registration对象，调用 `registrationDAO.insert()`
   - 重定向到活动列表页面带成功消息
8. 重定向后，新的请求经过同样的过滤器链，到达 `ActivityServlet.doGet()` 查询活动列表
9. 转发到 `jsp/activity/list.jsp` 渲染页面
10. JSP使用JSTL遍历活动列表，生成HTML返回浏览器

---

## 3. 代码目录结构详解

```
software_group_AI/
├── pom.xml                                    # Maven配置，所有依赖在这里
├── .gitignore                                 # Git忽略配置（含config.local.properties）
│
├── sql/
│   └── software_group.sql                     # ✅ 唯一完整建表脚本（44张表）
│
├── docs/                                      # 项目文档
│   ├── requirements.md                        # 需求文档
│   ├── development.md                         # 本文档（技术开发指南）
│   ├── database.md                            # 旧数据库文档（可参考）
│   └── ...
│
├── localstorage/                              # 运行时创建，不上传Git
│   ├── images/avatar/                         # 用户头像
│   ├── images/award/                          # 奖项证书图片
│   ├── files/project/                         # 项目文件
│   └── news/                                  # 新闻HTML正文
│
└── src/main/
    ├── resources/
    │   ├── config.properties                  # 配置模板（提交Git）
    │   └── config.local.properties            # 本地配置（不提交Git）
    │
    ├── java/
    │   ├── config/
    │   │   └── Config.java                    # 配置单例入口，static块加载properties
    │   │
    │   ├── filter/                            # 过滤器（按web.xml顺序执行）
    │   │   ├── CharacterEncodingFilter.java   # UTF-8编码
    │   │   ├── AuthFilter.java                # 登录与权限检查
    │   │   ├── LoggingFilter.java             # 操作日志记录
    │   │   └── SecurityFilter.java            # XSS清洗
    │   │
    │   ├── listener/
    │   │   ├── GroupMuteListener.java         # 启动群聊禁言定时任务
    │   │   └── StudySessionListener.java      # 启动自习会话定时任务
    │   │
    │   ├── model/                             # POJO，每个类对应一张表（部分表无对应）
    │   │   ├── User.java
    │   │   ├── MemberProfile.java
    │   │   ├── Activity.java
    │   │   ├── Project.java
    │   │   ├── Award.java
    │   │   ├── News.java
    │   │   └── ...（共35个）
    │   │
    │   ├── dao/                               # 数据访问对象，一个表一个DAO
    │   │   ├── UserDAO.java
    │   │   ├── ActivityDAO.java
    │   │   ├── ProjectDAO.java
    │   │   └── ...（共32个）
    │   │
    │   ├── service/
    │   │   ├── AIService.java                 # AI助手核心服务（2600+行，最大文件）
    │   │   ├── EnhancedIntentRecognizer.java # ⚠️ 死代码，没被调用
    │   │   └── ConversationContextManager.java# ⚠️ 死代码，没被调用
    │   │
    │   ├── servlet/                           # 控制器层
    │   │   ├── LoginServlet.java              # 登录/登出
    │   │   ├── ActivityServlet.java           # 活动管理（1300+行）
    │   │   ├── ProjectServlet.java            # 项目管理（1350+行）
    │   │   ├── AwardServlet.java              # 奖项管理
    │   │   ├── NewsServlet.java               # 新闻管理
    │   │   ├── AIServlet.java                 # AI助手入口
    │   │   ├── FileStorageServlet.java        # 统一文件上传下载
    │   │   └── ...（共31个）
    │   │
    │   └── util/
    │       ├── DBUtil.java                    # 数据库连接工具
    │       ├── FileUtil.java                  # 文件存储工具
    │       ├── DESUtil.java                   # DES加密（密码用）
    │       ├── AuthHelper.java                # 权限检查工具方法
    │       ├── AIClientUtil.java              # 多LLM API调用客户端
    │       ├── HtmlSanitizer.java             # HTML白名单清洗（防XSS）
    │       ├── StringUtil.java                # 字符串工具
    │       └── ...
    │
    └── webapp/                                # Web根目录（Maven标准，不要用老的WebContent/）
        ├── WEB-INF/
        │   └── web.xml                        # Servlet/Filter/Listener注册（重要！）
        ├── index.jsp                          # 网站首页
        ├── login.jsp                          # 登录页
        ├── problem-report.jsp                 # 问题反馈（游客可访问）
        │
        ├── admin/                             # 管理员入口页面
        ├── member/                            # 成员入口页面
        │
        ├── jsp/                               # 所有JSP视图
        │   ├── common/
        │   │   ├── layout_top.jsp             # 页面头部（统一引入CSS/导航栏）
        │   │   ├── layout_bottom.jsp          # 页面底部（引入JS）
        │   │   ├── admin_sidebar.jsp           # 管理员侧边栏
        │   │   └── member_sidebar.jsp          # 成员侧边栏
        │   ├── admin/                         # 管理员功能页面
        │   ├── member/                        # 成员功能页面
        │   ├── activity/ award/ project/      # 各模块JSP
        │   ├── news/ recruit/ group/
        │   ├── attendance/ study/ ai/
        │   └── error/                         # 错误页面
        │
        ├── css/                               # 样式文件（design-system.css是自定义样式）
        ├── js/                                # JavaScript文件
        └── images/                            # 静态图片资源
```

---

## 4. 核心基础设施

写任何功能前，你必须先理解这些基础设施类怎么用。

### 4.1 Config.java —— 配置读取

位置：`src/main/java/config/Config.java`

**使用方式**：静态方法直接调用，不需要new。

```java
// 读取配置项（带默认值）
String dbUrl = Config.getProperty("db.url");
String dbUrlWithDefault = Config.getProperty("db.url", "jdbc:mysql://localhost:3306/test");

// 常用便捷方法
String desKey = Config.getDesKey();                    // DES加密密钥
int maxFileSize = Config.getMaxFileSize();             // 单文件最大字节数
int sessionTimeout = Config.getSessionTimeout();       // 会话超时分钟数
String fileBaseDir = Config.getFileStorageBaseDir();   // 文件存储根目录
```

**加载优先级**：先加载 `config.local.properties`，再加载 `config.properties` 用 `putIfAbsent` 填缺。

**注意**：支持 `${user.dir}` 和 `${catalina.base}` 占位符，主要用于文件路径配置。

### 4.2 DBUtil.java —— 数据库连接

位置：`src/main/java/util/DBUtil.java`

**现状**：每次调用 `getConnection()` 都会用 `DriverManager` 创建新连接，没有使用连接池（HikariCP jar在pom里但没启用）。

**标准JDBC代码模板（照着写）**：

```java
public User findById(int id) {
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
        conn = DBUtil.getConnection();
        String sql = "SELECT * FROM user WHERE id = ? AND status = 1";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id);
        rs = pstmt.executeQuery();
        if (rs.next()) {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setUsername(rs.getString("username"));
            // ... 其他字段
            return user;
        }
        return null;
    } catch (SQLException e) {
        throw new RuntimeException("查询用户失败", e);
    } finally {
        DBUtil.closeResources(conn, pstmt, rs); // 必须关闭！
    }
}
```

**插入时获取自增ID**：

```java
public int insert(User user) {
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
        conn = DBUtil.getConnection();
        String sql = "INSERT INTO user (username, password, role) VALUES (?, ?, ?)";
        pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getRole());
        pstmt.executeUpdate();
        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            int id = rs.getInt(1);
            user.setId(id);
            return id;
        }
        return -1;
    } catch (SQLException e) {
        throw new RuntimeException("插入用户失败", e);
    } finally {
        DBUtil.closeResources(conn, pstmt, rs);
    }
}
```

**DAO方法命名约定**：
- `findById(id)` —— 主键查询
- `findByXxx(xxx)` —— 条件查询，返回单个对象
- `findListByXxx(xxx)` —— 条件查询，返回List
- `findByConditions(...)` —— 动态条件查询（用于搜索筛选）
- `insert(obj)` —— 插入，返回自增ID
- `update(obj)` —— 更新
- `deleteById(id)` —— 软删除（设置deleted=1或status=0）
- `countByXxx(xxx)` —— 统计数量

**事务处理**：目前项目几乎没用事务，只有ActivityDAO和RegistrationDAO有接受外部Connection的重载方法用于跨表事务。如果你需要跨多张表写操作（必须同时成功/失败），可以参考这个模式：

```java
// 事务模板
conn = DBUtil.getConnection();
try {
    conn.setAutoCommit(false);
    // 多个DAO操作，都传同一个conn
    dao1.someMethod(conn, ...);
    dao2.anotherMethod(conn, ...);
    conn.commit();
} catch (SQLException e) {
    conn.rollback();
    throw new RuntimeException("操作失败", e);
} finally {
    conn.setAutoCommit(true);
    DBUtil.closeResources(conn, null, null);
}
```

### 4.3 DESUtil.java —— 密码加密

位置：`src/main/java/util/DESUtil.java`

```java
// 加密（注册、改密时用）
String encrypted = DESUtil.encrypt("明文密码", Config.getDesKey());

// 验证登录
String inputEncrypted = DESUtil.encrypt(rawPassword, Config.getDesKey());
if (inputEncrypted.equals(storedPassword)) {
    // 密码正确
}
```

> ⚠️ DES是弱加密，但因为历史数据原因不能改算法。不要尝试换成BCrypt，会导致所有老用户密码失效。

### 4.4 AuthHelper.java —— 权限检查工具

位置：`src/main/java/util/AuthHelper.java`

在Servlet里直接静态调用：

```java
// 获取当前登录用户（没登录返回null）
User currentUser = AuthHelper.getCurrentUser(request);

// 检查是否已登录，没登录重定向到登录页（在doGet/doPost开头调用）
if (!AuthHelper.checkLogin(request, response)) {
    return; // 未登录已重定向，直接返回
}

// 检查是否管理员，不是管理员返回403
if (!AuthHelper.checkAdmin(request, response)) {
    return;
}

// 检查是否成员（MEMBER或ADMIN都可以）
if (!AuthHelper.checkMember(request, response)) {
    return;
}

// 判断是否管理员（不跳转，只返回布尔值）
boolean isAdmin = AuthHelper.isAdmin(request);
```

**Session里存了什么**：

| 属性名 | 类型 | 说明 |
|--------|------|------|
| `user` | `model.User` | 登录用户对象 |
| `username` | String | 用户名（冗余，方便JSP用） |
| `role` | String | 角色：ADMIN/MEMBER |
| `memberProfile` | `model.MemberProfile` | 成员档案（AuthFilter自动加载） |
| `adminProfile` | `model.AdminProfile` | 管理员档案（AuthFilter自动加载） |

在JSP里可以直接用 `${sessionScope.user.name}` 访问。

---

## 5. 过滤器链与请求生命周期

所有过滤器都在 `WEB-INF/web.xml` 中配置，**执行顺序就是web.xml中声明的顺序**：

```
请求进来 → CharacterEncodingFilter → AuthFilter → LoggingFilter → SecurityFilter → Servlet
响应出去 ← （过滤器链反向返回）← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ←
```

### 5.1 CharacterEncodingFilter

- **拦截路径**：`/*`（所有请求）
- **作用**：`request.setCharacterEncoding("UTF-8")` 和 `response.setCharacterEncoding("UTF-8")`，解决中文乱码。
- **注意**：只对POST请求的表单参数有效，GET请求参数在Tomcat的server.xml里需要配置 `URIEncoding="UTF-8"`。

### 5.2 AuthFilter（最重要的过滤器）

- **拦截路径**：`/member/*`, `/admin/*`, `/activity/*`, `/news/*`, `/project/*`, `/award/*`, `/recruit/*`, `/study/*`, `/ai/*`, `/group/*`
- **白名单（不拦截）**：
  - `/news/list`, `/news/detail`（新闻公开）
  - `/recruit/apply`, `/recruit/submit`, `/recruit/success`（招新公开）
  - `/ai`（AI助手公开）
  - `/problem`（问题反馈公开）
  - 首页 `/index.jsp`, `/`
- **权限规则**：
  - `/admin/*` 必须 ADMIN 角色
  - `/member/*` 必须 MEMBER 或 ADMIN 角色
  - 其他拦截路径只要登录即可
- **自动加载档案**：MEMBER登录用户会自动查询 `member_profile` 存入session。
- **⚠️ 已知gap**：`/attendance/*`、`/study/*` 路径未被AuthFilter拦截（这两个Servlet用@WebServlet注解注册），目前靠Servlet内部手动调用AuthHelper做权限检查。如果你在这些路径下加新功能记得加权限校验。

### 5.3 LoggingFilter

- **拦截路径**：`/*`
- **作用**：把 POST/PUT/DELETE 请求记录到 `operation_log` 表。GET请求不记日志（避免刷列表污染日志）。
- **记录内容**：用户名、访问URL、HTTP方法、IP地址、User-Agent、时间。
- **管理员可在后台"操作日志"查看**。

### 5.4 SecurityFilter

- **拦截路径**：`/*`
- **排除路径**：登录、登出、问题反馈等
- **作用**：
  1. 包装request，用Jsoup对所有请求参数做XSS清洗（移除危险标签和属性）
  2. CSRF Token已生成但目前**没有做服务端校验**（只有前端页面取token，后端没验证，是个安全gap，暂时保留）

---

## 6. 模块开发指南

这一章按模块讲解每个核心功能的Servlet怎么写，有什么注意事项。

### 6.1 Servlet开发通用模式

所有Servlet都继承 `HttpServlet`，按照下面这个模板写：

```java
@WebServlet("/your-module/*")  // 或在web.xml里注册
public class YourServlet extends HttpServlet {
    // DAO作为成员变量直接new出来，没有DI
    private YourDAO yourDAO = new YourDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. 设置编码（CharacterEncodingFilter已经做了，一般不用重复）
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 2. 获取action参数
        String action = request.getParameter("action");
        if (action == null) {
            action = "list"; // 默认action
        }

        // 3. 检查登录（如果这个Servlet不在AuthFilter拦截路径里）
        // User currentUser = AuthHelper.getCurrentUser(request);
        // if (currentUser == null) { response.sendRedirect(request.getContextPath() + "/login.jsp"); return; }

        // 4. 根据action路由
        switch (action) {
            case "list":
                list(request, response);
                break;
            case "detail":
                detail(request, response);
                break;
            case "add":      // 新增页面展示
                showAddForm(request, response);
                break;
            case "edit":     // 编辑页面展示
                showEditForm(request, response);
                break;
            // 注意：通过<a href>链接触发的操作（如删除、通过、驳回）
            // 必须在doGet里处理，因为<a>是GET请求
            case "delete":
                delete(request, response);
                break;
            case "approve":
                approve(request, response);
                break;
            default:
                response.sendError(404);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action) {
            case "save":     // 表单提交新增
                save(request, response);
                break;
            case "update":   // 表单提交更新
                update(request, response);
                break;
            // 建议：为了兼容性，POST也实现approve/delete等，方便表单提交
            case "delete":
                delete(request, response);
                break;
            case "approve":
                approve(request, response);
                break;
            default:
                response.sendError(404);
        }
    }

    // ===== 具体业务方法 =====

    private void list(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. 获取查询参数
        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        // 2. 调用DAO查询
        List<YourEntity> list = yourDAO.findByConditions(keyword, status);
        // 3. 把结果放到request
        request.setAttribute("list", list);
        // 4. 转发到JSP
        request.getRequestDispatcher("/jsp/your-module/list.jsp").forward(request, response);
    }

    private void save(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // 1. 获取表单参数
        String name = request.getParameter("name");
        // 2. 参数校验
        if (name == null || name.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/your-module?action=add&error="
                + URLEncoder.encode("名称不能为空", "UTF-8"));
            return;
        }
        // 3. 构造对象
        YourEntity entity = new YourEntity();
        entity.setName(name);
        // 4. 调用DAO
        yourDAO.insert(entity);
        // 5. 重定向（POST-REDIRECT-GET模式，防止重复提交）
        response.sendRedirect(request.getContextPath() + "/your-module?action=list&msg="
            + URLEncoder.encode("创建成功", "UTF-8"));
    }
}
```

**Servlet开发的关键约定**：

1. 所有跳转用 `response.sendRedirect()`（重定向，POST后必须重定向防重复提交）
2. 页面内部跳转用 `request.getRequestDispatcher().forward()`（转发，带request数据）
3. URL里带中文消息必须用 `URLEncoder.encode(..., "UTF-8")` 编码
4. DAO对象作为Servlet成员变量直接new，每次请求复用同一个DAO实例（DAO本身无状态，线程安全）
5. 每个Servlet自己处理 `action` 路由，不要在web.xml里为每个action配一个servlet

### 6.2 用户登录与注册模块

**相关文件**：
- Servlet：`servlet/LoginServlet.java`
- DAO：`dao/UserDAO.java`, `dao/MemberProfileDAO.java`
- 页面：`login.jsp`, `admin/index.jsp`, `member/index.jsp`
- 工具：`util/DESUtil.java`, `util/AuthHelper.java`

**登录流程**：
1. LoginServlet获取username和password
2. 密码用DES加密后调用 `userDAO.findByUsernameAndPassword(username, encryptedPwd)`
3. 查询到用户后把user对象存入session
4. 根据角色重定向到 `/admin/` 或 `/member/`

**默认账号密码**：
- admin / admin123
- member1 / 123456（招新审核通过的用户默认密码是123456）

**UserDAO.findByUsernameAndPassword注意**：这个方法在控制台会打印明文和加密后的密码（debug日志），生产环境如果要关掉可以注释那行System.out。

### 6.3 新闻模块

**相关文件**：
- Servlet：`servlet/NewsServlet.java`（`/news/*`）
- DAO：`dao/NewsDAO.java`
- 页面：
  - 公开：`jsp/news/list.jsp`, `jsp/news/detail.jsp`
  - 后台：`jsp/admin/news/manage.jsp`, `jsp/admin/news/edit.jsp`

**特殊点：新闻正文存HTML文件，不存数据库**：

- 数据库 `news.content_path` 字段存相对路径（如 `/localstorage/news/notice/1.html`）
- 正文HTML文件保存在 `localstorage/news/{type}/{id}.html`
- 保存时用富文本编辑器（当前是textarea，后续可以换成TinyMCE或wangEditor）
- 读取时根据content_path读文件内容，JSP用 `${news.content}` 输出（需注意XSS，因为是富文本信任内容，不做escape）

**注意安全**：新闻内容是管理员发布的，认为是可信的，所以JSP直接 `${content}` 输出不做转义。但其他用户输入（如评论、报名理由）必须用 `<c:out>` 转义。

### 6.4 活动模块

**相关文件**：
- Servlet：`servlet/ActivityServlet.java`（1300+行，最长的Servlet之一）
- DAO：`dao/ActivityDAO.java`, `dao/ActivityParticipantDAO.java`（也叫RegistrationDAO）
- 页面：`jsp/activity/`, `jsp/admin/activity/`

**核心业务规则**：

1. **时间窗口报名**：
   - 活动有 `registration_start_time` 和 `registration_end_time`
   - 只有在窗口期内才能报名，"报名"按钮才显示
   - 过了报名截止时间未审核的报名自动变成EXPIRED状态（动态计算，不存库）

2. **名额限制**：
   - `max_participants` 0表示不限人数
   - 报名时统计CONFIRMED状态的人数，超过限制报不上

3. **活动状态自动计算**（不是存库，是根据时间算）：
   - 当前时间 < activity_start_time → UPCOMING（即将开始）
   - start ≤ now ≤ end → ONGOING（进行中）
   - now > end → COMPLETED（已结束）
   - status字段是手动设置的CANCELED优先级最高

4. **删除规则**：
   - 有CONFIRMED报名的活动不能删
   - ONGOING/COMPLETED状态不能删
   - 删除时同时删除所有报名记录（软删除）

5. **批量审批**：管理员可以勾选多个报名记录批量通过/驳回

### 6.5 项目模块

**相关文件**：
- Servlet：`servlet/ProjectServlet.java`（1350+行）
- DAO：`dao/ProjectDAO.java`, `dao/ProjectMemberDAO.java`, `dao/ProjectPlanDAO.java`, `dao/ProjectProgressDAO.java`, `dao/ProjectHistoryDAO.java`等
- 页面：`jsp/project/`, `jsp/admin/project/`, `jsp/member/project/`

**核心特性**：

1. **双层审批**：
   - 成员申请创建项目 → 系统管理员审批（PENDING→APPROVED/REJECTED）
   - 普通成员申请加入已通过项目 → 项目管理员审批

2. **项目状态流转**：
   ```
   PENDING → APPROVED → IN_PROGRESS → COMPLETED
     ↓          ↓
   REJECTED   CANCELED
   ```

3. **年度项目数量限制**：每人每年最多创建3个项目（创建时校验）

4. **完整的项目工作区**（只有项目成员能进）：
   - 基本信息
   - 成员管理（项目管理员可以添加/移除）
   - 计划管理（时间线展示，仅项目管理员可编辑）
   - 进度更新（所有成员可添加进度条）
   - 文件/图片上传
   - 历史记录（自动记录所有变更）

5. **权限矩阵**：参考 `docs/requirements.md` 中的项目管理权限表，公开详情页只显示基础信息，成员才能看到计划/进度/文件。

### 6.6 奖项模块

**相关文件**：
- Servlet：`servlet/AwardServlet.java`（`/award`, `/award/*`）
- DAO：`dao/AwardDAO.java`, `dao/AwardMemberDAO.java`, `dao/AwardImageDAO.java`
- 页面：`jsp/award/`, `jsp/admin/award/`

**核心流程**：
1. 成员提交奖项（比赛名称、时间、等级、类型等）
2. 上传证书图片（支持多张，关联award_id）
3. 管理员审批（通过/驳回）
4. 通过后在公开列表展示，关联获奖成员

**多成员关联**：用 `award_member` 表做多对多，团队奖项可以关联多个成员。

### 6.7 招新模块

**相关文件**：
- Servlet：`servlet/RecruitServlet.java`（`/recruit/*`, `/admin/recruit/*`）
- DAO：`dao/RecruitApplicationDAO.java`, `dao/UserDAO.java`, `dao/MemberProfileDAO.java`
- 页面：公开的 `jsp/recruit/apply.jsp`，后台 `jsp/admin/recruit/`

**招新审批"通过"做了什么**（重要）：

当管理员点"通过"按钮时，RecruitServlet会：
1. 检查学号是否已存在（防止重复）
2. 创建 `user` 记录：username=学号，password=DES加密("123456")，role=MEMBER
3. 创建 `member_profile` 记录：关联user_id，填学号/专业/年级/手机/邮箱
4. 更新招新申请表状态为"已通过"

⚠️ **注意**：通过招新创建的用户默认密码是 `123456`，首次登录应该提示修改。

### 6.8 文件存储模块（重要）

**相关文件**：
- Servlet：`servlet/FileStorageServlet.java`（`/file`）—— **统一入口，用这个不要用旧的FileUploadServlet**
- DAO：`dao/FileStorageDAO.java`
- 工具：`util/FileUtil.java`

**FileStorageServlet支持的action**：

| URL | 用途 | 权限 |
|-----|------|------|
| `/file?action=view&id=123` | 查看/显示文件（图片直接显示） | 公开 |
| `/file?action=download&id=123` | 下载文件（附件形式） | 登录 |
| `/file?action=upload&category=images/avatar` | 上传文件（multipart POST） | 登录 |
| `/file?action=list&category=images/avatar` | 列出某分类下文件 | 登录 |
| `/file?action=delete&id=123` | 删除文件 | 文件所有者或管理员 |

**上传文件代码示例（在其他Servlet里调用）**：

实际上传还是走FileStorageServlet，前端表单直接提交到 `/file?action=upload&category=xxx`。上传成功返回JSON：

```json
{"success": true, "fileId": 123, "url": "/file?action=view&id=123"}
```

然后业务Servlet只需要把fileId存到自己的业务表里（比如 `member_profile.avatar_file_id`）。

**不要直接操作文件系统**，所有文件操作都通过FileUtil和FileStorageServlet。

### 6.9 群组聊天模块

**相关文件**：
- Servlet：`servlet/GroupServlet.java`（`/group/*`，页面展示）, `servlet/GroupApiServlet.java`（`/group/api/*`，AJAX接口）, `servlet/GroupMemberServlet.java`, `servlet/GroupAdminServlet.java`
- DAO：`dao/GroupMessageDAO.java`, `dao/GroupMemberDAO.java`, `dao/ActivityGroupDAO.java`
- 页面：`jsp/group/`
- 定时任务：`util/GroupMuteScheduler.java`，由 `listener/GroupMuteListener.java` 启动

**注意**：群聊禁言到期自动解除是通过 `ScheduledExecutorService` 每60秒检查一次实现的，Timer在contextInitialized时启动，contextDestroyed时关闭。

### 6.10 考勤与自习模块

**相关文件**：
- Servlet：`servlet/AttendanceServlet.java`（`/attendance/*`）, `servlet/StudySessionServlet.java`（`/study/*`）
- DAO：`dao/AttendanceDAO.java`, `dao/AttendanceConfigDAO.java`, `dao/AttendanceMakeupDAO.java`, `dao/StudySessionDAO.java`
- 定时任务：`util/StudySessionScheduler.java`
- 页面：`jsp/attendance/`, `jsp/study/`

**注意**：这两个模块用@WebServlet注解注册（不是web.xml），路径 `/attendance/*` 和 `/study/*` 没在AuthFilter拦截列表里，代码里手动调用了AuthHelper做权限检查。

### 6.11 简历模块（未完成）

**相关文件**：ResumeServlet和几个Resume*Servlet，数据库有resumes相关表，但功能尚未开发完，页面入口隐藏了。如果要完善简历功能可以基于现有框架继续做。

---

## 7. 前端开发指南（JSP + Tabler）

### 7.1 JSP布局系统

项目用统一布局，所有页面都套用 layout_top.jsp 和 layout_bottom.jsp：

```jsp
<%-- 你的页面：/jsp/xxx/list.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- 传入页面标题和侧边栏高亮项 --%>
<jsp:include page="/jsp/common/layout_top.jsp">
    <jsp:param name="title" value="活动列表 - 软件小组管理系统"/>
    <jsp:param name="active" value="activity"/> <%-- 对应sidebar里的菜单项name --%>
</jsp:include>

<%-- 页面内容开始 --%>
<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">活动列表</h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <%-- 消息提示（用Tabler alert样式）--%>
        <c:if test="${not empty param.msg}">
            <div class="alert alert-success alert-dismissible" role="alert">
                <div>${param.msg}</div>
                <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="alert alert-danger alert-dismissible" role="alert">
                <div>${param.error}</div>
                <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
            </div>
        </c:if>

        <%-- 你的页面内容 --%>
        <div class="card">
            <div class="card-body">
                <table class="table table-vcenter">
                    <thead>
                        <tr><th>名称</th><th>类型</th><th>状态</th><th>操作</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${list}" var="item">
                        <tr>
                            <td><c:out value="${item.name}"/></td>
                            <td><c:out value="${item.type}"/></td>
                            <td><span class="badge bg-green">已通过</span></td>
                            <td>
                                <a href="?action=detail&id=${item.id}" class="btn btn-sm btn-primary">查看</a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<%-- 页面内容结束 --%>

<jsp:include page="/jsp/common/layout_bottom.jsp"/>
```

### 7.2 Tabler UI使用要点

项目使用的是 **Tabler 1.4.0**（从CDN加载），基于Bootstrap 5。参考官方文档：https://tabler.io/docs/

**常用组件**：

```jsp
<%-- 按钮 --%>
<a href="?action=add" class="btn btn-primary">新增</a>
<button type="submit" class="btn btn-success">保存</button>
<button class="btn btn-danger btn-sm" onclick="return confirm('确定删除？')">删除</button>
<button class="btn btn-outline-primary">次要按钮</button>

<%-- 卡片 --%>
<div class="card">
    <div class="card-header"><h3 class="card-title">标题</h3></div>
    <div class="card-body">内容</div>
    <div class="card-footer">底部</div>
</div>

<%-- 表单 --%>
<div class="mb-3">
    <label class="form-label">名称</label>
    <input type="text" name="name" class="form-control" required>
</div>
<div class="mb-3">
    <label class="form-label">类型</label>
    <select name="type" class="form-select">
        <option value="">请选择</option>
        <option value="1">类型一</option>
    </select>
</div>

<%-- Badge状态标签 --%>
<span class="badge bg-green">已通过</span>
<span class="badge bg-yellow">审核中</span>
<span class="badge bg-red">已驳回</span>
<span class="badge bg-secondary">已过期</span>

<%-- 图标（Bootstrap Icons）--%>
<i class="ti ti-user"></i> <%-- Tabler自带图标 --%>
<i class="bi bi-person"></i> <%-- Bootstrap Icons，项目也引入了 --%>
```

### 7.3 JSTL常用标签

```jsp
<%-- 输出（自动转义防XSS，**必须用这个**不能直接${}输出用户输入）--%>
<c:out value="${user.name}"/>

<%-- 条件判断 --%>
<c:if test="${user.role == 'ADMIN'}">管理员可见内容</c:if>

<%-- 多条件 --%>
<c:choose>
    <c:when test="${status == 'APPROVED'}"><span class="badge bg-green">已通过</span></c:when>
    <c:when test="${status == 'PENDING'}"><span class="badge bg-yellow">待审核</span></c:when>
    <c:otherwise><span class="badge bg-secondary">未知</span></c:otherwise>
</c:choose>

<%-- 循环 --%>
<c:forEach items="${list}" var="item" varStatus="vs">
    ${vs.index + 1} <c:out value="${item.name}"/>
</c:forEach>

<%-- 日期格式化 --%>
<fmt:formatDate value="${activity.activityStartTime}" pattern="yyyy-MM-dd HH:mm"/>

<%-- 字符串函数 --%>
${fn:length(list)} <%-- 长度 --%>
${fn:substring(text, 0, 50)} <%-- 截取 --%>
```

⚠️ **XSS安全注意**：管理员发布的新闻富文本直接 `${content}` 输出（可信内容），但**所有用户输入的内容必须用 `<c:out>` 输出**，否则会有XSS漏洞。

### 7.4 AJAX请求

前端AJAX用原生fetch或jQuery（项目没引入jQuery，用fetch）：

```javascript
async function loadData() {
    try {
        const resp = await fetch('/software-group/group/api/messages?groupId=1');
        const data = await resp.json();
        if (data.success) {
            // 渲染数据
        } else {
            alert(data.message || '加载失败');
        }
    } catch (e) {
        console.error(e);
        alert('网络错误');
    }
}
```

后端AJAX接口返回JSON（参考GroupApiServlet的写法）：

```java
// Servlet里返回JSON
response.setContentType("application/json;charset=UTF-8");
JsonObject json = new JsonObject();
json.addProperty("success", true);
json.addProperty("message", "操作成功");
response.getWriter().write(json.toString());
```

项目用Gson做JSON序列化，也可以用JsonObject（其实是gson包里的）。

---

## 8. 编码规范与约定

### 8.1 Java编码规范

1. **命名**：
   - 类名：大驼峰 `XxxServlet`, `XxxDAO`, `Xxx`
   - 方法/变量：小驼峰 `findById`, `userName`
   - 常量：全大写下划线 `MAX_FILE_SIZE`
   - 包名：全小写 `dao`, `servlet`, `util`

2. **缩进**：4个空格，不要用tab。IDEA设置：Settings → Editor → Code Style → Java → Use tab character 取消勾选。

3. **大括号不换行**：
   ```java
   // ✅ 正确
   if (x > 0) {
       doSomething();
   }
   // ❌ 错误
   if (x > 0)
   {
       doSomething();
   }
   ```

4. **异常处理**：
   - DAO层捕获SQLException，包装成RuntimeException抛出
   - Servlet层一般不catch异常，让容器处理（会跳500错误页）
   - 不要吞异常（空catch块）

5. **日志**：目前项目用 `System.out.println` 打日志（不规范但历史原因），新代码如果要打日志可以用slf4j：
   ```java
   private static final Logger log = LoggerFactory.getLogger(YourClass.class);
   log.info("用户{}登录", username);
   log.error("查询失败", e);
   ```

### 8.2 数据库字段命名约定

| 规则 | 示例 |
|------|------|
| 表名：小写下划线，单数 | `user`, `member_profile`, `activity` |
| 字段名：小写下划线 | `user_id`, `create_time`, `activity_type` |
| 主键：固定叫 `id` | `id INT AUTO_INCREMENT PRIMARY KEY` |
| 外键：`表名单数_id` | `user_id`, `project_id` |
| 时间字段：`_time` 或 `_at` | `created_at`, `activity_start_time` |
| 软删除：`deleted TINYINT` | `0=正常, 1=已删除` |
| 状态字段：`status VARCHAR` 或 TINYINT | 用明确的字符串枚举值，不要用无意义数字 |

Java实体类用驼峰，DAO里手动映射（没有MyBatis自动映射）：
```java
// 数据库字段 create_time → Java属性 createTime
user.setCreateTime(rs.getTimestamp("created_at"));
```

### 8.3 状态值约定

| 模块 | 状态值 | 含义 |
|------|--------|------|
| 用户status | 1/0 | 启用/禁用 |
| 招新申请status | 1/2/0 | 待审核/已通过/已拒绝 |
| 奖项award_status | PENDING/APPROVED/REJECTED | 待审核/已通过/已拒绝 |
| 项目status | PENDING/APPROVED/IN_PROGRESS/COMPLETED/CANCELED/REJECTED | 对应各阶段 |
| 活动status | UPCOMING/ONGOING/COMPLETED/CANCELED | 自动计算+手动取消 |
| 报名status | PENDING/CONFIRMED/REJECTED/EXPIRED | 待确认/已确认/已驳回/已过期 |
| 新闻status | 1/0 | 发布/下线 |

⚠️ **注意**：不同模块状态值风格不统一（有的用数字有的用字符串），这是历史原因，**改现有模块时保持该模块原有风格，不要强行统一**。写新模块建议用字符串枚举值（可读性好）。

### 8.4 消息提示约定

操作成功/失败消息通过URL参数 `msg` 和 `error` 传递，JSP页面统一用Tabler alert显示：

```java
// 成功
response.sendRedirect(path + "?msg=" + URLEncoder.encode("创建成功", "UTF-8"));
// 失败
response.sendRedirect(path + "?error=" + URLEncoder.encode("名称不能为空", "UTF-8"));
```

### 8.5 Git提交规范

提交信息清晰说明改了什么：
```
feat: 新增活动签到二维码功能
fix: 修复活动报名人数统计不准问题
docs: 更新开发文档
refactor: 重构奖项DAO查询逻辑
style: 调整列表页样式
```

---

## 9. 数据库操作指南

### 9.1 动态条件查询模板

管理后台的搜索筛选几乎都用动态条件，照着这个模式写：

```java
public List<Activity> findByConditions(String keyword, String type, String status, int page, int pageSize) {
    StringBuilder sql = new StringBuilder("SELECT * FROM activity WHERE deleted = 0");
    List<Object> params = new ArrayList<>();

    // 关键词搜索（搜索多个字段）
    if (keyword != null && !keyword.trim().isEmpty()) {
        sql.append(" AND (name LIKE ? OR description LIKE ? OR location LIKE ?)");
        String like = "%" + keyword.trim() + "%";
        params.add(like);
        params.add(like);
        params.add(like);
    }
    // 类型筛选
    if (type != null && !type.isEmpty()) {
        sql.append(" AND activity_type = ?");
        params.add(type);
    }
    // 状态筛选
    if (status != null && !status.isEmpty()) {
        sql.append(" AND status = ?");
        params.add(status);
    }
    // 排序
    sql.append(" ORDER BY created_at DESC");
    // 分页
    sql.append(" LIMIT ?, ?");
    params.add((page - 1) * pageSize);
    params.add(pageSize);

    // 执行查询
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
        conn = DBUtil.getConnection();
        pstmt = conn.prepareStatement(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            pstmt.setObject(i + 1, params.get(i));
        }
        rs = pstmt.executeQuery();
        List<Activity> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs)); // 抽个私有方法映射ResultSet到对象
        }
        return list;
    } catch (SQLException e) {
        throw new RuntimeException("查询活动失败", e);
    } finally {
        DBUtil.closeResources(conn, pstmt, rs);
    }
}
```

**一定要用 `?` 占位符和PreparedStatement，绝对不能拼接SQL字符串，否则会有SQL注入漏洞！**

### 9.2 分页查询

分页需要两个方法：一个查数据列表，一个查总记录数：

```java
// 查询总数（和findByConditions条件一致）
public int countByConditions(String keyword, String type, String status) {
    // SELECT COUNT(*) FROM ... WHERE ...同样的条件
}
```

Servlet里把分页信息传给JSP：
```java
int page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;
int pageSize = 20;
List<Activity> list = activityDAO.findByConditions(keyword, type, status, page, pageSize);
int total = activityDAO.countByConditions(keyword, type, status);
int totalPages = (int) Math.ceil((double) total / pageSize);

request.setAttribute("list", list);
request.setAttribute("page", page);
request.setAttribute("totalPages", totalPages);
request.setAttribute("total", total);
```

### 9.3 数据字典（dictionary表）

下拉框选项不要硬编码在Java或JSP里，用 `dictionary` 表管理：

```sql
-- 字典表结构
CREATE TABLE dictionary (
    id INT PRIMARY KEY AUTO_INCREMENT,
    dict_type VARCHAR(50) NOT NULL,  -- 字典类型（如 ACTIVITY_TYPE）
    dict_code VARCHAR(50) NOT NULL,  -- 字典代码（如 LECTURE）
    dict_name VARCHAR(100) NOT NULL, -- 显示名称（如 讲座）
    sort_order INT DEFAULT 0,        -- 排序
    status TINYINT DEFAULT 1,
    UNIQUE KEY (dict_type, dict_code)
);
```

代码里用 `dictionaryDAO.findByType("ACTIVITY_TYPE")` 获取该类型下的所有选项，JSP里循环渲染select选项。

新增类型的字典项直接插数据库就行，不用改代码。

---

## 10. 文件存储机制

### 10.1 存储架构

```
浏览器 → /file?action=view&id=123 → FileStorageServlet → file_storage表查元数据
                                                         ↓
                                                  拼绝对路径
                                                         ↓
                                                  读取文件返回
```

**核心原则**：
- 数据库只存文件元数据（原名、类型、大小、存储路径、上传者）
- 文件本身存在webapp外面的 `localstorage/` 目录
- 不通过Tomcat的静态资源映射，全部走Servlet读取，方便做权限控制

### 10.2 FileUtil关键方法

```java
// 获取分类目录（自动创建）
File dir = FileUtil.getCategoryDir("images/avatar"); // 返回 <baseDir>/images/avatar/

// 生成存储文件名（时间戳_UUID.扩展名）
String storedName = FileUtil.generateStoredFileName(originalFilename);

// 逻辑路径转绝对物理路径（读文件用）
File file = FileUtil.resolvePhysicalPath("/localstorage/images/avatar/xxx.jpg");

// 获取存储根目录
String baseDir = FileUtil.getFileStorageBaseDir();
```

### 10.3 头像显示

在JSP里显示头像：

```jsp
<c:choose>
    <c:when test="${not empty memberProfile.avatarFileId}">
        <img src="${pageContext.request.contextPath}/file?action=view&id=${memberProfile.avatarFileId}&t=${System.currentTimeMillis()}"
             class="avatar" alt="头像">
    </c:when>
    <c:otherwise>
        <img src="${pageContext.request.contextPath}/images/default-avatar.png" class="avatar" alt="默认头像">
    </c:otherwise>
</c:choose>
```

`&t=时间戳` 是为了防止浏览器缓存旧头像，换头像后立即显示新的。

### 10.4 新功能加文件上传怎么做

1. 前端表单 `enctype="multipart/form-data"`，action提交到 `/file?action=upload&category=你的分类目录`
2. 上传成功返回fileId
3. 表单提交业务数据时把fileId一起传过去
4. 业务Servlet把fileId存到业务表的对应字段
5. 显示时用 `/file?action=view&id=xxx`

参考头像上传的完整实现：`web/js/editors.js` 里的上传逻辑，`ProfileServlet.updateProfile()` 里的保存逻辑。

---

## 11. AI助手模块实现说明

AI助手是项目里最复杂的模块之一，也是唯一有Service层的模块。

### 11.1 核心组件

- **入口**：`servlet/AIServlet.java`（`/ai`, `/ai/*`）
- **核心服务**：`service/AIService.java`（2600+行）
- **LLM客户端**：`util/AIClientUtil.java`（支持MiniMax/豆包/文心/通义/OpenAI多个提供商，简单工厂模式切换）
- **[ACTION]机制**：让AI输出操作指令，前端解析后自动执行

### 11.2 [ACTION]机制工作流

AI不能直接操作数据库，它只能输出文本，用特殊标记告诉前端要做什么：

```
用户：帮我查一下有什么即将开始的活动
AI：好的，我来帮你查询。
[ACTION]queryActivity|status=UPCOMING
```

工作流程：
1. 用户发送消息到 `/ai?action=send`
2. AIService构建带系统提示词的请求，调用大模型
3. 大模型返回回复文本（可能包含[ACTION]标记）
4. 后端返回JSON给前端 `{reply: "...", action: "queryActivity|status=UPCOMING"}`
5. 前端JS扫描回复文本：
   - 把[ACTION]前面的闲聊文本显示给用户
   - 提取action部分，POST到 `/ai?action=execute&action=queryActivity&status=UPCOMING`
6. AIService.executeAction()里有一个大switch，根据actionType调用对应DAO查询数据
7. 查询结果以文本形式返回给前端追加显示

AIService里有大量中文关键词匹配（buildOperationGuide方法），用来引导AI在合适的场景输出正确的[ACTION]。

### 11.3 AI配置

在 `config.local.properties` 里配置：

```properties
ai.provider=minimax              # minimax/volcengine/wenxin/qwen/openai
ai.api.key=你的key               # key留空或为your_api_key_here时，AIClientUtil不调用网络，返回预设帮助文本
ai.api.url=https://...           # 对应厂商API地址
ai.model=abab6.5s-chat           # 模型名
ai.max_tokens=2048
```

**无API Key时的降级模式**：AI会返回内置的帮助文本，不会报错，方便本地开发。

### 11.4 注意

- `EnhancedIntentRecognizer` 和 `ConversationContextManager` 是死代码，**不要用**，意图识别逻辑直接写在AIService里
- SSE流式接口 `action=sendStream` 已定义但前端没用，当前用的是普通POST `action=send`
- AI可以执行的操作在AIService的大switch里加，所有操作都要做权限检查（用AuthHelper判断当前用户角色）

---

## 12. 常见问题与调试技巧

### 12.1 启动类问题

**Q: 启动Tomcat后访问404？**
- 检查Application context是否配成 `/software-group`（不是 `/`）
- 检查war包是否真的部署了：看Tomcat的webapps目录下有没有software-group文件夹
- 检查Tomcat版本是不是9（不是10）

**Q: 数据库连接失败 "Unknown database 'software_group'"？**
- 先手动建库，建表脚本不会自动CREATE DATABASE
- 检查db.url里的serverTimezone参数是否正确（Asia/Shanghai）

**Q: 中文乱码？**
- 确认数据库是utf8mb4编码
- 确认Tomcat的server.xml里Connector加了 `URIEncoding="UTF-8"`（GET参数中文乱码就是这个原因）
- 所有JSP页面顶部 `<%@ page contentType="text/html;charset=UTF-8" %>`

**Q: 上传文件失败？**
- 检查localstorage目录是否有写入权限
- 检查文件大小是否超过10MB
- 确认表单加了 `enctype="multipart/form-data"`

**Q: 修改JSP不生效？**
- IDEA用war exploded模式部署，JSP修改后一般刷新浏览器就能生效
- 如果不生效：Build → Rebuild Project，或者Tomcat重启
- Java代码修改必须重启Tomcat（或者用JRebel热部署插件）

### 12.2 权限类问题

**Q: 访问某页面被重定向到登录页？**
- 检查是否真的登录了（F12看JSESSIONID Cookie）
- 检查访问路径是否在AuthFilter拦截列表里但没权限
- 检查Session是否超时（默认30分钟无操作过期）

**Q: 管理员账号登录后进不了后台？**
- 检查user表的role字段是不是 'ADMIN'（字符串，不是数字）

### 12.3 调试技巧

1. **看Tomcat日志**：`Tomcat目录/logs/catalina.out` 或IDEA的Tomcat控制台输出，所有异常堆栈都在这里
2. **在Servlet/DAO里打断点**：IDEA用Debug模式启动，可以在Java代码里打断点单步调试
3. **打印SQL**：DAO里可以临时加 `System.out.println(sql.toString())` 打印生成的SQL语句
4. **前端调试**：F12打开浏览器开发者工具，看Network标签页的请求参数和响应
5. **Session内容查看**：JSP里临时加 `${sessionScope}` 打印所有session属性
6. **直接操作数据库验证**：遇到数据问题先用SQL客户端查数据库确认数据对不对，再排查代码

### 12.4 报错页面

- 404：路径不对或Servlet没注册
- 403：权限不足
- 500：服务器内部错误，看堆栈信息找异常原因

常见500原因：
- `NullPointerException`：对象没拿到（findById返回null你没判断）
- `SQLException`：SQL写错了或字段名不对（注意数据库字段是下划线，Java是驼峰）
- `NumberFormatException`：前端传的参数不是数字但你用Integer.parseInt了
- `ClassCastException`：session里getAttribute的类型不对

### 12.5 双重注册坑

**FileStorageServlet和Resume*Servlet同时在web.xml和@WebServlet里注册了**，Tomcat会把它们注册两次，可能导致奇怪的问题。**新写的Servlet选择一种注册方式就好**，推荐用web.xml统一注册（和现有大部分Servlet保持一致）。

---

## 13. 测试框架与TDD开发流程

本项目采用 **JUnit 5 + Mockito + AssertJ + H2 + 嵌入式Tomcat + Testcontainers** 作为测试栈，支持TDD（测试驱动开发）流程。

### 13.1 为什么选这套技术栈

| 工具 | 版本 | 解决什么问题 |
|------|------|--------------|
| JUnit 5 (Jupiter) | 5.9.3 | Java 8 兼容的最后一个稳定版，支持嵌套测试、参数化测试、生命周期扩展 |
| Mockito | 4.11.0 | Mock HttpServletRequest/Connection等外部依赖，隔离被测代码 |
| AssertJ | 3.24.2 | 流式断言：`assertThat(x).isNotNull().isGreaterThan(0)`，比JUnit原生assert更可读 |
| H2 Database | 2.2.224 | 内存数据库，启动快（毫秒级），MySQL兼容模式，用于DAO层快速测试 |
| Tomcat Embed | 9.0.83 | 嵌入式启动真实Tomcat容器，端到端测试Servlet/Filter，不用部署 |
| Testcontainers | 1.18.3 | Docker启动真实MySQL 8.0，在H2方言不兼容时使用（可选） |
| Apache HttpClient | 4.5.14（已有） | 发真实HTTP请求测Servlet接口 |

**分层测试策略（从快到慢）**：

```
┌────────────────────────────────────────────────────────────┐
│  @Integration / @SlowTest  （几秒~十几秒，开发时手动触发）   │
│  - 嵌入式Tomcat 完整HTTP链路                                │
│  - Testcontainers 真MySQL DAO测试                           │
├────────────────────────────────────────────────────────────┤
│  @FastTest  （毫秒级，默认每次mvn test都跑）                 │
│  - 工具类纯单元测试（无依赖）                                │
│  - Service层测试（Mock DAO）                                │
│  - DAO层测试（H2内存库）                                    │
└────────────────────────────────────────────────────────────┘
```

TDD开发时平时只跑 `@FastTest`，保持Red-Green循环节奏；提交代码前再跑 `@SlowTest/@Integration` 验证端到端。

### 13.2 pom.xml 依赖配置

测试依赖全部加 `scope=test`，不打入生产WAR包：

```xml
<!-- ====== 测试依赖（scope=test，不进生产包） ====== -->

<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>${junit.version}</version>
    <scope>test</scope>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>${mockito.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>${mockito.version}</version>
    <scope>test</scope>
</dependency>

<!-- AssertJ 流式断言 -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>${assertj.version}</version>
    <scope>test</scope>
</dependency>

<!-- H2 内存数据库（默认） -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>${h2.version}</version>
    <scope>test</scope>
</dependency>

<!-- Tomcat 嵌入式（Servlet集成测试） -->
<dependency>
    <groupId>org.apache.tomcat.embed</groupId>
    <artifactId>tomcat-embed-core</artifactId>
    <version>${tomcat-embed.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.apache.tomcat.embed</groupId>
    <artifactId>tomcat-embed-jasper</artifactId>
    <version>${tomcat-embed.version}</version>
    <scope>test</scope>
</dependency>

<!-- Testcontainers（真MySQL，可选，要有Docker） -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>${testcontainers.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <version>${testcontainers.version}</version>
    <scope>test</scope>
</dependency>
```

Maven Surefire插件配置默认只跑 `*Test.java`，排除 `*IT.java`（集成测试）：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.1.2</version>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
        </includes>
        <excludes>
            <exclude>**/*IT.java</exclude>
        </excludes>
    </configuration>
</plugin>
```

跑集成测试：`mvn test -Dtest=**/*IT.java` 或在IDEA里右键运行。

### 13.3 测试代码目录结构

```
src/
├── main/                       # 生产代码
│   ├── java/
│   ├── resources/
│   └── webapp/
└── test/
    ├── java/
    │   └── software/group/
    │       ├── support/               # 测试基础设施（基类、工具）
    │       │   ├── FastTest.java            # @FastTest 标签注解
    │       │   ├── SlowTest.java            # @SlowTest 标签注解
    │       │   ├── IntegrationTest.java     # @IntegrationTest 标签注解
    │       │   ├── EmbeddedTomcat.java      # 嵌入式Tomcat启动器
    │       │   ├── TomcatTestBase.java      # Servlet测试基类
    │       │   ├── H2Database.java          # H2内存库初始化
    │       │   └── HttpRequest.java         # HTTP请求工具（发GET/POST）
    │       ├── util/                  # Util单元测试
    │       │   └── DESUtilTest.java
    │       ├── dao/                   # DAO测试（H2）
    │       ├── service/                # Service测试（Mock）
    │       └── servlet/                # Servlet集成测试（嵌入式Tomcat）
    └── resources/
        ├── test-config.properties        # 测试用配置（H2连接、空AI Key）
        └── h2-schema.sql                 # H2建表脚本（基于sql/software_group.sql适配）
```

### 13.4 测试标签注解

自定义注解给测试分类，不依赖于文件名：

```java
// support/FastTest.java
package software.group.support;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Tag("fast")
@Test
public @interface FastTest { }
```

`@SlowTest` 和 `@IntegrationTest` 同理，打 `@Tag("slow")` / `@Tag("integration")`。

Maven Surefire可以按Tag分组跑：
```bash
mvn test -Dgroups=fast          # 只跑快测试
mvn test -Dgroups=slow          # 只跑慢测试
mvn test                        # 默认跑所有没被排除的
```

### 13.5 三层测试写法示例

#### 13.5.1 Util层单元测试（纯Java，毫秒级）

```java
package software.group.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import software.group.support.FastTest;
import software.group.config.Config;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DES加密工具测试")
class DESUtilTest {

    private static final String KEY = Config.getDesKey();

    @FastTest
    @DisplayName("加密后再解密应能还原原文")
    void should_decrypt_back_to_original() {
        String original = "admin123";
        String encrypted = DESUtil.encrypt(original, KEY);
        String decrypted = DESUtil.decrypt(encrypted, KEY);
        assertThat(decrypted).isEqualTo(original);
    }

    @FastTest
    @DisplayName("相同明文每次加密结果应一致（确定性）")
    void should_produce_same_result() {
        String a = DESUtil.encrypt("hello", KEY);
        String b = DESUtil.encrypt("hello", KEY);
        assertThat(a).isEqualTo(b);
    }

    @FastTest
    @DisplayName("不同密码加密结果不同")
    void should_produce_different_result() {
        String a = DESUtil.encrypt("admin123", KEY);
        String b = DESUtil.encrypt("member123", KEY);
        assertThat(a).isNotEqualTo(b);
    }
}
```

**要点**：
- 测试类名 = 被测类 + `Test`，放在同名包下
- 用 `@DisplayName` 写中文描述，读起来像需求
- 用AssertJ的 `assertThat()` 流式断言
- 一个 `@FastTest` 只测一个行为

#### 13.5.2 Service层测试（Mockito Mock依赖）

TDD推荐先把业务逻辑抽到Service层（方便测），例如活动报名的时间校验：

```java
package software.group.service;

import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import software.group.support.FastTest;
import software.group.dao.ActivityDAO;
import software.group.model.Activity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("活动报名服务测试")
class ActivityServiceTest {

    @Mock
    ActivityDAO activityDAO;

    @InjectMocks
    ActivityService activityService;   // 被测Service，自动注入Mock

    @FastTest
    @DisplayName("报名截止时间已过应拒绝报名")
    void should_reject_when_deadline_passed() {
        Activity activity = new Activity();
        activity.setRegistrationEndTime(LocalDateTime.now().minusDays(1)); // 已过期
        when(activityDAO.findById(1)).thenReturn(activity);

        assertThatThrownBy(() -> activityService.register(1, 1001))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("报名已截止");
    }
}
```

**要点**：
- `@ExtendWith(MockitoExtension.class)` 启用Mockito
- `@Mock` 创建Mock对象（假的DAO，不会真查数据库）
- `@InjectMocks` 创建被测对象，把Mock自动注入进去
- `when(...).thenReturn(...)` 打桩，告诉Mock被调用时返回什么
- 异常断言用 `assertThatThrownBy`

#### 13.5.3 Servlet集成测试（嵌入式Tomcat）

继承 `TomcatTestBase`，启动一个真实的Tomcat，发HTTP请求验证：

```java
package software.group.servlet;

import org.junit.jupiter.api.DisplayName;
import software.group.support.HttpRequest;
import software.group.support.IntegrationTest;
import software.group.support.TomcatTestBase;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("登录流程集成测试")
class LoginServletIT extends TomcatTestBase {

    @IntegrationTest
    @DisplayName("正确的账号密码应能登录并跳转到成员首页")
    void should_login_success_with_correct_credentials() throws Exception {
        // 发POST登录请求
        HttpRequest.Response resp = HttpRequest.post(baseUrl() + "/login")
            .param("username", "admin")
            .param("password", "admin123")
            .execute();

        // 登录成功应该重定向到后台首页
        assertThat(resp.code()).isEqualTo(302);
        assertThat(resp.header("Location")).contains("/admin/");
        assertThat(resp.cookie("JSESSIONID")).isNotNull();
    }

    @IntegrationTest
    @DisplayName("错误密码应返回登录页错误提示")
    void should_fail_with_wrong_password() throws Exception {
        HttpRequest.Response resp = HttpRequest.post(baseUrl() + "/login")
            .param("username", "admin")
            .param("password", "wrong")
            .execute();

        // 回到登录页（转发或重定向）
        assertThat(resp.code()).isIn(200, 302);
        String body = resp.body();
        assertThat(body).containsAnyOf("密码错误", "用户名或密码");
    }
}
```

**TomcatTestBase 会做什么**：
1. 在 `@BeforeAll` 启动一个随机端口的嵌入式Tomcat
2. 自动把 `src/main/webapp` 作为docBase部署
3. 自动注册web.xml里配置的所有Servlet/Filter
4. 连接H2内存库初始化测试数据（一条admin记录）
5. 测试结束后关闭Tomcat

### 13.6 H2与MySQL兼容性注意

H2在MySQL兼容模式下能跑大部分MySQL语法，但有少数不兼容的地方：

| 问题 | 规避方式 |
|------|----------|
| `ENUM` 类型 | H2支持，但建议测试里用VARCHAR，或用专用建表脚本 |
| `AUTO_INCREMENT` | H2里也支持，写法相同 |
| `ON UPDATE CURRENT_TIMESTAMP` | H2不支持，测试时手动设updated_at |
| MySQL特有函数（如`DATE_ADD`、`IFNULL`） | H2兼容大部分，遇到不兼容的函数单独用Testcontainers真库测 |
| 表名/字段名大小写 | H2默认不区分大小写，Linux下MySQL区分，SQL里统一用小写下划线 |

策略：**默认用H2跑，遇到不兼容的那个DAO/方法单独加 `@EnabledIfDockerAvailable` 用Testcontainers真库**，不要为了兼容一个SQL就全量切换到Testcontainers。

Testcontainers真库用法：

```java
@Testcontainers
class ProjectDaoMySqlIT {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("test")
        .withUsername("test").withPassword("test");

    @FastTest
    void should_query_complex_sql() {
        // jdbc url: mysql.getJdbcUrl()
        // 真MySQL环境，能跑所有方言
    }
}
```

### 13.7 TDD开发流程（Red → Green → Refactor）

做任何新功能或修Bug都按这个循环来：

#### 第1步：写一个会失败的测试（Red）
- 不要先写实现。先写一个测试描述你要的行为。
- 测试名写清楚"应该怎样，当什么条件时"。
- 跑一下测试，看到它**变红**（失败）—— 这证明测试有效，不是永远能过的废测试。

```java
@FastTest
@DisplayName("已满员的活动不能再报名")
void should_reject_when_activity_full() {
    // 准备：一个max=2的活动，已经有2个CONFIRMED报名
    Activity activity = new Activity();
    activity.setMaxParticipants(2);
    when(activityDAO.findById(1)).thenReturn(activity);
    when(registrationDAO.countConfirmed(1)).thenReturn(2);

    // 期望：报名时抛出异常
    assertThatThrownBy(() -> service.register(1, 1001))
        .hasMessageContaining("已满");
}
```

此时跑测试会失败，因为Service还没有"人数满了"的判断逻辑。

#### 第2步：写最少代码让它通过（Green）
- 不要设计、不要抽象、不要完美，写最快让测试变绿的代码。
- 哪怕硬编码都行，目标是**绿色**。

```java
public void register(int activityId, int userId) {
    Activity a = activityDAO.findById(activityId);
    // ... 其他已有逻辑
    if (registrationDAO.countConfirmed(activityId) >= a.getMaxParticipants()) {
        throw new IllegalStateException("活动名额已满");
    }
    // ... 插入报名
}
```

跑测试，变绿。

#### 第3步：重构（Refactor）
- 现在测试在保护你了，大胆改结构：抽方法、改命名、消除重复。
- **每改一小步就跑测试**，保证一直是绿的。
- 重构完再加下一个测试。

#### 循环
```
写完一个测试 → 红 → 写代码 → 绿 → 重构 → 绿 → 写下一个测试
```

### 13.8 写测试的几个原则

1. **测试名用"应该...当..."句式**，读起来是需求文档
   ```java
   // ✅ 好
   @DisplayName("已通过的奖项不能重复审批")
   // ❌ 差
   @DisplayName("测试approveAward方法")
   ```

2. **一个测试只测一个行为**，不要一个@Test里测十件事

3. **AAA结构**：Arrange（准备数据）→ Act（执行被测代码）→ Assert（断言结果）
   ```java
   // Arrange
   Activity a = new Activity(); a.setMaxParticipants(2);
   when(dao.findById(1)).thenReturn(a);
   when(regDao.countConfirmed(1)).thenReturn(2);
   // Act + Assert
   assertThatThrownBy(() -> service.register(1, 1001))
       .hasMessageContaining("已满");
   ```

4. **不测试框架本身**：不测试Servlet容器能不能工作、不测试JDBC驱动，只测**你的业务逻辑**

5. **不依赖测试执行顺序**：每个测试独立，数据自己准备，不共享状态

6. **Bug修复先写测试**：修Bug前先写一个能复现Bug的测试（让它红），再修代码让它绿，这样Bug永远不会再回来

7. **代码提交前跑 mvn test**：保证所有 @FastTest 都是绿的

### 13.9 IDEA 运行测试配置

- **跑单个测试方法**：右键方法名 → Run
- **跑单个测试类**：右键类名 → Run
- **跑所有测试**：右键 `src/test/java` → Run 'All Tests'
- **TDD推荐配置**：装 **InfiniTest** 插件，保存文件时自动跑相关测试（不用手动点运行）
- **查看覆盖率**：右键 → Run 'xxxTest' with Coverage，能看到哪些代码被测试覆盖到

### 13.10 测试数据约定

- 单元测试：用Mock对象，不连数据库
- H2测试：每个测试类/方法用 `@BeforeEach` 初始化自己的数据，测试完回滚（通过事务或重建表）
- 集成测试：TomcatTestBase基类预置核心基础数据（admin/member1用户）
- 不要依赖真实开发库里的数据——测试必须能独立跑完

---

## 14. 第二阶段开发预备知识

虽然本文档聚焦当前Web端开发，但作为开发成员你需要知道下一阶段的技术方向：

### 14.1 第二阶段核心任务：移动化 + API化

1. **移动端**：优先开发微信小程序（Flutter/React Native作为备选），需要一套RESTful API
2. **后端改造**：现有Servlet是返回JSP的，需要逐步抽出返回JSON的API接口
3. **认证方式**：移动端不能用Cookie Session，需要支持Token认证（JWT）

### 14.2 当前架构需要为改造做的准备

**现在写新代码时就可以注意的点**：

1. **业务逻辑尽量抽离Service层**：从测试的角度也要求这样做——Servlet里的逻辑难测，抽成Service后用Mockito就能快速单元测试，TDD节奏最顺
2. **API返回格式统一**：新写AJAX接口统一返回 `{"success": true/false, "code": 200, "message": "...", "data": {...}}` 格式
3. **URL规划**：`/api/v1/xxx` 路径预留给移动端API，现在不要占用
4. **Token认证**：在现有Session认证基础上，后续会增加Token认证支持
5. **写代码前先写测试**：新功能按TDD流程开发，Service层测试用Mock快速迭代，最后加集成测试

### 14.3 长期演进方向

- 第三阶段：业务闭环（赛展一体化、Git集成、数据大屏）
- 第四阶段：AI深度赋能（AI新闻生成、知识图谱、Agent主动服务）

这些方向不需要现在做，但设计数据库和接口时可以留有余量。

---

## 附录A：快速参考速查表

| 功能 | 怎么做 |
|------|--------|
| 读配置 | `Config.getProperty("key")` |
| 获取当前用户 | `AuthHelper.getCurrentUser(request)` |
| 权限检查 | `AuthHelper.checkAdmin(request, response)` |
| 获取数据库连接 | `DBUtil.getConnection()` |
| 关闭资源 | `DBUtil.closeResources(conn, pstmt, rs)` |
| 密码加密 | `DESUtil.encrypt(pwd, Config.getDesKey())` |
| 获取文件目录 | `FileUtil.getCategoryDir("images/avatar")` |
| 返回JSON | `response.setContentType("application/json;charset=UTF-8")` |
| 重定向带中文消息 | `URLEncoder.encode("消息", "UTF-8")` |
| 输出用户内容 | `<c:out value="${xxx}"/>` 防XSS |
| 显示图片 | `/file?action=view&id=${fileId}` |
| 页头布局 | `<jsp:include page="/jsp/common/layout_top.jsp">` |
| 页尾布局 | `<jsp:include page="/jsp/common/layout_bottom.jsp"/>` |
| **写快单元测试** | 在测试类/方法上加 `@FastTest` |
| **写集成测试** | 继承 `TomcatTestBase`，发真实HTTP请求 |
| **跑快测试** | `mvn test`（默认只跑 `*Test.java`） |
| **跑所有测试含集成** | `mvn test -Dgroups=integration` |
| **Mock依赖对象** | 类上加 `@ExtendWith(MockitoExtension.class)`，字段加 `@Mock`/`@InjectMocks` |
| **断言（推荐）** | 用 `assertThat(x).isNotNull().isEqualTo(y)`（AssertJ） |

## 附录B：文件修改检查清单

**新增一张表 + 一套CRUD功能**你需要修改这些文件：

1. sql/software_group.sql —— 建表语句
2. src/main/java/model/Xxx.java —— 实体类
3. src/main/java/dao/XxxDAO.java —— DAO类
4. src/main/java/servlet/XxxServlet.java —— Servlet类
5. WEB-INF/web.xml —— 注册Servlet和URL映射
6. jsp/common/admin_sidebar.jsp 和 member_sidebar.jsp —— 加侧边栏菜单
7. 对应的JSP页面在 jsp/xxx/ 和 jsp/admin/xxx/ 目录下

**新增一个字典类型**：
1. 往dictionary表插数据即可，不用改代码

---

> **文档维护说明**：当你在开发中发现本文档与代码不符（你改了架构/加了新机制/发现文档写错了），**请及时更新本文档**。文档和代码一样重要。
>
> 遇到文档里没写到的问题，积极和团队成员沟通。
