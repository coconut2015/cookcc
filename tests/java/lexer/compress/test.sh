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

	$cookcc -lang plain -lexertable compressed $v | grep "compressed correctly" > output 2> /dev/null || error test for $v failed
	diff output test.output > /dev/null || error test for $v failed

	rm -f output
done
