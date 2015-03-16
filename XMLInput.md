# XML Input #

CookCC contains can generator both lexers and parsers.  The input XML ([DTD](http://code.google.com/p/cookcc/source/browse/trunk/src/resources/cookcc.dtd)) thus contains a [lexer](LexerXML.md) section and a [parser](ParserXML.md) section.  Only one of the two section is required.  The file extension is `*.xcc`.

There are plenty of examples shown in the [test cases](http://code.google.com/p/cookcc/source/browse/trunk/tests/).

# Overview #

A sample XML looks like:

```
<?xml version = "1.0" encoding="UTF-8"?>
<!DOCTYPE cookcc PUBLIC "-//CookCC//1.0" "http://code.google.com/p/cookcc/source/browse/trunk/src/resources/cookcc.dtd">
<cookcc unicode="false">

	<tokens>VARIABLE INTEGER WHILE IF PRINT</tokens>
	<tokens type="nonassoc">IFX</tokens>
	<tokens type="nonassoc">ELSE</tokens>
	<tokens type="left"><![CDATA[GE LE EQ NE '>' '<']]></tokens>
	<tokens type="left">'+' '-'</tokens>
	<tokens type="left">'*' '/'</tokens>
	<tokens type="nonassoc">UMINUS</tokens>

	<lexer>
		<!-- lexer section -->
	</lexer>
	<parser start="program">
		<!-- parser section -->
	</parser>

	<code name="default"><![CDATA[
		/* code section, can appear any where directly under the <cookcc> tag. */
	]]></code>
</cookcc>
```

Some XML editors such as the one in [IntelliJ IDEA](http://www.jetbrains.com/idea/) can perform on-the-fly XML syntax check, automatic tag and attribute suggestions.  CookCC also checks the validity of the input using the DTD before parsing.

# XML Tag Explanations #
## `<cookcc>` ##
This is the document root of the XML file.  It has a single attribute to indicate where or not the lexer parses unicode.  By default, `unicode` is false.

## `<code>` ##
The `<code>` tag can specify any pieces of code.  There can be multiple of such tags and it can locate any where directly under `<cookcc>` tag.  The name of the code needs to be unique.  If the name is not specified, it is assumed to be `"default"`.  The exact usage of that code depends on the template being used.  For Java output, you can take a look at the [Java output template](http://code.google.com/p/cookcc/source/browse/trunk/src/resources/templates/java/class.ftl) to get a rough idea.

## `<tokens>` ##
Simply a list of string names.  They can be separated by spaces, tabs or new lines.

The `type` attribute is used by the parser to determine the associativity of the tokens.  There are three types: `"left"`, `"right"`, and `"nonassoc"`.  If the type is not specified, it is assumed to be `"nonassoc"`.

Tokens inside the same `<tokens>` tag have the same precedence level.  Tokens specified in later `<tokens>` tags have higher precedence levels.