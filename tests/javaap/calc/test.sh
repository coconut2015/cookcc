#!/bin/sh

function error ()
{
	echo $@ && exit 1
}

test -z "$JAVA_HOME" && error need to set JAVA_HOME env
test -z "$COOKCC" && error need to set COOKCC env

classpath="${COOKCC};."
apt="${JAVA_HOME}/bin/apt -nocompile -cp $classpath -s ."

v=Calculator.java
echo testing $v

cp Parser.java.orig Parser.java

$apt $v
${JAVA_HOME}/bin/javac -classpath "$classpath" $v > /dev/null 2> /dev/null || error test for $v failed
${JAVA_HOME}/bin/java -cp . Calculator test.input > output || error test for $v failed
diff output test.output > /dev/null || error test for $v failed

rm -f Parser.java
rm -f *.class
rm -f output
