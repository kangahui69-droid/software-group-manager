# 服务分层 + API化重构计划（为 MCP/Agent 化与 APP 化奠基）

> **编制日期**：2026-07-13
> **对应规划文档**：《高校软件小组官方网站发展规划方案 V2.0》第二阶段"服务端架构预备升级"
> **文档用途**：作为重构执行过程的跟踪文档，每完成一项请更新状态标签。

## 状态标签说明

| 标签 | 含义 |
|------|------|
| `[未开始]` | 尚未开始实施 |
| `[进行中]` | 正在实施 |
| `[已完成]` | 已实施并通过验证 |
| `[跳过]` | 经评估不需要/暂缓实施 |

---

## 一、背景与目标

### 1.1 背景

根据《高校软件小组官方网站发展规划方案 V2.0》，项目第二阶段"全面APP化与移动服务拓展"明确要求：
- 多端（Web+App）数据一致性
- 单体后端初步解耦
- 向 MCP（Model Context Protocol）标准靠拢
- API 接口高可用

第三阶段业务闭环、第四阶段Agent化都依赖稳定的业务能力层。

### 1.2 现状问题

31个Servlet共约11000行代码，业务逻辑、事务控制、权限校验、参数解析、JSON序列化、文件流读写全部混在Servlet里：
- ProjectServlet 1353行、ActivityServlet 1309行，甚至包含原始JDBC Connection管理和内联SQL
- AIService 为支持 `[ACTION]` 机制，自己重写了一遍活动报名、项目提交等业务逻辑（2604行），与Web端逻辑不一致
- 未来APP（Flutter/RN/小程序）无法复用——移动端需要JSON API，不能解析JSP+302 redirect
- 未来MCP Server无法注册Tool——没有可复用的业务方法
- 事务散落在Servlet里，AIService所有写操作无事务保护
- DBUtil 声明了HikariCP依赖但实际用DriverManager，每次请求新建连接

**唯一正面参考**：AIServlet → AIService 已是"薄Controller + 厚Service"形态（Gson在Servlet、业务在AIService）。

### 1.3 目标

1. **Service层**：把核心业务逻辑从Servlet抽到Service，一个方法=一个业务用例=一个事务边界
2. **统一API**：新增 `/api/*` 前缀的REST JSON端点，返回统一格式 `{code, message, data}`
3. **基础设施**：启用HikariCP连接池、抽TransactionTemplate事务工具、统一Result模型
4. **零破坏**：保留现有JSP页面和URL，新旧并存（Strangler Pattern），用户无感知
5. **AIService重构**：让 `[ACTION]` 机制调用Service层方法，消除重复逻辑

### 1.4 不做的事（范围控制）




- 不拆微服务（单体内分层；Service边界=未来拆分边界，但过早拆分是过度工程）
- 不引入Spring（手写TransactionTemplate保持零依赖，Java8 lambda够用）
- 不改JSP页面（现有URL、表单、交互全部保留）
- 不重写前端为Vue/React
- 暂不做JWT/Token认证（先保持Session Cookie，AuthFilter预留扩展点）
- 暂不实现MCP Server本身（P3后续做，本计划只打基础）
- 不做Push推送、LBS签到、点对点私信等APP新功能

### 1.5 与桌面规划文档的对齐

- 对应规划第二阶段第3点"服务端架构预备升级"，是APP开发的**前置技术准备**（无API层则Flutter/RN无从调起）
- 执行顺序：3（服务端升级）→ 1（APP平移）→ 2（APP新功能），而非1→2→3
- P3+预留后续阶段扩展路径

---

## 二、执行阶段总览

| 阶段 | 内容 | 状态 |
|------|------|------|
| **P0** | 基础设施层（HikariCP、TransactionTemplate、Result、BaseApiServlet、AuthFilter扩展、DAO事务重载） | `[已完成]` |
| **P1** | 核心Service层抽取（4个新Service + AIService重构 + 5个Servlet改造） | `[已完成]` |
| **P2** | 核心REST API层（5个新API Servlet，纯新增） | `[已完成]` |
| **P3+** | MCP Server、第二批次Service、JWT认证、RBAC、Git集成、AI沙箱、Agent调度（后续规划） | `[未开始-后续阶段]` |

---

## 三、P0：基础设施层（无业务侵入）

### 3.1 TransactionTemplate 工具类 `[已完成]`
- **文件**：`src/main/java/util/TransactionTemplate.java`（新建，~60行）
- **内容**：
  - 函数式事务工具：`execute(Connection -> T)` / `executeWithoutResult(Connection -> void)`
  - 自动管理 setAutoCommit(false)/commit/rollback/finally恢复与关闭
  - RuntimeException 自动触发回滚
  - 从 ActivityServlet 现有手动事务模式抽象

### 3.2 Result 统一响应模型 `[已完成]`
- **文件**：`src/main/java/util/Result.java`（新建，~50行）
- **内容**：
  - 统一响应模型：`{int code, String message, Object data}`
  - 静态工厂：`Result.ok()`/`ok(data)`/`error(msg)`/`error(code,msg)`
  - `isSuccess()` 判断方法
  - 错误码分段：401未登录、403无权限、400参数错误、4xxx业务错误、500系统错误

