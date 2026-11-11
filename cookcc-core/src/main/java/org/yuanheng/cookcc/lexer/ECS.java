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

import java.util.HashMap;

/**
 * Equivalent character classes.
 *
 * @author Heng Yuan
 */
public class ECS
{
	private int m_groupCount;
	private final int[] m_groups;
	private int[] m_lookup;

	public ECS (int maxSymbol)
	{
		m_groups = new int[maxSymbol + 1];
		m_groupCount = 1;
	}

	public int getGroupCount ()
	{
		return m_groupCount;
	}

	public int[] getGroups ()
	{
		return m_groups;
	}

	public int getGroup (int group)
	{
		return m_groups[group];
	}

	public int[] getLookup ()
	{
		if (m_lookup == null)
		{
			// now compute the lookup
			m_lookup = new int[m_groupCount];
			for (int i = 0; i < m_groupCount; ++i)
			{
				for (int j = 0; j < m_groups.length; ++j)
				{
					if (m_groups[j] == i)
					{
						m_lookup[i] = j;
						break;
					}
				}
			}
		}
		return m_lookup;
	}

	/**
	 * @param	ch
	 * 			single character
	 * @param	nocase
	 * 			if nocase, ch passed in must be in lower case
	 */
	public void add (int ch, boolean nocase)
	{
		if (nocase)
		{
			int newGroup = ++m_groupCount;
			m_groups[ch] += newGroup;
			int upperCase = Character.toUpperCase (ch);
			if (upperCase != ch)
				m_groups[upperCase] += newGroup;
		}
		else
		{
			m_groups[ch] = ++m_groupCount;
		}
		compute ();
	}

	public void add (boolean[] ccl, boolean nocase)
	{
		if (nocase)
		{
			for (int i = 'a'; i <= 'z'; ++i)
			{
				int upper = 'A' + (i - 'a');
				if (ccl[i])
					ccl[upper] = true;
				else if (ccl[upper])
					ccl[i] = true;
			}
		}
		int newGroup = ++m_groupCount;
		for (int i = 0; i < ccl.length; ++i)
			if (ccl[i])
				m_groups[i] += newGroup;        // guarranteed to be larger than existing maximum number of groups
		compute ();
	}

	/**
	 * All non-zero values are treated as a group.  This function
	 * is for computing DFA compression error group.
	 *
	 * @param    error an error vector
	 */
	public void add (short[] error)                // for computing ecs for error array
	{
		int newGroup = ++m_groupCount;
		for (int i = 0; i < error.length; i++)
		{
			if (error[i] != 0)
				m_groups[i] += newGroup;
		}
		compute ();
	}

	/**
	 * Not a particularly efficient method to compute equivalent classes.
	 * It is quite memory consuming at present.
	 */
	private void compute ()
	{
		HashMap<Integer, Integer> numberMap = new HashMap<Integer, Integer> ();

		m_groupCount = 0;
		for (int c = 0; c < m_groups.length; c++)
		{
			Integer key = new Integer (m_groups[c]);
			Integer value;
			if ((value = numberMap.get (key)) == null)
			{
				numberMap.put (key, new Integer (m_groupCount));
				m_groups[c] = m_groupCount++;
			}
			else
				m_groups[c] = value.intValue ();
		}
		m_lookup = null;
	}

	@Override
	public String toString ()
	{
		return "equivalent classes: " + getGroupCount ();
	}
}
