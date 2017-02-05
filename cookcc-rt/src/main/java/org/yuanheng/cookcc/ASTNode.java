package org.yuanheng.cookcc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * ASTNode is used to represent a non-terminal node and its associated
 * children.
 * <p>
 * It should be noted that this class is mostly provided for convenience.
 * It is very risky and non-extensible in the long run to access the member
 * by its array index value.
 *
 * @author	Heng Yuan
 * @since	0.4
 */
public class ASTNode extends AST implements Collection<AST>
{
	/**
	 * The grammar rule used to generate this AST node.
	 */
	private final int m_rule;

	/**
	 * Child AST nodes.
	 */
	private ArrayList<AST> m_children;

	/**
	 * Set the integer value of the symbol.
	 *
	 * @param	symbol
	 *			the integer representation of a symbol in the AST.
	 * @param	symbolName
	 *			the symbol name of the AST.  The parser being generated
	 *			will attempt to re-use the same string as much as
	 *			possible to reduce the memory usage.
	 * @param	rule
	 *			the grammar that generated this ASTNode.
	 */
	public ASTNode (int symbol, String symbolName, int rule)
	{
		super (symbol, symbolName);
		m_rule = rule;
	}

	public AST get (int index)
	{
		return m_children.get (index);
	}

	/**
	 * Get the grammar rule used to generate this AST node.
	 *
	 * @return	the grammar rule used to generate this AST node.
	 */
	public int getRule ()
	{
		return m_rule;
	}

	/**
	 * Indicate that this node is a non-terminal node.
	 *
	 * @return	false
	 */
	@Override
	public final boolean isValue ()
	{
		return false;
	}

	@Override
	public int size ()
	{
		return m_children.size ();
	}

	@Override
	public boolean isEmpty ()
	{
		return m_children.isEmpty ();
	}

	@Override
	public boolean contains (Object o)
	{
		return m_children.contains (o);
	}

	@Override
	public Iterator<AST> iterator ()
	{
		return m_children.iterator ();
	}

	@Override
	public AST[] toArray ()
	{
		return m_children.toArray (new AST[m_children.size ()]);
	}

	@Override
	public <T> T[] toArray (T[] a)
	{
		return m_children.toArray (a);
	}

	@Override
	public boolean add (AST e)
	{
		return m_children.add (e);
	}

	@Override
	public boolean remove (Object o)
	{
		return m_children.remove (o);
	}

	@Override
	public boolean containsAll (Collection<?> c)
	{
		return m_children.containsAll (c);
	}

	@Override
	public boolean addAll (Collection<? extends AST> c)
	{
		return m_children.addAll (c);
	}

	@Override
	public boolean removeAll (Collection<?> c)
	{
		return m_children.removeAll (c);
	}

	@Override
	public boolean retainAll (Collection<?> c)
	{
		return m_children.retainAll (c);
	}

	@Override
	public void clear ()
	{
		m_children.clear ();
	}
}
