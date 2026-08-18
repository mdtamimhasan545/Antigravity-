#!/usr/bin/env sh
#
# Gradle startup script for UN*X
#

DIRNAME=$(dirname "$0")
APP_HOME=$(cd "$DIRNAME" && pwd)

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

MAX_FD="maximum"

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

if [ "$APP_HOME" = "" ] ; then
    APP_HOME="."
fi

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

JAVACMD="${JAVA_HOME}/bin/java"
if [ ! -x "$JAVACMD" ] ; then
    JAVACMD="java"
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
    "-Dorg.gradle.appname=$APP_BASE_NAME" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"