package org.yuanheng.cookcc.parser.ast;

import java.util.ArrayList;
import java.util.List;

import org.yuanheng.cookcc.doc.ParserDoc;

/**
 * @author	Heng Yuan
 */
public abstract class AbstractSymbol implements Symbol
{
	private final String m_name;
	private int m_value;

	AbstractSymbol (String name)
	{
		this (name, -1);
	}

	AbstractSymbol (String name, int value)
	{
		m_name = name;
		m_value = value;
	}

	@Override
	public String getName ()
	{
		return m_name;
	}

	@Override
	public int getValue (SymbolLibrary library, long lineNumber)
	{
		if (m_value == -1)
			m_value = library.getSymbolValue (m_name, lineNumber);
		return m_value;
	}

	@Override
	public void addNewRules (ArrayList<SingleRule> rules, SymbolLibrary library, long lineNumber, ParserDoc parseDoc, ProductionCounter counter)
	{
	}

	public static String toString (List<Symbol> symbols)
	{
		if (symbols == null)
			return null;
		StringBuilder buffer = new StringBuilder ();
		boolean first = true;
		for (Symbol s : symbols)
		{
			if (first)
				first = false;
			else
				buffer.append (' ');
			buffer.append (s.toString ());
		}
		return buffer.toString ();
	}
}
