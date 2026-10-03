#!/bin/sh

# Minimal Gradle Wrapper launcher. The executable wrapper implementation lives
# in gradle/wrapper/gradle-wrapper.jar and its distribution is pinned in
# gradle-wrapper.properties.

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd) || exit 1

if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi

if ! command -v "$JAVACMD" >/dev/null 2>&1 && [ ! -x "$JAVACMD" ]; then
    echo "ERROR: Java was not found. Set JAVA_HOME or add java to PATH." >&2
    exit 1
fi

exec "$JAVACMD" ${JAVA_OPTS:-} ${GRADLE_OPTS:-} \
    -Dorg.gradle.appname=gradlew \
    -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
