# 服务分层与API化完整计划

> **编制日期**：2026-07-25
> **版本**：v2.0（整合P0-P5完整规划）
> **对应规划文档**：《服务分层与API化重构计划.md》P0-P2已完成，本文档为P3-P5完整规划

---

## 一、背景与原则

### 1.1 核心理念

> **先有Service层，后有API层。Service是业务逻辑的核心，API Servlet应该是"薄"的。**

```
┌─────────────────────────────────────────────────────────────────┐
│                    正确架构：薄Servlet + 厚Service                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   Servlet (API层)           Service层           DAO层          │
│   ┌─────────────────┐      ┌─────────────┐   ┌────────┐      │
│   │ GroupApiServlet  │ ───▶ │ GroupService│ ──▶ │DAO     │      │
│   │                  │      │             │   │        │      │
│   │ - 解析参数       │      │ - 业务逻辑  │   └────────┘      │
│   │ - 调用Service    │      │ - 事务管理  │                   │
│   │ - 返回JSON       │      │ - 权限校验  │                   │
│   │                  │      │             │                   │
│   │ 代码量: ~100行   │      │ 代码量: ~500行                  │
│   └─────────────────┘      └─────────────┘                    │
│                                                                 │
│   Servlet职责：参数解析 + 调用Service + 返回JSON                 │
│   Service职责：业务逻辑 + 事务控制 + 业务校验                     │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 开发顺序原则

| 情况 | 做法 |
|------|------|
| 已有Service | 直接API化 |
| 没有Service | **先抽取Service，再API化** |

### 1.3 阶段总览

| 阶段 | 内容 | 交付物 | 状态 |
|------|------|--------|------|
| P0 | 基础设施层 | HikariCP、TransactionTemplate、Result、BaseApiServlet | ✅ 已完成 |
| P1 | 核心Service层 | Activity/User/File/Project/Award/AIService | ✅ 已完成 |
| P2 | 核心REST API层 | 5个核心API Servlet + 303测试 | ✅ 已完成 |
| **P3** | **核心业务分层** | **Group/Attendance/Recruit/Resume Service** | ✅ Resume已完成 |
| P3.5 | 核心业务API化 | Group/Attendance/Recruit/Resume API | ⏳ 待开始 |
| P4 | 扩展业务分层 | News/Problem/Member Service | ⏳ 待开始 |
| P4.5 | 扩展业务API化 | News/Problem/Member API | ⏳ 待开始 |
| P5 | 收尾分层与优化 | Study/Log Service + 优化项 | ⏳ 待开始 |

---

## 二、现状分析

### 2.1 Service层现状

```
┌─────────────────────────────────────────────────────────────────┐
│                    Service 层现状                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   【已完成 P1】6个Service                                        │
│   ━━━━━━━━━━━━━━━━━━━━━━━━                                      │
│   ✅ ActivityService    → ActivityServlet, ActivityApiServlet     │
│   ✅ UserService        → UserServlet, UserApiServlet            │
│   ✅ FileService       → FileStorageServlet, FileApiServlet    │
│   ✅ ProjectService    → ProjectServlet, ProjectApiServlet       │
│   ✅ AwardService     → AwardServlet, AwardApiServlet          │
│   ✅ AIService         → AIServlet                             │
│                                                                 │
│   【待分层 P3-P5】9个新Service                                  │
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━                                  │
│   ❌ GroupService       → GroupServlet + 2个相关Servlet          │
│   ❌ AttendanceService  → AttendanceServlet                      │
│   ❌ RecruitService    → RecruitServlet                        │
│   ❌ ResumeService     → ResumeServlet + 5个子模块Servlet       │
│   ❌ NewsService       → NewsServlet                           │
│   ❌ ProblemService    → 3个Problem相关Servlet                │
│   ❌ MemberService    → MemberServlet + ProfileServlet         │
│   ❌ StudyService     → StudySessionServlet                    │
│   ❌ LogService      → LogServlet                             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 API层现状

