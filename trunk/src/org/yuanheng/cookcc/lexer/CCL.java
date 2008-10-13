/*
 * Copyright (c) 2008, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Heng Yuan nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY Heng Yuan ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Heng Yuan BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.yuanheng.cookcc.lexer;

import org.yuanheng.cookcc.exception.CCLException;
import org.yuanheng.cookcc.exception.EscapeSequenceException;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class CCL
{
	private static class ByteCCL
	{
		private final static CCL s_instance = new CCL (255);
	}

	private static class CharacterCCL
	{
		private final static CCL s_instance = new CCL (Character.MAX_VALUE);
	}

	public static CCL getByteCCL ()
	{
		return ByteCCL.s_instance;
	}

	public static CCL getCharacterCCL ()
	{
		return CharacterCCL.s_instance;
	}

	/* 0 to max, then add <<EOF>> as a token */
	public final int MAX_SYMBOL;
	public final int EOF;

	public final boolean[] EMPTY;
	public final boolean[] ANY;
	public final boolean[] ALL;

	public final boolean[] LOWER;
	public final boolean[] UPPER;
	public final boolean[] ASCII;
	public final boolean[] ALPHA;
	public final boolean[] DIGIT;
	public final boolean[] ALNUM;
	public final boolean[] PUNCT;
	public final boolean[] GRAPH;
	public final boolean[] PRINT;
	public final boolean[] BLANK;
	public final boolean[] CNTRL;
	public final boolean[] XDIGIT;
	public final boolean[] SPACE;

	private CCL (int maxSymbol)
	{
		MAX_SYMBOL = maxSymbol + 1;
		EOF = MAX_SYMBOL;

		EMPTY = new boolean[MAX_SYMBOL + 1];
		ALL = negate (EMPTY.clone ());

		ANY = parseCCL ("[^\\n]");

		LOWER = parseCCL ("[a-z]");
		UPPER = parseCCL ("[A-Z]");
		ASCII = parseCCL ("[\\x0-\\x7f]");
		ALPHA = parseCCL ("[A-Za-z]");
		DIGIT = parseCCL ("[0-9]");
		ALNUM = parseCCL ("[A-Za-z0-9]");
		PUNCT = parseCCL ("[!\"#$%&'()*+,\\-./:;<=>?@\\[\\\\\\]^_`{|}~]");
		GRAPH = merge (ALNUM.clone (), PUNCT);
		PRINT = GRAPH;
		BLANK = parseCCL ("[ \t]");
		CNTRL = parseCCL ("[\\x0-\\x1f\\x7f]");
		XDIGIT = parseCCL ("[0-9a-fA-F]");
		SPACE = parseCCL ("[ \\t\\n\\x0B\\f\\r]");
	}

	/**
	 * Compute the escape sequence character.
	 *
	 * @return	the character scanned, or -1 on error (end of input etc).  pos
	 * 			also stores the scanned position.
	 * @throws	org.yuanheng.cookcc.exception.EscapeSequenceException
	 * 			when the input has invalid format or is empty
	 * @param	inputChars
	 * 			input char array
	 * @param	currentPos
	 * 			an array size 1 of the current position to be scanned.  New
	 * 			position after scan is stored back into this array.
	 */
	public static char esc (char[] inputChars, int[] currentPos) throws EscapeSequenceException
	{
		int start = currentPos[0];	// remember the original position for error reporting
		int pos = currentPos[0];
		if (inputChars[pos] != '\\')
		{
			++currentPos[0];
			return inputChars[pos];	// not a escape code
		}
		else
		{
			++pos;					// skip '\\'
			if (pos >= inputChars.length)
				throw new EscapeSequenceException (new String ("\\"));
			char ch = inputChars[pos++];
			currentPos[0] = pos;
			switch (ch)
			{
				case 'b':
				case 'B':
					return '\b';
				case 'f':
				case 'F':
					return '\f';
				case 'n':
				case 'N':
					return '\n';
				case 'r':
				case 'R':
					return '\r';
				case 's':
				case 'S':
					return ' ';
				case 't':
				case 'T':
					return '\t';
				case 'e':
				case 'E':
					return '\033';
				case 'x':
				{
					int rval = 0;

					if (pos >= inputChars.length)
						throw new EscapeSequenceException (new String (inputChars, start, inputChars.length - start));
					ch = inputChars[pos++];
					if ('0' <= ch && ch <= '9')
						rval = ch - '0';
					else if ('a' <= ch && ch <= 'f')
						rval = ch - 'a' + 10;
					else if ('A' <= ch && ch <= 'F')
						rval = ch - 'A' + 10;
					else
						throw new EscapeSequenceException (new String (inputChars, start, inputChars.length - start));

					if (pos >= inputChars.length)
					{
						currentPos[0] = pos;
						return (char)rval;
					}

					ch = inputChars[pos++];
					if ('0' <= ch && ch <= '9')
						rval = (rval << 4) + ch - '0';
					else if ('a' <= ch && ch <= 'f')
						rval = (rval << 4) + ch - 'a' + 10;
					else if ('A' <= ch && ch <= 'F')
						rval = (rval << 4) + ch - 'A' + 10;

					currentPos[0] = pos;
					return (char)rval;
				}

				case 'u':
				{
					int rval = 0;

					if (pos >= inputChars.length)
						throw new EscapeSequenceException (new String (inputChars, start, inputChars.length - start));
					ch = inputChars[pos++];
					if ('0' <= ch && ch <= '9')
						rval = ch - '0';
					else if ('a' <= ch && ch <= 'f')
						rval = ch - 'a' + 10;
					else if ('A' <= ch && ch <= 'F')
						rval = ch - 'A' + 10;
					else
						throw new EscapeSequenceException (new String (inputChars, start, inputChars.length - start));

					for (int i = 0; i < 3; ++i)
					{
						if (pos >= inputChars.length)
						{
							currentPos[0] = pos;
							return (char)rval;
						}

						ch = inputChars[pos++];
						if ('0' <= ch && ch <= '9')
							rval = (rval << 4) + ch - '0';
						else if ('a' <= ch && ch <= 'f')
							rval = (rval << 4) + ch - 'a' + 10;
						else if ('A' <= ch && ch <= 'F')
							rval = (rval << 4) + ch - 'A' + 10;
						else
							break;
					}
					currentPos[0] = pos;
					return (char)rval;
				}

				default:
				{
					if (ch < '0' || ch > '7')
					{
						currentPos[0] = pos;
						return ch;
					}
					else
					{
						int rval = ch - '0';

						if (pos >= inputChars.length)
						{
							currentPos[0] = pos;
							return (char)rval;
						}

						ch = inputChars[pos++];
						if ('0' <= ch && ch <= '7')
							rval = (rval << 3) + ch - '0';
						else
						{
							currentPos[0] = pos;
							return (char)rval;
						}

						if (pos >= inputChars.length)
						{
							currentPos[0] = pos;
							return (char)rval;
						}

						ch = inputChars[pos++];
						if ('0' <= ch && ch <= '7')
							rval = (rval << 3) + ch - '0';
						currentPos[0] = pos;
						return (char)rval;
					}
				}
			}
		}
	}

	public boolean[] parseCCL (String input) throws CCLException
	{
		char[] inputChars = input.toCharArray ();
		int[] escPos = new int[1];
		int pos = 0;

		boolean[] map = new boolean[MAX_SYMBOL + 1];

		pos++;						// skip past the [
		boolean negative = inputChars[pos] == '^';
		if (negative)				// check for negative
			pos++;

		int start = pos;			// mark the start

		try
		{
			char first = '\0';
			char ch;
			while (pos < inputChars.length && (ch = inputChars[pos]) != ']')
			{
				if (ch != '-')			// potentially the first side of a range
				{
					escPos[0] = pos;
					first = esc (inputChars, escPos);	// check escape sequence
					map[first] = true;
					pos = escPos[0];
				}
				else if (pos == start)	// leading '-'
				{
					map['-'] = true;
					++pos;
				}
				else					// now we have a range
				{
					char last;
					++pos;				// skip '-'
					escPos[0] = pos;
					last = esc (inputChars, escPos);	// check escape sequence
					pos = escPos[0];
					if (last < first)
					{
						char tmp = last;
						last = first;
						first = tmp;
					}
					for (int i = first; i <= last; ++i)
						map[i] = true;
				}
			}
		}
		catch (Exception ex)
		{
			throw new CCLException (input, ex);
		}

		if (inputChars[pos] != ']')
			throw new CCLException (input);			// give error

		if (negative)
			for (int i = 0; i <= MAX_SYMBOL; ++i)
				map[i] = !map[i];					// invert all bits

		return map;
	}

	public static boolean[] merge (boolean[] c1, boolean[] c2)
	{
		for (int i = 0; i < c1.length; ++i)
			c1[i] |= c2[i];
		return c1;
	}

	public static boolean[] subtract (boolean[] c1, boolean[] c2)
	{
		for (int i = 0; i < c1.length; ++i)
			c1[i] &= !c2[i];
		return c1;
	}

	public static boolean[] negate (boolean[] ccl)
	{
		for (int i = 0; i < ccl.length; ++i)
			ccl[i] = !ccl[i];
		return ccl;
	}

	private static String toString (char c)
	{
		int j = -1;
		switch (c)
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
			case '"':
			case '\'':
			case '\\':
			case '-':
			case '[':
			case ']':
				j = c;
				break;
		}

		if (j > 0)
			return "\\" + (char)j;

		if (c >= 32 && c < 127)
			return Character.toString (c);

		return "\\u" + Integer.toHexString ((c & 0xffff));
	}

	private String printCCL (boolean[] ccl)
	{
		String s = "";
		int cont = 0;
		int i;

		for (i = 0; i <= MAX_SYMBOL; i++)
		{
			if (ccl[i])
			{
				if (cont == 0)
					s += toString ((char)i);
				++cont;
			}
			else
			{
				if (cont == 2)
					s += toString ((char)(i - 1));
				else if (cont > 2)
				{
					s += '-';
					s += toString ((char)(i - 1));
				}
				cont = 0;
			}
		}
		if (cont == 2)
			s += toString ((char)(i - 1));
		else if (cont > 2)
		{
			s += '-';
			s += toString ((char)(i - 1));
		}
		return s;
	}

	/**
	 * Convert a CCL map to a string representation for debugging purpose.
	 */
	public String toString (boolean[] ccl)
	{
		if (ccl == ANY)
			return ".";
		if (ccl == ALL)
			return "ALL";

		boolean[] neg = negate (ccl.clone ());
		String s1 = printCCL (ccl);
		String s2 = printCCL (neg);

		String s = "[";
		if (s1.length () <= s2.length ())
			s += s1;
		else
		{
			s += '^';
			s += s2;
		}
		s += ']';

		return s;
	}
}