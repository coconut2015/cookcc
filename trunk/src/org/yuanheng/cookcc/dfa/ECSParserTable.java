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
public class ECSParserTable
{
	private final Parser m_parser;

	public ECSParserTable (Parser lexer)
	{
		m_parser = lexer;
	}

	public int getSize ()
	{
		return m_parser.getDFA ().size ();
	}

	public int getTotalSize ()
	{
		int rows = m_parser.getDFA ().size ();
		return rows * (m_parser.getUsedTerminalCount () + m_parser.getNonTerminalCount ());
	}

	public int[][] getTable ()
	{
		DFATable dfa = m_parser.getDFA ();
		Vector<short[]> gotoTable = m_parser.getGoto ();
		int rows = dfa.size ();
		int usedTerminalCount = m_parser.getUsedTerminalCount ();
		int nonTerminalCount = m_parser.getNonTerminalCount ();
		int cols = usedTerminalCount + nonTerminalCount;
		int[][] table = new int[rows][cols];
		for (int i = 0; i < rows; ++i)
		{
			short[] states = dfa.getRow (i).getStates ();
			short[] gotos = gotoTable.get (i);
			int[] array = table[i];
			for (int j = 0; j < usedTerminalCount; ++j)
				array[j] = states[j];
			for (int j = 0; j < nonTerminalCount; ++j)
				array[j + usedTerminalCount] = gotos[j];
		}
		return table;
	}
}
