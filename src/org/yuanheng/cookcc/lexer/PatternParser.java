/*
 * Copyright (c) 2008-2012, Heng Yuan
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
package org.yuanheng.cookcc.lexer;

import java.io.StringReader;

import org.yuanheng.cookcc.CookCCOption;
import org.yuanheng.cookcc.CookCCToken;
import org.yuanheng.cookcc.Lex;
import org.yuanheng.cookcc.Lexs;
import org.yuanheng.cookcc.Rule;
import org.yuanheng.cookcc.Shortcut;
import org.yuanheng.cookcc.Shortcuts;
import org.yuanheng.cookcc.TokenGroup;
import org.yuanheng.cookcc.TokenType;
import org.yuanheng.cookcc.exception.NestedSubExpressionException;
import org.yuanheng.cookcc.exception.VariableTrailContextException;

/**
 * @author Heng Yuan
 * @version $Id$
 */
@CookCCOption (start = "Start", unicode = true)
public class PatternParser extends PatternScanner
{
	@CookCCToken
	static enum Token
	{
		LQUOTE,
		RQUOTE,
		LPAREN,
		RPAREN,
		SUBEXPLPAREN,
		SUBEXPRPAREN,
		SLASH,
		DOT,

		@TokenGroup (type = TokenType.LEFT)
		OR,
		CCADD,
		CCMINUS,

		@TokenGroup
		DOLLAR,
		CHARCLASS,
		CHAR,

		@TokenGroup
		STAR,
		PLUS,
		QUESTION,
		REPEAT
	}

	private final CCL m_ccl;

	private int m_lineNumber;
	private String m_input;
	private StringBuffer m_cclBuffer;
	private boolean m_bol;
	private int m_subExpIdCounter;
	private LexerPattern m_pattern;

	public PatternParser (CCL ccl)
	{
		m_ccl = ccl;
	}

	private void validateLexerPattern (ChainPattern pattern, ChainPattern trailPattern)
	{
		if (trailPattern != null)
		{
			if (pattern.getLength () < 0 &&
				trailPattern.getLength () < 0)
			{
				throw new VariableTrailContextException (m_lineNumber, m_input);
			}

			if (trailPattern.hasSubExpression ())
			{
				throw new NestedSubExpressionException (m_lineNumber, m_input);
			}
		}
	}

	public LexerPattern parse (int lineNumber, String text)
	{
		try
		{
			m_lineNumber = lineNumber;
			m_input = text;
			setInput (new StringReader (text));
			boolean ok = (yyParse () == 0);
			LexerPattern p = m_pattern;
			p.setOriginalText (text);
			reset ();
			m_pattern = null;
			if (ok)
				return p;
			return null;
		}
		catch (Exception ex)
		{
			ex.printStackTrace ();
			return null;
		}
	}

	@Override
	public void reset ()
	{
		super.reset ();
		m_pattern = null;
		m_bol = false;
		m_subExpIdCounter = 0;
	}

	////////////////////////////////////////////////////////////////////////
	//
	// Lexer section
	//
	////////////////////////////////////////////////////////////////////////

	@Shortcuts ( shortcuts = {
		@Shortcut (name="nonws", pattern="[^ \\t\\n]"),
		@Shortcut (name="ws", pattern="[ \\t]")
	})
	@Lex (pattern = "^'^'")
	void scanBOL ()
	{
		m_bol = true;
	}

	@Lexs (patterns = {
		@Lex (pattern = "'.'", token = "DOT"),
		@Lex (pattern = "'*'", token = "STAR"),
		@Lex (pattern = "'+'", token = "PLUS"),
		@Lex (pattern = "'|'", token = "OR"),
		@Lex (pattern = "'?'", token = "QUESTION"),
		@Lex (pattern = "'$'", token = "DOLLAR"),
		@Lex (pattern = "'('", token = "LPAREN"),
		@Lex (pattern = "')'", token = "RPAREN"),
		@Lex (pattern = "'/'", token = "SLASH"),
		@Lex (pattern = "'{+}'", token = "CCADD"),
		@Lex (pattern = "'{-}'", token = "CCMINUS")
	})
	void scanSymbol ()
	{
	}

