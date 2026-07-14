-- H2 测试库建表脚本 - MySQL兼容模式
-- 从 sql/software_group.sql 自动转换，列名/类型与生产严格对齐
-- 通过 JDBC URL 的 NON_KEYWORDS 参数解决 USER/VALUE/YEAR/ROLE/KEY/INDEX/TYPE 等保留字问题
-- 重新生成：运行 python bin/convert_mysql_to_h2.py

CREATE TABLE IF NOT EXISTS activity (
    id INT NOT NULL AUTO_INCREMENT,
    name varchar(100) NOT NULL,
    description text,
    activity_type varchar(50) DEFAULT NULL,
    activity_start_time TIMESTAMP DEFAULT NULL,
    activity_end_time TIMESTAMP DEFAULT NULL,
    location varchar(100) DEFAULT NULL,
    organizers varchar(255) DEFAULT NULL,
    contact_info varchar(100) DEFAULT NULL,
    registration_start_time TIMESTAMP DEFAULT NULL,
    registration_end_time TIMESTAMP DEFAULT NULL,
    max_participants INT DEFAULT NULL,
    status VARCHAR(50) DEFAULT 'upcoming',
    approval_status VARCHAR(50) DEFAULT 'approved',
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT '0',
    creator_id INT DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS activity_group (
    id INT NOT NULL AUTO_INCREMENT,
    activity_id INT DEFAULT NULL,
    group_name varchar(100) NOT NULL,
    group_owner_id INT NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_muted TINYINT DEFAULT '0',
    muted_until TIMESTAMP DEFAULT NULL,
    mute_reason varchar(255) DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS activity_participant (
    activity_id INT NOT NULL,
    user_id INT NOT NULL,
    status VARCHAR(50) DEFAULT 'pending',
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NULL DEFAULT NULL,
    notes text,
    deleted TINYINT DEFAULT '0',
    PRIMARY KEY (activity_id,user_id)
);

CREATE TABLE IF NOT EXISTS admin_profile (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    title varchar(50) DEFAULT NULL,
    department varchar(100) DEFAULT NULL,
    education varchar(50) DEFAULT NULL,
    research_area varchar(200) DEFAULT NULL,
    bio text,
    status TINYINT NOT NULL DEFAULT '1',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    avatar_file_id INT DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS ai_conversation (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT DEFAULT '0',
    session_id varchar(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ai_conversation_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id varchar(64) DEFAULT NULL,
    user_id INT DEFAULT NULL,
    user_role varchar(20) DEFAULT NULL,
    question text NOT NULL,
    ai_answer text,
    source varchar(20) DEFAULT NULL,
    reference_id INT DEFAULT NULL,
    rating TINYINT DEFAULT NULL,
    is_validated TINYINT DEFAULT '0',
    validated_by INT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ai_faq_knowledge (
    id INT NOT NULL AUTO_INCREMENT,
    category varchar(50) DEFAULT NULL,
    question varchar(500) NOT NULL,
    answer text NOT NULL,
    keywords varchar(255) DEFAULT NULL,
    target_role varchar(20) DEFAULT 'ALL',
    priority INT DEFAULT '1',
    view_count INT DEFAULT '0',
    useful_count INT DEFAULT '0',
    status TINYINT DEFAULT '1',
    verified TINYINT DEFAULT '0',
    verified_at TIMESTAMP DEFAULT NULL,
    verified_by INT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ai_faq_statistics (
    id INT NOT NULL AUTO_INCREMENT,
    question_hash varchar(64) DEFAULT NULL,
    normalized_question varchar(500) DEFAULT NULL,
    query_count INT DEFAULT '1',
    avg_rating decimal(3,2) DEFAULT NULL,
    last_query_at TIMESTAMP DEFAULT NULL,
    suggested_faq TINYINT DEFAULT '0',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY question_hash (question_hash)
);

CREATE TABLE IF NOT EXISTS ai_knowledge_base (
    id INT NOT NULL AUTO_INCREMENT,
    category varchar(50) DEFAULT NULL,
    question varchar(500) NOT NULL,
    answer text NOT NULL,
    keywords varchar(255) DEFAULT NULL,
    status TINYINT DEFAULT '1',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ai_message (
    id INT NOT NULL AUTO_INCREMENT,
    conversation_id INT NOT NULL,
    role varchar(20) NOT NULL,
    content text NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS attendance (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    attendance_date date NOT NULL,
    check_in_time TIMESTAMP DEFAULT NULL,
    check_out_time TIMESTAMP DEFAULT NULL,
    check_in_status varchar(20) DEFAULT 'NONE',
    check_out_status varchar(20) DEFAULT 'NONE',
    work_duration INT DEFAULT NULL,
    location varchar(200) DEFAULT NULL,
    device_info varchar(200) DEFAULT NULL,
    remark varchar(500) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_date (user_id,attendance_date)
);

CREATE TABLE IF NOT EXISTS attendance_config (
    id INT NOT NULL AUTO_INCREMENT,
    config_key varchar(50) NOT NULL,
    config_value text,
    description varchar(200) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
);

CREATE TABLE IF NOT EXISTS attendance_makeup (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    date date NOT NULL,
    type varchar(20) NOT NULL,
    reason varchar(500) NOT NULL,
    status varchar(20) DEFAULT 'pending',
    approved_by INT DEFAULT NULL,
    approved_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS award (
    id INT NOT NULL AUTO_INCREMENT,
    name varchar(100) NOT NULL,
    competition varchar(100) NOT NULL,
    year INT NOT NULL,
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    competition_time date DEFAULT NULL,
    competition_location varchar(200) DEFAULT NULL,
    competition_session varchar(100) DEFAULT NULL,
    award_type INT DEFAULT NULL,
    award_category INT DEFAULT NULL,
    team_name varchar(100) DEFAULT NULL,
    award_status varchar(50) DEFAULT 'PENDING',
    created_by INT DEFAULT NULL,
    approved_by INT DEFAULT NULL,
    approved_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    award_level INT DEFAULT NULL,
    competition_level INT DEFAULT NULL,
    deleted TINYINT DEFAULT '0',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS award_image (
    id INT NOT NULL AUTO_INCREMENT,
    award_id INT NOT NULL,
    member_id INT DEFAULT NULL,
    is_main TINYINT NOT NULL DEFAULT '0',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    file_storage_id INT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS award_member (
    award_id INT NOT NULL,
    member_id INT NOT NULL,
    PRIMARY KEY (award_id,member_id)
);

CREATE TABLE IF NOT EXISTS dictionary (
    id INT NOT NULL AUTO_INCREMENT,
    code varchar(50) NOT NULL,
    name varchar(100) NOT NULL,
    type varchar(50) NOT NULL,
    sort_order INT DEFAULT '0',
    status TINYINT DEFAULT '1',
    description varchar(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code_type (code,type)
);

CREATE TABLE IF NOT EXISTS file_storage (
    id INT NOT NULL AUTO_INCREMENT,
    create_by INT NOT NULL,
    original_name varchar(255) NOT NULL,
    stored_name varchar(255) NOT NULL,
    file_path varchar(500) NOT NULL,
    file_type varchar(200) DEFAULT NULL,
    file_size BIGINT DEFAULT NULL,
    category varchar(50) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status TINYINT DEFAULT '1',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS group_member (
    id INT NOT NULL AUTO_INCREMENT,
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    role VARCHAR(50) DEFAULT 'MEMBER',
    joined_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    muted TINYINT DEFAULT '0',
    muted_until TIMESTAMP DEFAULT NULL,
    last_read_at TIMESTAMP DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_user (group_id,user_id)
);

CREATE TABLE IF NOT EXISTS group_message (
    id INT NOT NULL AUTO_INCREMENT,
    group_id INT NOT NULL,
    sender_id INT NOT NULL,
    content text NOT NULL,
    sent_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status varchar(20) DEFAULT 'normal',
    message_type varchar(20) DEFAULT 'TEXT',
    file_id INT DEFAULT NULL,
    file_name varchar(255) DEFAULT NULL,
    file_size BIGINT DEFAULT NULL,
    file_type varchar(100) DEFAULT NULL,
    file_path varchar(500) DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS member_profile (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    student_id varchar(20) NOT NULL,
    major varchar(100) NOT NULL,
    grade varchar(20) NOT NULL,
    birthday date DEFAULT NULL,
    gender VARCHAR(50) DEFAULT 'other',
    introduction text,
    skills text,
    github varchar(100) DEFAULT NULL,
    blog varchar(100) DEFAULT NULL,
    updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    avatar_file_id INT DEFAULT NULL,
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS news (
    id INT NOT NULL AUTO_INCREMENT,
    title varchar(200) NOT NULL,
    type VARCHAR(50) NOT NULL,
    content_path varchar(255) NOT NULL,
    summary varchar(500) DEFAULT NULL,
    author_id INT DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT '1',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activity_id INT DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS operation_log (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT DEFAULT NULL,
    username varchar(50) DEFAULT NULL,
    operation varchar(100) NOT NULL,
    module varchar(50) DEFAULT NULL,
    description text,
    ip_address varchar(50) DEFAULT NULL,
    user_agent varchar(500) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS problem_management (
    id INT NOT NULL AUTO_INCREMENT,
    report_id INT NOT NULL,
    category VARCHAR(50) NOT NULL DEFAULT 'UNVERIFIED',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    admin_comment text,
    handled_by INT DEFAULT NULL,
    handled_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS problem_report (
    id INT NOT NULL AUTO_INCREMENT,
    title varchar(200) NOT NULL,
    content text NOT NULL,
    reporter_name varchar(100) DEFAULT NULL,
    reporter_contact varchar(100) DEFAULT NULL,
    reporter_type VARCHAR(50) NOT NULL DEFAULT 'GUEST',
    user_id INT DEFAULT NULL,
    category VARCHAR(50) NOT NULL DEFAULT 'UNVERIFIED',
    status VARCHAR(50) DEFAULT 'PENDING',
    admin_comment text,
    handled_by INT DEFAULT NULL,
    handled_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS project (
    id INT NOT NULL AUTO_INCREMENT,
    name varchar(100) NOT NULL,
    description text,
    category varchar(50) DEFAULT NULL,
    year INT NOT NULL,
    expected_start_date date DEFAULT NULL,
    expected_end_date date DEFAULT NULL,
    actual_start_date date DEFAULT NULL,
    actual_end_date date DEFAULT NULL,
    status VARCHAR(50) DEFAULT 'pending',
    approved_by INT DEFAULT NULL,
    approved_at TIMESTAMP DEFAULT NULL,
    admin_id INT DEFAULT NULL,
    leader_id INT NOT NULL,
    budget decimal(10,2) DEFAULT NULL,
    repo_url varchar(500) DEFAULT NULL,
    doc_url varchar(500) DEFAULT NULL,
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT '0',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS project_file (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    file_id INT NOT NULL,
    description varchar(200) DEFAULT NULL,
    file_type varchar(50) DEFAULT NULL,
    sort_order INT DEFAULT '0',
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS project_history (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    operation_type varchar(50) NOT NULL,
    operator_id INT NOT NULL,
    operator_name varchar(100) DEFAULT NULL,
    description text,
    old_value text,
    new_value text,
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS project_image (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    file_id INT NOT NULL,
    description varchar(200) DEFAULT NULL,
    sort_order INT DEFAULT '0',
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS project_label (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    label_code varchar(50) NOT NULL,
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_label (project_id,label_code)
);

CREATE TABLE IF NOT EXISTS project_member (
    project_id INT NOT NULL,
    user_id INT NOT NULL,
    role varchar(50) DEFAULT NULL,
    PRIMARY KEY (project_id,user_id)
);

CREATE TABLE IF NOT EXISTS project_member_application (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    user_id INT NOT NULL,
    status varchar(20) DEFAULT 'PENDING',
    applied_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    handled_at timestamp NULL DEFAULT NULL,
    handled_by INT DEFAULT NULL,
    reason text,
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_user (project_id,user_id,status)
);

CREATE TABLE IF NOT EXISTS project_plan (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    title varchar(200) NOT NULL,
    description text,
    start_date date NOT NULL,
    end_date date NOT NULL,
    order_index INT DEFAULT '0',
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS project_progress (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    plan_id INT DEFAULT NULL,
    title varchar(200) NOT NULL,
    description text,
    completion_rate INT DEFAULT '0',
    created_by INT NOT NULL,
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS recruit_application (
    id INT NOT NULL AUTO_INCREMENT,
    name varchar(50) NOT NULL,
    student_id varchar(50) DEFAULT NULL,
    major varchar(100) DEFAULT NULL,
    grade varchar(20) DEFAULT NULL,
    phone varchar(20) NOT NULL,
    email varchar(100) DEFAULT NULL,
    reason text,
    status TINYINT NOT NULL DEFAULT '1',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS resume_awards (
    id INT NOT NULL AUTO_INCREMENT,
    resume_id INT NOT NULL,
    award_id INT DEFAULT NULL,
    award_name varchar(200) NOT NULL,
    competition_name varchar(200) DEFAULT NULL,
    award_level varchar(50) DEFAULT NULL,
    award_date date DEFAULT NULL,
    award_org varchar(200) DEFAULT NULL,
    description text,
    is_from_system TINYINT DEFAULT '0',
    display_order INT DEFAULT '0',
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS resume_educations (
    id INT NOT NULL AUTO_INCREMENT,
    resume_id INT NOT NULL,
    school_name varchar(200) NOT NULL,
    major varchar(100) DEFAULT NULL,
    degree varchar(50) DEFAULT NULL,
    start_date date DEFAULT NULL,
    end_date date DEFAULT NULL,
    is_current TINYINT DEFAULT '0',
    description text,
    display_order INT DEFAULT '0',
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS resume_projects (
    id INT NOT NULL AUTO_INCREMENT,
    resume_id INT NOT NULL,
    project_name varchar(200) NOT NULL,
    role varchar(100) DEFAULT NULL,
    team_size INT DEFAULT NULL,
    start_date date DEFAULT NULL,
    end_date date DEFAULT NULL,
    is_current TINYINT DEFAULT '0',
    description text,
    responsibilities text,
    technologies varchar(500) DEFAULT NULL,
    project_url varchar(500) DEFAULT NULL,
    achievements text,
    display_order INT DEFAULT '0',
    is_from_system TINYINT DEFAULT '0',
    system_project_id INT DEFAULT NULL,
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS resume_skills (
    id INT NOT NULL AUTO_INCREMENT,
    resume_id INT NOT NULL,
    skill_name varchar(100) NOT NULL,
    proficiency varchar(20) DEFAULT 'intermediate',
    proficiency_score INT DEFAULT NULL,
    category varchar(50) DEFAULT NULL,
    description varchar(500) DEFAULT NULL,
    display_order INT DEFAULT '0',
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS resume_submissions (
    id INT NOT NULL AUTO_INCREMENT,
    resume_id INT NOT NULL,
    user_id INT NOT NULL,
    target_type varchar(50) DEFAULT NULL,
    target_id INT DEFAULT NULL,
    target_name varchar(200) DEFAULT NULL,
    status TINYINT DEFAULT '0',
    submit_message text,
    feedback text,
    submitted_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    viewed_at timestamp NULL DEFAULT NULL,
    replied_at timestamp NULL DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS resumes (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    resume_name varchar(100) DEFAULT '我的简历',
    template_style varchar(50) DEFAULT 'default',
    summary text,
    career_objective varchar(500) DEFAULT NULL,
    phone varchar(20) DEFAULT NULL,
    email varchar(100) DEFAULT NULL,
    wechat varchar(50) DEFAULT NULL,
    github_url varchar(200) DEFAULT NULL,
    blog_url varchar(200) DEFAULT NULL,
    photo_url varchar(500) DEFAULT NULL,
    is_default TINYINT DEFAULT '0',
    status TINYINT DEFAULT '1',
    view_count INT DEFAULT '0',
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT '0',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS study_session (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    session_date date DEFAULT NULL,
    check_in_time TIMESTAMP DEFAULT NULL,
    check_out_time TIMESTAMP DEFAULT NULL,
    duration INT DEFAULT NULL,
    status varchar(20) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user (
    id INT NOT NULL AUTO_INCREMENT,
    username varchar(50) NOT NULL,
    password varchar(100) NOT NULL,
    name varchar(50) NOT NULL DEFAULT 'Unknown',
    email varchar(100) NOT NULL,
    phone varchar(20) DEFAULT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    status INT DEFAULT '1',
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    must_change_password TINYINT DEFAULT '0',
    PRIMARY KEY (id),
    UNIQUE KEY username (username),
    UNIQUE KEY email (email)
);

CREATE TABLE IF NOT EXISTS user_group (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    group_id INT NOT NULL,
    joined_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_group (user_id,group_id)
);
