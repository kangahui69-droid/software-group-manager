#!/bin/bash
# ============================================================
# 阶段三 E2E 冒烟测试脚本
# 对应计划文档：阶段三_微服务拆分实施计划 - 3.12 测试策略
# 使用方式：./scripts/e2e-smoke-test.sh
# 环境变量：BASE_URL（默认 http://localhost）
# ============================================================

# 不使用 set -e，因为测试失败不应该退出脚本

echo "========================================"
echo "阶段三 E2E 冒烟测试"
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

# 配置
BASE_URL=${BASE_URL:-http://localhost}
TIMEOUT=15

# 检查函数
# 用法: check_endpoint <名称> <期望状态码> <URL> [方法] [请求体] [请求头1] [请求头2] ...
# 请求头按 "Name: value" 逐个作为独立参数传入，不做二次引号包裹
check_endpoint() {
    local name=$1
    local expected_code=$2
    local url=$3
    local method=${4:-GET}
    local body=${5:-}
    local -a extra_headers=("${@:6}")

    echo -n "检查 $name ... "

    local -a cargs=(-s -o /dev/null -w "%{http_code}" --max-time "$TIMEOUT" -X "$method")
    local h
    for h in "${extra_headers[@]}"; do
        [ -n "$h" ] && cargs+=(-H "$h")
    done
    if [ -n "$body" ]; then
        cargs+=(-H "Content-Type: application/json" -d "$body")
    fi

    local response
    response=$(curl "${cargs[@]}" "$url" 2>/dev/null || echo "000")

    if [ "$response" = "$expected_code" ]; then
        echo -e "${GREEN}✓ 通过${NC} (HTTP $response)"
        PASSED=$((PASSED + 1))
    else
        echo -e "${RED}✗ 失败${NC} (期望 $expected_code, 实际 $response)"
        FAILED=$((FAILED + 1))
    fi
}

# 获取 Token
get_token() {
    local username=$1
    local password=$2
    TOKEN=$(curl -s -X POST "$BASE_URL/api/users/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"password\":\"$password\"}" \
        --max-time $TIMEOUT 2>/dev/null | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    echo "$TOKEN"
}

echo "========================================"
echo "1. 基础设施健康检查"
echo "========================================"

check_endpoint "Nginx 健康" "200" "$BASE_URL/health"
check_endpoint "Nginx 微服务健康" "200" "$BASE_URL/microservices/health"
check_endpoint "旧 WAR 首页" "200" "$BASE_URL/"
check_endpoint "旧 WAR 登录页" "200" "$BASE_URL/login.jsp"

echo ""
echo "========================================"
echo "2. 用户认证流程"
echo "========================================"

# 测试登录：使用专用测试账号，凭据由受控环境提供
if [ -n "${E2E_USERNAME:-}" ] && [ -n "${E2E_PASSWORD:-}" ]; then
    TOKEN=$(get_token "$E2E_USERNAME" "$E2E_PASSWORD")
else
    TOKEN=""
    echo -e "${YELLOW}⚠ 未提供 E2E_USERNAME/E2E_PASSWORD，跳过登录测试${NC}"
fi
if [ -n "$TOKEN" ]; then
    echo -e "${GREEN}✓ 登录成功${NC}"
    PASSED=$((PASSED + 1))
else
    if [ -n "${E2E_USERNAME:-}" ] && [ -n "${E2E_PASSWORD:-}" ]; then
        echo -e "${RED}✗ 登录失败${NC}"
        FAILED=$((FAILED + 1))
    fi
    TOKEN=""
fi

# 测试带 Token 的受保护端点
if [ -n "$TOKEN" ]; then
    check_endpoint "用户信息" "200" "$BASE_URL/api/users/profile" "GET" "" "Authorization: Bearer $TOKEN" "X-User-Id: 1"
    check_endpoint "成员列表" "200" "$BASE_URL/api/members?page=1&pageSize=10" "GET" "" "Authorization: Bearer $TOKEN" "X-User-Id: 1"
else
    echo -e "${YELLOW}⚠ 跳过受保护端点测试（无Token）${NC}"
fi

echo ""
echo "========================================"
echo "3. 微服务健康检查"
echo "========================================"

check_endpoint "file-service" "200" "http://localhost:8081/actuator/health"
check_endpoint "user-service" "200" "http://localhost:8082/actuator/health"
check_endpoint "activity-service" "200" "http://localhost:8084/actuator/health"
check_endpoint "project-award-service" "200" "http://localhost:8085/actuator/health"
check_endpoint "hr-service" "200" "http://localhost:8089/actuator/health"
check_endpoint "monitor-service" "200" "http://localhost:8086/actuator/health"
check_endpoint "content-service" "200" "http://localhost:8087/actuator/health"
check_endpoint "ai-service" "200" "http://localhost:8094/actuator/health"

echo ""
echo "========================================"
echo "4. 微服务 API 功能验证"
echo "========================================"

if [ -n "$TOKEN" ]; then
    # 业务接口默认需要 Bearer Token；管理员接口使用管理员账号的 Token
    check_endpoint "活动列表" "200" "$BASE_URL/api/activities" "GET" "" "Authorization: Bearer $TOKEN" "X-User-Id: 1"
    check_endpoint "考勤列表" "200" "$BASE_URL/api/attendance" "GET" "" "Authorization: Bearer $TOKEN" "X-User-Id: 1"
    check_endpoint "学习记录" "200" "$BASE_URL/api/study" "GET" "" "Authorization: Bearer $TOKEN" "X-User-Id: 1"

    # project-award-service
    check_endpoint "项目列表" "200" "$BASE_URL/api/projects" "GET" "" "Authorization: Bearer $TOKEN" "X-User-Id: 1"
    check_endpoint "奖项列表" "200" "$BASE_URL/api/awards" "GET" "" "Authorization: Bearer $TOKEN" "X-User-Id: 1"

    # hr-service
    check_endpoint "招聘列表" "200" "$BASE_URL/api/recruit" "GET" "" "Authorization: Bearer $TOKEN" "X-User-Id: 1"
    check_endpoint "简历列表" "200" "$BASE_URL/api/resumes" "GET" "" "Authorization: Bearer $TOKEN" "X-User-Id: 1"

    # monitor-service
    check_endpoint "问题列表" "200" "$BASE_URL/api/problems" "GET" "" "Authorization: Bearer $TOKEN" "X-User-Id: 1"
    check_endpoint "日志列表" "200" "$BASE_URL/api/logs" "GET" "" "Authorization: Bearer $TOKEN" "X-User-Id: 1"

    # content-service
    check_endpoint "群组列表" "200" "$BASE_URL/api/groups" "GET" "" "Authorization: Bearer $TOKEN" "X-User-Id: 1"
    check_endpoint "新闻列表" "200" "$BASE_URL/api/news" "GET" "" "Authorization: Bearer $TOKEN" "X-User-Id: 1"

    # file-service
    check_endpoint "文件信息" "200" "$BASE_URL/api/files/1" "GET" "" "Authorization: Bearer $TOKEN" "X-User-Id: 1"

    # ai-service（chat 为本地兜底回复，无外部 LLM 调用）
    check_endpoint "AI对话" "200" "$BASE_URL/api/ai/chat" "POST" '{"message":"你好"}' "Authorization: Bearer $TOKEN" "X-User-Id: 1"
else
    echo -e "${YELLOW}⚠ 跳过 API 功能测试（无Token）${NC}"
fi

echo ""
echo "========================================"
echo "5. 未授权访问验证"
echo "========================================"

# 八个服务统一认证入口；匿名仅可访问明确公开的接口
check_endpoint "未登录访问用户信息(受保护)" "401" "$BASE_URL/api/users/profile"
check_endpoint "未登录访问公开活动列表" "200" "$BASE_URL/api/activities"
check_endpoint "未登录访问公开项目列表" "200" "$BASE_URL/api/projects"
check_endpoint "未登录访问公开奖项列表" "200" "$BASE_URL/api/awards"
check_endpoint "未登录访问个人项目(受保护)" "401" "$BASE_URL/api/projects/my"
check_endpoint "未登录访问新闻列表(公开)" "200" "$BASE_URL/api/news"

echo ""
echo "========================================"
echo "测试结果汇总"
echo "========================================"
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ 所有测试通过！${NC}"
    exit 0
else
    echo -e "${RED}✗ 有 $FAILED 个测试失败${NC}"
    exit 1
fi
