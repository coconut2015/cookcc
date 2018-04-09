Java
====

Introduction
------------

Java is the default output language of CookCC. It can also be selected
using the ``-lang java`` command line option. The generated class is
self-contained and does not require additional libraries.

Command Line Options
--------------------

+-------------------------+-----------+-----------------------------------------+
| Option                  | Version   | Description                             |
+=========================+===========+=========================================+
| ``-d <directory>``      | 0.1+      | Select the output directory. By         |
|                         |           | default, it is the current directory.   |
+-------------------------+-----------+-----------------------------------------+
| ``-class <className>``  | 0.1+      | Set the class name. By default, the     |
|                         |           | class name is ``Lexer``. The output     |
|                         |           | Java file would be generated under the  |
|                         |           | appropriate package subdirectories of   |
|                         |           | the output directory. The               |
|                         |           | subdirectories would be created if they |
|                         |           | do not exist.                           |
+-------------------------+-----------+-----------------------------------------+
| ``-public``             | 0.1+      | Set class scope to ``public``. By       |
|                         |           | default, the class generated is in the  |
|                         |           | package scope.                          |
+-------------------------+-----------+-----------------------------------------+
| ``-abstract``           | 0.3+      | Make the output class abstract. It also |
|                         |           | disables the generation of ``main``     |
|                         |           | function.                               |
+-------------------------+-----------+-----------------------------------------+

Code Locations
--------------

For codes in ``<code name="name"></code>``, their locations in the
generated file can be seen in the following example. If the name is not
given, the name is assumed to be "default".

.. code:: java

    /* code name = "fileheader" */
    package foo;

    /**
     * code name = "classheader"
     */
    public class Bar
    {
        public int yyLex ()
        {
            // code name = "lexerprolog"

            // case switch codes
        }

        public int yyParse ()
        {
            // code name = "parserprolog"

            // case switch codes
        }

        // code name = "default"
    }

Lexer
-----

`Word count
tests <http://code.google.com/p/cookcc/source/browse/trunk/tests/#tests/fastwc>`__
on large files (5 MB - 22 MB) have shown that ``ecs`` table has about
the same performance as the ``full`` table for Java.

Buffer Size
~~~~~~~~~~~

When a particular match exceeds the buffer size (default 4096), CookCC
would increase the buffer length by 50%. For very long matches (such as
code dumping near the end), for the best performance, set the initial
buffer size to the size of the input.

yywrap
~~~~~~

By default, when the end of file of the current input is reached, a
special ``<<EOF>>`` character is generated. However, if one wishes to
hook this event, set the ``yywrap="true"`` option for the lexer and
define the ``protected boolean yywrap ()`` function, which would be
called.

The ``yywrap`` function should return ``true`` if no further action
should be done, and ``false`` if the lexer should attempt to read from
the input again.

Input Stack
~~~~~~~~~~~

Sometimes, it maybe useful to halt the current input and switch to
another input temporarily. For instance, ``#include "file"``. In these
cases, ``yyPushInput``, ``yyPopInput``, ``yyInputStackSize`` functions
are provided.

A `test
example <https://github.com/coconut2015/cookcc/blob/master/tests/java/lexer/pushinput/pushinput.xcc>`__
is provided.

Unicode Support
~~~~~~~~~~~~~~~

CookCC can generate tables for 16-bit characters. The default input
handling though is not clever enough to detect the encoding of the
input.

See `Input Encoding Detection <Input-Encoding-Detection.html>`__ for
more details.

CookCC 0.3.3 generates a string that is too long for Oracle's Java
compiler. A work around is to use `ECJ (Eclipse Core Java
compiler) <http://www.eclipse.org/jdt/core/index.php>`__ to compile the
generated code.

CookCC 0.4 fixes this issue.

Performance
~~~~~~~~~~~

Here is a Lexer performance chart using Flex's fastwc examples (lower
bar indicates better performance) on a simple 5 MB text file. The
following was tested using MinGW WC program, 5 version of word count
under Flex (full table), CookCC (ecs table), and JFlex (ecs table). Both
MinGW WC and Flex generated code were in C, while CookCC and JFlex
generated codes were in Java.

.. figure:: http://chart.apis.google.com/chart?chtt=Word%20Count%20Performance&cht=bvg&chs=670x300&chf=bg,s,ffffff%7Cc,s,ffffcc&chdl=MinGW%20WC%7CFlex%7CCookCC%7CJFlex&chco=ff0000,00ff00,0000ff,ff00ff&chxt=x,y&chxr=0,1,5%7C1,0,1&chxp=0,1,2,3,4,5%7C1,0,0.2,0.4,0.6000000000000001,0.8,1&chg=100,100,1,0&chd=t:12.9,12.9,12.9,12.9,12.9%7C15.7,15.5,15,15,15.1%7C51.2,46.2,42.9,41.7,46.3%7C74,75.9,74.1,77.3,74.9&nonsense=something_that_ends_with.png
   :alt: Word Count Performance Chart

   Word Count Performance Chart

The file is too small to really show the differences among Flex's five
different versions of word count for Flex, but the pattern shows quite
noticeably for CookCC and the performance is expected (#1 to #4 has
gradual improvements while #5 introduced backups and is actually
slower).

There were several reasons why JFlex was so much slower than CookCC.
JFlex has a very slow startup time due to its inefficient table packing
method. As the result, the DFA table size has a major impact to the
performance. JFlex also does not have local variable declaration section
and thus all variables need to be instance variables. It also does not
have a yyLength variable and must call yylength () function instead.

As a side note, ecs table and full table didn't make much of the
difference for CookCC.
