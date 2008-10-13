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

import org.yuanheng.cookcc.exception.LookaheadException;

/**
 * Hand written rule parser.
 *
 * @author Heng Yuan
 * @version $Id$
 */
class RuleParser
{
	private final boolean m_nocase;

	public RuleParser (boolean nocase)
	{
		m_nocase = nocase;
	}

	private String parseRegex ()
	{
		return null;
	}

	private boolean[] parseFullCCL (int lineNumber, char[] inputChars, int[] pos)
	{
		if (inputChars[pos[0]] != '[')
			return null;
		++pos[0];
		boolean neg = false;
		if (inputChars[pos[0]] == '^')
		{
			neg = true;
			++pos[0];
		}
		boolean[] ccl = new boolean[CCL.SYMBOL_MAX + 1];
		ccl = parseCCL (lineNumber, inputChars, pos, ccl);
		if (m_nocase)
		{
			for (int i = 'a'; i <= 'z'; ++i)
			{
				if (ccl[i])
					ccl[i - 'a' + 'A'] = true;
				if (ccl[i - 'a' + 'A'])
					ccl[i] = true;
			}
		}
		if (neg)
			CCL.negate (ccl);
		if (inputChars[pos[0]] != ']')
			throw new LookaheadException (lineNumber, ']', inputChars[pos[0]]);
		return ccl;
	}

	private boolean[] parseCCL (int lineNumber, char[] inputChars, int[] pos, boolean[] ccl)
	{
		if (parseCCE (inputChars, pos, ccl) == null)
			return null;
		return ccl;
	}

	private boolean[] parseCCE (char[] inputChars, int[] pos, boolean[] ccl)
	{
		if (ifMatch (inputChars, pos, "[:lower:]"))
			return CCL.merge (ccl, CCL.LOWER);
		if (ifMatch (inputChars, pos, "[:upper:]"))
			return CCL.merge (ccl, CCL.UPPER);
		if (ifMatch (inputChars, pos, "[:ascii:]"))
			return CCL.merge (ccl, CCL.ASCII);
		if (ifMatch (inputChars, pos, "[:alpha:]"))
			return CCL.merge (ccl, CCL.ALPHA);
		if (ifMatch (inputChars, pos, "[:digit:]"))
			return CCL.merge (ccl, CCL.DIGIT);
		if (ifMatch (inputChars, pos, "[:alnum:]"))
			return CCL.merge (ccl, CCL.ALNUM);
		if (ifMatch (inputChars, pos, "[:punct:]"))
			return CCL.merge (ccl, CCL.PUNCT);
		if (ifMatch (inputChars, pos, "[:graph:]"))
			return CCL.merge (ccl, CCL.GRAPH);
		if (ifMatch (inputChars, pos, "[:print:]"))
			return CCL.merge (ccl, CCL.PRINT);
		if (ifMatch (inputChars, pos, "[:blank:]"))
			return CCL.merge (ccl, CCL.BLANK);
		if (ifMatch (inputChars, pos, "[:cntrl:]"))
			return CCL.merge (ccl, CCL.CNTRL);
		if (ifMatch (inputChars, pos, "[:xdigit:]"))
			return CCL.merge (ccl, CCL.XDIGIT);
		if (ifMatch (inputChars, pos, "[:space:]"))
			return CCL.merge (ccl, CCL.SPACE);
		return null;
	}

	private boolean ifMatch (char[] inputChars, int[] pos, String str)
	{
		if (inputChars.length - pos[0] < str.length ())
			return false;
		int len = str.length ();
		int offset = pos[0];
		for (int i = 0; i < len; ++i)
			if (inputChars[offset++] != str.charAt (i))
				return false;
		pos[0] = offset;
		return true;
	}
}
