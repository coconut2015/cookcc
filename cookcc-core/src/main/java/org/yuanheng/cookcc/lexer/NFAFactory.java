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

import java.util.LinkedList;

/**
 * @author Heng Yuan
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
		if (nfa.nocase && ch > 0)
			ch = Character.toLowerCase (ch);
		nfa.thisChar = ch;
		nfa.charSet = ccl;
		if (ch >= 0)
			m_ecs.add (ch, nfa.nocase);
		else if (ch == NFA.ISCCL)
			m_ecs.add (ccl, nfa.nocase);
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
