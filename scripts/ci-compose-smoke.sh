#!/usr/bin/env bash
# Run on an isolated GitHub-hosted runner only; never run this against production Docker.
set -Eeuo pipefail

if [[ "${GITHUB_ACTIONS:-}" != "true" || -z "${RUNNER_TEMP:-}" ]]; then
  echo 'CI Compose smoke requires an isolated GitHub Actions runner' >&2
  exit 2
fi
if [[ "${1:-}" != "" && "${1:-}" != "--config-only" ]]; then
  echo 'Usage: ci-compose-smoke.sh [--config-only]' >&2
  exit 2
fi

cd "$(dirname "${BASH_SOURCE[0]}")/.."
export SOURCE_COMMIT="$(git rev-parse HEAD)"
scratch="$(mktemp -d "$RUNNER_TEMP/software-group-ci.XXXXXX")"
tls_dir="$scratch/tls"
env_file="$scratch/compose.env"
mkdir -p "$tls_dir"
cleanup_files() {
  rm -f "$env_file" "$tls_dir/fullchain.pem" "$tls_dir/privkey.pem" "$scratch/openssl.log"
  rmdir "$tls_dir" "$scratch" 2>/dev/null || true
}
trap cleanup_files EXIT

echo 'Preparing a disposable CI TLS certificate'
if ! openssl req -x509 -newkey rsa:2048 -nodes -days 1 \
  -keyout "$tls_dir/privkey.pem" -out "$tls_dir/fullchain.pem" \
  -subj '/CN=localhost' -addext 'subjectAltName=DNS:localhost,IP:127.0.0.1' \
  >"$scratch/openssl.log" 2>&1; then
  tail -n 4 "$scratch/openssl.log" >&2
  exit 1
fi

umask 077
{
  for name in MYSQL_ROOT_PASSWORD APP_DB_PASSWORD NACOS_DB_PASSWORD \
    FILE_DB_PASSWORD USER_DB_PASSWORD ACTIVITY_DB_PASSWORD PROJECT_AWARD_DB_PASSWORD \
    HR_DB_PASSWORD MONITOR_DB_PASSWORD CONTENT_DB_PASSWORD AI_DB_PASSWORD \
    REDIS_PASSWORD RABBITMQ_PASSWORD MINIO_SECRET_KEY JWT_SECRET \
    DES_KEY AI_API_KEY GRAFANA_PASSWORD; do
    printf '%s=%s\n' "$name" "$(openssl rand -hex 24)"
  done
  printf 'NACOS_AUTH_TOKEN=%s\n' "$(openssl rand -base64 48 | tr -d '\n')"
  printf 'NACOS_AUTH_IDENTITY_KEY=ci%s\n' "$(openssl rand -hex 16)"
  printf 'NACOS_AUTH_IDENTITY_VALUE=%s\n' "$(openssl rand -hex 24)"
  printf 'NACOS_USERNAME=ci_nacos_client\n'
  printf 'NACOS_PASSWORD=%s\n' "$(openssl rand -hex 24)"
  printf 'RABBITMQ_USER=ci_rabbitmq\n'
  printf 'MINIO_ACCESS_KEY=ci%s\n' "$(openssl rand -hex 8)"
  printf 'TLS_CERT_DIR=%s\n' "$tls_dir"
} > "$env_file"

set -a
source "$env_file"
set +a
project="sgci${GITHUB_RUN_ID:-local}"
compose=(docker compose --project-name "$project" --env-file "$env_file" \
  -f docker-compose.yml -f docker-compose.ci.yml)

echo 'Checking generated CI Compose configuration'
"${compose[@]}" config --quiet
"${PYTHON:-python3}" scripts/check-nacos-auth-config.py --env-file "$env_file"
"${PYTHON:-python3}" scripts/check-compose-port-policy.py
if [[ "${1:-}" == "--config-only" ]]; then
  echo 'CI Compose configuration and generated credentials passed'
  exit 0
fi

# No image or volume is shared with production: GitHub creates a fresh runner for this job.
"${compose[@]}" build app file-service user-service activity-service \
  project-award-service hr-service monitor-service content-service ai-service
"${compose[@]}" up -d --wait --wait-timeout 180 mysql

"${compose[@]}" exec -T -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql mysql -uroot \
  -e 'CREATE DATABASE nacos_config CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci'
docker run --rm --entrypoint cat nacos/nacos-server:v2.2.3 \
  /home/nacos/conf/mysql-schema.sql |
  "${compose[@]}" exec -T -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql \
    mysql -uroot nacos_config

if ! "${compose[@]}" up -d --no-build --wait --wait-timeout 600; then
  "${compose[@]}" ps >&2 || true
  "${compose[@]}" logs --no-color --tail=80 app nginx nacos db-migrate \
    file-service user-service activity-service project-award-service \
    hr-service monitor-service content-service ai-service >&2 || true
  exit 1
fi

BASE_URL=https://127.0.0.1 CURL_CA_BUNDLE="$tls_dir/fullchain.pem" \
  CHECK_LOCAL_SERVICES=1 bash scripts/e2e-smoke-test.sh
echo 'CI default Compose stack and HTTPS gateway smoke passed'
