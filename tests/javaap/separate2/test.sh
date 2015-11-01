#!/bin/bash

source ../../bin/settings.sh

v=CalcParser.java
echo testing $v

cp CalcParserGen.java.orig CalcParserGen.java
cp CalcLexerGen.java.orig CalcLexerGen.java

apt CalcParser.java Token.java
apt CalcLexer.java Token.java
compile CalcParser.java CalcParser.java CalcLexer.java Token.java
run CalcParser $v test.input test.output

rm -f CalcParserGen.java CalcLexerGen.java
rm -f *.class
rm -f output
