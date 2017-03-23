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
public class ASTNode extends AST implements Collection<Object>
{
	private final static Collection<Object> s_emptyContainer = new ArrayList<Object> ();

	/**
	 * The grammar rule used to generate this AST node.
	 */
	private final int m_rule;

	/**
	 * Child AST nodes.
	 */
	private ArrayList<Object> m_children;

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

	public Object get (int index)
	{
		if (m_children == null)
			return null;
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

	@Override
	public int size ()
	{
		if (m_children == null)
			return 0;
		return m_children.size ();
	}

	@Override
	public boolean isEmpty ()
	{
		if (m_children == null)
			return true;
		return m_children.isEmpty ();
	}

	@Override
	public boolean contains (Object o)
	{
		if (m_children == null)
			return false;
		return m_children.contains (o);
	}

	@Override
	public Iterator<Object> iterator ()
	{
		if (m_children == null)
			return s_emptyContainer.iterator ();
		return m_children.iterator ();
	}

	@Override
	public Object[] toArray ()
	{
		if (m_children == null)
			return new Object[0];
		return m_children.toArray ();
	}

	@Override
	public <T> T[] toArray (T[] a)
	{
		if (m_children == null)
			return s_emptyContainer.toArray (a);
		return m_children.toArray (a);
	}

	@Override
	public boolean add (Object e)
	{
		if (m_children == null)
		{
			m_children = new ArrayList<Object> ();
		}
		return m_children.add (e);
	}

	@Override
	public boolean remove (Object o)
	{
		if (m_children == null)
			return false;
		return m_children.remove (o);
	}

	@Override
	public boolean containsAll (Collection<?> c)
	{
		if (m_children == null)
			return false;
		return m_children.containsAll (c);
	}

	@Override
	public boolean addAll (Collection<? extends Object> c)
	{
		if (m_children == null)
			return false;
		return m_children.addAll (c);
	}

	@Override
	public boolean removeAll (Collection<?> c)
	{
		if (m_children == null)
			return false;
		return m_children.removeAll (c);
	}

	@Override
	public boolean retainAll (Collection<?> c)
	{
		if (m_children == null)
			return false;
		return m_children.retainAll (c);
	}

	@Override
	public void clear ()
	{
		if (m_children != null)
			m_children.clear ();
	}

	@Override
	public String toString ()
	{
		StringBuilder buffer = new StringBuilder ();
		buffer.append (getSymbolName()).append (" : ");
		if (m_children != null)
			buffer.append (m_children);
		return buffer.toString ();
	}
}