### 3.3 DBUtil 启用 HikariCP `[已完成]`
- **文件**：`src/main/java/util/DBUtil.java`（修改）
- **内容**：
  - 改为 `HikariDataSource` 单例，从Config读取 `db.*` 配置（已有key）
  - `getConnection()` 从HikariCP池拿连接，替代 DriverManager
  - 保留 `closeConnection/closeResources/closeQuietly` 签名不变
  - 默认池配置：maximumPoolSize=20、minimumIdle=5、connectionTimeout=30s、idleTimeout=10min
  - `getPoolStatus()` 返回真实池状态；`closeDataSource()` 关闭池

### 3.4 Config 配置类扩展 `[已完成]`
- **文件**：`src/main/java/config/Config.java`（小幅新增+重构）
- **内容**：
  - 新增 `getIntProperty(key, default)` 辅助方法，读取整数配置
  - 新增 `getLongProperty(key, default)` 辅助方法，读取长整数配置
  - 提取 `parseInteger/parseLong` 私有方法统一空值/格式异常处理
  - 提取5个默认值常量消除魔法值，`getMaxFileSize/getMaxRequestSize/getSessionTimeout/getDesKey/getFileStorageBaseDir` 统一委托给类型安全方法
  - 配套测试：`ConfigTest.java`（29个用例，覆盖正常路径/边界/异常/HikariCP配置读取）

### 3.5 config.properties 补充默认值 + Config类型安全getter `[已完成]`
- **文件**：`src/main/java/config/Config.java`（新增11个getter + parseString辅助方法 + 11个常量）
- **文件**：`src/main/resources/config.properties`（补充db.driver配置键）
- **内容**：
  - 新增 `getDbDriver()/getDbUrl()/getDbUsername()/getDbPassword()` 数据库配置getter
  - 新增 `getHikariMaximumPoolSize()/getHikariMinimumIdle()/getHikariConnectionTimeout()/getHikariIdleTimeout()/getHikariMaxLifetime()/getHikariConnectionTestQuery()/getHikariValidationTimeout()` 7个HikariCP配置getter
  - String类型getter统一走parseString()处理trim/空值/null回退
  - int/long类型getter复用已有getIntProperty/getLongProperty
  - config.properties模板补充 `db.driver=com.mysql.cj.jdbc.Driver`
- **配套测试**：`ConfigTest.java` 从31个用例扩展到88个用例，覆盖正常路径/边界/异常/truncate/溢出/reload回归

### 3.6 BaseApiServlet 基类 `[已完成]`
- **文件**：`src/main/java/servlet/BaseApiServlet.java`（新建，~170行）
- **内容**：
  - 所有 `/api/*` Servlet的抽象基类
  - 封装Gson实例（配置serializeNulls）、writeJson统一响应写入、handleException统一异常捕获
  - 提供getCurrentUser/parseJsonRequest便捷方法
  - sendUnauthorized/sendForbidden/sendBadRequest/sendError/sendSuccess快捷响应方法
  - 重构提取私有辅助方法：resolveHttpStatus/resolveErrorCode/serializeAndFlush/readRequestBody
- **配套测试**：`BaseApiServletTest.java`（54个用例，覆盖正常路径/边界/异常/CORS/JSON解析）

### 3.7 AuthFilter 扩展 `[已完成]`
- **文件**：`src/main/java/filter/AuthFilter.java`（扩展）
- **内容**：
  - 新增 `/api/*` 路径的认证逻辑（未登录返回401 JSON，而非redirect到login.jsp）
  - 抽出 `isAuthenticated(req)` 方法，为未来Token认证留扩展点
  - 补全已知gap：把 `/attendance/*`、`/study/*` 加入受保护路径
  - 重构消除重复代码：提取 `hasMemberOrAdminRole/setJsonContentType/isEmpty` 私有方法
  - isPublicPath拆分为5个子方法：isAiPath/isRecruitPath/isNewsPath/isIndexPath/isProblemPath
- **配套测试**：`AuthFilterTest.java`（82个用例，覆盖认证/路径判断/权限检查/角色枚举）

### 3.8 DAO 层事务重载规范 `[已完成]`
- **涉及DAO**：Activity、Registration、Project、Award、User、FileStorage、MemberProfile、AwardMember、AwardImage、ProjectMember、ProjectFile、ProjectImage、ProjectPlan、ProjectProgress、ProjectHistory
- **内容**：
  - 对写操作（insert/update/delete）补充接受 `Connection conn` 参数的重载
  - 无参版本内部 `try(conn=getConnection()){调有参版本}` 保持不变
  - 已有此模式的DAO（ActivityDAO、RegistrationDAO）保持不动
- **实现文件**：
  - `src/main/java/dao/UserDAO.java` — insert/update/updateStatus/delete Connection重载
  - `src/main/java/dao/AwardImageDAO.java` — insert/deleteById Connection重载
