#!/bin/bash

source ../../bin/settings.sh

for v in WC?.java
do
	echo testing $v

	CL=${v%.java}

	cp $CL\$Lexer.java.orig $CL\$Lexer.java

	apt $v
	compile $v $v
	run $CL $v ../../java/lexer/fastwc/test.input test.output

	rm -f $CL\$Lexer.java
	rm -f *.class
	rm -f output
done
