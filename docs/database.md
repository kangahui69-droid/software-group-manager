# 高校软件小组管理系统数据库结构

## 数据库表结构

### activity表
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 活动ID |
| name | varchar(100) | NOT NULL | 活动名称 |
| description | text | | 活动内容介绍 |
| activity_type | varchar(50) | | 活动类型（LECTURE/SEMINAR/TEA_PARTY/PROJECT_INTRO等） |
| activity_start_time | datetime | | 活动开始时间 |
| activity_end_time | datetime | | 活动结束时间 |
| location | varchar(100) | | 活动地点 |
| organizers | varchar(255) | | 组织人（支持多个，逗号分隔） |
| contact_info | varchar(100) | | 联系方式 |
| registration_start_time | datetime | | 报名开始时间 |
| registration_end_time | datetime | | 报名截止时间 |
| max_participants | int | | 最大参与人数 |
| status | enum('upcoming','ongoing','completed','canceled','rejected') | DEFAULT 'upcoming' | 活动进行状态 |
| approval_status | enum('pending','approved','rejected') | DEFAULT 'approved' | 审批状态 |
| deleted | tinyint | DEFAULT 0 | 软删除标志：0-正常, 1-已删除 |
| created_at | timestamp | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | timestamp | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### activity_participant表
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| activity_id | int | NOT NULL | 活动ID |
| user_id | int | NOT NULL | 用户ID |
| status | enum('pending','confirmed','rejected','expired') | DEFAULT 'pending' | 报名状态 |
| created_at | timestamp | DEFAULT CURRENT_TIMESTAMP | 报名时间 |
| updated_at | timestamp | DEFAULT NULL | 更新时间 |
| notes | varchar(255) | | 备注（驳回原因等） |
| deleted | tinyint | DEFAULT 0 | 软删除标志：0-正常, 1-已删除 |

### admin_profile表
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 管理员资料ID |
| user_id | int | NOT NULL | 用户ID |
| title | varchar(50) | | 职称 |
| department | varchar(100) | | 部门 |
| education | varchar(50) | | 学历 |
| research_area | varchar(200) | | 研究领域 |
| bio | text | | 个人简介 |
| status | tinyint(1) | NOT NULL DEFAULT '1' | 状态 |
| created_at | datetime | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| avatar_file_id | int | | 头像文件ID，关联file_storage表 |

### award表
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 奖项ID |
| name | varchar(100) | NOT NULL | 奖项名称 |
| competition | varchar(100) | NOT NULL | 比赛名称 |
| year | int | NOT NULL | 获奖年份 |
| created_at | timestamp | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| competition_time | date | | 比赛时间 |
| competition_location | varchar(200) | | 比赛地点 |
| competition_session | varchar(100) | | 比赛界别 |
| award_type | int | | 奖项类型（个人，团队） |
| award_category | int | | 奖项类别（算法，项目等） |
| team_name | varchar(100) | | 团队名称 |
| award_status | varchar(50) | DEFAULT 'PENDING' | 状态 |
| created_by | int | | 创建人 |
| approved_by | int | | 通过人 |
| approved_at | datetime | | 通过时间 |
| updated_at | datetime | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| award_level | int | | 奖项等级，关联dictionary表 |
| competition_level | int | | 比赛等级，关联dictionary表 |
| deleted | tinyint | DEFAULT 0 | 软删除标志：0-正常, 1-已删除 |

### award_image表
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 奖项图片ID |
| award_id | int | NOT NULL | 奖项ID号 |
| member_id | int | | 奖项成员ID（可以算作上传图片的人） |
| is_main | tinyint | NOT NULL DEFAULT '0' | 是否团队主持 |
| created_at | datetime | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| file_storage_id | int | NOT NULL | 文件存储ID |

### award_member表
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| award_id | int | NOT NULL | 奖项ID |
| member_id | int | NOT NULL | 成员ID |

### dictionary表
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 字典ID |
| code | varchar(50) | NOT NULL | 字典编码 |
| name | varchar(100) | NOT NULL | 字典名称 |
| type | varchar(50) | NOT NULL | 字典类型 |
| sort_order | int | DEFAULT '0' | 排序顺序 |
| status | tinyint | DEFAULT '1' | 状态 |
| description | varchar(255) | | 描述 |
| created_at | datetime | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### file_storage表
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 文件存储ID |
| create_by | int | NOT NULL | 创建者ID |
| original_name | varchar(255) | NOT NULL | 原始文件名 |
| stored_name | varchar(255) | NOT NULL | 存储文件名 |
| file_path | varchar(500) | NOT NULL | 文件路径 |
| file_type | varchar(50) | | 文件类型 |
| file_size | bigint | | 文件大小 |
| category | varchar(50) | | 文件分类 |
| status | tinyint | DEFAULT 1 | 软删除标志：1-正常, 0-已删除 |
| created_at | datetime | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### member_profile表
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 成员资料ID |
| user_id | int | NOT NULL | 用户ID |
| student_id | varchar(20) | NOT NULL | 学号（创建后不可修改） |
| major | varchar(100) | NOT NULL | 专业 |
| grade | varchar(20) | NOT NULL | 年级 |
| birthday | date | | 生日 |
| gender | enum('male','female','other') | DEFAULT 'other' | 性别 |
| introduction | text | | 个人介绍 |
| skills | text | | 技能 |
| github | varchar(100) | | GitHub |
| blog | varchar(100) | | 博客 |
| updated_at | timestamp | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| avatar_file_id | int | | 头像文件ID，关联file_storage表 |
| created_at | timestamp | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**注**：student_id（学号）和 user.name（姓名）在账号创建后不可修改，确保活动和项目中的信息一致性。

