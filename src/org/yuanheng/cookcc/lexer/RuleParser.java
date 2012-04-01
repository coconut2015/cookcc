/*
 * Copyright (c) 2008, Heng Yuan
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yuanheng.cookcc.doc.ShortcutDoc;
import org.yuanheng.cookcc.exception.*;

/**
 * Hand written rule parser.
 *
 * @author Heng Yuan
 * @version $Id$
 */
public class RuleParser
{
	private final static Pattern m_replaceName = Pattern.compile ("\\{[a-zA-Z_][a-zA-Z0-9_-]*[}]");

	private final Lexer m_lexer;
	private final NFAFactory m_nfaFactory;
	private final CCL m_ccl;
	private final boolean m_nocase;
	private int m_trailContext;
	private boolean m_varLen;
	private int m_ruleLen;
	private boolean m_bol;

	private boolean[] m_singletonCharSet;
	private boolean[] m_cclCharSet;
	private boolean[] m_quoteCharSet;
	private boolean[] m_singleQuoteCharSet;

	private RuleLexer m_lex;
	private int m_lineNumber;

	private class RuleLexer
	{
		private String m_input;
		private String m_currentStr;
		private int m_pos;

		public RuleLexer (String input)
		{
			m_input = input;
			m_currentStr = input;
			m_pos = 0;
		}

		public boolean isEmpty ()
		{
			return m_pos == m_currentStr.length ();
		}

		public Character ifMatchEsc ()
		{
			if (!ifMatch ('\\'))
				return null;
			--m_pos;
			int[] escPos = new int[]{ m_pos };
			char ch = CCL.esc (m_currentStr, escPos);
			m_pos = escPos[0];
			return new Character (ch);
		}

		public boolean ifMatchReplaceName ()
		{
			if (!ifMatch ('{'))
				return false;
			--m_pos;		// rewind the forward;
			m_currentStr = m_currentStr.substring (m_pos);
			m_pos = 0;
			Matcher matcher = m_replaceName.matcher (m_currentStr);
			if (!matcher.find (0) || matcher.start () != 0)
				return false;
			int index = m_currentStr.indexOf ('}');
			String name = m_currentStr.substring (1, index);
			ShortcutDoc shortcut = m_lexer.getDocument ().getLexer ().getShortcut (name);
			if (shortcut == null)
				throw new UnknownNameException (m_lineNumber, name, m_input);
			m_currentStr = "(" + shortcut.getPattern () + ")" + m_currentStr.substring (index + 1);
			return true;
		}

		public boolean ifMatch (String str)
		{
			if ((m_currentStr.length () - m_pos) < str.length ())
				return false;
			int len = str.length ();
			int offset = m_pos;
			for (int i = 0; i < len; ++i)
				if (m_currentStr.charAt (offset++) != str.charAt (i))
					return false;
			m_pos = offset;
			return true;
		}

		public boolean ifMatch (char ch)
		{
			if (m_pos >= m_currentStr.length () || m_currentStr.charAt (m_pos) != ch)
				return false;
			++m_pos;
			return true;
		}

		public Character ifMatch (boolean[] charSet)
		{
			Character ch;
			if (m_pos >= m_currentStr.length () || !charSet[m_currentStr.charAt (m_pos)])
				return null;
			ch = new Character (m_currentStr.charAt (m_pos));
			++m_pos;
			return ch;
		}

		public void match (char ch)
		{
			if (m_pos >= m_currentStr.length () || m_currentStr.charAt (m_pos) != ch)
				throw new LookaheadException (m_lineNumber, m_ccl, ch, m_input, m_pos);
			++m_pos;
		}

		public String getInput ()
		{
			return m_input;
		}

		public int getPos ()
		{
			return m_pos;
		}
	}

	public RuleParser (Lexer lexer, NFAFactory nfaFactory)
	{
		this (lexer, nfaFactory, false);
	}

	public RuleParser (Lexer lexer, NFAFactory nfaFactory, boolean nocase)
	{
		m_lexer = lexer;
		m_nfaFactory = nfaFactory;
		m_ccl = nfaFactory.getCCL ();
		m_nocase = nocase;
		m_trailContext = 0;
		m_varLen = false;
		m_ruleLen = 0;
		m_bol = false;

		m_singletonCharSet = CCL.subtract (m_ccl.ANY.clone (), m_ccl.parseCCL ("[$/|*+?.(){}]]"));
		m_cclCharSet = CCL.subtract (m_ccl.ANY.clone (), m_ccl.parseCCL ("[-\\]\\n]"));
		m_quoteCharSet = m_ccl.parseCCL ("[^\"\\n]");
		m_singleQuoteCharSet = m_ccl.parseCCL ("[^'\\n]");
	}

