package org.yuanheng.cookcc.parser.ast;

import java.util.HashMap;

/**
 * @author	Heng Yuan
 */
class DummySymbolLib implements SymbolLibrary
{
	private int m_internalSymbolCount;
	private final HashMap<String, Symbol> m_symbolMap = new HashMap<String, Symbol> ();
	private final HashMap<Character, Symbol> m_charSymbolMap = new HashMap<Character, Symbol> ();

	@Override
	public Symbol getSymbol (String name)
	{
		Symbol symbol = m_symbolMap.get (name);
		if (symbol == null)
		{
			symbol = new StringSymbol (name, false);
			m_symbolMap.put (name, symbol);
		}
		return symbol;
	}

	@Override
	public Symbol getSymbol (char ch)
	{
		Character c = Character.valueOf (ch);
		Symbol symbol = m_charSymbolMap.get (c);
		if (symbol == null)
		{
			symbol = new CharSymbol (ch);
			m_charSymbolMap.put (c, symbol);
		}
		return symbol;
	}

	@Override
	public int getSymbolValue (String symbol, long lineNumber)
	{
		return 0;
	}

	@Override
	public Symbol createInternalRule (Symbol[] rhs)
	{
		return null;
	}

	@Override
	public Symbol createInternalSymbol ()
	{
		String symbol = "$" + (++m_internalSymbolCount);
		return new StringSymbol (symbol, true);
	}

	@Override
	public void addRule (Symbol lhs, Symbol[] rhs)
	{
	}
}
