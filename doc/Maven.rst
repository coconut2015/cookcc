Maven
=====

Basic Usage
-----------

First, your project ``pom.xml``, there should be cookcc dependency.  CookCC
Maven plugin searches for this dependency to determine the jar file to use. 

.. code:: xml

		<dependency>
			<!-- CookCC is only needed at compile time -->
			<groupId>org.yuanheng.cookcc</groupId>
			<artifactId>cookcc</artifactId>
			<version>0.4.0</version>
			<scope>compile</scope>
		</dependency>

Then in your ``pom.xml`` build plugins, add cookcc-maven-plugin like
the following.

.. code:: xml

			<plugin>
				<groupId>org.yuanheng.cookcc</groupId>
				<artifactId>cookcc-maven-plugin</artifactId>
				<version>1.0</version>
				<configuration>
					<source>1.7</source>
					<target>1.7</target>
					<tasks>
						<task src="src/main/java/test/Test1.java"/>
						<task src="src/main/java/test/Test2.java"/>
						<task src="src/main/java/test/Test3.java" lexerAnalysis="true"/>
						<task src="src/main/java/test/Test4.java" debug="true"/>

						<task src="src/main/xcc/Test5.xcc">
							<option name="-class" value="test.Test5"/>
						</task>
					</tasks>
				</configuration>
				<executions>
					<execution>
						<phase>generate-sources</phase>
						<goals>
							<goal>run</goal>
						</goals>
					</execution>
				</executions>
			</plugin>

``<source>`` and ``<target>`` are optional.  If they are not specified, they
are assumed to be 1.8.  Ideally, it would be nice to be able to scan
maven-compiler-plugin configuration to get the info instead, but I have not
figured out how.  Please let me know if you know how to do it.

``<tasks>`` is simply a list of ``<task>`` tags.

``<task>`` Attributes
---------------------

+--------------------+-------------------------------------------------------+
| Attribute          | Description                                           |
+====================+=======================================================+
| **src**            | the input file.                                       |
+--------------------+-------------------------------------------------------+
| **debug**          | If the value is ``true``, it is the same as           |
|                    | ``-debug`` command line option.                       |
+--------------------+-------------------------------------------------------+
| **analysis**       | If the value is ``true``, it is the same as           |
|                    | ``-analysis`` command line option.                    |
+--------------------+-------------------------------------------------------+
| **lexerAnalysis**  | If the value is ``true``, it is the same as           |
|                    | ``-lexeranalysis`` command line option.               |
+--------------------+-------------------------------------------------------+
| **lexerTable**     | Same as ``-lexertable`` command line option.          |
+--------------------+-------------------------------------------------------+
| **parserTable**    | Same as ``-parsertable`` command line option.         |
+--------------------+-------------------------------------------------------+
| **defaultReduce**  | If the value is ``true``, it is the same as           |
|                    | ``-defaultreduce`` command line option.               |
+--------------------+-------------------------------------------------------+
| **public**         | If the value is ``true``, it is the same as           |
|                    | ``-public`` command line option.                      |
+--------------------+-------------------------------------------------------+
| **abstract**       | If the value is ``true``, it is the same as           |
|                    | ``-abstract`` command line option.                    |
+--------------------+-------------------------------------------------------+
| **extend**         | Same as ``-extend`` command line option.              |
+--------------------+-------------------------------------------------------+

``<option>`` Attributes
-----------------------

``<option>`` tag is used to specify options not mentioned above.

+--------------------+-------------------------------------------------------+
| Attribute          | Description                                           |
+====================+=======================================================+
| **name**           | option name.                                          |
+--------------------+-------------------------------------------------------+
| **value**          | optional option argument                              |
+--------------------+-------------------------------------------------------+

Run Maven
---------
You can run the following command to run all the CookCC tasks.

.. code:: bash

	mvn cookcc:run
	
In the execution above, by specifying generate-sources phase, when you run
the following command, CookCC is also run.

.. code:: bash

	mvn package
