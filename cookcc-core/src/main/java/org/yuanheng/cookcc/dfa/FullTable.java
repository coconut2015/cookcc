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
 * Utility class that computes the full table.
 *
 * @author Heng Yuan
 */
public class FullTable
{
	private final Lexer m_lexer;

	private int[][] m_table;

	public FullTable (Lexer lexer)
	{
		m_lexer = lexer;
	}

	public int[][] getTable ()
	{
		if (m_table == null)
		{
			DFATable dfa = m_lexer.getDFA ();
			int rows = dfa.size ();
			int cols = m_lexer.getCCL ().MAX_SYMBOL + 1;
			int[][] table = new int[rows][cols];
			int[] groups = m_lexer.getECS ().getGroups ();
			for (int i = 0; i < rows; ++i)
			{
				short[] states = dfa.getRow (i).getStates ();
				int[] array = table[i];
				for (int j = 0; j < cols; ++j)
					array[j] = states[groups[j]];
			}
			m_table = table;
		}
		return m_table;
	}

	public int getSize ()
	{
		return m_lexer.getDFA ().size ();
	}
}
