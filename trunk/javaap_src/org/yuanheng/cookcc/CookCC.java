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
package org.yuanheng.cookcc;

/**
 * This class is merely a place holder for functions that would be generated.  It does
 * not contain any actual source code, other than the JavaDoc.
 *
 * @author Heng Yuan
 * @version $Id$
 */
public abstract class CookCC
{
	protected boolean isBOL ()
	{
		return false;
	}

	public void setBOL (boolean bol)
	{
	}

	/**
	 * Print the current string token to the standard output.
	 */
	public void echo ()
	{
	}

	public boolean debugLexer (int matchedState, int accept)
	{
		System.err.println ("lexer: " + matchedState + ", " + accept + ", " + yyText ());
		return true;
	}

	public boolean debugLexerBackup (int backupState, String backupString)
	{
		System.err.println ("lexer backup: " + backupState + ", " + backupString);
		return true;
	}

	public String yyText ()
	{
		return null;
	}

	public int yyLex ()
	{
		return 0;
	}

	public int yyLength ()
	{
		return 0;
	}

	/**
	 * This function can only be called before lexing or after lexing has ended.
	 *
	 * @param	bufferSize the new buffer size.
	 */
	public void setBufferSize (int bufferSize)
	{
	}

	/**
	 * This function is used to change the initial state for the lexer.
	 *
	 * @param	state
	 *			the name of the state
	 */
	public void begin (String state)
	{
	}
}
