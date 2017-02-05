package org.yuanheng.cookcc.parser.ast;

/**
 * @author	Heng Yuan
 */
public class ProductionCounter
{
	private short m_count;

	public ProductionCounter ()
	{
	}

	public short newId ()
	{
		return ++m_count;
	}

	public short getCount ()
	{
		return m_count;
	}
}
