/*
 * Copyright (c) 2008, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Heng Yuan nor the
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

import java.util.*;

/**
 * @author Heng Yuan
 * @version $Id$
 */
class NFA
{
	public final static int EPSILON = -1;
	public final static int ISCCL = -2;
	public final static int EMPTY = -4;

	public final static int TRAIL_MASK = 0x03;
	public final static int TRAIL_NONE = 0;
	public final static int TRAIL_FIXHEAD = 0x01;
	public final static int TRAIL_FIXTAIL = 0x02;
	public final static int TRAIL_VAR = 0x03;

	/* for sorting NFA in printing */
	private final static Comparator<NFA> s_comparator = new Comparator<NFA> ()
	{
		public int compare (NFA o1, NFA o2)
		{
			return o1.id - o2.id;
		}
	};

	private NFAFactory m_factory;

	int thisChar;
	boolean[] charSet;
	int anchor;
	int caseValue;
	NFA next;
	NFA next2;
	boolean mark;
	int lineNumber = Integer.MAX_VALUE;

	final int id;

	NFA (NFAFactory factory)
	{
		id = factory.incNFACounter ();
		m_factory = factory;
		init ();
	}

	void init ()
	{
		thisChar = EPSILON;
		charSet = null;
		anchor = 0;
		caseValue = 0;
		next = null;
		next2 = null;
		mark = false;
		lineNumber = Integer.MAX_VALUE;
	}

	public void setState (int caseValue, int lineNumber, int trail)
	{
		anchor = trail;
		NFA end = last ();
		end.caseValue = caseValue;
		end.lineNumber = lineNumber;
		end.anchor = trail;
		/*
		if (trail != 0)
		{
			if ((trail & 7) != 6) 		// have const head or tail
			{							// then remove extra memory positions
				NFA t = this;
				while ((t.anchor & 0x03) == 0)
					t = t.next;
				t.anchor = 0;
			}
		}
		*/
	}

	public boolean isAccept ()
	{
		return next == null;
	}

	private void copy (NFA other)
	{
		thisChar = other.thisChar;
		charSet = other.charSet;
		anchor = other.anchor;
		caseValue = other.caseValue;
		next = other.next;
		next2 = other.next2;
		mark = other.mark;
		lineNumber = other.lineNumber;
	}

	public NFA last ()
	{
		NFA n = this;
		while (n.next != null)
			n = n.next;
		return n;
	}

	public NFA cat (NFA other)
	{
		NFA n = last ();
		n.copy (other);
		m_factory.deleteNFA (other);
		return this;
	}

	public NFA or (NFA other)
	{
		NFA n = m_factory.createNFA ();
		n.next = this;
		n.next2 = other;
		NFA e = m_factory.createNFA ();
		last ().next = e;
		other.last ().next = e;
		return n;
	}

	public NFA star ()
	{
		NFA n = m_factory.createNFA ();
		NFA e = m_factory.createNFA ();
		NFA oldend = last ();
		n.next = this;
		n.next2 = e;
		oldend.next = e;
		oldend.next2 = this;
		return n;
	}

	public NFA plus ()
	{
		NFA n = m_factory.createNFA ();
		NFA oldend = last ();
		oldend.next = m_factory.createNFA ();
		oldend.next2 = this;
		n.next = this;
		return n;
	}

	public NFA q ()
	{
		NFA n = m_factory.createNFA ();
		NFA oldend = last ();
		NFA e = m_factory.createNFA ();
		oldend.next = e;
		n.next = this;
		n.next2 = e;
		return n;
	}

	private void recursiveUpdateMap (NFA nfa, IdentityHashMap<NFA, NFA> nfaMap)
	{
		if (nfaMap.containsKey (nfa))
			return;
		nfaMap.put (nfa, null);
		if (nfa.next != null)
			recursiveUpdateMap (nfa.next, nfaMap);
		if (nfa.next2 != null)
			recursiveUpdateMap (nfa.next2, nfaMap);
	}

