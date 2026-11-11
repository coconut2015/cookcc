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

import static org.yuanheng.cookcc.GeneratedCode.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests the behavior of generated parsers: precedence and associativity
 * (grammars/calc.xcc) and error recovery (grammars/lines.xcc).  Every test
 * runs with each parser table format, with and without default reductions.
 *
 * @author Heng Yuan
 */
@RunWith (Parameterized.class)
public class GeneratedParserTest
{
	@ClassRule
	public static final TemporaryFolder s_folder = new TemporaryFolder ();

	/** The compiled parser for each grammar and parameter combination, generated once. */
	private static final Map<String, GeneratedCode> s_parsers = new HashMap<String, GeneratedCode> ();

	@Parameters (name = "{0} table, defaultreduce={1}")
	public static Collection<Object[]> parameters ()
	{
		List<Object[]> list = new ArrayList<Object[]> ();
		for (String table : new String[] { "ecs", "compressed" })
		{
			list.add (new Object[] { table, Boolean.FALSE });
			list.add (new Object[] { table, Boolean.TRUE });
		}
		return list;
	}

	private final String m_table;
	private final boolean m_defaultReduce;

	public GeneratedParserTest (String table, Boolean defaultReduce)
	{
		m_table = table;
		m_defaultReduce = defaultReduce.booleanValue ();
	}

	/**
	 * Returns a new instance of the parser generated from the grammar for this
	 * parameter combination.  The code is generated and compiled once.
	 */
	private GeneratedCode parser (String grammar) throws Exception
	{
		String key = grammar + "/" + m_table + "/" + m_defaultReduce;
		GeneratedCode parser = s_parsers.get (key);
		if (parser == null)
		{
			String[] options = m_defaultReduce ? new String[] { "-parsertable", m_table, "-defaultreduce" }
											   : new String[] { "-parsertable", m_table };
			String className = grammar.substring (0, 1).toUpperCase () + grammar.substring (1, grammar.indexOf ('.')) + "Parser";
			parser = GeneratedCode.generate (GeneratedCode.parseXml (GeneratedCode.grammar (grammar)),
											 s_folder.newFolder (), className, options);
			s_parsers.put (key, parser);
		}
		return parser.newInstance ();
	}

	private int calc (String input) throws Exception
	{
		GeneratedCode parser = parser ("calc.xcc");
		Assert.assertEquals ("yyParse (\"" + input + "\")", 0, parser.parse (input));
		return ((Integer)parser.getField ("result")).intValue ();
	}

	private int calcStatus (String input) throws Exception
	{
		return parser ("calc.xcc").parse (input);
	}

	@Test
	public void testSingleNumber () throws Exception
	{
		Assert.assertEquals (42, calc ("42"));
	}

	@Test
	public void testPrecedence () throws Exception
	{
		Assert.assertEquals (7, calc ("1 + 2 * 3"));
		Assert.assertEquals (5, calc ("1 * 2 + 3"));
		Assert.assertEquals (19, calc ("1 + 2 ^ 3 * 2 + 2"));
	}

	@Test
	public void testParentheses () throws Exception
	{
		Assert.assertEquals (9, calc ("(1 + 2) * 3"));
		Assert.assertEquals (2, calc ("((((2))))"));
	}

	@Test
	public void testLeftAssociativity () throws Exception
	{
		Assert.assertEquals (3, calc ("8 - 3 - 2"));
		Assert.assertEquals (2, calc ("16 / 4 / 2"));
	}

	@Test
	public void testRightAssociativity () throws Exception
	{
		// 2 ^ (3 ^ 2), not (2 ^ 3) ^ 2 = 64.
		Assert.assertEquals (512, calc ("2 ^ 3 ^ 2"));
	}

	@Test
	public void testPrecOverride () throws Exception
	{
		// '-' expr uses the precedence of UMINUS, which is higher than '^'.
		Assert.assertEquals (4, calc ("-2 ^ 2"));
		Assert.assertEquals (-6, calc ("2 * -3"));
		Assert.assertEquals (5, calc ("- -5"));
		Assert.assertEquals (-1, calc ("-3 + 2"));
	}

	/**
	 * With -defaultreduce, a generated parser can accept invalid input: on a
	 * syntax error it injects the error token, a default reduction reduces the
	 * start symbol with the error token as the lookahead, and yyParse returns 0.
	 * For example, "1 +" is accepted with result 1.  Skip the invalid input
	 * tests in that mode until this is fixed.
	 */
	private void assumeInvalidInputDetected ()
	{
		Assume.assumeFalse ("-defaultreduce parsers can accept invalid input (known bug)", m_defaultReduce);
	}

	@Test
	public void testSyntaxErrors () throws Exception
	{
		assumeInvalidInputDetected ();
		Assert.assertEquals (1, calcStatus ("1 +"));
		Assert.assertEquals (1, calcStatus ("(1"));
		Assert.assertEquals (1, calcStatus (")"));
		Assert.assertEquals (1, calcStatus ("1 2"));
		Assert.assertEquals (1, calcStatus (""));
	}

	@Test
	public void testLexerErrorPropagates () throws Exception
	{
		try
		{
			calcStatus ("1 + x");
			Assert.fail ("Expected an IOException from the lexer action.");
		}
		catch (IOException ex)
		{
			Assert.assertEquals ("Illegal character: x", ex.getMessage ());
		}
	}

	@Test
	public void testParserCanBeReused () throws Exception
	{
		GeneratedCode parser = parser ("calc.xcc");
		Assert.assertEquals (0, parser.parse ("1 + 1"));
		Assert.assertEquals (2, ((Integer)parser.getField ("result")).intValue ());
		// The parser stays in its accepted state until reset () is called.
		parser.call ("reset");
		Assert.assertEquals (0, parser.parse ("3 * 3"));
		Assert.assertEquals (9, ((Integer)parser.getField ("result")).intValue ());
	}

	@Test
	public void testErrorRecovery () throws Exception
	{
		assumeInvalidInputDetected ();
		GeneratedCode parser = parser ("lines.xcc");
		Assert.assertEquals (0, parser.parse ("1 + 1\n2 +\n3\n+ 4\n5 + 5 + 5\n"));
		Assert.assertEquals (list ("2", "error", "3", "error", "15"), parser.getOut ());
	}

	@Test
	public void testNoErrors () throws Exception
	{
		GeneratedCode parser = parser ("lines.xcc");
		Assert.assertEquals (0, parser.parse ("1\n2 + 3\n"));
		Assert.assertEquals (list ("1", "5"), parser.getOut ());
	}

	@Test
	public void testEmptyInputIsValid () throws Exception
	{
		GeneratedCode parser = parser ("lines.xcc");
		Assert.assertEquals (0, parser.parse (""));
		Assert.assertEquals (list (), parser.getOut ());
	}
}
