#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
TARGET_FILE="$ROOT_DIR/sdk/src/androidTest/java/com/herewhite/sdk/local/SdkTestDefaults.java"

if [ ! -f "$TARGET_FILE" ]; then
  echo "Missing file: $TARGET_FILE" >&2
  exit 1
fi

APP_ID_LINE="$(sed -n 's/.*DEFAULT_APP_IDENTIFIER = "\(.*\)";/\1/p' "$TARGET_FILE" | head -n 1)"
ROOM_UUID_LINE="$(sed -n 's/.*DEFAULT_ROOM_UUID = "\(.*\)";/\1/p' "$TARGET_FILE" | head -n 1)"
ROOM_TOKEN_LINE="$(sed -n 's/.*DEFAULT_ROOM_TOKEN = "\(.*\)";/\1/p' "$TARGET_FILE" | head -n 1)"

if [ -n "${APP_ID_LINE}" ] || [ -n "${ROOM_UUID_LINE}" ] || [ -n "${ROOM_TOKEN_LINE}" ]; then
  cat >&2 <<EOF
Refusing to proceed: sdk test fallback defaults contain non-empty local credentials.

Please reset:
  $TARGET_FILE

Expected:
  DEFAULT_APP_IDENTIFIER = ""
  DEFAULT_ROOM_UUID = ""
  DEFAULT_ROOM_TOKEN = ""

If you need local right-click testing, rerun:
  bash ./scripts/sync-sdk-test-config.sh

But do not commit those local values.
EOF
  exit 1
fi

echo "SdkTestDefaults.java is clean."
