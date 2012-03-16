package org.yuanheng.cookcc.lexer;

/**
 * @author Heng Yuan
 * @version $Id$
 */
class CharPattern implements Pattern
{
	private final char m_char;

	public CharPattern (char ch)
	{
		m_char = ch;
	}

	public char getChar ()
	{
		return m_char;
	}

	public int getLength ()
	{
		return 1;
	}

	@Override
	public String toString ()
	{
		switch (m_char)
		{
			case '(':
			case ')':
				return "\\" + m_char;
			default:
				return CCL.toString (m_char);
		}
	}

	public NFA constructNFA (NFAFactory factory, NFA start)
	{
		start.thisChar = m_char;
		NFA end = factory.createNFA ();
		start.next = end;
		return end;
	}
}
