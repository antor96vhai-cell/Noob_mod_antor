#!/usr/bin/env bash
# Noob_launchar 1.8.9 (Forge) — manual build pipeline.
#
# Why manual: the stock ForgeGradle 2.1 route cannot run in this sandbox
# (all Java TCP is blocked by the sandbox's other_tcp=Deny permission, so
# Gradle cannot download anything). This script reproduces exactly what
# ForgeGradle's setupDevWorkspace + reobf do, using curl for downloads and
# local-only Java tools (SpecialSource + javac) for the rest:
#
#   MC client+server jars (sha1-verified) --joined.srg--> SRG names
#   Forge userdev classes.jar            --joined.srg--> SRG names
#   merged SRG jar --srg2mcp.srg (from MCP snapshot CSVs)--> MCP names
#   javac the mod against the MCP jar, then reobfuscate MCP -> SRG
#   (the distribution form every 1.8.9 Forge mod ships in).
#
# Requires: JDK 8 at ~/workspace/jdk8 (Temurin), curl, python3, unzip.
set -euo pipefail
cd "$(dirname "$0")"

JDK="${JDK8:-$HOME/workspace/jdk8}"
M=manual
FORGE_MAVEN="https://maven.minecraftforge.net"
MC_VERSION="1.8.9"
FORGE_VERSION="1.8.9-11.15.1.2318-1.8.9"
SNAPSHOT="20160204"

mkdir -p "$M/libs" "$M/tools" "$M/mappings" "$M/work/mcpdata"

dl() { # dl <url> <dest> — skip if present and non-trivial
    if [ ! -s "$2" ]; then echo "Downloading $2"; curl -sL -o "$2" "$1"; fi
}

# ---------------------------------------------------------------- downloads
dl "$FORGE_MAVEN/net/minecraftforge/forge/$FORGE_VERSION/forge-$FORGE_VERSION-userdev.jar" "$M/work/userdev.jar"
dl "$FORGE_MAVEN/de/oceanlabs/mcp/mcp/$MC_VERSION/mcp-$MC_VERSION-srg.zip" "$M/mappings/mcp-srg.zip"
dl "$FORGE_MAVEN/de/oceanlabs/mcp/mcp_snapshot/$SNAPSHOT-$MC_VERSION/mcp_snapshot-$SNAPSHOT-$MC_VERSION.zip" "$M/mappings/mcp_snapshot-$SNAPSHOT.zip"
dl "https://repo1.maven.org/maven2/net/md-5/SpecialSource/1.8.6/SpecialSource-1.8.6-shaded.jar" "$M/tools/SpecialSource.jar"
dl "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json" "$M/work/version_manifest.json"

