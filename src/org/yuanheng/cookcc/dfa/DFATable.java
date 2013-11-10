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
package org.yuanheng.cookcc.dfa;

import java.util.Vector;

/**
 * A simple wrapper for Vector&lt;DFARow> that provides some utility
 * functions.
 *
 * @author Heng Yuan
 * @version $Id$
 */
public class DFATable
{
	private final Vector<DFARow> m_table;

	public DFATable ()
	{
		m_table = new Vector<DFARow> (512, 512);
	}

	// create a deep copy of the other table
	private DFATable (DFATable other)
	{
		m_table = new Vector<DFARow> (other.m_table.size ());
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

	public DFATable clone ()
	{
		return new DFATable (this);
	}
}
