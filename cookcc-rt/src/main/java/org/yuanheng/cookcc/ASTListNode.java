package org.yuanheng.cookcc;

/**
 * This is a simple class that indicates that it is a list.
 *
 * @author	Heng Yuan
 * @since	0.4
 */
public class ASTListNode extends ASTNode
{
	public ASTListNode (int symbol, String symbolName, int rule)
	{
		super (symbol, symbolName, rule);
	}

	@Override
	public String toString ()
	{
		if (size () == 0)
		{
			StringBuilder buffer = new StringBuilder ();
			buffer.append (getSymbolName()).append (" : []");
			return buffer.toString ();
		}
		return super.toString ();
	}
}
