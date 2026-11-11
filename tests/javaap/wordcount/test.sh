#!/bin/bash

source ../../bin/settings.sh

for v in WC*.java
do
	echo testing $v

	num=`echo $v | cut -c3`

	cp Lexer$num.java.orig Lexer$num.java

	apt $v
	compile $v $v
	run WC$num $v ../../java/lexer/fastwc/test.input test.output

	rm -f Lexer$num.java
	rm -f *.class
	rm -f output
done
