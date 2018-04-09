Parser
======

Introduction
------------

The generated parser is
`LALR(1) <http://en.wikipedia.org/wiki/LALR_parser>`__.

The grammar is specified as a series of terminals and non-terminals. For
example,

::

    <rhs>VARIABLE '=' expr ';'</rhs>

Extended Grammar
----------------

Extended grammar are mostly for convenience. New operators ``?``, ``*``,
``+``, ``|``, ``(`` and ``)`` are added. They are described below. Some
of the extended grammar requires a small runtime library
\`\ ``cookcc-rt``.

The
`tests <https://github.com/coconut2015/cookcc/tree/master/tests/java/parser/ext>`__
for the extended grammar have some examples.

Optional Operator
~~~~~~~~~~~~~~~~~

Optional operator ``?`` is used to indicate a symbol is optional. For
example, in the following grammar

.. code:: xml

    <grammar rule="G">
        <rhs>A B?</rhs> 
    </grammar>

It is equivalent to

.. code:: xml

    <grammar rule="G">
        <rhs>A @1</rhs>
    </grammar>
    <grammar rule="@1">
        <rhs>B</rhs>
        <action>$$ = $1;</action>
        <rhs></rhs>
        <action>$$ = null;</action>
    </grammar>

With ``()``, it is possible to enclose multiple symbols with ``?``
operator. For example, in the following

.. code:: xml

    <grammar rule="G">
        <rhs>A (B C)?</rhs>
    </grammar>

It is equivalent to

.. code:: xml

    <grammar rule="G">
        <rhs>A C</rhs>
    </grammar>
    <grammar rule="@1">
        <rhs>B C</rhs>
        <action>$$ = new ASTNode (); $$.add ($1); $$.add ($2);</action>
        <rhs></rhs>
        <action>$$ = null;</action>
    </grammar>

The collection used here is ``org.yuanheng.cookcc.ASTNode`` which is
part of *cookcc-rt* runtime.

List Operator
~~~~~~~~~~~~~

List operator ``+`` basically repeats a symbol or a set of symbols for
one or more iterations. It is very common for grammars to have repeated
terminal / non-terminals, so it should be helpful to avoid some tedious
works. For example, in the following grammar

.. code:: xml

    <grammar rule="G">
        <rhs>A B+</rhs>
    </grammar>

It is equivalent to

.. code:: xml

    <grammar rule="G">
        <rhs>A @1</rhs>
    </grammar>
    <grammar rule="@1">
        <rhs>B</rhs>
        <action>$$ = new ASTListNode (); $$.add ($1);</action>
        <rhs>@1 B</rhs>
        <action>$$.add ($2);</action>
    </grammar>

The collection used here is ``org.yuanheng.cookcc.ASTListNode`` which is
part of *cookcc-rt* runtime.

It is possible to repeat a set of symbols, but its use should be rare.
For example, in the following grammar

.. code:: xml

    <grammar rule="G">
        <rhs>A (B C)+</rhs> 
    </grammar>

It is equivalent to

.. code:: xml

    <grammar rule="G">
        <rhs>A @1</rhs> 
    </grammar>
    <grammar rule="@1">
        <rhs>B C</rhs> 
        <action>$$ = new ASTListNode (); $$.add ($1); $$.add ($2);</action>
        <rhs>@1 B C</rhs>
        <action>$$.add ($2); $$.add ($3);</action>
    </grammar>

It should be noted a single ``ASTListNode`` is used to contain all the
repeated symbols.

To avoid extra ``ASTListNode`` being created, the following two rules
are equivalent.

.. code:: xml

        <rhs>A B+</rhs>
        <rhs>A (B)+</rhs>

Optional List Operator
~~~~~~~~~~~~~~~~~~~~~~

Optional list operator ``*`` basically repeats a symbol or a set of
symbols for zero or more iterations. It is very similar to ``+``
operator except that the number of repeats can be zero.

Grouping Operator
~~~~~~~~~~~~~~~~~

``(`` and ``)`` are used enclose symbols. The behavior are the
following.

-  It has to enclose at least a symbol. Thus simply ``()`` is not
   allowed.
-  It is usually used in conjunction with other extended grammar
   operators. See notes in other extended grammar operators.
-  You can use ``(`` and ``)`` simply for grouping one or more symbols
   without using other extended grammar operators. For example:
   ``A (B) C``. An object ``org.yuanheng.cookcc.ASTNode`` is used to
   collect the symbol values.
-  Nesting such as ``((A))`` is allowed, but its use probably does not
   make practical sense.

Or Operator
~~~~~~~~~~~

Or operator ``|`` is used to make several possible choices. For example,
in the following grammar

.. code:: xml

    <grammar rule="G">
        <rhs>A (B | C)</rhs> 
    </grammar>

It is equivalent to

.. code:: xml

    <grammar rule="G">
        <rhs>A @1</rhs> 
    </grammar>
    <grammar rule="@1">
        <rhs>B</rhs> 
        <action>$$ = $1;</action>
        <rhs>C</rhs>
        <action>$$ = $1;</action>
    </grammar>

It is not possible to specify empty rule with ``|`` operator. Instead,
you should combine with ``?`` operator for this need. For example, in
the following grammar, A could be followed by B, C, or D.

.. code:: xml

    <grammar rule="G">
        <rhs>A (B | C) ? D</rhs> 
    </grammar>

cookcc-rt Runtime
~~~~~~~~~~~~~~~~~

cookcc-rt is a tiny Java runtime library for cookcc. It is only required
by certain extended grammars mentioned above.

Parser Table Format
-------------------

Currently, the following table formats are supported.

+------------------+-----------------------------------------------------------+
| Format           | Description                                               |
+==================+===========================================================+
| ``ecs``          | Good when there are not a lot symbols and states.         |
+------------------+-----------------------------------------------------------+
| ``compressed``   | A smaller table in most cases at some performance cost.   |
+------------------+-----------------------------------------------------------+

Default Reduce
--------------

the command line option ``-defaultreduce`` is specified, DFA states that
contain a reduceable item would convert all 0 (i.e. error) entries to
reduce. This approach can make the compressed table more compact, at the
expense of slightly more difficult error recovery.

Analysis Output
---------------

When the command line option ``-analysis`` is specified, a file named
`cookcc\_parser\_analysis.txt <https://github.com/coconut2015/cookcc/blob/master/tests/java/parser/calc/cookcc_parser_analysis.txt>`__
is generated in the current directory that contains the detail of the
parser. It can be useful in analyzing the grammar.

.. include::	Parser-Error-Recovery.rst
