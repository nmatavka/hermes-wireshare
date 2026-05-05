#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
exec "$SCRIPT_DIR/jre-linux/bin/java" -jar "$SCRIPT_DIR/WireShare.jar" "$@"
