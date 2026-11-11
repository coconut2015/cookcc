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
