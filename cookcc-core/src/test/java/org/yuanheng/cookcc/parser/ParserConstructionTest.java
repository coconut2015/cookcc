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
package org.yuanheng.cookcc.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.yuanheng.cookcc.OptionMap;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.exception.ParserException;
import org.yuanheng.cookcc.input.yacc.YaccParser;

/**
 * Tests the LALR (1) parser construction on small grammars written in yacc
 * format: conflict detection, conflict resolution with precedence, and
 * grammar errors.
 *
 * @author Heng Yuan
 */
public class ParserConstructionTest
{
	@Rule
	public final TemporaryFolder m_folder = new TemporaryFolder ();

	/**
	 * Builds the parser for a yacc grammar.
	 *
	 * @param	declarations	the yacc declarations before the first %%.
	 * @param	rules	the grammar rules.
	 * @return	the parser.
	 */
	private Parser build (String declarations, String rules) throws Exception
	{
		File file = m_folder.newFile ("grammar.y");
		Writer writer = new OutputStreamWriter (new FileOutputStream (file), "UTF-8");
		writer.write (declarations + "\n%%\n" + rules + "\n%%\n");
		writer.close ();

		Document doc = YaccParser.parse (file);
		Assert.assertNotNull (doc);
		return Parser.getParser (doc, new OptionMap ());
	}

	private Parser build (String rules) throws Exception
	{
		return build ("", rules);
	}

	@Test
	public void testUnambiguousGrammarHasNoConflicts () throws Exception
	{
		Parser parser = build ("E : E '+' T | T ; T : T '*' 'a' | 'a' ;");
		Assert.assertEquals (0, parser.getShiftConflict ());
		Assert.assertEquals (0, parser.getReduceConflict ());
	}

	@Test
	public void testLalrButNotSlrGrammarHasNoConflicts () throws Exception
	{
		// The classic grammar that has a conflict in SLR (1) but not in LALR (1).
		Parser parser = build ("S : L '=' R | R ; L : '*' R | 'i' ; R : L ;");
		Assert.assertEquals (0, parser.getShiftConflict ());
		Assert.assertEquals (0, parser.getReduceConflict ());
	}

	@Test
	public void testAmbiguousGrammarHasShiftReduceConflict () throws Exception
	{
		Parser parser = build ("E : E '+' E | 'a' ;");
		Assert.assertEquals (1, parser.getShiftConflict ());
		Assert.assertEquals (0, parser.getReduceConflict ());
	}

	@Test
	public void testPrecedenceResolvesConflicts () throws Exception
	{
		Parser parser = build ("%left '+'\n%left '*'", "E : E '+' E | E '*' E | 'a' ;");
		Assert.assertEquals (0, parser.getShiftConflict ());
		Assert.assertEquals (0, parser.getReduceConflict ());
	}

	@Test
	public void testDanglingElseHasShiftReduceConflict () throws Exception
	{
		Parser parser = build ("S : 'i' S | 'i' S 'e' S | 'x' ;");
		Assert.assertEquals (1, parser.getShiftConflict ());
	}

	@Test
	public void testDanglingElseResolvedWithPrec () throws Exception
	{
		Parser parser = build ("%nonassoc IFX\n%nonassoc 'e'", "S : 'i' S %prec IFX | 'i' S 'e' S | 'x' ;");
		Assert.assertEquals (0, parser.getShiftConflict ());
	}

	@Test
	public void testReduceReduceConflict () throws Exception
	{
		Parser parser = build ("S : A | B ; A : 'a' ; B : 'a' ;");
		Assert.assertEquals (1, parser.getReduceConflict ());
	}

	@Test
	public void testEmptyRule () throws Exception
	{
		Parser parser = build ("E : E o T | T ; T : 'a' | '(' E ')' ; o : 'x' | ;");
		Assert.assertEquals (0, parser.getShiftConflict ());
		Assert.assertEquals (0, parser.getReduceConflict ());
	}

	@Test
	public void testRules () throws Exception
	{
		Parser parser = build ("E : E '+' 'a' | 'a' ;");
		// The two rules of E, plus the start rule CookCC adds.
		Assert.assertEquals (3, parser.getRules ().size ());
		Assert.assertEquals (1, parser.getNonTerminalCount () - 1);
	}

	@Test
	public void testUnusedTokenIsAllowed () throws Exception
	{
		Parser parser = build ("%token UNUSED", "E : 'a' ;");
		Assert.assertEquals (0, parser.getShiftConflict ());
	}

	@Test (expected = ParserException.class)
	public void testMissingProductionIsAnError () throws Exception
	{
		build ("S : A 'b' ;");
	}

	@Test (expected = ParserException.class)
	public void testInvalidPrecTerminalIsAnError () throws Exception
	{
		build ("E : 'a' %prec NOSUCHTOKEN ;");
	}
}
