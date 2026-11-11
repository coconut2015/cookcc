CookCC is a lexer and parser (LALR (1)) generator project, combined.  It is written in Java, but the [target languages](http://coconut2015.github.io/cookcc/Target-Languages) can vary.

CookCC comes with two unique features, which were the original motivations for this work.

1. CookCC uses a unique approach of storing and loading DFA tables in Java that significantly reduces the starting up time.  Many efforts have been made to maximize the generated Java lexer and parser performances, painstakingly line-by-line, case-by-case fine turning the lexer and parser code.  I believe that CookCC is the fastest lexer for Java ( see the [performance test](../../wiki/TargetLanguageJava#performance) ).
1. CookCC allows lexer/parser patterns and rules to be specified using Java annotation.  This feature greatly simplifies and eases the writing of lexer and parser for Java.

Other Features
 * CookCC can produce highly compressed DFA tables for both the lexer and parser, using the similar compression algorithm found in flex.
 * For the lexer, DFA states constructed were minimal.  (In contrast, Flex does not construct minimal DFA states).

CookCC is built for Java 8: its source code is kept at the Java 8 language level and compiled to Java 8 bytecode, so it requires JRE 8+ to run and JDK 8+ to build.  The generated Java code can be compiled and run with earlier versions of Java.  There are *zero dependencies* for the generated Java code.  So it is light and fast.

The current release is 0.4.3.

## Building

Requirements
 * JDK 8 or later, with `JAVA_HOME` pointing to it.  The code stays at the Java 8 level regardless of the JDK used to build it.
 * Maven 3.6.3 or later on the `PATH`.
 * Internet access to Maven Central for the first build.
 * For the documentation only: Python 3 with the packages in `docs_src/requirements.txt` (`pip install -r docs_src/requirements.txt`).

Build with the script for your platform, run from any directory: `make.cmd` on Windows, `./make.sh` on Linux / macOS.  Run it without arguments, or with `help`, to list the commands.

| Command | What it does |
|---|---|
| `package` | Compiles all modules, runs the unit tests, generates the javadoc, and collects the jars in `dist/`. |
| `package --skip-tests` | The same, without running the unit tests. |
| `test` | Runs the integration tests in `tests/` against the jars in `dist/`, so run `package` first.  Add `--coverage` for a JaCoCo report in `target/site/jacoco-it`.  `make.sh` only: the tests are bash scripts for Linux / macOS. |
| `docs` | Builds the HTML documentation in `docs_src/_build/html`.  Open `TOC.html` there. |
| `full` | Builds everything: `package`, then `docs`.  Also accepts `--skip-tests`. |

For example:

```
./make.sh package
make.cmd package --skip-tests
./make.sh full
```

Any other arguments after `package` or `full` are passed to Maven, for example `./make.sh package -o` to build offline.  With `make.cmd`, quote Maven arguments that contain `=`.

The `package` results are placed in `dist/`:

| File | Contents |
|---|---|
| `cookcc-0.4.3.jar` | CookCC with all of its dependencies bundled.  Run it with `java -jar cookcc-0.4.3.jar`. |
| `cookcc-0.4.3-sources.jar` | Sources for the above. |
| `cookcc-0.4.3-javadoc.jar` | Javadoc for all modules. |
| `cookcc-rt-0.4.3.jar` | The small runtime library (AST node classes) used by parsers generated from grammars with extended rule syntax. |

## Dependencies

Maven downloads these automatically.  They are bundled into `cookcc-0.4.3.jar`, so nothing else is needed to run CookCC.

| Dependency | Version | Used for |
|---|---|---|
| [FreeMarker](https://freemarker.apache.org/) | 2.3.35 | Code generation templates |
| CookXml | 3.0.2 | Reading `.xcc` grammar files |
| [Xerces](https://xerces.apache.org/xerces2-j/) | 2.12.2 | XML parsing |

Build-only dependencies, not bundled:

| Dependency | Version | Used for |
|---|---|---|
| [Apache Ant](https://ant.apache.org/) | 1.10.18 | Compiling the CookCC Ant task (`provided`; Ant supplies it at run time) |
| [JUnit](https://junit.org/junit4/) | 4.13.2 | Unit tests |

This software is licensed under [APL 2.0](https://www.apache.org/licenses/LICENSE-2.0).  This license only applies to CookCC itself.  The code generated belongs to you.
