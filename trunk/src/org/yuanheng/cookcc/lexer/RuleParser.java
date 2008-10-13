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

import org.yuanheng.cookcc.exception.*;

/**
 * Hand written rule parser.
 *
 * @author Heng Yuan
 * @version $Id$
 */
public class RuleParser
{
	private static int s_caseCount = 0;

	private final NFAFactory m_nfaFactory;
	private final CCL m_ccl;
	private final boolean m_nocase;
	private int m_trailContext;
	private boolean m_varLen;
	private int m_ruleLen;
	private boolean m_bol;

	private boolean[] m_singletonCharSet;
	private boolean[] m_cclCharSet;

	public RuleParser (NFAFactory nfaFactory)
	{
		this (nfaFactory, false);
	}

	public RuleParser (NFAFactory nfaFactory, boolean nocase)
	{
		m_nfaFactory = nfaFactory;
		m_ccl = nfaFactory.getCCL ();
		m_nocase = nocase;
		m_trailContext = 0;
		m_varLen = false;
		m_ruleLen = 0;
		m_bol = false;

		m_singletonCharSet = CCL.subtract (m_ccl.ANY.clone (), m_ccl.parseCCL ("[/|*+?.(){}]]"));
		m_cclCharSet = CCL.subtract (m_ccl.ANY.clone (), m_ccl.parseCCL ("[-\\]\\n]"));
	}

	public boolean isBOL ()
	{
		return m_bol;
	}

	public NFA parse (int lineNumber, String input)
	{
		char[] inputChars = input.toCharArray ();
		int[] pos = new int[1];

		if (ifMatch (inputChars, pos, '^'))
			m_bol = true;

		NFA head = parseRegex (lineNumber, inputChars, pos);
		if (head == null)
			throw new InvalidRegExException (lineNumber, inputChars);

		if (ifMatch (inputChars, pos, '/'))
		{
			if (NFA.hasTrail (m_trailContext))
				throw new MultipleTrailContextException (lineNumber, inputChars);
			if (m_varLen)
				m_ruleLen = 0;
			m_trailContext = NFA.setTrailContext (m_ruleLen, !m_varLen, false);

			NFA tail = parseRegex (lineNumber, inputChars, pos);
			if (ifMatch (inputChars, pos, '$'))
			{
				NFA eol = m_nfaFactory.getEOL ();
				if (tail == null)
					tail = eol;
				else
					tail = tail.cat (eol);
				++m_ruleLen;
			}
			if (tail == null)
				throw new ParserException (lineNumber, "unexpected '/'");
			if ((m_trailContext & NFA.TRAIL_MASK) != NFA.TRAIL_FIXHEAD)
			{
				if (m_varLen)
					throw new VariableTrailContextException (lineNumber, inputChars);
				else
					m_trailContext = NFA.setTrailContext (m_ruleLen, false, true);
			}
			head = head.cat (tail);
		}
		else if (ifMatch (inputChars, pos, '$'))
		{
			if (NFA.hasTrail (m_trailContext))
				throw new MultipleTrailContextException (lineNumber, inputChars);
			m_trailContext = NFA.setTrailContext (1, false, true);
			head = head.cat (m_nfaFactory.createNFA ('\n', null));
		}

		head.setState (s_caseCount++, m_trailContext, true);
		return head;
	}

	private NFA parseRegex (int lineNumber, char[] inputChars, int[] pos)
	{
		m_varLen = false;
		m_ruleLen = 0;
		NFA head = parseSeries (lineNumber, inputChars, pos);
		if (head == null)
			throw new InvalidRegExException (lineNumber, inputChars);
		while (ifMatch (inputChars, pos, '|'))
		{
			NFA tail = parseSeries (lineNumber, inputChars, pos);
			if (tail == null)
				throw new InvalidRegExException (lineNumber, inputChars);
			head = head.or (tail);
		}

		return head;
	}

