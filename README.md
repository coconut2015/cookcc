CookCC is a lexer and parser (LALR (1)) generator project, combined.  It is written in Java, but the [target languages][TargetLanguages] can vary.

CookCC comes with two unique features, which were the original motivations for this work.

1. CookCC uses a unique approach of storing and loading DFA tables in Java that significantly reduces the starting up time.  Many efforts have been made to maximize the generated Java lexer and parser performances, painstakingly line-by-line, case-by-case fine turning the lexer and parser code.  I believe that CookCC is the fastest lexer for Java (see the [TargetLanguageJava#Performance performance test]).
1. CookCC allows lexer/parser patterns and rules to be specified using Java annotation.  This feature greatly simplifies and eases the writing of lexer and parser for Java.

Other Features
 * CookCC can produce highly compressed DFA tables for both the lexer and parser, using the similar compression algorithm found in flex.
 * For the lexer, DFA states constructed were minimal.  (In contrast, Flex does not construct minimal DFA states).

CookCC requires JRE 1.7+ to run, but the generated Java code can be compiled and run with earlier versions of Java.  There are *zero dependencies* for the generated Java code.  So it is light and fast.

The current release is 0.3.3.

I am currently working on 0.4.  Hopefully, I will find time between gaming and working to get it done.

Note: the BSD license only applies to CookCC itself.  The code generated belongs to you.
