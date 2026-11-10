#!/usr/bin/env bash
#
# Build script for CookCC.  Run "./make.sh help" for usage.
#
set -euo pipefail

cd "$(dirname "$0")"

usage ()
{
	cat <<'EOF'
Usage: ./make.sh <command> [options] [extra Maven arguments]

Commands:
  package     Build all modules, run the unit tests, generate the javadoc,
              and collect the jars in dist/.
  docs        Build the HTML documentation in docs_src/_build/html.
  full        Build everything: package, then docs.
  help        Show this help.

Options for package and full:
  --skip-tests  Do not run the unit tests.

Any other arguments are passed to Maven, for example -o for an offline build.

Examples:
  ./make.sh package
  ./make.sh package --skip-tests
  ./make.sh full -o

Requirements: JDK 8+ and Maven 3.6.3+ for package; Python 3 with the
packages in docs_src/requirements.txt for docs.
EOF
}

error ()
{
	echo "error: $*" >&2
	exit 1
}

build_package ()
{
	command -v mvn > /dev/null 2>&1 || error "mvn was not found on PATH.  Install Maven 3.6.3 or later."

	# Remove jars left over from earlier builds; mvn clean does not touch dist/.
	mkdir -p dist
	rm -f dist/*.jar

	# ${array[@]+...} keeps an empty array from failing with "set -u" in old bash.
	mvn clean package javadoc:aggregate-jar ${mvn_args[@]+"${mvn_args[@]}"}

	# The aggregated javadoc jar is named after the parent project
	# (cookcc-parent-<version>-javadoc.jar); publish it as cookcc-<version>-javadoc.jar.
	local found=0 f name
	for f in target/cookcc-parent-*-javadoc.jar; do
		[ -e "$f" ] || continue
		name=$(basename "$f")
		cp "$f" "dist/${name/cookcc-parent-/cookcc-}"
		found=1
	done
	[ $found -eq 1 ] || error "the aggregated javadoc jar was not found in target/."

	echo
	echo "Package complete.  Output in dist/:"
	ls -1 dist
}

build_docs ()
{
	local sphinx
	if command -v sphinx-build > /dev/null 2>&1; then
		sphinx=(sphinx-build)
	elif python3 -m sphinx --version > /dev/null 2>&1; then
		sphinx=(python3 -m sphinx)
	elif python -m sphinx --version > /dev/null 2>&1; then
		sphinx=(python -m sphinx)
	else
		error "Sphinx was not found.  Install it with: pip install -r docs_src/requirements.txt"
	fi

	rm -rf docs_src/_build
	# -W turns warnings into errors, the same as the documentation CI build.
	"${sphinx[@]}" -W --keep-going -b html docs_src docs_src/_build/html

	echo
	echo "Documentation complete.  Open docs_src/_build/html/TOC.html"
}

command=${1:-help}
[ $# -gt 0 ] && shift

mvn_args=()
for arg in "$@"; do
	case "$arg" in
		--skip-tests)	mvn_args+=(-DskipTests) ;;
		*)				mvn_args+=("$arg") ;;
	esac
done

case "$command" in
	help|-h|--help)
		usage
		;;
	package)
		build_package
		;;
	docs)
		[ $# -eq 0 ] || error "docs does not take any options."
		build_docs
		;;
	full)
		build_package
		echo
		build_docs
		;;
	*)
		echo "error: unknown command: $command" >&2
		echo >&2
		usage >&2
		exit 1
		;;
esac