	private NFA parseSeries (int lineNumber, char[] inputChars, int[] pos)
	{
		NFA head = parseSingleton (lineNumber, inputChars, pos, null);
		if (head == null)
			throw new InvalidRegExException (lineNumber, inputChars);
		NFA tail;
		while ((tail = parseSingleton (lineNumber, inputChars, pos, null)) != null)
			head = head.cat (tail);
		return head;
	}

	private NFA parseSingleton (int lineNumber, char[] inputChars, int[] pos, NFA head)
	{
		while (true)
		{
			if (head == null)
			{
				if (pos[0] >= inputChars.length)
					return null;

				boolean[] ccl;
				Character ch;
				if (ifMatch (inputChars, pos, '.'))
				{
					++m_ruleLen;
					head = m_nfaFactory.createNFA (NFA.ISCCL, m_ccl.ANY);
				}
				else if ((ccl = parseFullCCL (lineNumber, inputChars, pos)) != null)
				{
					++m_ruleLen;
					head = m_nfaFactory.createNFA (NFA.ISCCL, ccl);
				}
				else if (ifMatch (inputChars, pos, '"'))
				{
					head = parseString (lineNumber, inputChars, pos);
					if (head == null)
						throw new InvalidRegExException (lineNumber, inputChars);
					match (lineNumber, inputChars, pos, '"');
				}
				else if (ifMatch (inputChars, pos, '('))
				{
					head = parseRegex (lineNumber, inputChars, pos);
					if (head == null)
						throw new InvalidRegExException (lineNumber, inputChars);
					match (lineNumber, inputChars, pos, ')');
				}
				else if ((ch = parseChar (lineNumber, inputChars, pos, m_singletonCharSet)) != null)
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
				if (ifMatch (inputChars, pos, '*'))
				{
					m_varLen = true;
					head = head.star ();
				}
				else if (ifMatch (inputChars, pos, '+'))
				{
					m_varLen = true;
					head = head.plus ();
				}
				else if (ifMatch (inputChars, pos, '?'))
				{
					m_varLen = true;
					head = head.q ();
				}
				else if (ifMatch (inputChars, pos, '{'))
				{
					Integer number = parseNumber (lineNumber, inputChars, pos);
					if (number == null || number.intValue () < 0)
						throw new BadIterationException (lineNumber, number);
					if (ifMatch (inputChars, pos, '}'))
						head = head.repeat (number);
					else
					{
						int min, max;
						min = number.intValue ();
						match (lineNumber, inputChars, pos, ',');
						number = parseNumber (lineNumber, inputChars, pos);
						if (number == null)
							max = -1;
						else
							max = number.intValue ();
						head = head.repeat (min, max);
						match (lineNumber, inputChars, pos, '}');
					}
				}
				else
					break;
			}
		}
		return head;
	}

	private Character parseChar (int lineNumber, char[] inputChars, int[] pos, boolean[] charSet)
	{
		if (pos[0] >= inputChars.length || !charSet[inputChars[pos[0]]])
			return null;
		Character ch = new Character (inputChars[pos[0]]);
		++pos[0];
		return ch;
	}

	private Integer parseNumber (int lineNumber, char[] inputChars, int[] pos)
	{
		Character ch;
		boolean neg = false;
		if (ifMatch (inputChars, pos, '-'))
			neg = true;
		int number;
		if ((ch = parseChar (lineNumber, inputChars, pos, m_ccl.DIGIT)) == null)
			throw new LookaheadException (lineNumber, m_ccl, m_ccl.DIGIT, inputChars, pos[0]);
		number = ch.charValue () - '0';
		while ((ch = parseChar (lineNumber, inputChars, pos, m_ccl.DIGIT)) != null)
			number = number * 10 + ch.charValue () - '0';
		if (neg)
			number = -number;
		return new Integer (number);
	}

