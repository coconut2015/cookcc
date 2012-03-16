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
package org.yuanheng.cookcc.util;

import java.io.IOException;
import java.io.InputStream;

import java.util.Stack;


import java.io.ByteArrayInputStream;
import java.util.LinkedList;

/**
 * This class is used to parse tokens inside &lt;tokens> tag or similar tags that
 * list a series of symbols.  Things get complicated with single quoted characters
 * and such.
 *
 * @author Heng Yuan
 * @version $Id$
 */
public class TokenParser
{

	protected final static int INITIAL = 0;

	// an internal class for lazy initiation
	private final static class cc_lexer
	{
		private static char[] accept = ("\000\007\006\007\002\001\007\010\000\004\000\003\004\005\004\005").toCharArray ();
		private static char[] ecs = ("\000\000\000\000\000\000\000\000\000\001\002\000\000\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\000\000\000\000\000\000\003\000\000\000\000\001\000\000\000\004\004\004\004\004\004\004\004\004\004\000\000\000\000\000\000\000\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\000\007\000\000\006\000\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\010\006\006\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\011").toCharArray ();
		private static char[][] next = {("\001\002\002\003\004\005\005\006\005\007").toCharArray (),("\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\002\002\000\000\000\000\000\000\000").toCharArray (),("\010\010\010\000\010\010\010\000\010\000").toCharArray (),("\000\000\000\000\004\000\000\000\000\000").toCharArray (),("\000\000\000\000\005\005\005\000\005\000").toCharArray (),("\000\000\000\000\011\000\000\000\012\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\013\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\014\000\000\000\000\000").toCharArray (),("\000\000\000\000\015\015\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\016\000\000\000\000\000").toCharArray (),("\000\000\000\000\017\017\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000").toCharArray ()};
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
	 *
	 * Note, it does not change the buffer size, the input buffer, and the input stream.
	 *
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
		char[][] cc_next = cc_lexer.next;
		char[] cc_accept = cc_lexer.accept;

		byte[] buffer = _yyBuffer;

		while (true)
		{
			// initiate variables necessary for lookup
			int cc_matchedState = _yyBaseState;

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
				case 1:	// [a-zA-Z_][a-zA-Z_0-9]*
				{
					m_tokenList.add (yyText ());
				}
				case 12: break;
				case 2:	// [0-9]+
				{
					m_tokenList.add (yyText ());
				}
				case 13: break;
				case 3:	// \'[^\\']\'
				{
					m_tokenList.add (yyText ());
				}
				case 14: break;
				case 4:	// \\[0-9]{1,3}
				{
					m_tokenList.add (yyText ());
				}
				case 15: break;
				case 5:	// \\x[[:xdigit:]]{1,2}
				{
					m_tokenList.add (yyText ());
				}
				case 16: break;
				case 6:	// [, \r\t\n]+
				{
					
				}
				case 17: break;
				case 7:	// .
				{
					throw new IOException ("Invalid character: " + yyText ());
				}
				case 18: break;
				case 8:	// <<EOF>>
				{
					return 0;
				}
				case 19: break;
				case 9:	// .|\n
				{
					echo ();			// default character action
				}
				case 20: break;
				case 10:	// <<EOF>>
				{
					return 0;			// default EOF action
				}
				case 21: break;
				default:
					throw new IOException ("Internal error in TokenParser lexer.");
			}

		}
	}



	/**
	 * This is a stub main function that either reads the file that user specified
	 * or from the standard input.
	 *
	 * @param	args
	 *			command line arguments.
	 *
	 * @throws	Exception
	 *			in case of any errors.
	 */
	public static void main (String[] args) throws Exception
	{
		TokenParser tmpLexer = new TokenParser ();
		if (args.length > 0)
			tmpLexer.setInput (new java.io.FileInputStream (args[0]));

		tmpLexer.yyLex ();
	}


		private final LinkedList<String> m_tokenList = new LinkedList<String> ();

		private TokenParser ()
		{
		}

		public static String[] parseString (String input) throws IOException
		{
			TokenParser tokenParser = new TokenParser ();
			tokenParser.setInput (new ByteArrayInputStream (input.getBytes ("US-ASCII")));
			tokenParser.yyLex ();
			return tokenParser.m_tokenList.toArray (new String[tokenParser.m_tokenList.size ()]);
		}
	

/*
 * lexer properties:
 * unicode = false
 * bol = false
 * backup = true
 * cases = 10
 * table = ecs
 * ecs = 10
 * states = 16
 * max symbol value = 256
 *
 * memory usage:
 * full table = 4112
 * ecs table = 417
 *
 */
}
