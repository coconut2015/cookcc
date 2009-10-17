#!/bin/sh

if [ -z "$JAVA_HOME" ]; then
	echo need to set JAVA_HOME env
	exit 1
fi

if [ -z "$COOKCC" ]; then
	echo need to set COOKCC
	exit 1
fi

classpath="${COOKCC};."
apt="${JAVA_HOME}/bin/apt -nocompile -cp $classpath -s ."

v=Calculator.java
echo testing $v

cp Parser.java.orig Parser.java

$apt $v
${JAVA_HOME}/bin/javac -classpath $classpath $v > /dev/null 2> /dev/null
if [ $? -ne 0 ]; then echo test for $v failed; exit 1; fi

${JAVA_HOME}/bin/java -cp . Calculator test.input > output
if [ $? -ne 0 ]; then echo test for $v failed; exit 1; fi
diff output test.output > /dev/null
if [ $? -ne 0 ]; then echo test for $v failed; exit 1; fi

rm -f Parser.java
rm -f *.class
rm -f output
