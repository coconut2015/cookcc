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
package org.yuanheng.cookcc.input.javaap;

import java.io.IOException;
import java.io.InputStream;


/**
 * @author Heng Yuan
 * @version $Id$
 */
abstract class FileHeaderLexer
{

	protected final static int INITIAL = 0;
	protected final static int BLOCKCOMMENT = 17;

	// an internal class for lazy initiation
	private final static class cc_lexer
	{
		private static char[] accept = ("\000\000\006\001\002\006\007\005\001\002\005\001\004\000\005\001\003\000\000\015\014\015\016\016\017\014\012\014\000\013\014\012").toCharArray ();
		private static char[] ecs = ("\000\000\000\000\000\000\000\000\000\001\002\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\000\000\000\000\000\000\000\000\000\003\000\000\000\000\004\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\005").toCharArray ();
		private static char[][] next = {("\002\003\004\002\005\006").toCharArray (),("\007\010\011\007\012\006").toCharArray (),("\000\000\000\000\000\000").toCharArray (),("\000\013\000\000\000\000").toCharArray (),("\000\000\004\000\000\000").toCharArray (),("\000\000\000\014\015\000").toCharArray (),("\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000").toCharArray (),("\016\017\016\016\016\000").toCharArray (),("\000\000\004\000\000\000").toCharArray (),("\000\000\000\014\015\000").toCharArray (),("\000\013\000\000\000\000").toCharArray (),("\000\000\000\014\000\000").toCharArray (),("\015\015\020\015\015\000").toCharArray (),("\000\000\000\000\000\000").toCharArray (),("\016\017\016\016\016\000").toCharArray (),("\000\000\000\000\000\000").toCharArray (),("\023\024\025\026\027\030").toCharArray (),("\023\031\025\032\027\030").toCharArray (),("\025\000\025\000\000\000").toCharArray (),("\000\033\000\034\000\000").toCharArray (),("\025\000\025\000\000\000").toCharArray (),("\000\000\000\034\035\000").toCharArray (),("\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000").toCharArray (),("\000\036\000\037\000\000").toCharArray (),("\000\000\000\034\035\000").toCharArray (),("\000\033\000\034\000\000").toCharArray (),("\000\000\000\034\035\000").toCharArray (),("\000\000\000\000\000\000").toCharArray (),("\000\036\000\037\000\000").toCharArray (),("\000\000\000\034\035\000").toCharArray ()};
	}


	private InputStream _yyIs = System.in;
	private byte[] _yyBuffer;
	private int _yyBufferSize = 4096;
	private int _yyMatchStart;
	private int _yyBufferEnd;

	private int _yyBaseState;

	private int _yyTextStart;
	private int _yyLength;

	// we need to track beginning of line (BOL) status
	private boolean _yyIsNextBOL = true;
	private boolean _yyBOL = true;

	public void setInput (InputStream is)
	{
		_yyIs = is;
	}

	public InputStream getInput ()
	{
		return _yyIs;
	}

	/**
	 * Check whether or not the current token at the beginning of the line.  This
	 * function is not accurate if the user does multi-line pattern matching or
	 * have trail contexts at the end of the line.
	 *
	 * @return	whether or not the current token is at the beginning of the line.
	 */
	public boolean isBOL ()
	{
		return _yyBOL;
	}

	/**
	 * Get the current token text.
	 * <p>
	 * Avoid calling this function unless it is absolutely necessary since it creates
	 * a copy of the token string.  The string length can be found by reading _yyLength
	 * or calling yyLength () function.
	 *
	 * @return	the current text token.
	 */
	public String yyText ()
	{
		if (_yyMatchStart == _yyTextStart)		// this is the case when we have EOF
			return null;
		return new String (_yyBuffer, _yyTextStart, _yyMatchStart - _yyTextStart);
	}

	/**
	 * Get the current text token's length.  Actions specified in the CookCC file
	 * can directly access the variable _yyLength.
	 *
	 * @return	the string token length
	 */
	public int yyLength ()
	{
		return _yyLength;
	}

