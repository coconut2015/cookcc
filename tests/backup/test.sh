#!/bin/sh

cookcc="${JAVA_HOME}/java -jar ../../dist/cookcc-1.0.jar"

for v in *.xcc
do
	echo testing $v

	$cookcc $v > Lexer.java
	${JAVA_HOME}/javac Lexer.java > /dev/null 2> /dev/null
	if [ $? -ne 0 ]; then echo test for $v failed; break; fi

	${JAVA_HOME}/java -cp . Lexer test.input > output
	if [ $? -ne 0 ]; then echo test for $v failed; break; fi
	diff output test.output > /dev/null
	if [ $? -ne 0 ]; then echo test for $v failed; break; fi

done

rm -f Lexer.java
rm -f Lexer*.class
rm -f output
