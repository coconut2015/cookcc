#!/bin/bash

source ../../bin/settings.sh

for v in Calculator*.java
do
	echo testing $v

	cp Parser.java.orig Parser.java

	apt $v
	compile $v $v
	run2 ${v%.java} $v test.input ${v%.java}.output

	rm -f Parser.java
	rm -f *.class
	rm -f output
done