	@Lex (pattern = "'(/'", token = "SUBEXPLPAREN")
	Integer scanSubExpStart ()
	{
		return ++m_subExpIdCounter;
	}

	@Lex (pattern = "'/)'", token = "SUBEXPRPAREN")
	Integer scanSubExpEnd ()
	{
		return m_subExpIdCounter;
	}

	@Lex (pattern = "'['")
	void scanCCLStart ()
	{
		begin ("CCLSTATE");

		if (m_cclBuffer == null)
			m_cclBuffer = new StringBuffer ();
		else
			m_cclBuffer.setLength (0);
		m_cclBuffer.append ('[');
	}

	@Lex (pattern = "'\\\\'([0-9]{1,3})", token = "CHAR", state = "INITIAL, SQUOTE, DQUOTE")
	Character scanOct ()
	{
		return (char)Integer.parseInt (yyText ().substring (1), 8);
	}

	@Lex (pattern = "'\\\\x'[a-fA-F0-9]{1,2}", token = "CHAR", state = "INITIAL, SQUOTE, DQUOTE")
	Character scanHex ()
	{
		return (char)Integer.parseInt (yyText ().substring (2), 16);
	}

	@Lex (pattern = "'\\\\u'[a-fA-F0-9]{4}", token = "CHAR", state = "INITIAL, SQUOTE, DQUOTE")
	Character scanUnicode ()
	{
		return (char)Integer.parseInt (yyText ().substring (2), 16);
	}

	// Invalid cases for escape characters
	@Lexs (patterns = {
		@Lex (pattern = "'\\\\x'", state = "INITIAL, SQUOTE, DQUOTE", token = "error"),
		@Lex (pattern = "'\\\\u'", state = "INITIAL, SQUOTE, DQUOTE", token = "error")
	})
	void scanEscapeError ()
	{
	}

	@Lex (pattern = "'\\\\'.", token = "CHAR", state = "INITIAL, SQUOTE, DQUOTE")
	Character scanEscape ()
	{
		return CCL.esc (yyText (), new int[1]);
	}

	// This is the case where escape character is the last character in the pattern
	@Lex (pattern = "'\\\\'", state = "INITIAL, SQUOTE, DQUOTE", token = "error")
	void scanEscapeError2 ()
	{
	}

	@Lex (pattern = "'{[a-zA-Z_][a-zA-Z0-9_]*}'")
	void scanShortCut ()
	{
	}

	@Lex (pattern = "'{'{ws}*([0-9]+){ws}*'}'", token = "REPEAT")
	Repeat scanRepeat ()
	{
		String text = yyText ();
		int count = Integer.parseInt (text.substring (1, text.length () - 1).trim ());
		return new Repeat (count, count);
	}

	@Lex (pattern = "'{'{ws}*,{ws}*([0-9]+){ws}*'}'", token = "REPEAT")
	Repeat scanRepeat2 ()
	{
		String text = yyText ();
		text = text.substring (1, text.length () - 1).trim ();	// remove { }
		text = text.substring (1);	// remove ,
		int count = Integer.parseInt (text.trim ());
		return new Repeat (0, count);
	}

	@Lex (pattern = "'{'{ws}*([0-9]+){ws}*','{ws}*'}'", token = "REPEAT")
	Repeat scanRepeat3 ()
	{
		String text = yyText ();
		text = text.substring (1, text.length () - 1).trim ();	// remove { }
		text = text.substring (0, text.length () - 1);	// remove ,
		int count = Integer.parseInt (text.trim ());
		return new Repeat (count, Integer.MAX_VALUE);
	}

