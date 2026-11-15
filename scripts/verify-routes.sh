#!/usr/bin/env bash
# Read-only route check for the MIG-04 safe default. Run after nginx -t and reload.
set -euo pipefail

BASE_URL=${BASE_URL:-http://localhost}
BASE_URL=${BASE_URL%/}
TIMEOUT=${TIMEOUT:-10}
if [ "$#" -ne 0 ]; then
  echo "Usage: BASE_URL=http://host ./scripts/verify-routes.sh" >&2
  exit 2
fi

headers=$(mktemp)
trap 'rm -f "$headers"' EXIT
passed=0

check_route() {
  local path=$1 expected_route=$2 expected_code=${3:-}
  local code
  if ! code=$(curl -sS -D "$headers" -o /dev/null -w '%{http_code}' \
      --max-time "$TIMEOUT" "$BASE_URL$path"); then
    echo "FAIL $path: connection error" >&2
    exit 1
  fi
  if ! grep -qi "^X-SG-Route: $expected_route" "$headers"; then
    echo "FAIL $path: expected X-SG-Route: $expected_route" >&2
    exit 1
  fi
  if [ -n "$expected_code" ] && [ "$code" != "$expected_code" ]; then
    echo "FAIL $path: expected HTTP $expected_code, got $code" >&2
    exit 1
  fi
  if [ -z "$expected_code" ]; then
    case "$code" in
      200|400|401|403|405) ;;
      *) echo "FAIL $path: legacy WAR returned unexpected HTTP $code" >&2; exit 1 ;;
    esac
  fi
  echo "PASS $path -> $expected_route (HTTP $code)"
  passed=$((passed + 1))
}

# These GET requests do not create or modify business data. Auth failures are allowed.
for domain in files users activities attendance study projects awards recruit resumes problems logs groups news; do
  check_route "/api/$domain" legacy-war
  check_route "/api/$domain/" legacy-war
done
check_route /api/ai disabled 503
check_route /api/ai/chat disabled 503
check_route /api/members disabled 503
check_route /api/members/1 disabled 503

health=$(curl -fsS --max-time "$TIMEOUT" "$BASE_URL/microservices/health")
if ! grep -q '"apiRoute":"legacy-war"' <<< "$health" || \
   ! grep -q '"businessValidated":false' <<< "$health"; then
  echo 'FAIL /microservices/health: business status is misleading' >&2
  exit 1
fi
echo "PASS /microservices/health describes routing only"
echo "Verified $passed read-only API routes. Business write/read checks are a separate release gate."
