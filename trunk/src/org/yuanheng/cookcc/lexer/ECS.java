/*
 * Copyright (c) 2008, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Heng Yuan nor the
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
package org.yuanheng.cookcc.lexer;

import java.util.HashMap;

/**
 * Equivalent character classes.
 *
 * @author Heng Yuan
 * @version $Id$
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

	public void add (int ch)
	{
		m_groups[ch] = ++m_groupCount;
		compute ();
	}

	public void add (boolean[] ccl)
	{
		int newGroup = ++m_groupCount;
		for (int i = 0; i < ccl.length; ++i)
			if (ccl[i])
				m_groups[i] += newGroup;		// guarranteed to be larger than existing maximum number of groups
		compute ();
	}

	/**
	 * All non-zero values are treated as a group.  This function
	 * is for computing DFA compression error group.
	 *
	 * @param	error
	 * 			an error vector
	 */
	public void add (short[] error)				// for computing ecs for error array
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
		HashMap<Integer,Integer> numberMap = new HashMap<Integer,Integer> ();

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