```
┌─────────────────────────────────────────────────────────────────┐
│                    API 层现状                                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   【已完成 P2】5个API Servlet                                    │
│   ━━━━━━━━━━━━━━━━━━━━━━━━                                      │
│   ✅ ActivityApiServlet  → /api/activities/*                     │
│   ✅ UserApiServlet     → /api/*                                │
│   ✅ FileApiServlet     → /api/files/*                          │
│   ✅ ProjectApiServlet  → /api/projects/*                       │
│   ✅ AwardApiServlet    → /api/awards/*                         │
│                                                                 │
│   【待开发 P3.5-P5】8个新API Servlet                            │
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━                                  │
│   ❌ GroupApiServlet     → /api/groups/*                        │
│   ❌ AttendanceApiServlet → /api/attendance/*                   │
│   ❌ RecruitApiServlet   → /api/recruits/*                     │
│   ❌ ResumeApiServlet    → /api/resumes/*                       │
│   ❌ NewsApiServlet     → /api/news/*                          │
│   ❌ ProblemApiServlet   → /api/problems/*                      │
│   ❌ MemberApiServlet   → /api/members/*                       │
│   ❌ StudyApiServlet    → /api/study/*                         │
│   ❌ LogApiServlet     → /api/logs/*                          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 2.3 完整分层与API化映射

| 业务模块 | Service层 | API层 | 优先级 | 状态 |
|---------|----------|-------|--------|------|
| 活动 | ActivityService ✅ | ActivityApiServlet ✅ | - | 已完成 |
| 用户 | UserService ✅ | UserApiServlet ✅ | - | 已完成 |
| 文件 | FileService ✅ | FileApiServlet ✅ | - | 已完成 |
| 项目 | ProjectService ✅ | ProjectApiServlet ✅ | - | 已完成 |
| 奖项 | AwardService ✅ | AwardApiServlet ✅ | - | 已完成 |
| AI | AIService ✅ | (已有Servlet) | - | 已完成 |
| 群聊 | GroupService | GroupApiServlet | **P3** | 待分层→API化 |
| 考勤 | AttendanceService | AttendanceApiServlet | **P3** | 待分层→API化 |
| 招新 | RecruitService | RecruitApiServlet | **P3** | 待分层→API化 |
| 简历 | ResumeService ✅ | ResumeApiServlet | **P3** | 待API化 |
| 新闻 | NewsService | NewsApiServlet | P4 | 待分层→API化 |
| 问题 | ProblemService | ProblemApiServlet | P4 | 待分层→API化 |
| 成员 | MemberService | MemberApiServlet | P4 | 待分层→API化 |
| 学习 | StudyService | StudyApiServlet | P5 | 待分层→API化 |
| 日志 | LogService | LogApiServlet | P5 | 待分层→API化 |

---

## 三、开发流程

### 3.1 每个模块的开发顺序

```
┌─────────────────────────────────────────────────────────────────┐
│                 单个模块开发流程 (以Group为例)                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   Step 1: 分析现有Servlet                                         │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │ GroupServlet.java (约500行)                            │   │
│   │ ├── doGet() → listGroups, myGroups, showChat          │   │
│   │ ├── doPost() → create, send, addMembers               │   │
│   │ └── doDelete() → delete, removeMember                  │   │
│   └─────────────────────────────────────────────────────────┘   │
│                           │                                      │
│                           ▼                                      │
│   Step 2: 抽取Service (TDD)                                     │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │ GroupServiceTest.java (Red阶段 - 先写测试)              │   │
│   │ → 测试失败 (方法不存在)                                 │   │
│   │                                                         │   │
│   │ GroupService.java (Green阶段 - 最小实现)               │   │
│   │ → 测试通过                                              │   │
│   │                                                         │   │
│   │ GroupService.java (Refactor阶段 - 完善逻辑)             │   │
│   │ → 所有测试通过                                           │   │
│   └─────────────────────────────────────────────────────────┘   │
│                           │                                      │
│                           ▼                                      │
│   Step 3: 编写API Servlet (复用Service)                        │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │ GroupApiServlet.java                                     │   │
│   │ → 直接调用GroupService                                   │   │
│   │ → 只做参数解析和JSON响应                                 │   │
│   │ → 代码量约100-150行                                     │   │
│   └─────────────────────────────────────────────────────────┘   │
│                           │                                      │
│                           ▼                                      │
│   Step 4: 编写API测试                                          │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │ GroupApiServletTest.java                                │   │
│   │ → 60个测试用例                                          │   │
│   │ → 覆盖认证、CRUD、边界条件、异常场景                      │   │
│   └─────────────────────────────────────────────────────────┘   │
│                           │                                      │
│                           ▼                                      │
│   Step 5: 手动验证 + JSP冒烟测试                                │
│                           │                                      │
│                           ▼                                      │
│                        ✅ 完成                                    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 TDD开发规范

