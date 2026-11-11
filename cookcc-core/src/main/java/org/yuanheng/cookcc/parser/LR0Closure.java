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

/**
 * @author Heng Yuan
 */
class LR0Closure implements Closure
{
	private final Parser m_parser;

	public LR0Closure (Parser parser)
	{
		m_parser = parser;
	}

	/**
	 * Does epsilon closure for LR(0) items.
	 * Only consider spontaneously generated tokens
	 *
	 * @param    itemSet the ItemSet to perform closure operation on
	 */
	public void closure (ItemSet itemSet)
	{
		for (int i = 0; i < itemSet.size (); ++i)
		{
			Item item = itemSet.getItem (i);
			int[] production = item.getProduction ().getProduction ();
			int pos = item.getPosition ();

			if (pos >= production.length ||
				production[pos] <= m_parser.m_maxTerminal)
				continue;

			//
			// pre-compute the FIRST for the non-terminal to optimize things a little
			//
			TokenSet first = m_parser.createTokenSet ();
			m_parser.computeFirst (production, pos + 1, production.length, first);
			first.setEpsilon (false);    // important, since the above computeFirst () does not
			// have the lookahead tokens appeneded.

			// okay a non-terminal is found,
			// insert that terminal's production to this item set

			int nonTerminal = production[pos];

			Production[] table = m_parser.getProductionMap ().get (nonTerminal);
			for (Production k : table)
				itemSet.insertClosureItem (m_parser.createItem (k, 0, first));
		}
	}
}