### news表
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 新闻ID |
| title | varchar(200) | NOT NULL | 新闻标题 |
| type | enum('award','activity','notice') | NOT NULL | 新闻类型 |
| content_path | varchar(255) | NOT NULL | 新闻地址，html文件所在地 |
| summary | varchar(500) | | 新闻摘要 |
| author_id | int | | 作者ID |
| status | tinyint(1) | NOT NULL DEFAULT '1' | 审核状态 |
| created_at | datetime | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### operation_log表
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 操作日志ID |
| user_id | int | | 用户ID |
| username | varchar(50) | | 用户名 |
| operation | varchar(100) | NOT NULL | 操作 |
| module | varchar(50) | | 模块 |
| description | text | | 描述 |
| ip_address | varchar(50) | | IP地址 |
| user_agent | varchar(500) | | 用户代理 |
| created_at | datetime | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### project表
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 项目ID |
| name | varchar(100) | NOT NULL | 项目名称 |
| description | text | | 项目描述 |
| category | varchar(50) | | 项目类别 |
| year | int | NOT NULL | 年份 |
| expected_start_date | date | | 期望开始日期（项目成员填写） |
| expected_end_date | date | | 期望结束日期（项目成员填写） |
| actual_start_date | date | | 实际开始日期（管理员填写） |
| actual_end_date | date | | 实际结束日期（管理员填写） |
| status | enum('pending','approved','in_progress','completed','canceled') | DEFAULT 'pending' | 项目状态 |
| leader_id | int | NOT NULL | 负责人ID |
| admin_id | int | | 管理员ID |
| budget | decimal(10,2) | | 预算 |
| repo_url | varchar(500) | | 代码仓库地址 |
| doc_url | varchar(500) | | 文档地址 |
| approved_by | int | | 审批人ID |
| approved_at | datetime | | 审批时间 |
| deleted | tinyint | DEFAULT 0 | 软删除标志：0-正常, 1-已删除 |
| created_at | timestamp | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | timestamp | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### project_file表（项目文件）
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 项目文件ID |
| project_id | int | NOT NULL | 项目ID |
| file_id | int | NOT NULL | 文件ID，关联file_storage表 |
| description | varchar(200) | | 文件描述 |
| file_type | varchar(50) | | 文件类型 |
| sort_order | int | DEFAULT '0' | 排序顺序 |
| created_at | timestamp | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### project_history表（项目历史）
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 历史记录ID |
| project_id | int | NOT NULL | 项目ID |
| operation_type | varchar(50) | NOT NULL | 操作类型 |
| operator_id | int | NOT NULL | 操作人ID |
| operator_name | varchar(100) | | 操作人姓名 |
| description | text | | 描述 |
| old_value | text | | 旧值 |
| new_value | text | | 新值 |
| created_at | timestamp | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### project_image表（项目图片）
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 项目图片ID |
| project_id | int | NOT NULL | 项目ID |
| file_id | int | NOT NULL | 文件ID，关联file_storage表 |
| description | varchar(200) | | 图片描述 |
| sort_order | int | DEFAULT '0' | 排序顺序 |
| created_at | timestamp | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### project_label表（项目标签）
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 标签ID |
| project_id | int | NOT NULL | 项目ID |
| label_code | varchar(50) | NOT NULL | 标签代码，关联dictionary表 |
| created_at | timestamp | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### project_member表
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| project_id | int | NOT NULL | 项目ID |
| user_id | int | NOT NULL | 用户ID |
| role | varchar(50) | | 角色 |

### project_member_application表（项目成员申请）
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 申请ID |
| project_id | int | NOT NULL | 项目ID |
| user_id | int | NOT NULL | 用户ID |
| status | varchar(20) | DEFAULT 'PENDING' | 状态：PENDING/CONFIRMED/REJECTED |
| applied_at | timestamp | DEFAULT CURRENT_TIMESTAMP | 申请时间 |
| handled_at | timestamp | DEFAULT NULL | 处理时间 |
| handled_by | int | | 处理人ID |
| reason | text | | 申请原因/拒绝原因 |

