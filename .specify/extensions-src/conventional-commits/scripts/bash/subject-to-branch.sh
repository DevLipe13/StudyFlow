#!/usr/bin/env bash
# Convert a Conventional Commits subject into a git branch name.
# Usage: subject-to-branch.sh "feat(auth): add login guard"
# Prints: feat/auth-add-login-guard

set -euo pipefail

SUBJECT="${1:-}"
if [[ -z "$SUBJECT" ]]; then
  echo "Usage: subject-to-branch.sh \"type(scope): summary\"" >&2
  exit 1
fi

SUBJECT="$(printf '%s\n' "$SUBJECT" | head -n 1 | tr -d '\r')"

# type(scope)!: summary  OR  type: summary
if [[ "$SUBJECT" =~ ^([a-z]+)(\(([a-z0-9._/-]+)\))?(!)?:[[:space:]]+(.+)$ ]]; then
  TYPE="${BASH_REMATCH[1]}"
  SCOPE="${BASH_REMATCH[3]:-}"
  SUMMARY="${BASH_REMATCH[5]}"
else
  echo "ERROR: not a Conventional Commits subject: $SUBJECT" >&2
  exit 1
fi

slugify() {
  printf '%s' "$1" \
    | tr '[:upper:]' '[:lower:]' \
    | sed -E 's/[^a-z0-9]+/-/g; s/^-+//; s/-+$//; s/-+/-/g' \
    | cut -c1-60
}

SUMMARY_SLUG="$(slugify "$SUMMARY")"
if [[ -n "$SCOPE" ]]; then
  SCOPE_SLUG="$(slugify "$SCOPE")"
  BRANCH="${TYPE}/${SCOPE_SLUG}-${SUMMARY_SLUG}"
else
  BRANCH="${TYPE}/${SUMMARY_SLUG}"
fi

# collapse accidental double slashes / trailing dash
BRANCH="$(printf '%s' "$BRANCH" | sed -E 's#/+#/#g; s/-$//')"
printf '%s\n' "$BRANCH"
