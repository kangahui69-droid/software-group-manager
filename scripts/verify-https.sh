#!/usr/bin/env bash
# Run from outside the VM after DNS/IP routing, firewall and client CA trust are ready.
set -euo pipefail

if [ "$#" -ne 2 ]; then
  echo 'Usage: verify-https.sh <public-IPv4> <trusted-CA-certificate>' >&2
  exit 2
fi
ip=$1
ca_cert=$2
headers=$(mktemp)
trap 'rm -f "$headers"' EXIT

code=$(curl -sS --max-time 10 -D "$headers" -o /dev/null -w '%{http_code}' "http://$ip/health")
if [ "$code" != 308 ] || ! grep -qi "^Location: https://$ip/health" "$headers"; then
  echo "HTTP redirect failed: status=$code" >&2
  exit 1
fi

code=$(curl -sS --cacert "$ca_cert" --max-time 10 -D "$headers" -o /dev/null -w '%{http_code}' "https://$ip/health")
if [ "$code" != 200 ] || ! grep -qi '^Strict-Transport-Security: max-age=31536000' "$headers"; then
  echo "HTTPS health or HSTS failed: status=$code" >&2
  exit 1
fi

code=$(curl -sS --cacert "$ca_cert" --max-time 10 -D "$headers" -o /dev/null -w '%{http_code}' "https://$ip/api/ai")
if [ "$code" != 503 ] || ! grep -qi '^Strict-Transport-Security: max-age=31536000' "$headers"; then
  echo "HTTPS API or HSTS failed: status=$code" >&2
  exit 1
fi

curl -fsS --cacert "$ca_cert" --tlsv1.2 --tls-max 1.2 --max-time 10 -o /dev/null "https://$ip/health"
echo "HTTPS PASS for $ip: redirect, trusted IP certificate, HSTS, API and TLS 1.2"
