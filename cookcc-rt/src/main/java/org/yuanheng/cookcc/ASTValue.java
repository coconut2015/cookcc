package org.yuanheng.cookcc;

import java.util.Collection;

/**
 * An ASTValue represents a terminal symbol passed by the lexer and its
 * corresponding value.
 *
 * @author	Heng Yuan
 * @since	0.4
 */
public final class ASTValue extends AST
{
	/**
	 * The enum value for the token.
	 *
	 * Not all terminals have a corresponding enum token value
	 * (such as character tokens like ',').  In these cases,
	 * this value is null.
	 */
	private final Enum<?> m_token;

	/**
	 * The value associated with this terminal.
	 */
	private Object m_value;

	/**
	 * Captured terminals.
	 */
	private Collection<ASTValue> m_capturedTerminals;

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
	public ASTValue (int symbol, String symbolName, Enum<?> token)
	{
		super (symbol, symbolName);
		m_token = token;
	}

	/**
	 * Get the value associated with the terminal.
	 *
	 * @return	the value associated with the terminal.
	 */
	public Object getValue ()
	{
		return m_value;
	}

	/**
	 * Set the value associated with the terminal.
	 * <p>
	 * This function typically should only be called by the generated parser.
	 *
	 * @param	value
	 *			the value associated with the terminal.
	 */
	public void setValue (Object value)
	{
		m_value = value;
	}

	public Collection<ASTValue> getCapturedTerminals ()
	{
		return m_capturedTerminals;
	}

	/**
	 * Add a new add AST node as child.
	 *
	 * @param	capturedTerminals
	 *			a list of ignored but captured terminals came right before this
	 *			ASTValue.
	 */
	public void setCapturedTerminals (Collection<ASTValue> capturedTerminals)
	{
		m_capturedTerminals = capturedTerminals;
	}

	/**
	 * Get the enum token value associated with the terminal.
	 *
	 * @return	The enum token value associated with the terminal.
	 */
	public Enum<?> getToken ()
	{
		return m_token;
	}

	/**
	 * Indicates that this AST node is an ASTValue node.
	 *
	 * @return	true
	 */
	@Override
	public final boolean isValue ()
	{
		return true;
	}

	/**
	 * Return the string representation of the value for debugging purpose.
	 *
	 * @return	the string representation of the value.
	 */
	@Override
	public String toString ()
	{
		if (m_value == null)
			return "null";
		return "[" + m_value.getClass ().getName () + ":" + m_value.toString () + "]";
	}
}
