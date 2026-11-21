#!/usr/bin/env bash
# Issue or renew a private-CA certificate for an internal IPv4 address.
# Keep CA_DIR and TLS_CERT_DIR outside the Git checkout and in separate directories.
set -euo pipefail

if [ "$#" -ne 3 ]; then
  echo 'Usage: issue-internal-ip-cert.sh <IPv4> <CA_DIR> <TLS_CERT_DIR>' >&2
  exit 2
fi

ip=$1
ca_dir=$2
cert_dir=$3
if [[ ! $ip =~ ^([0-9]{1,3}\.){3}[0-9]{1,3}$ ]]; then
  echo 'Expected a valid IPv4 address' >&2
  exit 2
fi
IFS=. read -r -a octets <<< "$ip"
for octet in "${octets[@]}"; do
  if [ "$octet" != "0" ] && [[ $octet == 0* ]]; then
    echo 'IPv4 address must use canonical decimal notation' >&2
    exit 2
  fi
  if (( 10#$octet > 255 )); then
    echo 'Expected a valid IPv4 address' >&2
    exit 2
  fi
done
umask 077
mkdir -p "$ca_dir" "$cert_dir"
ca_dir=$(cd "$ca_dir" && pwd -P)
cert_dir=$(cd "$cert_dir" && pwd -P)
case "$ca_dir/" in
  "$cert_dir/"*)
    echo 'TLS_CERT_DIR must not contain the CA private key directory' >&2
    exit 2
    ;;
esac
chmod 0700 "$ca_dir" "$cert_dir"
if { [ -e "$ca_dir/ca.key" ] && [ ! -e "$ca_dir/ca.crt" ]; } || \
   { [ ! -e "$ca_dir/ca.key" ] && [ -e "$ca_dir/ca.crt" ]; }; then
  echo 'Incomplete CA files; refusing to replace the existing identity' >&2
  exit 1
fi
if [ ! -e "$ca_dir/ca.key" ]; then
  openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out "$ca_dir/ca.key" 2>/dev/null
  MSYS_NO_PATHCONV=1 openssl req -new -x509 -sha256 -days 3650 \
    -key "$ca_dir/ca.key" -out "$ca_dir/ca.crt" \
    -subj '/CN=Software Group Internal TLS CA' \
    -addext 'basicConstraints=critical,CA:TRUE,pathlen:0' \
    -addext 'keyUsage=critical,keyCertSign,cRLSign'
  chmod 0600 "$ca_dir/ca.key"
  chmod 0644 "$ca_dir/ca.crt"
fi
if ! openssl x509 -in "$ca_dir/ca.crt" -noout -checkend 7776000 >/dev/null; then
  echo 'Internal CA expires within 90 days; rotate it deliberately and re-distribute trust' >&2
  exit 1
fi

if [ -f "$cert_dir/fullchain.pem" ] && [ -f "$cert_dir/privkey.pem" ] && \
   openssl x509 -in "$cert_dir/fullchain.pem" -noout -checkend 2592000 >/dev/null 2>&1 && \
   openssl x509 -in "$cert_dir/fullchain.pem" -noout -ext subjectAltName | tr ',' '\n' | sed 's/^[[:space:]]*//' | grep -Fxq "IP Address:$ip" && \
   openssl verify -CAfile "$ca_dir/ca.crt" "$cert_dir/fullchain.pem" >/dev/null 2>&1; then
  echo "Certificate already valid for more than 30 days: $cert_dir/fullchain.pem"
  exit 0
fi

tmp_dir=$(mktemp -d "$cert_dir/.issue-XXXXXX")
trap 'rm -rf -- "$tmp_dir"' EXIT
MSYS_NO_PATHCONV=1 openssl req -new -newkey rsa:3072 -nodes -sha256 \
  -keyout "$tmp_dir/privkey.pem" -out "$tmp_dir/request.csr" \
  -subj "/CN=$ip" 2>/dev/null
cat > "$tmp_dir/leaf.ext" <<EOF
basicConstraints=critical,CA:FALSE
keyUsage=critical,digitalSignature,keyEncipherment
extendedKeyUsage=serverAuth
subjectAltName=IP:$ip
EOF
openssl x509 -req -sha256 -days 90 \
  -in "$tmp_dir/request.csr" -CA "$ca_dir/ca.crt" -CAkey "$ca_dir/ca.key" \
  -CAcreateserial -extfile "$tmp_dir/leaf.ext" -out "$tmp_dir/leaf.pem" >/dev/null 2>&1
openssl verify -CAfile "$ca_dir/ca.crt" "$tmp_dir/leaf.pem" >/dev/null
cat "$tmp_dir/leaf.pem" "$ca_dir/ca.crt" > "$tmp_dir/fullchain.pem"
chmod 0600 "$tmp_dir/privkey.pem"
chmod 0644 "$tmp_dir/fullchain.pem"
mv -f "$tmp_dir/privkey.pem" "$cert_dir/privkey.pem"
mv -f "$tmp_dir/fullchain.pem" "$cert_dir/fullchain.pem"
echo "Certificate issued for $ip; import $ca_dir/ca.crt into authorized client trust stores"
echo "TLS_CERT_DIR=$cert_dir"
