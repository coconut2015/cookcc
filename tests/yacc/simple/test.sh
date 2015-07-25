#!/bin/bash

source ../../bin/settings.sh

rm -f output
for v in *.y
do
	echo testing $v
	echo $v :>>output
	cookcc $v > /dev/null 2>> output
done

diff output test.output > /dev/null || error test failed

rm -f Lexer.java
rm -f output