	@Lex (pattern = "'{'{ws}*([0-9]+){ws}*','{ws}*([0-9]+){ws}*'}'", token = "REPEAT")
	Repeat scanRepeat4 ()
	{
		String text = yyText ();
		text = text.substring (1, text.length () - 1).trim ();	// remove { }
		String[] texts = text.split (",");
		return new Repeat (Integer.parseInt (texts[0].trim ()), Integer.parseInt (texts[1].trim ()));
	}

	@Lex (pattern = "'{'", token = "error")
	void scanRepeatError ()
	{
	}

	@Lex (pattern = "[\']", token = "LQUOTE")
	void scanSQStringStart ()
	{
		begin ("SQUOTE");
	}

	@Lex (pattern = "[\"]", token = "LQUOTE")
	void scanDQStringStart ()
	{
		begin ("DQUOTE");
	}

	@Lex (pattern = ".|\\n", token = "CHAR")
	Character scanChar ()
	{
		return yyText ().charAt (0);
	}

	@Lex (pattern = "<<EOF>>")
	int scanEof ()
	{
		return 0;
	}

	////////////////////////////////////////////////////////////////////////
   	// CCL
	////////////////////////////////////////////////////////////////////////
	@Lex (pattern = "']'", state = "CCLSTATE", token = "CHARCLASS")
	boolean[] scanCCLEnd ()
	{
		begin ("INITIAL");
		m_cclBuffer.append (']');
		return m_ccl.parseCCL (m_cclBuffer.toString ());
	}

	@Lex (pattern = "'['", state = "CCLSTATE", token = "error")
	void scanCCLError ()
	{
	}

	@Lexs (patterns = {
		@Lex (pattern = "\\.", state = "CCLSTATE"),
		@Lex (pattern = "[^\\[\\]]+", state = "CCLSTATE"),
		@Lex (pattern = "'[:'('^'?)([a-zA-Z]+)':]'", state = "CCLSTATE")
	})
	void scanCCLText ()
	{
		m_cclBuffer.append (yyText ());
	}

	////////////////////////////////////////////////////////////////////////
   	// QUOTE
	////////////////////////////////////////////////////////////////////////

	@Lexs (patterns = {
		@Lex (pattern = "[']", state = "SQUOTE", token = "RQUOTE"),
		@Lex (pattern = "[\"]", state = "DQUOTE", token = "RQUOTE")
	})
	void scanQuoteEnd ()
	{
		begin ("INITIAL");
	}

	@Lex (pattern = ".|\\n", state = "SQUOTE, DQUOTE", token = "CHAR")
	Character scanQuoteChar ()
	{
		return yyText ().charAt (0);
	}

	@Lex (pattern = "<<EOF>>", state = "CCLSTATE, SQUOTE, DQUOTE", token = "error")
	void scanEofError ()
	{
	}

	////////////////////////////////////////////////////////////////////////
	//
	// Parser section
	//
	////////////////////////////////////////////////////////////////////////

	@Rule (lhs = "Start", rhs = "LexerPattern", args = "1")
	int parseStart (LexerPattern lexerPattern)
	{
		m_pattern = lexerPattern;
		return 0;
	}

	@Rule (lhs = "LexerPattern", rhs = "Patterns", args = "1")
	LexerPattern parseLexerPattern (ChainPattern pattern)
	{
		return new LexerPattern (pattern, null, m_bol, false);
	}

	@Rule (lhs = "LexerPattern", rhs = "Patterns DOLLAR", args = "1")
	LexerPattern parseLexerPatternEol (ChainPattern pattern)
	{
		return new LexerPattern (pattern, null, m_bol, true);
	}
	
	@Rule (lhs = "LexerPattern", rhs = "Patterns SLASH Patterns", args = "1 3")
	LexerPattern parseLexerPattern (ChainPattern pattern, ChainPattern trailPattern)
	{
		validateLexerPattern (pattern, trailPattern);
		return new LexerPattern (pattern, trailPattern, m_bol, false);
	}

	@Rule (lhs = "LexerPattern", rhs = "Patterns SLASH Patterns DOLLAR", args = "1 3")
	LexerPattern parseLexerPatternEol (ChainPattern pattern, ChainPattern trailPattern)
	{
		validateLexerPattern (pattern, trailPattern);
		return new LexerPattern (pattern, trailPattern, m_bol, true);
	}

