package org.yuanheng.cookcc.parser.ast;

/**
 * @author	Heng Yuan
 */
public abstract class InternalSymbol extends AbstractSymbol
{
	private final Symbol m_symbol;
	public InternalSymbol (Symbol symbol)
	{
		super (symbol.getName ());
		m_symbol = symbol;
	}

	Symbol getSymbol ()
	{
		return m_symbol;
	}

	@Override
	public int getValue (SymbolLibrary library, long lineNumber)
	{
		return m_symbol.getValue (library, lineNumber);
	}

	@Override
	public boolean isInternal ()
	{
		return true;
	}

}