	public boolean isBOL ()
	{
		return m_bol;
	}

	public NFA parse (int precedence, int lineNumber, String input)
	{
		m_lineNumber = lineNumber;
		m_lex = new RuleLexer (input);

		if (m_lex.ifMatch ('^'))
			m_bol = true;

		NFA head = parseRegex ();
		if (head == null)
			throw new InvalidRegExException (lineNumber, input);

		if (m_lex.ifMatch ('/'))
		{
			if (NFA.hasTrail (m_trailContext))
				throw new MultipleTrailContextException (lineNumber, input);
			if (m_varLen)
				m_ruleLen = 0;
			m_trailContext = NFA.getTrailContext (m_ruleLen, !m_varLen, false);

			NFA tail = parseRegex ();
			if (m_lex.ifMatch ('$'))
			{
				NFA eol = m_nfaFactory.createNFA ('\n', null);
				tail = tail == null ? eol : tail.cat (eol);
				++m_ruleLen;
			}
			if (tail == null)
				throw new ParserException (lineNumber, "unexpected '/'");
			if ((m_trailContext & NFA.TRAIL_MASK) != NFA.TRAIL_FIXHEAD)
			{
				if (m_varLen)
					throw new VariableTrailContextException (lineNumber, input);
				else
					m_trailContext = NFA.getTrailContext (m_ruleLen, false, true);
			}
			head = head.cat (tail);
		}
		else if (m_lex.ifMatch ('$'))
		{
			if (NFA.hasTrail (m_trailContext))
				throw new MultipleTrailContextException (lineNumber, input);
			m_trailContext = NFA.getTrailContext (1, false, true);
			head = head.cat (m_nfaFactory.createNFA ('\n', null));
		}

		head.setState (m_lexer.incCaseCounter (), precedence, lineNumber, m_trailContext);
		return head;
	}

	private NFA parseRegex ()
	{
		m_varLen = false;
		m_ruleLen = 0;
		NFA head = parseSeries ();
		if (head == null)
			throw new InvalidRegExException (m_lineNumber, m_lex.getInput ());
		while (m_lex.ifMatch ('|'))
		{
			NFA tail = parseSeries ();
			if (tail == null)
				throw new InvalidRegExException (m_lineNumber, m_lex.getInput ());
			head = head.or (tail);
		}

		return head;
	}

	private NFA parseSeries ()
	{
		NFA head = parseSingleton (null);
		if (head == null)
			throw new InvalidRegExException (m_lineNumber, m_lex.getInput ());
		NFA tail;
		while ((tail = parseSingleton (null)) != null)
			head = head.cat (tail);
		return head;
	}

	private NFA parseSingleton (NFA head)
	{
		while (true)
		{
			if (head == null)
			{
				boolean[] ccl;
				Character ch;
				if (m_lex.ifMatch ('.'))
				{
					++m_ruleLen;
					head = m_nfaFactory.createNFA (NFA.ISCCL, m_ccl.ANY);
				}
				else if (m_lex.ifMatch ("<<EOF>>"))
				{
					if (!m_lex.isEmpty ())
						throw new LookaheadException (m_lineNumber, m_ccl, m_ccl.EOF, m_lex.getInput (), m_lex.getPos ());
					head = m_nfaFactory.createNFA (m_ccl.EOF, null);
				}
				else if (m_lex.ifMatchReplaceName ())
				{
					continue;
				}
				else if ((ccl = parseFullCCL (true)) != null)
				{
					++m_ruleLen;
					head = m_nfaFactory.createNFA (NFA.ISCCL, ccl);
				}
				else if (m_lex.ifMatch ('"'))
				{
					head = parseString (m_quoteCharSet);
					if (head == null)
						throw new InvalidRegExException (m_lineNumber, m_lex.getInput ());
					m_lex.match ('"');
				}
				else if (m_lex.ifMatch ('\''))
				{
					head = parseString (m_singleQuoteCharSet);
					if (head == null)
						throw new InvalidRegExException (m_lineNumber, m_lex.getInput ());
					m_lex.match ('\'');
				}
				else if (m_lex.ifMatch ('('))
				{
					head = parseRegex ();
					if (head == null)
						throw new InvalidRegExException (m_lineNumber, m_lex.getInput ());
					m_lex.match (')');
				}
				else if ((ch = parseChar (m_singletonCharSet)) != null)
				{
					++m_ruleLen;
					if (m_nocase)
					{
						char c = ch.charValue ();
						ccl = m_ccl.EMPTY.clone ();
						ccl[c] = true;
						if (c >= 'a' && c <= 'z')
							ccl[c - 'a' + 'A'] = true;
						else if (c >= 'A' && c <= 'Z')
							ccl[c - 'A' + 'a'] = true;
						head = m_nfaFactory.createNFA (NFA.ISCCL, ccl);
					}
					else
						head = m_nfaFactory.createNFA (ch.charValue (), null);
				}
				else
					return null;
			}
			else
			{
				if (m_lex.ifMatch ('*'))
				{
					m_varLen = true;
					head = head.star ();
				}
				else if (m_lex.ifMatch ('+'))
				{
					m_varLen = true;
					head = head.plus ();
				}
				else if (m_lex.ifMatch ('?'))
				{
					m_varLen = true;
					head = head.q ();
				}
				if (m_lex.ifMatchReplaceName ())
				{
					continue;
				}
				else if (m_lex.ifMatch ('{'))
				{
					Integer number = parseNumber ();
					if (number == null || number.intValue () < 0)
						throw new BadIterationException (m_lineNumber, number);
					if (m_lex.ifMatch ('}'))
						head = head.repeat (number);
					else
					{
						int min, max;
						min = number.intValue ();
						m_lex.match (',');
						number = parseNumber ();
						if (number == null)
							max = -1;
						else
							max = number.intValue ();
						head = head.repeat (min, max);
						m_lex.match ('}');
					}
				}
				else
					break;
			}
		}
		return head;
	}

