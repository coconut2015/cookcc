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
package org.yuanheng.cookcc.parser;

import java.util.ArrayList;

import org.yuanheng.cookcc.dfa.DFATable;

/**
 * @author Heng Yuan
 */
class LALR
{
	private final Parser m_parser;

	public LALR (Parser parser)
	{
		m_parser = parser;
	}

	void propagateMove (ItemSet itemSet, int state)
	{
		DFATable dfa = m_parser.getDFA ();
		ArrayList<short[]> gotoTable = m_parser.getGoto ();
		short[] dfaColumn = dfa.getRow (state).getStates ();
		short[] gotoColumn = gotoTable.get (state);

		int[] usedSymbols = m_parser.getUsedSymbols ();

		int usedSymbolCount = m_parser.getUsedSymbolCount ();
		int usedTerminalCount = m_parser.getUsedTerminalCount ();

		for (int i = 0; i < usedSymbolCount; ++i)
		{
			short destState;

			if (i < usedTerminalCount)
				destState = dfaColumn[i];
			else
				destState = gotoColumn[i - usedTerminalCount];

			if (destState <= 0)
				continue;

			// there is a move for _tokens[i], propagate lookahead
			// to that state

			ItemSet destSet = m_parser._DFAStates.get (destState);

			for (Item item : itemSet.getItems ())
			{
				int[] production = item.getProduction ().getProduction ();
				int pos = item.getPosition ();

				if (pos < production.length && production[pos] == usedSymbols[i])
					destSet.updateItem (item.getProduction (), pos + 1, item.getLookahead ());
			}
		}
	}

	//
	// propagate lookaheads
	//
	void propagate ()
	{
		boolean changed = true;

		ArrayList<ItemSet> dfaStates = m_parser._DFAStates;

		while (changed)
		{
			changed = false;

			// go through all item sets
			for (int i = 0; i < dfaStates.size (); ++i)
			{
				ItemSet itemSet = dfaStates.get (i);

				if (!itemSet.isChanged ())
					continue;

				changed = true;
				itemSet.setChanged (false);

				// first update closure
				m_parser.propagateClosure (itemSet);
				// then update
				propagateMove (itemSet, i);
			}
		}
	}

	void build ()
	{
		// disable lookahead = comparison
//		_compareLA = false;

		// first build DFA states of LR(0) items

		m_parser.buildStates (new LR0Closure (m_parser), Item.getClosureComparator ());

		// then propagate lookaheads to the states

		propagate ();

/*
		System.out.println ("== after propagation");
		int size = m_parser._DFAStates.size ();
		for (int i = 0; i < size; ++i)
		{
			System.out.println ("-- state " + i);
			System.out.println (m_parser.toString (m_parser._DFAStates.get (i)));
		}
*/
	}
}
