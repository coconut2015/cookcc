package org.yuanheng.cookcc.parser.ast;

/**
 * @author	Heng Yuan
 */
public interface SymbolLibrary
{
	/**
	 * Get the token node associated with a symbol.
	 *
	 * @param	name
	 * 			string name
	 * @return	symbol node representing the terminal / nonTerminal.
	 */
	public Symbol getSymbol (String name);

	/**
	 * Get the token node associated with a character.
	 *
	 * @param	ch
	 * 			character representation
	 * @return	symbol node representing the terminal represented using character.
	 */
	public Symbol getSymbol (char ch);

	/**
	 * Create a new internal symbol.
	 *
	 * @return	a new internal symbol.
	 */
	public Symbol createInternalSymbol ();

	/**
	 * Given a string symbol, get the integer value for the symbol.
	 *
	 * @param	symbol
	 *			string representation of the token
	 * @param	lineNumber
	 * 			the line number for the symbol or the rule where the symbol
	 * 			is in.
	 * @return	the integer value associated with the symbol
	 */
	public int getSymbolValue (String symbol, long lineNumber);

	/**
	 * Creates a new internal rule
	 *
	 * @param	rhs
	 * 			The production for the rule.
	 * @return	An internal symbol of LHS.
	 */
	public Symbol createInternalRule (Symbol[] rhs);

	/**
	 * Add a new rule.
	 * @param	lhs
	 * 			the left hand symbol
	 * @param	rhs
	 * 			the right hand symbols
	 */
	public void addRule (Symbol lhs, Symbol[] rhs);
}
