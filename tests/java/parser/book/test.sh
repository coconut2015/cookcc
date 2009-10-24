#!/bin/sh

function error ()
{
	echo $@ && exit 1
}

test -z "$JAVA_HOME" && error need to set JAVA_HOME env
test -z "$COOKCC" && error need to set COOKCC env

cookcc="${JAVA_HOME}/bin/java -jar ${COOKCC}"

for v in *.xcc
do
	echo testing $v

	$cookcc $v
	${JAVA_HOME}/bin/javac Lexer.java > /dev/null 2> /dev/null || error test for $v failed
	${JAVA_HOME}/bin/java -cp . Lexer ${v}.input > output || error test for $v failed
	diff output ${v}.output > /dev/null || error test for $v failed

	rm -f Lexer.java
	rm -f Lexer*.class
	rm -f output
done
