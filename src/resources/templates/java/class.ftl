<#setting number_format=0>
<#macro array a><#list a as i><#if i_index &gt; 0>,</#if>${i}</#list></#macro>
<#macro intarray a>${a?javastring()}</#macro>
<#macro type a b c d>${a?type(b,c,d)}</#macro>
<#if code.fileheader?has_content>
${code.fileheader}
</#if>
<#if package?has_content>
package ${package};
</#if>

import java.io.IOException;
<#if unicode>
import java.io.InputStreamReader;
import java.io.Reader;
<#else>
import java.io.InputStream;
</#if>

<#if parser?has_content>
import java.util.LinkedList;
import java.util.Vector;
</#if>
<#if lexer?has_content>
import java.util.Stack;
</#if>

<#if code.classheader?has_content>
${code.classheader}
</#if>
<#if public?has_content && public?string == "true">public </#if><#if abstract?has_content && abstract?string == "true">abstract </#if>class ${ccclass}<#if extend?has_content> extends ${extend}</#if>
{
<#if parser?has_content && parser.tokens?has_content>
<#list parser.tokens as i>
	protected final static int ${i.name} = ${i.value};
</#list>
</#if>

<#if lexer?has_content>
<#list lexer.states as i>
	protected final static int ${i} = ${lexer.begins[i_index]};
</#list>

	// an internal class for lazy initiation
	private final static class cc_lexer
	{
		private static char[] accept = <@intarray lexer.accept/>;
<#if lexer.table == "ecs" || lexer.table == "compressed">
		private static char[] ecs = <@intarray lexer.ecs/>;
</#if>
<#if lexer.table == "full" || lexer.table == "ecs">
		private static char[][] next = {<#list lexer.dfa.table as i><#if i_index &gt; 0>,</#if><@intarray i/></#list>};
</#if>
<#if lexer.table == "compressed">
		private static char[] base = <@intarray lexer.dfa.base/>;
		private static char[] next = <@intarray lexer.dfa.next/>;
		private static char[] check = <@intarray lexer.dfa.check/>;
	<#if lexer.dfa.default?has_content>
		private static char[] defaults = <@intarray lexer.dfa.default/>;
	</#if>
	<#if lexer.dfa.meta?has_content>
		private static char[] meta = <@intarray lexer.dfa.meta/>;
	</#if>
</#if>
	}
</#if>

<#if parser?has_content>
	// an internal class for lazy initiation
	private final static class cc_parser
	{
		private static char[] rule = <@intarray parser.rules/>;
		private static char[] ecs = <@intarray parser.ecs/>;
<#if parser.table == "ecs">
		private static char[][] next = {<#list parser.dfa.table as i><#if i_index &gt; 0>,</#if><@intarray i/></#list>};
<#else>
		private static char[] base = <@intarray parser.dfa.base/>;
		private static char[] next = <@intarray parser.dfa.next/>;
		private static char[] check = <@intarray parser.dfa.check/>;
		<#if parser.dfa.default?has_content>
		private static char[] defaults = <@intarray parser.dfa.default/>;
		</#if>
		<#if parser.dfa.meta?has_content>
		private static char[] meta = <@intarray parser.dfa.meta/>;
		</#if>
		<#if parser.dfa.gotoDefault?has_content>
		private static char[] gotoDefault = <@intarray parser.dfa.gotoDefault/>;
		</#if>
</#if>
		private static char[] lhs = <@intarray parser.lhs/>;
<#if debug>
		private static String[] symbols = {<#list parser.symbols as i><#if i_index &gt; 0>,</#if>"${i}"</#list>};
</#if>
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

<#if generics?has_content && generics?string == "true">
	// lookahead stack for the parser
	private final LinkedList<YYParserState> _yyLookaheadStack = new LinkedList<YYParserState> ();
	// state stack for the parser
	private final Vector<YYParserState> _yyStateStack = new Vector<YYParserState> (512, 512);
<#else>
	// lookahead stack for the parser
	private final LinkedList _yyLookaheadStack = new LinkedList ();
	// state stack for the parser
	private final Vector _yyStateStack = new Vector (512, 512);
</#if>

<#if parser.recovery>
	// flag that indicates error
	private boolean _yyInError;
</#if>
	// internal track of the argument start
	private int _yyArgStart;
	// for passing value from lexer to parser
	private Object _yyValue;
</#if>

<#if lexer?has_content>
<#if unicode>
	private Reader _yyIs = new InputStreamReader (System.in);
	private char[] _yyBuffer;
<#else>
	private InputStream _yyIs = System.in;
	private byte[] _yyBuffer;
</#if>
	private int _yyBufferSize = 4096;
	private int _yyMatchStart;
	private int _yyBufferEnd;

	private int _yyBaseState;

	private int _yyTextStart;
	private int _yyLength;

<#if generics?has_content && generics?string == "true">
	private Stack<Integer> _yyLexerStack;
	private Stack<Object[]> _yyInputStack;
<#else>
	private Stack _yyLexerStack;
	private Stack _yyInputStack;
</#if>

<#if lexer.bol>
	// we need to track beginning of line (BOL) status
	private boolean _yyIsNextBOL = true;
	private boolean _yyBOL = true;
</#if>
</#if>

<#if lexer?has_content>
<#if unicode>
	/**
	 * Set the current input.
	 *
	 * @param	reader
	 *			the new input.
	 */
	public void setInput (Reader reader)
	{
		_yyIs = reader;
	}

	/**
	 * Obtain the current input.
	 *
	 * @return	the current input
	 */
	public Reader getInput ()
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
	public void yyPushInput (Reader is)
	{
		int len = _yyBufferEnd - _yyMatchStart;
		char[] leftOver = new char[len];
		System.arraycopy (_yyBuffer, _yyMatchStart, leftOver, 0, len);

		Object[] states = new Object[4];
		states[0] = _yyIs;
		states[1] = leftOver;

		if (_yyInputStack == null)
<#if generics?has_content && generics?string == "true">
			_yyInputStack = new Stack<Object[]> ();
<#else>
			_yyInputStack = new Stack ();
</#if>
		_yyInputStack.push (states);

		_yyIs = is;
		_yyMatchStart = 0;
		_yyBufferEnd = 0;
	}

	/**
	 * Switch the current input to the old input on stack.  The current input
	 * and its buffered characters are all switch to the old ones.
	 */
	public void yyPopInput ()
	{
<#if generics?has_content && generics?string == "true">
		Object[] states = _yyInputStack.pop ();
<#else>
		Object[] states = (Object[])_yyInputStack.pop ();
</#if>
		_yyIs = (Reader)states[0];
		char[] leftOver = (char[])states[1];

		int curLen = _yyBufferEnd - _yyMatchStart;

		if ((leftOver.length + curLen) > _yyBuffer.length)
		{
			char[] newBuffer = new char[leftOver.length + curLen];
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
			char[] buffer = _yyBuffer;

			for (int i = 0; start < end; ++i, ++start)
				buffer[i] = buffer[start];
			System.arraycopy (leftOver, 0, buffer, curLen, leftOver.length);
			_yyMatchStart = 0;
			_yyBufferEnd = leftOver.length + curLen;
		}
	}
<#else>
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
<#if generics?has_content && generics?string == "true">
			_yyInputStack = new Stack<Object[]> ();
<#else>
			_yyInputStack = new Stack ();
</#if>
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
<#if generics?has_content && generics?string == "true">
		Object[] states = _yyInputStack.pop ();
<#else>
		Object[] states = (Object[])_yyInputStack.pop ();
</#if>
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
</#if>

	/**
	 * Obtain the number of input objects on the stack.
	 *
	 * @return	the number of input objects on the stack.
	 */
	public int yyInputStackSize ()
	{
		return _yyInputStack == null ? 0 : _yyInputStack.size ();
	}

<#if lexer.bol>
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
	 * Set whether or not the next token at the beginning of the line.
	 *
	 * @param	bol
	 *			the bol status
	 */
	public void setBOL (boolean bol)
	{
		_yyIsNextBOL = bol;
	}
</#if>

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
<#if generics?has_content && generics?string == "true">
			_yyLexerStack = new Stack<Integer> ();
<#else>
			_yyLexerStack = new Stack ();
</#if>
		_yyLexerStack.push (new Integer (_yyBaseState));
		begin (newState);
	}

	/**
	 * Restore the previous lexer state.
	 */
	protected void yyPopLexerState ()
	{
<#if generics?has_content && generics?string == "true">
		begin (_yyLexerStack.pop ());
<#else>
		begin (((Integer)_yyLexerStack.pop ()).intValue ());
</#if>
	}

	<#if debug>
	protected boolean debugLexer (int baseState, int matchedState, int accept)
	{
		System.err.println ("lexer: " + baseState + ", " + matchedState + ", " + accept + ", " + yyText ());
		return true;
	}

	protected boolean debugLexerBackup (int baseState, int backupState, String backupString)
	{
		System.err.println ("lexer backup: " + baseState + ", " + backupState + ", " + backupString);
		return true;
	}
	</#if>

	// read more data from the input
	protected boolean yyRefreshBuffer () throws IOException
	{
		if (_yyBuffer == null)
			_yyBuffer = new <#if unicode>char<#else>byte</#if>[_yyBufferSize];
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
			<#if unicode>char<#else>byte</#if>[] newBuffer = new <#if unicode>char<#else>byte</#if>[_yyBuffer.length + _yyBuffer.length / 2];

			System.arraycopy (_yyBuffer, 0, newBuffer, 0, _yyBufferEnd);
			_yyBuffer = newBuffer;
		}

		int readSize = _yyIs.read (_yyBuffer, _yyBufferEnd, _yyBuffer.length - _yyBufferEnd);
		if (readSize > 0)
			_yyBufferEnd += readSize;
	<#if lexer.yywrap>
		else if (readSize < 0 && !yyWrap ())		// since we are at EOF, call yyWrap ().  If the return value of yyWrap is false, refresh buffer again
			return yyRefreshBuffer ();
	</#if>
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
			_yyBuffer = new <#if unicode>char<#else>byte</#if>[bufferSize];
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
<#if parser?has_content>
		// reset parser state
		_yyLookaheadStack.clear ();
		_yyStateStack.clear ();
		_yyArgStart = 0;
		_yyValue = null;
<#if parser.recovery>
		_yyInError = false;
</#if>
</#if>

<#if lexer?has_content>
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

<#if lexer.bol>
		_yyIsNextBOL = true;
		_yyBOL = true;
</#if>

</#if>
	}

	/**
	 * Call this function to start the scanning of the input.
	 *
	 * @return	a token or status value.
	 * @throws	IOException
	 *			in case of I/O error.
	 */
	<#if parser?has_content>protected<#else>public</#if> int yyLex () throws IOException
	{
<#if code.lexerprolog?has_content>
	${code.lexerprolog}
</#if>

<#if lexer.table == "ecs" || lexer.table == "compressed">
		char[] cc_ecs = cc_lexer.ecs;
</#if>
<#if lexer.table == "ecs" || lexer.table == "full">
		char[][] cc_next = cc_lexer.next;
<#elseif lexer.table="compressed">
		char[] cc_next = cc_lexer.next;
		char[] cc_check = cc_lexer.check;
	<#if lexer.dfa.base?has_content>
		char[] cc_base = cc_lexer.base;
	</#if>
	<#if lexer.dfa.default?has_content>
		char[] cc_default = cc_lexer.defaults;
	</#if>
	<#if lexer.dfa.meta?has_content>
		char[] cc_meta = cc_lexer.meta;
	</#if>
</#if>
		char[] cc_accept = cc_lexer.accept;

		<#if unicode>char<#else>byte</#if>[] buffer = _yyBuffer;

		while (true)
		{
			// initiate variables necessary for lookup
<#if lexer.bol>
			_yyBOL = _yyIsNextBOL;
			_yyIsNextBOL = false;
	<#if lexer.bolStates>
			int cc_matchedState = _yyBaseState + (_yyBOL ? 1 : 0);
	<#else>
			int cc_matchedState = _yyBaseState;
	</#if>
<#else>
			int cc_matchedState = _yyBaseState;
</#if>

			int matchedLength = 0;

			int internalBufferEnd = _yyBufferEnd;
			int lookahead = _yyMatchStart;

<#if lexer.backup>
			int cc_backupMatchedState = cc_matchedState;
			int cc_backupMatchedLength = 0;
</#if>

			// the DFA lookup
			while (true)
			{
				// check buffer status
				if (lookahead < internalBufferEnd)
				{
					// now okay to process the character
					int cc_toState;
<#if lexer.lineMode>
					<#if unicode>char<#else>byte</#if> ch = buffer[lookahead];
</#if>
<#if lexer.table == "full">
					cc_toState = cc_next[cc_matchedState][<#if lexer.lineMode>ch<#else>buffer[lookahead]</#if><#if !unicode> & 0xff</#if>];
<#elseif lexer.table == "ecs">
					cc_toState = cc_next[cc_matchedState][cc_ecs[<#if lexer.lineMode>ch<#else>buffer[lookahead]</#if><#if !unicode> & 0xff</#if>]];
<#else>
					int symbol = cc_ecs[<#if lexer.lineMode>ch<#else>buffer[lookahead]</#if><#if !unicode> & 0xff</#if>];
					cc_toState = cc_matchedState;
	<#if !lexer.dfa.default?has_content>
					if (cc_check[symbol + cc_base[cc_matchedState]] == cc_matchedState)
						cc_toState = cc_next[symbol + cc_base[cc_matchedState]];
					else
						cc_toState = 0;
	<#elseif !lexer.dfa.error?has_content>
					if (cc_check[symbol + cc_base[cc_matchedState]] == cc_matchedState)
						cc_toState = cc_next[symbol + cc_base[cc_matchedState]];
					else
						cc_toState = cc_default[cc_matchedState];
	<#elseif !lexer.dfa.meta?has_content>
					while (cc_check[symbol + cc_base[cc_toState]] != cc_toState)
					{
						cc_toState = cc_default[cc_toState];
						if (cc_toState >= ${lexer.dfa.size})
							symbol = 0;
					}
					cc_toState = cc_next[symbol + cc_base[cc_toState]];
	<#else>
					while (cc_check[symbol + cc_base[cc_toState]] != cc_toState)
					{
						cc_toState = cc_default[cc_toState];
						if (cc_toState >= ${lexer.dfa.size})
							symbol = cc_meta[symbol];
					}
					cc_toState = cc_next[symbol + cc_base[cc_toState]];
	</#if>
</#if>

<#if lexer.backup>
					if (cc_toState == 0)
					{
	<#if debug>
						debugLexerBackup (_yyBaseState, cc_matchedState, new String (_yyBuffer, _yyMatchStart, matchedLength));
	</#if>
						cc_matchedState = cc_backupMatchedState;
						matchedLength = cc_backupMatchedLength;
						break;
					}
<#else>
					if (cc_toState == 0)
						break;
</#if>

					cc_matchedState = cc_toState;
					++lookahead;
					++matchedLength;

<#if lexer.backup>
					if (cc_accept[cc_matchedState] > 0)
					{
						cc_backupMatchedState = cc_toState;
						cc_backupMatchedLength = matchedLength;
					}
</#if>
<#if lexer.lineMode>
					if (ch == '\n')
					{
	<#if lexer.backup>
						cc_matchedState = cc_backupMatchedState;
						matchedLength = cc_backupMatchedLength;
	</#if>
						break;
					}
</#if>
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
<#if lexer.table == "full">
						cc_toState = cc_next[cc_matchedState][${lexer.eof}];
<#elseif lexer.table == "ecs">
						cc_toState = cc_next[cc_matchedState][cc_ecs[${lexer.eof}]];
<#elseif lexer.table == "compressed">
						int symbol = cc_ecs[${lexer.eof}];
	<#if !lexer.dfa.default?has_content>
						if (cc_check[symbol + cc_base[cc_matchedState]] == cc_matchedState)
							cc_toState = cc_next[symbol + cc_base[cc_matchedState]];
						else
							cc_toState = 0;
	<#elseif !lexer.dfa.error>
						if (cc_check[symbol + cc_base[cc_matchedState]] == cc_matchedState)
							cc_toState = cc_next[symbol + cc_base[cc_matchedState]];
						else
							cc_toState = cc_default[cc_matchedState];
	<#elseif !lexer.dfa.meta?has_content>
						cc_toState = cc_matchedState;
						while (cc_check[symbol + cc_base[cc_toState]] != cc_toState)
						{
							cc_toState = cc_default[cc_toState];
							if (cc_toState >= ${lexer.dfa.size})
								symbol = 0;
						}
						cc_toState = cc_next[symbol + cc_base[cc_toState]];
	<#else>
						cc_toState = cc_matchedState;
						while (cc_check[symbol + cc_base[cc_toState]] != cc_toState)
						{
							cc_toState = cc_default[cc_toState];
							if (cc_toState >= ${lexer.dfa.size})
								symbol = cc_meta[symbol];
						}
						cc_toState = cc_next[symbol + cc_base[cc_toState]];
	</#if>

</#if>
						if (cc_toState != 0)
							cc_matchedState = cc_toState;
<#if lexer.backup>
						else
						{
							cc_matchedState = cc_backupMatchedState;
							matchedLength = cc_backupMatchedLength;
						}
</#if>
						break;
					}
				}
			}

			_yyTextStart = _yyMatchStart;
			_yyMatchStart += matchedLength;
			_yyLength = matchedLength;

<#if debug>
			debugLexer (_yyBaseState, cc_matchedState, cc_accept[cc_matchedState]);
</#if>

			switch (cc_accept[cc_matchedState])
			{
<#list lexer.cases as i>
	<#if !i.internal>
		<#list i.patterns as p>
				case ${p.caseValue}:	// ${p.pattern}
				{
			<#if p.trailContext != 0 && p.trailContext != 3>
				<#if (p.trailContext % 2) == 1>
					_yyLength = ${p.trailLength};
					_yyMatchStart = _yyTextStart + ${p.trailLength};
				<#else>
					_yyLength -= ${p.trailLength};
					_yyMatchStart -= ${p.trailLength};
				</#if>
			</#if>
					${i.action}
				}
				case ${p.caseValue + lexer.caseCount + 1}: break;
		</#list>
	<#else>
		<#list i.patterns as p>
				case ${p.caseValue}:	// ${p.pattern}
				{
			<#if p.pattern == "<<EOF>>">
					return 0;			// default EOF action
			<#else>
					echo ();			// default character action
			</#if>
				}
				case ${p.caseValue + lexer.caseCount + 1}: break;
		</#list>
	</#if>
</#list>
				default:
					throw new IOException ("Internal error in ${ccclass} lexer.");
			}

<#if lexer.bol>
			// check BOL here since '\n' may be unput back into the stream buffer

			// specifically used _yyBuffer since it could be changed by user
			if (_yyMatchStart > 0 && _yyBuffer[_yyMatchStart - 1] == '\n')
				_yyIsNextBOL = true;
</#if>
		}
	}
</#if>
<#if !lexer?has_content>
	/**
	 * Override this function to start the scanning of the input.  This function
	 * is used by the parser to scan the tokens.
	 *
	 * @return	a status value.
	 * @throws	IOException
	 *			in case of I/O error.
	 */
	protected int yyLex () throws IOException
	{
		return 0;
	}
</#if>

<#if parser?has_content>
	<#if debug>
	protected boolean debugParser (int fromState, int toState, int reduceState, String reduceStateName, int ecsToken, String tokenName)
	{
		if (toState == 0)
			System.err.println ("parser: " + fromState + ", " + toState + ", " + tokenName + ", error");
		else if (toState < 0)
			System.err.println ("parser: " + fromState + ", " + toState + ", " + tokenName + ", reduce " + reduceStateName);
		else
			System.err.println ("parser: " + fromState + ", " + toState + ", " + tokenName + ", shift");
		return true;
	}
	</#if>

	/**
	 * Call this function to start parsing.
	 *
	 * @return	0 if everything is okay, or 1 if an error occurred.
	 * @throws	IOException
	 *			in case of error
	 */
<#if parser.getProperty("SuppressUnCheckWarning")?has_content && parser.getProperty("SuppressUnCheckWarning")?string == 'true'>
	@SuppressWarnings ("unchecked") 
</#if>
	public int yyParse () throws IOException
	{
 <#if code.parserprolog?has_content>
		${code.parserprolog}

 </#if>
		char[] cc_ecs = cc_parser.ecs;
<#if parser.table == "ecs">
		char[][] cc_next = cc_parser.next;
<#else>
		char[] cc_next = cc_parser.next;
		char[] cc_check = cc_parser.check;
		char[] cc_base = cc_parser.base;
	<#if parser.dfa.default?has_content>
		char[] cc_default = cc_parser.defaults;
	</#if>
	<#if parser.dfa.meta?has_content>
		char[] cc_meta = cc_parser.meta;
	</#if>
	<#if parser.dfa.gotoDefault?has_content>
		char[] cc_gotoDefault = cc_parser.gotoDefault;
	</#if>
</#if>
		char[] cc_rule = cc_parser.rule;
		char[] cc_lhs = cc_parser.lhs;

<#if generics?has_content && generics?string == "true">
		LinkedList<YYParserState> cc_lookaheadStack = _yyLookaheadStack;
		Vector<YYParserState> cc_stateStack = _yyStateStack;
<#else>
		LinkedList cc_lookaheadStack = _yyLookaheadStack;
		Vector cc_stateStack = _yyStateStack;
</#if>
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
				cc_lookahead = new YYParserState (val, _yyValue);
				cc_lookaheadStack.add (cc_lookahead);
			}
			else
<#if generics?has_content && generics?string == "true">
				cc_lookahead = cc_lookaheadStack.getLast ();
<#else>
				cc_lookahead = (YYParserState)cc_lookaheadStack.getLast ();
</#if>

			cc_ch = cc_ecs[cc_lookahead.token];
<#if generics?has_content && generics?string == "true">
			cc_fromState = cc_stateStack.get (cc_stateStack.size () - 1).state;
<#else>
			cc_fromState = ((YYParserState)cc_stateStack.get (cc_stateStack.size () - 1)).state;
</#if>
<#if parser.table == "ecs">
			cc_toState = (short)cc_next[cc_fromState][cc_ch];
<#else>
	<#if !parser.dfa.default?has_content>
			if (cc_check[cc_ch + cc_base[cc_fromState]] == cc_fromState)
				cc_toState = (short)cc_next[cc_ch + cc_base[cc_fromState]];
			else
				cc_toState = 0;
	<#elseif !parser.dfa.error?has_content>
			if (cc_check[cc_ch + cc_base[cc_fromState]] == cc_fromState)
				cc_toState = (short)cc_next[cc_ch + cc_base[cc_fromState]];
			else
				cc_toState = (short)cc_default[cc_fromState];
	<#elseif !parser.dfa.meta?has_content>
			int cc_symbol = cc_ch;
			cc_toState = cc_fromState;
			while (cc_check[cc_symbol + cc_base[cc_toState]] != cc_toState)
			{
				cc_toState = cc_default[cc_toState];
				if (cc_toState >= ${parser.dfa.size})
					cc_symbol = 0;
			}
			cc_toState = (short)cc_next[cc_symbol + cc_base[cc_toState]];
	<#else>
			int cc_symbol = cc_ch;
			cc_toState = cc_fromState;
			while (cc_check[cc_symbol + cc_base[cc_toState]] != cc_toState)
			{
				cc_toState = cc_default[cc_toState];
				if (cc_toState >= ${parser.dfa.size})
					cc_symbol = cc_meta[cc_symbol];
			}
			cc_toState = (short)cc_next[cc_symbol + cc_base[cc_toState]];
	</#if>
</#if>

<#if debug>
			debugParser (cc_fromState, cc_toState, cc_toState < 0 ? cc_parser.lhs[-cc_toState] : 0, cc_toState < 0 ? cc_parser.symbols[cc_parser.lhs[-cc_toState]] : "", cc_ch, cc_parser.symbols[cc_ch]);
</#if>

			//
			// check the value of toState and determine what to do
			// with it
			//
			if (cc_toState > 0)
			{
				// shift
				cc_lookahead.state = cc_toState;
				cc_stateStack.add (cc_lookahead);
				cc_lookaheadStack.removeLast ();
				continue;
			}
			else if (cc_toState == 0)
			{
<#if parser.recovery>
				// error
				if (_yyInError)
				{
					// first check if the error is at the lookahead
					if (cc_ch == 1)
					{
						// so we need to reduce the stack until a state with reduceable
						// action is found
						if (_yyStateStack.size () > 1)
							_yyStateStack.setSize (_yyStateStack.size () - 1);
						else
							return 1;	// can't do much we exit the parser
					}
					else
					{
						// this means that we need to dump the lookahead.
						if (cc_ch == 0)		// can't do much with EOF;
							return 1;
						cc_lookaheadStack.removeLast ();
					}
					continue;
				}
				else
				{
					if (yyParseError (cc_lookahead.token))
						return 1;
	<#if debug>
					System.err.println ("parser: inject error token as lookahead");
	</#if>
					_yyLookaheadStack.add (new YYParserState (1, _yyValue));
					_yyInError = true;
					continue;
				}
<#else>
				return 1;
</#if>
			}
<#if parser.recovery>
			_yyInError = false;
</#if>
			// now the reduce action
			int cc_ruleState = -cc_toState;

			_yyArgStart = cc_stateStack.size () - cc_rule[cc_ruleState] - 1;
			//
			// find the state that said need this non-terminal
			//
<#if generics?has_content && generics?string == "true">
			cc_fromState = cc_stateStack.get (_yyArgStart).state;
<#else>
			cc_fromState = ((YYParserState)cc_stateStack.get (_yyArgStart)).state;
</#if>

			//
			// find the state to goto after shifting the non-terminal
			// onto the stack.
			//
			if (cc_ruleState == 1)
				cc_toState = 0;			// reset the parser
			else
			{
<#if parser.table == "ecs">
				cc_toState = cc_next[cc_fromState][cc_lhs[cc_ruleState]];
<#else>
				cc_toState = cc_fromState + ${parser.dfa.baseAdd};
				int cc_tmpCh = cc_lhs[cc_ruleState] - ${parser.dfa.usedTerminalCount};
	<#if !parser.dfa.gotoDefault?has_content>
				if (cc_check[cc_tmpCh + cc_base[cc_toState]] == cc_toState)
					cc_toState = cc_next[cc_tmpCh + cc_base[cc_toState]];
				else
					cc_toState = 0;
	<#else>
				while (cc_check[cc_tmpCh + cc_base[cc_toState]] != cc_toState)
					cc_toState = cc_gotoDefault[cc_toState - ${parser.dfa.baseAdd}];
				cc_toState = cc_next[cc_tmpCh + cc_base[cc_toState]];
	</#if>
</#if>
			}

			_yyValue = null;

			switch (cc_ruleState)
			{
				case 1:					// accept
					return 0;
<#list parser.cases as i>
<#if i.type == 'n'>
<#list i.rhs as p>
				case ${p.caseValue}:	// ${i.rule} : ${p.terms}
				{
					<#list p.action?actioncode() as a><#if a_index % 2 == 0>${a}<#else><#if a == "$">_yyValue<#else><@type p a parser.formats "yyGetValue (" + a + ")"/></#if></#if></#list>
				}
				case ${p.caseValue + parser.caseCount}: break;
</#list>
<#elseif i.type == '?'>
<#list i.rhs as p>
				// internally generated optional rule
				case ${p.caseValue}:	// ${i.rule} : ${p.terms}
				{
				<#if p_index == 0>
					_yyValue = null;
				<#else>
					_yyValue = yyGetValue (1);
				</#if>
					break;
				}
</#list>
<#elseif i.type == '*'>
<#list i.rhs as p>
				// internally generated optional list rule
				case ${p.caseValue}:	// ${i.rule} : ${p.terms}
				{
				<#if p_index == 0>
					_yyValue = new LinkedList ();
				<#else>
					_yyValue = yyGetValue (1);
					((LinkedList)_yyValue).add (yyGetValue (2));
				</#if>
					break;
				}
</#list>
<#elseif i.type == '+'>
<#list i.rhs as p>
				// internally generated list rule
				case ${p.caseValue}:	// ${i.rule} : ${p.terms}
				{
				<#if p_index == 0>
					_yyValue = new LinkedList ();
					((LinkedList)_yyValue).add (yyGetValue (1));
				<#else>
					_yyValue = yyGetValue (1);
					((LinkedList)_yyValue).add (yyGetValue (2));
				</#if>
					break;
				}
</#list>
</#if>
</#list>
				default:
					throw new IOException ("Internal error in ${ccclass} parser.");
			}

			YYParserState cc_reduced = new YYParserState (-cc_ruleState, _yyValue, cc_toState);
			_yyValue = null;
			cc_stateStack.setSize (_yyArgStart + 1);
			cc_stateStack.add (cc_reduced);
		}
	}

<#if parser.recovery>
	/**
	 * This function is used by the error handling grammars to check the immediate
	 * lookahead token on the stack.
	 *
	 * @return	the top of lookahead stack.
	 */
	protected YYParserState yyPeekLookahead ()
	{
<#if generics?has_content && generics?string == "true">
		return _yyLookaheadStack.getLast ();
<#else>
		return (YYParserState)_yyLookaheadStack.getLast ();
</#if>
	}

	/**
	 * This function is used by the error handling grammars to pop an unwantted
	 * token from the lookahead stack.
	 */
	protected void yyPopLookahead ()
	{
		_yyLookaheadStack.removeLast ();
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

<#if parser.parseError>
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
<#if debug>
		System.err.println ("parser: fatal error");
</#if>
		return false;
	}
</#if>
</#if>

	/**
	 * Gets the object value associated with the symbol at the argument's position.
	 *
	 * @param	arg
	 *			the symbol position starting from 1.
	 * @return	the object value associated with symbol.
	 */
	protected Object yyGetValue (int arg)
	{
<#if generics?has_content && generics?string == "true">
		return _yyStateStack.get (_yyArgStart + arg).value;
<#else>
		return ((YYParserState)_yyStateStack.get (_yyArgStart + arg)).value;
</#if>
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

</#if>


<#if main?has_content && main?string == "true">
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
<#if parser?has_content>
		${ccclass} tmpParser = new ${ccclass} ();
	<#if lexer?has_content>
		<#if unicode>
		if (args.length > 0)
			tmpParser.setInput (new InputStreamReader (new java.io.FileInputStream (args[0])));
		<#else>
		if (args.length > 0)
			tmpParser.setInput (new java.io.FileInputStream (args[0]));
		</#if>
	</#if>

		if (tmpParser.yyParse () > 0)
		{
			System.err.println ("parser: fatal error!");
			System.exit (1);
		}
<#else>
		${ccclass} tmpLexer = new ${ccclass} ();
	<#if unicode>
		if (args.length > 0)
			tmpLexer.setInput (new InputStreamReader (new java.io.FileInputStream (args[0])));
	<#else>
		if (args.length > 0)
			tmpLexer.setInput (new java.io.FileInputStream (args[0]));
	</#if>

		tmpLexer.yyLex ();
</#if>
	}
</#if>

<#if code.default?has_content>
${code.default}
</#if>

/*
 * lexer properties:
 * unicode = ${unicode?string}
<#if lexer?has_content>
 * bol = ${lexer.bol?string}
 * backup = ${lexer.backup?string}
 * cases = ${lexer.caseCount}
 * table = ${lexer.table}
<#if lexer.table == "ecs" || lexer.table == "compressed">
 * ecs = ${lexer.ecsGroupCount}
</#if>
 * states = ${lexer.dfa.size}
 * max symbol value = ${lexer.maxSymbol}
 *
 * memory usage:
 * full table = ${(lexer.eof + 1) * lexer.dfa.size}
<#if lexer.table == "ecs" || lexer.table == "compressed">
 * ecs table = ${lexer.eof + 1 + lexer.ecsGroupCount * lexer.dfa.size}
</#if>
<#if lexer.table == "compressed">
 * next = ${lexer.dfa.next?size}
 * check = ${lexer.dfa.check?size}
<#if !lexer.dfa.default?has_content>
 * compressed table = ${lexer.eof + 1 + lexer.dfa.next?size + lexer.dfa.next?size}
<#else>
 * default = ${lexer.dfa.default?size}
<#if !lexer.dfa.meta?has_content>
 * compressed table = ${lexer.eof + 1 + lexer.dfa.next?size + lexer.dfa.next?size + lexer.dfa.default?size}
<#else>
 * meta = ${lexer.dfa.meta?size}
 * compressed table = ${lexer.eof + 1 + lexer.dfa.next?size + lexer.dfa.next?size + lexer.dfa.default?size + lexer.dfa.meta?size}
</#if>
</#if>
</#if>
<#else>
</#if>
 *
<#if parser?has_content>
 * parser properties:
 * symbols = ${parser.symbols?size}
 * max terminal = ${parser.maxTerminal}
 * used terminals = ${parser.usedTerminalCount}
 * non-terminals = ${parser.nonTerminalCount}
 * rules = ${parser.rules?size - 1}
 * shift/reduce conflicts = ${parser.shiftConflict}
 * reduce/reduce conflicts = ${parser.reduceConflict}
 *
 * memory usage:
 * ecs table = ${(parser.ecs?size + (parser.usedTerminalCount + parser.nonTerminalCount) * parser.dfa.size)}
<#if parser.table == "compressed">
 * compressed table = ${parser.ecs?size + parser.dfa.totalSize}
</#if>
</#if>
 */
}
