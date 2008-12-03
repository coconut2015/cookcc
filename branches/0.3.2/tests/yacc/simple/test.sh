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

rm -f output
for v in *.y
do
	echo testing $v
	echo $v :>>output
	$cookcc $v > /dev/null 2>> output
done

diff output test.output > /dev/null
if [ $? -ne 0 ]; then echo test failed; exit 1; fi

rm -f Lexer.java
rm -f output
