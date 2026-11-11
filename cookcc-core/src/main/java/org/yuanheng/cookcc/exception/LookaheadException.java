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
package org.yuanheng.cookcc.exception;

import java.text.MessageFormat;

import org.yuanheng.cookcc.lexer.CCL;

/**
 * @author Heng Yuan
 */
public class LookaheadException extends ParserException
{
	private static final long serialVersionUID = -3965848588475205612L;

	public static MessageFormat ERROR_MSG = new MessageFormat ("expected token {0}, but found {1} for input {2}");

	private final int m_expected;
	private final CCL m_ccl;
	private final boolean[] m_charSet;
	private final int m_lookahead;

	public LookaheadException (int lineNumber, CCL ccl, boolean[] charSet, String input, int pos)
	{
		super (lineNumber, ERROR_MSG.format (new Object[]{ccl.toString (charSet), pos >= input.length () ? "<<EOF>>" : "" + input.charAt (pos), input}));
		m_expected = -1;
		m_ccl = ccl;
		m_charSet = charSet;
		m_lookahead = pos >= input.length () ? -1 : input.charAt (pos);
	}

	public LookaheadException (int lineNumber, CCL ccl, int expected, String input, int pos)
	{
		super (lineNumber, ERROR_MSG.format (new Object[]{expected < 0 ? "<<EOF>>" : (ccl.PRINT[expected] ? "'" + (char)expected + "'" : new Integer (expected)), pos >= input.length () ? "<<EOF>>" : "" + input.charAt (pos), input}));
		m_expected = expected;
		m_ccl = ccl;
		m_charSet = null;
		m_lookahead = pos >= input.length () ? -1 : input.charAt (pos);
	}

	public int getExpected ()
	{
		return m_expected;
	}

	public int getLookahead ()
	{
		return m_lookahead;
	}

	public CCL getCCL ()
	{
		return m_ccl;
	}

	public boolean[] getCharacterSet ()
	{
		return m_charSet;
	}
}