	/**
	 * Print the current string token to the standard output.
	 */
	public void echo ()
	{
		System.out.print (yyText ());
	}

	/**
	 * Put all but n characters back to the input stream.  Be aware that calling
	 * yyLess (0) is allowed, but be sure to change the state some how to avoid
	 * an endless loop.
	 *
	 * @param	n
	 * 			The number of characters.
	 */
	protected void yyLess (int n)
	{
		if (n < 0)
			throw new IllegalArgumentException ("yyLess function requires a non-zero value.");
		if (n > (_yyMatchStart - _yyTextStart))
			throw new IndexOutOfBoundsException ("yyLess function called with a too large index value " + n + ".");
		_yyMatchStart = _yyTextStart + n;
	}

	/**
	 * Set the lexer's current state.
	 *
	 * @param	baseState
	 *			the base state index
	 */
	protected void begin (int baseState)
	{
		_yyBaseState = baseState;
	}


	// read more data from the input
	protected boolean yyRefreshBuffer () throws IOException
	{
		if (_yyBuffer == null)
			_yyBuffer = new byte[_yyBufferSize];
		if (_yyMatchStart > 0)
		{
			if (_yyBufferEnd > _yyMatchStart)
			{
				System.arraycopy (_yyBuffer, _yyMatchStart, _yyBuffer, 0, _yyBufferEnd - _yyMatchStart);
				_yyBufferEnd -= _yyMatchStart;
				_yyMatchStart = 0;
			}
			else
			{
				_yyMatchStart = 0;
				_yyBufferEnd = 0;
			}
		}
		int readSize = _yyIs.read (_yyBuffer, _yyBufferEnd, _yyBufferSize - _yyBufferEnd);
		if (readSize > 0)
			_yyBufferEnd += readSize;
		return readSize >= 0;
	}

	/**
	 * Reset the internal buffer.
	 */
	public void yyResetBuffer ()
	{
		_yyMatchStart = 0;
		_yyBufferEnd = 0;
	}

	/**
	 * Set the internal buffer size.  This action can only be performed
	 * when the buffer is empty.  Having a large buffer is useful to read
	 * a whole file in to increase the performance sometimes.
	 *
	 * @param	bufferSize
	 *			the new buffer size.
	 */
	public void setBufferSize (int bufferSize)
	{
		if (_yyBufferEnd > _yyMatchStart)
			throw new IllegalArgumentException ("Cannot change lexer buffer size at this moment.");
		_yyBufferSize = bufferSize;
		_yyMatchStart = 0;
		_yyBufferEnd = 0;
		if (_yyBuffer != null && bufferSize != _yyBuffer.length)
			_yyBuffer = new byte[bufferSize];
	}

