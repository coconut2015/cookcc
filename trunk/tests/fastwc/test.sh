#!/bin/sh

cookcc="${JAVA_HOME}/java -jar ../../dist/cookcc-1.0.jar"

for v in *.xcc
do
	echo testing $v

	$cookcc $v > Lexer.java
	${JAVA_HOME}/javac Lexer.java > /dev/null 2> /dev/null
	if [ $? -ne 0 ]; then echo test for $v failed; fi

	${JAVA_HOME}/java -cp . Lexer bible12.txt > output
	if [ $? -ne 0 ]; then echo test for $v failed; fi
done
