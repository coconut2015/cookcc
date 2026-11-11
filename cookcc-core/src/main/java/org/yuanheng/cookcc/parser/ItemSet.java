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

import java.util.*;

/**
 * @author Heng Yuan
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
