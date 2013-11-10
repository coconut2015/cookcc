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

import java.util.Comparator;

/**
 * @author Heng Yuan
 * @version $Id$
 */
class Item
{
	private static Comparator<Item> s_closureComparator = new Comparator<Item> ()
	{
		public int compare (Item o1, Item o2)
		{
			int c = o1.m_production.compareTo (o2.m_production);
			if (c != 0)
				return c;
			if (o1.m_position != o2.m_position)
				return o1.m_position - o2.m_position;
			return 0;
		}
	};

	public static Comparator<Item> getClosureComparator ()
	{
		return s_closureComparator;
	}

	private Production m_production;
	private int m_position;
	private final TokenSet m_lookahead;
	private final TokenSet m_first;
	private boolean m_changed;

	Item (Production production, int pos, TokenSet lookahead, TokenSet tokenSet)
	{
		m_production = production;
		m_position = pos;
		m_lookahead = lookahead;
		m_first = tokenSet;
		m_changed = true;
	}

	Item (Item item, int position)
	{
		m_production = item.m_production;
		m_position = position;
		m_lookahead = item.m_lookahead.clone ();
		m_first = item.m_first.clone ();
		m_changed = true;
	}

	public Production getProduction ()
	{
		return m_production;
	}

	public TokenSet getFirst ()
	{
		return m_first;
	}

	public TokenSet getLookahead ()
	{
		return m_lookahead;
	}

	void setProduction (Production production)
	{
		m_production = production;
	}

	int getPosition ()
	{
		return m_position;
	}

	void setPosition (int position)
	{
		m_position = position;
	}

	public boolean updateLookahead (TokenSet src)
	{
		boolean changed = m_lookahead.or (src);
		if (changed)
			m_changed = true;
		return changed;
	}

	public boolean isChanged ()
	{
		return m_changed;
	}

	public void setChanged (boolean changed)
	{
		m_changed = changed;
	}
}
