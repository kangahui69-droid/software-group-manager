# 高校软件小组管理系统

基于 **Maven + JSP + Servlet + JDBC** 的 Java Web 项目，专为高校软件小组设计的综合性管理平台。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **构建工具** | Maven 3.x | 项目构建和依赖管理 |
| **后端框架** | Servlet 4.0 | MVC 控制器 |
| **持久层** | JDBC + HikariCP | 数据库连接池 |
| **前端技术** | JSP + JSTL | 视图层 |
| **UI 框架** | Bootstrap 5 + Tabler Admin | 响应式界面 |
| **数据库** | MySQL 8.0+ | 数据存储 |
| **Web 容器** | Apache Tomcat 9.0+ | 应用服务器 |
| **JDK 版本** | Java 1.8 | 开发环境 |

## 项目结构

```
Software_group/
├── pom.xml                      # Maven 配置文件
├── src/
│   └── main/
│       ├── java/               # Java 源代码
│       │   ├── servlet/        # Servlet 控制器
│       │   ├── dao/            # 数据访问层 (DAO)
│       │   ├── model/          # 实体类 (POJO)
│       │   ├── filter/         # Servlet 过滤器
│       │   ├── util/           # 工具类
│       │   └── config/         # 配置类
│       ├── resources/          # 配置文件
│       │   └── config.properties
│       └── webapp/             # Web 资源 (等同于 WebContent)
│           ├── WEB-INF/
│           │   └── web.xml     # Web 配置
│           ├── admin/          # 管理员页面
│           ├── member/         # 成员页面
│           ├── jsp/            # 公共 JSP
│           └── index.jsp
├── WebContent/                 # Web 资源 (开发目录)
├── docs/                       # 项目文档
├── database/                   # 数据库备份
├── tools/                      # 开发工具
└── target/                     # Maven 构建输出
    └── software-group.war      # 部署包
```

## 功能模块

| 模块 | 功能说明 | 访问角色 |
|------|----------|----------|
| **用户管理** | 登录、登出、权限控制 | ALL |
| **新闻管理** | 发布/编辑/删除、HTML文件存储 | ADMIN |
| **招新管理** | 报名表单、后台审核、自动创建用户 | ADMIN |
| **成员管理** | 个人档案、信息维护 | MEMBER |
| **活动管理** | 活动发布、时间窗口报名、状态跟踪 | ALL |
| **奖项管理** | 提交/审批、图片上传、多成员关联 | MEMBER |
| **项目管理** | 申请/审批、成员管理、计划/进度跟踪 | MEMBER |
| **日志管理** | 操作日志自动记录、分页查询 | ADMIN |
| **文件存储** | 统一上传/下载/管理、头像/奖项图片 | ALL |

## 快速开始

### 1. 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Tomcat 9.0+

### 2. 数据库配置

```sql
CREATE DATABASE software_group 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

导入数据库文件：`database/` 目录下的最新 SQL 文件

### 3. 修改配置文件

编辑 `src/main/resources/config.properties`：

```properties
# 数据库配置
db.url=jdbc:mysql://localhost:3306/software_group?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
db.username=root
db.password=your_password

# DES加密密钥
des.key=your_secret_key
```

### 4. 构建项目

```bash
# 进入项目目录
cd Software_group

# 使用 Maven 编译
mvn clean compile

# 打包 WAR 文件
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests
```

### 5. 部署到 Tomcat

**方式一：直接部署 WAR**

```bash
# 复制 WAR 文件到 Tomcat webapps 目录
cp target/software-group.war $TOMCAT_HOME/webapps/

# 启动 Tomcat
$TOMCAT_HOME/bin/startup.sh
```

**方式二：IDEA 部署**

1. 在 IDEA 中配置 Tomcat Server
2. 设置 Deployment 为 `software-group:war`
3. 点击 Run 或 Debug

### 6. 访问系统

- 地址：`http://localhost:8080/software-group/`
- 登录页：`http://localhost:8080/software-group/login.jsp`

**默认账号：**
- 管理员：`admin` / `admin123`
- 成员：`member1` / `member123`

## 文档索引

| 文档 | 说明 |
|------|------|
| [docs/INSTALL.md](docs/INSTALL.md) | 详细安装指南 |
| [docs/database.md](docs/database.md) | 数据库结构设计 |
| [docs/CONFIG_GUIDE.md](docs/CONFIG_GUIDE.md) | 配置指南 |
| [docs/高校软件小组管理系统.md](docs/高校软件小组管理系统.md) | 系统功能说明 |
| [tools/README.md](tools/README.md) | 开发工具说明 |
| AI_README.md | AI 开发规范 |

## 技术架构图

```
┌─────────────────────────────────────────────────────────────┐
│                         浏览器层                              │
│  (Bootstrap 5 + jQuery + AJAX)                              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       Tomcat 9.0+                          │
│  ┌─────────────────────────────────────────────────────┐  │
│  │                   Servlet 容器                        │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │  │
│  │  │   Filter    │  │   Servlet   │  │   Listener  │  │  │
│  │  │(Auth/Encode)│  │(MVC Control)│  │             │  │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  │  │
│  └─────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      业务逻辑层                               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │   Servlet   │  │    DAO      │  │    Model    │             │
│  │(Controller) │  │(Data Access)│  │   (POJO)    │             │
│  └─────────────┘  └─────────────┘  └─────────────┘             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        数据层                                │
│  ┌─────────────────────────────────────────────────────┐     │
│  │                  HikariCP 连接池                      │     │
│  │              (高性能数据库连接池)                       │     │
│  └─────────────────────────────────────────────────────┘     │
│                              │                                │
│                              ▼