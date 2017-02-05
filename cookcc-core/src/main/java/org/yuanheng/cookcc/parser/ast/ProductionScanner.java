package org.yuanheng.cookcc.parser.ast;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

abstract class ProductionScanner
{
	////////////////////////////////////////////////////////////////////////
	//
	// Terminal Definitions
	//
	////////////////////////////////////////////////////////////////////////
	protected final static int SYMBOL = 256;
	protected final static int LPAREN = 257;
	protected final static int RPAREN = 258;
	protected final static int OR = 259;
	protected final static int QUESTION = 260;
	protected final static int STAR = 261;
	protected final static int PLUS = 262;


	////////////////////////////////////////////////////////////////////////
	//
	// Lexer States
	//
	////////////////////////////////////////////////////////////////////////
	protected final static int INITIAL = 0;

	// an internal class for lazy initiation
	private final static class cc_lexer
	{
		private final static char[] accept = ("\000\016\014\015\016\016\002\003\005\006\004\007\001\017\000\000\010\000\000\000\011\000\000\013\000\012\000").toCharArray ();
		private final static char[] ecs = ("\000\000\000\000\000\000\000\000\000\001\002\000\001\003\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\000\000\000\000\000\000\004\005\006\007\010\000\000\000\000\011\011\011\011\011\011\011\011\011\011\000\000\000\000\000\012\000\013\013\013\013\013\013\014\014\014\014\014\014\014\014\014\014\014\014\014\014\014\014\014\014\014\014\000\015\000\000\014\000\013\013\013\013\013\013\014\014\014\014\014\014\014\014\014\014\014\014\014\014\014\014\014\016\014\014\000\017\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\020").toCharArray ();
		private final static char[] base = ("\025\000\000\000\000\000\000\000\000\000\000\005\000\000\001\001\000\042\036\011\000\003\000\000\044\000\045\000\006\052").toCharArray ();
		private final static char[] next = ("\016\002\003\016\031\020\021\027\021\032\022\032\030\017\013\023\013\013\026\013\026\001\002\003\004\005\006\007\010\011\001\012\013\013\001\013\014\015\024\025\027\031\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray ();
		private final static char[] check = ("\033\002\004\033\026\016\034\025\034\026\017\026\025\005\013\017\013\013\023\013\023\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\021\022\030\032\035\035\035\035\035\035\035\035\035\035\035\035").toCharArray ();
		private final static char[] defaults = ("\035\035\001\035\001\033\035\035\035\035\035\001\035\035\001\034\035\001\021\021\035\001\001\035\001\035\001\035\035\035").toCharArray ();
		private final static char[] meta = ("\000\000\001\002\002\000\000\000\000\003\000\000\000\002\003\000\001").toCharArray ();
	}

	// an internal class for lazy initiation
	private final static class cc_parser
	{
		private final static char[] rule = ("\000\001\000\001\001\001\001\002\002\003\003\003\003\001\002\002\002\002\002\002").toCharArray ();
		private final static char[] ecs = ("\000\001\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\004\005\006\007\010\011").toCharArray ();
		private final static char[] base = ("\017\020\002\005\000\001\000\023\000\000\000\030\003\000\000\030\000\000\000\000\000\000\004\013\015\025\042\043\044\045\046\047\050\051\052\014\024\053\055\073\062\073\062\073\073\073\073\073\073\000\073\066\073\073\070\073\073\073\073\073\000\000\073").toCharArray ();
		private final static char[] next = ("\ufffd\ufffc\000\000\ufff7\uffff\015\020\025\ufff7\ufff7\ufff6\ufff5\ufff3\ufff5\ufffe\ufff6\ufff6\001\002\ufff4\ufffb\ufff4\010\011\012\021\022\023\024\015\021\022\023\ufffa\uffee\ufff0\ufff2\ufff9\ufff8\uffed\uffef\ufff1\000\000\000\003\004\005\006\007\000\013\014\016\017\026\000\027\000\000\000\000\000\000").toCharArray ();
		private final static char[] check = ("\004\005\002\014\026\003\004\005\014\026\026\027\043\030\043\000\027\027\000\000\044\031\044\001\001\001\007\007\007\013\013\017\017\017\032\033\034\035\036\037\040\041\042\045\045\045\046\046\046\046\046\050\050\050\052\052\063\063\066\076\076\076\076\076\076").toCharArray ();
		private final static char[] defaults = ("\045\030\000\045\000\003\031\032\033\034\035\002\005\002\036\037\002\040\041\042\043\044\000\000\045\045\045\045\045\045\045\045\045\045\045\045\045\045").toCharArray ();
		private final static char[] meta = ("\000\001\001\000\000\000\000\002\002\002").toCharArray ();
		private final static char[] gotoDefault = ("\076\076\046\076\076\076\076\076\076\076\076\052\076\050\076\076\063\076\076\076\076\076\052\052\076").toCharArray ();
		private final static char[] lhs = ("\000\012\013\013\013\014\014\014\014\015\015\017\017\016\016\016\016\016\016\016").toCharArray ();
	}