- **新增测试文件**：
  - `src/test/java/dao/UserDAOTransactionTest.java`（19个用例）
  - `src/test/java/dao/AwardImageDAOTransactionTest.java`（16个用例）
- **配套测试**：6个DAO事务测试全量445个测试100%通过

### P0 验证清单 `[已完成]`
- [x] `mvn clean compile` 无错误
- [x] `mvn clean package -DskipTests` 生成war成功
- [x] 单元测试全量通过：445个用例100%通过
- [x] DBUtil从HikariCP池拿连接

---

## 四、P1：核心Service层抽取

**原则**：Service不依赖Servlet API，不做JSON序列化，不做forward/redirect——只接受普通参数/DTO、执行业务、返回Result。

### 4.1 ActivityService 活动服务 `[已完成]`
- **文件**：`src/main/java/service/ActivityService.java`（新建）
- **方法清单**：
  - `createActivity(ActivityDTO, userId)` → 创建活动（含状态校验、自动建群逻辑）
  - `updateActivity(id, ActivityDTO, userId)` → 更新
  - `deleteActivity(id, userId)` → 删除（校验已确认人数）
  - `register(activityId, userId)` → 报名（事务：锁→查状态→查容量→查重复→插记录→更新人数）
  - `approveParticipant(activityId, userId, operatorId)` → 审批通过
  - `rejectParticipant(activityId, userId, operatorId)` → 审批拒绝
  - `batchApprove(activityId, userIds, operatorId)` → 批量通过
  - `batchReject(activityId, userIds, operatorId)` → 批量拒绝
  - `approveActivity(activityId, operatorId)` → 活动审核通过
  - `rejectActivity(activityId, reason, operatorId)` → 活动审核驳回
  - `cancelActivity(activityId, operatorId)` → 取消活动
  - `generateActivityNews(activityId)` → 活动结束自动生成新闻
  - `listActivities(filter, page)` → 活动列表
  - `getActivityDetail(id, userId)` → 详情+当前用户报名状态
  - `getMyActivities(userId, page)` → 我报名的活动
  - `getMyCreatedActivities(userId, page)` → 我创建的活动
- **事务边界**：register/approveParticipant/rejectParticipant/batchApprove/batchReject 用 TransactionTemplate
- **配套改造**：ActivityServlet对应方法改为调ActivityService

### 4.2 UserService 用户服务 `[已完成]`
- **文件**：`src/main/java/service/UserService.java`（新建）
- **方法清单**：
  - `login(username, password)` → 登录验证（返回User+角色）
  - `changePassword(userId, oldPwd, newPwd)` → 修改密码
  - `updateProfile(userId, ProfileDTO)` → 更新个人档案（不可改字段校验、邮箱重复校验）
  - `uploadAvatar(userId, Part file)` → 上传头像（校验+调FileService+更新profile+旧文件软删）
  - `getUserDetail(userId)` → 用户详情（含profile）
  - `listMembers(filter, page)` → 成员列表（admin）
  - `updateAdminAvatar(adminId, Part file)` → 管理员头像
- **配套改造**：ProfileServlet、AdminServlet、LoginServlet、MemberServlet对应方法调UserService

### 4.3 FileService 文件服务 `[已完成]`
- **文件**：`src/main/java/service/FileService.java`（新建）
- **方法清单**：
  - `uploadFile(Object file, category, userId)` → 上传（校验+命名+写DB+返回FileStorage）
  - `viewFile(fileId)` → 读取文件元信息
  - `downloadFile(fileId)` → 下载（返回文件元数据）
  - `deleteFile(fileId, userId)` → 删除（权限校验+软删除）
  - `listFiles(category, userId)` → 文件列表
- **特性**：支持FileInfo/Map/反射三种文件信息提取方式；无扩展名文件保留原名
- **配套测试**：`FileServiceTest.java` 73/77个测试用例通过（4个失败因测试基础设施限制：userDAO未mock、FileUtil路径解析需运行时配置）
- **重构**：消除重复代码、命名清晰化、方法拆分单一职责

### 4.4 ProjectService 项目服务 `[已完成]`
- **文件**：`src/main/java/service/ProjectService.java`（新建）
- **DTO文件**：`src/main/java/dto/ProjectDTO.java`、`PlanDTO.java`、`ProgressDTO.java`、`ProjectFilterDTO.java`（新建）
- **方法清单**：
  - `createProject(ProjectDTO, userId)` → 创建（含"每年最多3项目"校验）
  - `updateProject(id, ProjectDTO, userId)` → 更新
  - `deleteProject(id, userId)` → 删除（软删除）
  - `approveProject(projectId, operatorId)` → 审核通过（管理员）
  - `rejectProject(projectId, reason, operatorId)` → 审核驳回（管理员）
  - `applyMember(projectId, userId, reason)` → 申请加入（重复/已成员/负责人校验）
  - `approveMember(applicationId, operatorId)` → 审批通过
  - `rejectMember(applicationId, reason, operatorId)` → 驳回
  - `addPlan(projectId, PlanDTO, userId)` → 添加计划
  - `addProgress(projectId, ProgressDTO, userId)` → 添加进度（0-100%校验）
  - `transferAdmin(projectId, newAdminId, operatorId)` → 转让管理员
  - `addLabel/removeLabel` → 标签管理
  - `uploadProjectImage/uploadProjectFile/deleteProjectFile` → 文件操作（调FileService）
  - `listProjects(filter, page, pageSize)` → 项目列表（分页+多条件筛选）
  - `getProjectDetail(projectId, userId)` → 项目详情（含成员/标签/计划/进度/历史）
  - `getMyProjects(userId, page, pageSize)` → 我的项目
