# 系统字典数据说明文档

## 1. 概述

系统字典表（dictionary）用于存储系统中所有需要下拉选择的数据，如比赛级别、奖项类型、用户角色等。通过字典表可以实现数据的统一管理和维护，便于后期扩展和修改。

## 2. 字典表结构

| 字段名 | 数据类型 | 说明 |
| --- | --- | --- |
| id | INT | 主键ID |
| code | VARCHAR(50) | 字典代码 |
| name | VARCHAR(100) | 字典名称 |
| type | VARCHAR(50) | 字典类型 |
| sort_order | INT | 排序顺序 |
| status | TINYINT | 状态：1-启用，0-禁用 |
| description | VARCHAR(255) | 描述 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

## 3. 字典数据详情

### 3.1 比赛级别 (COMPETITION_LEVEL)

| 代码 | 名称 | 排序 | 状态 | 描述 |
| --- | --- | --- | --- | --- |
| LEVEL_NATIONAL | 国家级 | 1 | 1 | 国家级比赛 |
| LEVEL_REGIONAL | 地区级别 | 2 | 1 | 地区级比赛 |
| LEVEL_PROVINCIAL | 省级 | 3 | 1 | 省级比赛 |
| LEVEL_OTHER | 其他级别 | 4 | 1 | 其他级别的比赛 |

### 3.2 奖项类型 (AWARD_TYPE)

| 代码 | 名称 | 排序 | 状态 | 描述 |
| --- | --- | --- | --- | --- |
| TYPE_INDIVIDUAL | 个人 | 1 | 1 | 个人奖项 |
| TYPE_TEAM | 团队 | 2 | 1 | 团队奖项 |

### 3.3 奖项类别 (AWARD_CATEGORY)

| 代码 | 名称 | 排序 | 状态 | 描述 |
| --- | --- | --- | --- | --- |
| CATEGORY_PROJECT | 项目 | 1 | 1 | 项目类奖项 |
| CATEGORY_ALGORITHM | 经典算法 | 2 | 1 | 算法类奖项 |
| CATEGORY_AI | 人工智能 | 3 | 1 | 人工智能类奖项 |
| CATEGORY_DOCUMENT | 文档类 | 4 | 1 | 文档类奖项 |
| CATEGORY_OTHER | 其他 | 5 | 1 | 其他类别的奖项 |

### 3.4 用户角色 (USER_ROLE)

| 代码 | 名称 | 排序 | 状态 | 描述 |
| --- | --- | --- | --- | --- |
| ROLE_ADMIN | 管理员 | 1 | 1 | 系统管理员 |
| ROLE_MEMBER | 成员 | 2 | 1 | 团队成员 |
| ROLE_STUDENT | 学生 | 3 | 1 | 普通学生 |

### 3.5 奖项状态 (AWARD_STATUS)

| 代码 | 名称 | 排序 | 状态 | 描述 |
| --- | --- | --- | --- | --- |
| PENDING | 待审核 | 1 | 1 | 待管理员审核的奖项 |
| APPROVED | 已审核 | 2 | 1 | 管理员已审核通过的奖项 |
| REJECTED | 已拒绝 | 3 | 1 | 管理员已拒绝的奖项 |

### 3.6 新闻状态 (NEWS_STATUS)

| 代码 | 名称 | 排序 | 状态 | 描述 |
| --- | --- | --- | --- | --- |
| DRAFT | 草稿 | 1 | 1 | 未发布的新闻草稿 |
| PUBLISHED | 已发布 | 2 | 1 | 已发布的新闻 |
| ARCHIVED | 已归档 | 3 | 1 | 已归档的新闻 |

### 3.7 活动类型 (ACTIVITY_TYPE)

| 代码 | 名称 | 排序 | 状态 | 描述 |
| --- | --- | --- | --- | --- |
| LECTURE | 讲座 | 1 | 1 | 技术讲座、学术讲座等 |
| SEMINAR | 座谈 | 2 | 1 | 座谈会、研讨会 |
| TEA_PARTY | 茶话会 | 3 | 1 | 休闲交流活动 |
| PROJECT_INTRO | 项目介绍会 | 4 | 1 | 项目展示、介绍活动 |
| WORKSHOP | 工作坊 | 5 | 1 | 实践工作坊 |
| COMPETITION | 竞赛 | 6 | 1 | 编程竞赛、技术竞赛 |
| TRAINING | 培训 | 7 | 1 | 技术培训、技能培训 |
| TEAM_BUILDING | 团建 | 8 | 1 | 团队建设活动 |
| OTHER | 其他 | 9 | 1 | 其他类型活动 |

