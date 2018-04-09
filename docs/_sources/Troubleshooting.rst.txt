Troubleshooting
===============

Mysterious Crash in APT
-----------------------

For the following exception

::

       [cookcc] An exception has occurred in apt (1.6.0_22). Please file a bug at the Java Developer Connection (http://java.sun.com/webapps/bugreport)  after checking the Bug Parade for duplicates. Include your program and the following diagnostic in your report.  Thank you.
       [cookcc] java.lang.ClassCastException: com.sun.tools.javac.tree.JCTree$JCErroneous cannot be cast to com.sun.tools.javac.tree.JCTree$JCAnnotation
       [cookcc]     at com.sun.tools.javac.comp.Annotate.enterAttributeValue(Annotate.java:205)
       [cookcc]     at com.sun.tools.javac.comp.Annotate.enterAttributeValue(Annotate.java:219)
       [cookcc]     at com.sun.tools.javac.comp.Annotate.enterAnnotation(Annotate.java:167)
       [cookcc]     at com.sun.tools.javac.comp.MemberEnter.enterAnnotations(MemberEnter.java:743)
       [cookcc]     at com.sun.tools.javac.comp.MemberEnter.access$300(MemberEnter.java:42)
       [cookcc]     at com.sun.tools.javac.comp.MemberEnter$5.enterAnnotation(MemberEnter.java:711)
       [cookcc]     at com.sun.tools.javac.comp.Annotate.flush(Annotate.java:95)
       [cookcc]     at com.sun.tools.javac.comp.Annotate.enterDone(Annotate.java:87)
       [cookcc]     at com.sun.tools.javac.comp.Enter.complete(Enter.java:485)
       [cookcc]     at com.sun.tools.javac.comp.Enter.main(Enter.java:442)
       [cookcc]     at com.sun.tools.apt.main.JavaCompiler.compile(JavaCompiler.java:250)
       [cookcc]     at com.sun.tools.apt.main.Main.compile(Main.java:1102)
       [cookcc]     at com.sun.tools.apt.main.Main.compile(Main.java:964)
       [cookcc]     at com.sun.tools.apt.Main.processing(Main.java:95)
       [cookcc]     at com.sun.tools.apt.Main.process(Main.java:43)
       [cookcc]     at com.sun.tools.apt.Main.main(Main.java:34)

The cause of the issue is likely due to an extra comma after the last
element in the annotation array.

.. code:: java

        @Shortcuts ( shortcuts = {
            @Shortcut (name="arg", pattern="[^=;, \\t\\r\\n\\f'\"]"),
            @Shortcut (name="ws", pattern="[ \\t\\r\\n\\f]"),
        })

Eclipse Java Compiler (ECJ) does not report the extra comma in
annotation arrays as a syntax error. Sun's APT does not report the
correct error message. So it gets a bit confusing. Once removing this
extra comma, the problem would go away.

If the problem still persists, check your class path passed to APT. The
incorrect PATH separator can lead to the similar back trace as well.
