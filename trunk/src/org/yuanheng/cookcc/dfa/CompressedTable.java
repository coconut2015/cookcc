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

import org.yuanheng.cookcc.lexer.Lexer;

/**
 * Perform table compression
 *
 * @author Heng Yuan
 * @version $Id$
 */
public class CompressedTable
{
	private final Lexer m_lexer;
	private boolean m_computed;

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
		return m_lexer.getECS ().getGroups ().clone ();
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