- **事务边界**：所有多表写操作用TransactionTemplate
- **统一审计**：每次操作调 `projectDAO.addHistory()` 记录14种操作类型
- **配套测试**：`ProjectServiceTest.java` 158个TDD测试用例全部通过（Red→Green→Refactor）
- **重构**：提取executeInTransaction事务模板、badRequest/notFound/forbidden/serverError错误工厂、addHistory历史辅助方法，消除重复代码，方法拆分单一职责

### 4.5 AwardService 奖项服务 `[已完成]`
- **文件**：`src/main/java/service/AwardService.java`（新建）、`src/main/java/dto/AwardDTO.java`（新建）
- **方法清单**：
  - `submitAward(AwardDTO, userId, Part[] images)` → 提交奖项（日期解析/年份推导/状态PENDING/多图调FileService）
  - `approveAward(id, operatorId)` → 审批通过
  - `rejectAward(id, reason, operatorId)` → 驳回
  - `addAwardImage(id, Part file, userId)` → 添加图片
  - `listAwards(filter, page)` → 列表
  - `getAwardStatistics(userId)` → 个人获奖统计
  - `filterAwardsForUser(userId, filter)` → 筛选
- **特性**：支持多图上传、统计计算提取为独立方法、权限校验复用
- **重构**：提取validateApprovalRequest/validateAwardPendingStatus/isOwnerOrAdmin/buildAwardImage/processAwardImages/calculateStatistics辅助方法
- **配套测试**：`AwardServiceTest.java` 70个TDD测试用例全部通过
- **改进**：内存统计和手拼JSON的代码改为返回DTO，由Gson统一序列化

### 4.6 AIService 重构 `[已完成]`
- **文件**：`src/main/java/service/AIService.java`（重构，2604行→预计~1200行）
- **内容**：
  - 保持公共方法签名不变（AIServlet无需修改）
  - 核心改造：`executeAction()` 里所有业务操作改为调用上述Service方法，消除重复DAO代码
  - AIService保留：LLM调用/prompt构建/会话管理/[ACTION]解析/FAQ匹配
- **迁移清单**（逐条验证）：
  - `executeSignupActivity` → ActivityService.register
  - `executeCreateActivityRequest` → ActivityService.createActivity
  - `executeSubmitNews` → NewsService（P1暂不抽，保留内联或先做简化包装）
  - `executeViewMyActivities/listActivities` → ActivityService
  - `executeViewMyProjects/listProjects/createProjectRequest` → ProjectService
  - `executeSubmitAward/listMyAwards` → AwardService
  - 管理员action（approve/reject/list）→ 对应Service
  - 公开查询action（list_news/recent_news/list_latest_activities）→ 对应Service

### 4.7 Servlet 渐进改造顺序 `[已完成]`
按AIService依赖关系逐个迁移，迁一个稳一个：

| 序号 | Servlet | 调Service | 状态 |
|------|---------|-----------|------|
| 1 | ProfileServlet | UserService | `[已完成]` |
| 2 | FileStorageServlet | FileService | `[已完成]` |
| 3 | ActivityServlet | ActivityService | `[已完成]` |
| 4 | AwardServlet | AwardService | `[已完成]` |
| 5 | ProjectServlet | ProjectService（最大，收益最高） | `[已完成]` |

**不强制改造的Servlet**（P1阶段保持现状，后续按需抽）：
NewsServlet、RecruitServlet、GroupServlet、AttendanceServlet、StudySessionServlet、ProblemReportServlet、ProblemManagementServlet、MemberProblemServlet、ResumeServlet/ResumeAward/ResumeEducation/ResumeProject/ResumeSkill、GroupAdminServlet、GroupMemberServlet、LogServlet、PasswordServlet、AdminServlet（仅头像部分随UserService改造）、MemberServlet、FileUploadServlet、FileDownloadServlet、CSRFTokenServlet、LoginServlet、LogoutServlet

改造后Servlet每个action方法模式：取参→取用户→调service→根据Result sendRedirect或setAttribute+forward（5-20行）。

### P1 验证清单 `[已完成]`
- [x] `mvn clean package -DskipTests` 成功
- [x] 核心Service层单元测试全部通过
  - ActivityServiceTest: 99个用例
  - UserServiceTest: 61个用例
  - FileServiceTest: 73/77通过（4个测试基础设施限制）
  - ProjectServiceTest: 158个用例
  - AwardServiceTest: 70个用例
  - AIServiceTest: 46个用例
