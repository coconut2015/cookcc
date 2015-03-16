

# Parser #

## Introduction ##

The generated parser is [LALR(1)](http://en.wikipedia.org/wiki/LALR_parser).

The grammar is specified as a series of terminals and non-terminals.  For example,
`	<rhs>VARIABLE '=' expr ';'</rhs>`

## Grammar Shortcuts ##

The following grammar shortcuts are available.
  * Optional
> > `  <rhs>A B?</rhs>  `
> > It is equivalent to
> > `  Goal = A C;  C = B | /* empty */;  `
  * Optional list
> > `	<rhs>A B*</rhs>  `
> > It is equivalent to
> > `  Goal = A C;  C = C B | /* empty */;  `
  * List
> > `	<rhs>A B+</rhs>  `
> > It is equivalent to
> > `  Goal = A C;  C = C B | B;  `

For Java output language, both optional list and list values are stored in LinkedList objects.

## Parser Table Format ##

Currently, the following table formats are supported.

| `ecs` | Good when there are not a lot symbols and states. |
|:------|:--------------------------------------------------|
| `compressed` | A smaller table in most cases at some performance cost. |

## Default Reduce ##

the command line option `-defaultreduce` is specified, DFA states that contain a reduceable item would convert all 0 (i.e. error) entries to reduce.  This approach can make the compressed table more compact, at the expense of slightly more difficult error recovery.

## Analysis Output ##

When the command line option `-analysis` is specified, a file named [cookcc\_parser\_analysis.txt](http://code.google.com/p/cookcc/source/browse/trunk/tests/java/parser/calc/cookcc_parser_analysis.txt) is generated in the current directory that contains the detail of the parser.  It can be useful in analyzing the grammar.