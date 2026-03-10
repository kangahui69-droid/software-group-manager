# 多环境配置管理方案

## 问题背景

在团队协作开发中，每个开发者都有自己的本地数据库配置（用户名、密码、数据库名可能都不同）。如果直接把包含个人密码的配置文件提交到Git仓库，会导致：

1. **密码泄露风险**：数据库密码暴露在代码仓库中
2. **配置冲突**：不同开发者的配置互相覆盖
3. **协作困难**：新人拉取代码后无法直接运行，需要手动修改配置

## 解决方案

本项目采用 **"模板文件 + 本地配置文件 + 环境变量"** 的方案，完美解决上述问题。

## 使用方法

### 1. 配置文件目录结构

```
Software_group/
├── config/                          # 配置文件夹
│   ├── config.properties.template   # 配置模板（已提交到Git）
│   ├── config.local.properties      # 本地开发配置（不提交到Git）
│   ├── config.test.properties       # 测试环境配置（不提交到Git）
│   └── config.prod.properties       # 生产环境配置（不提交到Git）
├── .gitignore                       # Git忽略规则（已配置忽略本地配置文件）
└── src/
    └── config/
        └── ConfigManager.java       # 配置管理类
```

### 2. 首次使用配置步骤

#### 步骤1：复制模板文件

将模板文件复制为本地配置文件：

```bash
# Windows命令行
copy config\config.properties.template config\config.local.properties

# 或者手动复制文件后重命名为 config.local.properties
```

#### 步骤2：修改本地配置

编辑 `config.local.properties` 文件，修改以下配置项：

```properties
# ============================================
# 数据库配置 - 根据你的本地数据库修改
# ============================================

# 数据库URL（如果数据库名不同，请修改）
db.url=jdbc:mysql://localhost:3306/software_group?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true

# 数据库用户名（修改为你的用户名）
db.username=root

# 数据库密码（修改为你的密码）
db.password=你的实际密码

# ============================================
# 其他配置（可选）
# ============================================

# 文件上传路径（如果使用不同路径，请修改）
file.storage.path=WebContent/localstorage/files
image.storage.path=WebContent/localstorage/images

# 调试模式（开发环境建议开启）
system.debug=true
```

#### 步骤3：验证配置

运行配置测试程序：

```bash
cd F:/Software_group
java -cp "lib/*;out" config.ConfigManagerTest
```

### 3. 团队协作指南

#### 对于项目维护者（已有配置的成员）

1. **不要提交本地配置文件**
   ```bash
   # 确保 config.local.properties 不被提交
   git status
   # 应该显示 config.local.properties 为未跟踪文件
   ```

2. **提交模板更新**
   ```bash
   # 如果修改了模板，提交模板文件
   git add config/config.properties.template
   git commit -m "更新配置模板，添加新配置项"
   ```

3. **在README中说明配置步骤**
   - 确保新成员知道如何配置

#### 对于新加入的开发者

1. **克隆仓库**
   ```bash
   git clone <仓库地址>
   cd Software_group
   ```

2. **复制配置文件**
   ```bash
   # Windows
   copy config\config.properties.template config\config.local.properties
   
   # Mac/Linux
   cp config/config.properties.template config/config.local.properties
   ```

3. **修改配置**
   - 编辑 `config.local.properties`
   - 修改数据库用户名、密码
   - 根据需要调整其他配置

4. **验证配置**
   ```bash
   # 编译项目
   javac -encoding UTF-8 -cp "lib/*" -d out src/util/DBUtil.java
   
   # 运行测试
   java -cp "lib/*;out" util.DBUtil
   ```

5. **开始开发**
   - 现在可以正常启动Tomcat进行开发了

### 4. 不同环境的切换

#### 使用环境变量（推荐）

```bash
# Windows PowerShell
$env:ENV="test"

# Windows CMD
set ENV=test

# Mac/Linux
export ENV=test
```

然后在代码中：
```java
String env = System.getenv("ENV"); // 自动读取环境变量
```

#### 使用JVM参数

```bash
java -Denv=prod -jar application.jar
```

### 5. 安全最佳实践

#### ⚠️ 绝对不能做的事情

❌ **不要将包含真实密码的文件提交到Git**
```bash
# 错误示例
 git add config.local.properties  # 不要这样做！
```

❌ **不要在代码中硬编码密码**
```java
// 错误示例
String password = "my_real_password_123";  // 不要这样做！
```

❌ **不要将配置文件发给其他人**
- 不要通过QQ、微信发送 `config.local.properties`
- 如果必须分享，先删除密码等敏感信息

#### ✅ 推荐的安全做法

✅ **使用 .gitignore 忽略本地配置文件**
```gitignore
# 忽略所有本地配置文件
config/config.local.properties
config/config.*.properties
!config/config.properties.template
```

✅ **使用环境变量存储敏感信息**
```bash
# Windows
set DB_PASSWORD=your_password

# 在Java中读取
String password = System.getenv("DB_PASSWORD");
```

✅ **定期更换密码**
- 建议每3个月更换一次数据库密码
- 定期更新 DES 加密密钥（Config.DES_KEY）

✅ **使用密码管理工具**
- 推荐使用 KeePass、1Password、Bitwarden 等工具管理密码
- 不要明文存储密码在文本文件中

### 6. 常见问题排查

#### Q1: 配置修改后不生效？

**可能原因**：
1. Tomcat 缓存了旧的 class 文件
2. 配置文件没有被正确加载

**解决方法**：
```bash
# 1. 清理并重新编译
rm -rf out/*
javac -encoding UTF-8 -cp "lib/*" -d out src/util/DBUtil.java

# 2. 重启 Tomcat
```

#### Q2: 新成员拉取代码后无法连接数据库？

**检查清单**：
1. 是否复制了 `config.properties.template` 到 `config.local.properties`？
2. 是否修改了数据库用户名和密码？
3. 数据库服务是否已启动？
4. 数据库名是否正确？

#### Q3: 如何添加新的配置项？

**步骤**：
1. 在 `config.properties.template` 中添加新的配置项（带注释说明）
2. 提交模板文件到Git
3. 通知团队成员在各自的 `config.local.properties` 中添加新配置

#### Q4: 生产环境如何部署？

**推荐流程**：
1. 在服务器上创建 `config.prod.properties`
2. 配置生产数据库连接
3. 设置 `system.debug=false`
4. 使用环境变量或JVM参数指定环境：`java -Denv=prod ...`
5. 确保配置文件权限正确（只有应用用户可读取）

---

## 📋 总结

### 核心要点

1. **永远不要提交真实密码到Git**
   - 使用 `config.properties.template` 作为模板
   - 本地配置文件添加到 `.gitignore`

2. **灵活的环境切换**
   - 支持 local/test/prod 多环境
   - 通过环境变量或JVM参数切换

3. **团队协作友好**
   - 新成员只需复制模板并修改
   - 清晰的配置说明和文档

4. **安全可靠**
   - 密码不硬编码在代码中
   - 支持从环境变量读取敏感信息

### 快速开始清单

- [ ] 1. 复制 `config.properties.template` → `config.local.properties`
- [ ] 2. 修改数据库用户名和密码
- [ ] 3. 验证配置：运行 `DBUtilTest`
- [ ] 4. 将 `config.*.properties` 添加到 `.gitignore`
- [ ] 5. 开始开发！

---

**🎉 恭喜！您现在拥有了一个适合团队协作的配置管理系统！**