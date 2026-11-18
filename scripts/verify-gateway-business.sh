#!/usr/bin/env bash
# Read-only business smoke through the public HTTPS Nginx entry.
# Use a dedicated low-privilege account; the login creates a session and audit entry.
set -euo pipefail

: "${BASE_URL:?Set BASE_URL to the HTTPS entry, for example https://192.168.65.33}"
: "${SMOKE_USERNAME:?Set SMOKE_USERNAME to the dedicated read-only account}"
: "${SMOKE_PASSWORD:?Set SMOKE_PASSWORD securely in the environment}"
BASE_URL=${BASE_URL%/}
if [[ $BASE_URL != https://* ]]; then
  echo 'BASE_URL must use HTTPS' >&2
  exit 2
fi
curl_ca_args=()
if [ -n "${CURL_CA_BUNDLE:-}" ]; then
  curl_ca_args=(--cacert "$CURL_CA_BUNDLE")
fi

cookies=$(mktemp)
page=$(mktemp)
headers=$(mktemp)
body=$(mktemp)
trap 'rm -f "$cookies" "$page" "$headers" "$body"' EXIT
chmod 0600 "$cookies" "$page" "$headers" "$body"

curl "${curl_ca_args[@]}" -fsS --max-time 10 -c "$cookies" -o "$page" "$BASE_URL/software-group/login.jsp"
token=$(sed -n 's/.*<meta name="csrf-token" content="\([^"]*\)".*/\1/p' "$page" | head -n 1)
if [ -z "$token" ]; then
  echo 'Login page did not provide a CSRF token' >&2
  exit 1
fi

# Read the password from stdin so it is not placed in the curl command line.
status=$(printf '%s' "$SMOKE_PASSWORD" | curl "${curl_ca_args[@]}" -sS --max-time 15 \
  -b "$cookies" -c "$cookies" -D "$headers" -o "$body" -w '%{http_code}' \
  -H "X-CSRF-TOKEN: $token" --data-urlencode "username=$SMOKE_USERNAME" \
  --data-urlencode 'password@-' "$BASE_URL/software-group/login")
if [ "$status" != 302 ] || ! grep -Eiq '^Location: .*/software-group/index\.jsp' "$headers" || \
   grep -Eiq '^Location: http://' "$headers"; then
  echo "Login through Nginx failed (HTTP $status); check the dedicated account" >&2
  exit 1
fi

status=$(curl "${curl_ca_args[@]}" -sS --max-time 15 -b "$cookies" -o "$body" -w '%{http_code}' \
  "$BASE_URL/api/projects/my?page=1&pageSize=1")
if [ "$status" != 200 ]; then
  echo "Authenticated project read through Nginx failed (HTTP $status)" >&2
  exit 1
fi
"${PYTHON_BIN:-python3}" - "$body" <<'PY'
import json
import sys

with open(sys.argv[1], encoding="utf-8") as response:
    payload = json.load(response)
if payload.get("code") != 0 or payload.get("data") is None:
    raise SystemExit("Authenticated project response did not report success")
PY
echo 'Gateway business smoke PASS: login session and my-projects read through HTTPS'
