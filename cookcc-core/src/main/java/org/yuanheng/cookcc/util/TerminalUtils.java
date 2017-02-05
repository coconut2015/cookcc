package org.yuanheng.cookcc.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

import org.yuanheng.cookcc.doc.TokensDoc;
import org.yuanheng.cookcc.exception.ParserException;
import org.yuanheng.cookcc.lexer.CCL;
import org.yuanheng.cookcc.parser.Token;

/**
 * @author Heng Yuan
 */
public class TerminalUtils
{
	private static Pattern s_tokenNamePattern = Pattern.compile ("[a-zA-Z_][a-zA-Z_0-9]*");

	public final static int INIT_MAX_TERMINALS = 255;

	public static String checkTerminalName (long lineNumber, String name, int[] value, boolean noInternal, Map<Integer, String> symbolMap)
	{
		try
		{
			if (name.startsWith ("'"))
			{
				int[] pos = new int[1];
				pos[0] = 1;
				char ch = CCL.esc (name, pos);
				if (name.length () == (pos[0] + 1) && name.charAt (pos[0]) == '\'')
				{
					value[0] = ch;
					++pos[0];

					if (symbolMap.get ((int)ch) == null)
						symbolMap.put ((int)ch, name);
					return String.valueOf ((int)ch);
				}
			}
			else if (s_tokenNamePattern.matcher (name).matches ())
			{
				if (noInternal && "error".equals (name))
					throw new ParserException (0, "error token is built-in");
				return name;
			}
		}
		catch (ParserException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
		}
		throw new ParserException (lineNumber, "Invalid token name: " + name);
	}

	public static int parseTerminals (Map<String, Token> terminals,
									  Map<Integer, Token> terminalMap,
									  Map<Integer, String> symbolMap,
									  ArrayList<Token> tokens,
									  TokensDoc[] tokensDocs)
	{
		int maxTerminalValue = INIT_MAX_TERMINALS;

		if (tokensDocs == null)
			return maxTerminalValue;

		int precedenceLevel = 0;
		int[] checkValue = new int[1];

		for (TokensDoc tokensDoc : tokensDocs)
		{
			int level = precedenceLevel++;
			String[] names = tokensDoc.getTokens ();
			if (names == null)
				continue;
			for (String name : names)
			{
				if (terminals.containsKey (name))
					throw new ParserException (tokensDoc.getLineNumber (), "Duplicate token " + name + " specified.");

				checkValue[0] = 0;
				name = checkTerminalName (tokensDoc.getLineNumber (), name, checkValue, true, symbolMap);
				int v = checkValue[0];
				if (v == 0)
					v = ++maxTerminalValue;
				Token token = new Token (name, level, v, tokensDoc.getType ());
				terminals.put (name, token);
				terminalMap.put (v, token);

				if (symbolMap.get (v) == null)
					symbolMap.put (v, name);
				if (checkValue[0] == 0)
					tokens.add (token);
			}
		}
		return maxTerminalValue;
	}
}
