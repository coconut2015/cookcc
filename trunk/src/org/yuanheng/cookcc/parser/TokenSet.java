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
package org.yuanheng.cookcc.parser;

/**
 * @author Heng Yuan
 * @version $Id$
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
