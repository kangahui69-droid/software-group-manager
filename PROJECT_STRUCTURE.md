# 项目结构整理说明

## 整理日期
2026-03-10

## 主要变更

### 1. 创建 disposable 目录

**位置**：`src/main/java/disposable/`

**用途**：存放一次性工具程序

**迁移的文件**：
- `CheckAdminPassword.java` - 密码验证工具
- `DecryptTest.java` - 解密测试
- `DirectPasswordFix.java` - 直接密码修复
- `FinalPasswordFix.java` - 最终密码修复
- `FixAdminPassword.java` - 管理员密码修复
- `LoginDiagnostic.java` - 登录诊断
- `LoginDiagnosticTool.java` - 诊断工具
- `PasswordGenerator.java` - 密码生成器
- `PasswordVerify.java` - 密码验证
- `PasswordVerifyTest.java` - 密码测试（从test目录迁移）
- `*.class` - 编译后的类文件

### 2. 清理 test 目录

**操作**：删除 `src/main/java/test/` 目录

**原因**：测试类已移至 disposable 目录

### 3. 更新 util 目录

**保留的核心工具类**：
- `DBUtil.java` - 数据库连接工具
- `DESUtil.java` - DES加密工具
- `FileUtil.java` - 文件操作工具
- `StringUtil.java` - 字符串处理工具

**移除的文件**：所有一次性工具类和.class文件

### 4. 创建/更新文档

**新增文档**：
- `AI_README.md` - AI智能体开发指南
- `src/main/java/disposable/README.md` - 一次性工具说明
- `PROJECT_STRUCTURE.md` - 本文档

## 当前项目结构

```
Software_group/
├── pom.xml                         # Maven配置文件
├── README.md                       # 项目主文档
├── AI_README.md                    # AI开发指南
├── PROJECT_STRUCTURE.md            # 项目结构说明
├── QUICKSTART.md                   # 快速开始指南
│
├── src/
│   └── main/
│       ├── java/
│       │   ├── config/             # 配置类
│       │   │   ├── Config.java
│       │   │   ├── ConfigManager.java
│       │   │   └── SimpleConfig.java
│       │   │
│       │   ├── dao/                # 数据访问对象 (14个DAO)
│       │   │   ├── ActivityDAO.java
│       │   │   ├── ActivityParticipantDAO.java
│       │   │   ├── AdminProfileDAO.java
│       │   │   ├── AwardDAO.java
│       │   │   ├── AwardImageDAO.java
│       │   │   ├── DictionaryDAO.java
│       │   │   ├── FileStorageDAO.java
│       │   │   ├── MemberProfileDAO.java
│       │   │   ├── NewsDAO.java
│       │   │   ├── OperationLogDAO.java
│       │   │   ├── ProjectDAO.java
│       │   │   ├── RecruitApplicationDAO.java
│       │   │   ├── RegistrationDAO.java
│       │   │   └── UserDAO.java
│       │   │
│       │   ├── disposable/         # 一次性工具（AI不要修改）
│       │   │   ├── README.md
│       │   │   ├── CheckAdminPassword.java
│       │   │   ├── DecryptTest.java
│       │   │   ├── DirectPasswordFix.java
│       │   │   ├── FinalPasswordFix.java
│       │   │   ├── FixAdminPassword.java
│       │   │   ├── LoginDiagnostic.java
│       │   │   ├── LoginDiagnosticTool.java
│       │   │   ├── PasswordGenerator.java
│       │   │   ├── PasswordVerify.java
│       │   │   ├── PasswordVerifyTest.java
│       │   │   └── *.class
│       │   │
│       │   ├── filter/             # Servlet过滤器
│       │   │   ├── AuthFilter.java
│       │   │   ├── CharacterEncodingFilter.java
│       │   │   └── LoggingFilter.java
│       │   │
│       │   ├── model/              # 实体类 (18个Model)
│       │   │   ├── Activity.java
│       │   │   ├── AdminProfile.java
│       │   │   ├── Award.java
│       │   │   ├── AwardImage.java
│       │   │   ├── Dictionary.java
│       │   │   ├── FileStorage.java
│       │   │   ├── MemberProfile.java
│       │   │   ├── News.java
│       │   │   ├── OperationLog.java
│       │   │   ├── Project.java
│       │   │   ├── ProjectHistory.java
│       │   │   ├── ProjectMember.java
│       │   │   ├── ProjectMemberApplication.java
│       │   │   ├── ProjectPlan.java
│       │   │   ├── ProjectProgress.java
│       │   │   ├── RecruitApplication.java
│       │   │   ├── Registration.java
│       │   │   └── User.java
│       │   │
│       │   ├── servlet/              # Servlet控制器 (16个Servlet)
│       │   │   ├── ActivityServlet.java
│       │   │   ├── AdminServlet.java
│       │   │   ├── AvatarUploadServlet.java
│       │   │   ├── AwardServlet.java
│       │   │   ├── FileDownloadServlet.java
│       │   │   ├── FileStorageServlet.java
│       │   │   ├── FileUploadServlet.java
│       │   │   ├── LoginServlet.java
│       │   │   ├── LogoutServlet.java
│       │   │   ├── LogServlet.java
│       │   │   ├── MemberServlet.java
│       │   │   ├── NewsServlet.java
│       │   │   ├── PasswordServlet.java
│       │   │   ├── ProfileServlet.java
│       │   │   ├── ProjectServlet.java
│       │   │   └── RecruitServlet.java
│       │   │
│       │   ├── util/                 # 核心工具类
│       │   │   ├── DBUtil.java       # 数据库连接
│       │   │   ├── DESUtil.java      # DES加密
│       │   │   ├── FileUtil.java     # 文件操作
│       │   │   └── StringUtil.java   # 字符串处理
│       │   │
│       │   └── test/                 # 已删除（移至disposable）
│       │
│       ├── resources/
│       │   └── config.properties     # 配置文件
│       │
│       └── webapp/                   # Web资源
│           ├── WEB-INF/
│           │   └── web.xml           # Web配置
│           ├── admin/                # 管理员页面
│           ├── member/               # 成员页面
│           ├── jsp/                  # 公共JSP
│           └── index.jsp
│
├── WebContent/                       # Web资源（开发目录，同webapp）
├── docs/                             # 项目文档
├── database/                         # 数据库备份
├── tools/                            # 开发工具
├── lib/                              # 依赖库
├── target/                           # Maven构建输出
└── pom.xml                           # Maven配置
```

## 总结

本次整理主要完成了以下工作：

1. **创建disposable目录**：将13个一次性工具类和编译文件迁移至此
2. **清理test目录**：删除空的test目录
3. **简化util目录**：仅保留4个核心工具类
4. **新增3个重要文档**：
   - `AI_README.md` - AI开发规范
   - `disposable/README.md` - 工具说明
   - `PROJECT_STRUCTURE.md` - 项目结构说明

项目现在更加整洁，核心代码与工具代码分离，便于后续开发和维护。
