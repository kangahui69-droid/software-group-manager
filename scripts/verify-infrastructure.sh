#!/bin/bash
# ============================================================
# 阶段三基础设施验证脚本
# 对应计划文档：阶段三_微服务拆分实施计划 - 3.1 基础设施搭建
# 使用方式：./scripts/verify-infrastructure.sh
# ============================================================

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
    FAILED=$((FAILED + 1))
    return 1
}

# 等待 TCP 端口就绪（MySQL/Redis 等非 HTTP 服务）
wait_for_tcp() {
    local name=$1
    local host=$2
    local port=$3
    local max_wait=${4:-60}
    local wait_interval=${5:-5}

    echo -n "等待 $name 启动 (TCP ${host}:${port}, 最多 ${max_wait}s) ... "

    elapsed=0
    while [ $elapsed -lt $max_wait ]; do
        if timeout 2 bash -c "echo > /dev/tcp/${host}/${port}" 2>/dev/null; then
            echo -e "${GREEN}✓ 已就绪${NC}"
            return 0
        fi
        sleep $wait_interval
        elapsed=$((elapsed + wait_interval))
    done

    echo -e "${RED}✗ 超时${NC}"
    FAILED=$((FAILED + 1))
    return 1
}

echo "========================================"
echo "1. 等待基础服务就绪"
echo "========================================"

# 等待 MySQL（MySQL 协议，非 HTTP -> TCP 探）
wait_for_tcp "MySQL" "localhost" "3307" 30

# 等待 Redis（Redis 协议，非 HTTP -> TCP 探）
wait_for_tcp "Redis" "localhost" "6379" 20

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

# 存储桶通过应用的上传测试验证；本脚本不读取或显示 MinIO 凭据
echo -e "${YELLOW}⚠ 存储桶检查请通过上传测试完成${NC}"

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

echo "DB-03 专用运行账号由 db-users-init 一次性任务在 Flyway 成功后创建。"
echo -e "${YELLOW}在私有 .env 中设置各服务独立密码并运行后，可验证：${NC}"
echo ""
echo "  docker compose run --rm --no-deps db-users-init"
echo "  docker exec -it sg_mysql mysql -uroot -p"
echo "  SHOW GRANTS FOR 'sg_file_svc'@'%';"
echo "  SHOW GRANTS FOR 'sg_nacos_svc'@'%';"
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
    echo "  1. 按 docs/数据库运行账号切换说明.md 配置独立密码并验证 db-users-init"
    echo "  2. 继续 3.2 父 POM 和基础框架搭建"
    exit 0
else
    echo -e "${RED}✗ 部分检查失败，请排查问题${NC}"
    echo ""
    echo "常见问题排查："
    echo "  1. Nacos 启动慢 - 等待 90 秒后再试"
    echo "  2. MinIO 连接失败 - 检查私有环境文件中的凭据"
    echo "  3. 容器未运行 - docker-compose ps 查看状态"
    exit 1
fi
