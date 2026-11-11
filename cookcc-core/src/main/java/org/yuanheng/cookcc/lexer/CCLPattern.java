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
public class CCLPattern implements Pattern
{
	private final boolean[] m_charSet;

	public CCLPattern (boolean[] ccl)
	{
		m_charSet = ccl;
	}

	public int getLength ()
	{
		return 1;
	}

	public boolean[] getCharSet ()
	{
		return m_charSet;
	}

	@Override
	public String toString ()
	{
		if (m_charSet.length <= 257)
		{
			return CCL.getByteCCL ().toString (m_charSet);
		}
		return CCL.getCharacterCCL ().toString (m_charSet);
	}

	public NFA constructNFA (NFAFactory factory, NFA start)
	{
		factory.setNFA (start, NFA.ISCCL, m_charSet);
		NFA end = factory.createNFA (start);
		start.next = end;
		return end;
	}

	public boolean hasSubExpression ()
	{
		return false;
	}
}
