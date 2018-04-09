``<parser>``
~~~~~~~~~~~~

A sample XML for ``<parser>`` section looks like:

.. code:: xml

        <parser start="program">
            <type format="((Node){0})">stmt expr stmt_list</type>
            <type format="((String){0})">VARIABLE</type>
            <type format="((Integer){0})">INTEGER</type>
            <grammar rule="program">
                <rhs>function</rhs>
                <action>return 0;</action>
            </grammar>
            <grammar rule="function">
                <rhs>function stmt</rhs>
                <action>interpret ($2);</action>
                <rhs></rhs>
            </grammar>
            <grammar rule="stmt">
                <rhs>';'</rhs>
                <action>$$ = new SemiColonNode ();</action>

                <rhs>expr ';'</rhs>
                <action>$$ = $1;</action>

                <rhs>PRINT expr ';'</rhs>
                <action>$$ = new PrintNode ($2);</action>

                <rhs>VARIABLE '=' expr ';'</rhs>
                <action>$$ = new AssignNode ($1, $3);</action>

                <rhs>WHILE '(' expr ')' stmt</rhs>
                <action>$$ = new WhileNode ($3, $5);</action>

                <rhs precedence="IFX">IF '(' expr ')' stmt</rhs>
                <action>$$ = new IfNode ($3, $5, null);</action>

                <rhs>IF '(' expr ')' stmt ELSE stmt</rhs>
                <action>$$ = new IfNode ($3, $5, $7);</action>

                <rhs>'{' stmt_list '}'</rhs>
                <action>$$ = $2;</action>
            </grammar>
        </parser>

Options for the parser are specified as attributes of the ``<parser>``
tag.

+---------------------+------------------------------------------------------+
| Attribute           | Description                                          |
+=====================+======================================================+
| ``start``           | Specify the start non-terminal. If this attribute is |
|                     | not specified, the LHS of the first grammar is used. |
+---------------------+------------------------------------------------------+
| ``recovery``        | Should the parser try to generate error recovery     |
|                     | routines. This attribute is default ``true``. Set    |
|                     | this attribute to ``false`` for speedy exit from the |
|                     | parser in case of error.                             |
+---------------------+------------------------------------------------------+
| ``parseerror``      | Should the parser generate the error function since  |
|                     | the user is going to supply one. This attribute is   |
|                     | default ``true``.                                    |
+---------------------+------------------------------------------------------+

See the `parser recovery page <ParserErrorRecovery.html>`__ for more
information on error recovery.

``<type>``
~~~~~~~~~~

This tag is used to specify the necessary code that should be used to
cast / retrieve members of arguments ``{0}``. In the example above,
``$1`` was automatically converted to ``((Node)$1)`` if ``$1`` is a
``stmt``, ``expr``, or ``stmt_list``. ``$1`` itself is internally
translated to the appropriate variable/function call.

(Note in the Java code generator, the format does not apply to ``$$``).

``<grammar>``
~~~~~~~~~~~~~

The attribute value of ``rule`` is a non-terminal. All the productions
in ``<rhs>`` are for this particular terminal.

``<rhs>``
~~~~~~~~~

This tag represents the production for the non-terminal of the parent
``grammar`` tag. Its action code should be immediately followed. If not,
there are no actions performed for this particular production.

The attributes for the ``rhs`` tag are

+---------------------+------------------------------------------------------+
| Attribute           | Description                                          |
+=====================+======================================================+
| ``precedence``      | Specify the precedence for the production to the     |
|                     | precedence of a particular terminal.                 |
+---------------------+------------------------------------------------------+

``<action>``
~~~~~~~~~~~~

This tag is used to specify the code that should be called for the
production in the immediate ``<rhs>`` above.

``$$`` represents the value for the LHS non-terminal. ``$1``, ``$2`` etc
represent the object values of the symbols at the position for
productions specified in the ``<lhs>`` tag, starting from 1.
