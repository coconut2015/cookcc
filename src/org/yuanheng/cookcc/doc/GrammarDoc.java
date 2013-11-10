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
package org.yuanheng.cookcc.doc;

import java.util.LinkedList;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class GrammarDoc extends TreeDoc
{
	private final String m_rule;

	private final LinkedList<RhsDoc> m_rhs = new LinkedList<RhsDoc> ();

	private char m_type = 'n';

	public GrammarDoc (String rule)
	{
		if (rule == null || rule.length () == 0)
			throw new IllegalArgumentException ("rule must not be empty.");
		m_rule = rule;
	}

	/**
	 * Internally called for internally generated grammar rules.
	 *
	 * @param    type One of 'n', '?', '+', '*'.
	 */
	public void setType (char type)
	{
		m_type = type;
	}

	/**
	 * The type of the rule.  For non-internally generated rules should
	 * have a character 'n'.  '?' means optional token, '*' is an optional list,
	 * '+' is a list.
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
