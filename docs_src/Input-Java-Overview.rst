Overview
~~~~~~~~

The following example is a simple `calculator script
interpreter <https://github.com/coconut2015/cookcc/tree/master/tests/javaap/calc>`__
adapted from `A Compact Guide to Lex &
Yacc <http://epaperpress.com/lexandyacc/>`__.

Setup
*****

First, add CookCC jar file to your project path. This jar file is only
required for building the parser, and setting up the ant task. It is not
required at runtime.

First Step: Annotate a Class
++++++++++++++++++++++++++++

``@CookCCOption`` is used to mark a class that uses the generated
lexer/parser. In the following example, we mark the ``Calculator``
class.

.. code:: java

    import org.yuanheng.cookcc.*;

    @CookCCOption (lexerTable = "compressed", parserTable = "compressed")
    public class Calculator extends Parser
    {
        // code
    }

The generated class is actually the parent class of ``Calculator``, in
this case, ``Parser`` class (which needs to be in the same package as
``Calculator``). Since we haven't really generated this class yet, so
what we needed to do to work on the ``Calculator`` class without the
error in editor (assuming that you are using a decent Java IDE such as
Eclipse or IntelliJ IDEA) is to create an empty ``Parser.java`` like the
following code.

.. code:: java

    /* Copyright (c) 2008 by Heng Yuan */
    import org.yuanheng.cookcc.CookCCByte;

    /**
     * @author Heng Yuan
     * @version $Id$
     */
    public class Parser extends CookCCByte
    {
    }

Notice that we have the file header (copyright notice) and the class
header. CookCC Java input will keep these in the generated class. It
also keep the scope of the class, ``public`` in this case.  In general
as a good practice though, the generated class should be in the same
package of ``Calculator``, and in the package scope.

``CookCCByte`` is class that contains all the possible generated
functions (not all of them will be available depending on the options
and the patterns/rules), so that code in ``Calculator`` can use them in
advance. Since we are not dealing with Unicode, so we extend
``CookCCByte`` class. For lexers that deal with Unicode, extend
``CookCCChar`` class.

Note that all the CookCC annotations and ``CookCCByte`` are merely
required for compile time, they are not required for runtime. The
generated class no longer extends ``CookCCByte``.

Second Step: Mark a Token Enum
++++++++++++++++++++++++++++++

If you are only going to use a `lexer <Input-Java-Lexer.html>`__, you
can skip this section.

Within ``Calculator`` class, you can specify the token
`Enum <http://java.sun.com/j2se/1.5.0/docs/guide/language/enums.html>`__
class. It is not required to have this Enum as a nested class of
``Calculator`` since ``@CookCCOption`` can be used to specify the Enum
class defined elsewhere. However, I personally find that having such
nested declaration makes it more visual.

Use ``@CookCCToken`` to mark a Enum declaration. All the names defined
here are all treated as terminals by CookCC.

To specify the precedence and the type, mark a token with
``@TokenGroup`` and set its type to LEFT, RIGHT or NONASSOC. If the type
is not specified, it is assumed to be NONASSOC. All unmarked tokens
would inherit the precedence and type of the previous token.

You can use `static
import <http://java.sun.com/j2se/1.5.0/docs/guide/language/static-import.html>`__
to avoid typing ``TokenType.LEFT`` and only need to type ``LEFT``. I
dislike static import, and I copy/paste code anyways. So it doesn't
really matter.

.. code:: java

        @CookCCToken
        static enum Token
        {
            @TokenGroup
            VARIABLE, INTEGER, WHILE, IF, PRINT, ASSIGN, SEMICOLON,
            @TokenGroup
            IFX,
            @TokenGroup
            ELSE,

            @TokenGroup (type = TokenType.LEFT)
            GE, LE, EQ, NE, LT, GT,
            @TokenGroup (type = TokenType.LEFT)
            ADD, SUB,
            @TokenGroup (type = TokenType.LEFT)
            MUL, DIV,
            @TokenGroup (type = TokenType.LEFT)
            UMINUS
        }

One of the unfortunate drawback of using Enum token is that it is
necessary to give a name to terminals such as ``'='`` ``'<'``, etc. On
the other hand, it is good to have them defined since it is easier to
use them in abstract syntax trees (ASTs).

(Currently, CookCC does not have a tree generator yet. Hopefully it can
be added in the near future.)
