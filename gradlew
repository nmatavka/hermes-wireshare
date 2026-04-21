#!/bin/sh

set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname "$0")" && pwd)

exec "$ROOT_DIR/jlibtorrent/gradlew" -p "$ROOT_DIR" "$@"
