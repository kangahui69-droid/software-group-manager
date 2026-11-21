#!/usr/bin/env bash
# Read-only production acceptance evidence for manual section 19.12.
# Run on the VM only after deploying the candidate and required database migrations.
set -uo pipefail

if [ "$#" -ne 0 ] || [ -z "${BASE_URL:-}" ]; then
  echo 'Usage: BASE_URL=https://VM_IP [CURL_CA_BUNDLE=/path/to/ca.crt] bash scripts/collect-ops07-acceptance.sh' >&2
  exit 2
fi

BASE_URL=${BASE_URL%/}
if [[ ! "$BASE_URL" =~ ^https?://[^/@?#]+$ ]]; then
  echo 'BASE_URL must be an HTTP(S) origin without credentials, path, query or fragment' >&2
  exit 2
fi

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir" || exit 2
stamp=$(date -u -d '+8 hours' +%Y%m%d_%H%M%S)
result_dir=${OPS07_OUTPUT_DIR:-"$project_dir/backup/ops07-acceptance-$stamp"}
mkdir -p -- "$result_dir" || exit 2
chmod 700 "$result_dir"
rows="$result_dir/rows.tmp"
: > "$rows"
failures=0

run_check() {
  local id=$1 label=$2 log="$result_dir/$1.log" rc status
  shift 2
  if "$@" > "$log" 2>&1; then
    rc=0
    status='通过'
  else
    rc=$?
    status='失败'
    failures=$((failures + 1))
  fi
  printf '| %s | %s | %s | %s | `%s.log` |\n' "$id" "$label" "$status" "$rc" "$id" >> "$rows"
  printf '%s: %s (exit=%s)\n' "$id" "$status" "$rc"
}

check_war_direct() {
  local output code
  if ! output=$(docker compose exec -T nginx wget -S --spider \
      http://app:8080/software-group/login.jsp 2>&1); then
    printf '%s\n' "$output"
    return 1
  fi
  code=$(printf '%s\n' "$output" |
    sed -nE 's/^[[:space:]]*HTTP\/[0-9.]+[[:space:]]+([0-9]{3}).*/\1/p' | tail -n 1)
  printf 'app-direct HTTP %s\n' "${code:-unknown}"
  [ "$code" = 200 ]
}

check_nginx_entry() {
  local code
  if ! code=$(curl --noproxy '*' -sS --max-time 10 -o /dev/null \
      -w '%{http_code}' "$BASE_URL/software-group/login.jsp"); then
    printf 'via-nginx connection failed (HTTP %s)\n' "${code:-000}"
    return 1
  fi
  printf 'via-nginx HTTP %s\n' "$code"
  [ "$code" = 200 ]
}

check_routes() {
  BASE_URL="$BASE_URL" bash scripts/verify-routes.sh
}

check_smoke() {
  BASE_URL="$BASE_URL" CHECK_LOCAL_SERVICES=1 bash scripts/e2e-smoke-test.sh
}

check_mig43() {
  BASE_URL="$BASE_URL" bash scripts/verify-mig43-production-readiness.sh
}

run_check infrastructure '基础设施与容器状态' bash scripts/verify-infrastructure.sh
run_check routes '网关回退路由' check_routes
run_check smoke '网关与服务健康冒烟' check_smoke
run_check app-direct 'Nginx 容器内直连旧 WAR' check_war_direct
run_check via-nginx 'Nginx 统一入口登录页' check_nginx_entry

if [ "${MIG43_CANDIDATE_DEPLOYED:-0}" = 1 ]; then
  run_check mig43 '4.3 生产只读预检' check_mig43
else
  printf '| mig43 | 4.3 生产只读预检 | 待执行：候选版本及数据库迁移未确认 | — | — |\n' >> "$rows"
fi

git_sha=$(git rev-parse HEAD 2>/dev/null || printf '未取得')
if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  worktree_state='非 Git 工作区'
elif [ -n "$(git status --porcelain --untracked-files=no 2>/dev/null)" ]; then
  worktree_state='有未提交的受跟踪文件改动'
else
  worktree_state='无未提交的受跟踪文件改动'
fi
docker compose ps -q 2>/dev/null | while IFS= read -r container_id; do
  [ -n "$container_id" ] || continue
  docker inspect --format '{{.Name}} {{.Image}} {{.State.Status}}' "$container_id"
done > "$result_dir/containers.txt"

summary="$result_dir/summary.md"
{
  printf '# OPS-07 生产步骤 6 验收记录\n\n'
  printf -- '- 执行时间（北京时间）：%s\n' "$(date -u -d '+8 hours' '+%Y-%m-%d %H:%M:%S +08:00')"
  printf -- '- 生产入口：`%s`\n' "$BASE_URL"
  printf -- '- Git 提交：`%s`；工作区：%s\n' "$git_sha" "$worktree_state"
  printf -- '- 镜像 ID 与容器状态：见 `containers.txt`\n'
  printf -- '- 本记录仅包含只读检查；登录、上传和业务读写需单独验收。\n\n'
  printf '| 编号 | 检查 | 结果 | 退出码 | 输出文件 |\n'
  printf '|---|---|---|---:|---|\n'
  cat "$rows"
  printf '\n核心 5 项结果：%s。4.3 预检仅在候选版本部署并完成迁移后运行。\n' \
    "$(if [ "$failures" -eq 0 ]; then printf '通过'; else printf '失败（%s 项）' "$failures"; fi)"
} > "$summary"
rm -f -- "$rows"
printf 'Evidence: %s\n' "$summary"
[ "$failures" -eq 0 ]
