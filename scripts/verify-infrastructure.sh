#!/bin/bash
# ============================================================
# 阶段三基础设施验证脚本
# 对应计划文档：阶段三_微服务拆分实施计划 - 3.1 基础设施搭建
# 使用方式：./scripts/verify-infrastructure.sh
# ============================================================

set -e

echo "========================================"
echo "阶段三基础设施验证脚本"
echo "========================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 计数器
PASSED=0
FAILED=0

# 检查函数
check_service() {
    local name=$1
    local url=$2
    local expected_code=${3:-200}

    echo -n "检查 $name ... "

    response=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000")

    if [ "$response" = "$expected_code" ]; then
        echo -e "${GREEN}✓ 通过${NC} (HTTP $response)"
        ((PASSED++))
    else
        echo -e "${RED}✗ 失败${NC} (期望 $expected_code, 实际 $response)"
        ((FAILED++))
    fi
}

# 等待服务启动（带超时）
wait_for_service() {
    local name=$1
    local url=$2
    local max_wait=${3:-60}
    local wait_interval=${4:-5}

    echo -n "等待 $name 启动 (最多 ${max_wait}s) ... "

    elapsed=0
    while [ $elapsed -lt $max_wait ]; do
        response=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000")
        if [ "$response" = "200" ]; then
            echo -e "${GREEN}✓ 已就绪${NC}"
            return 0
        fi
        sleep $wait_interval
        elapsed=$((elapsed + wait_interval))
    done

    echo -e "${RED}✗ 超时${NC}"
    return 1
}

echo "========================================"
echo "1. 等待基础服务就绪"
echo "========================================"

# 等待 MySQL
wait_for_service "MySQL" "http://localhost:3307" 30

# 等待 Redis
wait_for_service "Redis" "http://localhost:6379" 20

# 等待 RabbitMQ
wait_for_service "RabbitMQ" "http://localhost:15672" 30

echo ""
echo "========================================"
echo "2. 检查阶段三基础设施服务"
echo "========================================"

# 等待 Nacos（启动较慢，可能需要 60 秒以上）
wait_for_service "Nacos" "http://localhost:8848/nacos/" 90

# 检查 Sentinel
check_service "Sentinel Dashboard" "http://localhost:8858/" 200

# 检查 MinIO
check_service "MinIO Health" "http://localhost:9000/minio/health/live" 200

echo ""
echo "========================================"
echo "3. 检查 MinIO 存储桶"
echo "========================================"

# 检查 MinIO 存储桶是否存在，不存在则创建
echo -n "检查 software-group 存储桶 ... "
buckets=$(curl -s -u minioadmin:minioadmin123 "http://localhost:9000/api/v1/buckets" 2>/dev/null || echo "[]")
if echo "$buckets" | grep -q "software-group"; then
    echo -e "${GREEN}✓ 已存在${NC}"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠ 不存在（将在首次上传时自动创建）${NC}"
fi

echo ""
echo "========================================"
echo "4. 检查 Docker 容器状态"
echo "========================================"

containers=("sg_mysql" "sg_redis" "sg_rabbitmq" "sg_nacos" "sg_sentinel" "sg_minio" "sg_app" "sg_nginx")

for container in "${containers[@]}"; do
    echo -n "检查容器 $container ... "
    status=$(docker ps --filter "name=$container" --format "{{.Status}}" 2>/dev/null || echo "not found")
    if [ -n "$status" ] && [ "$status" != "not found" ]; then
        echo -e "${GREEN}✓ 运行中${NC} ($status)"
        ((PASSED++))
    else
        echo -e "${RED}✗ 未运行${NC}"
        ((FAILED++))
    fi
done

echo ""
echo "========================================"
echo "5. 检查 Nacos 服务注册"
echo "========================================"

# 检查 Nacos 是否有服务注册（需要等待服务启动后才有）
echo -n "检查 Nacos 服务列表 ... "
nacos_response=$(curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=" 2>/dev/null || echo "")
if [ -n "$nacos_response" ]; then
    echo -e "${GREEN}✓ Nacos 可访问${NC}"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠ 暂无注册服务（正常，新服务尚未部署）${NC}"
fi

echo ""
echo "========================================"
echo "6. 端口占用检查"
echo "========================================"

ports=("3307:MYSQL" "6379:REDIS" "5672:RABBITMQ" "15672:RABBITMQ_MGMT" "8848:NACOS" "9848:NACOS_GRPC" "8858:SENTINEL" "9000:MINIO" "9001:MINIO_CONSOLE" "8080:APP" "80:NGINX")

for port_info in "${ports[@]}"; do
    port="${port_info%%:*}"
    name="${port_info##*:}"

    if netstat -an 2>/dev/null | grep -q "LISTENING.*:$port " || ss -tlnp 2>/dev/null | grep -q ":$port "; then
        echo -e "${GREEN}✓${NC} 端口 $port ($name) - 已监听"
        ((PASSED++))
    else
        echo -e "${YELLOW}⚠${NC} 端口 $port ($name) - 未监听"
    fi
done

echo ""
echo "========================================"
echo "7. 数据库用户权限验证"
echo "========================================"

echo "执行 sql/db_users.sql 创建微服务专用用户..."
echo -e "${YELLOW}请手动执行以下命令验证数据库用户权限：${NC}"
echo ""
echo "  docker exec -it sg_mysql mysql -uroot -proot123 software_group"
echo "  SOURCE /docker-entrypoint-initdb.d/db_users.sql;"
echo "  SELECT user, host FROM mysql.user WHERE user LIKE '%_svc%';"
echo "  SHOW GRANTS FOR 'file_svc'@'%';"
echo ""

echo ""
echo "========================================"
echo "验证结果汇总"
echo "========================================"
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ 所有检查通过！基础设施搭建成功！${NC}"
    echo ""
    echo "下一步："
    echo "  1. 执行 sql/db_users.sql 创建微服务数据库用户"
    echo "  2. 继续 3.2 父 POM 和基础框架搭建"
    exit 0
else
    echo -e "${RED}✗ 部分检查失败，请排查问题${NC}"
    echo ""
    echo "常见问题排查："
    echo "  1. Nacos 启动慢 - 等待 90 秒后再试"
    echo "  2. MinIO 连接失败 - 检查 minioadmin 凭证"
    echo "  3. 容器未运行 - docker-compose ps 查看状态"
    exit 1
fi