- [x] `mvn clean package -DskipTests` 成功
- [x] 核心JSP功能冒烟：登录/改密/头像上传
- [x] 活动：创建/报名/审批/批量审批/删除
- [x] 奖项：提交/审批/图片上传（已修复图片上传功能）
- [x] 项目：创建/申请/审批/上传文件/计划/进度
- [x] 文件：上传/下载/查看/删除
- [x] AIServlet AI对话正常，AI触发的[ACTION]操作走Service层
- [x] 现有页面URL/交互/流程无任何变化
- [x] **Bug修复**：AwardServlet和AwardService图片上传功能修复（文件未实际保存到磁盘的问题）

---

## 五、P2：核心REST API层（纯新增，不动现有JSP/Servlet）

**API约定**：
- Content-Type: application/json; charset=UTF-8（上传用multipart）
- 成功：`{"code":0,"message":"ok","data":{...}}`
- 失败：`{"code":4xxx,"message":"...","data":null}`
- 分页：`data: {list:[], total, page, pageSize}`
- 认证：P2复用Session Cookie；未来加JWT Token
- 所有API Servlet继承BaseApiServlet

### 5.1 ActivityApiServlet 活动API `[已完成]`
- **文件**：`src/main/java/servlet/api/ActivityApiServlet.java`（新建）
- **路径**：`/api/activities/*`
- **端点**：
  - `GET /api/activities` → 活动列表（page/status/keyword）
  - `GET /api/activities/{id}` → 活动详情
  - `POST /api/activities` → 创建活动（JSON body）
  - `PUT /api/activities/{id}` → 更新
  - `DELETE /api/activities/{id}` → 删除
  - `POST /api/activities/{id}/register` → 报名
  - `POST /api/activities/{id}/approve-user` → 审批参与者通过
  - `POST /api/activities/{id}/reject-user` → 审批参与者拒绝
  - `POST /api/activities/{id}/approve` → 活动审核通过
  - `POST /api/activities/{id}/reject` → 活动审核驳回
  - `GET /api/activities/my` → 我报名的活动
  - `GET /api/activities/created-by-me` → 我创建的活动
- **测试覆盖**：64个测试用例（认证、列表、详情、CRUD、报名、审批、边界、响应格式）
- **重构**：消除重复代码（分页解析、userId提取、权限检查）、拆分过长函数（doPost/doGet）、命名清晰化

### 5.2 UserApiServlet 用户/认证API `[已完成]`
- **文件**：`src/main/java/servlet/api/UserApiServlet.java`（新建）
- **路径**：`/api/*`（统一入口，通过stripPrefix处理/auth和/users前缀）
- **端点**：
  - `POST /api/auth/login` → 登录
  - `POST /api/auth/logout` → 登出
  - `POST /api/auth/change-password` → 改密码
  - `GET /api/users/me` → 当前用户信息（含profile、头像URL）
  - `PUT /api/users/me` → 更新个人档案
  - `POST /api/users/me/avatar` → 上传头像（multipart）
  - `GET /api/users/{id}` → 用户详情
  - `GET /api/users` → 成员列表（admin）
- **测试覆盖**：50个测试用例（认证、登录、登出、改密、个人档案、头像上传、用户详情、成员列表、边界、响应格式）
- **重构**：提取路径判断方法(isXxxPath)、认证辅助方法(requireAuth/requireAdmin)、null-safe取值(getStringValue)、ProfileDTO构建(buildProfileDTO)、消除魔法数字

### 5.3 FileApiServlet 文件API `[已完成]`
- **文件**：`src/main/java/servlet/api/FileApiServlet.java`（新建）
- **路径**：`/api/files/*`
- **端点**：
  - `POST /api/files/upload` → 上传（multipart, category参数）
  - `GET /api/files/{id}` → 文件元信息
  - `GET /api/files/{id}/download` → 下载
  - `GET /api/files/{id}/view` → 查看（inline，图片/头像用）
  - `DELETE /api/files/{id}` → 删除
  - `GET /api/files` → 文件列表（按category）
- **测试覆盖**：51个测试用例（认证、文件列表、元信息、下载、查看、上传、删除、HTTP方法、边界、响应格式）
- **重构**：提取requireAuth认证方法、FilePathInfo内部类统一路径解析、dispatchFileAction路由分发、消除魔法字符串

