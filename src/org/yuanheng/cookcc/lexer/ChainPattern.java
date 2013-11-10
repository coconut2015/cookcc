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

import java.util.ArrayList;

/**
 * @author Heng Yuan
 * @version $Id$
 */
class ChainPattern implements Pattern
{
	private final ArrayList<Pattern> m_patterns = new ArrayList<Pattern> ();

	private int m_subExpId;
	private int m_length;

	public ChainPattern (Pattern pattern)
	{
		m_patterns.add (pattern);
		m_length = pattern.getLength ();
	}

	public void setSubExpId (int id)
	{
		m_subExpId = id;
	}

	public int getSubExpId ()
	{
		return m_subExpId;
	}

	public int size ()
	{
		return m_patterns.size ();
	}

	public Pattern getFirstPattern ()
	{
		return m_patterns.get (0);
	}

	public void addPattern (Pattern pattern)
	{
		m_patterns.add (pattern);

		if (m_length == -1)
			return;
		int length = pattern.getLength ();
		if (length == -1)
			m_length = -1;
		else
			m_length += length;
	}

	public int getLength ()
	{
		return m_length;
	}

	@Override
	public String toString ()
	{
		if (m_patterns.size () == 1)
			return m_patterns.get (0).toString ();
		StringBuffer buffer = new StringBuffer ();
		buffer.append ("(");
		for (Pattern pattern : m_patterns)
		{
			buffer.append (pattern);
		}
		buffer.append (")");
		return buffer.toString ();
	}

	public NFA constructNFA (NFAFactory factory, NFA start)
	{
		for (Pattern pattern : m_patterns)
		{
			start = pattern.constructNFA (factory, start);
		}
		return start;
	}

	public boolean hasSubExpression ()
	{
		return m_subExpId > 0;
	}
}
