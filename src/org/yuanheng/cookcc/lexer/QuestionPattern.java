package org.yuanheng.cookcc.lexer;

/**
 * @author Heng Yuan
 * @version $Id$
 */
class QuestionPattern implements Pattern
{
	private final Pattern m_pattern;

	public QuestionPattern (Pattern pattern)
	{
		m_pattern = pattern;
	}

	public int getLength ()
	{
		return -1;
	}

	public NFA constructNFA (NFAFactory factory, NFA start)
	{
		start.next = factory.createNFA ();
		NFA end = m_pattern.constructNFA (factory, start.next);
		end.next = factory.createNFA ();
		start.next2 = end.next;
		return end.next;
	}

	@Override
	public String toString ()
	{
		return m_pattern + "?";
	}
}
