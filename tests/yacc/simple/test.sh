#!/bin/bash

source ../../bin/settings.sh

rm -f output
# Natural order (G1, G2, ..., G16), so the output matches test.output
# regardless of the locale's sort order.
for v in $(ls *.y | sort -V)
do
	echo testing $v
	echo $v :>>output
	"$java" $COOKCC_JVM_OPTS -jar "${COOKCC}" $v > /dev/null 2>> output
done

diff output test.output > /dev/null || error test failed

rm -f Lexer.java
rm -f output
