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

import java.util.LinkedList;

/**
 * @author Heng Yuan
 * @version $Id$
 */
class NFAFactory
{
	private final CCL m_ccl;
	/* for computing equivalent classes */
	private final ECS m_ecs;
	/* for recycling NFAs */
	private final LinkedList<NFA> m_spareNFAs = new LinkedList<NFA> ();

	private int m_nfaCounter = 0;

	NFAFactory (CCL ccl)
	{
		m_ccl = ccl;
		m_ecs = new ECS (ccl.MAX_SYMBOL);
	}

	int incNFACounter ()
	{
		return m_nfaCounter++;
	}

	public CCL getCCL ()
	{
		return m_ccl;
	}

	public int getTotalNFACount ()
	{
		return m_nfaCounter - m_spareNFAs.size ();
	}

	public NFA createNFA ()
	{
		NFA nfa;
		if (m_spareNFAs.isEmpty ())
			nfa = new NFA (this);
		else
			nfa = m_spareNFAs.removeFirst ();
		return nfa;
	}

	public NFA createNFA (NFA sample)
	{
		NFA nfa = createNFA ();
		nfa.copyStates (sample);
		return nfa;
	}

	public NFA createNFA (int ch, boolean[] ccl)
	{
		NFA nfa = createNFA ();
		setNFA (nfa, ch, ccl);
		nfa.next = createNFA ();
		return nfa;
	}

	public void setNFA (NFA nfa, int ch, boolean[] ccl)
	{
		nfa.thisChar = ch;
		nfa.charSet = ccl;
		if (ch >= 0)
			m_ecs.add (ch);
		else if (ch == NFA.ISCCL)
			m_ecs.add (ccl);
	}

	public ECS getECS ()
	{
		return m_ecs;
	}

	public void deleteNFA (NFA nfa)
	{
		nfa.init ();
		m_spareNFAs.add (nfa);
	}

	@Override
	public String toString ()
	{
		return "total NFAs: " + getTotalNFACount () + ", maximum NFAs: " + m_nfaCounter;
	}
}
