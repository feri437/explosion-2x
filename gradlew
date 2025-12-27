#!/usr/bin/env sh
# Minimal Gradle wrapper script for AndroidIDE/Termux. It will download Gradle if needed.
DIR="$(cd "$(dirname "$0")" && pwd)"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$DIR/.gradle}"
export GRADLE_USER_HOME

WRAPPER_PROPS="$DIR/gradle/wrapper/gradle-wrapper.properties"
DIST_URL=$(grep -E '^distributionUrl=' "$WRAPPER_PROPS" | cut -d'=' -f2-)
DIST_URL=$(printf "%b" "$DIST_URL")
DIST_NAME=$(basename "$DIST_URL")
DIST_DIR="$GRADLE_USER_HOME/wrapper/dists/${DIST_NAME%.zip}"

if [ ! -d "$DIST_DIR" ]; then
  mkdir -p "$DIST_DIR"
  echo "Downloading Gradle: $DIST_URL" >&2
  curl -L "$DIST_URL" -o "$DIST_DIR/$DIST_NAME" || wget -O "$DIST_DIR/$DIST_NAME" "$DIST_URL"
  (cd "$DIST_DIR" && unzip -q "$DIST_NAME")
fi

GRADLE_BIN=$(find "$DIST_DIR" -maxdepth 2 -type f -name gradle | head -n 1)
exec "$GRADLE_BIN" "$@"
