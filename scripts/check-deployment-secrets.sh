#!/usr/bin/env bash
# Read-only preflight. Never prints values from the private environment file.
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
env_file="${1:-$project_dir/.env}"

if [[ ! -f "$env_file" || -L "$env_file" ]]; then
    echo "Private .env file is missing or is a symbolic link" >&2
    exit 1
fi

mode="$(stat -c '%a' "$env_file")"
if [[ "$mode" != "600" ]]; then
    echo "Private .env file must have mode 600 (current mode: $mode)" >&2
    exit 1
fi

cd "$project_dir"
docker compose --env-file "$env_file" config --quiet
echo "Private .env permissions and required Compose variables passed"
