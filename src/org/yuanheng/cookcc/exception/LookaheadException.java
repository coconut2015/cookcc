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
package org.yuanheng.cookcc.exception;

import java.text.MessageFormat;

import org.yuanheng.cookcc.lexer.CCL;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class LookaheadException extends ParserException
{
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
