#!/bin/sh

function error ()
{
	echo $@ && exit 1
}

test -z "$JAVA_HOME" && error need to set JAVA_HOME env
test -z "$COOKCC" && error need to set COOKCC env

cookcc="${JAVA_HOME}/bin/java -jar ${COOKCC}"

rm -f output
for v in *.y
do
	echo testing $v
	echo $v :>>output
	$cookcc $v > /dev/null 2>> output
done

diff output test.output > /dev/null || error test failed

rm -f Lexer.java
rm -f output
