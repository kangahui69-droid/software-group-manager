#!/usr/bin/env bash
# Run only against disposable local MySQL databases, never against production.
set -euo pipefail

: "${MIG01_MYSQL_URL:?Set MIG01_MYSQL_URL to an isolated mig01_test database}"
: "${MIG02_MYSQL_URL:?Set MIG02_MYSQL_URL to an isolated mig02_test database}"
: "${MIG03_MYSQL_URL:?Set MIG03_MYSQL_URL to an isolated mig03_test database}"

for spec in "MIG01_MYSQL_URL:mig01_test" "MIG02_MYSQL_URL:mig02_test" "MIG03_MYSQL_URL:mig03_test"; do
  variable=${spec%%:*}
  database=${spec##*:}
  value=${!variable}
  if [[ ! "$value" =~ ^jdbc:mysql://(127\.0\.0\.1|localhost):[0-9]+/$database([?].*)?$ ]]; then
    echo "$variable must point to local $database; refusing to run destructive integration fixtures" >&2
    exit 2
  fi
done

MVN=${MVN:-mvn}
"$MVN" -o -pl project-award-service -am -Dtest=ProjectAwardMySqlTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
"$MVN" -o -pl content-service -am -Dtest=ContentMySqlTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
"$MVN" -o -pl ai-service -am -Dtest=AiSessionMySqlTest \
  -Dsurefire.failIfNoSpecifiedTests=false test

echo "DB write/read integration checks passed. Nginx route and client-contract checks remain separate gates."