```
【Red阶段】先写测试
├── 编写会失败的测试（测试方法不存在）
├── 编译通过，测试失败
└── 明确要实现什么

【Green阶段】写最小代码
├── 用最小代码让测试通过
├── 不管代码丑不丑
└── 专注于功能正确

【Refactor阶段】重构优化
├── 消除重复代码
├── 优化命名和结构
├── 测试仍然通过
└── 保证不破坏功能
```

---

## 四、P3阶段：核心业务分层与API化

### 4.1 GroupService 群聊服务 `[已完成]`

**文件**：`src/main/java/service/GroupService.java`
**合并**：GroupServlet + GroupAdminServlet + GroupMemberServlet

**核心方法**：
| 方法 | 功能 | 复杂度 |
|------|------|--------|
| listGroups(filter, page) | 群聊列表 | 中 |
| getGroupDetail(id, userId) | 群聊详情 | 中 |
| createGroup(name, type, creatorId) | 创建群聊 | 高 |
| updateGroup(id, dto, userId) | 更新群聊 | 中 |
| deleteGroup(id, userId) | 删除群聊 | 中 |
| addMember(groupId, userId, operatorId) | 添加成员 | 中 |
| removeMember(groupId, userId, operatorId) | 移除成员 | 中 |
| getMessages(groupId, page) | 消息历史 | 中 |
| sendMessage(groupId, userId, content) | 发送消息 | 高 |
| muteMember(groupId, userId, until, reason) | 禁言 | 中 |
| unmuteMember(groupId, userId) | 取消禁言 | 低 |
| deleteMessage(msgId, operatorId) | 删除消息 | 低 |
| getMyGroups(userId, page) | 我的群聊 | 低 |
| getCreatedGroups(userId, page) | 我创建的 | 低 |

**涉及的DAO**：ActivityGroupDAO, GroupMemberDAO, GroupMessageDAO, UserGroupDAO, FileStorageDAO, UserDAO, MemberProfileDAO

**GroupApiServlet 端点** `[已完成]`：
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/groups` | 群聊列表 |
| GET | `/api/groups/{id}` | 群聊详情 |
| POST | `/api/groups` | 创建群聊 |
| PUT | `/api/groups/{id}` | 更新群聊 |
| DELETE | `/api/groups/{id}` | 删除群聊 |
| GET | `/api/groups/{id}/members` | 成员列表 |
| POST | `/api/groups/{id}/members` | 添加成员 |
| DELETE | `/api/groups/{id}/members/{userId}` | 移除成员 |
| GET | `/api/groups/{id}/messages` | 消息历史 |
| POST | `/api/groups/{id}/messages` | 发送消息 |
| POST | `/api/groups/{id}/mute` | 禁言 |
| POST | `/api/groups/{id}/unmute` | 取消禁言 |
| DELETE | `/api/groups/{id}/messages/{msgId}` | 删除消息 |
| GET | `/api/groups/my` | 我的群聊 |
| GET | `/api/groups/created-by-me` | 我创建的 |

---

### 4.2 AttendanceService 考勤服务 `[已完成]`

**文件**：`src/main/java/service/AttendanceService.java`
**对应**：AttendanceServlet

**核心方法**：
| 方法 | 功能 | 复杂度 |
|------|------|--------|
| checkIn(userId) | 签到 | 中 |
| checkOut(userId) | 签退 | 中 |
| listAttendance(filter, page) | 考勤列表 | 低 |
| getAttendanceStats(userId) | 考勤统计 | 中 |
| approveMakeup(id, operatorId) | 审批补签 | 中 |
| rejectMakeup(id, operatorId) | 拒绝补签 | 低 |
| getMyAttendance(userId, page) | 我的考勤 | 低 |
| getMyStats(userId) | 我的统计 | 低 |
| applyMakeup(date, reason, userId) | 申请补签 | 中 |

**涉及的DAO**：AttendanceDAO, AttendanceMakeupDAO, UserDAO

**AttendanceApiServlet 端点** `[已完成]`：
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/attendance` | 考勤列表 |
| GET | `/api/attendance/stats` | 考勤统计 |
| POST | `/api/attendance/check-in` | 签到 |
| POST | `/api/attendance/check-out` | 签退 |
| GET | `/api/attendance/my` | 我的考勤 |
| GET | `/api/attendance/my/stats` | 我的统计 |
| POST | `/api/attendance/makeup` | 补签申请 |
| GET | `/api/attendance/makeup` | 补签列表 |
| POST | `/api/attendance/{id}/approve` | 审批通过 |
| POST | `/api/attendance/{id}/reject` | 审批拒绝 |

