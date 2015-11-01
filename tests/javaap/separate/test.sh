#!/bin/bash

source ../../bin/settings.sh

v=Calculator.java
echo testing $v

cp Parser.java.orig Parser.java

apt $v Token.java
compile $v $v Token.java
run Calculator $v test.input test.output

rm -f Parser.java
rm -f *.class
rm -f output
