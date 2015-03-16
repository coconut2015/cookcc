

# Lexer #

CookCC Lexer has the following features.

## Lexer Table Format ##

CookCC supports DFA tables for 8-bit and 16-bit characters.  16-bit character tables are intended for unicode support.  Currently, the following table formats are supported.

| `full` | A full table.  Very memory intensive. |
|:-------|:--------------------------------------|
| `ecs` | A much smaller table using equivalent classes. |
| `compressed` | An even smaller table in most cases at some performance cost. |

## Line Mode ##
Added in 0.4+.

This mode is mostly for interactive mode scanning where `\n` immediately triggers existing patterns.

Multi-line patterns will not work in this mode.

There is a slight performance hit due to one extra comparison per character.

## Pattern Warnings ##

CookCC generates warnings on
  * patterns that cause [backup](http://code.google.com/p/cookcc/source/browse/trunk/tests/java/lexer/backup),
  * patterns that were [never reached](http://code.google.com/p/cookcc/source/browse/trunk/tests/java/lexer/unreachable),
  * states that have [incomplete patterns](http://code.google.com/p/cookcc/source/browse/trunk/tests/java/lexer/incomplete), or
  * having [multi-line patterns](http://code.google.com/p/cookcc/source/browse/trunk/tests/java/lexer/linemode/linemode1.xcc) in line mode.

## Trail Context ##

CookCC at present only handles either fixed head (e.g., `abc/xyz` or `abc/x*z`) or tail (e.g., `a.*b/xyz`) trail contexts.

## TODO List ##

The following features are yet to be implemented:
| yyMore | Make the current string available for the next time. |
|:-------|:-----------------------------------------------------|
| REJECT | Reject a token and go to the next available accept case. |
| Variable trail context | This feature is related to REJECT. |