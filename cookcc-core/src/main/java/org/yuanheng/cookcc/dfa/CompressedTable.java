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

import org.yuanheng.cookcc.lexer.Lexer;

/**
 * Perform table compression
 *
 * @author Heng Yuan
 */
public class CompressedTable
{
	private final Lexer m_lexer;
	private boolean m_computed;

	private int[] m_ecs;
	private short[] m_base;
	private short[] m_next;
	private short[] m_check;

	private short[] m_default;
	private boolean m_error;
	private short[] m_meta;

	public CompressedTable (Lexer lexer)
	{
		m_lexer = lexer;
	}

	private void compute ()
	{
		if (m_computed)
			return;
		m_computed = true;
		TableCompressor compressor = new TableCompressor (m_lexer.getDFA ());

		compressor.compute ();

		m_base = compressor.getBase ();
		m_next = compressor.getNext ();
		m_check = compressor.getCheck ();

		m_default = compressor.getDefault ();
		m_error = compressor.getError ();
		m_meta = compressor.getMeta ();
	}

	public int getSize ()
	{
		return m_lexer.getDFA ().size ();
	}

	public int[] getEcs ()
	{
		if (m_ecs == null)
			m_ecs = m_lexer.getECS ().getGroups ().clone ();
		return m_ecs;
	}

	public short[] getBase ()
	{
		compute ();
		return m_base;
	}

	public short[] getNext ()
	{
		compute ();
		return m_next;
	}

	public short[] getCheck ()
	{
		compute ();
		return m_check;
	}

	public short[] getDefault ()
	{
		compute ();
		return m_default;
	}

	public boolean getError ()
	{
		compute ();
		return m_error;
	}

	public short[] getMeta ()
	{
		compute ();
		return m_meta;
	}


	public boolean getCorrect ()
	{
		// verify compressed tables are ok
		short[] next = getNext ();
		short[] check = getCheck ();
		short[] base = getBase ();
		short[] defaults = getDefault ();
		short[] meta = getMeta ();
		boolean error = getError ();

		DFATable dfa = m_lexer.getDFA ();
		int numStates = dfa.size ();
		int numGroups = m_lexer.getECS ().getGroupCount ();

		for (int state = 0; state < numStates; ++state)
		{
			short[] row = dfa.getRow (state).getStates ();
			for (int symbol = 0; symbol < numGroups; ++symbol)
			{
//				System.out.println ("state: " + state + ", sym: " + symbol);
				int currentState;
				if (defaults == null)
				{
					if (check[symbol + base[state]] == state)
						currentState = next[symbol + base[state]];
					else
						currentState = 0;
				}
				else if (!error)
				{
					if (check[symbol + base[state]] == state)
						currentState = next[symbol + base[state]];
					else
						currentState = defaults[state];
				}
				else if (meta == null)
				{
					currentState = state;
					int e = symbol;
					while (check[e + base[currentState]] != currentState)
					{
						currentState = defaults[currentState];
						if (currentState >= numStates)
							e = 0;
					}
					currentState = next[e + base[currentState]];
				}
				else
				{
					currentState = state;
					int e = symbol;
					while (check[e + base[currentState]] != currentState)
					{
						currentState = defaults[currentState];
						if (currentState >= numStates)
							e = meta[e];
					}
					currentState = next[e + base[currentState]];
				}
				if (row[symbol] != currentState)
//					throw new RuntimeException ("Compressed table and ecs table do not match");
					return false;
			}
		}
		return true;
	}
}