---

### 4.3 RecruitService 招新服务 `[已完成]`

**文件**：`src/main/java/service/RecruitService.java`（新建）
**DTO文件**：`src/main/java/dto/RecruitApplicationDTO.java`（新建）
**对应**：RecruitServlet

**核心方法**：
| 方法 | 功能 | 复杂度 |
|------|------|--------|
| submitApplication(dto) | 提交申请 | 高 |
| listApplications(year, status, keyword, round) | 申请列表 | 中 |
| getApplicationDetail(id) | 申请详情 | 低 |
| approveApplication(id, operatorId) | 审批通过 | 高 |
| rejectApplication(id, operatorId) | 审批拒绝 | 中 |
| deleteApplication(id) | 删除申请 | 低 |
| countPending() | 待审核数量 | 低 |
| findAllYears() | 所有年份 | 低 |
| validateApplication(dto) | 申请信息校验 | 中 |

**涉及的DAO**：RecruitApplicationDAO, UserDAO, MemberProfileDAO
**状态常量**：STATUS_PENDING=1、STATUS_APPROVED=2、STATUS_REJECTED=0

**RecruitApiServlet 端点** `[已完成]`：
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/recruit` | 申请列表 |
| GET | `/api/recruit/{id}` | 申请详情 |
| POST | `/api/recruit` | 提交申请 |
| POST | `/api/recruit/{id}/approve` | 审批通过 |
| POST | `/api/recruit/{id}/reject` | 审批拒绝 |
| DELETE | `/api/recruit/{id}` | 删除申请 |
| GET | `/api/recruit/years` | 所有年份 |
| GET | `/api/recruit/count` | 待审核数量 |

**TDD测试**：RecruitServiceTest 92个用例、RecruitApiServletTest 41个用例，全部通过

---

### 4.4 ResumeService 简历服务 `[已完成]`

**文件**：`src/main/java/service/ResumeService.java`
**合并**：ResumeServlet + ResumeAwardServlet + ResumeEducationServlet + ResumeProjectServlet + ResumeSkillServlet

**核心方法**：
| 方法 | 功能 | 复杂度 |
|------|------|--------|
| createResume(userId) | 创建简历 | 中 |
| updateResume(id, dto, userId) | 更新简历 | 中 |
| deleteResume(id, userId) | 删除简历(软) | 低 |
| setDefaultResume(id, userId) | 设为默认 | 低 |
| getResumeDetail(id, userId) | 简历详情 | 中 |
| listResumes(userId, page) | 简历列表 | 低 |
| getRecycleBin(userId) | 回收站 | 低 |
| restoreResume(id, userId) | 恢复简历 | 低 |
| permanentDelete(id, userId) | 永久删除 | 中 |
| addEducation(resumeId, dto, userId) | 添加教育经历 | 中 |
| updateEducation(id, dto, userId) | 更新教育经历 | 中 |
| deleteEducation(id, userId) | 删除教育经历 | 低 |
| addSkill(resumeId, dto, userId) | 添加技能 | 低 |
| updateSkill(id, dto, userId) | 更新技能 | 低 |
| deleteSkill(id, userId) | 删除技能 | 低 |
| addProject(resumeId, dto, userId) | 添加项目经历 | 中 |
| updateProject(id, dto, userId) | 更新项目经历 | 中 |
| deleteProject(id, userId) | 删除项目经历 | 低 |
| addAward(resumeId, dto, userId) | 添加获奖 | 中 |
| updateAward(id, dto, userId) | 更新获奖 | 中 |
| deleteAward(id, userId) | 删除获奖 | 低 |

**涉及的DAO**：ResumeDAO, ResumeEducationDAO, ResumeSkillDAO, ResumeProjectDAO, ResumeAwardDAO, UserDAO

**ResumeApiServlet 端点** `[测试完成]`：
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/resumes` | 简历列表 |
| GET | `/api/resumes/{id}` | 简历详情 |
| POST | `/api/resumes` | 创建简历 |
| PUT | `/api/resumes/{id}` | 更新简历 |
| DELETE | `/api/resumes/{id}` | 删除简历 |
| PUT | `/api/resumes/{id}/default` | 设为默认 |
| GET | `/api/resumes/{id}/education` | 教育经历 |
| POST | `/api/resumes/{id}/education` | 添加教育 |
| PUT | `/api/resumes/{id}/education/{eid}` | 更新教育 |
| DELETE | `/api/resumes/{id}/education/{eid}` | 删除教育 |
| GET | `/api/resumes/{id}/skills` | 技能列表 |
| POST | `/api/resumes/{id}/skills` | 添加技能 |
| PUT | `/api/resumes/{id}/skills/{sid}` | 更新技能 |
| DELETE | `/api/resumes/{id}/skills/{sid}` | 删除技能 |
| GET | `/api/resumes/{id}/projects` | 项目经历 |
| POST | `/api/resumes/{id}/projects` | 添加项目 |
| PUT | `/api/resumes/{id}/projects/{pid}` | 更新项目 |
| DELETE | `/api/resumes/{id}/projects/{pid}` | 删除项目 |
| GET | `/api/resumes/{id}/awards` | 获奖经历 |
| POST | `/api/resumes/{id}/awards` | 添加获奖 |
| PUT | `/api/resumes/{id}/awards/{aid}` | 更新获奖 |
| DELETE | `/api/resumes/{id}/awards/{aid}` | 删除获奖 |
| GET | `/api/resumes/recycle-bin` | 回收站 |
| POST | `/api/resumes/{id}/restore` | 恢复简历 |
| DELETE | `/api/resumes/{id}/permanent` | 永久删除 |

