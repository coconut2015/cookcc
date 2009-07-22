#!/bin/sh

if [ -z "$JAVA_HOME" ]; then
	echo need to set JAVA_HOME env
	exit 1
fi

if [ -z "$COOKCC" ]; then
	echo need to set COOKCC
	exit 1
fi

classpath="${COOKCC};."
apt="${JAVA_HOME}/apt -nocompile -cp $classpath -s ."

for v in WC?.java
do
	echo testing $v

	CL=`echo $v | cut -d. -f1`

	cp $CL\$Lexer.java.orig $CL\$Lexer.java

	$apt $v
	${JAVA_HOME}/javac -classpath $classpath $v > /dev/null 2> /dev/null
	if [ $? -ne 0 ]; then echo test for $v failed; exit 1; fi

	${JAVA_HOME}/java -cp . $CL ../../java/lexer/fastwc/test.input > output
	if [ $? -ne 0 ]; then echo test for $v failed; exit 1; fi
	diff output test.output > /dev/null
	if [ $? -ne 0 ]; then echo test for $v failed; exit 1; fi

	rm -f $CL\$Lexer.java
	rm -f *.class
	rm -f output
done
