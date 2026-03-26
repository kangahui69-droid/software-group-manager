-- ============================================
-- AI助手功能数据库脚本
-- 高校软件小组管理系统
-- ============================================

-- 创建AI对话日志表（原有表）
CREATE TABLE IF NOT EXISTS `ai_conversation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `session_id` VARCHAR(64) DEFAULT NULL COMMENT '会话ID',
    `user_id` INT DEFAULT NULL COMMENT '用户ID',
    `user_role` VARCHAR(20) DEFAULT NULL COMMENT '用户角色',
    `question` TEXT NOT NULL COMMENT '用户提问',
    `ai_answer` TEXT COMMENT 'AI回答',
    `source` VARCHAR(20) DEFAULT NULL COMMENT '回答来源：faq/llm',
    `reference_id` INT DEFAULT NULL COMMENT '关联FAQ ID',
    `rating` TINYINT DEFAULT NULL COMMENT '用户评分1-5',
    `is_validated` TINYINT DEFAULT '0' COMMENT '是否已验证：1-是，0-否',
    `validated_by` INT DEFAULT NULL COMMENT '验证人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_user_role` (`user_role`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_source` (`source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话日志表';

-- 创建FAQ知识表（原有表）
CREATE TABLE IF NOT EXISTS `ai_faq_knowledge` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT 'FAQ ID',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '问题分类',
    `question` VARCHAR(500) NOT NULL COMMENT '问题',
    `answer` TEXT NOT NULL COMMENT '答案',
    `keywords` VARCHAR(255) DEFAULT NULL COMMENT '关键词，用于匹配',
    `target_role` VARCHAR(20) DEFAULT 'ALL' COMMENT '目标角色：ADMIN/MEMBER/GUEST/ALL',
    `priority` INT DEFAULT '1' COMMENT '优先级1-5',
    `view_count` INT DEFAULT '0' COMMENT '查看次数',
    `useful_count` INT DEFAULT '0' COMMENT '点赞次数',
    `status` TINYINT DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
    `verified` TINYINT DEFAULT '0' COMMENT '是否已验证：1-是，0-否',
    `verified_at` DATETIME DEFAULT NULL COMMENT '验证时间',
    `verified_by` INT DEFAULT NULL COMMENT '验证人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_category` (`category`),
    INDEX `idx_target_role` (`target_role`),
    INDEX `idx_status` (`status`),
    INDEX `idx_verified` (`verified`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FAQ知识表';

-- 创建AI对话记录表
CREATE TABLE IF NOT EXISTS `ai_conversation` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `user_id` INT DEFAULT 0 COMMENT '用户ID，0表示游客',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID，唯一标识',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话记录表';

-- 创建AI消息表
CREATE TABLE IF NOT EXISTS `ai_message` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `conversation_id` INT NOT NULL COMMENT '会话ID，外键',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：user/assistant/system',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_conversation_id` (`conversation_id`),
    FOREIGN KEY (`conversation_id`) REFERENCES `ai_conversation`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI消息表';

-- 创建AI知识库表
CREATE TABLE IF NOT EXISTS `ai_knowledge_base` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类，如：奖项申报、活动报名',
    `question` VARCHAR(500) NOT NULL COMMENT '问题',
    `answer` TEXT NOT NULL COMMENT '答案',
    `keywords` VARCHAR(255) DEFAULT NULL COMMENT '关键词，用于匹配',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_category` (`category`),
    INDEX `idx_keywords` (`keywords`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI知识库表';

-- 创建AI提问统计表
CREATE TABLE IF NOT EXISTS `ai_faq_statistics` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `question_hash` VARCHAR(64) UNIQUE COMMENT '问题哈希值',
    `normalized_question` VARCHAR(500) COMMENT '标准化问题',
    `query_count` INT DEFAULT 1 COMMENT '查询次数',
    `avg_rating` DECIMAL(3,2) DEFAULT NULL COMMENT '平均评分',
    `last_query_at` DATETIME DEFAULT NULL COMMENT '最后查询时间',
    `suggested_faq` TINYINT DEFAULT 0 COMMENT '是否建议FAQ：1-是，0-否',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_query_count` (`query_count`),
    INDEX `idx_suggested` (`suggested_faq`),
    INDEX `idx_last_query` (`last_query_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI提问统计表';

-- ============================================
-- 初始化知识库数据
-- ============================================

INSERT INTO `ai_knowledge_base` (`category`, `question`, `answer`, `keywords`, `status`) VALUES
-- 新闻管理类
('新闻管理', '如何发布新的新闻？', '进入"新闻管理"页面 → 点击"发布新闻"按钮 → 填写标题、内容、类型（获奖/活动/通知）→ 选择发布状态 → 点击保存', '发布新闻,新闻管理,创建新闻', 1),
('新闻管理', '如何修改已发布的新闻？', '进入"新闻管理"页面 → 找到目标新闻 → 点击"编辑"按钮 → 修改内容 → 保存更新', '修改新闻,编辑新闻,新闻编辑', 1),
('新闻管理', '如何删除新闻？', '进入"新闻管理"页面 → 找到目标新闻 → 点击"删除"按钮 → 确认删除（注：已关联活动的新闻无法删除）', '删除新闻,移除新闻', 1),

-- 奖项管理类
('奖项管理', '如何审核成员提交的奖项申请？', '进入"奖项管理"页面 → 点击"待审核"筛选 → 查看奖项详情 → 点击"通过"或"拒绝"按钮 → 填写审核意见', '审核奖项,奖项审核,审批奖项,奖项申请', 1),
('奖项管理', '奖项申请被拒绝后还能重新申请吗？', '可以，成员可以在"我的奖项"页面重新提交申请', '重新申请,拒绝后申请', 1),

-- 活动管理类
('活动管理', '如何创建新活动？', '进入"活动管理"页面 → 点击"创建活动" → 填写活动名称、时间、地点 → 设置报名起止时间 → 点击保存', '创建活动,新建活动,活动创建', 1),
('活动管理', '如何查看活动报名情况？', '进入"活动管理"页面 → 点击目标活动的"查看详情" → 在详情页查看已报名成员列表', '查看报名,活动报名,报名情况', 1),

-- 成员管理类
('成员管理', '如何添加新成员？', '新成员通过招新报名系统自动创建，或管理员在"用户管理"中手动添加', '添加成员,新增成员', 1),
('成员管理', '如何重置成员密码？', '进入"用户管理"页面 → 找到目标用户 → 点击"重置密码" → 系统生成默认密码并通知用户（默认密码为123456）', '重置密码,密码重置,忘记密码', 1),

-- 项目管理类
('项目管理', '如何审批项目申请？', '进入"项目管理"页面 → 查看待审核项目 → 点击"详情"查看项目信息 → 点击"通过"或"拒绝"', '审批项目,项目审批,审核项目', 1),
('项目管理', '项目最多允许多少人参与？', '每个项目最多20名成员，由项目负责人添加', '项目人数,成员数量', 1),

-- 招新管理类
('招新管理', '如何处理新的招新报名？', '进入"报名管理"页面 → 查看待处理申请 → 点击"审核"查看详情 → 点击"同意"创建账号或"拒绝"驳回申请', '处理报名,招新审核,报名审核', 1),
('招新管理', '同意报名后成员需要做什么？', '系统会自动创建账号并发送通知，成员使用学号作为用户名登录，默认密码为123456', '同意报名,创建账号', 1),

-- 账号问题类
('账号问题', '忘记密码怎么办？', '联系管理员重置密码，默认密码为123456', '忘记密码,密码找回,登录问题', 1),
('账号问题', '账户被锁定怎么办？', '联系管理员解锁账户', '账户锁定,账号锁定,解锁', 1),

-- 通用操作类
('通用操作', '如何修改个人信息？', '登录后进入"个人中心" -> "个人信息" -> 点击"编辑"进行修改', '修改信息,编辑资料,个人信息', 1),
('通用操作', '如何提交奖项？', '登录后进入"我的奖项" -> 点击"提交奖项" -> 填写表单 -> 上传证书 -> 提交', '提交奖项,申报奖项,奖项申报', 1),
('通用操作', '如何报名参加活动？', '进入活动列表 -> 选择活动 -> 点击"报名" -> 确认报名（需在报名时间内）', '报名活动,参加活动,活动报名', 1),
('通用操作', '如何创建项目？', '登录后进入"我的项目" -> 点击"创建项目" -> 填写项目信息 -> 提交等待审核', '创建项目,新建项目,项目创建', 1);

-- ============================================
-- 脚本执行完成
-- ============================================
-- 使用说明：
-- 1. 确保您使用的是MySQL 5.7+或MySQL 8.0+
-- 2. 执行此脚本前请备份数据库
-- 3. 执行：source path/to/ai_assistant.sql
-- 或：mysql -u root -p software_group < ai_assistant.sql
-- ============================================