### 3.8 报名状态 (REGISTRATION_STATUS)

| 代码 | 名称 | 排序 | 状态 | 描述 |
| --- | --- | --- | --- | --- |
| PENDING | 申请审核中 | 1 | 1 | 报名待管理员确认 |
| CONFIRMED | 申请已确认 | 2 | 1 | 报名已通过审核 |
| REJECTED | 申请已驳回 | 3 | 1 | 报名被管理员驳回 |
| EXPIRED | 申请已过期 | 4 | 1 | 超过报名截止时间未审核（动态计算） |

### 3.9 项目类型 (PROJECT_TYPE)

| 代码 | 名称 | 排序 | 状态 | 描述 |
| --- | --- | --- | --- | --- |
| WEB_DEV | WEB开发 | 1 | 1 | Web前端/后端开发项目 |
| APP_DEV | APP开发 | 2 | 1 | 移动应用开发项目 |
| CS_DEV | CS开发 | 3 | 1 | 客户端/服务器开发项目 |
| PROTOCOL_DEV | 协议开发 | 4 | 1 | 网络协议开发项目 |
| COMPREHENSIVE_DEV | 综合开发 | 5 | 1 | 综合类开发项目 |
| HARMONY_DEV | 鸿蒙开发 | 6 | 1 | 鸿蒙系统应用开发 |
| GAME_DEV | 游戏开发 | 7 | 1 | 游戏开发项目 |
| AI_APP | 人工智能应用 | 8 | 1 | AI应用开发项目 |
| ALGORITHM_DESIGN | 算法设计 | 9 | 1 | 算法设计与实现项目 |

### 3.10 项目标签 (PROLABEL)

| 代码 | 名称 | 排序 | 状态 | 描述 |
| --- | --- | --- | --- | --- |
| BEGINNER_FRIENDLY | 适合新手 | 1 | 1 | 适合新手参与的项目 |
| CHALLENGING | 有挑战 | 2 | 1 | 具有挑战性的项目 |
| INNOVATIVE | 创新项目 | 3 | 1 | 创新类项目 |
| RESEARCH | 科研类 | 4 | 1 | 科研类项目 |
| PRACTICAL | 实战项目 | 5 | 1 | 实战落地项目 |
| OPEN_SOURCE | 开源项目 | 6 | 1 | 开源项目 |
| TEAM_COLLABORATION | 团队协作 | 7 | 1 | 注重团队协作的项目 |

### 3.11 项目状态 (PROJECT_STATUS)

| 代码 | 名称 | 排序 | 状态 | 描述 |
| --- | --- | --- | --- | --- |
| PENDING | 申请审核中 | 1 | 1 | 项目申请待审批 |
| APPROVED | 确认成立 | 2 | 1 | 项目已审批通过，正式成立 |
| IN_PROGRESS | 进行中 | 3 | 1 | 项目已开始执行 |
| COMPLETED | 已完成 | 4 | 1 | 项目已完成 |
| CANCELED | 项目取消 | 5 | 1 | 项目被取消 |
| REJECTED | 申请驳回 | 6 | 1 | 项目申请被驳回 |
| PAUSED | 项目暂停 | 7 | 1 | 项目暂停执行 |

### 3.12 项目历史操作类型 (PROJECT_HISTORY_OPERATION)

| 代码 | 名称 | 排序 | 状态 | 描述 |
| --- | --- | --- | --- | --- |
| PROJECT_APPLY | 项目申请 | 1 | 1 | 成员申请创建项目 |
| PROJECT_APPROVE | 项目审批通过 | 2 | 1 | 管理员审批通过项目 |
| PROJECT_REJECT | 项目审批驳回 | 3 | 1 | 管理员驳回项目申请 |
| PROJECT_TRANSFER | 项目管理员转移 | 4 | 1 | 项目管理员变更 |
| MEMBER_JOIN | 成员加入 | 5 | 1 | 成员加入项目 |
| MEMBER_APPLY | 成员申请 | 6 | 1 | 成员申请加入项目 |
| MEMBER_APPROVE | 成员审批通过 | 7 | 1 | 项目管理员批准加入申请 |
| MEMBER_REJECT | 成员审批驳回 | 8 | 1 | 项目管理员驳回加入申请 |
| MEMBER_LEAVE | 成员离开 | 9 | 1 | 成员离开项目 |
| PROJECT_INFO_UPDATE | 项目信息修改 | 10 | 1 | 项目信息被更新 |
| PROJECT_STATUS_CHANGE | 项目状态变更 | 11 | 1 | 项目状态发生变化 |
| PROJECT_LABEL_ADD | 添加项目标签 | 12 | 1 | 添加项目标签 |
| PROJECT_LABEL_REMOVE | 移除项目标签 | 13 | 1 | 移除项目标签 |

