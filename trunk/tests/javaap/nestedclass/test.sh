#!/bin/sh

function error ()
{
	echo $@ && exit 1
}

test -z "$JAVA_HOME" && error need to set JAVA_HOME env
test -z "$COOKCC" && error need to set COOKCC env

classpath="${COOKCC};."
apt="${JAVA_HOME}/bin/apt -nocompile -cp $classpath -s ."

for v in WC?.java
do
	echo testing $v

	CL=`echo $v | cut -d. -f1`

	cp $CL\$Lexer.java.orig $CL\$Lexer.java

	$apt $v
	${JAVA_HOME}/bin/javac -classpath $classpath $v > /dev/null 2> /dev/null || error test for $v failed
	${JAVA_HOME}/bin/java -cp . $CL ../../java/lexer/fastwc/test.input > output || error test for $v failed
	diff output test.output > /dev/null || error test for $v failed

	rm -f $CL\$Lexer.java
	rm -f *.class
	rm -f output
done
