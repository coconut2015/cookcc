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
  test        Run the integration tests in tests/ against the jars in dist/.
              Run package first.
  docs        Build the HTML documentation in docs_src/_build/html.
  full        Build everything: package, then docs.
  help        Show this help.

Options for package and full:
  --skip-tests  Do not run the unit tests.

Options for test:
  --coverage    Record JaCoCo code coverage of CookCC while the tests run, and
                write the report to target/site/jacoco-it.

Any other arguments to package and full are passed to Maven, for example -o
for an offline build.

Examples:
  ./make.sh package
  ./make.sh package --skip-tests
  ./make.sh package && ./make.sh test
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

run_tests ()
{
	# The suite runs the jars that package puts in dist/.
	local cookcc="" cookcc_rt="" f
	for f in dist/cookcc-[0-9]*.jar; do
		case "$f" in
			*-sources.jar|*-javadoc.jar) ;;
			*) if [ -e "$f" ]; then cookcc=$(pwd)/$f; fi ;;
		esac
	done
	for f in dist/cookcc-rt-[0-9]*.jar; do
		if [ -e "$f" ]; then cookcc_rt=$(pwd)/$f; fi
	done
	if [ -z "$cookcc" ] || [ -z "$cookcc_rt" ]; then
		error "the CookCC jars were not found in dist/.  Run ./make.sh package first."
	fi

	# The test scripts run $JAVA_HOME/bin/java and javac.
	if [ -z "${JAVA_HOME:-}" ]; then
		local javac
		javac=$(command -v javac) || error "JAVA_HOME is not set and javac was not found on PATH."
		JAVA_HOME=$(dirname "$(dirname "$(readlink -f "$javac")")")
		export JAVA_HOME
	fi

	local exec=""
	if [ "$coverage" = 1 ]; then
		setup_coverage
		exec=$(pwd)/target/jacoco-it.exec
		rm -f "$exec"
		# The test scripts pass this to each JVM that runs CookCC.  Coverage
		# from all of them is appended to the same file.
		export COOKCC_JVM_OPTS="-javaagent:$(pwd)/target/jacoco/org.jacoco.agent-runtime.jar=destfile=$exec,includes=org.yuanheng.*:freemarker.core.*"
	fi

	echo "Running tests/ with $cookcc"
	(cd tests && COOKCC=$cookcc COOKCC_RT=$cookcc_rt ./test.sh)

	if [ "$coverage" = 1 ]; then
		report_coverage "$exec"
	fi
}

# Downloads the JaCoCo agent and command line tool into target/jacoco.
setup_coverage ()
{
	case "$(pwd)" in
		*" "*) error "--coverage does not work in a directory whose path contains spaces." ;;
	esac
	command -v mvn > /dev/null 2>&1 || error "mvn was not found on PATH.  It is needed to download JaCoCo."

	# Same JaCoCo version as the unit test coverage in pom.xml.
	local version
	version=$(sed -n 's:.*<jacoco.version>\(.*\)</jacoco.version>.*:\1:p' pom.xml)
	[ -n "$version" ] || error "jacoco.version was not found in pom.xml."

	local artifact
	for artifact in org.jacoco.agent:runtime org.jacoco.cli:nodeps; do
		mvn -q -N dependency:copy -Dartifact="org.jacoco:${artifact%%:*}:$version:jar:${artifact##*:}" \
			-DoutputDirectory=target/jacoco -Dmdep.stripVersion=true
	done
}

# Writes the coverage report for cookcc-core and cookcc-ap.  The classes in
# the tested jar must be the same build as the module target/classes, so run
# package and test together.
report_coverage ()
{
	local exec=$1 module args=()
	[ -f "$exec" ] || error "no coverage data was recorded in $exec."
	for module in cookcc-core cookcc-ap; do
		[ -d "$module/target/classes" ] || error "$module/target/classes was not found.  Run ./make.sh package first."
		args+=(--classfiles "$module/target/classes" --sourcefiles "$module/src/main/java")
	done

	mkdir -p target/site/jacoco-it
	"$JAVA_HOME/bin/java" -jar target/jacoco/org.jacoco.cli-nodeps.jar report "$exec" "${args[@]}" \
		--name "CookCC integration tests" --xml target/site/jacoco-it/jacoco.xml --html target/site/jacoco-it

	echo
	echo "Coverage report: target/site/jacoco-it/index.html (XML: target/site/jacoco-it/jacoco.xml)"
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
	test)
		coverage=0
		for arg in "$@"; do
			case "$arg" in
				--coverage)	coverage=1 ;;
				*)			error "unknown option for test: $arg" ;;
			esac
		done
		run_tests
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