	private final static class cc_parser_symbol
	{
		private final static String[] symbols =
		{
			"SYMBOL","LPAREN","RPAREN","OR","QUESTION","STAR","PLUS","@start","start","rule","orRule","symbol","parenRule"
		};
	}

	private final static class YYParserState	// internal tracking tool
	{
		int token;			// the current token type
		Object value;		// the current value associated with token
		int state;			// the current scan state

		YYParserState ()	// EOF token construction
		{
			this (0, null, 0);
		}
		YYParserState (int token, Object value)
		{
			this (token, value, 0);
		}
		YYParserState (int token, Object value, int state)
		{
			this.token = token;
			this.value = value;
			this.state = state;
		}
	}

	// for storing integer objects (so we do not create too many objects)
	private Integer[] _yySymbolArray;
	// lookahead stack for the parser
	private final ArrayList<YYParserState> _yyLookaheadStack = new ArrayList<YYParserState> (512);
	// state stack for the parser
	private final ArrayList<YYParserState> _yyStateStack = new ArrayList<YYParserState> (512);

	// flag that indicates error
	private boolean _yyInError;
	// internal track of the argument start
	private int _yyArgStart;
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
		// reset parser state
		_yyLookaheadStack.clear ();
		_yyStateStack.clear ();
		_yyArgStart = 0;
		_yyValue = null;
		_yyInError = false;

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
	protected int yyLex () throws IOException
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
					if (! refresh)
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
				case 1:	// '|'
				{
					m_this.scanOperator (); return OR;
				}
				case 19: break;
				case 2:	// '('
				{
					m_this.scanOperator (); return LPAREN;
				}
				case 20: break;
				case 3:	// ')'
				{
					m_this.scanOperator (); return RPAREN;
				}
				case 21: break;
				case 4:	// '?'
				{
					m_this.scanOperator (); return QUESTION;
				}
				case 22: break;
				case 5:	// '*'
				{
					m_this.scanOperator (); return STAR;
				}
				case 23: break;
				case 6:	// '+'
				{
					m_this.scanOperator (); return PLUS;
				}
				case 24: break;
				case 7:	// [a-zA-Z_][a-zA-Z_0-9]*
				{
					_yyValue = m_this.scanSymbol (); return SYMBOL;
				}
				case 25: break;
				case 8:	// ['][^'\\\r\n][']
				{
					_yyValue = m_this.scanTerminal (); return SYMBOL;
				}
				case 26: break;
				case 9:	// [']\\.[']
				{
					_yyValue = m_this.scanTerminal (); return SYMBOL;
				}
				case 27: break;
				case 10:	// [']\\x([0-9a-fA-F]{1,2})[']
				{
					_yyValue = m_this.scanTerminal (); return SYMBOL;
				}
				case 28: break;
				case 11:	// [']\\([0-9]{1,3})[']
				{
					_yyValue = m_this.scanTerminal (); return SYMBOL;
				}
				case 29: break;
				case 12:	// [ \t\f]+
				{
					m_this.ignoreWhiteSpace ();
				}
				case 30: break;
				case 13:	// \n|\r\n
				{
					m_this.scanNewLine ();
				}
				case 31: break;
				case 14:	// .
				{
					m_this.scanError ();
				}
				case 32: break;
				case 15:	// <<EOF>>
				{
					return m_this.scanEof ();
				}
				case 33: break;
				case 16:	// .|\n
				{
					echo ();			// default character action
				}
				case 34: break;
				case 17:	// <<EOF>>
				{
					return 0;			// default EOF action
				}
				case 35: break;
				default:
					throw new IOException ("Internal error in ProductionScanner lexer.");
			}

		}
	}

	/**
	 * Obtain the string representation for a symbol, which includes terminals
	 * and non-terminals.
	 *
	 * @param	symbol
	 *			The integer value of a symbol
	 * @return	the string representation of the symbol
	 */
	protected String getSymbolName (int symbol)
	{
		if (symbol < 0 || symbol > (255 + cc_parser_symbol.symbols.length))
			return "Unknown symbol: " + symbol;
		switch (symbol)
		{
			case 0:
				return "$";
			case 1:
				return "error";
			case '\\':
				return "'\\\\'";
			default:
				if (symbol > 255)
					return cc_parser_symbol.symbols[symbol - 256];
				if (symbol < 32 || symbol >= 127)
					return "'\\x" + Integer.toHexString (symbol) + "'";
				return "'" + ((char)symbol) + "'";
		}
	}

	/**
	 * Get the debugging string that represent the current parsing stack.
	 *
	 * @param	states
	 *			the current stack
	 * @return	a string representation of the parsing stack.
	 */
	protected String getStateString (Collection<YYParserState> states)
	{
		StringBuffer buffer = new StringBuffer ();
		boolean first = true;
		for (YYParserState state : states)
		{
			if (!first)
				buffer.append (" ");
			if (state.token < 0)
				buffer.append (state.token);
			else
				buffer.append (getSymbolName (state.token));
			first = false;
		}
		return buffer.toString ();
	}


	/**
	 * Call this function to start parsing.
	 *
	 * @return	0 if everything is okay, or 1 if an error occurred.
	 * @throws	IOException
	 *			in case of error
	 */
	@SuppressWarnings ("unchecked") 
	public int yyParse () throws IOException
	{
		char[] cc_ecs = cc_parser.ecs;
		char[] cc_next = cc_parser.next;
		char[] cc_check = cc_parser.check;
		char[] cc_base = cc_parser.base;
		char[] cc_default = cc_parser.defaults;
		char[] cc_meta = cc_parser.meta;
		char[] cc_gotoDefault = cc_parser.gotoDefault;
		char[] cc_rule = cc_parser.rule;
		char[] cc_lhs = cc_parser.lhs;

		ArrayList<YYParserState> cc_lookaheadStack = _yyLookaheadStack;
		ArrayList<YYParserState> cc_stateStack = _yyStateStack;
		if (cc_stateStack.size () == 0)
			cc_stateStack.add (new YYParserState ());

		int cc_toState;

		for (;;)
		{
			YYParserState cc_lookahead;

			int cc_fromState;
			char cc_ch;

			//
			// check if there are any lookahead tokens on stack
			// if not, then call yyLex ()
			//
			if (cc_lookaheadStack.size () == 0)
			{
				_yyValue = null;
				int val = yyLex ();
				cc_ch = cc_ecs[val];
				cc_lookahead = new YYParserState (val, _yyValue);
				cc_lookaheadStack.add (cc_lookahead);
			}
			else
			{
				cc_lookahead = cc_lookaheadStack.get (cc_lookaheadStack.size () - 1);
				cc_ch = cc_ecs[cc_lookahead.token];
			}

			cc_fromState = cc_stateStack.get (cc_stateStack.size () - 1).state;
			int cc_symbol = cc_ch;
			cc_toState = cc_fromState;
			while (cc_check[cc_symbol + cc_base[cc_toState]] != cc_toState)
			{
				cc_toState = cc_default[cc_toState];
				if (cc_toState >= 24)
					cc_symbol = cc_meta[cc_symbol];
			}
			cc_toState = (short)cc_next[cc_symbol + cc_base[cc_toState]];


			//
			// check the value of toState and determine what to do
			// with it
			//
			if (cc_toState > 0)
			{
				// shift
				cc_lookahead.state = cc_toState;
				cc_stateStack.add (cc_lookahead);
				cc_lookaheadStack.remove (cc_lookaheadStack.size () - 1);
				continue;
			}
			else if (cc_toState == 0)
			{
				// error
				if (_yyInError)
				{
					// first check if the error is at the lookahead
					if (cc_ch == 1)
					{
						// so we need to reduce the stack until a state with reduceable
						// action is found
						if (_yyStateStack.size () > 1)
							_yyStateStack.remove (_yyStateStack.size () - 1);
						else
							return 1;	// can't do much we exit the parser
					}
					else
					{
						// this means that we need to dump the lookahead.
						if (cc_ch == 0)		// can't do much with EOF;
							return 1;
						cc_lookaheadStack.remove (cc_lookaheadStack.size () - 1);
					}
					continue;
				}
				else
				{
					if (yyParseError (cc_lookahead.token))
						return 1;
					_yyLookaheadStack.add (new YYParserState (1, _yyValue));
					_yyInError = true;
					continue;
				}
			}
			_yyInError = false;
			// now the reduce action
			int cc_ruleState = -cc_toState;

			_yyArgStart = cc_stateStack.size () - cc_rule[cc_ruleState] - 1;
			//
			// find the state that said need this non-terminal
			//
			cc_fromState = cc_stateStack.get (_yyArgStart).state;

			//
			// find the state to goto after shifting the non-terminal
			// onto the stack.
			//
			if (cc_ruleState == 1)
				cc_toState = 0;			// reset the parser
			else
			{
				cc_toState = cc_fromState + 38;
				int cc_tmpCh = cc_lhs[cc_ruleState] - 10;
				while (cc_check[cc_tmpCh + cc_base[cc_toState]] != cc_toState)
					cc_toState = cc_gotoDefault[cc_toState - 38];
				cc_toState = cc_next[cc_tmpCh + cc_base[cc_toState]];
			}

			_yyValue = null;

			switch (cc_ruleState)
			{
				case 1:					// accept
					return 0;
				case 2:	// start : 
				{
					m_this.parseStart ();
				}
				case 22: break;
				case 3:	// start : rule
				{
					m_this.parseStart ((java.util.ArrayList<org.yuanheng.cookcc.parser.ast.Symbol>)yyGetValue (1));
				}
				case 23: break;
				case 4:	// start : orRule
				{
					m_this.parseStart ((org.yuanheng.cookcc.parser.ast.OrSymbol)yyGetValue (1));
				}
				case 24: break;
				case 5:	// rule : symbol
				{
					_yyValue = m_this.parseRule ((org.yuanheng.cookcc.parser.ast.Symbol)yyGetValue (1));
				}
				case 25: break;
				case 6:	// rule : parenRule
				{
					_yyValue = m_this.parseRule ((java.util.ArrayList<org.yuanheng.cookcc.parser.ast.Symbol>)yyGetValue (1));
				}
				case 26: break;
				case 7:	// rule : rule symbol
				{
					_yyValue = m_this.parseRule ((java.util.ArrayList<org.yuanheng.cookcc.parser.ast.Symbol>)yyGetValue (1), (org.yuanheng.cookcc.parser.ast.Symbol)yyGetValue (2));
				}
				case 27: break;
				case 8:	// rule : rule parenRule
				{
					_yyValue = m_this.parseRule ((java.util.ArrayList<org.yuanheng.cookcc.parser.ast.Symbol>)yyGetValue (1), (java.util.ArrayList<org.yuanheng.cookcc.parser.ast.Symbol>)yyGetValue (2));
				}
				case 28: break;
				case 9:	// orRule : rule OR rule
				{
					_yyValue = m_this.parseOr ((java.util.ArrayList<org.yuanheng.cookcc.parser.ast.Symbol>)yyGetValue (1), (java.util.ArrayList<org.yuanheng.cookcc.parser.ast.Symbol>)yyGetValue (3));
				}
				case 29: break;
				case 10:	// orRule : orRule OR rule
				{
					_yyValue = m_this.parseOr ((org.yuanheng.cookcc.parser.ast.OrSymbol)yyGetValue (1), (java.util.ArrayList<org.yuanheng.cookcc.parser.ast.Symbol>)yyGetValue (3));
				}
				case 30: break;
				case 11:	// parenRule : LPAREN rule RPAREN
				{
					_yyValue = m_this.parseParenRule ((java.util.ArrayList<org.yuanheng.cookcc.parser.ast.Symbol>)yyGetValue (2));
				}
				case 31: break;
				case 12:	// parenRule : LPAREN orRule RPAREN
				{
					_yyValue = m_this.parseParenRule ((org.yuanheng.cookcc.parser.ast.Symbol)yyGetValue (2));
				}
				case 32: break;
				case 13:	// symbol : SYMBOL
				{
					_yyValue = m_this.parseSymbol ((org.yuanheng.cookcc.parser.ast.Symbol)yyGetValue (1));
				}
				case 33: break;
				case 14:	// symbol : SYMBOL PLUS
				{
					_yyValue = m_this.parsePlus ((org.yuanheng.cookcc.parser.ast.Symbol)yyGetValue (1));
				}
				case 34: break;
				case 15:	// symbol : parenRule PLUS
				{
					_yyValue = m_this.parsePlus ((java.util.ArrayList<org.yuanheng.cookcc.parser.ast.Symbol>)yyGetValue (1));
				}
				case 35: break;
				case 16:	// symbol : SYMBOL STAR
				{
					_yyValue = m_this.parseStar ((org.yuanheng.cookcc.parser.ast.Symbol)yyGetValue (1));
				}
				case 36: break;
				case 17:	// symbol : parenRule STAR
				{
					_yyValue = m_this.parseStar ((java.util.ArrayList<org.yuanheng.cookcc.parser.ast.Symbol>)yyGetValue (1));
				}
				case 37: break;
				case 18:	// symbol : SYMBOL QUESTION
				{
					_yyValue = m_this.parseQuestion ((org.yuanheng.cookcc.parser.ast.Symbol)yyGetValue (1));
				}
				case 38: break;
				case 19:	// symbol : parenRule QUESTION
				{
					_yyValue = m_this.parseQuestion ((java.util.ArrayList<org.yuanheng.cookcc.parser.ast.Symbol>)yyGetValue (1));
				}
				case 39: break;
				default:
					throw new IOException ("Internal error in ProductionScanner parser.");
			}

			YYParserState cc_reduced = new YYParserState (-cc_ruleState, _yyValue, cc_toState);
			_yyValue = null;
			cc_stateStack.subList (_yyArgStart + 1, cc_stateStack.size ()).clear ();
			cc_stateStack.add (cc_reduced);
		}
	}

	/**
	 * This function is used by the error handling grammars to check the immediate
	 * lookahead token on the stack.
	 *
	 * @return	the top of lookahead stack.
	 */
	protected YYParserState yyPeekLookahead ()
	{
		return _yyLookaheadStack.get (_yyLookaheadStack.size () - 1);
	}

	/**
	 * This function is used by the error handling grammars to pop an unwantted
	 * token from the lookahead stack.
	 */
	protected void yyPopLookahead ()
	{
		_yyLookaheadStack.remove (_yyLookaheadStack.size () - 1);
	}

	/**
	 * Clear the error flag.  If this flag is present and the parser again sees
	 * another error transition, it would immediately calls yyParseError, which
	 * would by default exit the parser.
	 * <p>
	 * This function is used in error recovery.
	 */
	protected void yyClearError ()
	{
		_yyInError = false;
	}

	/**
	 * Check if the terminal is not handled by the parser.
	 *
	 * @param	terminal
	 *			terminal obtained from calling yyLex ()
	 * @return	true if the terminal is not handled by the parser.
	 * 			false otherwise.
	 */
	protected boolean isUnhandledTerminal (int terminal)
	{
		return cc_parser.ecs[terminal] == 2;
	}

	/**
	 * This function reports error and return true if critical error occurred, or
	 * false if the error has been successfully recovered.  IOException is an optional
	 * choice of reporting error.
	 *
	 * @param	terminal
	 *			the terminal that caused the error.
	 * @return	true if irrecoverable error occurred.  Or simply throw an IOException.
	 *			false if the parsing can be continued to check for specific
	 *			error tokens.
	 * @throws	IOException
	 *			in case of error.
	 */
	protected boolean yyParseError (int terminal) throws IOException
	{
		if (isUnhandledTerminal (terminal))
			return true;
		return false;
	}

	/**
	 * Gets the object value associated with the symbol at the argument's position.
	 *
	 * @param	arg
	 *			the symbol position starting from 1.
	 * @return	the object value associated with symbol.
	 */
	protected Object yyGetValue (int arg)
	{
		return _yyStateStack.get (_yyArgStart + arg).value;
	}

	/**
	 * Set the object value for the current non-terminal being reduced.
	 *
	 * @param	value
	 * 			the object value for the current non-terminal.
	 */
	protected void yySetValue (Object value)
	{
		_yyValue = value;
	}

	/**
	 * Obtain the current list of captured terminals.
	 * <p>
	 * Each Object[] contains two values.  The first is the {@link Integer} value
	 * of the terminal.  The second value is the value associated with the terminal.
	 *
	 * @param	arg
	 *			the symbol position starting from 1.
	 * @return	the captured terminals associated with the symbol
	 */
	protected Collection<Object[]> getCapturedTerminals (int arg)
	{
		return null;
	}

	/**
	 * A small utility to avoid too many Integer object creations.
	 *
	 * @param	symbol
	 *			an integer value.  Usually it is a symbol.
	 * @return	an Integer value matching the symbol value passed in.
	 */
	private Integer getInteger (int symbol)
	{
		if (_yySymbolArray == null)
			_yySymbolArray = new Integer[262 + 6 + 1];
		if (symbol < 0 || symbol >= _yySymbolArray.length)
			return new Integer (symbol);
		if (_yySymbolArray[symbol] == null)
			_yySymbolArray[symbol] = new Integer (symbol);
		return _yySymbolArray[symbol];
	}

	private final org.yuanheng.cookcc.parser.ast.ProductionParser m_this = (org.yuanheng.cookcc.parser.ast.ProductionParser)this;

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
 * backup = true
 * cases = 17
 * table = compressed
 * ecs = 17
 * states = 27
 * max symbol value = 256
 *
 * memory usage:
 * full table = 6939
 * ecs table = 716
 * next = 54
 * check = 54
 * default = 30
 * meta = 17
 * compressed table = 412
 *
 * parser properties:
 * symbols = 13
 * max terminal = 262
 * used terminals = 10
 * non-terminals = 6
 * rules = 19
 * shift/reduce conflicts = 0
 * reduce/reduce conflicts = 0
 *
 * memory usage:
 * ecs table = 647
 * compressed table = 529
 */
}
