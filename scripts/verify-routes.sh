#!/bin/bash
# ========== Nginx 路由验证脚本 ==========
# 阶段三_微服务拆分实施计划 - 3.9 Nginx 路由配置
# 用途：验证 Nginx 路由是否正确将请求转发到对应的微服务
#
# 使用方式：
#   ./scripts/verify-routes.sh              # 验证所有路由
#   ./scripts/verify-routes.sh file-service # 只验证 file-service

set -e

# 配置
NGINX_HOST=${NGINX_HOST:-localhost}
NGINX_PORT=${NGINX_PORT:-80}
BASE_URL="http://${NGINX_HOST}:${NGINX_PORT}"
TIMEOUT=10

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试计数器
PASSED=0
FAILED=0

# 测试函数
test_route() {
    local path=$1
    local expected_service=$2
    local method=${3:-GET}
    local body=${4:-}
    local description=${5:-}

    local url="${BASE_URL}${path}"
    local response
    local http_code

    echo -n "Testing ${method} ${path} -> ${expected_service} ... "

    # 发送请求并获取响应头
    if [ "$method" = "GET" ]; then
        http_code=$(curl -s -o /dev/null -w "%{http_code}" --max-time $TIMEOUT "$url" 2>/dev/null || echo "000")
    else
        http_code=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" -H "Content-Type: application/json" \
            --max-time $TIMEOUT "$url" -d "$body" 2>/dev/null || echo "000")
    fi

    # 检查 HTTP 状态码
    if [ "$http_code" != "000" ]; then
        echo -e "${GREEN}✓${NC} (HTTP ${http_code})"
        ((PASSED++))
    else
        echo -e "${RED}✗${NC} (连接失败)"
        ((FAILED++))
    fi
}

# 测试服务健康检查
test_health() {
    local service=$1
    local port=$2

    echo -n "Health check ${service} (${port}) ... "

    local http_code=$(curl -s -o /dev/null -w "%{http_code}" --max-time $TIMEOUT "http://localhost:${port}/actuator/health" 2>/dev/null || echo "000")

    if [ "$http_code" = "200" ]; then
        echo -e "${GREEN}✓${NC}"
        ((PASSED++))
    else
        echo -e "${RED}✗${NC} (HTTP ${http_code})"
        ((FAILED++))
    fi
}

# Nginx 配置语法检查
test_nginx_config() {
    echo -n "Nginx config syntax ... "

    # 在容器内执行 nginx -t
    local result=$(docker exec sg_nginx nginx -t 2>&1 || echo "failed")

    if echo "$result" | grep -q "syntax is okay"; then
        echo -e "${GREEN}✓${NC}"
        ((PASSED++))
    else
        echo -e "${RED}✗${NC} (配置错误)"
        echo "  $result"
        ((FAILED++))
    fi
}

# 显示标题
echo "============================================"
echo "  Nginx 路由验证脚本"
echo "  阶段三_微服务拆分实施计划 - 3.9"
echo "============================================"
echo ""

# 检查参数
SERVICE_FILTER=$1

# 1. Nginx 配置语法检查
echo "=== 1. Nginx 配置语法检查 ==="
test_nginx_config
echo ""

# 2. 基础设施健康检查
echo "=== 2. 基础设施健康检查 ==="
test_health "MySQL" "3307"
test_health "Redis" "6379"
test_health "Nacos" "8848"
test_health "MinIO" "9000"
test_health "Sentinel" "8858"
echo ""

# 3. 微服务健康检查
echo "=== 3. 微服务健康检查 ==="
if [ -z "$SERVICE_FILTER" ] || [ "$SERVICE_FILTER" = "file-service" ]; then
    test_health "file-service" "8081"
fi
if [ -z "$SERVICE_FILTER" ] || [ "$SERVICE_FILTER" = "user-service" ]; then
    test_health "user-service" "8082"
fi
if [ -z "$SERVICE_FILTER" ] || [ "$SERVICE_FILTER" = "activity-service" ]; then
    test_health "activity-service" "8084"
fi
if [ -z "$SERVICE_FILTER" ] || [ "$SERVICE_FILTER" = "project-award-service" ]; then
    test_health "project-award-service" "8085"
fi
if [ -z "$SERVICE_FILTER" ] || [ "$SERVICE_FILTER" = "hr-service" ]; then
    test_health "hr-service" "8089"
fi
if [ -z "$SERVICE_FILTER" ] || [ "$SERVICE_FILTER" = "monitor-service" ]; then
    test_health "monitor-service" "8086"
fi
if [ -z "$SERVICE_FILTER" ] || [ "$SERVICE_FILTER" = "content-service" ]; then
    test_health "content-service" "8087"
