Ant Task
========

Initiate CookCC Ant Task
------------------------

To use CookCC Ant Task, first add the following line (and modify the
class path) to your ``build.xml`` so that Ant recognizes ``<cookcc>``.

.. code:: xml

        <target name="initcookcc">
            <taskdef name="cookcc" classname="org.yuanheng.cookcc.ant.Task" classpath="${basedir}/tool/cookcc-0.3.jar"/>
        </target>

Generic CookCC Input
--------------------

For input files such as ``*.xcc``, the typical setup is.

.. code:: xml

        <target name="TokenParser.java" depends="initcookcc">
            <cookcc src="${basedir}/src/org/yuanheng/cookcc/util/TokenParser.xcc">
                <option name="-class" value="org.yuanheng.cookcc.util.TokenParser"/>
                <option name="-d" value="${basedir}/src"/>
            </cookcc>
        </target>

The attributes of ``<cookcc>`` are the following:

+--------------------+-------------------------------------------------------+
| Attribute          | Description                                           |
+====================+=======================================================+
| **src**            | the input file.                                       |
+--------------------+-------------------------------------------------------+
| **lang**           | Same as ``-lang`` command line option. Select the     |
|                    | output language. The default is ``java``              |
+--------------------+-------------------------------------------------------+
| **analysis**       | Same as ``-analysis`` command line option.            |
+--------------------+-------------------------------------------------------+
| **lexertable**     | Same as ``-lexertable`` command line option.          |
+--------------------+-------------------------------------------------------+
| **parsertable**    | Same as ``-parsertable`` command line option.         |
+--------------------+-------------------------------------------------------+
| **defaultreduce**  | Same as ``-defaultreduce`` command line option.       |
+--------------------+-------------------------------------------------------+

Additional language specific options can be specified using the nested
``<option>`` tag.

Annotation Processing Tool (APT)
--------------------------------

If the input file specified in the ``src`` attribute has the ``.java``
extension, APT mode is assumed.

The annotation processing API changed from 1.5 to 1.6+.  As the result CookCC
0.3.3 only supports JDK1.5 and 1.6, while CookCC 0.4.0 supports JDK1.6+.

The typical setup is the following.

.. code:: xml

        <target name="FileHeaderLexer.java" depends="initcookcc">
            <cookcc srcdir="${basedir}/javaap_src" src="org/yuanheng/cookcc/input/javaap/FileHeaderScanner.java"/>
        </target>

+--------------------+-------------------------------------------------------+
| Attribute          | Description                                           |
+====================+=======================================================+
| **srcdir**         | the source directory. It is **required**.             |
+--------------------+-------------------------------------------------------+
| **destdir**        | the output directory. If not specified, it would be   |
|                    | the same as ``srcdir``.                               |
+--------------------+-------------------------------------------------------+
| **src**            | a list of java files separated by space. Note that    |
|                    | the path is relative to the directory specified by    |
|                    | ``srcdir`` attribute.                                 |
+--------------------+-------------------------------------------------------+
| **lang**           | Same as ``-lang`` command line option. Select the     |
|                    | output language. The default is ``java``              |
+--------------------+-------------------------------------------------------+
| **analysis**       | Same as ``-analysis`` command line option.            |
+--------------------+-------------------------------------------------------+
| **lexertable**     | Same as ``-lexertable`` command line option.          |
+--------------------+-------------------------------------------------------+
| **parsertable**    | Same as ``-parsertable`` command line option.         |
+--------------------+-------------------------------------------------------+
| **defaultreduce**  | Same as ``-defaultreduce`` command line option.       |
+--------------------+-------------------------------------------------------+

Additional language specific options can be specified using the nested
``<option>`` tag.

Nested Option Tag
-----------------

``<option>`` is used to specify additional language specific command
line options. It has two attributes.

+-------------+---------------------------------------------------+
| Attribute   | Description                                       |
+=============+===================================================+
| **name**    | the option name, such as ``-class``, ``-d`` etc   |
+-------------+---------------------------------------------------+
| **value**   | the option value. It is optional.                 |
+-------------+---------------------------------------------------+