VJSON_URL=$(python3 -c "
import json
m = json.load(open('$M/work/version_manifest.json'))
print([v['url'] for v in m['versions'] if v['id'] == '$MC_VERSION'][0])")
dl "$VJSON_URL" "$M/work/$MC_VERSION.json"
CLIENT_URL=$(python3 -c "import json; print(json.load(open('$M/work/$MC_VERSION.json'))['downloads']['client']['url'])")
SERVER_URL=$(python3 -c "import json; print(json.load(open('$M/work/$MC_VERSION.json'))['downloads']['server']['url'])")
dl "$CLIENT_URL" "$M/work/minecraft-$MC_VERSION-client.jar"
dl "$SERVER_URL" "$M/work/minecraft-$MC_VERSION-server.jar"
echo "Client sha1 (expect $(python3 -c "import json; print(json.load(open('$M/work/$MC_VERSION.json'))['downloads']['client']['sha1'])")):"
sha1sum "$M/work/minecraft-$MC_VERSION-client.jar"

LIB="https://libraries.minecraft.net"
dl "$LIB/org/lwjgl/lwjgl/lwjgl/2.9.4-nightly-20150209/lwjgl-2.9.4-nightly-20150209.jar" "$M/libs/lwjgl-2.9.4-nightly-20150209.jar"
dl "$LIB/org/lwjgl/lwjgl/lwjgl_util/2.9.4-nightly-20150209/lwjgl_util-2.9.4-nightly-20150209.jar" "$M/libs/lwjgl_util-2.9.4-nightly-20150209.jar"
dl "$LIB/com/google/code/gson/gson/2.2.4/gson-2.2.4.jar" "$M/libs/gson-2.2.4.jar"
dl "$LIB/com/google/guava/guava/17.0/guava-17.0.jar" "$M/libs/guava-17.0.jar"
dl "$LIB/org/apache/logging/log4j/log4j-api/2.0-beta9/log4j-api-2.0-beta9.jar" "$M/libs/log4j-api-2.0-beta9.jar"
dl "$LIB/org/apache/commons/commons-lang3/3.3.2/commons-lang3-3.3.2.jar" "$M/libs/commons-lang3-3.3.2.jar"
dl "$LIB/com/mojang/authlib/1.5.21/authlib-1.5.21.jar" "$M/libs/authlib-1.5.21.jar"
dl "$LIB/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar" "$M/libs/launchwrapper-1.12.jar"
dl "https://repo1.maven.org/maven2/org/ow2/asm/asm-all/5.0.3/asm-all-5.0.3.jar" "$M/libs/asm-all-5.0.3.jar"

# ---------------------------------------------------------------- mapping data
(cd "$M/work/mcpdata" && unzip -qo "../../mappings/mcp-srg.zip" joined.srg && unzip -qo "../../mappings/mcp_snapshot-$SNAPSHOT.zip")
python3 "$M/mappings/generate_srg2mcp.py" "$M/work/mcpdata/joined.srg" "$M/work/mcpdata/fields.csv" "$M/work/mcpdata/methods.csv" "$M/mappings"

# ---------------------------------------------------------------- remap chain
python3 "$M/merge_jars.py" "$M/work/joined-obf.jar" "$M/work/minecraft-$MC_VERSION-server.jar" "$M/work/minecraft-$MC_VERSION-client.jar" | tail -1
"$JDK/bin/java" -jar "$M/tools/SpecialSource.jar" --in-jar "$M/work/joined-obf.jar" --out-jar "$M/work/joined-srg.jar" --srg-in "$M/work/mcpdata/joined.srg" | tail -1
mkdir -p "$M/work/userdev-x" && (cd "$M/work/userdev-x" && unzip -qo ../userdev.jar)
"$JDK/bin/java" -jar "$M/tools/SpecialSource.jar" --in-jar "$M/work/userdev-x/classes.jar" --out-jar "$M/work/forge-classes-srg.jar" --srg-in "$M/work/mcpdata/joined.srg" | tail -1
python3 "$M/merge_jars.py" "$M/work/merged-srg.jar" "$M/work/joined-srg.jar" "$M/work/forge-classes-srg.jar" | tail -1
"$JDK/bin/java" -jar "$M/tools/SpecialSource.jar" --in-jar "$M/work/merged-srg.jar" --out-jar "$M/libs/minecraft-1.8.9-forge-mcp.jar" --srg-in "$M/mappings/srg2mcp.srg" | tail -1

# ---------------------------------------------------------------- compile the mod
rm -rf classes && mkdir -p classes
find src/main/java -name '*.java' | sort > sources.txt
CP="$(find "$M/libs" -name '*.jar' | sort | tr '\n' ':')"
"$JDK/bin/javac" -encoding UTF-8 -cp "$CP" -d classes @sources.txt
cp -r src/main/resources/. classes/

# ---------------------------------------------------------------- reobfuscate (MCP -> SRG) and package
"$JDK/bin/jar" cf "$M/work/mod-mcp.jar" -C classes .
python3 "$M/merge_jars.py" "$M/work/context.jar" "$M/libs/minecraft-1.8.9-forge-mcp.jar" "$M/work/mod-mcp.jar" | tail -1
"$JDK/bin/java" -jar "$M/tools/SpecialSource.jar" --in-jar "$M/work/context.jar" --out-jar "$M/work/context-srg.jar" --srg-in "$M/mappings/mcp2srg.srg" | tail -1
rm -rf "$M/work/final" && mkdir -p "$M/work/final"
(cd "$M/work/final" && unzip -qo ../context-srg.jar 'nooblaunchar/*' mcmod.info)
"$JDK/bin/jar" cf Noob_launchar-1.0.0.jar -C "$M/work/final" .

echo "Built: $(pwd)/Noob_launchar-1.0.0.jar ($(stat -c%s Noob_launchar-1.0.0.jar) bytes)"
