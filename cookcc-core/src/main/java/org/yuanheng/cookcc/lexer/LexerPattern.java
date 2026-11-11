/*
 * Copyright (c) 2008-2013, Heng Yuan
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

class LexerPattern
{
	private final boolean m_bol;
	private final ChainPattern m_pattern;
	private final ChainPattern m_trailPattern;
	private int m_precedence;

	public LexerPattern (ChainPattern pattern, ChainPattern trailPattern, boolean bol, boolean eol)
	{
		m_pattern = pattern;
		m_bol = bol;
		if (eol)
		{
			if (trailPattern == null)
			{
				trailPattern = new ChainPattern (new CharPattern ('\n'));
			}
			else
			{
				trailPattern.addPattern (new CharPattern ('\n'));
			}
		}
		m_trailPattern = trailPattern;
	}

	public boolean requiresPreprocessing ()
	{
		return m_trailPattern != null || m_pattern.hasSubExpression ();
	}

	public int getTrailContext ()
	{
		if (m_trailPattern == null)
			return 0;
		if (m_pattern.getLength () > 0)
			return NFA.getTrailContext (m_pattern.getLength (), true, false);
		else
			return NFA.getTrailContext (m_trailPattern.getLength (), false, true);
	}

	public int getPrecedence ()
	{
		return m_precedence;
	}

	public void setPrecedence (int precedence)
	{
		m_precedence = precedence;
	}

	public boolean isBol ()
	{
		return m_bol;
	}

	public ChainPattern getPattern ()
	{
		return m_pattern;
	}

	@Override
	public String toString ()
	{
		StringBuffer buffer = new StringBuffer ();
		if (m_bol)
			buffer.append ('^');
		buffer.append (m_pattern);
		if (m_trailPattern != null)
		{
			boolean isEol = false;
			if (m_trailPattern.getLength () == 1)
			{
				Pattern pattern = m_trailPattern.getFirstPattern ();
				if (pattern instanceof CharPattern)
				{
					if (((CharPattern)pattern).getChar () == '\n')
					{
						isEol = true;
						buffer.append ('$');
					}
				}
			}
			if (!isEol)
				buffer.append ('/').append (m_trailPattern);
		}
		return m_pattern.toString ();
	}

	public int getLength ()
	{
		return m_pattern.getLength ();
	}

	public boolean hasSubExpression ()
	{
		return m_pattern.hasSubExpression ();
	}

	public NFA constructNFA (NFAFactory factory, int caseValue, long lineNumber, boolean nocase)
	{
		NFA start = factory.createNFA ();
		start.caseValue = caseValue;
		start.lineNumber = lineNumber;
		start.precedence = m_precedence;
		start.trailContext = getTrailContext ();
		start.nocase = nocase;

		NFA end = m_pattern.constructNFA (factory, start);
		if (m_trailPattern != null)
		{
			m_trailPattern.constructNFA (factory, end);
		}

		return start;
	}
}
