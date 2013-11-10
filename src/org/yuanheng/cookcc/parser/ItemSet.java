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

import java.util.*;

/**
 * @author Heng Yuan
 * @version $Id$
 */
class ItemSet implements Comparable<ItemSet>
{
	private final LinkedList<Item> m_itemList;
	private final TreeSet<Item> m_kernelSet;
	private final TreeMap<Item, Item> m_closureSet;
	private boolean m_changed = true;

	ItemSet (Comparator<Item> kernelSorter)
	{
		m_itemList = new LinkedList<Item> ();
		m_kernelSet = new TreeSet<Item> (kernelSorter);
		m_closureSet = new TreeMap<Item, Item> (Item.getClosureComparator ());
	}

	@SuppressWarnings ("unchecked")
	private ItemSet (ItemSet other)
	{
		m_itemList = (LinkedList<Item>)other.m_itemList.clone ();
		m_kernelSet = (TreeSet<Item>)other.m_kernelSet.clone ();
		m_closureSet = (TreeMap<Item, Item>)other.m_closureSet.clone ();
	}

	public Item getItem (int index)
	{
		return m_itemList.get (index);
	}

	public Item[] getItems ()
	{
		return m_itemList.toArray (new Item[m_itemList.size ()]);
	}

	public Item[] getKernelItems ()
	{
		return m_kernelSet.toArray (new Item[m_kernelSet.size ()]);
	}

	public int size ()
	{
		return m_itemList.size ();
	}

	public boolean isKernelItem (Item item)
	{
		return m_kernelSet.contains (item);
	}

	public Item find (Item item)
	{
		return m_closureSet.get (item);
	}

	public ItemSet clone ()
	{
		return new ItemSet (this);
	}

	public int compareTo (ItemSet other)
	{
		if (m_kernelSet.size () != other.m_kernelSet.size ())
			return m_kernelSet.size () - other.m_kernelSet.size ();

		Iterator<Item> i1;
		Iterator<Item> i2;
		for (i1 = m_kernelSet.iterator (),
				 i2 = other.m_kernelSet.iterator ();
			 i1.hasNext (); )
		{
			Item item1 = i1.next ();
			Item item2 = i2.next ();
			int c = item1.getProduction ().compareTo (item2.getProduction ());
			if (c != 0)
				return c;
			if (item1.getPosition () != item2.getPosition ())
				return item1.getPosition () - item2.getPosition ();
		}
		return 0;
	}

	public void insertKernelItem (Item item)
	{
		if (!m_kernelSet.contains (item))
		{
			m_itemList.add (item);
			m_kernelSet.add (item);
			m_closureSet.put (item, item);
		}
	}

	public void insertClosureItem (Item item)
	{
		Item internalItem = m_closureSet.get (item);

		if (internalItem == null)
		{
			m_closureSet.put (item, item);
			m_itemList.add (item);
		}
		else
			internalItem.updateLookahead (item.getLookahead ());
	}

	void updateItem (Production production, int position, TokenSet lookahead)
	{
		Item dummyItem = new Item (production, position, lookahead, null);
		Item item = m_closureSet.get (dummyItem);

		if (item.updateLookahead (lookahead))
			m_changed = true;
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
