#!/usr/bin/env bash
# KUTU Client — manual build (no Gradle/Loom), mirroring the autovault-port setup.
# Compiles against the jars in ~/workspace/autovault-port/manual/libs and
# packages classes + resources into Noob_launchar-1.0.0_mc26.2.jar.
set -euo pipefail
cd "$(dirname "$0")"

JDK="$HOME/workspace/jdk25"
LIBS="$HOME/workspace/autovault-port/manual/libs"
CP="$(find "$LIBS" -name '*.jar' | sort | tr '\n' ':')"

rm -rf classes
mkdir -p classes
find src -name '*.java' | sort > sources.txt

"$JDK/bin/javac" -encoding UTF-8 -cp "$CP" -d classes @sources.txt
cp -r resources/. classes/

"$JDK/bin/jar" --create --file Noob_launchar-1.0.0_mc26.2.jar -C classes .
echo "Built: $(pwd)/Noob_launchar-1.0.0_mc26.2.jar ($(stat -c%s Noob_launchar-1.0.0_mc26.2.jar) bytes)"
