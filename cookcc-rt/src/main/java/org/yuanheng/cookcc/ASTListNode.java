package org.yuanheng.cookcc;

/**
 * This is a simple class that indicates that it is a list.
 *
 * @author	Heng Yuan
 * @since	0.4
 */
public class ASTListNode extends ASTNode
{
	public ASTListNode (int symbol, String symbolName, int rule, int elementRule)
	{
		super (symbol, symbolName, rule);
	}
}