	private Character parseChar (boolean[] charSet)
	{
		Character ch = m_lex.ifMatchEsc ();
		if (ch == null)
			ch = m_lex.ifMatch (charSet);
		return ch;
	}

	private Integer parseNumber ()
	{
		Character ch;
		boolean neg = false;
		if (m_lex.ifMatch ('-'))
			neg = true;
		int number;
		if ((ch = parseChar (m_ccl.DIGIT)) == null)
			throw new LookaheadException (m_lineNumber, m_ccl, m_ccl.DIGIT, m_lex.getInput (), m_lex.getPos ());
		number = ch.charValue () - '0';
		while ((ch = parseChar (m_ccl.DIGIT)) != null)
			number = number * 10 + ch.charValue () - '0';
		if (neg)
			number = -number;
		return new Integer (number);
	}

	private NFA parseString (boolean[] charSet)
	{
		NFA head = null;
		Character ch;
		while ((ch = parseChar (charSet)) != null)
		{
			++m_ruleLen;
			NFA tail;
			if (m_nocase)
			{
				char c = ch.charValue ();
				boolean[] ccl = m_ccl.EMPTY.clone ();
				ccl[c] = true;
				if (c >= 'a' && c <= 'z')
					ccl[c - 'a' + 'A'] = true;
				else if (c >= 'A' && c <= 'Z')
					ccl[c - 'A' + 'a'] = true;
				tail = m_nfaFactory.createNFA (NFA.ISCCL, ccl);
			}
			else
				tail = m_nfaFactory.createNFA (ch.charValue (), null);
			if (head == null)
				head = tail;
			else
				head = head.cat (tail);
		}
		return head;
	}

	private boolean[] parseFullCCL (boolean matchNext)
	{
		if (!m_lex.ifMatch ('['))
			return null;
		boolean neg = false;
		if (m_lex.ifMatch ('^'))
			neg = true;
		boolean[] ccl = m_ccl.EMPTY.clone ();
		ccl = parseCCL (ccl);
		if (m_nocase)
		{
			for (int i = 'a'; i <= 'z'; ++i)
			{
				if (ccl[i])
					ccl[i - 'a' + 'A'] = true;
				if (ccl[i - 'a' + 'A'])
					ccl[i] = true;
			}
		}
		if (neg)
			CCL.negate (ccl);
		m_lex.match (']');
		if (matchNext)
		{
			for (;;)
			{
				if (m_lex.ifMatch ("{-}"))
				{
					boolean[] sub = parseFullCCL (false);
					if (sub == null)
						throw new LookaheadException (m_lineNumber, null, '[', m_lex.getInput (), m_lex.getPos ());
					ccl = CCL.subtract (ccl, sub);
				}
				else if (m_lex.ifMatch ("{+}"))
				{
					boolean[] sub = parseFullCCL (false);
					if (sub == null)
						throw new LookaheadException (m_lineNumber, null, '[', m_lex.getInput (), m_lex.getPos ());
					ccl = CCL.merge (ccl, sub);
				}
				else
					break;
			}
		}
		return ccl;
	}

	private boolean[] parseCCL (boolean[] ccl)
	{
		while (parseCCE (ccl) != null ||
			   parseCCLChar (ccl) != null)
			;
		return ccl;
	}

