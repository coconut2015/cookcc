@echo off
rem
rem Builds all CookCC modules and collects the distribution jars in dist\.
rem
rem Usage:
rem   make.cmd [extra Maven arguments]
rem
rem Examples:
rem   make.cmd                 full build, including unit tests
rem   make.cmd -DskipTests     skip the unit tests
rem   make.cmd -o              offline build (dependencies already downloaded)
rem
setlocal EnableDelayedExpansion

cd /d "%~dp0"

where mvn > nul 2> nul
if errorlevel 1 (
	echo error: mvn was not found on PATH.  Install Maven 3.6.3 or later. 1>&2
	exit /b 1
)

rem Remove jars left over from earlier builds; mvn clean does not touch dist\.
if not exist dist mkdir dist
if exist dist\*.jar del /q dist\*.jar

call mvn clean package javadoc:aggregate-jar %*
if errorlevel 1 exit /b 1

rem The aggregated javadoc jar is named after the parent project
rem (cookcc-parent-<version>-javadoc.jar); publish it as cookcc-<version>-javadoc.jar.
set "found="
for %%f in (target\cookcc-parent-*-javadoc.jar) do (
	set "name=%%~nxf"
	copy /y "%%f" "dist\!name:cookcc-parent-=cookcc-!" > nul
	set "found=1"
)
if not defined found (
	echo error: the aggregated javadoc jar was not found in target\. 1>&2
	exit /b 1
)

echo.
echo Build complete.  Output in dist\:
dir /b dist
