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

/**
 * @author Heng Yuan
 * @version $Id$
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
