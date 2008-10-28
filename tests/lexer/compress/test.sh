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

	$cookcc -lang plain -lexertable compressed $v | grep "compressed correctly" > output 2> /dev/null
	if [ $? -ne 0 ]; then echo test for $v failed; break; fi
	diff output test.output > /dev/null
	if [ $? -ne 0 ]; then echo test for $v failed; break; fi

	rm -f output
done
