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

import java.util.LinkedList;
import java.util.HashMap;
import java.util.Comparator;
import java.util.TreeSet;
import javax.swing.text.html.HTMLDocument;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class NFA
{
	private final static int EPSILON = -1;
	private final static int ISCCL = -2;
	private final static int EMPTY = -4;

	private final static int TRAIL_MASK = 0x06;
	private final static int TRAIL_NONE = 0;
	private final static int TRAIL_FIXHEAD = 0x02;
	private final static int TRAIL_FIXTAIL = 0x04;
	private final static int TRAIL_VAR = 0x06;

	private static int s_count = 0;

	/* for computing equivalent classes */
	private final static ECS s_ecs = new ECS ();
	/* for recycling NFAs */
	private final static LinkedList<NFA> s_spareNFAs = new LinkedList<NFA> ();

	private final static Comparator<NFA> s_comparator = new Comparator<NFA> ()
	{
		public int compare (NFA o1, NFA o2)
		{
			return o1.m_id - o2.m_id;
		}
	};

	public static int getTotalNFACount ()
	{
		return s_count - s_spareNFAs.size ();
	}

	public static NFA createNFA ()
	{
		NFA nfa;
		if (s_spareNFAs.isEmpty ())
			nfa = new NFA ();
		else
			nfa = s_spareNFAs.removeFirst ();
		return nfa;
	}

	public static NFA createNFA (int ch, boolean[] ccl)
	{
		NFA nfa;
		if (s_spareNFAs.isEmpty ())
			nfa = new NFA ();
		else
			nfa = s_spareNFAs.removeFirst ();

		nfa.m_char = ch;
		nfa.m_ccl = ccl;
		if (ch >= 0)
			s_ecs.add ((char)ch);
		else if (ch == ISCCL)
			s_ecs.add (ccl);
		return nfa;
	}

	public static ECS getECS ()
	{
		return s_ecs;
	}

	public static void deleteNFA (NFA nfa)
	{
		nfa.init ();
		s_spareNFAs.add (nfa);
	}

	private int m_char;
	private boolean[] m_ccl;
	private int m_anchor;
	private int m_value;
	private NFA m_next;
	private NFA m_next2;

	private final int m_id;

	private NFA ()
	{
		m_id = s_count++;
		init ();
	}

	private void init ()
	{
		m_char = EPSILON;
		m_ccl = null;
		m_anchor = 0;
		m_value = 0;
		m_next = null;
		m_next2 = null;
	}

	public boolean isAccept ()
	{
		return m_next == null;
	}

	private void copy (NFA other)
	{
		m_char = other.m_char;
		m_ccl = other.m_ccl;
		m_anchor = other.m_anchor;
		m_value = other.m_value;
		m_next = other.m_next;
		m_next2 = other.m_next2;
	}

	public NFA last ()
	{
		NFA n = this;
		while (n.m_next != null)
			n = n.m_next;
		return n;
	}

	public NFA cat (NFA other)
	{
		NFA n = last ();
		n.copy (other);
		deleteNFA (other);
		return this;
	}

	public NFA or (NFA other)
	{
		NFA n = createNFA ();
		n.m_next = this;
		n.m_next2 = other;
		NFA e = createNFA ();
		last ().m_next = e;
		other.last ().m_next = e;
		return n;
	}

	public NFA star ()
	{
		NFA n = createNFA ();
		NFA e = createNFA ();
		NFA oldend = last ();
		n.m_next = this;
		n.m_next2 = e;
		oldend.m_next = e;
		oldend.m_next2 = this;
		return n;
	}

	public NFA plus ()
	{
		NFA n = createNFA ();
		NFA oldend = last ();
		oldend.m_next = createNFA ();
		oldend.m_next2 = this;
		n.m_next = this;
		return n;
	}

	public NFA q ()
	{
		NFA n = createNFA ();
		NFA oldend = last ();
		NFA e = createNFA ();
		oldend.m_next = e;
		n.m_next = this;
		n.m_next2 = e;
		return n;
	}

	private void recursiveUpdateMap (NFA nfa, HashMap<NFA, NFA> nfaMap)
	{
		nfaMap.put (nfa, null);
		if (nfa.m_next != null && nfaMap.get (nfa.m_next) == null)
			recursiveUpdateMap (nfa.m_next, nfaMap);
		if (nfa.m_next2 != null && nfaMap.get (nfa.m_next2) == null)
			recursiveUpdateMap (nfa.m_next2, nfaMap);
	}

	/**
	 * Create a complete deep copy of this NFA.
	 *
	 * @return	a deep copy of this NFA
	 */
	private NFA duplicate ()
	{
		HashMap<NFA, NFA> nfaMap = new HashMap<NFA, NFA> ();
		// serialize all the NFA into nfaMap
		recursiveUpdateMap (this, nfaMap);
		// create a corresponding set of NFA
		NFA[] keys = nfaMap.keySet ().toArray (new NFA[nfaMap.size ()]);
		for (int i = 0; i < keys.length; ++i)
			nfaMap.put (keys[i], createNFA ());
		// update branches
		for (int i = 0; i < keys.length; ++i)
		{
			NFA v = nfaMap.get (keys[i]);
			if (keys[i].m_next != null)
				v.m_next = nfaMap.get (keys[i].m_next);
			if (keys[i].m_next2 != null)
				v.m_next2 = nfaMap.get (keys[i].m_next2);
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
			n = n.cat (copy.duplicate ());
		return n.cat (copy);
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

	private void toString (StringBuffer buffer)
	{
		buffer.append ('[').append (m_id).append ("]: ");

		String cclStr = m_ccl == null ? null : CCL.toString (m_ccl);

		if (m_char >= 0)
			buffer.append ('\'').append ((char)m_char).append ("' ");
		else if (m_char == ISCCL)
		{
			if (cclStr.length () <= 3)
				buffer.append (cclStr);
			else
				buffer.append ("CCL ");
		}
		else
			buffer.append ("--- ");

		if (m_next != null)
			buffer.append ("next = ").append (m_next.m_id);
		else
			buffer.append ("accept state ").append (isAccept ());

		if (m_next2 != null)
			buffer.append ("\tnext2 = ").append (m_next2.m_id);
		else
			buffer.append ("\t\t");

		if (cclStr != null && cclStr.length () > 3)
			buffer.append ("\tCCL = ").append (CCL.toString (m_ccl));
		buffer.append ('\n');
	}

	public String toString ()
	{
		StringBuffer buffer = new StringBuffer ();

		HashMap<NFA, NFA> nfaMap = new HashMap<NFA, NFA> ();
		// serialize all the NFA into nfaMap
		recursiveUpdateMap (this, nfaMap);

		toString (buffer);
		nfaMap.remove (this);
		TreeSet<NFA> set = new TreeSet<NFA> (s_comparator);
		set.addAll (nfaMap.keySet ());

		for (NFA n: set)
			n.toString (buffer);

		return buffer.toString ();
	}

	public static boolean hasTrail (int flag)
	{
		return (flag & TRAIL_MASK) != 0;
	}

	public static int trailCount (int flag)
	{
		return flag >> 3;
	}
}
