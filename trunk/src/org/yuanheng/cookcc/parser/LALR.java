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
package org.yuanheng.cookcc.parser;

import java.util.Vector;

import org.yuanheng.cookcc.dfa.DFATable;

/**
 * @author Heng Yuan
 * @version $Id$
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
		Vector<short[]> gotoTable = m_parser.getGoto ();
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

		Vector<ItemSet> dfaStates = m_parser._DFAStates;

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
