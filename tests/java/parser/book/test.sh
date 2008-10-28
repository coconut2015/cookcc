#!/bin/sh

if [ -z "$JAVA_HOME" ]; then
	echo need to set JAVA_HOME env
	exit 1
fi

if [ -z "$COOKCC" ]; then
	echo need to set COOKCC
	exit 1
fi

cookcc="${JAVA_HOME}/java -jar ${COOKCC}"

for v in *.xcc
do
	echo testing $v

	$cookcc $v
	${JAVA_HOME}/javac Lexer.java > /dev/null 2> /dev/null
	if [ $? -ne 0 ]; then echo test for $v failed; exit 1; fi

	${JAVA_HOME}/java -cp . Lexer ${v}.input > output
	if [ $? -ne 0 ]; then echo test for $v failed; exit 1; fi
	diff output ${v}.output > /dev/null
	if [ $? -ne 0 ]; then echo test for $v failed; exit 1; fi

	rm -f Lexer.java
	rm -f Lexer*.class
	rm -f output
done
