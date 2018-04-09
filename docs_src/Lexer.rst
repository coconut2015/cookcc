Lexer
=====

CookCC Lexer has the following features.

Lexer Table Format
------------------

CookCC supports DFA tables for 8-bit and 16-bit characters. 16-bit
character tables are intended for unicode support. Currently, the
following table formats are supported.

+------------------+-----------------------------------------------------------------+
| Format           | Description                                                     |
+==================+=================================================================+
| ``full``         | A full table. Very memory intensive.                            |
+------------------+-----------------------------------------------------------------+
| ``ecs``          | A much smaller table using equivalent classes.                  |
+------------------+-----------------------------------------------------------------+
| ``compressed``   | An even smaller table in most cases at some performance cost.   |
+------------------+-----------------------------------------------------------------+

Line Mode
---------

Added in 0.4+.

This mode is mostly for interactive mode scanning where ``\n``
immediately triggers the current longest pattern to be matched. It is
very similar to matching ``<<EOF>>`` character where ``\n`` must be
consumed in the current line.

Multi-line patterns will not work in this mode.

In this mode, the lexer will not block and read the character on the
next line before fully processing the patterns on the existing line.
Thus, it is perfectly suitable for interactive procesing.

There is a slight performance hit due to one extra comparison per
character, but usually it is not an issue in interactive mode.

Trail Context
-------------

CookCC at present only handles either fixed head (e.g., ``abc/xyz`` or
``abc/x*z``) or tail (e.g., ``a.*b/xyz``) trail contexts.

.. include::	Lexer-Warnings.rst

TODO List
---------

The following features are yet to be implemented. These features are
difficult to implement and I do not have any experiences using them, so
they are quite low in the priority list.

+------------------------------------------------------+---------------------+
| Feature                                              | Description         |
+======================================================+=====================+
| yyMore                                               | Make the current    |
|                                                      | string available    |
|                                                      | for the next time.  |
+------------------------------------------------------+---------------------+
| REJECT                                               | Reject a token and  |
|                                                      | go to the next      |
|                                                      | available accept    |
|                                                      | case.               |
+------------------------------------------------------+---------------------+
| Variable trail context                               | Both the head and   |
|                                                      | tail are variable   |
|                                                      | length.             |
+------------------------------------------------------+---------------------+
| `Marked                                              | Perl-like matching  |
| sub-expression <http://www.boost.org/doc/libs/1_54_0 | that automatically  |
| /libs/regex/doc/html/boost_regex/captures.html>`__   | extract             |
|                                                      | sub-expressions as  |
|                                                      | well.               |
+------------------------------------------------------+---------------------+

Some of them can be worked around by utilizing Java's
`Pattern <https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html>`__
class to perform the secondary match.