	private NFA parseString (int lineNumber, char[] inputChars, int[] pos)
	{
		NFA head = null;
		Character ch;
		while ((ch = parseChar (lineNumber, inputChars, pos, m_singletonCharSet)) != null)
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

	private boolean[] parseFullCCL (int lineNumber, char[] inputChars, int[] pos)
	{
		if (inputChars[pos[0]] != '[')
			return null;
		++pos[0];
		boolean neg = false;
		if (inputChars[pos[0]] == '^')
		{
			neg = true;
			++pos[0];
		}
		boolean[] ccl = m_ccl.EMPTY.clone ();
		ccl = parseCCL (lineNumber, inputChars, pos, ccl);
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
		match (lineNumber, inputChars, pos, ']');
		return ccl;
	}

	private boolean[] parseCCL (int lineNumber, char[] inputChars, int[] pos, boolean[] ccl)
	{
		while (parseCCE (inputChars, pos, ccl) != null ||
			   parseCCLChar (lineNumber, inputChars, pos, ccl) != null)
			;
		return ccl;
	}

	private boolean[] parseCCLChar (int lineNumber, char[] inputChars, int[] pos, boolean[] ccl)
	{
		Character start = parseChar (lineNumber, inputChars, pos, m_cclCharSet);
		if (start == null)
			return null;
		if (!ifMatch (inputChars, pos, '-'))
		{
			ccl[start.charValue ()] = true;
			return ccl;
		}
		Character end = parseChar (lineNumber, inputChars, pos, m_cclCharSet);
		if (end == null)
			throw new LookaheadException (lineNumber, m_ccl, m_cclCharSet, inputChars, pos[0]);
		for (int i = start.charValue (); i <= end.charValue (); ++i)
			ccl[i] = true;
		return ccl;
	}

	private boolean[] parseCCE (char[] inputChars, int[] pos, boolean[] ccl)
	{
		if (ifMatch (inputChars, pos, "[:lower:]"))
			return CCL.merge (ccl, m_ccl.LOWER);
		if (ifMatch (inputChars, pos, "[:upper:]"))
			return CCL.merge (ccl, m_ccl.UPPER);
		if (ifMatch (inputChars, pos, "[:ascii:]"))
			return CCL.merge (ccl, m_ccl.ASCII);
		if (ifMatch (inputChars, pos, "[:alpha:]"))
			return CCL.merge (ccl, m_ccl.ALPHA);
		if (ifMatch (inputChars, pos, "[:digit:]"))
			return CCL.merge (ccl, m_ccl.DIGIT);
		if (ifMatch (inputChars, pos, "[:alnum:]"))
			return CCL.merge (ccl, m_ccl.ALNUM);
		if (ifMatch (inputChars, pos, "[:punct:]"))
			return CCL.merge (ccl, m_ccl.PUNCT);
		if (ifMatch (inputChars, pos, "[:graph:]"))
			return CCL.merge (ccl, m_ccl.GRAPH);
		if (ifMatch (inputChars, pos, "[:print:]"))
			return CCL.merge (ccl, m_ccl.PRINT);
		if (ifMatch (inputChars, pos, "[:blank:]"))
			return CCL.merge (ccl, m_ccl.BLANK);
		if (ifMatch (inputChars, pos, "[:cntrl:]"))
			return CCL.merge (ccl, m_ccl.CNTRL);
		if (ifMatch (inputChars, pos, "[:xdigit:]"))
			return CCL.merge (ccl, m_ccl.XDIGIT);
		if (ifMatch (inputChars, pos, "[:space:]"))
			return CCL.merge (ccl, m_ccl.SPACE);
		return null;
	}

	private boolean ifMatch (char[] inputChars, int[] pos, String str)
	{
		if (inputChars.length - pos[0] < str.length ())
			return false;
		int len = str.length ();
		int offset = pos[0];
		for (int i = 0; i < len; ++i)
			if (inputChars[offset++] != str.charAt (i))
				return false;
		pos[0] = offset;
		return true;
	}

	private boolean ifMatch (char[] inputChars, int[] pos, char ch)
	{
		if (pos[0] >= inputChars.length || inputChars[pos[0]] != ch)
			return false;
		++pos[0];
		return true;
	}

	private void match (int lineNumber, char[] inputChars, int[] pos, char ch)
	{
		if (pos[0] >= inputChars.length || inputChars[pos[0]] != ch)
			throw new LookaheadException (lineNumber, m_ccl, ch, inputChars, pos[0]);
		++pos[0];
	}
}