	/**
	 * Call this function to start the scanning of the input.
	 *
	 * @return	a token or status value.
	 * @throws	IOException
	 *			in case of I/O error.
	 */
	public int yyLex () throws IOException
	{

		char[] cc_ecs = cc_lexer.ecs;
		char[][] cc_next = cc_lexer.next;
		char[] cc_accept = cc_lexer.accept;

		byte[] buffer = _yyBuffer;

		while (true)
		{
			// initiate variables necessary for lookup
			_yyBOL = _yyIsNextBOL;
			_yyIsNextBOL = false;
			int cc_matchedState = _yyBaseState + (_yyBOL ? 1 : 0);

			int matchedLength = 0;

			int internalBufferEnd = _yyBufferEnd;
			int lookahead = _yyMatchStart;

			int cc_backupMatchedState = cc_matchedState;
			int cc_backupMatchedLength = 0;

			// the DFA lookup
			while (true)
			{
				// check buffer status
				if (lookahead < internalBufferEnd)
				{
					// now okay to process the character
					int cc_toState;
					cc_toState = cc_next[cc_matchedState][cc_ecs[buffer[lookahead] & 0xff]];

					if (cc_toState == 0)
					{
						cc_matchedState = cc_backupMatchedState;
						matchedLength = cc_backupMatchedLength;
						break;
					}

					cc_matchedState = cc_toState;
					++lookahead;
					++matchedLength;

					if (cc_accept[cc_matchedState] > 0)
					{
						cc_backupMatchedState = cc_toState;
						cc_backupMatchedLength = matchedLength;
					}
				}
				else
				{
					int lookPos = lookahead - _yyMatchStart;
					boolean refresh = yyRefreshBuffer ();
					buffer = _yyBuffer;
					internalBufferEnd = _yyBufferEnd;
					lookahead = _yyMatchStart + lookPos;
					if (! refresh)
					{
						// <<EOF>>
						int cc_toState;
						cc_toState = cc_next[cc_matchedState][cc_ecs[256]];
						if (cc_toState != 0)
							cc_matchedState = cc_toState;
						else
						{
							cc_matchedState = cc_backupMatchedState;
							matchedLength = cc_backupMatchedLength;
						}
						break;
					}
				}
			}

			_yyTextStart = _yyMatchStart;
			_yyMatchStart += matchedLength;
			_yyLength = matchedLength;


			switch (cc_accept[cc_matchedState])
			{
				case 1:	// {ws}+
				{
					m_this.ignoreWhiteSpace ();
				}
				case 17: break;
				case 2:	// \n+
				{
					m_this.ignoreWhiteSpace ();
				}
				case 18: break;
				case 3:	// [/][/].*\n
				{
					m_this.scanLineComment ();
				}
				case 19: break;
				case 4:	// [/][*]+
				{
					m_this.startBlockComment ();
				}
				case 20: break;
				case 10:	// ^{ws}*[*]
				{
					m_this.ignoreLeadingStar ();
				}
				case 26: break;
				case 11:	// {ws}*[*]+[/]
				{
					m_this.endBlockComment ();
				}
				case 27: break;
				case 12:	// {ws}+
				{
					m_this.scanBlockComment ();
				}
				case 28: break;
				case 13:	// [^ \t*/]+
				{
					m_this.scanBlockComment ();
				}
				case 29: break;
				case 14:	// .
				{
					m_this.scanBlockComment ();
				}
				case 30: break;
				case 5:	// ^{ws}*[^ \t]
				{
					return m_this.doneScanning ();
				}
				case 21: break;
				case 6:	// .
				{
					return m_this.doneScanning ();
				}
				case 22: break;
				case 7:	// <<EOF>>
				{
					return m_this.doneScanning ();
				}
				case 23: break;
				case 15:	// <<EOF>>
				{
					return m_this.doneScanning ();
				}
				case 31: break;
				case 8:	// .|\n
				{
					echo ();			// default character action
				}
				case 24: break;
				case 9:	// <<EOF>>
				{
					return 0;			// default EOF action
				}
				case 25: break;
				default:
					throw new IOException ("Internal error in FileHeaderLexer lexer.");
			}

			// check BOL here since '\n' may be unput back into the stream buffer

			// specifically used _yyBuffer since it could be changed by user
			if (_yyMatchStart > 0 && _yyBuffer[_yyMatchStart - 1] == '\n')
				_yyIsNextBOL = true;
		}
	}




	private final org.yuanheng.cookcc.input.javaap.FileHeaderScanner m_this = (org.yuanheng.cookcc.input.javaap.FileHeaderScanner)this;

	/**
	 * This function is used to change the initial state for the lexer.
	 *
	 * @param	state
	 *			the name of the state
	 */
	protected void begin (String state)
	{
		if ("INITIAL".equals (state))
		{
			begin (INITIAL);
			return;
		}
		if ("BLOCKCOMMENT".equals (state))
		{
			begin (BLOCKCOMMENT);
			return;
		}
		throw new IllegalArgumentException ("Unknown lexer state: " + state);
	}


/*
 * lexer properties:
 * unicode = false
 * bol = true
 * backup = true
 * cases = 15
 * table = ecs
 * ecs = 6
 * states = 32
 * max symbol value = 256
 *
 * memory usage:
 * full table = 8224
 * ecs table = 449
 *
 */
}
