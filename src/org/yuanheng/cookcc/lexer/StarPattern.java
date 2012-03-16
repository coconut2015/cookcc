package org.yuanheng.cookcc.lexer;

/**
 * @author Heng Yuan
 * @version $Id$
 */
class StarPattern implements Pattern
{
	private final Pattern m_pattern;

	public StarPattern (Pattern pattern)
	{
		m_pattern = pattern;
	}

	public int getLength ()
	{
		return -1;
	}

	@Override
	public String toString ()
	{
		return m_pattern + "*";
	}

	public NFA constructNFA (NFAFactory factory, NFA start)
	{
		start.next = factory.createNFA ();
		NFA end = m_pattern.constructNFA (factory, start.next);
		end.next = factory.createNFA ();
		end.next2 = start.next;
		start.next2 = end.next;
		return end.next;
	}
}
