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

import java.io.IOException;
import java.io.InputStream;

import java.util.Stack;

abstract class CCLScanner
{

	protected final static int INITIAL = 0;

	// an internal class for lazy initiation
	private final static class cc_lexer
	{
		private static char[] accept = ("\000\000\015\014\015\012\003\016\001\000\011\004\010\007\002\000\000\004\000\005\000\004\000\005\013\000\006").toCharArray ();
		private static char[] ecs = ("\000\000\000\000\000\000\000\000\000\000\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\002\000\000\003\003\003\003\003\003\003\003\003\003\004\000\000\000\000\000\000\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\010\011\012\000\000\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\006\006\006\006\013\006\006\014\006\006\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\015").toCharArray ();
		private static char[] base = ("\000\012\000\000\001\000\000\000\030\011\000\003\015\023\000\000\031\041\024\027\034\000\030\000\000\033\000\000\037\046").toCharArray ();
		private static char[] next = ("\002\002\003\013\024\011\021\004\005\006\000\014\015\007\017\017\022\010\022\020\017\017\023\026\023\026\027\031\027\031\032\012\032\012\016\000\025\030\000\000\000\000\000\000\000\000\000").toCharArray ();
		private static char[] check = ("\033\033\000\005\017\004\013\000\000\000\017\005\005\000\011\011\014\001\014\011\011\011\015\022\015\022\023\026\023\026\031\034\031\034\010\020\021\024\035\035\035\035\035\035\035\035\035").toCharArray ();
		private static char[] defaults = ("\033\000\035\035\002\034\035\035\004\002\035\002\002\002\035\011\011\002\002\002\002\035\002\035\035\002\035\035\035\035").toCharArray ();
		private static char[] meta = ("\000\001\002\001\000\000\000\002\002\002\000\001\001\003").toCharArray ();
	}


	private InputStream _yyIs = System.in;
	private byte[] _yyBuffer;
	private int _yyBufferSize = 4096;
	private int _yyMatchStart;
	private int _yyBufferEnd;

	private int _yyBaseState;

	private int _yyTextStart;
	private int _yyLength;

	private Stack<Integer> _yyLexerStack;
	private Stack<Object[]> _yyInputStack;

	// we need to track beginning of line (BOL) status
	private boolean _yyIsNextBOL = true;
	private boolean _yyBOL = true;

	/**
	 * Set the current input.
	 *
	 * @param    is the new input.
	 */
	public void setInput (InputStream is)
	{
		_yyIs = is;
	}

	/**
	 * Obtain the current input.
	 *
	 * @return the current input
	 */
	public InputStream getInput ()
	{
		return _yyIs;
	}

	/**
	 * Switch the current input to the new input.  The old input and already
	 * buffered characters are pushed onto the stack.
	 *
	 * @param    is the new input
	 */
	public void yyPushInput (InputStream is)
	{
		int len = _yyBufferEnd - _yyMatchStart;
		byte[] leftOver = new byte[len];
		System.arraycopy (_yyBuffer, _yyMatchStart, leftOver, 0, len);

		Object[] states = new Object[4];
		states[0] = _yyIs;
		states[1] = leftOver;

		if (_yyInputStack == null)
			_yyInputStack = new Stack<Object[]> ();
		_yyInputStack.push (states);

		_yyIs = is;
		_yyMatchStart = 0;
		_yyBufferEnd = 0;
	}

	/**
	 * Switch the current input to the old input on stack.  The currently
	 * buffered characters are inserted infront of the old buffered characters.
	 */
	public void yyPopInput ()
	{
		Object[] states = _yyInputStack.pop ();
		_yyIs = (InputStream)states[0];
		byte[] leftOver = (byte[])states[1];

		int curLen = _yyBufferEnd - _yyMatchStart;

		if ((leftOver.length + curLen) > _yyBuffer.length)
		{
			byte[] newBuffer = new byte[leftOver.length + curLen];
			System.arraycopy (_yyBuffer, _yyMatchStart, newBuffer, 0, curLen);
			System.arraycopy (leftOver, 0, newBuffer, curLen, leftOver.length);
			_yyBuffer = newBuffer;
			_yyMatchStart = 0;
			_yyBufferEnd = leftOver.length + curLen;
		}
		else
		{
			int start = _yyMatchStart;
			int end = _yyBufferEnd;
			byte[] buffer = _yyBuffer;

			for (int i = 0; start < end; ++i, ++start)
				buffer[i] = buffer[start];
			System.arraycopy (leftOver, 0, buffer, curLen, leftOver.length);
			_yyMatchStart = 0;
			_yyBufferEnd = leftOver.length + curLen;
		}
	}

