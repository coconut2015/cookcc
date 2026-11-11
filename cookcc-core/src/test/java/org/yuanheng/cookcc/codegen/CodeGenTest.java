/*
 * Copyright (c) 2026, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Heng Yuan nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY Heng Yuan ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Heng Yuan BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.yuanheng.cookcc.codegen;

import static org.yuanheng.cookcc.GeneratedCode.grammar;
import static org.yuanheng.cookcc.GeneratedCode.parseXml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.yuanheng.cookcc.GeneratedCode;
import org.yuanheng.cookcc.Main;
import org.yuanheng.cookcc.OptionMap;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.input.xml.XmlParser;
import org.yuanheng.cookcc.input.yacc.YaccParser;
import org.yuanheng.cookcc.interfaces.CodeGen;
import org.yuanheng.cookcc.parser.Parser;

/**
 * Tests the code generators for each output language, and the Java code
 * generator options.
 *
 * @author Heng Yuan
 */
public class CodeGenTest
{
	@Rule
	public final TemporaryFolder m_folder = new TemporaryFolder ();

	private static String read (File file) throws Exception
	{
		StringBuilder builder = new StringBuilder ();
		Reader reader = new InputStreamReader (new FileInputStream (file), "UTF-8");
		char[] buffer = new char[4096];
		int n;
		while ((n = reader.read (buffer)) > 0)
			builder.append (buffer, 0, n);
		reader.close ();
		return builder.toString ();
	}

	/**
	 * Generates output for a language that writes a single file (-o option).
	 */
	private File generateFile (String lang, Document doc, String fileName) throws Exception
	{
		File output = new File (m_folder.getRoot (), fileName);
		CodeGen codeGen = Main.getCodeGen (lang);
		codeGen.getOptions ().addOption ("-o", output.getPath ());
		codeGen.generateOutput (doc);
		Assert.assertTrue ("No output for " + lang, output.isFile ());
		return output;
	}

	/**
	 * Generates Java source with the given options, without compiling it.
	 */
	private String generateJavaSource (String grammarName, String className, String... options) throws Exception
	{
		CodeGen codeGen = Main.getCodeGen ("java");
		OptionMap optionMap = codeGen.getOptions ();
		optionMap.addOption ("-d", m_folder.getRoot ().getPath ());
		optionMap.addOption ("-class", className);
		for (int i = 0; i < options.length; ++i)
		{
			if (i + 1 < options.length && !options[i + 1].startsWith ("-"))
				optionMap.addOption (options[i], options[++i]);
			else
				optionMap.addOption (options[i]);
		}
		codeGen.generateOutput (parseXml (grammar (grammarName)));
		return read (new File (m_folder.getRoot (), className.replace ('.', File.separatorChar) + ".java"));
	}

	@Test
	public void testLanguages () throws Exception
	{
		java.util.List<String> languages = java.util.Arrays.asList (Main.getLanguages ());
		Assert.assertTrue (languages.containsAll (java.util.Arrays.asList ("java", "plain", "xml", "yacc")));
		Assert.assertFalse (languages.contains ("default"));
	}

	@Test (expected = IllegalArgumentException.class)
	public void testUnknownLanguage () throws Exception
	{
		Main.getCodeGen ("nosuchlanguage");
	}

	@Test
	public void testXmlOutputRoundTrip () throws Exception
	{
		// Write calc.xcc back out as XML, read that, and check that the parser
		// generated from it still computes the same results.
		File xml = generateFile ("xml", parseXml (grammar ("calc.xcc")), "calc-copy.xcc");
		Document copy = XmlParser.parse (xml);
		Assert.assertNotNull ("Unable to read the XML output", copy);

		GeneratedCode parser = GeneratedCode.generate (copy, m_folder.newFolder (), "CalcCopy");
		Assert.assertEquals (0, parser.parse ("2 ^ 3 ^ 2 - (1 + 2) * 3"));
		Assert.assertEquals (503, ((Integer)parser.getField ("result")).intValue ());
	}

	@Test
	public void testYaccOutputRoundTrip () throws Exception
	{
		// Write the calc.xcc grammar as yacc, read that, and check that it
		// builds an equivalent parser.
		Document original = parseXml (grammar ("calc.xcc"));
		Parser originalParser = Parser.getParser (original, new OptionMap ());

		File y = generateFile ("yacc", original, "calc.y");
		Document copy = YaccParser.parse (y);
		Parser copyParser = Parser.getParser (copy, new OptionMap ());

		Assert.assertEquals (originalParser.getRules ().size (), copyParser.getRules ().size ());
		Assert.assertEquals (originalParser.getDFA ().size (), copyParser.getDFA ().size ());
		Assert.assertEquals (0, copyParser.getShiftConflict ());
		Assert.assertEquals (0, copyParser.getReduceConflict ());
	}

	@Test
	public void testYaccOutputContainsDeclarations () throws Exception
	{
		String y = read (generateFile ("yacc", parseXml (grammar ("calc.xcc")), "calc.y"));
		Assert.assertTrue (y, y.contains ("%right"));
		Assert.assertTrue (y, y.contains ("%prec UMINUS"));
		Assert.assertTrue (y, y.contains ("%%"));
	}

	@Test
	public void testPlainOutput () throws Exception
	{
		String text = read (generateFile ("plain", parseXml (grammar ("calc.xcc")), "calc.txt"));
		Assert.assertTrue (text.length () > 0);
	}

	@Test
	public void testJavaDefaultScopeIsPackage () throws Exception
	{
		String source = generateJavaSource ("tokens.xcc", "Scanner");
		Assert.assertTrue (source.contains ("\nclass Scanner"));
	}

	@Test
	public void testJavaPublic () throws Exception
	{
		Assert.assertTrue (generateJavaSource ("tokens.xcc", "Scanner", "-public").contains ("public class Scanner"));
	}

	@Test
	public void testJavaPackage () throws Exception
	{
		String source = generateJavaSource ("tokens.xcc", "a.b.Scanner");
		Assert.assertTrue (source.contains ("package a.b;"));
		Assert.assertTrue (new File (m_folder.getRoot (), "a/b/Scanner.java").isFile ());
	}

	@Test
	public void testJavaAbstract () throws Exception
	{
		String source = generateJavaSource ("tokens.xcc", "Scanner", "-abstract");
		Assert.assertTrue (source.contains ("abstract class Scanner"));
		// An abstract class cannot be instantiated, so no main () is generated.
		Assert.assertFalse (source.contains ("public static void main"));
	}

	@Test
	public void testJavaExtend () throws Exception
	{
		Assert.assertTrue (generateJavaSource ("tokens.xcc", "Scanner", "-extend", "my.Base").contains ("extends my.Base"));
	}
}
