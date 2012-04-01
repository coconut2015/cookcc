package org.yuanheng.cookcc.lexer;

/**
 * @author Heng Yuan
 * @version $Id$
 */
interface Pattern
{
	public boolean hasSubExpression ();

	public int getLength ();

	/**
	 * Construct NFAs for the existing pattern.
	 *
	 * @param	start
	 *			The start NFA to work with
	 * @return	The end NFA of the generated pattern.
	 */
	public NFA constructNFA (NFAFactory factory, NFA start);
}