---

### P3 验证清单

- [ ] GroupService 分层完成 + 50个测试通过
- [ ] AttendanceService 分层完成 + 40个测试通过
- [x] RecruitService 分层完成 + 92个测试通过 ✅
- [x] ResumeService 分层完成 + 159个测试通过 ✅
- [ ] GroupApiServlet API化 + 60个测试通过
- [ ] AttendanceApiServlet API化 + 45个测试通过
- [x] RecruitApiServlet API化 + 41个测试通过 ✅
- [x] ResumeApiServlet API化 + 25个测试通过 ✅
- [ ] `mvn verify` 全部通过
- [ ] JSP功能冒烟测试通过

---

## 五、P4阶段：扩展业务分层与API化

### 5.1 NewsService 新闻服务 `[待分层]`

**文件**：`src/main/java/service/NewsService.java`
**对应**：NewsServlet

**核心方法**：
| 方法 | 功能 | 复杂度 |
|------|------|--------|
| listNews(filter, page, pageSize) | 新闻列表(分页) | 低 |
| getNewsByType(type, page, pageSize) | 按类型查询 | 低 |
| getNewsDetail(id) | 新闻详情 | 低 |
| createNews(dto, authorId) | 创建新闻 | 中 |
| updateNews(id, dto, operatorId) | 更新新闻 | 中 |
| deleteNews(id, operatorId) | 删除新闻(软删除) | 低 |
| publishNews(id, operatorId) | 发布新闻 | 中 |
| unpublishNews(id, operatorId) | 取消发布 | 低 |

**涉及的DAO**：NewsDAO, FileStorageDAO, UserDAO

**DTO**：NewsDTO, NewsFilterDTO

