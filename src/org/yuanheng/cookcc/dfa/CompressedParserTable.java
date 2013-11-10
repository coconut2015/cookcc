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

import org.yuanheng.cookcc.parser.Parser;

/**
 * Perform table compression
 *
 * @author Heng Yuan
 * @version $Id$
 */
public class CompressedParserTable
{
	private final Parser m_parser;
	private boolean m_computed;

	private short[] m_base;
	private short[] m_next;
	private short[] m_check;

	private short[] m_default;
	private boolean m_error;
	private short[] m_meta;
	private short[] m_gotoDefault;

	private int m_baseAdd;

	public CompressedParserTable (Parser lexer)
	{
		m_parser = lexer;
	}

	private void compute ()
	{
		if (m_computed)
			return;
		m_computed = true;
		TableCompressor compressor = new TableCompressor (m_parser.getDFA ());

		compressor.compute ();

		m_base = compressor.getBase ();
		m_next = compressor.getNext ();
		m_check = compressor.getCheck ();

		m_default = compressor.getDefault ();
		m_error = compressor.getError ();
		m_meta = compressor.getMeta ();

		GotoTableCompressor gotoCompressor = new GotoTableCompressor (m_parser.getGoto ());
		gotoCompressor.compute (m_base, m_next, m_check);
		m_base = gotoCompressor.getBase ();
		m_next = gotoCompressor.getNext ();
		m_check = gotoCompressor.getCheck ();
		m_gotoDefault = gotoCompressor.getDefault ();
		m_baseAdd = gotoCompressor.getBaseAdd ();
	}

	public int getSize ()
	{
		return m_parser.getDFA ().size ();
	}

	public int getTotalSize ()
	{
		compute ();
		int totalSize = 0;
		totalSize += m_base.length;
		totalSize += m_next.length;
		totalSize += m_check.length;
		totalSize += m_default == null ? 0 : m_default.length;
		totalSize += m_meta == null ? 0 : m_meta.length;
		totalSize += m_gotoDefault == null ? 0 : m_gotoDefault.length;
		return totalSize;
	}

	public Vector<short[]> getGoto ()
	{
		return m_parser.getGoto ();
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

	public int getBaseAdd ()
	{
		compute ();
		return m_baseAdd;
	}

	public short[] getGotoDefault ()
	{
		compute ();
		return m_gotoDefault;
	}

	public int getUsedTerminalCount ()
	{
		compute ();
		return m_parser.getUsedTerminalCount ();
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
		short[] gotoDefault = getGotoDefault ();

		DFATable dfa = m_parser.getDFA ();
		Vector<short[]> gotoTable = m_parser.getGoto ();
		int numStates = dfa.size ();
		int usedTerminalCount = m_parser.getUsedTerminalCount ();
		int nonTerminalCount = m_parser.getNonTerminalCount ();
		int baseAdd = getBaseAdd ();

		for (int state = 0; state < numStates; ++state)
		{
			short[] row = dfa.getRow (state).getStates ();
			short[] gotoRow = gotoTable.get (state);
			for (int symbol = 0; symbol < usedTerminalCount; ++symbol)
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
			for (int symbol = 0; symbol < nonTerminalCount; ++symbol)
			{
				int currentState;
				if (gotoDefault == null)
				{
					currentState = state + baseAdd;
					if (check[symbol + base[currentState]] == (currentState))
						currentState = next[symbol + base[currentState]];
					else
						currentState = 0;
				}
				else
				{
					currentState = state + baseAdd;
					while (check[symbol + base[currentState]] != currentState)
						currentState = gotoDefault[currentState - baseAdd];
					currentState = next[symbol + base[currentState]];
				}
				if (gotoRow[symbol] != currentState)
					return false;
			}
		}
		return true;
	}
}
