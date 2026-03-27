# 高校软件小组管理系统 - 项目理解

## 项目概述

**项目名称**: 高校软件小组管理系统  
**类型**: Java Web 应用 (Maven WAR 项目)  
**技术栈**: Servlet + JSP + MySQL + HikariCP  
**前端**: Tabler 主题 (Bootstrap 变体)，原生 JavaScript  
**AI 集成**: MiniMax M2.7 模型 (流式输出)

## 技术架构

### 核心框架
- **Java 8** (源码 level)
- **Servlet 4.0** (Tomcat 9.x)
- **JSP + JSTL 1.2**
- **MySQL 8.0** + **HikariCP** (连接池)

### 关键依赖
| 库 | 用途 |
|---|---|
| HikariCP | 数据库连接池 |
| Gson | JSON 序列化 |
| Jsoup | XSS 防护 (HTML 净化) |
| Apache HttpClient | AI API 调用 |
| SLF4J + simple | 日志 |

### 前端
- **Tabler 主题** v1.4.0 (Bootstrap 变体，非标准 Bootstrap)
- **Bootstrap Icons** v1.10.0
- 原生 `<dialog>` 元素替代 Bootstrap Modal
- SSE (Server-Sent Events) 实现 AI 流式输出

## 项目结构

```
softwaregroup5/
├── src/main/java/
│   ├── servlet/          # Servlet 控制器 (25个)
│   ├── dao/              # 数据访问层
│   ├── model/            # 数据模型
│   ├── service/          # 业务逻辑
│   ├── filter/           # 过滤器 (AuthFilter)
│   ├── util/             # 工具类 (含 AI 客户端)
│   └── config/           # 配置类
├── src/main/resources/
│   └── config.properties # 主配置文件
├── src/main/webapp/      # Maven webapp 源目录
├── WebContent/           # IDE 部署目录 (实际运行用)
├── target/software-group/# 编译输出目录
├── pom.xml               # Maven 配置
└── sql/                  # 数据库脚本
```

### 三目录同步机制
项目存在**三个目录**需要保持同步：
1. `src/main/webapp/` - Maven 源目录
2. `WebContent/` - IDE 部署目录 (服务器实际读取)
3. `target/software-group/` - 编译输出目录

**重要**: 修改 JSP/静态文件后需要同步到三个目录，或运行 `mvn compile`

## 用户角色

| 角色 | Role 值 | 说明 |
|---|---|---|
| 游客 | GUEST | 未登录用户 |
| 成员 | MEMBER | 普通成员 |
| 管理员 | ADMIN | 管理员 |

## 核心功能模块

### 1. 认证与授权
- **LoginServlet** - 登录处理
- **AuthFilter** - 权限过滤器 (web.xml 配置)
- **AuthHelper** - 权限辅助工具

### 2. 新闻管理
- **NewsServlet** - 新闻 CRUD，支持类型：通知/获奖/活动
- 支持富文本编辑

### 3. 活动管理
- **ActivityServlet** - 活动发布、报名管理

### 4. 项目管理
- **ProjectServlet** - 项目申请、审批

### 5. 奖项管理
- **AwardServlet** - 奖项申请、审核

### 6. 招新管理
- **RecruitServlet** - 招新报名、审核

### 7. 考勤管理 (融合页面)
- **AttendanceServlet** - 签到/签退记录
- **StudySessionServlet** - 学习时段记录
- 融合页面 `/attendance/manage` 同时显示签到和学习记录

### 8. AI 助手
- **AIServlet** - AI 对话处理
- **AIService** - AI 业务逻辑
- **AIClientUtil** - MiniMax API 客户端
- 支持 SSE 流式输出
- AI 配置: `config.properties` 中的 `ai.*` 项

### 9. 问题反馈
- **ProblemReportServlet** - 游客/成员问题提交
- **MemberProblemServlet** - 成员问题提交
- **ProblemManagementServlet** - 管理员问题管理

