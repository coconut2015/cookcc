<#setting number_format=0>
<#macro array a><#list a as i><#if i_index &gt; 0>,</#if>${i}</#list></#macro>
<#macro intarray a>${a?javastring()}</#macro>
<#if code.fileheader?has_content>
${code.fileheader}
</#if>
<#if package?has_content>
package ${package};
</#if>

<#if unicode>
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
<#else>
import java.io.FileInputStream;
import java.io.InputStream;
</#if>
import java.io.IOException;

<#if parser?has_content>
import java.util.LinkedList;
import java.util.Vector;
</#if>

<#if code.classheader?has_content>
${code.classheader}
</#if>
<#if public?has_content && public?string == "true">public </#if>class ${ccclass}
{
<#if tokens?has_content>
<#list tokens as i>
	public final static int ${i} = ${lexer.eof + i_index + 1};
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
	</#if>
<#if parser.defaultReduce?has_content>
		private static char[] reduce = <@intarray parser.defaultReduce/>;
</#if>
		private static char[] lhs = <@intarray parser.lhs/>;
	}

	private final static class ParserState	// internal tracking tool
	{
		int token;			// the current token type
		Object value;		// the current value associated with token
		short state;		// the current scan state

		ParserState ()	// EOF token construction
		{
			this (0, null, (short)0);
		}
		ParserState (int token)
		{
			this (token, null, (short)0);
		}
		ParserState (int token, Object value)
		{
			this (token, value, (short)0);
		}
		ParserState (int token, Object value, short state)
		{
			this.token = token;
			this.value = value;
			this.state = state;
		}
	};

	// lookahead stack for the parser
	private final LinkedList _yyLookaheadStack = new LinkedList ();
	// state stack for the parser
	private final Vector _yyStateStack = new Vector (512, 512);
	// flag that indicates error
	private boolean _yyInError;
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

<#if lexer.bol>
	// we need to track beginning of line (BOL) status
	private boolean _yyIsNextBOL = true;
	private boolean _yyBOL = true;
</#if>
</#if>

<#if lexer?has_content>
<#if unicode>
	public void setInput (Reader reader)
	{
		_yyIs = reader;
	}

	public Reader getInput ()
	{
		return _yyIs;
	}
<#else>
	public void setInput (InputStream is)
	{
		_yyIs = is;
	}

	public InputStream getInput ()
	{
		return _yyIs;
	}
</#if>

