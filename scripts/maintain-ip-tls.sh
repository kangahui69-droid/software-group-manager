#!/usr/bin/env bash
# Daily renewal entry point. A nonzero exit must trigger an operator alert.
set -euo pipefail

if [ "$#" -ne 3 ]; then
  echo 'Usage: maintain-ip-tls.sh <IPv4> <CA_DIR> <TLS_CERT_DIR>' >&2
  exit 2
fi
ip=$1
ca_dir=$2
cert_dir=$3
script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

before=$(sha256sum "$cert_dir/fullchain.pem" 2>/dev/null | cut -d ' ' -f 1 || true)
bash "$script_dir/issue-internal-ip-cert.sh" "$ip" "$ca_dir" "$cert_dir"
bash "$script_dir/check-ip-tls-cert.sh" "$ip" "$ca_dir/ca.crt" "$cert_dir"
after=$(sha256sum "$cert_dir/fullchain.pem" | cut -d ' ' -f 1)
if [ "$before" != "$after" ]; then
  docker exec sg_nginx nginx -t
  docker exec sg_nginx nginx -s reload
  echo 'Nginx reloaded with the renewed certificate'
fi
