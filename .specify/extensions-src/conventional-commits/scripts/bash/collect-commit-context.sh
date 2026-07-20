#!/usr/bin/env bash
# Print git context for Conventional Commit generation (stdout).
# Usage: collect-commit-context.sh [--staged-only]

set -euo pipefail

STAGED_ONLY=false
if [[ "${1:-}" == "--staged-only" ]]; then
  STAGED_ONLY=true
fi

if ! command -v git >/dev/null 2>&1; then
  echo "git not found in PATH" >&2
  exit 1
fi

if ! git rev-parse --git-dir >/dev/null 2>&1; then
  echo "Not inside a git repository" >&2
  exit 1
fi

echo "=== BRANCH ==="
git rev-parse --abbrev-ref HEAD 2>/dev/null || true
echo

echo "=== STATUS ==="
git status --short
echo

echo "=== STAGED STAT ==="
git diff --cached --stat || true
echo

echo "=== STAGED DIFF ==="
git diff --cached || true
echo

if [[ "$STAGED_ONLY" == false ]]; then
  echo "=== UNSTAGED STAT ==="
  git diff --stat || true
  echo

  echo "=== UNTRACKED ==="
  git ls-files --others --exclude-standard || true
  echo
fi

echo "=== RECENT LOG ==="
git log --oneline -8 2>/dev/null || echo "(no commits yet)"
