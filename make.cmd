@echo off
rem
rem Build script for CookCC.  Run "make.cmd help" for usage.
rem
setlocal EnableDelayedExpansion

cd /d "%~dp0"

set "COMMAND=%~1"
if "%COMMAND%"=="" goto help
if /i "%COMMAND%"=="help" goto help
if "%COMMAND%"=="-h" goto help
if "%COMMAND%"=="--help" goto help
if "%COMMAND%"=="/?" goto help
shift

rem Collect the options.  --skip-tests becomes -DskipTests; everything else goes to Maven.
set "MVN_ARGS="
set "HAS_ARGS="
:parse
if "%~1"=="" goto parsed
set "HAS_ARGS=1"
if /i "%~1"=="--skip-tests" (
	set "MVN_ARGS=!MVN_ARGS! -DskipTests"
) else (
	set "MVN_ARGS=!MVN_ARGS! %1"
)
shift
goto parse
:parsed

rem Dispatch with goto: in cmd, "exit /b 1" inside nested parentheses can lose
rem its exit code, so the commands below avoid nested blocks.
if /i "%COMMAND%"=="package" goto cmd_package
if /i "%COMMAND%"=="docs" goto cmd_docs
if /i "%COMMAND%"=="full" goto cmd_full

echo error: unknown command: %COMMAND% 1>&2
echo. 1>&2
call :usage 1>&2
exit /b 1

:cmd_package
call :build_package
exit /b %errorlevel%

:cmd_docs
if defined HAS_ARGS goto docs_has_args
call :build_docs
exit /b %errorlevel%
:docs_has_args
echo error: docs does not take any options. 1>&2
exit /b 1

:cmd_full
call :build_package
if errorlevel 1 exit /b 1
echo.
call :build_docs
exit /b %errorlevel%

:help
call :usage
exit /b 0

rem ---------------------------------------------------------------------------

:usage
echo Usage: make.cmd ^<command^> [options] [extra Maven arguments]
echo.
echo Commands:
echo   package     Build all modules, run the unit tests, generate the javadoc,
echo               and collect the jars in dist\.
echo   docs        Build the HTML documentation in docs_src\_build\html.
echo   full        Build everything: package, then docs.
echo   help        Show this help.
echo.
echo Options for package and full:
echo   --skip-tests  Do not run the unit tests.
echo.
echo Any other arguments are passed to Maven, for example -o for an offline build.
echo Quote arguments that contain "=", for example "-Dmaven.test.failure.ignore=true".
echo.
echo Examples:
echo   make.cmd package
echo   make.cmd package --skip-tests
echo   make.cmd full -o
echo.
echo Requirements: JDK 8+ and Maven 3.6.3+ for package; Python 3 with the
echo packages in docs_src\requirements.txt for docs.
exit /b 0

:build_package
where mvn > nul 2> nul
if errorlevel 1 (
	echo error: mvn was not found on PATH.  Install Maven 3.6.3 or later. 1>&2
	exit /b 1
)

rem Remove jars left over from earlier builds; mvn clean does not touch dist\.
if not exist dist mkdir dist
if exist dist\*.jar del /q dist\*.jar

call mvn clean package javadoc:aggregate-jar!MVN_ARGS!
if errorlevel 1 exit /b 1

rem The aggregated javadoc jar is named after the parent project
rem (cookcc-parent-<version>-javadoc.jar); publish it as cookcc-<version>-javadoc.jar.
set "FOUND="
for %%f in (target\cookcc-parent-*-javadoc.jar) do (
	set "NAME=%%~nxf"
	copy /y "%%f" "dist\!NAME:cookcc-parent-=cookcc-!" > nul
	set "FOUND=1"
)
if not defined FOUND (
	echo error: the aggregated javadoc jar was not found in target\. 1>&2
	exit /b 1
)

echo.
echo Package complete.  Output in dist\:
dir /b dist
exit /b 0

:build_docs
set "SPHINX="
where sphinx-build > nul 2> nul
if not errorlevel 1 (
	set "SPHINX=sphinx-build"
) else (
	python -m sphinx --version > nul 2> nul
	if not errorlevel 1 (
		set "SPHINX=python -m sphinx"
	) else (
		py -3 -m sphinx --version > nul 2> nul
		if not errorlevel 1 set "SPHINX=py -3 -m sphinx"
	)
)
if not defined SPHINX (
	echo error: Sphinx was not found.  Install it with: pip install -r docs_src\requirements.txt 1>&2
	exit /b 1
)

if exist docs_src\_build rmdir /s /q docs_src\_build
rem -W turns warnings into errors, the same as the documentation CI build.
%SPHINX% -W --keep-going -b html docs_src docs_src\_build\html
if errorlevel 1 exit /b 1

echo.
echo Documentation complete.  Open docs_src\_build\html\TOC.html
exit /b 0
