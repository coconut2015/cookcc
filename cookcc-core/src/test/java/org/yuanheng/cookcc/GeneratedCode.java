/*
 * Copyright (c) 2026, Heng Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yuanheng.cookcc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.Assert;
import org.junit.Assume;
import org.yuanheng.cookcc.codegen.java.JavaCodeGen;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.input.xml.XmlParser;
import org.yuanheng.cookcc.interfaces.CodeGen;

/**
 * Generates Java code from a grammar, compiles it, and runs it, so that tests
 * can check the behavior of the generated lexer and parser.  This exercises
 * the whole pipeline: grammar input, lexer DFA construction, LALR parser
 * construction, table compression, and the code generation templates.
 * <p>
 * Compiling needs a JDK.  On a JRE, the tests that use this class are skipped.
 *
 * @author Heng Yuan
 */
public class GeneratedCode
{
	private final Object m_instance;

	private GeneratedCode (Object instance)
	{
		m_instance = instance;
	}

	/**
	 * Gets a grammar from the test resources in org/yuanheng/cookcc/grammars.
	 *
	 * @param	name	the file name of the grammar.
	 * @return	the grammar file.
	 */
	public static File grammar (String name) throws Exception
	{
		URL url = GeneratedCode.class.getResource ("grammars/" + name);
		Assert.assertNotNull ("Missing test grammar " + name, url);
		return new File (url.toURI ());
	}

	/**
	 * Parses an .xcc grammar.  Each call returns a new Document, since the
	 * lexer and parser built from a Document are cached in it.
	 *
	 * @param	file	the grammar file.
	 * @return	the parsed Document.
	 */
	public static Document parseXml (File file)
	{
		Document doc = XmlParser.parse (file);
		Assert.assertNotNull ("Unable to parse " + file, doc);
		return doc;
	}

	/**
	 * Generates the Java class for the Document, compiles it, and creates an
	 * instance of it.
	 *
	 * @param	doc	the grammar.
	 * @param	dir	the directory for the generated source and class files.
	 * @param	className	the name of the class to generate.
	 * @param	options	JavaCodeGen options, such as "-lexertable", "ecs".  Each
	 *			option that takes an argument is followed by its argument.
	 * @return	the generated code, ready to run.
	 */
	public static GeneratedCode generate (Document doc, File dir, String className, String... options) throws Exception
	{
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler ();
		Assume.assumeNotNull (compiler);

		CodeGen codeGen = new JavaCodeGen ();
		OptionMap optionMap = codeGen.getOptions ();
		optionMap.addOption ("-d", dir.getPath ());
		optionMap.addOption ("-class", className);
		// Public, so that the tests can call it by reflection.
		optionMap.addOption ("-public");
		addOptions (optionMap, options);
		codeGen.generateOutput (doc);

		File source = new File (dir, className + ".java");
		Assert.assertTrue ("Not generated: " + source, source.isFile ());

		ByteArrayOutputStream messages = new ByteArrayOutputStream ();
		int status = compiler.run (null, messages, messages,
								   "-nowarn", "-encoding", "UTF-8", "-d", dir.getPath (), source.getPath ());
		Assert.assertEquals ("Generated code did not compile:\n" + messages.toString ("UTF-8"), 0, status);

		// The loader stays open: the generated class is used after this returns.
		@SuppressWarnings ("resource")
		URLClassLoader loader = new URLClassLoader (new URL[] { dir.toURI ().toURL () }, GeneratedCode.class.getClassLoader ());
		Class<?> cl = loader.loadClass (className);
		return new GeneratedCode (cl.getDeclaredConstructor ().newInstance ());
	}

	/**
	 * @return	a new instance of the same generated class, with fresh state.
	 */
	public GeneratedCode newInstance () throws Exception
	{
		return new GeneratedCode (m_instance.getClass ().getDeclaredConstructor ().newInstance ());
	}

	/**
	 * Adds options in the form "-option", "-option2", "argument", ...  An
	 * option is followed by its argument when the next string does not start
	 * with "-".
	 */
	static void addOptions (OptionMap optionMap, String... options) throws Exception
	{
		for (int i = 0; i < options.length; ++i)
		{
			if (i + 1 < options.length && !options[i + 1].startsWith ("-"))
			{
				optionMap.addOption (options[i], options[i + 1]);
				++i;
			}
			else
				optionMap.addOption (options[i]);
		}
	}

	/**
	 * Sets the input of the generated lexer.  A Unicode lexer reads characters
	 * from a Reader; otherwise it reads UTF-8 bytes from an InputStream.
	 *
	 * @param	input	the text to scan.
	 * @return	this object.
	 */
	public GeneratedCode setInput (String input) throws Exception
	{
		for (Method method : m_instance.getClass ().getMethods ())
		{
			if (!"setInput".equals (method.getName ()) || method.getParameterTypes ().length != 1)
				continue;
			Class<?> type = method.getParameterTypes ()[0];
			if (type == Reader.class)
				method.invoke (m_instance, new StringReader (input));
			else if (type == InputStream.class)
				method.invoke (m_instance, new ByteArrayInputStream (input.getBytes ("UTF-8")));
			else
				continue;
			return this;
		}
		Assert.fail ("setInput not found");
		return this;
	}

	/**
	 * Calls a method with no arguments, such as yyLex or yyParse, even if it
	 * is protected.  An exception thrown by the method is thrown as is.
	 *
	 * @param	name	the method name.
	 * @return	the int returned, or 0 for a void method.
	 */
	public int call (String name) throws Exception
	{
		Method method = findMethod (m_instance.getClass (), name);
		method.setAccessible (true);
		try
		{
			Object value = method.invoke (m_instance);
			return value == null ? 0 : ((Integer)value).intValue ();
		}
		catch (InvocationTargetException ex)
		{
			if (ex.getCause () instanceof Exception)
				throw (Exception)ex.getCause ();
			throw ex;
		}
	}

	private static Method findMethod (Class<?> cl, String name) throws NoSuchMethodException
	{
		for (Class<?> c = cl; c != null; c = c.getSuperclass ())
		{
			try
			{
				return c.getDeclaredMethod (name);
			}
			catch (NoSuchMethodException ex)
			{
			}
		}
		throw new NoSuchMethodException (name);
	}

	/**
	 * Scans the whole input with yyLex.
	 *
	 * @param	input	the text to scan.
	 * @return	the "out" list that the grammar actions add to.
	 */
	public List<String> lex (String input) throws Exception
	{
		setInput (input);
		call ("yyLex");
		return getOut ();
	}

	/**
	 * Parses the input with yyParse.
	 *
	 * @param	input	the text to parse.
	 * @return	the value returned by yyParse: 0 on success, 1 on a syntax error.
	 */
	public int parse (String input) throws Exception
	{
		setInput (input);
		return call ("yyParse");
	}

	/**
	 * @param	name	the name of a field in the generated class.
	 * @return	the value of the field.
	 */
	public Object getField (String name) throws Exception
	{
		Field field = m_instance.getClass ().getDeclaredField (name);
		field.setAccessible (true);
		return field.get (m_instance);
	}

	/**
	 * @return	a copy of the "out" list that the grammar actions add to.
	 */
	@SuppressWarnings ("unchecked")
	public List<String> getOut () throws Exception
	{
		return new ArrayList<String> ((List<String>)getField ("out"));
	}

	public static List<String> list (String... values)
	{
		return Arrays.asList (values);
	}
}
