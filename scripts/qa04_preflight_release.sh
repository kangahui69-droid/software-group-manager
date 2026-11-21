#!/usr/bin/env bash
# Gate a release build on a clean, committed source tree.
set -euo pipefail

project_dir="${1:-.}"
cd "$project_dir"
git rev-parse --is-inside-work-tree | grep -Fxq true
branch="$(git branch --show-current)"
if [ "$branch" != "Version-2" ]; then
  printf 'Release requires Version-2; current branch: %s\n' "$branch" >&2
  exit 1
fi
if [ -n "$(git status --porcelain=v1 --untracked-files=all)" ]; then
  printf 'Release requires a clean worktree. Review and commit source changes first.\n' >&2
  exit 1
fi
commit="$(git rev-parse HEAD)"
printf 'SOURCE_COMMIT=%s\n' "$commit"
printf 'BRANCH=%s\n' "$branch"