	/**
	 * Obtain the number of input objects on the stack.
	 *
	 * @return the number of input objects on the stack.
	 */
	public int yyInputStackSize ()
	{
		return _yyInputStack == null ? 0 : _yyInputStack.size ();
	}

	/**
	 * Check whether or not the current token at the beginning of the line.  This
	 * function is not accurate if the user does multi-line pattern matching or
	 * have trail contexts at the end of the line.
	 *
	 * @return whether or not the current token is at the beginning of the line.
	 */
	public boolean isBOL ()
	{
		return _yyBOL;
	}

	/**
	 * Set whether or not the next token at the beginning of the line.
	 *
	 * @param    bol the bol status
	 */
	public void setBOL (boolean bol)
	{
		_yyIsNextBOL = bol;
	}

	/**
	 * Get the current token text.
	 * <p/>
	 * Avoid calling this function unless it is absolutely necessary since it creates
	 * a copy of the token string.  The string length can be found by reading _yyLength
	 * or calling yyLength () function.
	 *
	 * @return the current text token.
	 */
	public String yyText ()
	{
		if (_yyMatchStart == _yyTextStart)        // this is the case when we have EOF
			return null;
		return new String (_yyBuffer, _yyTextStart, _yyMatchStart - _yyTextStart);
	}

	/**
	 * Get the current text token's length.  Actions specified in the CookCC file
	 * can directly access the variable _yyLength.
	 *
	 * @return the string token length
	 */
	public int yyLength ()
	{
		return _yyLength;
	}

	/** Print the current string token to the standard output. */
	public void echo ()
	{
		System.out.print (yyText ());
	}

	/**
	 * Put all but n characters back to the input stream.  Be aware that calling
	 * yyLess (0) is allowed, but be sure to change the state some how to avoid
	 * an endless loop.
	 *
	 * @param    n The number of characters.
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
	 * @param    baseState the base state index
	 */
	protected void begin (int baseState)
	{
		_yyBaseState = baseState;
	}

	/**
	 * Push the current state onto lexer state onto stack and
	 * begin the new state specified by the user.
	 *
	 * @param    newState the new state.
	 */
	protected void yyPushLexerState (int newState)
	{
		if (_yyLexerStack == null)
			_yyLexerStack = new Stack<Integer> ();
		_yyLexerStack.push (new Integer (_yyBaseState));
		begin (newState);
	}

	/** Restore the previous lexer state. */
	protected void yyPopLexerState ()
	{
		begin (_yyLexerStack.pop ());
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
		else if (_yyBufferEnd == _yyBuffer.length)
		{
			byte[] newBuffer = new byte[_yyBuffer.length + _yyBuffer.length / 2];
			System.arraycopy (_yyBuffer, 0, newBuffer, 0, _yyBufferEnd);
			_yyBuffer = newBuffer;
		}

		int readSize = _yyIs.read (_yyBuffer, _yyBufferEnd, _yyBuffer.length - _yyBufferEnd);
		if (readSize > 0)
			_yyBufferEnd += readSize;
		else if (readSize < 0 && !yyWrap ())        // since we are at EOF, call yyWrap ().  If the return value of yyWrap is false, refresh buffer again
			return yyRefreshBuffer ();
		return readSize >= 0;
	}

	/** Reset the internal buffer. */
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
	 * @param    bufferSize the new buffer size.
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
	 * Reset the internal state to reuse the same parser.
	 * <p/>
	 * Note, it does not change the buffer size, the input buffer, and the input stream.
	 * <p/>
	 * Making this function protected so that it can be enabled only if the child class
	 * decides to make it public.
	 */
	protected void reset ()
	{

		// reset lexer state
		_yyMatchStart = 0;
		_yyBufferEnd = 0;
		_yyBaseState = 0;
		_yyTextStart = 0;
		_yyLength = 0;

		if (_yyLexerStack != null)
			_yyLexerStack.clear ();
		if (_yyInputStack != null)
			_yyInputStack.clear ();

		_yyIsNextBOL = true;
		_yyBOL = true;

	}

