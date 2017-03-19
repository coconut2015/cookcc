package org.yuanheng.cookcc;

/**
 * This class is for people who wish to get more detailed information
 * than the default handling of shortcuts as well as automatically
 * generated trees.
 * <p>
 * The default handling and auto tree generation only considers the
 * yyValue() obtained from the lexer, without storing any additional
 * information such as token name, tree name etc.
 * <p>
 * This class allows additional information such as symbol value,
 * symbol name, enum value for the lexical tokens, etc to be captured
 * as well.
 * <p>
 * It should be noted that while it is very useful for small tree nodes
 * that do not get referenced much in a grammar, it is not recommended
 * to use it for important tree nodes.
 * <p>
 * For example, you can use it to quickly parser certain flags and it is
 * convenient.
 * <pre>
 *     Flags : ( PUBLIC | STATIC | FINAL | CONST | VOLATILE )*
 * </pre>
 * It saves you from writing the like the following since all you care
 * is an array of flags and you will have to check for duplicates later
 * on anyways.
 * <pre>
 *     Flag : PUBLIC { ... }
 *          | STATIC { ... }
 *          | FINAL { ... }
 *          ...
 *          ;
 *    Flags : Flags Flag { ... }
 *          | {...}
 *          ;
 * </pre>
 *
 * But using it in cases like the following is bad.
 * <pre>
 *     Expr : Expr '+' Expr
 *          | '(' Expr ')'
 * </pre>
 * The main reason is that you have to reference individual parts by
 * index value.  It gets messy and not extensible when the grammar is
 * changed slightly (and thus positions gets shifted).  On top of that,
 * the above example makes use of heterogeneous types for the same index
 * position.
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

	/**
	 * Check if this AST node is an ASTValue node.
	 *
	 * @return	if this node is an ASTValue
	 */
	public abstract boolean isValue ();

	@Override
	public String toString ()
	{
		return m_symbolName;
	}
}
