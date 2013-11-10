/*
 * Copyright (c) 2008-2013, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *    Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    Neither the name of the Heng Yuan nor the
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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.yuanheng.cookcc.CookCCOption;
import org.yuanheng.cookcc.Lex;
import org.yuanheng.cookcc.Lexs;
import org.yuanheng.cookcc.exception.CCLException;
import org.yuanheng.cookcc.exception.EscapeSequenceException;
import org.yuanheng.cookcc.exception.InvalidPOSIXCharacterClassException;

@CookCCOption
class CCLParser extends CCLScanner
{
	private final CCL m_ccl;

	private int m_lineNumber;
	private String m_input;

	private boolean m_notCCL;
	private boolean[] m_cclSet;
	private int m_rangeStart;
	private boolean m_hasRange;

	public CCLParser (CCL ccl)
	{
		m_ccl = ccl;
	}

	public boolean[] parse (String input, int lineNumber) throws IOException
	{
		m_lineNumber = lineNumber;
		m_input = input;

		m_cclSet = m_ccl.EMPTY.clone ();
		setInput (new ByteArrayInputStream (input.getBytes ()));
		if (yyLex () != 0)
		{
			throw new CCLException (m_input, null);
		}
		boolean[] cclSet = m_cclSet;
		if (m_notCCL)
		{
			cclSet = CCL.negate (cclSet);
		}
		reset ();
		return cclSet;
	}

	public void reset ()
	{
		super.reset ();
		m_notCCL = false;
		m_cclSet = null;
		m_rangeStart = 0;
		m_hasRange = false;
	}

	private void updateToChar (int ch)
	{
		if (m_hasRange)
		{
			for (int i = m_rangeStart; i <= ch; ++i)
			{
				m_cclSet[i] = true;
			}
			m_hasRange = false;
		}
		else
		{
			m_cclSet[ch] = true;
		}
		m_rangeStart = ch;
	}

	@Lex (pattern = "^'['")
	void scanCCLStart ()
	{
		m_notCCL = false;
	}

	@Lex (pattern = "^'[^'")
	void scanNotCCLStart ()
	{
		m_notCCL = true;
	}

	@Lex (pattern = "']'")
	int scanCCLEnd ()
	{
		return 0;
	}

	@Lex (pattern = "'\\\\'([0-9]{1,3})")
	void scanOct ()
	{
		int ch = Integer.parseInt (yyText ().substring (1), 8);
		updateToChar (ch);
	}

	@Lex (pattern = "'\\\\x'[a-fA-F0-9]{1,2}")
	void scanHex ()
	{
		int ch = Integer.parseInt (yyText ().substring (2), 16);
		updateToChar (ch);
	}

	@Lex (pattern = "'\\\\u'[a-fA-F0-9]{4}")
	void scanUnicode ()
	{
		int ch = Integer.parseInt (yyText ().substring (2), 16);
		updateToChar (ch);
	}

	// Invalid cases for escape characters
	@Lexs (patterns = {
		@Lex (pattern = "'\\\\x'"),
		@Lex (pattern = "'\\\\u'")
	})
	void scanEscapeError ()
	{
		throw new EscapeSequenceException (yyText ());
	}

	@Lex (pattern = "'\\\\'.")
	void scanEscape ()
	{
		char ch = CCL.esc (yyText (), new int[1]);
		updateToChar (ch);
	}

	// This is the case where escape character is the last character in the pattern
	@Lex (pattern = "'\\\\'")
	void scanEscapeError2 ()
	{
		throw new EscapeSequenceException (yyText ());
	}

	@Lex (pattern = "'[:'('^'?)([a-zA-Z]+)':]'")
	void scanPosixCCL ()
	{
		boolean[] posixCCL = m_ccl.getPosixCCL (yyText ());
		if (posixCCL == null)
		{
			throw new InvalidPOSIXCharacterClassException (m_lineNumber, yyText (), m_input);
		}
		CCL.merge (m_cclSet, posixCCL);
	}

	@Lex (pattern = "'-'")
	void scanRange ()
	{
		m_hasRange = true;
	}

	@Lex (pattern = ".|\\n")
	void scanChar ()
	{
		updateToChar (yyText ().charAt (0));
	}

	@Lex (pattern = "<<EOF>>")
	int scanEOF ()
	{
		return -1;
	}
}
