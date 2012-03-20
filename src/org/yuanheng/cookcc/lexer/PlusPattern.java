package org.yuanheng.cookcc.lexer;

/**
 * @author Heng Yuan
 * @version $Id$
 */
class PlusPattern implements Pattern
{
	private final Pattern m_pattern;

	public PlusPattern (Pattern pattern)
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
		return m_pattern + "+";
	}

	public NFA constructNFA (NFAFactory factory, NFA start)
	{
		start.next = factory.createNFA (start);
		NFA end = m_pattern.constructNFA (factory, start.next);
		end.next = factory.createNFA (start);
		end.next2 = start.next;
		return end.next;
	}
}
