#!/bin/bash

# disable unicode testing for this one
export UNICODE=

source ../../../bin/settings.sh

for v in *.xcc
do
	echo testing $v

	INPUT=test.input
	OUTPUT=${v%.xcc}.output

	cookcc $v
	compile Lexer.java $v
	run Lexer $v $INPUT $OUTPUT

	rm -f Lexer.java
	rm -f Lexer*.class
	rm -f output
done
