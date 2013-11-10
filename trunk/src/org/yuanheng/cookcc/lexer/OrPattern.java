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

/**
 * @author Heng Yuan
 * @version $Id$
 */
class OrPattern implements Pattern
{
	private static boolean[] getCCL (CCL ccl, Pattern pattern, boolean clone)
	{
		if (pattern instanceof CCLPattern)
		{
			boolean[] c = ((CCLPattern)pattern).getCharSet ();
			if (clone)
				return c.clone ();
			return c;
		}
		else if (pattern instanceof CharPattern)
		{
			boolean[] ccl1 = ccl.EMPTY.clone ();
			ccl1[((CharPattern)pattern).getChar ()] = true;
			return ccl1;
		}
		return null;
	}

	public static ChainPattern getOrPattern (CCL ccl, ChainPattern p1, ChainPattern p2)
	{
		if (p1.getLength () == 1 &&
			p2.getLength () == 1 &&
			p1.size () == 1 &&
			p2.size () == 1)
		{
			Pattern pattern1 = p1.getFirstPattern ();
			Pattern pattern2 = p2.getFirstPattern ();

			boolean[] c1 = getCCL (ccl, pattern1, true);
			boolean[] c2 = getCCL (ccl, pattern2, false);

			if (c1 != null && c2 != null)
			{
				CCL.merge (c1, c2);
				return new ChainPattern (new CCLPattern (c1));
			}
		}

		return new ChainPattern (new OrPattern (p1, p2));
	}

	private final ChainPattern m_left;
	private final ChainPattern m_right;

	private int m_length;

	private OrPattern (ChainPattern left, ChainPattern right)
	{
		m_left = left;
		m_right = right;

		int len = m_left.getLength ();
		if (len == -1 || len != m_right.getLength ())
			m_length = -1;
		else
			m_length = len;
	}

	public int getLength ()
	{
		return m_length;
	}

	@Override
	public String toString ()
	{
		return "(" + m_left + "|" + m_right + ")";
	}

	public NFA constructNFA (NFAFactory factory, NFA start)
	{
		start.next = factory.createNFA (start);
		start.next2 = factory.createNFA (start);
		NFA leftEnd = m_left.constructNFA (factory, start.next);
		NFA rightEnd = m_right.constructNFA (factory, start.next2);
		NFA end = factory.createNFA (start);
		leftEnd.next = end;
		rightEnd.next = end;
		return end;
	}

	public boolean hasSubExpression ()
	{
		return m_left.hasSubExpression () || m_right.hasSubExpression ();
	}
}
