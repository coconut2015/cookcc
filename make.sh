#!/usr/bin/env bash
#
# Builds all CookCC modules and collects the distribution jars in dist/.
#
# Usage:
#   ./make.sh [extra Maven arguments]
#
# Examples:
#   ./make.sh                 full build, including unit tests
#   ./make.sh -DskipTests     skip the unit tests
#   ./make.sh -o              offline build (dependencies already downloaded)
#
set -euo pipefail

cd "$(dirname "$0")"

if ! command -v mvn > /dev/null 2>&1; then
	echo "error: mvn was not found on PATH.  Install Maven 3.6.3 or later." >&2
	exit 1
fi

# Remove jars left over from earlier builds; mvn clean does not touch dist/.
mkdir -p dist
rm -f dist/*.jar

mvn clean package javadoc:aggregate-jar "$@"

# The aggregated javadoc jar is named after the parent project
# (cookcc-parent-<version>-javadoc.jar); publish it as cookcc-<version>-javadoc.jar.
found=0
for f in target/cookcc-parent-*-javadoc.jar; do
	[ -e "$f" ] || continue
	name=$(basename "$f")
	cp "$f" "dist/${name/cookcc-parent-/cookcc-}"
	found=1
done
if [ $found -eq 0 ]; then
	echo "error: the aggregated javadoc jar was not found in target/." >&2
	exit 1
fi

echo
echo "Build complete.  Output in dist/:"
ls -1 dist
