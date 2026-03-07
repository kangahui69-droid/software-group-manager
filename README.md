# 高校软件小组管理系统

基于 JSP + Servlet + JDBC 的传统 Java Web 项目，专为高校软件小组设计的综合性管理平台。

## 技术栈

- **后端**: Servlet 4.0 + JDBC
- **前端**: JSP + JSTL + Bootstrap 5 + Tabler Admin
- **数据库**: MySQL 8.0+
- **容器**: Apache Tomcat 9.0+
- **Java**: 1.8

## 项目结构

```
Software_group/
├── src/                    # Java源代码
│   ├── servlet/           # Servlet控制器
│   ├── dao/               # 数据访问层
│   ├── model/             # 实体类
│   ├── filter/            # 过滤器
│   ├── util/              # 工具类
│   └── config/            # 配置类
├── WebContent/            # Web资源 (JSP页面、CSS、JS、图片)
├── lib/                   # 依赖jar包
├── docs/                  # 项目文档
├── database/              # 数据库备份文件
├── scripts/               # 启动脚本
├── build/                 # 编译输出目录
└── out/                   # IDE输出目录
```

## 功能模块

| 模块 | 说明 |
|------|------|
| 用户管理 | 登录、权限控制 |
| 新闻管理 | 发布/编辑/删除新闻 |
| 招新管理 | 招新表单、报名管理 |
| 成员管理 | 成员档案、个人信息 |
| 活动管理 | 活动发布、报名、状态跟踪 |
| 奖项管理 | 奖项提交、审批、图片上传 |
| 项目管理 | 项目申请、审批、成员管理、进度跟踪 |
| 日志管理 | 操作日志记录 |

## 快速开始

详见 [docs/INSTALL.md](docs/INSTALL.md)

### 1. 数据库配置

```sql
CREATE DATABASE software_group CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

导入数据库文件: `database/` 目录下的 SQL 文件

### 2. 修改数据库连接配置

编辑 `src/util/DBUtil.java` 中的数据库配置

### 3. 部署

将项目部署到 Tomcat 9.0+，访问 `http://localhost:8080/Software_group`

### 默认账号

- **管理员**: admin / admin123
- **成员**: member1 / member123

## 文档

- [安装指南](docs/INSTALL.md)
- [数据库结构](docs/database.md)
- [字典说明](docs/dictionary_doc.md)
- [测试用例](docs/测试用例文档.md)
- [系统说明](docs/高校软件小组管理系统.md)

## 许可证

本项目为教学示范项目，仅供学习和研究使用。
