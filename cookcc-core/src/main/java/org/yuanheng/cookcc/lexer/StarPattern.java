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
class StarPattern implements Pattern
{
	private final Pattern m_pattern;

	public StarPattern (Pattern pattern)
	{
		m_pattern = pattern;
	}

	public int getLength ()
	{
		return -1;
	}

	@Override
	public String toString ()
	{
		return m_pattern + "*";
	}

	public NFA constructNFA (NFAFactory factory, NFA start)
	{
		start.next = factory.createNFA (start);
		NFA end = m_pattern.constructNFA (factory, start.next);
		end.next = factory.createNFA (start);
		end.next2 = start.next;
		start.next2 = end.next;
		return end.next;
	}

	public boolean hasSubExpression ()
	{
		return m_pattern.hasSubExpression ();
	}
}
