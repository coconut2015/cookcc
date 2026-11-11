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
package org.yuanheng.cookcc.lexer;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Heng Yuan
 */
class ESet implements Comparable<ESet>
{
	private final Set<NFA> m_set = NFA.getSortedSet ();
	private int m_stateId;
	private boolean m_eol;

	public ESet ()
	{
	}

	public void setStateId (int stateId)
	{
		m_stateId = stateId;
	}

	public int getStateId ()
	{
		return m_stateId;
	}

	public NFA isAccept ()
	{
		NFA acceptState = null;
		for (NFA n : m_set)
			if (n.isAccept ())
			{
				if (acceptState == null || n.precedence < acceptState.precedence)
					acceptState = n;
			}
		return acceptState;
	}

	public Set<NFA> getSet ()
	{
		return m_set;
	}

	public void add (NFA nfa)
	{
		m_set.add (nfa);
	}

	public int compareTo (ESet o)
	{
		if (m_set.size () != o.m_set.size ())
			return m_set.size () - o.m_set.size ();

		Iterator<NFA> i1 = m_set.iterator ();
		Iterator<NFA> i2 = o.m_set.iterator ();
		for (; i1.hasNext (); )
		{
			NFA n1 = i1.next ();
			NFA n2 = i2.next ();
			if (n1 != n2)
				return n1.id - n2.id;
		}
		return 0;
	}

	/**
	 * Debugging function.
	 *
	 * @return debug info for this set.
	 */
	@Override
	public String toString ()
	{
		if (m_set.size () == 0)
			return "empty set";
		StringBuffer buffer = new StringBuffer ();
		for (NFA nfa : m_set)
		{
			nfa.toString (buffer);
		}
		return buffer.toString ();
	}

	/**
	 * Is this state reached by '\n' character?
	 * 
	 * @return	Indicates whether or not this state is reached via '\n' character.
	 */
	public boolean isEol ()
	{
		return m_eol;
	}

	/**
	 * Sets whether or not this state is reached via '\n' character.
	 * 
	 * @parm	eol
	 * 			whether or not this state is reached via '\n' character.
	 */
	public void setEol (boolean eol)
	{
		m_eol = eol;
	}
}
