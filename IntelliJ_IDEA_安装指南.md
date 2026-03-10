# IntelliJ IDEA Community 安装指南

## 第一步：下载安装包

1. 打开浏览器访问：https://www.jetbrains.com/idea/download/?section=windows
2. 在页面上找到 **"IntelliJ IDEA Community Edition"**（社区版 - 免费）
3. 点击 **"Download"** 按钮
4. 等待下载完成（文件约500MB左右，文件名类似：ideaIC-2024.3.1.1.exe）

## 第二步：运行安装程序

1. 找到下载的文件（通常在 下载 文件夹）
2. 双击运行 `ideaIC-xxxxx.exe`
3. 如果弹出"用户账户控制"窗口，点击 **"是"**

## 第三步：安装向导

### 第1页 - 欢迎
- 点击 **"Next >"**

### 第2页 - 安装位置
- 保持默认路径（如：`C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3`）
- 或者点击 **"Browse..."** 选择其他位置
- 点击 **"Next >"**

### 第3页 - 安装选项
勾选以下选项（推荐）：
- ✅ **64-bit launcher**（64位启动器）
- ✅ **Add "Open Folder as Project"**（添加到右键菜单）
- ✅ **Add to PATH**（添加到环境变量）
- ✅ **.java** - 关联Java文件（可选）

点击 **"Next >"**

### 第4页 - 开始菜单文件夹
- 保持默认
- 点击 **"Install"**（开始安装）

### 第5页 - 安装进度
- 等待安装完成（约2-5分钟）

### 第6页 - 安装完成
- ✅ 勾选 **"Run IntelliJ IDEA"**（立即运行）
- 点击 **"Finish"**

## 第四步：首次启动配置

1. **导入设置**：选择 **"Do not import settings"**（不导入设置）→ 点击 **"OK"**

2. **用户协议**：
   - 勾选 **"I confirm that I have read and accept the terms of this User Agreement"**
   - 点击 **"Continue"**

3. **数据共享**：
   - 选择 **"Don't send"**（不发送统计数据）→ 点击 **"Continue"**

4. **主题选择**：
   - 选择 **"Light"**（明亮主题）或 **"Darcula"**（暗色主题）
   - 点击 **"Next: Default plugins"**

5. **默认插件**：
   - 保持默认勾选 → 点击 **"Next: Featured plugins"**

6. **特色插件**：
   - 可以跳过 → 点击 **"Start using IntelliJ IDEA"**

## 第五步：打开项目

1. 在欢迎界面，点击 **"Open"**（打开项目）
2. 导航到项目目录：`F:\Software_group`
3. 点击 **"OK"**
4. 等待IDEA加载项目并解析Maven依赖（首次加载可能需要几分钟）

## 第六步：配置Tomcat服务器

1. 点击右上角 **"Add Configuration..."**
2. 点击 **"+"** 按钮
3. 选择 **"Tomcat Server"** → **"Local"**
4. 在 **"Application server"** 旁点击 **"Configure..."**
5. 点击 **"+"** 添加Tomcat安装路径
6. 选择你的Tomcat目录（如：`F:\Tomcat\apache-tomcat-9.0.115`）
7. 点击 **"OK"**
8. 在 **"Deployment"** 标签页，点击 **"+"** → **"Artifact"**
9. 选择 **"software-group:war exploded"**
10. 修改 **Application context** 为 `/software-group`
11. 点击 **"OK"**

## 第七步：启动项目

1. 点击右上角的 **"Run"** 按钮（绿色三角形）或按 **Shift + F10**
2. 等待Tomcat启动
3. 打开浏览器访问：`http://localhost:8080/software-group/`

## 默认账号

- **管理员**：admin / admin123
- **成员**：member1 / member123

## 常见问题

### 1. Maven依赖下载慢
- 点击 **File** → **Settings** → **Build, Execution, Deployment** → **Build Tools** → **Maven**
- 修改 **User settings file** 为自定义的settings.xml（配置阿里云镜像）

### 2. 内存不足
- 点击 **Help** → **Edit Custom VM Options**
- 增加内存分配：
  ```
  -Xms1024m
  -Xmx2048m
  ```

### 3. 端口被占用
- 修改Tomcat端口号：在 **Run/Debug Configurations** 中，**HTTP port** 改为 8081 或其他

## 完成！

现在你已经成功安装并配置了IntelliJ IDEA，可以开始愉快地使用IDEA开发你的Java Web项目了！

如果在安装过程中遇到任何问题，随时告诉我！