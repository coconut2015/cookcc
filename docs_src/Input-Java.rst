Java
----

One of the cool feature of CookCC is being able to directly specify the
lexer/parser right in the Java code using
`annotation <http://java.sun.com/j2se/1.5.0/docs/guide/language/annotations.html>`__,
without going into an obscure / proprietary input file.

Why Java Annotation
~~~~~~~~~~~~~~~~~~~

You can look at `CookCC presentation
slides <https://github.com/coconut2015/cookcc/blob/master/doc/CookCC.pdf>`__
for more detailed comparisons and a quick tutorial.

Java Annotation vs Lex/Yacc
***************************

The main benefit of using Java annotation is that you can take full
advantages of modern IDEs without having to deal with proprietary text
files.

-  syntax highlighting
-  context sensitive hints
-  code usage analysis
-  refactoring
-  auto-completion
-  instant error checking
-  etc

So it takes a lot of pain away from writing lexer / parsers.

This approach can be extended to other languages, such as Python or C#,
even C / C++.

Java Annotation vs JavaDoc
***************************

Although it is possible to use
`JavaDoc <http://java.sun.com/j2se/javadoc/>`__ doclet to extract
annotations, the
`annotation <http://java.sun.com/j2se/1.5.0/docs/guide/language/annotations.html>`__
capabilities of Java 1.5 is simply a lot easier to use and deal with.
For this reason, I settled to use Java 1.5. This however, does not mean
the code generated has to be run under JVM 1.5+. One can always target
the output class files for earlier versions of JVM.

Annotation Processing API Changes
*********************************

It should be note that Annotation Processing Tool (APT) was deprecated
in Java 7. The newer java compiler based processing API was available
since Java 6.

CookCC 0.3.3 only supports the older APT. CookCC 0.4+ supports the newer
API (although I kept the old API code, but it was not used for any newer
features introduced in 0.4+). If you cannot use Java 6 and later JDK,
then you will have to use CookCC 0.3.3.

.. include::	Input-Java-Overview.rst
.. include::	Input-Java-Lexer.rst
.. include::	Input-Java-Parser.rst

Examples
~~~~~~~~

There are a number of examples in `test
cases <https://github.com/coconut2015/cookcc/tree/master/tests/javaap>`__.

Related Work
~~~~~~~~~~~~

-  `SPARK <http://pages.cpsc.ucalgary.ca/~aycock/spark/>`__ which is for
   Python. It uses doc string to specify the lexer/parser. The paper
   reference: Aycock, J. "Compiling Little Languages in Python",
   Proceedings of the Seventh International Python Conference, p100,
   1998.
