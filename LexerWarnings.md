

# CookCC Lexer Generator Warnings #

## Backup ##

This situation happens when a pattern proceeds to match a relatively long string without intermediate states that are acceptable.

You can take a look at a simple [example](http://code.google.com/p/cookcc/source/browse/trunk/?#trunk/tests/backup) that cause such a problem.

Backups can cause slight performance degradations, depending the target language.  For Java, the difference is not so noticeable.

## Incomplete States ##

This situation happens when patterns concerning part of the character sets have been specified.  By default, CookCC internally add states that simply dumps the characters not matched by the user patterns to the standard output.

One way to avoid such warning is by adding a pattern `.|\n` as the last pattern for the state.  This is in fact the way internally CookCC does.  However, it then runs into the potential problem of having patterns that can never be matched.

CookCC also requires user to specify `<<EOF>>` conditions for all states, just in case of an unexpected end of file.  If not specified, the default action is to exit from lexer with a value of 0.

Here are some [examples](http://code.google.com/p/cookcc/source/browse/trunk/?#trunk/tests/incomplete) that cause such a problem.

## Some Patterns Can Never Be Matched ##

By default, patterns specified earlier have precedence patterns specified later.  Thus, for some patterns, the matchable strings could always be matched by other patterns first.

Here are some [examples](http://code.google.com/p/cookcc/source/browse/trunk/?#trunk/tests/unreachable) that cause such a problem.

## Multi-Line Patterns in Line Mode ##

When line mode is used in lexer.  Multi-line patterns simply cannot matched.

When this warning is given, other warnings may not be accurate until this warning is fixed.