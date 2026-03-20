-- 检查 resume_awards 表结构
DESCRIBE resume_awards;

-- 检查表的外键约束
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    TABLE_SCHEMA = 'software_group'
    AND TABLE_NAME = 'resume_awards'
    AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 检查表中是否有数据
SELECT COUNT(*) as total_count FROM resume_awards;

-- 查看最近插入的5条记录（如果有）
SELECT id, resume_id, award_id, award_name, award_date, is_from_system, created_at
FROM resume_awards
ORDER BY created_at DESC
LIMIT 5;

-- 检查是否有 resume_id 为 NULL 的记录
SELECT COUNT(*) as null_resume_count FROM resume_awards WHERE resume_id IS NULL;

-- 检查 award_id 的分布情况
SELECT
    CASE
        WHEN award_id IS NULL THEN 'NULL'
        ELSE '有值'
    END as award_id_status,
    COUNT(*) as count
FROM resume_awards
GROUP BY award_id_status;
