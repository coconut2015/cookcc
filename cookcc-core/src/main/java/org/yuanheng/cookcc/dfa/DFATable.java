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
package org.yuanheng.cookcc.dfa;

import java.util.ArrayList;

/**
 * A simple wrapper for {@code ArrayList<DFARow>} that provides some utility
 * functions.
 *
 * @author Heng Yuan
 */
public class DFATable
{
	private final ArrayList<DFARow> m_table;

	public DFATable ()
	{
		m_table = new ArrayList<DFARow> (512);
	}

	// create a deep copy of the other table
	private DFATable (DFATable other)
	{
		m_table = new ArrayList<DFARow> (other.m_table.size ());
		for (DFARow row : other.m_table)
			m_table.add (row.clone ());
	}

	public void add (DFARow row)
	{
		m_table.add (row);
	}

	public int[] getAccepts ()
	{
		int[] accepts = new int[m_table.size ()];
		for (int i = 0; i < accepts.length; ++i)
			accepts[i] = m_table.get (i).getCaseValue ();
		return accepts;
	}

	public int size ()
	{
		return m_table.size ();
	}

	public DFARow getRow (int i)
	{
		return m_table.get (i);
	}

	public DFARow[] getRows ()
	{
		return m_table.toArray (new DFARow[m_table.size ()]);
	}

	@Override
	public DFATable clone ()
	{
		return new DFATable (this);
	}
}
