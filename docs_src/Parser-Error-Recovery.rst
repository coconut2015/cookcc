Error Recovery
--------------

``<parser>`` has an option ``recovery`` which would turn on/off the
error recovery code depending whether or not the value is ``true`` or
``false``. This value is by default ``true``.

Turning off error recovery can be useful since in many cases we do not
really care much about the corrupted data, and error recovery can be
slow.

The exact behavior of error recovery depends on the specific
implementation of output language.

Error Recovery in Java
~~~~~~~~~~~~~~~~~~~~~~

When the ``recovery`` option is set to ``false``, the parser simply
returns with a value of ``1`` to indicate that an error has occurred.

The option ``parseerror`` controls whether or not the code generator
should generatet he ``yyParseError`` function. Set this option to
``false`` if you want to the parser to use your own function.

Otherwise, the behavior of the parser is the following.

When a token not belonging to one of the lookahead (i.e. cannot either
reduce or shift) is encountered. ``yyParseError`` function is called. If
this function returns ``true``, the parser stops and returns a value
``1``. If the function returns ``false`` (by default), an error token is
pushed onto the lookahead stack and an internal error flag
``_yyInError`` is set. Then the parsing is resumed.

If the error token can be shifted, then a grammar dealing with error
recovery is found. Otherwise, the parser would start discarding a state
on the stack until a grammar that can handle the "error" token is
reached.

With the error token shifted on to the stack. The state should be
immediately reduceable if the grammar does not require any tokens after
the error token. Otherwise, it means the grammar is looking for a
specific terminal. Then the input is continuously consumed until the
desired token is found, or the end of file is reached.

Additionally, ``yyPeekLookahead ()`` is provided to check the cause of
the error (only accurate if the user didn't specify any terminals after
``error`` in the grammar). ``yyPopLookahead ()`` is provided to remove
the possible offending token. However, this function should be called
only once.

Here are some `test
cases <https://github.com/coconut2015/cookcc/tree/master/tests/java/parser/error>`__
that demonstrate these behaviors.
