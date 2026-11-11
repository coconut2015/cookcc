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
package org.yuanheng.cookcc.parser;

/**
 * @author Heng Yuan
 */
class TokenSet implements Comparable<TokenSet>
{
	private final int[] m_terminalGroups;
	private final int[] m_terminalLookup;
	private final boolean[] m_terminals;
	private boolean m_epsilon;

	TokenSet (int size, int[] terminalGroups, int[] terminalLookup)
	{
		m_terminals = new boolean[size];
		m_terminalGroups = terminalGroups;
		m_terminalLookup = terminalLookup;
	}

	private TokenSet (TokenSet o)
	{
		m_terminals = o.m_terminals.clone ();
		m_terminalGroups = o.m_terminalGroups;
		m_terminalLookup = o.m_terminalLookup;
		m_epsilon = o.m_epsilon;
	}

	public void addSymbol (int symbol)
	{
		m_terminals[m_terminalGroups[symbol]] = true;
	}

	public boolean hasSymbol (int symbol)
	{
		return m_terminals[m_terminalGroups[symbol]];
	}

	public boolean hasEpsilon ()
	{
		return m_epsilon;
	}

	public void setEpsilon (boolean epsilon)
	{
		m_epsilon = epsilon;
	}

	/**
	 * Perform or equals operation and check if anything changed.
	 *
	 * @param    tokenSet the input token set
	 * @return true if any items has been changed
	 */
	boolean or (TokenSet tokenSet)
	{
		boolean changed = false;
		for (int i = 0; i < m_terminals.length; ++i)
		{
			boolean b = m_terminals[i];
			m_terminals[i] |= tokenSet.m_terminals[i];
			changed |= b != m_terminals[i];
		}
		return changed;
	}

	public TokenSet clone ()
	{
		return new TokenSet (this);
	}

	public int compareTo (TokenSet o)
	{
		if (m_epsilon != o.m_epsilon)
			return m_epsilon ? 1 : -1;
		for (int i = 0; i < m_terminals.length; ++i)
			if (m_terminals[i] != o.m_terminals[i])
				return m_terminals[i] ? 1 : -1;
		return 0;
	}
}
