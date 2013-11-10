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
package org.yuanheng.cookcc.lexer;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Heng Yuan
 * @version $Id$
 */
class ESet implements Comparable<ESet>
{
	private final Set<NFA> m_set = NFA.getSortedSet ();
	private int m_stateId;

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
			buffer.append ("  ");
		}
		return buffer.toString ();
	}
}
