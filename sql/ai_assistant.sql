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

-- ==================== 游客可用功能 ====================
('游客指南', '如何登录系统？', '访问系统首页，点击右上角"登录"按钮，输入学号和密码即可登录。新成员需要管理员审核通过后才能获得账号。', '登录,登录系统,如何登录', 1),
('游客指南', '如何注册账号？', '系统不开放自主注册。新成员需联系管理员进行招新报名，由管理员审核通过后自动创建账号。', '注册,注册账号,新成员', 1),
('游客指南', '可以浏览哪些信息？', '游客可以浏览首页新闻列表、查看活动信息、浏览获奖新闻等公开内容。', '浏览,查看新闻,查看活动', 1),
('游客指南', '如何提交问题反馈？', '点击导航栏"问题反馈"，填写问题标题和详细内容后提交。管理员审核后会更新处理状态。', '问题反馈,提交问题,反馈问题', 1),
('游客指南', '如何报名参加活动？', '首先需要登录系统（联系管理员创建账号），然后访问活动列表，选择活动后点击"报名"按钮。', '报名活动,参加活动', 1),

-- ==================== 成员-个人信息 ====================
('成员-个人信息', '如何修改个人资料？', '登录后点击右上角头像进入"个人中心"，点击"编辑资料"按钮进行修改，保存后生效。', '修改资料,编辑资料,个人信息', 1),
('成员-个人信息', '如何修改登录密码？', '登录后点击右上角头像进入"个人中心"，点击"修改密码"，输入原密码和新密码后确认。', '修改密码,更改密码', 1),
('成员-个人信息', '个人中心有哪些功能？', '个人中心提供：个人信息查看/编辑、项目管理、活动管理、奖项管理、简历管理、学习记录查看等功能入口。', '个人中心,我的', 1),

-- ==================== 成员-活动 ====================
-- 注意：活动申请和查看功能已通过快捷按钮和ACTION实现，不再需要FAQ引导
('成员-活动', '如何报名参加活动？', '点击顶部导航"活动申请"，或直接点击"申请活动"按钮，系统会引导您完成报名。', '如何报名参加活动', 1),
('成员-活动', '如何查看我报名的活动？', '登录后进入"个人中心"，点击"我的活动"查看所有报名记录和处理状态。', '我的活动,查看报名,已报名的活动', 1),
('成员-活动', '如何取消已报名的活动？', '在"我的活动"页面，找到要取消的活动，点击"取消报名"按钮。需在报名截止前操作。', '取消报名,退出活动', 1),

-- ==================== 成员-项目 ====================
('成员-项目', '如何创建新项目？', '进入"我的项目"页面，点击"创建项目"按钮，填写项目名称、描述、团队成员等信息后提交，等待管理员审核。', '创建项目,新建项目,项目创建', 1),
('成员-项目', '如何查看我的项目申请记录？', '登录后进入"个人中心"，点击"我的项目"查看所有项目及其审核状态。', '我的项目,项目记录', 1),
('成员-项目', '项目申请被拒绝后可以重新申请吗？', '可以，修改项目信息后可以重新提交申请。', '项目被拒,重新申请', 1),

-- ==================== 成员-奖项 ====================
('成员-奖项', '如何提交奖项申请？', '进入"我的奖项"页面，点击"提交奖项"按钮，填写奖项名称、获奖时间等信息，上传证书图片，提交后等待审核。', '提交奖项,申报奖项,奖项申报', 1),
('成员-奖项', '如何查看我的获奖记录？', '登录后进入"个人中心"，点击"我的奖项"查看所有获奖记录及审核状态。', '我的奖项,获奖记录', 1),
('成员-奖项', '奖项申请被拒绝后怎么办？', '可以在"我的奖项"页面重新编辑信息后再次提交申请。', '奖项被拒,重新申请', 1),

-- ==================== 成员-学习 ====================
('成员-学习', '如何开始学习签到？', '点击导航"学习中心"，在规定时间（6:00-22:00）内点击"开始学习"按钮进行签到。', '开始学习,签到,学习签到', 1),
('成员-学习', '学习时间有什么规则？', '学习时间为6:00-22:00。18:00前签到为正常，18:00-19:00为迟到，19:00后为严重迟到。22:00系统自动结束学习。', '学习规则,签到规则', 1),
('成员-学习', '如何签退结束学习？', '在学习中心点击"签退"按钮结束学习。21:30前签退为正常早退，21:30后为正常签退。', '签退,结束学习', 1),
('成员-学习', '忘记了签到怎么办？', '请在下次学习时注意签到时间，系统会自动记录。目前不支持补签。', '忘记签到,补签', 1),

-- ==================== 成员-问题反馈 ====================
('成员-问题反馈', '如何提交问题反馈？', '点击导航"问题反馈"，填写问题标题和详细描述后提交。管理员处理后可在列表查看状态。', '提交反馈,问题反馈,提交问题', 1),
('成员-问题反馈', '如何查看我提交的问题？', '在问题反馈页面可以看到所有历史反馈记录及处理状态（待确认/属实/不属实）。', '查看反馈,我的反馈', 1),