fi
if [ -z "$SERVICE_FILTER" ] || [ "$SERVICE_FILTER" = "ai-service" ]; then
    test_health "ai-service" "8094"
fi
echo ""

# 4. Nginx 路由验证
echo "=== 4. Nginx 路由验证 ==="

# 文件服务路由
if [ -z "$SERVICE_FILTER" ] || [ "$SERVICE_FILTER" = "file-service" ]; then
    echo "--- file-service 路由 ---"
    test_route "/api/files/upload" "file-service" "POST" "{}" "文件上传"
    test_route "/api/files/1" "file-service" "GET" "" "文件查看"
    echo ""
fi

# 用户服务路由
if [ -z "$SERVICE_FILTER" ] || [ "$SERVICE_FILTER" = "user-service" ]; then
    echo "--- user-service 路由 ---"
    test_route "/api/users/login" "user-service" "POST" '{"username":"admin","password":"admin123"}' "用户登录"
    test_route "/api/users/register" "user-service" "POST" '{"username":"test","password":"test123"}' "用户注册"
    test_route "/api/users/profile" "user-service" "GET" "" "用户信息"
    test_route "/api/members" "user-service" "GET" "" "成员列表"
    echo ""
fi

# 活动服务路由
if [ -z "$SERVICE_FILTER" ] || [ "$SERVICE_FILTER" = "activity-service" ]; then
    echo "--- activity-service 路由 ---"
    test_route "/api/activities" "activity-service" "GET" "" "活动列表"
    test_route "/api/attendance" "activity-service" "GET" "" "考勤列表"
    test_route "/api/study" "activity-service" "GET" "" "学习记录"
    echo ""
fi

# 项目奖项服务路由
if [ -z "$SERVICE_FILTER" ] || [ "$SERVICE_FILTER" = "project-award-service" ]; then
    echo "--- project-award-service 路由 ---"
    test_route "/api/projects" "project-award-service" "GET" "" "项目列表"
    test_route "/api/awards" "project-award-service" "GET" "" "奖项列表"
    echo ""
fi

# HR服务路由
if [ -z "$SERVICE_FILTER" ] || [ "$SERVICE_FILTER" = "hr-service" ]; then
    echo "--- hr-service 路由 ---"
    test_route "/api/recruit" "hr-service" "GET" "" "招聘列表"
    test_route "/api/resumes" "hr-service" "GET" "" "简历列表"
    echo ""
fi

# 监控服务路由
if [ -z "$SERVICE_FILTER" ] || [ "$SERVICE_FILTER" = "monitor-service" ]; then
    echo "--- monitor-service 路由 ---"
    test_route "/api/problems" "monitor-service" "GET" "" "问题列表"
    test_route "/api/logs" "monitor-service" "GET" "" "日志列表"
    echo ""
fi

# 内容服务路由
if [ -z "$SERVICE_FILTER" ] || [ "$SERVICE_FILTER" = "content-service" ]; then
    echo "--- content-service 路由 ---"
    test_route "/api/groups" "content-service" "GET" "" "群组列表"
    test_route "/api/news" "content-service" "GET" "" "新闻列表"
    echo ""
fi

# AI服务路由
if [ -z "$SERVICE_FILTER" ] || [ "$SERVICE_FILTER" = "ai-service" ]; then
    echo "--- ai-service 路由 ---"
    test_route "/api/ai/chat" "ai-service" "POST" '{"message":"test"}' "AI对话"
    echo ""
fi

# 5. 旧 WAR 兜底路由
echo "--- 旧 WAR 兜底路由 ---"
test_route "/login.jsp" "tomcat_backend" "GET" "" "登录页面"
test_route "/" "tomcat_backend" "GET" "" "首页"
test_route "/member/" "tomcat_backend" "GET" "" "会员首页"
echo ""

# 6. 静态资源路由
echo "--- 静态资源路由 ---"
test_route "/css/main.css" "nginx_static" "GET" "" "CSS"
test_route "/js/app.js" "nginx_static" "GET" "" "JS"
test_route "/images/logo.png" "nginx_static" "GET" "" "图片"
echo ""

# 7. 健康检查端点
echo "--- 健康检查端点 ---"
test_route "/health" "nginx" "GET" "" "Nginx健康"
test_route "/microservices/health" "nginx" "GET" "" "微服务健康"
echo ""

# 总结
echo "============================================"
echo "  测试结果汇总"
echo "============================================"
echo -e "  ${GREEN}通过: ${PASSED}${NC}"
echo -e "  ${RED}失败: ${FAILED}${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ 所有测试通过！${NC}"
    exit 0
else
    echo -e "${RED}✗ 有 ${FAILED} 个测试失败${NC}"
    exit 1
fi