	/**
	 * Call this function to start the scanning of the input.
	 *
	 * @return a token or status value.
	 * @throws IOException in case of I/O error.
	 */
	public int yyLex () throws IOException
	{

		char[] cc_ecs = cc_lexer.ecs;
		char[] cc_next = cc_lexer.next;
		char[] cc_check = cc_lexer.check;
		char[] cc_base = cc_lexer.base;
		char[] cc_default = cc_lexer.defaults;
		char[] cc_meta = cc_lexer.meta;
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
					int symbol = cc_ecs[buffer[lookahead] & 0xff];
					cc_toState = cc_matchedState;
					while (cc_check[symbol + cc_base[cc_toState]] != cc_toState)
					{
						cc_toState = cc_default[cc_toState];
						if (cc_toState >= 27)
							symbol = cc_meta[symbol];
					}
					cc_toState = cc_next[symbol + cc_base[cc_toState]];

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
					if (!refresh)
					{
						// <<EOF>>
						int cc_toState;
						int symbol = cc_ecs[256];
						cc_toState = cc_matchedState;
						while (cc_check[symbol + cc_base[cc_toState]] != cc_toState)
						{
							cc_toState = cc_default[cc_toState];
							if (cc_toState >= 27)
								symbol = cc_meta[symbol];
						}
						cc_toState = cc_next[symbol + cc_base[cc_toState]];

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
				case 1:    // ^'['
				{
					m_this.scanCCLStart ();
				}
				case 18:
					break;
				case 2:    // ^'[^'
				{
					m_this.scanNotCCLStart ();
				}
				case 19:
					break;
				case 3:    // ']'
				{
					return m_this.scanCCLEnd ();
				}
				case 20:
					break;
				case 4:    // '\\'([0-9]{1,3})
				{
					m_this.scanOct ();
				}
				case 21:
					break;
				case 5:    // '\\x'[a-fA-F0-9]{1,2}
				{
					m_this.scanHex ();
				}
				case 22:
					break;
				case 6:    // '\\u'[a-fA-F0-9]{4}
				{
					m_this.scanUnicode ();
				}
				case 23:
					break;
				case 7:    // '\\x'
				{
					m_this.scanEscapeError ();
				}
				case 24:
					break;
				case 8:    // '\\u'
				{
					m_this.scanEscapeError ();
				}
				case 25:
					break;
				case 9:    // '\\'.
				{
					m_this.scanEscape ();
				}
				case 26:
					break;
				case 10:    // '\\'
				{
					m_this.scanEscapeError2 ();
				}
				case 27:
					break;
				case 11:    // '[:'('^'?)([a-zA-Z]+)':]'
				{
					m_this.scanPosixCCL ();
				}
				case 28:
					break;
				case 12:    // '-'
				{
					m_this.scanRange ();
				}
				case 29:
					break;
				case 13:    // .|\n
				{
					m_this.scanChar ();
				}
				case 30:
					break;
				case 14:    // <<EOF>>
				{
					return m_this.scanEOF ();
				}
				case 31:
					break;
				case 15:    // .|\n
				{
					echo ();            // default character action
				}
				case 32:
					break;
				case 16:    // <<EOF>>
				{
					return 0;            // default EOF action
				}
				case 33:
					break;
				default:
					throw new IOException ("Internal error in CCLScanner lexer.");
			}

			// check BOL here since '\n' may be unput back into the stream buffer

			// specifically used _yyBuffer since it could be changed by user
			if (_yyMatchStart > 0 && _yyBuffer[_yyMatchStart - 1] == '\n')
				_yyIsNextBOL = true;
		}
	}


	private final org.yuanheng.cookcc.lexer.CCLParser m_this = (org.yuanheng.cookcc.lexer.CCLParser)this;

	/**
	 * This function is used to change the initial state for the lexer.
	 *
	 * @param    state the name of the state
	 */
	protected void begin (String state)
	{
		if ("INITIAL".equals (state))
		{
			begin (INITIAL);
			return;
		}
		throw new IllegalArgumentException ("Unknown lexer state: " + state);
	}

	/**
	 * Push the current state onto lexer state onto stack and
	 * begin the new state specified by the user.
	 *
	 * @param    state the new state.
	 */
	protected void yyPushLexerState (String state)
	{
		if ("INITIAL".equals (state))
		{
			yyPushLexerState (INITIAL);
			return;
		}
		throw new IllegalArgumentException ("Unknown lexer state: " + state);
	}

	/**
	 * Check if there are more inputs.  This function is called when EOF is
	 * encountered.
	 *
	 * @return true to indicate no more inputs.
	 * @throws IOException in case of an IO error
	 */
	protected boolean yyWrap () throws IOException
	{
		if (yyInputStackSize () > 0)
		{
			yyPopInput ();
			return false;
		}
		return true;
	}


/*
 * lexer properties:
 * unicode = false
 * bol = true
 * backup = true
 * cases = 16
 * table = compressed
 * ecs = 14
 * states = 27
 * max symbol value = 256
 *
 * memory usage:
 * full table = 6939
 * ecs table = 635
 * next = 47
 * check = 47
 * default = 30
 * meta = 14
 * compressed table = 395
 *
 */
}
