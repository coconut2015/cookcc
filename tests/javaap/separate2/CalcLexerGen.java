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

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

import java.util.Stack;

/**
 * @author Heng Yuan
 * @version $Id$
 */
abstract class CalcLexerGen
{

	////////////////////////////////////////////////////////////////////////
	//
	// Terminal Definitions
	//
	////////////////////////////////////////////////////////////////////////
	protected final static int VARIABLE = 256;
	protected final static int INTEGER = 257;
	protected final static int WHILE = 258;
	protected final static int IF = 259;
	protected final static int PRINT = 260;
	protected final static int ASSIGN = 261;
	protected final static int SEMICOLON = 262;
	protected final static int IFX = 263;
	protected final static int ELSE = 264;
	protected final static int GE = 265;
	protected final static int LE = 266;
	protected final static int EQ = 267;
	protected final static int NE = 268;
	protected final static int LT = 269;
	protected final static int GT = 270;
	protected final static int ADD = 271;
	protected final static int SUB = 272;
	protected final static int MUL = 273;
	protected final static int DIV = 274;
	protected final static int UMINUS = 275;

	////////////////////////////////////////////////////////////////////////
	//
	// Lexer States
	//
	////////////////////////////////////////////////////////////////////////
	protected final static int INITIAL = 0;

	// an internal class for lazy initiation
	private final static class cc_lexer
	{
		private final static char[] accept = ("\000\025\024\025\016\006\004\005\007\001\002\010\003\011\023\023\023\023\023\026\014\013\015\012\023\020\023\023\023\023\023\021\023\023\022\017").toCharArray ();
		private final static char[] ecs = ("\000\000\000\000\000\000\000\000\000\001\002\000\000\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\003\000\000\000\000\000\000\004\004\005\006\000\007\004\010\011\011\011\011\011\011\011\011\011\011\000\012\013\014\015\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\016\016\016\016\017\020\016\021\022\016\016\023\016\024\016\025\016\026\027\030\016\016\031\016\016\016\004\000\004\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\032").toCharArray ();
		private final static char[] base = ("\000\000\032\021\000\000\000\000\000\025\000\023\024\025\024\033\037\032\040\000\000\000\000\000\033\000\041\042\046\042\044\000\040\052\000\000\072").toCharArray ();
		private final static char[] next = ("\001\002\002\003\004\005\006\007\010\011\012\013\014\015\016\017\016\016\020\016\016\021\016\016\016\022\023\002\002\024\011\025\026\027\016\016\016\016\016\016\016\016\016\016\016\016\030\031\032\033\034\035\036\037\040\041\042\043\000\000\000\000\000\000\000\000\000\000\000").toCharArray ();
		private final static char[] check = ("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\002\002\003\011\013\014\015\016\016\016\016\016\016\016\016\016\016\016\016\017\020\021\022\030\032\033\034\035\036\040\041\044\044\044\044\044\044\044\044\044\044\044").toCharArray ();
		private final static char[] defaults = ("\044\044\001\001\044\044\044\044\044\001\044\001\001\001\001\016\016\016\016\044\044\044\044\044\016\016\016\016\016\016\016\016\016\016\016\016\044").toCharArray ();
	}

	// for passing value from lexer to parser
	private Object _yyValue;

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


	/**
	 * Return the object associate with the token.  This function is only generated
	 * when the parser is not specified.
	 *
	 * @return	the object assoicated with the token.
	 */
	public Object yyValue ()
	{
		return _yyValue;
	}
	/**
	 * Set the current input.
	 *
	 * @param	is
	 *			the new input.
	 */
	public void setInput (InputStream is)
	{
		_yyIs = is;
	}

	/**
	 * Obtain the current input.
	 *
	 * @return	the current input
	 */
	public InputStream getInput ()
	{
		return _yyIs;
	}

