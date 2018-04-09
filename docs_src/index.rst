CookCC documentation
====================

CookCC is a `lexer <http://en.wikipedia.org/wiki/Lexical_analysis>`__
and `parser <http://en.wikipedia.org/wiki/Parsing>`__ (`LALR
(1) <http://en.wikipedia.org/wiki/LALR_parser>`__) generator project,
combined. It is written in Java, but the `target
languages <Target-Languages.html>`__ can vary.

CookCC comes with two unique features, which were the original
motivations for this work.

-  CookCC uses a unique approach of storing and loading DFA tables in
   Java that significantly reduces the starting up time. Many efforts
   have been made to maximize the generated Java lexer and parser
   performances, painstakingly line-by-line, case-by-case fine turning
   the lexer and parser code. I believe that CookCC is the fastest lexer
   for Java (see the performance test).
-  CookCC allows lexer/parser patterns and rules to be specified using
   Java annotation. This feature greatly simplifies and eases the
   writing of lexer and parser for Java.

Other Features

-  CookCC can produce highly compressed DFA tables for both the lexer
   and parser, using the similar compression algorithm found in
   `Flex <http://flex.sourceforge.net/>`__.
-  For the lexer, DFA states constructed were minimal. In contrast, Flex
   does not construct minimal DFA states.

The current release is 0.4 which requires 1.7+ to run, mainly due to the
annotation processing API. The generated Java code can be compiled and
run with earlier versions of Java. There are **zero dependencies** for
the generated Java code unless you use extended grammar. So it is light
and fast.

Before 0.4, CookCC uses the new BSD license.  For 0.4+, it is moved to
`APL 2.0 license <https://www.apache.org/licenses/LICENSE-2.0>`__.  It
should be noted that the license only applies to CookCC itself. The
code generated belongs to you.
