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

import org.yuanheng.cookcc.parser.Parser;

/**
 * Perform table compression
 *
 * @author Heng Yuan
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
		ArrayList<short[]> gotoTable = m_parser.getGoto ();
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