	/**
	 * Create a complete deep copy of this NFA.
	 *
	 * @return	a deep copy of this NFA
	 */
	private NFA duplicate ()
	{
		IdentityHashMap<NFA, NFA> nfaMap = new IdentityHashMap<NFA, NFA> ();
		// serialize all the NFA into nfaMap
		recursiveUpdateMap (this, nfaMap);
		// create a corresponding set of NFA
		NFA[] keys = nfaMap.keySet ().toArray (new NFA[nfaMap.size ()]);
		for (NFA key : keys)
		{
			NFA copy = m_factory.createNFA ();
			copy.copy (key);
			nfaMap.put (key, copy);
		}
		// update branches
		for (NFA key : keys)
		{
			NFA v = nfaMap.get (key);
			if (key.next != null)
				v.next = nfaMap.get (key.next);
			if (key.next2 != null)
				v.next2 = nfaMap.get (key.next2);
		}
		return nfaMap.get (this);
	}

	public NFA repeat (int count)
	{
		if (count == 0)
			return q ();
		if (count == 1)
			return this;
		NFA copy = duplicate ();
		NFA n = this;
		for (; count > 2; --count)
			n = copy.duplicate ().cat (n);
		return copy.cat (n);
	}

	public NFA repeat (int min, int max)
	{
		if (max == -1)
		{
			if (min == 0)					// n{0,} == n*
				return star ();
			if (min == 1)					// n{1,} = n+
				return plus ();
			NFA copy = duplicate ();        // n{3,} = nnn+
			NFA n = repeat (min - 1);
			copy = copy.plus ();
			return n.cat (copy);
		}
		if (min == max)
			return repeat (min);
		if (min == 0)
			return q ().repeat (max);

		NFA copy = duplicate ().q ();
		NFA n = repeat (min);
		return n.cat (copy.repeat (max - min));
	}

	/**
	 * Debugging function.  Print only this NFA to the buffer.
	 * @param	buffer
	 * 			the output string buffer.
	 */
	void toString (StringBuffer buffer)
	{
		buffer.append ('[').append (id).append ("]: ");

		String cclStr = charSet == null ? null : m_factory.getCCL ().toString (charSet);

		if (thisChar == m_factory.getCCL ().EOF)
			buffer.append ("EOF ");
		else if (thisChar >= 0)
		{
			String str = CCL.toString ((char)thisChar);
			if (str.length () == 1)
				buffer.append ('\'').append (str).append ("' ");
			else
				buffer.append (str).append ("  ");
		}
		else if (thisChar == ISCCL)
		{
			if (cclStr.length () <= 3)
				buffer.append (cclStr).append ("    ".substring (cclStr.length ()));
			else
				buffer.append ("CCL ");
		}
		else
			buffer.append ("--- ");

		if (next != null)
			buffer.append ("next = ").append (next.id);
		else
			buffer.append ("accept state ").append (isAccept ());

		if (next2 != null)
			buffer.append ("\tnext2 = ").append (next2.id);
		else
			buffer.append ("\t\t");

		if (cclStr != null && cclStr.length () > 3)
			buffer.append ("\tCCL = ").append (m_factory.getCCL ().toString (charSet));
		buffer.append ('\n');
	}

	@Override
	public String toString ()
	{
		StringBuffer buffer = new StringBuffer ();

		IdentityHashMap<NFA, NFA> nfaMap = new IdentityHashMap<NFA, NFA> ();
		// serialize all the NFA into nfaMap
		recursiveUpdateMap (this, nfaMap);

		toString (buffer);
		nfaMap.remove (this);
		Collection<NFA> set = getSortedSet ();
		set.addAll (nfaMap.keySet ());

		for (NFA n: set)
			n.toString (buffer);

		return buffer.toString ();
	}

	public static boolean hasTrail (int flag)
	{
		return (flag & TRAIL_MASK) != 0;
	}

	public static int setTrailContext (int distance, boolean fixhead, boolean fixtail)
	{
		distance <<= 2;
		if (fixhead)
			return distance | TRAIL_FIXHEAD;
		else if (fixtail)
			return distance | TRAIL_FIXTAIL;
		else
			return distance | TRAIL_VAR;
	}

	public static Set<NFA> getSortedSet ()
	{
		return new TreeSet<NFA> (s_comparator);
	}
}
