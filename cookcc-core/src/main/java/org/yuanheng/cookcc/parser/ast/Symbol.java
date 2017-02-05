package org.yuanheng.cookcc.parser.ast;

import java.util.ArrayList;

import org.yuanheng.cookcc.doc.ParserDoc;

/**
 * @author	Heng Yuan
 */
public interface Symbol
{
	/**
	 * Gets the string representation for the token.
	 *
	 * @return	the string representation for the token.
	 */
	public String getName ();

	/**
	 * Gets the integer value associated with the token.
	 *
	 * @param	library
	 *			symbolLibrary used to obtain the integer value if the value was
	 *			not obtained before.
	 * @param	lineNumber
	 * 			the line number associated with the token.
	 * @return	the integer value associated with the token.
	 */
	public int getValue (SymbolLibrary library, long lineNumber);

	/**
	 * If this symbol is an internal symbol.
	 *
	 * @return	if this symbol is an internal symbol.
	 */
	public boolean isInternal ();

	public void addNewRules (ArrayList<SingleRule> rules, long lineNumber, ParserDoc parseDoc, ProductionCounter counter);
}
