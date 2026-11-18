#!/usr/bin/env bash
# Read-only gateway smoke check for the MIG-04 safe route policy.
# Usage: BASE_URL=http://host bash scripts/e2e-smoke-test.sh
# Run on the VM with CHECK_LOCAL_SERVICES=1 to also check container-local Actuator endpoints.
set -euo pipefail

BASE_URL=${BASE_URL:-http://localhost}
BASE_URL=${BASE_URL%/}
TIMEOUT=${TIMEOUT:-10}
SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)

if [ "$#" -ne 0 ]; then
  echo 'Usage: BASE_URL=http://host bash scripts/e2e-smoke-test.sh' >&2
  exit 2
fi

check_page() {
  local path=$1 expected=$2 code
  if ! code=$(curl -sS -L -o /dev/null -w '%{http_code}' --max-time "$TIMEOUT" "$BASE_URL$path"); then
    echo "FAIL $path: connection error" >&2
    exit 1
  fi
  if [ "$code" != "$expected" ]; then
    echo "FAIL $path: expected HTTP $expected, got $code" >&2
    exit 1
  fi
  echo "PASS $path (HTTP $code)"
}

check_page /health 200
check_page / 200
check_page /login.jsp 200

# Verifies the legacy WAR route marker, 503 routes and honest proxy health response.
BASE_URL="$BASE_URL" TIMEOUT="$TIMEOUT" bash "$SCRIPT_DIR/verify-routes.sh"

if [ "${CHECK_LOCAL_SERVICES:-0}" = 1 ]; then
  if ! docker compose exec -T nginx wget --no-check-certificate -q --spider \
    https://127.0.0.1/ready; then
    echo 'FAIL Nginx: legacy WAR readiness through the gateway failed' >&2
    exit 1
  fi
  echo 'PASS Nginx container-local readiness'
  for entry in \
    "file-service:${FILE_SERVICE_PORT:-8081}" \
    "user-service:${USER_SERVICE_PORT:-8082}" \
    "activity-service:${ACTIVITY_SERVICE_PORT:-8084}" \
    "project-award-service:${PROJECT_AWARD_SERVICE_PORT:-8085}" \
    "hr-service:${HR_SERVICE_PORT:-8089}" \
    "monitor-service:${MONITOR_SERVICE_PORT:-8086}" \
    "content-service:${CONTENT_SERVICE_PORT:-8087}" \
    "ai-service:${AI_SERVICE_PORT:-8094}"; do
    service=${entry%%:*}
    port=${entry##*:}
    if ! docker compose exec -T "$service" wget -q --spider \
      "http://127.0.0.1:$port/actuator/health"; then
      echo "FAIL $service: container-local health check failed" >&2
      exit 1
    fi
    echo "PASS $service container-local health"
  done
fi

echo 'Gateway smoke passed. Business writes, login sessions and microservice cutover require separate verification.'
