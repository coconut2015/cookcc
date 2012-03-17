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
}
