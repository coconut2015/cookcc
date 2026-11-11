#!/bin/bash

source ../../../bin/settings.sh

for v in *.xcc
do
	echo testing $v

	INPUT=test.input
	OUTPUT=${v%.xcc}.output

	cookcc $v
	compile $v Lexer.java
	run2 Lexer $v $INPUT $OUTPUT

	rm -f Lexer.java
	rm -f Lexer*.class
	rm -f output
done
