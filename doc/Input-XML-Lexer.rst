``<lexer>``
~~~~~~~~~~~

A sample XML for the ``<lexer>`` section looks like:

.. code:: xml

        <lexer table="ecs">
            <shortcut name="nonws">[^ \t\r\n]</shortcut>
            <shortcut name="word">{nonws}+</shortcut>
            <rule>
                <pattern>{word}</pattern>
                <pattern>{word}[ \t\r\n]*</pattern>
                <action>
                    ++wordCount;
                </action>
            </rule>
            <state name="INITIAL,TEST">
                <rule>
                    <pattern>.|\n</pattern>
                    <action>
                        // ignore
                    </action>
                </rule>
                <rule state="ANOTHER_STATE">
                    <pattern><![CDATA[<<EOF>>]]></pattern>
                    <action>
                        return 0;  /* exit lexer loop */
                    </action>
                </rule>
            </state>
        </lexer>

Options for the lexer are specified as attributes of the ``<lexer>``
tag.

+------------------------+---------------------------------------------------+
| Attribute              | Description                                       |
+========================+===================================================+
| ``table``              | The DFA table format. Can be one of ``"ecs"``,    |
|                        | ``"full"``, ``"compressed"`` options. Command     |
|                        | line options can override the choice here.        |
+------------------------+---------------------------------------------------+
| ``bol``                | Instruct the lexer to keep track of the BOL       |
|                        | (beginning of line) information even when there   |
|                        | are no patterns use that information.             |
+------------------------+---------------------------------------------------+
| ``warnbackup``         | Generate warning of backup lexer states if set to |
|                        | true. Default is false.                           |
+------------------------+---------------------------------------------------+
| ``yywrap``             | Indicates that ``yyWrap ()`` function should be   |
|                        | called when EOF is encountered. Default is false. |
+------------------------+---------------------------------------------------+
| ``linemode``           | Instruct the lexer to match patterns one line at  |
|                        | a time. This mode is primarily useful for         |
|                        | interactive modes where inputs are delimited by   |
|                        | ``'\n'`` character. Multi-line patterns will      |
|                        | generate warnings since they cannot be matched in |
|                        | this mode.                                        |
+------------------------+---------------------------------------------------+

``<shortcut>``
~~~~~~~~~~~~~~

This tag is used to specify frequently used subset of patterns. In the
above example, when the pattern ``{word}`` is seen, it is replaced with
``({nonws})``, which is in turn replaced with ``([^ \t\r\n])``. So the
actual pattern is ``[^ \t\r\n]+``.

``<shortcut>`` tags can only be specified as immediate children of
``<lexer>``.

``<state>``
~~~~~~~~~~~

``<state>`` tags are used to indicate the state conditions. It has only
one attribute ``name`` to specify a comma separated list of state names.
All rules specified under this tag are automatically added to this
particular state. If the ``name`` attribute is not specified, it is
assumed to be {{{INITIAL}}, which is required as the initial state at
the start of the lexer.

``<rule>``
~~~~~~~~~~

Rule tags are used to specify patterns and their associated action
codes. It can have multiple ``<pattern>`` children, but one and only one
``<action>`` child.

+----------------+-----------------------------------------------------------+
| Attribute      | Description                                               |
+================+===========================================================+
| ``state``      | A comma separated list of state names that this rule is   |
|                | in. If the current rule is already under a ``<state>``    |
|                | tag, then the rule is added to all of them.               |
+----------------+-----------------------------------------------------------+

``<pattern>``
~~~~~~~~~~~~~

+--------------+-------------------------------------------------------------------+
| Attribute    | Description                                                       |
+==============+===================================================================+
| ``bol``      | Specify that the pattern only works at BOL (beginning of line).   |
+--------------+-------------------------------------------------------------------+
| ``nocase``   | Specify that the pattern does case insensitive match.             |
+--------------+-------------------------------------------------------------------+

Although multiple patterns may be under the same rule and share the
action code, in actual generated code, the action code is replicated for
each pattern. This is to avoid the problem that some patterns may work
at BOL while some other patterns may not. To avoid action code
replication, try put them inside a single ``<pattern>`` tag with ``|``
in between.

``<action>``
~~~~~~~~~~~~

It contains the code to be executed when the pattern is matched.
