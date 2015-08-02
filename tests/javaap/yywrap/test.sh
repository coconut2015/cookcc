#!/bin/bash

source ../../bin/settings.sh

v=PushInput.java
echo testing $v

cp Lexer.java.orig Lexer.java

apt $v
compile $v $v
run PushInput $v test.input test.output

rm -f Lexer.java
rm -f *.class
rm -f output