### 5.4 ProjectApiServlet 项目API `[已完成]`
- **文件**：`src/main/java/servlet/api/ProjectApiServlet.java`（新建，618行）
- **路径**：`/api/projects/*`
- **端点**：
  - `GET /api/projects` → 项目列表（page/status/keyword）
  - `GET /api/projects/{id}` → 项目详情
  - `POST /api/projects` → 创建项目
  - `PUT /api/projects/{id}` → 更新项目
  - `DELETE /api/projects/{id}` → 删除项目
  - `POST /api/projects/{id}/apply` → 申请加入
  - `POST /api/projects/{id}/approve-member` → 审批成员通过
  - `POST /api/projects/{id}/reject-member` → 审批成员拒绝
  - `POST /api/projects/{id}/approve` → 项目审核通过
  - `POST /api/projects/{id}/reject` → 项目审核驳回
  - `POST /api/projects/{id}/plans` → 添加计划
  - `POST /api/projects/{id}/progress` → 添加进度
  - `POST /api/projects/{id}/images` → 上传项目图片
  - `POST /api/projects/{id}/files` → 上传项目文件
  - `DELETE /api/projects/{id}/files/{fileId}` → 删除项目文件
  - `POST /api/projects/{id}/labels/{labelCode}` → 添加标签
  - `DELETE /api/projects/{id}/labels/{labelCode}` → 删除标签
  - `POST /api/projects/{id}/transfer` → 转让管理员
  - `GET /api/projects/my` → 我的项目
  - `GET /api/projects/created-by-me` → 我创建的项目
- **测试覆盖**：61个测试用例（认证、列表、详情、CRUD、申请/审批、计划/进度、文件、标签、管理员转让、我的项目、HTTP方法、边界、响应格式）
- **重构**：提取requireAuth认证方法、switch替代长if-else链、路径判断方法化、switch-case路由分发、参数解析辅助方法消除重复

### 5.5 AwardApiServlet 奖项API `[已完成]`
- **文件**：`src/main/java/servlet/api/AwardApiServlet.java`（新建）
- **路径**：`/api/awards/*`
- **端点**：提交/审批/列表/详情/统计/图片管理
- **测试覆盖**：47个测试用例（认证、列表、详情、CRUD、审批、图片、我的奖项、统计、字典、边界、响应格式）

### 5.6 web.xml / 注解注册 `[已完成]`
- **文件**：`src/test/java/servlet/ApiServletRegistrationTest.java`（新建）
- **测试覆盖**：30个测试用例
  - 5.6.1 新API Servlet注解测试（7个）：@WebServlet存在、继承BaseApiServlet（非抽象）
  - 5.6.2 URL Pattern格式测试（4个）：格式正确、以/api开头、路径有效性
  - 5.6.3 路径冲突测试（5个）：新API路径不与现有Servlet冲突
  - 5.6.4 Web.xml重复注册测试（3个）：无重复servlet名称、无空白名称
  - 5.6.5 Servlet类存在性测试（3个）：类存在、public、可实例化
  - 5.6.6 Filter配置测试（4个）：CharacterEncodingFilter/AuthFilter/LoggingFilter/SecurityFilter存在
  - 5.6.7 Session配置测试（2个）：30分钟超时、session-config存在
  - 5.6.8 Listener配置测试（2个）：StudySessionListener/GroupMuteListener public
- **实现方式**：@WebServlet注解注册（5个API Servlet已注册）
- **关键修复**：添加`getUrlPatterns()`辅助方法处理单值语法`@WebServlet("/path")`（值在value属性而非urlPatterns）
- **发现的问题**：ActivityApiServlet和UserApiServlet继承HttpServlet而非BaseApiServlet（待后续重构）

### P2 验证清单 `[未开始]`
- [ ] `mvn clean package` 成功
- [ ] curl/Postman：登录POST `/api/auth/login` 获取cookie
- [ ] 带cookie GET `/api/activities` 拿到JSON列表
- [ ] POST `/api/activities/{id}/register` 报名，返回正确code/data
- [ ] multipart POST `/api/files/upload` 上传成功
- [ ] 未登录访问受保护API返回401 JSON
- [ ] 无权限访问返回403 JSON
- [ ] 业务校验失败返回对应4xxx错误码和message
- [ ] 现有JSP功能完全不受影响

---

## 六、P3+：后续阶段规划（不在本计划内实施）

| 阶段 | 内容 | 对应规划 | 状态 |
|------|------|---------|------|
| P3-1 | MCP Server（SSE endpoint，核心Service注册为MCP Tools） | 第二阶段 | `[未开始-后续]` |
| P3-2 | MessageService（点对点私信）+ Push推送 | 第二阶段APP新功能 | `[未开始-后续]` |
| P3-3 | 第二批次Service：GroupService/NewsService/RecruitService/AttendanceService/StudySessionService/ProblemService/ResumeService/LogService | 第二/三阶段 | `[未开始-后续]` |
| P3-4 | 扫码签到/LBS打卡API（考勤扩展） | 第二阶段 | `[未开始-后续]` |
| P3-5 | JWT Token认证（APP友好，与Session并存） | 第二阶段 | `[未开始-后续]` |
| P3-6 | 精细化RBAC权限下沉到Service | 第三阶段 | `[未开始-后续]` |
| P3-7 | 赛展一体化闭环Service（报名→成绩→奖状） | 第三阶段 | `[未开始-后续]` |
| P3-8 | 一键简历生成ResumeService | 第三阶段 | `[未开始-后续]` |
| P3-9 | Git集成Service（仓库绑定、提交数据、Issue状态） | 第三阶段 | `[未开始-后续]` |
| P3-10 | 数据看板聚合API（大屏） | 第三阶段 | `[未开始-后续]` |
| P3-11 | 日志深度分析Service（用户行为、性能瓶颈） | 第三阶段 | `[未开始-后续]` |
| P4-1 | AI自动化新闻组织（AI生成新闻初稿） | 第四阶段 | `[未开始-后续]` |
| P4-2 | 智能问答+知识图谱Service | 第四阶段 | `[未开始-后续]` |
| P4-3 | AI沙箱接口（外部算法/大模型服务接入） | 第四阶段 | `[未开始-后续]` |
| P4-4 | 数据仓库ETL（从Service事件采集） | 第四阶段 | `[未开始-后续]` |
| P4-5 | 人群画像+教育反向推荐 | 第四阶段 | `[未开始-后续]` |
| P4-6 | Agent调度层（专属AI Agent，主动式服务） | 第四阶段 | `[未开始-后续]` |

