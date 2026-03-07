# 安装部署指南

## 环境要求

- JDK 1.8 或更高版本
- MySQL 5.7 或更高版本
- Apache Tomcat 8.5 或更高版本
- Eclipse/IntelliJ IDEA（可选，用于开发）

## 部署步骤

### 1. 数据库准备

#### 1.1 创建数据库

```sql
CREATE DATABASE software_group CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 1.2 执行建表脚本

使用MySQL客户端执行 `database.sql` 脚本：

```bash
mysql -u root -p software_group < database.sql
```

或者在MySQL客户端中：

```sql
USE software_group;
SOURCE database.sql;
```

#### 1.3 验证数据库

检查表是否创建成功：

```sql
SHOW TABLES;
```

应该能看到以下表：
- user
- member_profile
- news
- recruit_application
- activity
- activity_participant
- award
- award_member
- project
- project_member
- file_storage
- operation_log

### 2. 配置数据库连接

编辑 `src/util/DBUtil.java`，修改数据库连接信息：

```java
private static final String URL = "jdbc:mysql://localhost:3306/software_group?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai";
private static final String USERNAME = "root";  // 修改为你的数据库用户名
private static final String PASSWORD = "root";  // 修改为你的数据库密码
```

### 3. 添加依赖jar包

#### 3.1 下载MySQL驱动

从MySQL官网下载 `mysql-connector-java-8.0.x.jar`，放到项目的 `lib/` 目录。

下载地址：https://dev.mysql.com/downloads/connector/j/

#### 3.2 Servlet和JSP API

如果使用IDE开发，需要将以下jar包添加到编译路径（Tomcat的lib目录中）：
- `servlet-api.jar`
- `jsp-api.jar`

**注意**：部署到Tomcat时不需要这两个jar包，因为Tomcat已包含。

### 4. 配置IDE（Eclipse示例）

#### 4.1 创建Dynamic Web Project

1. File → New → Dynamic Web Project
2. Project name: `software-group`（或任意名称）
3. Target runtime: 选择你的Tomcat版本
4. Dynamic web module version: 4.0（或3.1）
5. 勾选 "Generate web.xml deployment descriptor"

#### 4.2 导入源代码

1. 将 `src/` 目录下的所有文件复制到项目的 `src/` 目录
2. 将 `WebContent/` 目录下的所有文件复制到项目的 `WebContent/` 目录
3. 将 `lib/` 目录下的jar包添加到项目的构建路径：
   - 右键项目 → Properties → Java Build Path → Libraries
   - Add External JARs → 选择 `lib/mysql-connector-java-8.0.x.jar`

#### 4.3 配置Tomcat服务器

1. Window → Preferences → Server → Runtime Environments
2. Add → 选择你的Tomcat版本 → Next
3. 选择Tomcat安装目录 → Finish
4. 右键项目 → Run As → Run on Server
5. 选择配置好的Tomcat服务器

### 5. 手动部署到Tomcat

#### 5.1 编译项目

使用IDE导出WAR文件，或手动编译：

```bash
# 编译Java文件（需要设置classpath包含servlet-api和jsp-api）
javac -cp "lib/*:tomcat/lib/*" -d WebContent/WEB-INF/classes src/**/*.java
```

#### 5.2 部署WAR文件

1. 将整个 `WebContent` 目录打包成WAR文件：
   ```bash
   cd WebContent
   jar -cvf software-group.war *
   ```

2. 将WAR文件复制到Tomcat的 `webapps/` 目录

3. 启动Tomcat：
   ```bash
   catalina.sh start  # Linux/Mac
   catalina.bat start  # Windows
   ```

#### 5.3 直接部署目录

也可以直接将 `WebContent` 目录复制到Tomcat的 `webapps/` 目录，并重命名为项目名称（如 `software-group`）。

### 6. 设置文件存储目录权限

确保Tomcat有权限在以下目录创建文件和文件夹：

- `WebContent/localstorage/news/`
- `WebContent/localstorage/files/`

Linux/Mac系统可能需要设置权限：

```bash
chmod -R 777 WebContent/localstorage
```

### 7. 访问系统

1. 启动Tomcat服务器
2. 在浏览器中访问：`http://localhost:8080/项目名称/`
3. 使用默认管理员账号登录：
   - 用户名：`admin`
   - 密码：`admin123`

### 8. 常见问题

#### 问题1：数据库连接失败

**错误信息**：`java.sql.SQLException: Access denied for user...`

**解决方法**：
- 检查数据库用户名和密码是否正确
- 确认数据库服务是否启动
- 检查数据库用户是否有足够的权限

#### 问题2：找不到Servlet类

**错误信息**：`java.lang.ClassNotFoundException`

**解决方法**：
- 确认Java类已正确编译到 `WEB-INF/classes/` 目录
- 检查web.xml中的servlet类名是否正确
- 确认项目的编译路径配置正确

#### 问题3：文件上传失败

**错误信息**：`IOException` 或权限错误

**解决方法**：
- 检查 `localstorage` 目录是否存在
- 确认Tomcat有该目录的写权限
- 检查文件大小是否超过10MB限制

#### 问题4：中文乱码

**解决方法**：
- 确认所有JSP文件的pageEncoding为UTF-8
- 确认数据库字符集为utf8mb4
- 确认数据库连接的URL中包含 `characterEncoding=UTF-8`

## 开发建议

1. **使用IDE开发**：推荐使用Eclipse或IntelliJ IDEA，可以自动管理编译和部署
2. **日志调试**：可以在代码中添加 `System.out.println()` 或使用日志框架（如log4j）进行调试
3. **数据库工具**：推荐使用Navicat、DBeaver等工具管理数据库
4. **版本控制**：建议使用Git进行版本管理，忽略 `localstorage/files/` 目录

## 下一步

- 阅读 `README.md` 了解系统功能
- 查看 `高校软件小组管理系统.md` 了解详细需求
- 根据实际需求修改和扩展功能

