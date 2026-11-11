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
package org.yuanheng.cookcc.lexer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.yuanheng.cookcc.OptionMap;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.doc.LexerStateDoc;
import org.yuanheng.cookcc.doc.PatternDoc;
import org.yuanheng.cookcc.exception.LexerException;
import org.yuanheng.cookcc.exception.UnknownNameException;
import org.yuanheng.cookcc.exception.VariableTrailContextException;
import org.yuanheng.cookcc.input.xml.XmlParser;

/**
 * Tests the lexer DFA construction and its analysis: DFA minimization,
 * patterns that can never match, states that do not cover all characters,
 * backup, and errors in patterns.
 *
 * @author Heng Yuan
 */
public class LexerConstructionTest
{
	@Rule
	public final TemporaryFolder m_folder = new TemporaryFolder ();

	/**
	 * Builds a lexer with one rule per pattern, all in the INITIAL state.
	 */
	private Lexer build (String... patterns) throws Exception
	{
		StringBuilder xml = new StringBuilder ("<?xml version=\"1.0\"?>\n<cookcc>\n<lexer>\n");
		xml.append ("<shortcut name=\"digit\">[0-9]</shortcut>\n");
		for (String pattern : patterns)
			xml.append ("<rule><pattern><![CDATA[").append (pattern).append ("]]></pattern><action>return 1;</action></rule>\n");
		xml.append ("</lexer>\n</cookcc>\n");

		File file = m_folder.newFile ();
		Writer writer = new OutputStreamWriter (new FileOutputStream (file), "UTF-8");
		writer.write (xml.toString ());
		writer.close ();

		Document doc = XmlParser.parse (file);
		Assert.assertNotNull (doc);
		return Lexer.getLexer (doc, new OptionMap ());
	}

	private static List<String> patterns (Map<LexerStateDoc, Collection<PatternDoc>> map)
	{
		List<String> list = new ArrayList<String> ();
		if (map != null)
		{
			for (Collection<PatternDoc> patternDocs : map.values ())
				for (PatternDoc patternDoc : patternDocs)
					list.add (patternDoc.getPattern ());
		}
		return list;
	}

	@Test
	public void testEquivalentPatternsGiveSameMinimalDfa () throws Exception
	{
		// The same language written three ways.  Without minimization, each
		// would give a different number of DFA states.
		int size = build ("(a|b)*abb").getDFA ().size ();
		Assert.assertEquals (size, build ("[ab]*abb").getDFA ().size ());
		Assert.assertEquals (size, build ("(a*b*)*abb").getDFA ().size ());
		Assert.assertEquals (size, build ("(a|b)*a(b|b)b").getDFA ().size ());
	}

	@Test
	public void testRedundantAlternativesAddNoStates () throws Exception
	{
		Assert.assertEquals (build ("abc").getDFA ().size (), build ("abc|abc|(abc)").getDFA ().size ());
	}

	@Test
	public void testShadowedPatternCannotMatch () throws Exception
	{
		Lexer lexer = build ("[a-z]+", "abc", "[0-9]+");
		Assert.assertEquals (java.util.Arrays.asList ("abc"), patterns (lexer.getUnusedPatterns ()));
	}

	@Test
	public void testNoUnusedPatterns () throws Exception
	{
		Lexer lexer = build ("abc", "[a-z]+");
		Assert.assertTrue (patterns (lexer.getUnusedPatterns ()).isEmpty ());
	}

	@Test
	public void testIncompleteState () throws Exception
	{
		Lexer lexer = build ("[a-z]+");
		Assert.assertNotNull (lexer.getIncompleteStates ());
		Assert.assertEquals ("INITIAL", lexer.getIncompleteStates ().iterator ().next ().getName ());
	}

	@Test
	public void testMissingEofRuleIsIncomplete () throws Exception
	{
		// Every character is covered, but end of file is not.
		Lexer lexer = build ("[a-z]+", ".|\\n");
		Assert.assertNotNull (lexer.getIncompleteStates ());
	}

	@Test
	public void testCompleteState () throws Exception
	{
		Lexer lexer = build ("[a-z]+", ".|\\n", "<<EOF>>");
		Assert.assertNull (lexer.getIncompleteStates ());
	}

	@Test
	public void testBackup () throws Exception
	{
		// After reading "ab", the lexer must back up to "a" if no "c" follows.
		Lexer lexer = build ("abc", "a", ".|\\n");
		Assert.assertTrue (lexer.hasBackup ());
	}

	@Test
	public void testNoBackup () throws Exception
	{
		Lexer lexer = build ("[a-z]+", "[0-9]+", ".|\\n");
		Assert.assertFalse (lexer.hasBackup ());
	}

	@Test
	public void testShortcut () throws Exception
	{
		Assert.assertEquals (build ("[0-9]+").getDFA ().size (), build ("{digit}+").getDFA ().size ());
	}

	@Test (expected = UnknownNameException.class)
	public void testUnknownShortcut () throws Exception
	{
		build ("{nosuchshortcut}");
	}

	@Test (expected = VariableTrailContextException.class)
	public void testVariableLengthTrailingContext () throws Exception
	{
		// Either the pattern or its trailing context must have a fixed length.
		build ("a*/b*");
	}

	@Test
	public void testFixedLengthTrailingContext () throws Exception
	{
		Assert.assertNotNull (build ("a*/b", "a/b*", ".|\\n"));
	}

	@Test (expected = LexerException.class)
	public void testUnbalancedParenthesis () throws Exception
	{
		build ("(ab");
	}

	@Test (expected = LexerException.class)
	public void testUnterminatedCharacterClass () throws Exception
	{
		build ("[ab");
	}
}
