#!/usr/bin/env bash
# Exit nonzero when the deployed certificate is missing, mismatched, or near expiry.
set -euo pipefail

if [ "$#" -ne 3 ]; then
  echo 'Usage: check-ip-tls-cert.sh <IPv4> <CA_CERT> <TLS_CERT_DIR>' >&2
  exit 2
fi
ip=$1
ca_cert=$2
cert_dir=$3
openssl x509 -in "$cert_dir/fullchain.pem" -noout -checkend 604800 >/dev/null || {
  echo 'TLS certificate expires within 7 days or is invalid' >&2
  exit 1
}
openssl x509 -in "$cert_dir/fullchain.pem" -noout -ext subjectAltName | tr ',' '\n' | sed 's/^[[:space:]]*//' | grep -Fxq "IP Address:$ip" || {
  echo 'TLS certificate does not contain the requested IP SAN' >&2
  exit 1
}
openssl verify -CAfile "$ca_cert" "$cert_dir/fullchain.pem" >/dev/null || {
  echo 'TLS certificate does not chain to the expected CA' >&2
  exit 1
}
cert_pub=$(openssl x509 -in "$cert_dir/fullchain.pem" -pubkey -noout | openssl pkey -pubin -outform DER | openssl dgst -sha256)
key_pub=$(openssl pkey -in "$cert_dir/privkey.pem" -pubout -outform DER | openssl dgst -sha256)
if [ "$cert_pub" != "$key_pub" ]; then
  echo 'TLS certificate and private key do not match' >&2
  exit 1
fi
echo "TLS certificate OK for $ip"
