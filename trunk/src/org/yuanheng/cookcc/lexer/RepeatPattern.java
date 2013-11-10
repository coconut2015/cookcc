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

/**
 * @author Heng Yuan
 * @version $Id$
 */
class RepeatPattern implements Pattern
{
	private final Pattern m_pattern;
	private final Repeat m_repeat;
	private final int m_length;

	public RepeatPattern (Pattern pattern, Repeat repeat)
	{
		m_pattern = pattern;
		m_repeat = repeat;

		if (m_pattern.getLength () > 0 &&
			m_repeat.getMin () == m_repeat.getMax ())
		{
			m_length = m_pattern.getLength () * m_repeat.getMax ();
		}
		else
			m_length = -1;
	}

	public int getLength ()
	{
		return m_length;
	}

	public NFA constructNFA (NFAFactory factory, NFA start)
	{
		int min = m_repeat.getMin ();
		int max = m_repeat.getMax ();

		for (int i = 0; i < min; ++i)
		{
			start = m_pattern.constructNFA (factory, start);
		}

		for (int i = min; i < max; ++i)
		{
			start.next = factory.createNFA (start);
			NFA end = m_pattern.constructNFA (factory, start.next);
			start.next2 = end;
			start = end;
		}
		return start;
	}

	@Override
	public String toString ()
	{
		return m_pattern.toString () + m_repeat.toString ();
	}

	public boolean hasSubExpression ()
	{
		return m_pattern.hasSubExpression ();
	}
}
