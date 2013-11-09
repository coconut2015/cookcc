#!/bin/sh

function error ()
{
	echo $@ && exit 1
}

test -z "$JAVA_HOME" && error need to set JAVA_HOME env
test -z "$COOKCC" && error need to set COOKCC env

classpath="${COOKCC}:."
apt="${JAVA_HOME}/bin/apt -nocompile -cp $classpath -s ."

for v in WC*.java
do
	echo testing $v

	num=`echo $v | cut -c3`

	cp Lexer$num.java.orig Lexer$num.java

	$apt $v
	${JAVA_HOME}/bin/javac -classpath $classpath $v > /dev/null 2> /dev/null || error test for $v failed
	${JAVA_HOME}/bin/java -cp . WC$num ../../java/lexer/fastwc/test.input > output || error test for $v failed
	diff output test.output > /dev/null || error test for $v failed

	rm -f Lexer$num.java
	rm -f *.class
	rm -f output
done
