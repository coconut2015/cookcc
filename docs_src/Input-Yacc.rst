Yacc
====

CookCC Yacc input allows CookCC to read traditional yacc/bison input
files. However, there are some deviations since some features are not
support by CookCC.

Deviations
----------

Non-terminal name restrictions
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The pattern of non-terminal names recognized by CookCC is
``[_a-zA-Z][_a-zA-Z0-9]*``.

Embedded actions are not supported
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

CookCC does not allow embedded action for yacc input at present. The
presence of such grammar would result in parsing error.

An embedded action is like the following:

.. code:: c

        A   : B { /* embedded action */ } C
              { /* regular action */ }
            ;

It is equivalent to

.. code:: c

        A   : B T C
              { /* regular action */ }
            ;
        T   :
              { /* embedded action */ }
            ;

Token declaration works somewhat differently
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

In CookCC, tokens declared using ``%token`` are treated as
non-associative terminals (same as ``%nonassoc``).

In contrast, in yacc / bison, tokens by default have the lowest
precedence can be specified later again in ``%left``, ``%right``,
``%nonassoc`` directives to specify the precedence and associativity.
CookCC does not allow tokens specified in ``%token`` directives to be
specified in other token directives.

Code Sections
-------------

The code surrounded by ``%{`` and ``%}`` pairs in section 1 of yacc
input are treated as ``"fileheader"`` code.

The section 3 code of yacc input are treated as ``"default"`` code.
