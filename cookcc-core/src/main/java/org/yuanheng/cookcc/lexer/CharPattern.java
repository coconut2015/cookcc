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
class CharPattern implements Pattern
{
	private final char m_char;

	public CharPattern (char ch)
	{
		m_char = ch;
	}

	public char getChar ()
	{
		return m_char;
	}

	public int getLength ()
	{
		return 1;
	}

	@Override
	public String toString ()
	{
		switch (m_char)
		{
			case '(':
			case ')':
				return "\\" + m_char;
			default:
				return CCL.toString (m_char);
		}
	}

	public NFA constructNFA (NFAFactory factory, NFA start)
	{
		factory.setNFA (start, m_char, null);
		NFA end = factory.createNFA (start);
		start.next = end;
		return end;
	}

	public boolean hasSubExpression ()
	{
		return false;
	}
}
