-- ============================================================================
-- 简历模块数据库设计
-- 文件名: resume_database.sql
-- 创建日期: 2026-03-12
-- 作者: AI开发助手
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 简历主表 (resumes)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS resumes (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '简历ID',
    user_id INT NOT NULL COMMENT '用户ID（关联users表）',
    resume_name VARCHAR(100) DEFAULT '我的简历' COMMENT '简历名称',
    template_style VARCHAR(50) DEFAULT 'default' COMMENT '简历模板风格：default/minimal/academic/creative',
    summary TEXT COMMENT '个人简介/自我评价',
    career_objective VARCHAR(500) COMMENT '求职意向',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '联系邮箱',
    wechat VARCHAR(50) COMMENT '微信号',
    github_url VARCHAR(200) COMMENT 'GitHub主页',
    blog_url VARCHAR(200) COMMENT '技术博客地址',
    photo_url VARCHAR(500) COMMENT '个人照片URL',
    is_default TINYINT DEFAULT 0 COMMENT '是否为默认简历：0-否，1-是',
    status TINYINT DEFAULT 1 COMMENT '简历状态：0-草稿，1-已发布，2-已隐藏',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '软删除标志：0-正常，1-已删除',

    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_is_default (is_default),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历主表';

-- ----------------------------------------------------------------------------
-- 2. 教育经历表 (resume_educations)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS resume_educations (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '教育经历ID',
    resume_id INT NOT NULL COMMENT '简历ID（关联resumes表）',
    school_name VARCHAR(200) NOT NULL COMMENT '学校名称',
    major VARCHAR(100) COMMENT '专业名称',
    degree VARCHAR(50) COMMENT '学历/学位：高中/大专/本科/硕士/博士',
    start_date DATE COMMENT '入学时间',
    end_date DATE COMMENT '毕业时间',
    is_current TINYINT DEFAULT 0 COMMENT '是否在读：0-已毕业，1-在读',
    description TEXT COMMENT '在校经历描述（主修课程、GPA、获奖情况等）',
    display_order INT DEFAULT 0 COMMENT '显示顺序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_resume_id (resume_id),
    INDEX idx_display_order (display_order),
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历-教育经历表';

-- ----------------------------------------------------------------------------
-- 3. 技能特长表 (resume_skills)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS resume_skills (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '技能ID',
    resume_id INT NOT NULL COMMENT '简历ID（关联resumes表）',
    skill_name VARCHAR(100) NOT NULL COMMENT '技能名称',
    proficiency VARCHAR(20) DEFAULT 'intermediate' COMMENT '熟练程度：beginner/elementary/intermediate/advanced/expert',
    proficiency_score INT COMMENT '熟练度分数：1-100',
    category VARCHAR(50) COMMENT '技能分类：编程语言/开发框架/数据库/工具/语言/其他',
    description VARCHAR(500) COMMENT '技能描述',
    display_order INT DEFAULT 0 COMMENT '显示顺序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_resume_id (resume_id),
    INDEX idx_category (category),
    INDEX idx_display_order (display_order),
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历-技能特长表';

-- ----------------------------------------------------------------------------
-- 4. 项目经历表 (resume_projects)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS resume_projects (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '项目ID',
    resume_id INT NOT NULL COMMENT '简历ID（关联resumes表）',
    project_name VARCHAR(200) NOT NULL COMMENT '项目名称',
    role VARCHAR(100) COMMENT '担任角色：项目负责人/核心开发/参与开发等',
    team_size INT COMMENT '团队规模',
    start_date DATE COMMENT '开始时间',
    end_date DATE COMMENT '结束时间',
    is_current TINYINT DEFAULT 0 COMMENT '是否进行中：0-已完成，1-进行中',
    description TEXT COMMENT '项目描述',
    responsibilities TEXT COMMENT '个人职责',
    technologies VARCHAR(500) COMMENT '使用技术（逗号分隔）',
    project_url VARCHAR(500) COMMENT '项目链接/演示地址',
    achievements TEXT COMMENT '项目成果',
    display_order INT DEFAULT 0 COMMENT '显示顺序',
    is_from_system TINYINT DEFAULT 0 COMMENT '是否来自系统项目模块：0-手动添加，1-关联系统项目',
    system_project_id INT COMMENT '关联的系统项目ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_resume_id (resume_id),
    INDEX idx_is_current (is_current),
    INDEX idx_display_order (display_order),
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历-项目经历表';

-- ----------------------------------------------------------------------------
-- 5. 获奖情况关联表 (resume_awards)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS resume_awards (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    resume_id INT NOT NULL COMMENT '简历ID（关联resumes表）',
    award_id INT COMMENT '关联的系统奖项ID（awards表），手动添加可为空',
    award_name VARCHAR(200) NOT NULL COMMENT '奖项名称',
    competition_name VARCHAR(200) COMMENT '比赛/活动名称',
    award_level VARCHAR(50) COMMENT '奖项等级：特等奖/一等奖/二等奖/三等奖/优秀奖等',
    award_date DATE COMMENT '获奖时间',
    award_org VARCHAR(200) COMMENT '颁奖机构',
    description TEXT COMMENT '获奖描述',
    is_from_system TINYINT DEFAULT 0 COMMENT '是否来自系统奖项：0-手动添加，1-关联系统奖项',
    display_order INT DEFAULT 0 COMMENT '显示顺序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_resume_id (resume_id),
    INDEX idx_award_id (award_id),
    INDEX idx_display_order (display_order),
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    FOREIGN KEY (award_id) REFERENCES award(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历-获奖情况关联表';

-- ----------------------------------------------------------------------------
-- 6. 简历投递记录表 (resume_submissions) - 可选扩展
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS resume_submissions (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '投递记录ID',
    resume_id INT NOT NULL COMMENT '简历ID',
    user_id INT NOT NULL COMMENT '用户ID',
    target_type VARCHAR(50) COMMENT '投递对象类型：activity-活动/项目，recruit-招新，job-职位',
    target_id INT COMMENT '投递对象ID',
    target_name VARCHAR(200) COMMENT '投递对象名称',
    status TINYINT DEFAULT 0 COMMENT '投递状态：0-已投递，1-已查看，2-已通过，3-未通过',
    submit_message TEXT COMMENT '投递附言',
    feedback TEXT COMMENT '反馈信息',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
    viewed_at TIMESTAMP NULL COMMENT '查看时间',
    replied_at TIMESTAMP NULL COMMENT '回复时间',

    INDEX idx_resume_id (resume_id),
    INDEX idx_user_id (user_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_status (status),
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历投递记录表';

-- ----------------------------------------------------------------------------
-- 初始化数据（可选）
-- ----------------------------------------------------------------------------

-- 为现有用户创建默认简历（如果需要）
-- INSERT INTO resumes (user_id, resume_name, is_default, status)
-- SELECT id, '我的简历', 1, 1 FROM users WHERE deleted = 0;

-- ============================================================================
-- 文档结束
-- ============================================================================
