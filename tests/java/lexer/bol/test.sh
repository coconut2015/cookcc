#!/bin/bash

source ../../../bin/settings.sh

for v in *.xcc
do
	echo testing $v

	INPUT=test.input
	OUTPUT=${v%.xcc}.output

	cookcc $v

	"$javac" Lexer.java > /dev/null 2> /dev/null || testerror $v
	"$java" -cp . Lexer $INPUT > output || testerror $v
	diff output $OUTPUT > /dev/null || testerror $v

	rm -f Lexer.java
	rm -f Lexer*.class
	rm -f output
done
