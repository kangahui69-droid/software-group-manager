#!/usr/bin/env bash
# Read-only checks after deploying the MIG-01 to MIG-04 candidate on the VM.
# Run from /opt/software-group. Does not print credentials or modify database rows.
set -euo pipefail

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
BASE_URL=${BASE_URL:-http://127.0.0.1}
BASE_URL=${BASE_URL%/}

if [ "$#" -ne 0 ]; then
  echo 'Usage: BASE_URL=http://127.0.0.1 bash scripts/verify-mig43-production-readiness.sh' >&2
  exit 2
fi

docker compose config --quiet

db_value() {
  docker exec -i sg_mysql sh -c \
    'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot -N -D "${MYSQL_DATABASE:-software_group}"' <<< "$1" \
    | tr -d '\r\n'
}

check_number() {
  local name=$1 actual=$2 expected=$3
  if [ "$actual" != "$expected" ]; then
    echo "FAIL $name: expected $expected, got $actual" >&2
    exit 1
  fi
  echo "PASS $name=$actual"
}

duplicate_sessions=$(db_value 'SELECT COUNT(*) FROM (SELECT session_id FROM ai_conversation GROUP BY session_id HAVING COUNT(*) > 1) duplicates;')
session_index=$(db_value "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='ai_conversation' AND INDEX_NAME='uk_ai_conversation_session_id' AND NON_UNIQUE=0;")
mute_column=$(db_value "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='group_member' AND COLUMN_NAME='mute_reason';")
check_number duplicate_session_ids "$duplicate_sessions" 0
check_number session_unique_index "$session_index" 1
check_number member_mute_reason_column "$mute_column" 1

init_id=$(docker compose ps -aq content-storage-init)
if [ -z "$init_id" ] || [ "$(docker inspect "$init_id" --format '{{.State.ExitCode}}')" != 0 ]; then
  echo 'FAIL content-storage-init: no successful completion' >&2
  exit 1
fi
echo 'PASS content-storage-init exited successfully'

if ! docker exec sg_content_service sh -c \
  'test -w /usr/local/tomcat/webapps/localstorage/news/award && test -w /usr/local/tomcat/webapps/localstorage/news/activity && test -w /usr/local/tomcat/webapps/localstorage/news/notice'; then
  echo 'FAIL content-service cannot write all shared news directories as its runtime user' >&2
  exit 1
fi
echo 'PASS content-service runtime user can write shared news directories'

for entry in \
  'file-service:8081' 'user-service:8082' 'activity-service:8084' \
  'project-award-service:8085' 'hr-service:8089' 'monitor-service:8086' \
  'content-service:8087' 'ai-service:8094'; do
  service=${entry%%:*}
  container_port=${entry##*:}
  binding=$(docker compose port "$service" "$container_port")
  case "$binding" in
    127.0.0.1:*) echo "PASS $service host binding is loopback" ;;
    *) echo "FAIL $service host binding is $binding" >&2; exit 1 ;;
  esac
done

docker exec sg_nginx nginx -t
BASE_URL="$BASE_URL" bash "$SCRIPT_DIR/verify-routes.sh"
echo 'MIG-01 to MIG-04 release preflight passed. Authenticated page and write/read checks still require manual verification.'
