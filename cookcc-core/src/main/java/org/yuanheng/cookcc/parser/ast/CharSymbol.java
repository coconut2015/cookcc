package org.yuanheng.cookcc.parser.ast;

/**
 * @author	Heng Yuan
 */
public class CharSymbol extends AbstractSymbol
{
	private static String getName (char ch)
	{
		int j = -1;
		switch (ch)
		{
			case 0:
				j = '0';
				break;
			case '\b':
				j = 'b';
				break;
			case '\f':
				j = 'f';
				break;
			case '\t':
				j = 't';
				break;
			case '\n':
				j = 'n';
				break;
			case '\r':
				j = 'r';
				break;
			case ' ':
				j = 's';
				break;
			case '"':
			case '\'':
			case '\\':
				j = ch;
				break;
		}

		if (j > 0)
			return "'\\" + (char)j + "'";

		if (ch < ' ')
		{
			return "'\\x" + Integer.toHexString (ch) + "'";
		}

		return "'" + ch + "'";
	}

	public CharSymbol (char ch)
	{
		super (getName (ch), ch);
	}

	@Override
	public boolean isInternal ()
	{
		return false;
	}

	@Override
	public String toString ()
	{
		return getName ();
	}
}