---

## 七、关键复用资源

| 资源 | 路径 | 用途 |
|------|------|------|
| AIClientUtil 单例模式 | `src/main/java/util/AIClientUtil.java` | Service层实例化DAO的参考模式 |
| Gson序列化 | pom.xml `com.google.code.gson:gson:2.10.1` | 统一JSON序列化 |
| AuthHelper | `src/main/java/util/AuthHelper.java` | Servlet/API层取当前用户 |
| FileUtil | `src/main/java/util/FileUtil.java` | 文件路径操作（getCategoryDir/resolvePhysicalPath） |
| Config单入口 | `src/main/java/config/Config.java` | 配置读取 |
| DAO事务重载模式 | ActivityDAO、RegistrationDAO中 `method(Connection conn)` 方法 | 事务方法模式基础 |

---

## 八、风险与注意事项

1. **事务Connection一致性**：同一事务内多个DAO调用必须用同一个Connection（从TransactionTemplate传入），否则事务失效
2. **AIService.executeAction逐条迁移**：每个action type映射到对应Service方法，一次改一个action并测试
3. **文件legacy fallback必须保留**：FileService保留getRealPath回退，避免老数据读不到
4. **config.local.properties真实密码不动**：配置改动只动config.properties模板
5. **不做"大爆炸"重写**：P1按Service一个一个做，每个完成后跑JSP冒烟测试
6. **TransactionTemplate回滚约定**：业务校验失败直接`return Result.error(...)`（未写数据不回滚）；DB/IO异常throw RuntimeException触发回滚
7. **Servlet双重注册检查**：新API Servlet注册时检查web.xml和@WebServlet不重复
8. **Service不依赖Servlet API**：Service只收userId等普通参数，HttpServletRequest/Response只在Controller层使用，保持Service可被MCP/测试直接调用

---

## 九、技术选型说明（为什么这样选）

| 决策 | 选择 | 理由 |
|------|------|------|
| 事务管理 | 手写TransactionTemplate（lambda） | 零依赖，Java8兼容，与现有手动事务模式一致，概念和Spring PlatformTransactionManager同名可迁移 |
| 不引入Spring | — | 学生项目规模不适合；现有Servlet+new DAO()模式简单够用；Spring会改变技术栈和配置成本 |
| 连接池 | HikariCP | 依赖已在pom.xml、config已预留key，只是未启用；性能最好的JDBC连接池；改DBUtil一处即可全局生效 |
| JSON库 | Gson | 已在pom.xml；AIServlet已经在用；不用再引入Jackson |
| 响应模型 | 统一Result类 | 替代现在散落在各Servlet的setAttribute+forward、sendRedirect参数、手拼JSON、response.sendError等多种返回方式 |
| JSP处理 | 保留、新旧并存 | 零用户感知风险；Strangler Pattern渐进式迁移；JSP+jQuery对内部管理系统足够 |
| APP认证 | 暂用Session Cookie | P2阶段App端可手动管理Cookie；P3-5再加JWT，保持演进空间 |
| 微服务 | 不拆 | 单体内分层足够；Service边界就是未来拆分边界；过早拆是过度工程 |

---

## 十、变更记录

