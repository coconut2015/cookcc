package org.yuanheng.cookcc.lexer;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class CCLPattern implements Pattern
{
	private final boolean[] m_charSet;

	public CCLPattern (boolean[] ccl)
	{
		m_charSet = ccl;
	}

	public int getLength ()
	{
		return 1;
	}

	public boolean[] getCharSet ()
	{
		return m_charSet;
	}

	@Override
	public String toString ()
	{
		if (m_charSet.length <= 257)
		{
			return CCL.getByteCCL ().toString (m_charSet);
		}
		return CCL.getCharacterCCL ().toString (m_charSet);
	}

	public NFA constructNFA (NFAFactory factory, NFA start)
	{
		start.charSet = m_charSet;
		NFA end = factory.createNFA ();
		start.next = end;
		return end;
	}
}
