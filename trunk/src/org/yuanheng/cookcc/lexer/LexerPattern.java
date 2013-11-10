/*
 * Copyright (c) 2008-2013, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *    Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    Neither the name of the Heng Yuan nor the
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

class LexerPattern implements Pattern
{
	private final boolean m_bol;
	private final ChainPattern m_pattern;
	private final ChainPattern m_trailPattern;
	private int m_precedence;
	private String m_originalText;

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

	public void setOriginalText (String originalText)
	{
		m_originalText = originalText;
	}

	public String getOriginalText ()
	{
		return m_originalText;
	}

	public int getLength ()
	{
		return m_pattern.getLength ();
	}

	public NFA constructNFA (NFAFactory factory, NFA start)
	{
		start.trailContext = getTrailContext ();
		NFA end = m_pattern.constructNFA (factory, start);
		if (m_trailPattern == null)
			return end;
		return m_trailPattern.constructNFA (factory, end);
	}

	public boolean hasSubExpression ()
	{
		return m_pattern.hasSubExpression ();
	}

	public NFA constructNFA (NFAFactory factory, int caseValue, int lineNumber)
	{
		NFA start = factory.createNFA ();
		constructNFA (factory, start);
		start.setState (caseValue, m_precedence, lineNumber, start.trailContext);
		return start;
	}
}
