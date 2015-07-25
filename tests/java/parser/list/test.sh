#!/bin/bash

source ../../../bin/settings.sh

for v in *.xcc
do
	echo testing $v

	INPUT=${v%.xcc}.input
	OUTPUT=${v%.xcc}.output

	cookcc $v

	"$javac" Lexer.java > /dev/null 2> /dev/null || testerror $v
	# no error check on the following line since we are going to create errors
	"$java" -cp . Lexer $INPUT > output 2>&1
	diff output $OUTPUT > /dev/null || testerror $v

	rm -f Lexer.java
	rm -f Lexer*.class
	rm -f output
done