<#if lexer.bol>
	public boolean isBOL ()
	{
		return _yyBOL;
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
	public String getText ()
	{
		if (_yyMatchStart == _yyTextStart)		// this is the case when we have EOF
			return null;
		return new String (_yyBuffer, _yyTextStart, _yyMatchStart - _yyTextStart);
	}

	/**
	 * Get the current text token's length.  Actions specified in the CookCC file
	 * can directly access the variable
	 * @return	the string token length
	 */
	public int getLength ()
	{
		return _yyLength;
	}

	/**
	 * Print the current string token to the standard output.
	 */
	public void echo ()
	{
		System.out.print (getText ());
	}

	/**
	 * Put all but n characters back to the input stream.  Be aware that calling
	 * yyLess (0) is allowed, but be sure to change the state some how to avoid
	 * an endless loop.
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

	protected boolean debugLexer (int matchedState, int accept)
	{
		System.err.println ("lexer: " + _yyBaseState + ", " + matchedState + ", " + accept + ", " + getText ());
		return true;
	}

	protected boolean debugLexerBackup (int backupState, String backupString)
	{
		System.err.println ("lexer backup: " + _yyBaseState + ", " + backupState + ", " + backupString);
		return true;
	}

	// read more data from the input
	protected boolean yyRefreshBuffer () throws IOException
	{
<#if unicode>
		if (_yyBuffer == null)
			_yyBuffer = new char[_yyBufferSize];
<#else>
		if (_yyBuffer == null)
			_yyBuffer = new byte[_yyBufferSize];
</#if>
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
	public void resetBuffer ()
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
<#if unicode>
			_yyBuffer = new char[bufferSize];
<#else>
			_yyBuffer = new byte[bufferSize];
</#if>
	}

	/**
	 * Call this function to start the scanning of the input.
	 *
	 * @return	a token or status value.
	 * @throws	IOException
	 *			in case of I/O error.
	 */
	public ${yytype} yyLex () throws IOException
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

<#if unicode>
		char[] buffer = _yyBuffer;
<#else>
		byte[] buffer = _yyBuffer;
</#if>

		while (true)
		{
			// initiate variables necessary for lookup
<#if lexer.bol>
			_yyBOL = _yyIsNextBOL;
			_yyIsNextBOL = false;
	<#if lexer.bolStates>
			int matchedState = _yyBaseState + (_yyBOL ? 1 : 0);
	<#else>
			int matchedState = _yyBaseState;
	</#if>
<#else>
			int matchedState = _yyBaseState;
</#if>

			int matchedLength = 0;

			int internalBufferEnd = _yyBufferEnd;
			int lookahead = _yyMatchStart;

<#if lexer.backup>
			int cc_backupMatchedState = matchedState;
			int cc_backupMatchedLength = 0;
</#if>

			// the DFA lookup
			while (true)
			{
				// check buffer status
				if (lookahead < internalBufferEnd)
				{
					// now okay to process the character
					int currentState;
<#if lexer.table == "full">
					currentState = cc_next[matchedState][buffer[lookahead]<#if !unicode> & 0xff</#if>];
<#elseif lexer.table == "ecs">
					currentState = cc_next[matchedState][cc_ecs[buffer[lookahead]<#if !unicode> & 0xff</#if>]];
<#else>
					int symbol = cc_ecs[buffer[lookahead]<#if !unicode> & 0xff</#if>];
					currentState = matchedState;
	<#if !lexer.dfa.default?has_content>
					if (cc_check[symbol + cc_base[matchedState]] == matchedState)
						currentState = cc_next[symbol + cc_base[matchedState]];
					else
						currentState = 0;
	<#elseif !lexer.dfa.error?has_content>
					if (cc_check[symbol + cc_base[matchedState]] == matchedState)
						currentState = cc_next[symbol + cc_base[matchedState]];
					else
						currentState = cc_default[matchedState];
	<#elseif !lexer.dfa.meta?has_content>
					while (cc_check[symbol + cc_base[currentState]] != currentState)
					{
						currentState = cc_default[currentState];
						if (currentState >= ${lexer.dfa.size})
							symbol = 0;
					}
					currentState = cc_next[symbol + cc_base[currentState]];
	<#else>
					while (cc_check[symbol + cc_base[currentState]] != currentState)
					{
						currentState = cc_default[currentState];
						if (currentState >= ${lexer.dfa.size})
							symbol = cc_meta[symbol];
					}
					currentState = cc_next[symbol + cc_base[currentState]];
	</#if>
</#if>

<#if lexer.backup>
					if (currentState == 0)
					{
	<#if debug>
						debugLexerBackup (matchedState, new String (_yyBuffer, _yyMatchStart, matchedLength));
	</#if>
						matchedState = cc_backupMatchedState;
						matchedLength = cc_backupMatchedLength;
						break;
					}
<#else>
					if (currentState == 0)
						break;
</#if>

					matchedState = currentState;
					++lookahead;
					++matchedLength;

<#if lexer.backup>
					if (cc_accept[matchedState] > 0)
					{
						cc_backupMatchedState = currentState;
						cc_backupMatchedLength = matchedLength;
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
						int currentState;
<#if lexer.table == "full">
						currentState = cc_next[matchedState][${lexer.eof}];
<#elseif lexer.table == "ecs">
						currentState = cc_next[matchedState][cc_ecs[${lexer.eof}]];
<#elseif lexer.table == "compressed">
						int symbol = cc_ecs[${lexer.eof}];
	<#if !lexer.dfa.default?has_content>
						if (cc_check[symbol + cc_base[matchedState]] == matchedState)
							currentState = cc_next[symbol + cc_base[matchedState]];
						else
							currentState = 0;
	<#elseif !lexer.dfa.error>
						if (cc_check[symbol + cc_base[matchedState]] == matchedState)
							currentState = cc_next[symbol + cc_base[matchedState]];
						else
							currentState = cc_default[matchedState];
	<#elseif !lexer.dfa.meta?has_content>
						currentState = matchedState;
						while (cc_check[symbol + cc_base[currentState]] != currentState)
						{
							currentState = cc_default[currentState];
							if (currentState >= ${lexer.dfa.size})
								symbol = 0;
						}
						currentState = cc_next[symbol + cc_base[currentState]];
	<#else>
						currentState = matchedState;
						while (cc_check[symbol + cc_base[currentState]] != currentState)
						{
							currentState = cc_default[currentState];
							if (currentState >= ${lexer.dfa.size})
								symbol = cc_meta[symbol];
						}
						currentState = cc_next[symbol + cc_base[currentState]];
	</#if>

</#if>
						if (currentState != 0)
							matchedState = currentState;
<#if lexer.backup>
						else
						{
							matchedState = cc_backupMatchedState;
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
			debugLexer (matchedState, cc_accept[matchedState]);
</#if>

			switch (cc_accept[matchedState])
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
	/**
	 * Call this function to start parsing.
	 *
	 * @return	0 if everything is okay, or 1 if an error occurred.
	 * @throws	IOException
	 *			in case of error
	 */
	public int yyParse () throws IOException
	{
		char[] cc_ecs = cc_parser.ecs;
<#if parser.table == "ecs">
		char[][] cc_next = cc_parser.next;
</#if>
		char[] cc_rule = cc_parser.rule;
<#if parser.defaultReduce?has_content>
		char[] cc_reduce = cc_parser.reduce;
</#if>
		char[] cc_lhs = cc_parser.lhs;

		LinkedList cc_lookaheadStack = _yyLookaheadStack;
		Vector cc_stateStack = _yyStateStack;

		if (cc_stateStack.size () == 0)
			cc_stateStack.add (new ParserState ());

		short cc_toState = 0;

		for (;;)
		{
			ParserState cc_lookahead;

			short cc_fromState;
			char cc_ch;

			//
			// check if there are any lookahead tokens on stack
			// if not, then call yyLex ()
			//
			if (cc_lookaheadStack.size () == 0)
			{
				_yyValue = null;
				int val = yyLex ();
				cc_lookahead = new ParserState (val, _yyValue);
				cc_lookaheadStack.add (cc_lookahead);
			}
			else
				cc_lookahead = (ParserState)cc_lookaheadStack.getLast ();

			cc_ch = cc_ecs[cc_lookahead.token];
			cc_fromState = ((ParserState)cc_stateStack.get (cc_stateStack.size () - 1)).state;
<#if parser.table == "ecs">
			cc_toState = (short)cc_next[cc_fromState][cc_ch];
<#else>
</#if>

<#if parser.defaultReduce?has_content>
			//
			// first check if can reduce in case of error
			//
			if (_yyInError && cc_lookahead.token != 1)
				cc_toState = (short)cc_reduce[((ParserState)cc_stateStack.get (cc_stateStack.size () - 1)).state];
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
<#if parser.defaultReduce?has_content>
			else if (cc_toState == 0 && !_yyInError &&
					 (cc_toState = (short)cc_reduce[cc_fromState]) == 0)
<#else>
			else if (cc_toState == 0)
</#if>
			{
				// error
				if (yyParseError (cc_ch))
					return 1;
				continue;
			}

			// now the reduce action
			short cc_ruleState = (short)-cc_toState;

			_yyArgStart = cc_stateStack.size () - cc_rule[cc_ruleState] - 1;
			//
			// find the state that said need this non-terminal
			//
			cc_fromState = ((ParserState)cc_stateStack.get (_yyArgStart)).state;

			//
			// find the state to goto after shifting the non-terminal
			// onto the stack.
			//
			if (cc_ruleState == 1)
				cc_toState = 0;			// reset the parser
			else
<#if parser.table == "ecs">
				cc_toState = (short)cc_next[cc_fromState][cc_lhs[cc_ruleState]];
<#else>
</#if>

			_yyValue = null;

			switch (cc_ruleState)
			{
				case 1:					// accept
					return 0;
<#list parser.cases as i>
<#list i.rhs as p>
				case ${p.caseValue}:	// ${i.rule} : ${p.terms}
				{
					${p.action}
				}
				case ${p.caseValue + parser.caseCount}: break;
</#list>
</#list>
				default:
					throw new IOException ("Internal error in ${ccclass} parser.");
			}

			//
			ParserState cc_reduced = new ParserState (-cc_ruleState, _yyValue, cc_toState);
			cc_stateStack.setSize (_yyArgStart + 1);
			cc_stateStack.add (cc_reduced);
		}

	}

	/**
	 * This function reports error and return true if critical error occurred, or
	 * false if the error has been successfully recovered.  IOException is an optional
	 * choice of reporting error.
	 *
	 * @param	ecsToken
	 *			this token is the ecs group id of the input token.
	 * @return	true if irrecoverable error occurred.  Or simply throw an IOException.
	 *			false if the parsing can be continued.
	 * @throws	IOException
	 *			in case of error.
	 */
	protected boolean yyParseError (char ecsToken) throws IOException
	{
		return true;
	}

	private Object yyGetValue (int arg)
	{
		return ((ParserState)_yyStateStack.get (_yyArgStart + arg)).value;
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
			tmpParser.setInput (new InputStreamReader (new FileInputStream (args[0])));
		<#else>
		if (args.length > 0)
			tmpParser.setInput (new FileInputStream (args[0]));
		</#if>
	</#if>

		tmpParser.yyParse ();
<#else>
		${ccclass} tmpLexer = new ${ccclass} ();
	<#if unicode>
		if (args.length > 0)
			tmpLexer.setInput (new InputStreamReader (new FileInputStream (args[0])));
	<#else>
		if (args.length > 0)
			tmpLexer.setInput (new FileInputStream (args[0]));
	</#if>

		tmpLexer.yyLex ();
</#if>
	}
</#if>

<#if code.default?has_content>
${code.default}
</#if>

/*
 * properties and statistics:
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
</#if>
 */
}
