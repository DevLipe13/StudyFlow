#!/usr/bin/env bash
# Validate a Conventional Commits message (first line / subject).
# Usage:
#   validate-message.sh "feat(auth): add login guard"
#   echo "feat: message" | validate-message.sh
# Exit 0 = valid, 1 = invalid

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
EXT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
CONFIG="$EXT_DIR/conventional-commits-config.yml"
TEMPLATE="$EXT_DIR/conventional-commits-config.template.yml"

if [[ -f "$CONFIG" ]]; then
  CONFIG_FILE="$CONFIG"
elif [[ -f "$TEMPLATE" ]]; then
  CONFIG_FILE="$TEMPLATE"
else
  CONFIG_FILE=""
fi

DEFAULT_TYPES="feat|fix|docs|refactor|test|chore|style|perf|ci|build"
SUBJECT_MAX=72

if [[ -n "$CONFIG_FILE" ]] && command -v python3 >/dev/null 2>&1; then
  PARSED="$(python3 - "$CONFIG_FILE" <<'PY'
import re, sys
path = sys.argv[1]
text = open(path, encoding="utf-8").read()
types = re.findall(r"^\s*-\s*([a-z]+)\s*$", text, flags=re.M)
# Prefer types under a types: block — take first contiguous list after "types:"
m = re.search(r"(?m)^types:\s*\n((?:\s*-\s*[a-z]+\s*\n)+)", text)
if m:
    types = re.findall(r"-\s*([a-z]+)", m.group(1))
max_m = re.search(r"(?m)^subject_max_length:\s*(\d+)", text)
print("|".join(types) if types else "")
print(max_m.group(1) if max_m else "")
PY
)"
  TYPES_LINE="$(printf '%s\n' "$PARSED" | sed -n '1p')"
  MAX_LINE="$(printf '%s\n' "$PARSED" | sed -n '2p')"
  [[ -n "$TYPES_LINE" ]] && DEFAULT_TYPES="$TYPES_LINE"
  [[ -n "$MAX_LINE" ]] && SUBJECT_MAX="$MAX_LINE"
fi

if [[ $# -ge 1 ]]; then
  MESSAGE="$*"
else
  MESSAGE="$(cat)"
fi

SUBJECT="$(printf '%s\n' "$MESSAGE" | head -n 1 | tr -d '\r')"

if [[ -z "${SUBJECT// }" ]]; then
  echo "ERROR: empty commit subject" >&2
  exit 1
fi

# type(scope)!: summary  OR  type: summary
PATTERN="^(${DEFAULT_TYPES})(\\([a-z0-9._/-]+\\))?(!)?:[[:space:]]+.+$"

if [[ ! "$SUBJECT" =~ $PATTERN ]]; then
  echo "ERROR: subject does not match Conventional Commits" >&2
  echo "  got:  $SUBJECT" >&2
  echo "  need: type(optional-scope)!: summary" >&2
  echo "  types: ${DEFAULT_TYPES//|/, }" >&2
  exit 1
fi

if ((${#SUBJECT} > SUBJECT_MAX)); then
  echo "ERROR: subject length ${#SUBJECT} > max ${SUBJECT_MAX}" >&2
  exit 1
fi

if [[ "$SUBJECT" == *. ]]; then
  echo "ERROR: subject must not end with a period" >&2
  exit 1
fi

echo "OK: valid Conventional Commit subject"
exit 0
