Lexer Section
*************

Specifying Shortcuts
++++++++++++++++++++

``@Shortcut`` is used to specify a single frequently used pattern can be
re-used in actual lexical patterns. Multiple ``@Shortcut`` can be
defined using ``@Shortcuts`` annotation. Just specify it on any
functions. The order of shortcut is not important and it is possible to
contain references to other shortcuts. Just be careful not to create
cyclic references.

.. code:: java

        @Shortcuts ( shortcuts = {
            @Shortcut (name="nonws", pattern="[^ \\t\\n]"),
            @Shortcut (name="ws", pattern="[ \\t]")
        })
        @Lex (pattern="{nonws}+", state="INITIAL")
        void matchWord ()
        {
            m_cc += yyLength ();
            ++m_wc;
        }

Specifying Lexical Patterns
+++++++++++++++++++++++++++

``@Lex`` is use to specify a single lexical pattern. ``@Lexs`` is used
to specify multiple lexical patterns that share a common action.

There are three types functions that can be marked with these two
annotations. They each has different meanings.

None of the functions can be ``private``, since they are called from the
generated class. They can be ``protected``, ``public`` or in the package
scope (if the generated class is in the same package as this class).

Case 1: Function returns void
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

This is the most simple case. The lexer would call this function and
then move on to matching the next potential pattern. For example:

.. code:: java

        @Lex (pattern = "[ \\t\\r\\n]+")
        protected void ignoreWhiteSpace ()
        {
        }

Note that it is necessary to use double backslashes here for escape
sequences because Java itself also interpret escape sequences. This is
perhaps one of the main drawback using Java annotation to specify the
lexer. Fortunately usually the lexer is fairly easy to get it working
correctly. IntelliJ IDEA also has a nice feature which pasting code with
escape sequence such as ``[ \t\r\n]+`` inside a pair of double quotes
would automatically adds the extra backslashes.

Case 2: Function returns a non-int value
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

In this case, ``@Lex`` needs to contain the terminal token that would be
returned. The return value from the function is going to be the value
associated with this terminal.

We have to specify the terminal in String due to the technical
limitation.

.. code:: java

        @Lex (pattern="[0-9]+", token="INTEGER") 
        protected Integer parseInt ()
        {
            return Integer.parseInt (yyText ());
        }

        @Lexs (patterns = {
            @Lex (pattern = "while", token = "WHILE"),
            @Lex (pattern = "if", token = "IF"),
            @Lex (pattern = "else", token = "ELSE"),
            @Lex (pattern = "print", token = "PRINT")
        })
        protected Object parseKeyword ()
        {
            return null;
        }

Case 3: Function returns an int value
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

In this case, the lexer would return the value. For example:

.. code:: java

        @Lex (pattern="[(){}.]")
        protected int parseSymbol ()
        {
            return yyText ().charAt (0);
        }

Be extra careful if the return value is used as terminals in the parser.
Values not in the valid used terminals can result in the early
termination of the parser.

Note that when ``<<EOF>>`` is encountered, it is necessary to return a
value or the lexer would get into an infinite loop. There are a number
of ways of doing so:

.. code:: java

        @Lex (pattern = "<<EOF>>", token = "$")
        protected void parseEOF ()
        {
        }

Or you can simply do

.. code:: java

        @Lex (pattern = "<<EOF>>")
        protected int parseEOF ()
        {
            return 0;
        }

This is because ``$`` terminal corresponds to 0.