### project_plan表（项目计划）
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 计划ID |
| project_id | int | NOT NULL | 项目ID |
| title | varchar(200) | NOT NULL | 计划标题 |
| description | text | | 计划描述 |
| start_date | date | NOT NULL | 开始日期 |
| end_date | date | NOT NULL | 结束日期 |
| order_index | int | DEFAULT '0' | 排序顺序 |
| created_at | timestamp | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | timestamp | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### project_progress表（项目进度）
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 进度ID |
| project_id | int | NOT NULL | 项目ID |
| plan_id | int | | 关联的计划ID |
| title | varchar(200) | NOT NULL | 进度标题 |
| description | text | | 进度描述 |
| completion_rate | int | DEFAULT '0' | 完成百分比（0-100） |
| created_by | int | NOT NULL | 创建人ID |
| created_at | timestamp | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | timestamp | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### recruit_application表
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 招聘申请ID |
| name | varchar(50) | NOT NULL | 姓名（必填） |
| student_id | varchar(50) | | 学号（可为空） |
| major | varchar(100) | | 专业 |
| grade | varchar(20) | | 年级 |
| phone | varchar(20) | NOT NULL | 电话 |
| email | varchar(100) | | 邮箱 |
| reason | text | | 申请原因 |
| status | tinyint(1) | NOT NULL DEFAULT '1' | 状态：1-待处理, 2-已通过, 0-已拒绝 |
| created_at | datetime | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### user表
| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | int | NOT NULL AUTO_INCREMENT | 用户ID |
| username | varchar(50) | NOT NULL | 用户名（学号） |
| password | varchar(100) | NOT NULL | 密码 |
| name | varchar(50) | NOT NULL | 姓名（创建后不可修改） |
| email | varchar(100) | NOT NULL | 邮箱 |
| phone | varchar(20) | | 电话 |
| role | enum('ADMIN','MEMBER','TEACHER','GUEST') | NOT NULL DEFAULT 'MEMBER' | 角色 |
| status | int | DEFAULT '1' | 状态：1-启用, 0-禁用 |
| created_at | timestamp | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | timestamp | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**注**：name（姓名）字段在账号创建后不可修改，与 member_profile.student_id 共同确保数据一致性。

## 外键关系

| 表名 | 外键字段 | 引用表 | 引用字段 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| activity_participant | activity_id | activity | id | 活动参与关联活动 |
| activity_participant | user_id | user | id | 活动参与关联用户 |
| admin_profile | user_id | user | id | 管理员资料关联用户 |
| admin_profile | avatar_file_id | file_storage | id | 管理员资料关联头像文件 |
| award | created_by | user | id | 奖项关联创建者 |
| award | approved_by | user | id | 奖项关联审核者 |
| award | award_type | dictionary | id | 奖项关联奖项类型 |
| award | award_category | dictionary | id | 奖项关联奖项类别 |
| award | award_level | dictionary | id | 奖项关联奖项等级 |
| award | competition_level | dictionary | id | 奖项关联比赛等级 |
| award_image | award_id | award | id | 奖项图片关联奖项 |
| award_image | member_id | user | id | 奖项图片关联成员 |
| award_image | file_storage_id | file_storage | id | 奖项图片关联文件存储 |
| award_member | award_id | award | id | 奖项成员关联奖项 |
| award_member | member_id | user | id | 奖项成员关联成员 |
| file_storage | create_by | user | id | 文件存储关联创建者 |
| member_profile | user_id | user | id | 成员资料关联用户 |
| member_profile | avatar_file_id | file_storage | id | 成员资料关联头像文件 |
| news | author_id | user | id | 新闻关联作者 |
| project | leader_id | user | id | 项目关联负责人 |
| project | admin_id | user | id | 项目关联管理员 |
| project | approved_by | user | id | 项目关联审批人 |
| project_file | project_id | project | id | 项目文件关联项目 |
| project_file | file_id | file_storage | id | 项目文件关联文件存储 |
| project_history | project_id | project | id | 项目历史关联项目 |
| project_history | operator_id | user | id | 项目历史关联操作人 |
| project_image | project_id | project | id | 项目图片关联项目 |
| project_image | file_id | file_storage | id | 项目图片关联文件存储 |
| project_label | project_id | project | id | 项目标签关联项目 |
| project_member | project_id | project | id | 项目成员关联项目 |
| project_member | user_id | user | id | 项目成员关联用户 |
| project_member_application | project_id | project | id | 成员申请关联项目 |
| project_member_application | user_id | user | id | 成员申请关联用户 |
| project_member_application | handled_by | user | id | 成员申请关联处理人 |
| project_plan | project_id | project | id | 项目计划关联项目 |
| project_progress | project_id | project | id | 项目进度关联项目 |
| project_progress | plan_id | project_plan | id | 项目进度关联计划 |
| project_progress | created_by | user | id | 项目进度关联创建人 |

---

## 疑似问题/未使用字段汇总

（所有问题已修复，字段已清理）

### activity表
| 字段名 | 问题描述 |
| :--- | :--- |
| （无） | 所有字段均已使用，无冗余 |

### activity_participant表
| 字段名 | 问题描述 |
| :--- | :--- |
| （无） | 所有字段均已使用，无冗余 |

### project表
| 字段名 | 问题描述 |
| :--- | :--- |
| （无） | 所有字段均已使用 |

### member_profile表
| 字段名 | 问题描述 |
| :--- | :--- |
| （无） | avatar字段已移除，仅保留avatar_file_id |
