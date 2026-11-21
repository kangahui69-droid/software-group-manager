#!/usr/bin/env bash
# Read-only production provenance collection. Run from /opt/software-group on the VM.
set -euo pipefail

project_dir="${1:-/opt/software-group}"
paths_file="${2:-}"
cd "$project_dir"
stamp="$(date -u +%Y%m%dT%H%M%SZ)"
output_dir="${3:-$project_dir/backup/qa04-production-$stamp}"
mkdir -p "$output_dir"

date -u +%Y-%m-%dT%H:%M:%SZ > "$output_dir/collected-utc.txt"
hostname > "$output_dir/hostname.txt"
if [ -d .git ]; then
  git rev-parse HEAD > "$output_dir/git-head.txt"
  git status --porcelain=v1 > "$output_dir/git-status.txt"
else
  printf 'NO_GIT_WORKTREE\n' > "$output_dir/git-head.txt"
fi

# Hash configuration without copying .env or printing environment values.
for path in docker-compose.yml Dockerfile.tomcat nginx/nginx.conf .env; do
  if [ -f "$path" ]; then
    sha256sum -- "$path" >> "$output_dir/config-sha256.txt"
  fi
done

if [ -n "$paths_file" ]; then
  : > "$output_dir/source-sha256.txt"
  while IFS= read -r path || [ -n "$path" ]; do
    case "$path" in
      ''|/*|../*|*/../*|*/..|*'\\'*)
        printf 'Rejected unsafe relative path: %s\n' "$path" >&2
        exit 1
        ;;
    esac
    if [ -f "$path" ] && [ ! -L "$path" ]; then
      sha256sum -- "$path" >> "$output_dir/source-sha256.txt"
    else
      printf 'MISSING\t%s\n' "$path" >> "$output_dir/source-sha256.txt"
    fi
  done < "$paths_file"
fi

printf 'container\tservice\timage_reference\timage_id\tstatus\n' > "$output_dir/containers.tsv"
while IFS= read -r container_id; do
  [ -n "$container_id" ] || continue
  docker inspect --format '{{.Name}}{{printf "\t"}}{{index .Config.Labels "com.docker.compose.service"}}{{printf "\t"}}{{.Config.Image}}{{printf "\t"}}{{.Image}}{{printf "\t"}}{{.State.Status}}' \
    "$container_id" >> "$output_dir/containers.tsv"
done < <(docker compose ps -q)

printf 'image_id\trepo_digests\trepo_tags\tsource_revision_label\n' > "$output_dir/images.tsv"
cut -f 4 "$output_dir/containers.tsv" | tail -n +2 | sort -u | while IFS= read -r image_id; do
  [ -n "$image_id" ] || continue
  docker image inspect --format '{{.Id}}{{printf "\t"}}{{json .RepoDigests}}{{printf "\t"}}{{json .RepoTags}}{{printf "\t"}}{{index .Config.Labels "org.opencontainers.image.revision"}}' \
    "$image_id" >> "$output_dir/images.tsv"
done

# Query migration metadata only. Credentials stay inside the MySQL container.
if docker ps --format '{{.Names}}' | grep -Fxq sg_mysql; then
  docker exec sg_mysql sh -c \
    'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot -N -e "SELECT installed_rank, COALESCE(version, '\''<baseline>'\''), description, success FROM software_group.flyway_schema_history ORDER BY installed_rank DESC LIMIT 20"' \
    > "$output_dir/flyway-history.tsv" 2> "$output_dir/flyway-query-error.txt" || true
fi

printf 'Evidence saved to %s\n' "$output_dir"
