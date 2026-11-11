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
package org.yuanheng.cookcc.doc;

import java.util.LinkedList;

/**
 * @author Heng Yuan
 */
public class GrammarDoc extends TreeDoc
{
	private final String m_rule;

	private final LinkedList<RhsDoc> m_rhs = new LinkedList<RhsDoc> ();

	private char m_type = 'n';
	private int m_symbol;

	public GrammarDoc (String rule)
	{
		if (rule == null || rule.length () == 0)
			throw new IllegalArgumentException ("rule must not be empty.");
		m_rule = rule;
	}

	/**
	 * Internally called for internally generated grammar rules.
	 *
	 * @param	type
	 * 			One of 'n', '?', '+', '*', '|', 'g'.
	 */
	public void internalSetType (char type)
	{
		m_type = type;
	}

	/**
	 * Internally called for setting the symbol for the rule.
	 *
	 * @param	symbol
	 * 			integer value of the symbol
	 */
	public void internalSetSymbol (int symbol)
	{
		m_symbol = symbol;
	}

	/**
	 * Gets the integer symbol value for the rule.
	 *
	 * @return	the integer symbol value for the rule.
	 */
	public int getSymbol ()
	{
		return m_symbol;
	}

	/**
	 * The type of the rule.
	 *
	 * <ul>
	 * 	<li>'n' - normal rules</li>
	 * 	<li>'?' - optional</li>
	 * 	<li>'*' - optional list</li>
	 * 	<li>'+' - list</li>
	 * 	<li>'|' - an OR operation
	 * </ul>
	 *
	 * @return One of 'n', '?', '+', '*'.
	 */
	public char getType ()
	{
		return m_type;
	}

	public String getRule ()
	{
		return m_rule;
	}

	public void addRhs (RhsDoc rhs)
	{
		m_rhs.add (rhs);
	}

	public RhsDoc[] getRhs ()
	{
		return m_rhs.toArray (new RhsDoc[m_rhs.size ()]);
	}

	/**
	 * Internal use for parsing input.  Utility function.
	 *
	 * @return the last RhsDoc added to this grammar.
	 */
	public RhsDoc getLastRhs ()
	{
		return m_rhs.getLast ();
	}
}