**NewsApiServlet 端点** `[待API化]`：
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/news` | 新闻列表 |
| GET | `/api/news/{id}` | 新闻详情 |
| POST | `/api/news` | 创建新闻 |
| PUT | `/api/news/{id}` | 更新新闻 |
| DELETE | `/api/news/{id}` | 删除新闻 |
| POST | `/api/news/{id}/publish` | 发布新闻 |
| POST | `/api/news/{id}/unpublish` | 取消发布 |
| GET | `/api/news/types` | 新闻类型列表 |

---

### 5.2 ProblemService 问题服务 `[待分层]`

**文件**：`src/main/java/service/ProblemService.java`
**合并**：ProblemReportServlet + MemberProblemServlet + ProblemManagementServlet

**核心方法**：
| 方法 | 功能 | 复杂度 |
|------|------|--------|
| submitProblem(dto, userId) | 提交问题 | 中 |
| getProblemDetail(id) | 问题详情 | 低 |
| listProblems(filter, page, pageSize) | 问题列表(分页) | 中 |
| getMyProblems(userId, page, pageSize) | 我的问题列表 | 低 |
| updateProblem(id, dto, operatorId) | 更新问题 | 中 |
| updateStatus(id, status, adminComment, operatorId) | 更新状态 | 中 |
| updateCategory(id, category, operatorId) | 更新分类 | 低 |
| addComment(id, adminComment, operatorId) | 添加管理员备注 | 低 |
| deleteProblem(id, operatorId) | 删除问题 | 低 |
| getStatistics() | 问题统计 | 低 |

**涉及的DAO**：ProblemReportDAO, ProblemManagementDAO, UserDAO

**DTO**：ProblemDTO, ProblemFilterDTO

**ProblemApiServlet 端点** `[待API化]`：
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/problems` | 问题列表(分页) |
| GET | `/api/problems/{id}` | 问题详情 |
| POST | `/api/problems` | 提交问题 |
| PUT | `/api/problems/{id}` | 更新问题 |
| DELETE | `/api/problems/{id}` | 删除问题 |
| POST | `/api/problems/{id}/status` | 更新状态 |
| POST | `/api/problems/{id}/category` | 更新分类 |
| POST | `/api/problems/{id}/comment` | 添加备注 |
| GET | `/api/problems/my` | 我的问题 |
| GET | `/api/problems/stats` | 问题统计 |

---

### 5.3 MemberService 成员服务 `[待分层]`

**文件**：`src/main/java/service/MemberService.java`
**合并**：MemberServlet + ProfileServlet

**核心方法**：
| 方法 | 功能 | 复杂度 |
|------|------|--------|
| listMembers(filter, page, pageSize) | 成员列表(分页) | 中 |
| getMemberDetail(id) | 成员详情(含档案) | 低 |
| createMember(dto) | 创建成员 | 中 |
| updateMember(id, dto, operatorId) | 更新成员 | 中 |
| deleteMember(id, operatorId) | 删除成员 | 低 |
| enableMember(id, operatorId) | 启用成员 | 低 |
| disableMember(id, operatorId) | 禁用成员 | 低 |
| resetPassword(id, operatorId) | 重置密码 | 低 |
| getMemberAwards(id) | 成员获奖列表 | 低 |
| updateProfile(id, dto, userId) | 更新个人档案 | 中 |
| getProfile(id) | 获取个人档案 | 低 |
| uploadAvatar(id, file, userId) | 上传头像 | 中 |

**涉及的DAO**：UserDAO, MemberProfileDAO, FileStorageDAO, AdminProfileDAO, AwardDAO

**DTO**：MemberDTO, MemberFilterDTO, ProfileDTO

