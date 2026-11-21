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

# 容器端口不再发布到宿主机，直接等待 Docker 的容器内健康检查。
wait_for_container_health() {
    local name=$1 container=$2 max_wait=${3:-60}
    local elapsed=0 status

    echo -n "等待 $name 容器健康 (最多 ${max_wait}s) ... "
    while [ "$elapsed" -lt "$max_wait" ]; do
        status=$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$container" 2>/dev/null || true)
        if [ "$status" = healthy ]; then
            echo -e "${GREEN}✓ 已就绪${NC}"
            PASSED=$((PASSED + 1))
            return 0
        fi
        if [ "$status" = exited ] || [ "$status" = dead ]; then
            break
        fi
        sleep 5
        elapsed=$((elapsed + 5))
    done
    echo -e "${RED}✗ 未就绪${NC} (状态: ${status:-未找到})"
    FAILED=$((FAILED + 1))
    return 1
}

echo "========================================"
echo "1. 等待基础服务就绪"
echo "========================================"

wait_for_container_health "MySQL" sg_mysql 30
wait_for_container_health "Redis" sg_redis 20
wait_for_container_health "RabbitMQ" sg_rabbitmq 30

echo ""
echo "========================================"
echo "2. 检查阶段三基础设施服务"
echo "========================================"

wait_for_container_health "Nacos" sg_nacos 90
wait_for_container_health "Sentinel Dashboard" sg_sentinel 60
wait_for_container_health "MinIO" sg_minio 60
wait_for_container_health "Nginx + WAR readiness" sg_nginx 60

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
echo "5. Nacos 认证与服务注册"
echo "========================================"

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if python3 "$script_dir/verify-nacos-auth-runtime.py"; then
    PASSED=$((PASSED + 1))
else
    FAILED=$((FAILED + 1))
fi

echo ""
echo "========================================"
echo "6. Docker 宿主端口发布检查"
echo "========================================"

containers=("sg_mysql" "sg_redis" "sg_rabbitmq" "sg_nacos" "sg_sentinel" "sg_minio" "sg_app" "sg_file_service" "sg_user_service" "sg_activity_service" "sg_project_award_service" "sg_hr_service" "sg_monitor_service" "sg_content_service" "sg_ai_service" "sg_nginx")
for container in "${containers[@]}"; do
    if ! docker inspect "$container" >/dev/null 2>&1; then
        echo -e "${RED}✗${NC} $container 不存在"
        FAILED=$((FAILED + 1))
        continue
    fi
    bindings=$(docker port "$container" 2>/dev/null || true)
    if [ "$container" = sg_nginx ]; then
        if printf '%s\n' "$bindings" | grep -q '^80/tcp -> ' &&
           printf '%s\n' "$bindings" | grep -q '^443/tcp -> ' &&
           ! printf '%s\n' "$bindings" | grep -qEv '^(80|443)/tcp -> |^$'; then
            echo -e "${GREEN}✓${NC} Nginx 只发布 Web 端口 80/443"
            PASSED=$((PASSED + 1))
        else
            echo -e "${RED}✗${NC} Nginx 端口发布异常: $bindings"
            FAILED=$((FAILED + 1))
        fi
    elif [ -z "$bindings" ]; then
        echo -e "${GREEN}✓${NC} $container 未发布宿主端口"
        PASSED=$((PASSED + 1))
    else
        echo -e "${RED}✗${NC} $container 仍发布宿主端口: $bindings"
        FAILED=$((FAILED + 1))
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
    echo -e "${GREEN}✓ 基础设施、端口和 Nacos 认证检查通过${NC}"
    echo ""
    echo "下一步："
    echo "  1. 按 docs/数据库运行账号切换说明.md 核对独立数据库账号"
    echo "  2. 按 docs/OPS-02-Nacos认证切换与验收.md 核对八服务注册"
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
