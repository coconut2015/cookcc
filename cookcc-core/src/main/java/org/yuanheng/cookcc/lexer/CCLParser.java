/*
 * Copyright (c) 2008-2013, Heng Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

	private long m_lineNumber;
	private String m_input;

	private boolean m_notCCL;
	private boolean[] m_cclSet;
	private int m_rangeStart;
	private boolean m_hasRange;

	public CCLParser (CCL ccl)
	{
		m_ccl = ccl;
	}

	public boolean[] parse (String input, long lineNumber) throws IOException
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

	@Override
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
