package org.yuanheng.cookcc;

/**
 * This class is for extended grammar with ( ), +, *, ?, | operators.
 *
 * @author	Heng Yuan
 * @since	0.4
 */
public abstract class AST
{
	/**
	 * This symbol value is the parser symbol being
	 */
	private final int m_symbol;

	/**
	 * This is the string name for the symbol.
	 */
	private final String m_symbolName;

	/**
	 * Set the integer value of the symbol.
	 *
	 * @param	symbol
	 *			the integer representation of a symbol in the AST.
	 * @param	symbolName
	 *			the symbol name of the AST.  The parser being generated
	 *			will attempt to re-use the same string as much as
	 *			possible to reduce the memory usage.
	 */
	public AST (int symbol, String symbolName)
	{
		m_symbol = symbol;
		m_symbolName = symbolName;
	}

	/**
	 * Get the integer value of symbol.
	 *
	 * @return	the symbol of the node.
	 */
	public int getSymbol ()
	{
		return m_symbol;
	}

	/**
	 * Get the string representation of the symbol.
	 *
	 * @return	the string representation of the symbol.
	 */
	public String getSymbolName ()
	{
		return m_symbolName;
	}

	@Override
	public String toString ()
	{
		return m_symbolName;
	}
}
