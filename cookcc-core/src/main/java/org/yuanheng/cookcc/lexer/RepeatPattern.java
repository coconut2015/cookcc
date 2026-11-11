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

/**
 * @author Heng Yuan
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
