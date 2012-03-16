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

	private final ChainPattern	m_left;
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
		start.next = factory.createNFA ();
		start.next2 = factory.createNFA ();
		NFA leftEnd = m_left.constructNFA (factory, start.next);
		NFA rightEnd = m_right.constructNFA (factory, start.next2);
		NFA end = factory.createNFA ();
		leftEnd.next = end;
		rightEnd.next = end;
		return end;
	}
}