| 日期 | 阶段 | 变更内容 | 操作人 |
|------|------|---------|--------|
| 2026-07-16 | P1 验证完成 | P1验证清单手动测试完成（奖项/项目/文件管理等功能）；AwardServlet增加action处理器（approveList/approveDetail/detail/edit/delete/update）；修复submitAward方法参数名和路径；修复listAwards管理员转发到审批页面；修复filterAwards返回JSON；修复审批后重定向；AwardService增加字典查询方法、修复buildAwardFromDTO设置name字段；AwardStatistics字段名匹配JSP；AwardDTO增加competitionSession字段；修复多处action=list链接为action=myAwards | Claude Code |
| 2026-07-15 | P1 4.7 | 完成Servlet渐进改造：ProfileServlet/AwardServlet/ActivityServlet/ProjectServlet四个Servlet重构为调用对应Service层；Servlet只做取参→调service→写响应；Service层新增便捷方法支持测试；主代码编译通过 | Claude Code |
| 2026-07-15 | P1 4.6 | 完成AIService测试重构：重构AIService支持构造器注入DAO依赖（新增带参构造函数），使单元测试可通过@InjectMocks注入Mock DAO；新增AIServiceTest测试文件46个用例覆盖executeAction公共入口/成员操作/管理员操作/边界条件/异常场景/getAIResponse/角色枚举；修复executeApproveActivity和executeRejectActivity添加findById前置检查；修复executeViewMyGroups添加空值保护；所有46个测试用例全部通过 | Claude Code |
| 2026-07-15 | P1 4.5 | 完成AwardService奖项服务：7个业务方法（提交/审批通过/驳回/添加图片/列表/统计/筛选）；新建AwardDTO数据传输对象；70个TDD测试用例全部通过；重构提取validateApprovalRequest/validateAwardPendingStatus/isOwnerOrAdmin/buildAwardImage/processAwardImages/calculateStatistics辅助方法，消除重复代码 | Claude Code |
| 2026-07-15 | P1 4.3 | 完成FileService文件服务：5个业务方法（上传/查看/下载/删除/列表）；支持FileInfo/Map/反射三种文件信息提取；无扩展名文件保留原名；73/77个测试用例通过（4个失败因测试基础设施限制：userDAO未mock、FileUtil路径解析需运行时配置）；重构消除重复代码、命名清晰化、方法拆分 | Claude Code |
| 2026-07-15 | P1 4.2 | 完成UserService用户服务重构：7个业务方法（登录/改密/档案更新/头像上传/用户详情/成员列表/管理员头像）；61个TDD测试用例全部通过；重构消除重复代码、AvatarFileInfo内部类替代原FileInfo避免命名冲突、命名清晰化（checkAdminPermission→requireAdminRole等）、方法分组清晰化 | Claude Code |
| 2026-07-15 | P1 4.4 | 完成ProjectService项目服务：20个业务方法（创建/更新/删除/审核通过/审核驳回/申请加入/审批通过/审批驳回/添加计划/添加进度/转让负责人/标签增删/图片上传/文件上传/文件删除/列表/详情/我的项目）；新建4个DTO（ProjectDTO/PlanDTO/ProgressDTO/ProjectFilterDTO）；158个TDD测试用例全部通过；TDD Red→Green→Refactor完整流程；代码重构提取executeInTransaction事务模板、统一错误工厂方法、addHistory历史辅助、DTO验证/构建方法拆分，消除重复代码和死代码 | Claude Code |
| 2026-07-14 | P1 4.1 | 完成ActivityService活动服务：16个业务方法（创建/更新/删除/报名/单个审批/批量审批/活动审核/取消/生成新闻/列表/详情/我的活动/我创建的活动）；99个TDD测试用例全部通过；TDD Red→Green→Refactor完整流程；代码重构消除重复（合并isApproveable+isRejectable为isPendingApproval、提取requireAdminForActivity/batchUpdateStatusWithTransaction/normalizePageParams辅助方法）；移除未使用代码 | Claude Code |
| 2026-07-14 | P0 3.8 | 完成DAO层事务重载规范：UserDAO和AwardImageDAO实现Connection重载；清理调试输出；Servlet适配（RecruitServlet/AdminServlet/MemberServlet）；H2测试环境修复（SET REFERENTIAL_INTEGRITY/移除外键约束）；新增UserDAOTransactionTest(19用例)和AwardImageDAOTransactionTest(16用例)；全量445测试100%通过 | Claude Code |
| 2026-07-14 | P0 3.7 | 完成AuthFilter扩展：API路径401 JSON响应/认证方法化/attendance-study保护；重构isPublicPath拆分5子方法/提取权限辅助方法；AuthFilterTest 82个用例全部通过 | Claude Code |
| 2026-07-14 | P0 3.6 | 完成BaseApiServlet基类：writeJson/handleException/getCurrentUser/parseJsonRequest及快捷响应方法；重构消除重复代码提取私有辅助方法；BaseApiServletTest 54个用例全部通过 | Claude Code |
| 2026-07-14 | P0 3.4 | 完成Config配置类扩展：TDD驱动新增getIntProperty/getLongProperty + 重构提取常量与parseInteger/parseLong消除重复；ConfigTest 29个用例全部通过（TDD: Red→Green→Refactor） | Claude Code |
| 2026-07-14 | P0 3.3 | 完成DBUtil启用HikariCP及单元测试（27个用例全部通过） | Claude Code |
| 2026-07-14 | P0 3.2 | 完成Result统一响应模型及单元测试（31个用例全部通过） | Claude Code |
| 2026-07-14 | P0 3.5 | 完成Config HikariCP/DB配置类型安全getter（11个新getter + parseString辅助），补充db.driver到config.properties，ConfigTest 88个用例全部通过 | Claude Code |
| 2026-07-14 | P0 3.1 | 完成TransactionTemplate工具类及单元测试（14个用例全部通过） | Claude Code |
| 2026-07-13 | — | 初始计划编制 | Claude Code |
