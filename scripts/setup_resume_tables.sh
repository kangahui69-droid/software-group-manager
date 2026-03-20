#!/bin/bash

# 简历模块数据库表创建脚本
# 数据库: software_group

# 设置字符编码
export LANG=en_US.UTF-8

echo "=========================================="
echo "简历模块数据库表创建工具"
echo "=========================================="
echo ""

# 数据库连接信息
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="software_group"
DB_USER="root"
DB_PASS="kang2005."

echo "数据库信息:"
echo "  主机: $DB_HOST"
echo "  端口: $DB_PORT"
echo "  数据库: $DB_NAME"
echo "  用户名: $DB_USER"
echo ""

# 检查SQL文件是否存在
SQL_FILE="$(dirname "$0")/../docs/resume_database.sql"

if [ ! -f "$SQL_FILE" ]; then
    echo "错误: SQL文件不存在!"
    echo "请确保文件路径: $SQL_FILE"
    exit 1
fi

echo "SQL文件检查通过"
echo ""

# 执行SQL脚本
echo "开始执行SQL脚本..."
echo ""

mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME < "$SQL_FILE"

if [ $? -ne 0 ]; then
    echo ""
    echo "错误: SQL执行失败!"
    echo "请检查:"
    echo "  1. MySQL服务是否运行"
    echo "  2. 数据库连接信息是否正确"
    echo "  3. 数据库 $DB_NAME 是否存在"
    exit 1
fi

echo ""
echo "=========================================="
echo "SQL脚本执行成功!"
echo "=========================================="
echo ""
echo "已创建的表:"
echo "  - resumes (简历主表)"
echo "  - resume_educations (教育经历表)"
echo "  - resume_skills (技能特长表)"
echo "  - resume_projects (项目经历表)"
echo "  - resume_awards (获奖情况表)"
echo "  - resume_submissions (简历投递记录表)"
echo ""