**MemberApiServlet 端点** `[待API化]`：
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/members` | 成员列表(分页) |
| GET | `/api/members/{id}` | 成员详情 |
| POST | `/api/members` | 添加成员 |
| PUT | `/api/members/{id}` | 更新成员 |
| DELETE | `/api/members/{id}` | 删除成员 |
| POST | `/api/members/{id}/enable` | 启用成员 |
| POST | `/api/members/{id}/disable` | 禁用成员 |
| POST | `/api/members/{id}/reset-password` | 重置密码 |
| GET | `/api/members/{id}/awards` | 成员获奖列表 |
| GET | `/api/members/{id}/profile` | 个人档案 |
| PUT | `/api/members/{id}/profile` | 更新档案 |
| POST | `/api/members/{id}/avatar` | 上传头像 |

---

### P4 验证清单

- [ ] NewsService 分层完成 + 35个测试通过
- [ ] ProblemService 分层完成 + 40个测试通过
- [ ] MemberService 分层完成 + 45个测试通过
- [ ] NewsApiServlet API化 + 40个测试通过
- [ ] ProblemApiServlet API化 + 45个测试通过
- [ ] MemberApiServlet API化 + 50个测试通过
- [ ] `mvn verify` 全部通过
- [ ] JSP功能冒烟测试通过

---

## 六、P5阶段：收尾分层与优化

### 6.1 StudyService 学习服务 `[待分层]`

**文件**：`src/main/java/service/StudyService.java`
**对应**：StudySessionServlet

**核心方法**：
| 方法 | 功能 | 复杂度 |
|------|------|--------|
| startSession(userId) | 开始学习 | 低 |
| endSession(userId) | 结束学习(获取当前进行中) | 低 |
| autoEndSession() | 自动结束超时会话(22:00) | 中 |
| getSessionDetail(id) | 学习记录详情 | 低 |
| listSessions(filter, page, pageSize) | 学习记录列表(分页) | 低 |
| getMySessions(userId, page, pageSize) | 我的学习记录 | 低 |
| getTodaySession(userId) | 获取今日进行中会话 | 低 |
| getStatistics(userId) | 学习统计 | 中 |
| getWeekStatistics(userId) | 本周学习统计 | 中 |
| getConsecutiveDays(userId) | 连续学习天数 | 低 |

**涉及的DAO**：StudySessionDAO, UserDAO

**DTO**：StudySessionDTO, StudyFilterDTO, StudyStatisticsDTO

**StudyApiServlet 端点** `[待API化]`：
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/study` | 学习记录列表(分页) |
| GET | `/api/study/{id}` | 学习记录详情 |
| GET | `/api/study/today` | 今日进行中会话 |
| POST | `/api/study/start` | 开始学习 |
| POST | `/api/study/end` | 结束学习 |
| GET | `/api/study/my` | 我的学习记录 |
| GET | `/api/study/stats` | 学习统计 |
| GET | `/api/study/week-stats` | 本周学习统计 |

---

### 6.2 LogService 日志服务 `[待分层]`

**文件**：`src/main/java/service/LogService.java`
**对应**：LogServlet

**核心方法**：
| 方法 | 功能 | 复杂度 |
|------|------|--------|
| listLogs(filter, page, pageSize) | 日志列表(分页) | 低 |
| getLogDetail(id) | 日志详情 | 低 |

**涉及的DAO**：OperationLogDAO

**DTO**：LogDTO, LogFilterDTO