	private boolean[] parseCCLChar (boolean[] ccl)
	{
		Character start = parseChar (m_cclCharSet);
		if (start == null)
			return null;
		if (!m_lex.ifMatch ('-'))
		{
			ccl[start.charValue ()] = true;
			return ccl;
		}
		Character end = parseChar (m_cclCharSet);
		if (end == null)
		{
			// - is at the end, so just treat this character as a literal
			ccl['-'] = true;
			return ccl;
		}
		for (int i = start.charValue (); i <= end.charValue (); ++i)
			ccl[i] = true;
		return ccl;
	}

	private boolean[] parseCCE (boolean[] ccl)
	{
		if (m_lex.ifMatch ("[:lower:]"))
			return CCL.merge (ccl, m_ccl.LOWER);
		if (m_lex.ifMatch ("[:upper:]"))
			return CCL.merge (ccl, m_ccl.UPPER);
		if (m_lex.ifMatch ("[:ascii:]"))
			return CCL.merge (ccl, m_ccl.ASCII);
		if (m_lex.ifMatch ("[:alpha:]"))
			return CCL.merge (ccl, m_ccl.ALPHA);
		if (m_lex.ifMatch ("[:digit:]"))
			return CCL.merge (ccl, m_ccl.DIGIT);
		if (m_lex.ifMatch ("[:alnum:]"))
			return CCL.merge (ccl, m_ccl.ALNUM);
		if (m_lex.ifMatch ("[:punct:]"))
			return CCL.merge (ccl, m_ccl.PUNCT);
		if (m_lex.ifMatch ("[:graph:]"))
			return CCL.merge (ccl, m_ccl.GRAPH);
		if (m_lex.ifMatch ("[:print:]"))
			return CCL.merge (ccl, m_ccl.PRINT);
		if (m_lex.ifMatch ("[:blank:]"))
			return CCL.merge (ccl, m_ccl.BLANK);
		if (m_lex.ifMatch ("[:cntrl:]"))
			return CCL.merge (ccl, m_ccl.CNTRL);
		if (m_lex.ifMatch ("[:xdigit:]"))
			return CCL.merge (ccl, m_ccl.XDIGIT);
		if (m_lex.ifMatch ("[:space:]"))
			return CCL.merge (ccl, m_ccl.SPACE);

		if (m_lex.ifMatch ("[:^lower:]"))
			return CCL.merge (ccl, CCL.subtract (m_ccl.ANY.clone (), m_ccl.LOWER));
		if (m_lex.ifMatch ("[:^upper:]"))
			return CCL.merge (ccl, CCL.subtract (m_ccl.ANY.clone (), m_ccl.UPPER));
		if (m_lex.ifMatch ("[:^ascii:]"))
			return CCL.merge (ccl, CCL.subtract (m_ccl.ANY.clone (), m_ccl.ASCII));
		if (m_lex.ifMatch ("[:^alpha:]"))
			return CCL.merge (ccl, CCL.subtract (m_ccl.ANY.clone (), m_ccl.ALPHA));
		if (m_lex.ifMatch ("[:^digit:]"))
			return CCL.merge (ccl, CCL.subtract (m_ccl.ANY.clone (), m_ccl.DIGIT));
		if (m_lex.ifMatch ("[:^alnum:]"))
			return CCL.merge (ccl, CCL.subtract (m_ccl.ANY.clone (), m_ccl.ALNUM));
		if (m_lex.ifMatch ("[:^punct:]"))
			return CCL.merge (ccl, CCL.subtract (m_ccl.ANY.clone (), m_ccl.PUNCT));
		if (m_lex.ifMatch ("[:^graph:]"))
			return CCL.merge (ccl, CCL.subtract (m_ccl.ANY.clone (), m_ccl.GRAPH));
		if (m_lex.ifMatch ("[:^print:]"))
			return CCL.merge (ccl, CCL.subtract (m_ccl.ANY.clone (), m_ccl.PRINT));
		if (m_lex.ifMatch ("[:^blank:]"))
			return CCL.merge (ccl, CCL.subtract (m_ccl.ANY.clone (), m_ccl.BLANK));
		if (m_lex.ifMatch ("[:^cntrl:]"))
			return CCL.merge (ccl, CCL.subtract (m_ccl.ANY.clone (), m_ccl.CNTRL));
		if (m_lex.ifMatch ("[:^xdigit:]"))
			return CCL.merge (ccl, CCL.subtract (m_ccl.ANY.clone (), m_ccl.XDIGIT));
		if (m_lex.ifMatch ("[:^space:]"))
			return CCL.merge (ccl, CCL.subtract (m_ccl.ANY.clone (), m_ccl.SPACE));
		return null;
	}
}
