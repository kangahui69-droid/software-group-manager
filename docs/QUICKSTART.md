# 🚀 快速开始指南

## 首次使用步骤

### 1. 克隆项目（如果是新成员）

```bash
git clone <你的仓库地址>
cd Software_group
```

### 2. 配置本地数据库

**步骤1：复制配置模板**

```bash
# Windows
copy config\config.properties.template config\config.local.properties

# Mac/Linux
cp config/config.properties.template config/config.local.properties
```

**步骤2：编辑本地配置**

用文本编辑器打开 `config/config.local.properties`，修改以下配置：

```properties
# 修改数据库连接URL（如果需要）
db.url=jdbc:mysql://localhost:3306/software_group?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true

# 修改为你的数据库用户名
db.username=root

# 修改为你的数据库密码
db.password=你的实际密码

# 其他配置根据需要进行修改...
```

**步骤3：验证配置**

运行配置验证程序：

```bash
# 编译配置管理类（如果之前没有编译）
javac -encoding UTF-8 -cp "lib/*" -d out src/config/SimpleConfig.java

# 运行验证程序
java -cp "lib/*;out" config.SimpleConfig
```

如果看到如下输出，说明配置成功：
```
[SimpleConfig] 已加载本地配置: config/config.local.properties
[SimpleConfig] 配置加载成功
```

### 3. 编译项目

```bash
# 编译所有Java文件
javac -encoding UTF-8 -cp "lib/*" -d out src/**/*.java
```

### 4. 启动Tomcat

```bash
# 使用启动脚本
scripts/start_tomcat.bat
```

或者手动启动：
```bash
# 进入Tomcat bin目录
cd F:\Tomcat\apache-tomcat-9.0.115-windows-x64\apache-tomcat-9.0.115\bin

# 启动Tomcat
startup.bat
```

### 5. 访问系统

打开浏览器访问：http://localhost:8080/Software_group/login.jsp

默认账号：
- 管理员：admin / admin123
- 成员：member1 / member123

## 团队协作规范

### 提交代码前检查清单

- [ ] 我没有提交包含真实密码的配置文件
- [ ] 我检查了 `.gitignore` 确保配置文件被忽略
- [ ] 我只提交了模板文件 `config.properties.template`
- [ ] 我测试了代码可以正常运行

### 配置更新流程

**如果需要添加新的配置项**：

1. **更新模板文件**
   ```bash
   # 编辑 config/config.properties.template
   # 添加新的配置项，带有注释说明
   ```

2. **提交模板文件到Git**
   ```bash
   git add config/config.properties.template
   git commit -m "添加新的配置项：xxx"
   git push
   ```

3. **通知团队成员**
   - 在群里或邮件通知所有开发者
   - 告知需要更新本地配置文件
   - 说明新配置项的作用和如何设置

4. **团队成员更新本地配置**
   ```bash
   # 拉取最新代码
   git pull
   
   # 编辑本地配置文件，添加新配置项
   # config/config.local.properties
   ```

## 常见问题

### Q1: 我拉取了最新代码，但启动时提示数据库连接失败？

**可能原因**：
1. 新添加了数据库配置项，但您的本地配置文件没有更新
2. 数据库密码已更改

**解决方法**：
```bash
# 1. 查看模板文件是否有新的配置项
cat config/config.properties.template

# 2. 对比您的本地配置文件
cat config/config.local.properties

# 3. 添加缺失的配置项
```

### Q2: 我不小心提交了包含密码的配置文件怎么办？

**紧急处理**：
```bash
# 1. 从Git历史中移除该文件（会修改历史，谨慎使用）
git filter-branch --force --index-filter 'git rm --cached --ignore-unmatch config/config.local.properties' HEAD

# 2. 强制推送到远程（会覆盖远程历史，确保团队成员知情）
git push origin --force --all

# 3. 立即修改数据库密码
# 登录MySQL，修改root密码
```

**预防措施**：
- 立即更新 `.gitignore` 确保配置文件被忽略
- 定期检查 `git status` 确保没有敏感文件被标记为待提交

### Q3: 如何在不同环境（开发/测试/生产）使用不同配置？

**方法1：使用不同的配置文件**
```bash
# 开发环境
copy config\config.properties.template config\config.local.properties

# 测试环境
copy config\config.properties.template config\config.test.properties

# 生产环境
copy config\config.properties.template config\config.prod.properties
```

**方法2：使用环境变量**
```bash
# Windows
set ENV=prod

# Mac/Linux
export ENV=prod
```

然后在代码中读取环境变量：
```java
String env = System.getenv("ENV"); // local, test, 或 prod
```

## 联系支持

如果在配置过程中遇到问题：

1. **查看详细文档**：`docs/CONFIG_GUIDE.md`
2. **检查日志输出**：查看控制台错误信息
3. **联系项目负责人**：提供具体的错误信息和操作步骤

---

**🎉 现在您已经准备好开始团队协作开发了！**