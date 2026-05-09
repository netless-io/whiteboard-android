#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SOURCE_XML="$ROOT_DIR/app/src/main/res/values/string_white_sdk_config.xml"
TARGET_DEFAULTS="$ROOT_DIR/sdk/src/androidTest/java/com/herewhite/sdk/local/SdkTestDefaults.java"

if [ ! -f "$SOURCE_XML" ]; then
  echo "Missing source config: $SOURCE_XML" >&2
  exit 1
fi

extract_string() {
  local key="$1"
  sed -n "s:.*<string name=\"$key\">\"\\{0,1\\}\\([^\"]*\\)\"\\{0,1\\}</string>.*:\\1:p" "$SOURCE_XML" | head -n 1
}

APP_ID="$(extract_string sdk_app_id)"
ROOM_UUID="$(extract_string room_uuid)"
ROOM_TOKEN="$(extract_string room_token)"

cat > "$TARGET_DEFAULTS" <<EOF
package com.herewhite.sdk.local;

public final class SdkTestDefaults {
    public static final String DEFAULT_APP_IDENTIFIER = "$APP_ID";
    public static final String DEFAULT_ROOM_UUID = "$ROOM_UUID";
    public static final String DEFAULT_ROOM_TOKEN = "$ROOM_TOKEN";

    private SdkTestDefaults() {
    }
}
EOF

echo "Updated $TARGET_DEFAULTS from $SOURCE_XML"
