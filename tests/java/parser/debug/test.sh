#!/bin/bash

source ../../../bin/settings.sh

for v in *.xcc
do
	echo testing $v

	INPUT=test.input
	OUTPUT=${v%.xcc}.output
	DEBUGOUTPUT=${v%.xcc}.debugoutput

	cookcc -debug $v
	compile $v Lexer.java
	run Lexer $v $INPUT $OUTPUT 2>debugoutput
	diff debugoutput $DEBUGOUTPUT || testerror $v

	rm -f Lexer.java
	rm -f Lexer*.class
	rm -f output debugoutput
done
