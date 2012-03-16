package org.yuanheng.cookcc.lexer;

/**
 * @author Heng Yuan
 * @version $Id$
 */
class Repeat
{
	private final int m_min;
	private final int m_max;

	public Repeat (int min, int max)
	{
		m_min = min;
		m_max = max;
	}

	public int getMin ()
	{
		return m_min;
	}

	public int getMax ()
	{
		return m_max;
	}

	@Override
	public String toString ()
	{
		if (m_min == m_max)
			return "{" + m_min + "}";
		String text;
		if (m_min == 0)
			text = "{,";
		else
			text = "{" + m_min + ",";
		if (m_max == Integer.MAX_VALUE)
			text += "}";
		else
			text += m_max + "}";
		return text;
	}
}