## 4. 字典使用说明

1. **添加新字典类型**：在dictionary表中添加新的type值和对应的code、name等信息
2. **修改字典值**：直接更新dictionary表中对应的记录
3. **禁用字典值**：将status字段设置为0即可禁用该字典值，但不会删除数据
4. **查询字典**：通过DictionaryDAO类的findByType方法获取指定类型的所有字典值

## 5. 字典扩展计划

### 5.1 已添加的字典类型

| 字典类型 | 说明 | 状态 |
| --- | --- | --- |
| ACTIVITY_TYPE | 活动类型（讲座、座谈、茶话会等） | ✅ 已添加 |
| REGISTRATION_STATUS | 报名状态（待审核、已确认、已驳回、已过期） | ✅ 已添加 |
| PROJECT_TYPE | 项目类型（WEB开发、APP开发、鸿蒙开发等） | ✅ 已添加（2026-02-23） |
| PROLABEL | 项目标签（适合新手、有挑战、创新项目等） | ✅ 已添加（2026-02-23） |
| PROJECT_STATUS | 项目状态（申请审核中、确认成立、项目暂停等） | ✅ 已添加（2026-02-23） |
| PROJECT_HISTORY_OPERATION | 项目历史操作类型 | ✅ 已添加（2026-02-23） |

### 5.2 待添加的字典类型

| 字典类型 | 说明 | 计划添加时间 |
| --- | --- | --- |
| PROJECT_STATUS | 项目状态 | ✅ 已添加（2026-02-23） |
| PROJECT_HISTORY_OPERATION | 项目历史操作类型 | ✅ 已添加（2026-02-23） |
| GENDER | 性别 | 后续版本 |
| EDUCATION_LEVEL | 学历 | 后续版本 |
| MAJOR | 专业 | 后续版本 |

### 5.2 待完善的字典值

1. **比赛级别**：考虑添加国际级比赛
2. **奖项类别**：细化算法、人工智能等类别的子类
3. **用户角色**：考虑添加导师、嘉宾等角色

## 6. 代码示例

### 6.1 获取字典值的Java代码示例

```java
DictionaryDAO dictionaryDAO = new DictionaryDAO();
List<Dictionary> competitionLevels = dictionaryDAO.findByType("COMPETITION_LEVEL");
for (Dictionary dict : competitionLevels) {
    System.out.println(dict.getCode() + " - " + dict.getName());
}
```

### 6.2 JSP页面中使用字典的示例

```jsp
<select class="form-control" name="competitionLevel">
    <option value="">请选择</option>
    <c:forEach var="dict" items="${competitionLevels}">
        <option value="${dict.code}" ${award.competitionLevel eq dict.code ? 'selected' : ''}>
            ${dict.name}
        </option>
    </c:forEach>
</select>
```

## 7. 维护说明

1. 字典数据的增删改查操作由系统管理员负责
2. 修改字典值时需确保不会影响现有业务数据
3. 添加新字典类型时需同步更新DictionaryDAO和相关业务代码
4. 定期备份字典数据，确保数据安全

---

**文档更新记录**

| 更新时间 | 更新内容 | 更新人 |
| --- | --- | --- |
| 2026-01-12 | 初始化文档，添加基本字典类型和数据 | 系统管理员 |
| 2026-02-19 | 添加活动类型字典(ACTIVITY_TYPE)和报名状态字典(REGISTRATION_STATUS) | 系统管理员 |
| 2026-02-23 | 添加项目类型字典(PROJECT_TYPE)、项目标签字典(PROLABEL)、项目状态字典(PROJECT_STATUS)、项目历史操作类型字典(PROJECT_HISTORY_OPERATION) | 系统管理员 |
