#!/bin/bash
set -euo pipefail
BASEDIR=$(cd "$(dirname "$0")" && pwd -P)
SOURCE_DIR=${1:-"$BASEDIR/../Whiteboard-bridge/build"}
SOURCE_DIR=$(cd "$SOURCE_DIR" && pwd -P)
test -f "$SOURCE_DIR/index.html"
cd "$BASEDIR"

rm -rf sdk/src/main/assets/whiteboard
mkdir -p sdk/src/main/assets/whiteboard
cp -R "$SOURCE_DIR/." sdk/src/main/assets/whiteboard
