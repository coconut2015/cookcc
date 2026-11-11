#!/bin/bash

source ../../../bin/settings.sh

for v in *.xcc
do
	echo testing $v

	INPUT=test.input
	OUTPUT=test.output

	"$java" $COOKCC_JVM_OPTS -jar "${COOKCC}" -lang plain -lexertable compressed $v | grep "compressed correctly" > output 2> /dev/null || testerror $v
	diff output $OUTPUT > /dev/null || testerror $v

	rm -f output
done