	/**
	 * Switch the current input to the new input.  The old input and already
	 * buffered characters are pushed onto the stack.
	 *
	 * @param	is
	 * 			the new input
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
	 * @return	the number of input objects on the stack.
	 */
	public int yyInputStackSize ()
	{
		return _yyInputStack == null ? 0 : _yyInputStack.size ();
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

	/**
	 * Push the current state onto lexer state onto stack and
	 * begin the new state specified by the user.
	 *
	 * @param	newState
	 *			the new state.
	 */
	protected void yyPushLexerState (int newState)
	{
		if (_yyLexerStack == null)
			_yyLexerStack = new Stack<Integer> ();
		_yyLexerStack.push (new Integer (_yyBaseState));
		begin (newState);
	}

	/**
	 * Restore the previous lexer state.
	 */
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
		else if (readSize < 0 && !yyWrap ())		// since we are at EOF, call yyWrap ().  If the return value of yyWrap is false, refresh buffer again
			return yyRefreshBuffer ();
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
	 * Reset the internal state to reuse the same parser.
	 * <p>
	 * Note, it does not change the buffer size, the input buffer, and the input stream.
	 * <p>
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
		char[] cc_next = cc_lexer.next;
		char[] cc_check = cc_lexer.check;
		char[] cc_base = cc_lexer.base;
		char[] cc_default = cc_lexer.defaults;
		char[] cc_accept = cc_lexer.accept;

		byte[] buffer = _yyBuffer;

		while (true)
		{
			// initiate variables necessary for lookup
			int cc_matchedState = _yyBaseState;

			int matchedLength = 0;

			int internalBufferEnd = _yyBufferEnd;
			int lookahead = _yyMatchStart;


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
						if (cc_toState >= 36)
							symbol = 0;
					}
					cc_toState = cc_next[symbol + cc_base[cc_toState]];

					if (cc_toState == 0)
						break;

					cc_matchedState = cc_toState;
					++lookahead;
					++matchedLength;

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
						int symbol = cc_ecs[256];
						cc_toState = cc_matchedState;
						while (cc_check[symbol + cc_base[cc_toState]] != cc_toState)
						{
							cc_toState = cc_default[cc_toState];
							if (cc_toState >= 36)
								symbol = 0;
						}
						cc_toState = cc_next[symbol + cc_base[cc_toState]];

						if (cc_toState != 0)
							cc_matchedState = cc_toState;
						break;
					}
				}
			}

			_yyTextStart = _yyMatchStart;
			_yyMatchStart += matchedLength;
			_yyLength = matchedLength;


			switch (cc_accept[cc_matchedState])
			{
				case 1:	// [0-9]+
				{
					_yyValue = m_this.parseInt (); return INTEGER;
				}
				case 26: break;
				case 2:	// [;]
				{
					_yyValue = m_this.parseOp (); return SEMICOLON;
				}
				case 27: break;
				case 3:	// [=]
				{
					_yyValue = m_this.parseOp (); return ASSIGN;
				}
				case 28: break;
				case 4:	// [+]
				{
					_yyValue = m_this.parseOp (); return ADD;
				}
				case 29: break;
				case 5:	// \-
				{
					_yyValue = m_this.parseOp (); return SUB;
				}
				case 30: break;
				case 6:	// [*]
				{
					_yyValue = m_this.parseOp (); return MUL;
				}
				case 31: break;
				case 7:	// [/]
				{
					_yyValue = m_this.parseOp (); return DIV;
				}
				case 32: break;
				case 8:	// [<]
				{
					_yyValue = m_this.parseOp (); return LT;
				}
				case 33: break;
				case 9:	// [>]
				{
					_yyValue = m_this.parseOp (); return GT;
				}
				case 34: break;
				case 10:	// >=
				{
					_yyValue = m_this.parseOp (); return GE;
				}
				case 35: break;
				case 11:	// <=
				{
					_yyValue = m_this.parseOp (); return LE;
				}
				case 36: break;
				case 12:	// !=
				{
					_yyValue = m_this.parseOp (); return NE;
				}
				case 37: break;
				case 13:	// ==
				{
					_yyValue = m_this.parseOp (); return EQ;
				}
				case 38: break;
				case 14:	// [(){}.]
				{
					return m_this.parseSymbol ();
				}
				case 39: break;
				case 15:	// while
				{
					_yyValue = m_this.parseKeyword (); return WHILE;
				}
				case 40: break;
				case 16:	// if
				{
					_yyValue = m_this.parseKeyword (); return IF;
				}
				case 41: break;
				case 17:	// else
				{
					_yyValue = m_this.parseKeyword (); return ELSE;
				}
				case 42: break;
				case 18:	// print
				{
					_yyValue = m_this.parseKeyword (); return PRINT;
				}
				case 43: break;
				case 19:	// [a-z]+
				{
					_yyValue = m_this.parseVariable (); return VARIABLE;
				}
				case 44: break;
				case 20:	// [ \t\r\n]+
				{
					m_this.ignoreWhiteSpace ();
				}
				case 45: break;
				case 21:	// .
				{
					m_this.invalidCharacter ();
				}
				case 46: break;
				case 22:	// <<EOF>>
				{
					m_this.parseEOF (); return 0;  // token = $
				}
				case 47: break;
				case 23:	// .|\n
				{
					echo ();			// default character action
				}
				case 48: break;
				case 24:	// <<EOF>>
				{
					return 0;			// default EOF action
				}
				case 49: break;
				default:
					throw new IOException ("Internal error in CalcLexerGen lexer.");
			}

		}
	}


	private final CalcLexer m_this = (CalcLexer)this;

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
		throw new IllegalArgumentException ("Unknown lexer state: " + state);
	}

	/**
	 * Push the current state onto lexer state onto stack and
	 * begin the new state specified by the user.
	 *
	 * @param	state
	 *			the new state.
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
	 * @return	true to indicate no more inputs.
	 * @throws	IOException
	 * 			in case of an IO error
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


	protected static InputStream open (String file) throws IOException
	{
		return new FileInputStream (file);
	}


/*
 * lexer properties:
 * unicode = false
 * bol = false
 * backup = false
 * cases = 24
 * table = compressed
 * ecs = 27
 * states = 36
 * max symbol value = 256
 *
 * memory usage:
 * full table = 9252
 * ecs table = 1229
 * next = 69
 * check = 69
 * default = 37
 * compressed table = 432
 *
 */
}
