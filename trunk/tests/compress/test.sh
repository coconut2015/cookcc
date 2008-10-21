#!/bin/sh

cookcc="${JAVA_HOME}/java -jar ../../dist/cookcc-1.0.jar"

for v in *.xcc
do
	echo testing $v

	$cookcc -lang plain -lexertable compressed $v | grep "compressed correctly" > output 2> /dev/null
	if [ $? -ne 0 ]; then echo test for $v failed; break; fi
	diff output test.output > /dev/null
	if [ $? -ne 0 ]; then echo test for $v failed; break; fi

	rm -f output
done
