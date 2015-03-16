CookCC is a lexer and parser (LALR (1)) generator project, combined.  It is written in Java, but the [target languages](TargetLanguages.md) can vary.

CookCC comes with two unique features, which were the original motivations for this work.

  1. CookCC uses a unique approach of storing and loading DFA tables in Java that significantly reduces the starting up time.  Many efforts have been made to maximize the generated Java lexer and parser performances, painstakingly line-by-line, case-by-case fine turning the lexer and parser code.  I believe that CookCC is the fastest lexer for Java (see the [performance test](TargetLanguageJava#Performance.md)).
  1. CookCC allows lexer/parser patterns and rules to be specified using Java annotation.  This feature greatly simplifies and eases the writing of lexer and parser for Java.

Other Features
  * CookCC can produce highly compressed DFA tables for both the lexer and parser, using the similar compression algorithm found in flex.
  * For the lexer, DFA states constructed were minimal.  (In contrast, Flex does not construct minimal DFA states).

CookCC requires JRE 1.5+ to run, but the generated Java code can be compiled and run with earlier versions of Java.  There are **zero dependencies** for the generated Java code.  So it is light and fast.

The current release is 0.3.3.

I am currently working on examples for 0.4.  Hopefully, I will find time between gaming and working to get it done soon.

Note: the BSD license only applies to CookCC itself.  The code generated belongs to you.

# Road Map for 0.5 #
  * add [re2c](http://re2c.org/)-like direct code generation option for Lexer rather than only using table lookup (as of now).
    * Possible mixed mode of execution to reduce table size (and code size), by reducing the number of states to be stored and possibly fewer equivalent classes.
    * Performance gain for Java is questionable but I have thought out the way doing it.
  * C and C++ code generation.
    * More of the long term because right now I do not have need to do so.  The performance of [flex](http://flex.sourceforge.net/) for C is **extremely** difficult to beat anyways.

# What's New #
## 0.4 (Upcoming Release) ##
  * changed from hand written lexer parser to cookcc generated.  Internally, an intermediate parse tree was generated for each RegEx so that some folding could occur.  `(a|b)` can be converted to `[ab]` internally.
  * added [:word:] character class (which is not POSIX).
  * added -extend option to set the parent class of the generated class
  * updated debugLexer, debugLexerBackup, debugParser signature so that it is actually meaningful overload these debugging functions.
  * [Issue 27](https://code.google.com/p/cookcc/issues/detail?id=27): Add an option of using '\0' as EOB to further improve lexer performance.
  * [Issue 26](https://code.google.com/p/cookcc/issues/detail?id=26): Add line mode scanning.
  * [Issue 25](https://code.google.com/p/cookcc/issues/detail?id=25): When exception occurred during code generation, the file gets overwritten with garbage.
  * [Issue 24](https://code.google.com/p/cookcc/issues/detail?id=24): Add @SuppressWarnings ("unchecked") automatically for yyParse() to avoid compiler warnings.
  * [Issue 23](https://code.google.com/p/cookcc/issues/detail?id=23): added reset() function in the generated class.
  * [Issue 20](https://code.google.com/p/cookcc/issues/detail?id=20): allowed the start symbol to be specified in Java annotation input (by default it was the LHS of the first @Rule).
  * [Issue 19](https://code.google.com/p/cookcc/issues/detail?id=19): allowed grammar on Java interfaces.
  * [Issue 18](https://code.google.com/p/cookcc/issues/detail?id=18): added -generics option to generate Java code that use generics.
  * [Issue 17](https://code.google.com/p/cookcc/issues/detail?id=17): added optional / optional list / list grammar shortcuts.
  * Possible tree generator (grammar on Java annotations only)

## 0.3.3 ##
  * allowed the internal buffer to be automatically increased for long matches.
  * [Issue 14](https://code.google.com/p/cookcc/issues/detail?id=14): added yyPushInput, yyPopInput, yyInputStackSize, yyWrap functions (and yywrap option).
  * [Issue 13](https://code.google.com/p/cookcc/issues/detail?id=13): turn on backup lex state warning only when requested.
  * [Issue 12](https://code.google.com/p/cookcc/issues/detail?id=12): added setBOL function to set the next token to be at BOL.
  * [Issue 11](https://code.google.com/p/cookcc/issues/detail?id=11): yacc output does not have %start.
  * [Issue 10](https://code.google.com/p/cookcc/issues/detail?id=10): yacc output fails on empty TokensDoc.

## 0.3.2 ##
  * added yacc grammar input and output.
  * added yyPushLexerState and yyPopLexerState functions.
  * added line number information for the error messages for Java input.
  * added "parserprolog" section for the generated Java code.
  * [Issue 9](https://code.google.com/p/cookcc/issues/detail?id=9): unable to handle `'\''` terminals in the grammar.
  * [Issue 8](https://code.google.com/p/cookcc/issues/detail?id=8): incorrect lalr item lookahead calculation.  Now tested against bison using several major language grammars.
  * [Issue 7](https://code.google.com/p/cookcc/issues/detail?id=7): disable APT compile for the CookCC Ant task to prevent class files from generated.
  * [Issue 6](https://code.google.com/p/cookcc/issues/detail?id=6): erroneous warning of unreachable pattern when a lex pattern is shared among multiple lex states.
  * [Issue 5](https://code.google.com/p/cookcc/issues/detail?id=5): `<rule>` tag `state` attribute did not work.

## 0.3.1 ##
  * added single quoted literal string as lex patterns.

## 0.3 ##
  * added input using Java annotation.
  * [Issue 2](https://code.google.com/p/cookcc/issues/detail?id=2): multiple incomplete state can cause internal lex error due to reassignment of the internal pattern case values.
  * [Issue 1](https://code.google.com/p/cookcc/issues/detail?id=1): incorrectly generated parser if start non-terminal is not specified

## 0.2 ##
  * added parser generator.

## 0.1 ##
  * initial release.  Only includes lexer generator.