### 10. 成员管理
- **MemberServlet** - 成员信息管理
- **AdminServlet** - 管理员控制台 (Dashboard)

## 重要配置

### 数据库
```properties
db.url=jdbc:mysql://localhost:3306/software_group
db.username=root
db.password=Fu2006220
```

### AI 配置
```properties
ai.provider=minimax
ai.api.key=sk-cp-txjhN6kZo9T_MTDDJ2JP2dPhlu6LaQVEAUf0Ipdffz4t_t-y8GWQNITks5tIhO-ZnmaDXqCsrhbf0tgEsP2-CrARmHUgsCN7FkHofEfhfoCsIWVcvqURyRw
ai.api.url=https://api.minimaxi.com/v1/chat/completions
ai.model=MiniMax-M2.7
```

### Servlet 映射 (web.xml)
部分 Servlet 使用 `@WebServlet` 注解，部分使用 `web.xml` 配置。**注意**: 不能同时使用，否则冲突。

### AuthFilter 白名单 (web.xml)
需要包含的路径：
- `/login.jsp`
- `/logout`
- `/ai`
- `/news`
- `/recruit/apply.jsp`
- `/problem-report.jsp`
- `/member/problem`
- `/problem`

## 常见问题与修复

### 1. Bootstrap Modal 错误
**问题**: `bootstrap is not defined`  
**原因**: Tabler 主题不暴露全局 `bootstrap` 对象  
**解决**: 使用原生 HTML `<dialog>` 元素替代 Bootstrap Modal

### 2. Servlet 映射冲突
**问题**: `cannot be mapped to one url-pattern`  
**原因**: 同时使用 `@WebServlet` 注解和 `web.xml` 配置  
**解决**: 移除 `@WebServlet` 注解或 web.xml 配置

### 3. 目录同步问题
**问题**: 修改后页面不更新  
**原因**: 三目录未同步  
**解决**: 修改后同步到三个目录或重新 `mvn compile`

### 4. Admin Dashboard 统计数据为 0
**问题**: 直接访问 `/admin/index.jsp` 时 pending counts 为 0  
**原因**: 绕过 AdminServlet，未加载统计数据  
**解决**: 使用 `/admin/dashboard` 访问

### 5. AI API 404 错误
**问题**: MiniMax API 返回 404  
**解决**: 确认 API URL 为 `https://api.minimaxi.com/v1/chat/completions`

## 页面路径速查

| 功能 | URL |
|---|---|
| 登录 | `/login.jsp` |
| 首页 | `/index.jsp` |
| 管理后台首页 | `/admin/dashboard` (通过 AdminServlet) |
| AI 对话 | `/ai?action=chat` |
| AI 统计 | `/ai?action=statistics` |
| 考勤管理 | `/attendance/manage` |
| 问题反馈(管理员) | `/admin/problem` |
| 问题反馈(成员) | `/member/problem` |
| 问题反馈(游客) | `/problem-report.jsp` |
| 学习中心 | `/study` |
| 新闻列表 | `/news?type=xxx` |

## 构建与部署

```bash
# 编译
mvn clean compile

# 打包 WAR
mvn package

# 部署到 Tomcat
# 1. 复制 WAR 到 Tomcat webapps/
# 2. 或使用 IDE (如 IntelliJ) 直接部署
```

**注意**: 修改 Java 代码需要重新编译，修改 JSP/静态文件需要同步或重新部署。

## 代码规范注意事项

1. **JSP 文件头**: 必须包含 `<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>`
2. **EL 表达式**: 使用 `${pageContext.request.contextPath}` 获取上下文路径
3. **XSS 防护**: 使用 `HtmlSanitizer.sanitizeBasic()` 净化用户输入
4. **JSON 响应**: 使用 `response.setContentType("application/json;charset=UTF-8")`
5. **重定向**: POST 请求后应使用 redirect，避免表单重复提交
