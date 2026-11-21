#!/usr/bin/env bash
# Build and start the HR image without Compose or external services.
set -Eeuo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir"

image="software-group-hr:ops06-test-$$"
container="sg_hr_ops06_test_$$"
cleanup() {
  docker rm -f "$container" >/dev/null 2>&1 || true
  docker image rm "$image" >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker build --file hr-service/Dockerfile --tag "$image" .

exposed="$(docker image inspect "$image" --format '{{json .Config.ExposedPorts}}')"
if [[ "$exposed" != '{"8089/tcp":{}}' ]]; then
  echo "HR image exposes unexpected ports: $exposed" >&2
  exit 1
fi

docker run --detach --name "$container" --network none \
  --env DB_PASSWORD=ops06-test-only \
  --env REDIS_PASSWORD=ops06-test-only \
  --env JWT_SECRET=0123456789abcdef0123456789abcdef \
  --env SPRING_CLOUD_NACOS_DISCOVERY_ENABLED=false \
  --env SPRING_CLOUD_NACOS_CONFIG_ENABLED=false \
  --env SPRING_CLOUD_DISCOVERY_ENABLED=false \
  --env SPRING_CLOUD_SENTINEL_ENABLED=false \
  --env ALIBABA_CLOUD_SENTINEL_ENABLED=false \
  --env MANAGEMENT_HEALTH_DB_ENABLED=false \
  --env SPRING_MAIN_LAZY_INITIALIZATION=true \
  --env SERVER_PORT=8089 \
  "$image" >/dev/null

for attempt in $(seq 1 90); do
  state="$(docker inspect "$container" --format '{{.State.Status}}|{{.State.Health.Status}}')"
  case "$state" in
    running\|healthy)
      health="$(docker exec "$container" wget -q -O - http://127.0.0.1:8089/actuator/health)"
      if [[ "$health" != *'"status":"UP"'* ]]; then
        echo "HR image health response is not UP: $health" >&2
        exit 1
      fi
      if docker exec "$container" wget -q --spider http://127.0.0.1:8085/actuator/health; then
        echo "HR image unexpectedly serves the old port 8085" >&2
        exit 1
      fi
      echo "HR image starts independently and is healthy on 8089"
      exit 0
      ;;
    exited\|*|dead\|*|running\|unhealthy)
      echo "HR image failed to become healthy: $state" >&2
      docker logs "$container" >&2
      exit 1
      ;;
  esac
  sleep 2
done

echo "Timed out waiting for HR image health check" >&2
docker inspect "$container" --format '{{json .State.Health}}' >&2
docker logs "$container" >&2
exit 1
