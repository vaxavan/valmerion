#!/bin/sh
#
# Gradle wrapper script for Unix.
#

# Attempt to set APP_HOME
APP_HOME="${APP_HOME:-$(cd "$(dirname "$0")" && pwd)}"

APP_NAME="Gradle"
APP_BASE_NAME="$(basename "$0")"

# Default JVM options
DEFAULT_JVM_OPTS='-Xmx512m -Xms128m'

# OS specific support (must be 'true' or 'false').
cygwin=false
darwin=false
nonstop=false
case "$(uname)" in
  CYGWIN*)  cygwin=true  ;;
  Darwin*)  darwin=true  ;;
  NONSTOP*) nonstop=true ;;
esac

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ]; then
  if [ -x "$JAVA_HOME/jre/sh/java" ]; then
    JAVACMD="$JAVA_HOME/jre/sh/java"
  else
    JAVACMD="$JAVA_HOME/bin/java"
  fi
  if [ ! -x "$JAVACMD" ]; then
    die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
  fi
else
  JAVACMD=java
  which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
  "-Dorg.gradle.appname=$APP_BASE_NAME" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
