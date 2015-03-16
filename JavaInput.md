

# Java Input #

One of the cool feature of CookCC is being able to directly specify the lexer/parser right in the Java code, without going into an obscure / proprietary input file.  This feature greatly simplifies the task of writing lexer/parser.

This capability can be extended to other languages, such as Python or C#, even C / C++.

You can check out the [CookCC presentation slides](http://cookcc.googlecode.com/svn/trunk/doc/CookCC.pdf) to get started.

## CookCC Input Using Java Annotation ##

Although it is possible to use [JavaDoc](http://java.sun.com/j2se/javadoc/) doclet to extract annotations, the [annotation](http://java.sun.com/j2se/1.5.0/docs/guide/language/annotations.html) capabilities of Java 1.5 is simply a lot easier to use and deal with.  For this reason, I settled to use Java 1.5.  This however, does not mean the code generated has to be run under JVM 1.5+.  One can always target the output class files for earlier version of JVM.

Please goto [overview](JavaInputOverview.md) section to start.

There are a number of examples in [test cases](http://code.google.com/p/cookcc/source/browse/trunk/tests/javaap).

## Related Work ##

  * [SPARK](http://pages.cpsc.ucalgary.ca/~aycock/spark/) which is for Python.  It uses doc string to specify the lexer/parser.  The paper reference:  Aycock, J. "Compiling Little Languages in Python", Proceedings of the Seventh International Python Conference, p100, 1998.