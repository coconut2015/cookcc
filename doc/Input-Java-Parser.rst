Parser Section
**************

Specifying Parser Rules
+++++++++++++++++++++++

``@Rule`` specifies a single grammar rule. ``@Rules`` can be used to
specify multiple rules that share the same function action.

There are also three cases of functions marked using ``@Rule``

Case 1: Function returns void
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

In this case, the value associated with the non-terminal of the LHS is
null.

.. code:: java

        @Rule (lhs = "function", rhs = "function stmt", args = "2")
        protected void parseFunction (Node node)
        {
            interpret (node);
        }

Case 2: Function returns a non-int value
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

In this case, the return value is automatically associated with the
non-terminal on the LHS.

.. code:: java

        @Rule (lhs = "stmt", rhs = "SEMICOLON")
        protected Node parseStmt ()
        {
            return new SemiColonNode ();
        }

Case 3: Function returns an int value
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

This function is used by the grammar start non-terminal to signal the
exit of the parser with the particular value. It can be used by error
processing functions as well.

.. code:: java

        @Rule (lhs = "program", rhs = "function")
        protected int parseProgram ()
        {
            return 0;
        }

Passing Arguments
+++++++++++++++++

``args`` of the ``@Rule`` annotation is a list of indexes (separated by
comma or space) of the symbols which the method expects as arguments.
The indexing value starts from 1 for the production on the RHS.

For example:

.. code:: java

        @Rule (lhs = "stmt", rhs = "VARIABLE ASSIGN expr SEMICOLON", args = "1 3")
        protected Node parseAssign (String var, Node expr)
        {
            return new AssignNode (var, expr);
        }

This will assign the value of symbol ``VARIABLE`` to the method
parameter ``String var`` and the value of symbol ``expr`` to the method
parameter ``Node expr``.

Note that the indexes need not be in any specific order. This would be
equivalent (indexes and method parameters swapped):

.. code:: java

        @Rule (lhs = "stmt", rhs = "VARIABLE ASSIGN expr SEMICOLON", args = "3 1")
        protected Node parseAssign (Node expr, String var)
        {
            return new AssignNode (var, expr);
        }

As you can see, one does not have to mess with ``$$``, ``$1`` etc, and
does not have to deal with type information specified elsewhere. This
approach is much more intuitive.