**LogApiServlet 端点** `[待API化]`：
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/logs` | 日志列表(分页) |
| GET | `/api/logs/{id}` | 日志详情 |

---

### 6.3 优化项

| 优化项 | 说明 | 优先级 |
|--------|------|--------|
| API统一错误处理 | BaseApiServlet统一异常捕获 | 中 |
| API限流 | 基于Redis的API限流实现 | 低 |
| API版本管理 | URL版本化 `/api/v1/*` | 低 |
| API文档生成 | Swagger/OpenAPI注解 | 低 |

---

### P5 验证清单

- [ ] StudyService 分层完成 + 25个测试通过
- [ ] LogService 分层完成 + 15个测试通过
- [ ] StudyApiServlet API化 + 30个测试通过
- [ ] LogApiServlet API化 + 20个测试通过
- [ ] API统一错误处理测试
- [ ] `mvn verify` 全部通过

---

## 七、API约定（与P2保持一致）

- Content-Type: `application/json; charset=UTF-8`（上传用multipart）
- 成功：`{"code":0,"message":"ok","data":{...}}`
- 失败：`{"code":4xxx,"message":"...","data":null}`
- 分页：`data: {list:[], total, page, pageSize}`
- 认证：Session Cookie

---

## 八、技术债务清理

| Bug | 位置 | 描述 | 状态 |
|-----|------|------|------|
| 1 | ActivityParticipantDAO.getParticipantStatus | 读取列名错误 | `✅ 已修复` |
| 2 | AwardDAO.findApproved | 按competition_year排序但列名是year | `✅ 已修复` |
| 3 | ProjectApiServletTest | 5个测试失败（待分析） | `[未修复]` |

---

## 九、完整交付清单

| 阶段 | Service | API Servlet | 测试用例 | 状态 |
|------|---------|-------------|----------|------|
| P0 | - | BaseApiServlet | 54 | ✅ |
| P1 | 6个Service | - | 445+ | ✅ |
| P2 | - | 5个API Servlet | 303 | ✅ |
| P3 | 4个Service | 4个API Servlet | 370+ | ⏳ |
| P4 | 3个Service | 3个API Servlet | 270+ | ⏳ |
| P5 | 2个Service | 2个API Servlet | 130+ | ⏳ |
| **合计** | **15个Service** | **14个API Servlet** | **1500+** | |

---

## 十、变更记录

| 日期 | 阶段 | 变更内容 | 操作人 |
|------|------|---------|--------|
| 2026-07-19 | P0-P2 | 服务分层与API化重构计划 v1.0 | Claude Code |
| 2026-07-25 | P3-P5 | 整合为完整计划v2.0，新增"先分层后API化"原则 | Claude Code |
| 2026-07-25 | Bug修复 | 修复Bug1(ActivityParticipantDAO)、Bug2(AwardDAO)；Bug3(ProjectApiServletTest)待分析 | Claude Code |
| 2026-07-25 | P3 4.3 | 完成RecruitService招新服务：submitApplication/approveApplication/rejectApplication/listApplications/getApplicationDetail/deleteApplication/countPending/findAllYears/validateApplication共9个方法；92个TDD测试用例全部通过；TDD Red→Green→Refactor完整流程；execute(SUPPLIER<R>)统一事务入口模式；approveExistingUser/createUserAndProfile/checkEmailAvailability辅助方法；InvalidPathException异常模式 | Claude Code |
| 2026-07-25 | P3 4.3 | 完成RecruitApiServlet招新API：8个REST端点；41个TDD测试用例全部通过；requireAuth提取认证重复代码；parseAndValidatePathInfo改抛异常消除null返回模式；RecruitPathInfo内部类统一路径解析；完整TDD开发流程：测试先行→路由修复→路由修复验证 | Claude Code |
| 2026-07-25 | P4/P5 | 补充P4/P5详细设计(方法列表/复杂度/端点/DTO) | Claude Code |
| 2026-07-25 | P3 4.4 | 完成ResumeService简历服务：21个业务方法；159个TDD测试用例全部通过；TDD Red→Green→Refactor完整流程；validateSubItemOwnership函数式接口抽取子项目公共权限验证；删除6个未使用的辅助方法和内部异常类；代码从942行精简至873行(减少7.3%)；isBlank/isDeleted/applyXxxUpdates/buildXxxFromDTO等辅助方法清晰化命名与职责 | Claude Code |

---

## 附录A：Service开发模板

```java
// XxxService.java
public class XxxService {
    
    private final XxxDAO xxxDAO;
    private final OtherDAO otherDAO;
    
    // 构造器注入 (用于测试)
    public XxxService(XxxDAO xxxDAO, OtherDAO otherDAO) {
        this.xxxDAO = xxxDAO;
        this.otherDAO = otherDAO;
    }
    
    // 无参构造器 (运行时使用)
    public XxxService() {
        this(new XxxDAO(), new OtherDAO());
    }
    
    public Result<XxxDTO> createXxx(XxxDTO dto, int userId) {
        // 1. 参数校验
        if (dto == null || dto.getName() == null) {
            return Result.error(400, "参数错误");
        }
        
        // 2. 业务逻辑
        Xxx entity = new Xxx();
        entity.setName(dto.getName());
        // ...
        
        // 3. 事务处理
        return TransactionTemplate.execute(conn -> {
            int id = xxxDAO.insert(entity, conn);
            return Result.ok(xxxDAO.findById(id));
        });
    }
    
    public Result<List<XxxDTO>> listXxx(FilterDTO filter, int page, int pageSize) {
        // 业务逻辑
        List<Xxx> list = xxxDAO.findByFilter(filter, page, pageSize);
        long total = xxxDAO.countByFilter(filter);
        return Result.ok(PageUtil.build(list, total, page, pageSize));
    }
}
```

---

## 附录B：API Servlet开发模板

```java
// XxxApiServlet.java
public class XxxApiServlet extends BaseApiServlet {
    
    private XxxService xxxService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.xxxService = new XxxService();
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            sendUnauthorized(resp);
            return;
        }
        
        String path = derivePath(req);
        
        if (isListPath(path)) {
            handleList(req, resp, currentUser);
        } else if (isDetailPath(path)) {
            handleDetail(req, resp, currentUser);
        } else {
            sendError(resp, 400, "无效路径");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            sendUnauthorized(resp);
            return;
        }
        
        String path = derivePath(req);
        
        if (isCreatePath(path)) {
            handleCreate(req, resp, currentUser);
        } else {
            sendError(resp, 400, "无效路径");
        }
    }
    
    // ... handler methods
}
```
