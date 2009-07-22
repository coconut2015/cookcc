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
apt="${JAVA_HOME}/apt -nocompile -cp $classpath -s ."

v=PushInput.java
echo testing $v

cp Lexer.java.orig Lexer.java

$apt $v
${JAVA_HOME}/javac -classpath $classpath $v > /dev/null 2> /dev/null
if [ $? -ne 0 ]; then echo test for $v failed; exit 1; fi

${JAVA_HOME}/java -cp . PushInput test.input > output
if [ $? -ne 0 ]; then echo test for $v failed; exit 1; fi
diff output test.output > /dev/null
if [ $? -ne 0 ]; then echo test for $v failed; exit 1; fi

rm -f Lexer.java
rm -f *.class
rm -f output
