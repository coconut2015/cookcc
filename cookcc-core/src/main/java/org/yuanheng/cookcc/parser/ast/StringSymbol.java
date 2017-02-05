package org.yuanheng.cookcc.parser.ast;

/**
 * @author	Heng Yuan
 */
public class StringSymbol extends AbstractSymbol
{
	private boolean m_internal;

	public StringSymbol (String name, boolean internal)
	{
		super (name);
	}

	@Override
	public boolean isInternal ()
	{
		return m_internal;
	}

	@Override
	public String toString ()
	{
		return getName ();
	}
}
