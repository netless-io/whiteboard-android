#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
HOOKS_DIR="$ROOT_DIR/.githooks"

git -C "$ROOT_DIR" config core.hooksPath "$HOOKS_DIR"
chmod +x "$HOOKS_DIR/pre-commit"

echo "Configured core.hooksPath to $HOOKS_DIR"
