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
package org.yuanheng.cookcc;

import static org.yuanheng.cookcc.GeneratedCode.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests the behavior of a generated lexer (grammars/tokens.xcc).  Every test
 * runs with each lexer table format, with and without Unicode input, since
 * each combination builds and compresses the DFA differently but must scan
 * the same way.
 *
 * @author Heng Yuan
 */
@RunWith (Parameterized.class)
public class GeneratedLexerTest
{
	private static final String[] TABLES = { "ecs", "full", "compressed" };

	@ClassRule
	public static final TemporaryFolder s_folder = new TemporaryFolder ();

	/** The compiled lexer for each parameter combination, generated once. */
	private static final Map<String, GeneratedCode> s_lexers = new HashMap<String, GeneratedCode> ();

	@Parameters (name = "{0} table, unicode={1}")
	public static Collection<Object[]> parameters ()
	{
		List<Object[]> list = new ArrayList<Object[]> ();
		for (String table : TABLES)
		{
			list.add (new Object[] { table, Boolean.FALSE });
			// Not full + Unicode: each row of the 16-bit full table is one
			// 65536-character string constant, which is too long for javac (a
			// known limitation, see docs_src/Output-Java.rst).
			if (!"full".equals (table))
				list.add (new Object[] { table, Boolean.TRUE });
		}
		return list;
	}

	private final String m_table;
	private final boolean m_unicode;

	public GeneratedLexerTest (String table, Boolean unicode)
	{
		m_table = table;
		m_unicode = unicode.booleanValue ();
	}

	/**
	 * Returns a new instance of the lexer for this parameter combination.
	 * The code is generated and compiled the first time only.
	 */
	private GeneratedCode lexer () throws Exception
	{
		String key = m_table + "/" + m_unicode;
		GeneratedCode lexer = s_lexers.get (key);
		if (lexer == null)
		{
			String[] options = m_unicode ? new String[] { "-lexertable", m_table, "-unicode" }
										 : new String[] { "-lexertable", m_table };
			lexer = GeneratedCode.generate (GeneratedCode.parseXml (GeneratedCode.grammar ("tokens.xcc")),
											s_folder.newFolder (), "TokenLexer", options);
			s_lexers.put (key, lexer);
		}
		return lexer.newInstance ();
	}

	@Test
	public void testKeywordBeatsIdentifierOfSameLength () throws Exception
	{
		Assert.assertEquals (list ("IF", "ID:iffy", "ID:x1"), lexer ().lex ("if iffy x1"));
	}

	@Test
	public void testLongestMatch () throws Exception
	{
		Assert.assertEquals (list ("EQ", "ASSIGN", "EQ", "ASSIGN"), lexer ().lex ("== = ==="));
	}

	@Test
	public void testShortcut () throws Exception
	{
		Assert.assertEquals (list ("NUM:42", "NUM:7"), lexer ().lex ("42 7"));
	}

	@Test
	public void testTrailingContext () throws Exception
	{
		// The trailing "px" counts toward the match length, but is scanned again.
		Assert.assertEquals (list ("PIXELS:10", "ID:px", "NUM:10", "ID:pt"), lexer ().lex ("10px 10pt"));
	}

	@Test
	public void testBeginningOfLine () throws Exception
	{
		Assert.assertEquals (list ("DIRECTIVE:#define a", "NL", "ID:x", "CHAR:#", "ID:y"),
							 lexer ().lex ("#define a\nx #y"));
	}

	@Test
	public void testBeginningOfLineAfterNewLine () throws Exception
	{
		Assert.assertEquals (list ("ID:x", "NL", "DIRECTIVE:#y"), lexer ().lex ("x\n#y"));
	}

	@Test
	public void testLexerStates () throws Exception
	{
		Assert.assertEquals (list ("ID:a", "ID:b"), lexer ().lex ("a /* if 1\n== */ b"));
	}

	@Test
	public void testEndOfFileInState () throws Exception
	{
		Assert.assertEquals (list ("ID:a", "UNTERMINATED"), lexer ().lex ("a /* never closed"));
	}

	@Test
	public void testEmptyInput () throws Exception
	{
		Assert.assertEquals (list (), lexer ().lex (""));
	}

	@Test
	public void testCatchAll () throws Exception
	{
		Assert.assertEquals (list ("CHAR:?", "CHAR:!", "ID:z"), lexer ().lex ("?!z"));
	}

	@Test
	public void testNonAsciiInput () throws Exception
	{
		if (m_unicode)
			Assert.assertEquals (list ("ID:a", "CHAR:é", "CHAR:中"), lexer ().lex ("aé中"));
		else
		{
			// A byte lexer sees each byte of the UTF-8 encoding as a character:
			// C3 A9 for U+00E9, then E4 B8 AD for U+4E2D.  Only the count is
			// checked, since yyText () decodes the bytes with the platform's
			// default charset.
			List<String> tokens = lexer ().lex ("é中");
			Assert.assertEquals (tokens.toString (), 5, tokens.size ());
			for (String token : tokens)
				Assert.assertTrue (token, token.startsWith ("CHAR:"));
		}
	}

	@Test
	public void testLongInput () throws Exception
	{
		// Longer than the default buffer, so the lexer has to refill it.
		StringBuilder input = new StringBuilder ();
		List<String> expected = new ArrayList<String> ();
		for (int i = 0; i < 5000; ++i)
		{
			input.append ("v").append (i).append (" = ").append (i).append ('\n');
			expected.add ("ID:v" + i);
			expected.add ("ASSIGN");
			expected.add ("NUM:" + i);
			expected.add ("NL");
		}
		Assert.assertEquals (expected, lexer ().lex (input.toString ()));
	}
}