	@Rule (lhs = "Patterns", rhs = "Pattern", args = "1")
	ChainPattern parsePatterns (Pattern pattern)
	{
		return new ChainPattern (pattern);
	}

	@Rule (lhs = "Patterns", rhs = "Patterns Pattern", args = "1 2")
	ChainPattern parsePatterns (ChainPattern patterns, Pattern pattern)
	{
		patterns.addPattern (pattern);
		return patterns;
	}

	@Rule (lhs = "Patterns", rhs = "Patterns OR Patterns", args = "1 3")
	ChainPattern parseOrPattern (ChainPattern p1, ChainPattern p2)
	{
		return OrPattern.getOrPattern (m_ccl, p1, p2);
	}

	@Rule (lhs = "Pattern", rhs = "LPAREN Patterns RPAREN", args = "2")
	ChainPattern parseParenPattern (ChainPattern pattern)
	{
		return pattern;
	}

	@Rule (lhs = "Pattern", rhs = "SUBEXPLPAREN Patterns SUBEXPRPAREN", args = "1 2 3")
	ChainPattern parseSubExpPattern (Integer subExpId, ChainPattern pattern, Integer matchingSubExpId)
	{
		if (subExpId.intValue () != matchingSubExpId.intValue ())
		{
			throw new NestedSubExpressionException (m_lineNumber, m_input);
		}
		pattern.setSubExpId (subExpId);
		return pattern;
	}

	@Rule (lhs = "Pattern", rhs = "LQUOTE Patterns RQUOTE", args = "2")
	ChainPattern parseQuotePattern (Pattern pattern)
	{
		return new ChainPattern (pattern);
	}

	@Rule (lhs = "Pattern", rhs = "CHAR", args = "1")
	Pattern parseCharPattern (Character ch)
	{
		return new CharPattern (ch);
	}

	@Rule (lhs = "Pattern", rhs = "DOT")
	Pattern parseDotPattern ()
	{
		return new CCLPattern (m_ccl.ANY);
	}

	@Rule (lhs = "Pattern", rhs = "Pattern STAR", args = "1")
	Pattern parseStarPattern (Pattern pattern)
	{
		return new StarPattern (pattern);
	}

	@Rule (lhs = "Pattern", rhs = "Pattern PLUS", args = "1")
	Pattern parsePlusPattern (Pattern pattern)
	{
		return new PlusPattern (pattern);
	}

	@Rule (lhs = "Pattern", rhs = "Pattern QUESTION", args = "1")
	Pattern parseQuestionPattern (Pattern pattern)
	{
		return new QuestionPattern (pattern);
	}

	@Rule (lhs = "Pattern", rhs = "Pattern REPEAT", args = "1 2")
	Pattern parseRepeatPattern (Pattern pattern, Repeat repeat)
	{
		return new RepeatPattern (pattern, repeat);
	}

	@Rule (lhs = "Pattern", rhs = "CharGroup", args = "1")
	Pattern parseCCLPattern (boolean[] charGroup)
	{
		return new CCLPattern (charGroup);
	}

	@Rule (lhs = "CharGroup", rhs = "CHARCLASS", args = "1")
	boolean[] parseCharGroup (boolean[] ccl)
	{
		return ccl;
	}

	@Rule (lhs = "CharGroup", rhs = "CharGroup CCMINUS CharGroup", args = "1 3")
	boolean[] parseCharGroupMinus (boolean[] ccl1, boolean[] ccl2)
	{
		return CCL.subtract (ccl1, ccl2);
	}

	@Rule (lhs = "CharGroup", rhs = "CharGroup CCADD CharGroup", args = "1 3")
	boolean[] parseCharGroupMerge (boolean[] ccl1, boolean[] ccl2)
	{
		return CCL.merge (ccl1, ccl2);
	}
}