-- ==================== 管理员-新闻管理 ====================
('管理员-新闻管理', '如何发布新闻？', '点击"新闻管理"，点击"发布新闻"按钮，选择新闻类型（通知/获奖/活动），填写标题和内容，设置发布状态后保存。', '发布新闻,创建新闻', 1),
('管理员-新闻管理', '如何修改已发布的新闻？', '在新闻列表找到目标新闻，点击"编辑"按钮修改内容后保存。', '修改新闻,编辑新闻', 1),
('管理员-新闻管理', '如何删除新闻？', '在新闻列表找到目标新闻，点击"删除"按钮确认删除。注意：已关联活动的新闻无法删除。', '删除新闻', 1),

-- ==================== 管理员-活动管理 ====================
('管理员-活动管理', '如何创建新活动？', '点击"活动管理"，点击"创建活动"，填写活动名称、时间、地点，设置报名起止时间后保存。', '创建活动,新建活动', 1),
('管理员-活动管理', '如何查看活动报名情况？', '在活动列表点击"报名管理"按钮，可查看所有报名成员及审核状态。', '查看报名,报名情况', 1),
('管理员-活动管理', '如何删除活动？', '在活动列表找到目标活动，点击"删除"按钮。注意：进行中或已结束的活动无法删除。', '删除活动', 1),

-- ==================== 管理员-项目管理 ====================
('管理员-项目管理', '如何审核项目申请？', '在项目管理页面查看"待审核"状态的项目，点击"批准"通过或"编辑"查看详情后处理。', '审核项目,审批项目', 1),
('管理员-项目管理', '项目状态有哪几种？', '项目状态分为：待审核（黄色）、已通过（绿色）、已拒绝（红色）三种。', '项目状态', 1),

-- ==================== 管理员-奖项管理 ====================
('管理员-奖项管理', '如何审核奖项申请？', '在奖项管理页面查看待审核申请，点击"通过"或"拒绝"按钮处理。', '审核奖项,审批奖项', 1),
('管理员-奖项管理', '奖项状态有哪几种？', '奖项状态分为：待审核、已通过、已拒绝三种。', '奖项状态', 1),

-- ==================== 管理员-成员管理 ====================
('管理员-成员管理', '如何重置成员密码？', '在成员管理页面找到目标成员，点击"重置密码"按钮。系统会将密码重置为默认密码123456。', '重置密码,忘记密码', 1),

-- ==================== 管理员-招新管理 ====================
('管理员-招新管理', '如何处理招新报名？', '点击"招新管理"，查看待处理申请，点击"同意"为申请者创建账号（学号即用户名，默认密码123456），或点击"拒绝"驳回。', '处理报名,审核报名', 1),

-- ==================== 管理员-学习管理 ====================
('管理员-学习管理', '如何查看学习记录？', '点击"学习管理"，可按日期范围和成员筛选查看学习时段记录，包括开始时间、结束时间、学习时长等。', '查看学习,学习记录', 1),
('管理员-学习管理', '可以统计学习数据吗？', '可以，学习管理页面顶部显示总学习次数、已完成次数、总时长、平均时长等统计信息。', '学习统计', 1),

-- ==================== 管理员-问题管理 ====================
('管理员-问题管理', '如何处理问题反馈？', '点击"问题管理"，以三个标签页查看（属实/不属实/待确认），点击"查看"可更新分类、状态和处理备注。', '处理问题,审核问题', 1),
('管理员-问题管理', '如何删除问题反馈？', '在问题列表点击"删除"按钮，可移除不再需要的问题记录。', '删除问题', 1),

-- ==================== 账号通用 ====================
('账号问题', '忘记密码怎么办？', '联系管理员重置密码，重置后默认密码为123456，首次登录后请立即修改。', '忘记密码,密码找回', 1),
('账号问题', '默认登录密码是什么？', '新账号默认密码为123456，登录后请立即修改密码。', '默认密码', 1),
('账号问题', '账户被锁定怎么办？', '联系管理员解锁账户。', '账户锁定,账号锁定,解锁', 1),

-- ==================== 学习规则 ====================
('学习规则', '学习时间段是什么？', '系统学习时间为每日6:00-22:00。18:00系统会强制签退（如正在学习中）。', '学习时间,时段', 1),
('学习规则', '签到状态如何判定？', '6:00-18:00签到为正常；18:00-19:00为迟到；19:00之后为严重迟到。', '签到状态', 1),
('学习规则', '签退状态如何判定？', '21:30前签退为早退；21:30后签退为正常；22:00系统自动结束学习。', '签退状态', 1);

-- ============================================
-- 脚本执行完成
-- ============================================
-- 使用说明：
-- 1. 确保您使用的是MySQL 5.7+或MySQL 8.0+
-- 2. 执行此脚本前请备份数据库
-- 3. 执行：source path/to/ai_assistant.sql
-- 或：mysql -u root -p software_group < ai_assistant.sql
-- ============================================