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

import java.util.Comparator;

/**
 * @author Heng Yuan
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
