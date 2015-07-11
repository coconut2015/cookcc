/*
 * CookCC Copyright (c) 2008-2009, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <copyright holder> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <copyright holder> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.yuanheng.cookcc;

import java.io.IOException;
import java.io.Reader;

/**
 * This class is merely a place holder.
 *
 * @author Heng Yuan
 * @version $Id$
 * @since 0.3
 */
public abstract class CookCCChar
{
	/**
	 * Check whether or not the current token at the beginning of the line.  This
	 * function is not accurate if the user does multi-line pattern matching or
	 * have trail contexts at the end of the line.
	 *
	 * @return	whether or not the current token is at the beginning of the line.
	 */
	protected boolean isBOL ()
	{
		return false;
	}

	/**
	 * Set whether or not the next token at the beginning of the line.
	 *
	 * @param	bol
	 *			the bol status
	 */
	public void setBOL (boolean bol)
	{
	}

	/**
	 * Print the current string token to the standard output.
	 */
	public void echo ()
	{
	}

	protected boolean debugLexer (int baseState, int matchedState, int accept)
	{
		return true;
	}

	protected boolean debugLexerBackup (int baseState, int backupState, String backupString)
	{
		return true;
	}

	protected boolean debugParser (int fromState, int toState, int reduceState, String reduceStateName, int ecsToken, String tokenName)
	{
		return true;
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
		return null;
	}

	/**
	 * Get the current text token's length.  Actions specified in the CookCC file
	 * can directly access the variable _yyLength.
	 *
	 * @return	the string token length
	 */
	public int yyLength ()
	{
		return 0;
	}

	/**
	 * Call this function to start the scanning of the input.
	 *
	 * @return	a token or status value.
	 * @throws java.io.IOException
	 *			in case of I/O error.
	 */
	protected int yyLex () throws IOException
	{
		return 0;
	}

	/**
	 * Reset the internal buffer.
	 */
	public void yyResetBuffer ()
	{
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
	 * Reset the internal state to reuse the same parser.
	 *
	 * Note, it does not change the buffer size, the input buffer, and the input stream.
	 *
	 * Making this function protected so that it can be enabled only if the child class
	 * decides to make it public.
	 */
	protected void reset ()
	{
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
	}

	/**
	 * This function is used to change the initial state for the lexer.
	 *
	 * @param	state
	 *			the state value
	 */
	protected void begin (int state)
	{
	}

	/**
	 * This function is used to change the initial state for the lexer.
	 *
	 * @param	state
	 *			the name of the state
	 */
	protected void begin (String state)
	{
	}

	/**
	 * Push the current state onto lexer state onto stack and
	 * begin the new state specified by the user.
	 *
	 * @param	newState
	 * 			the new state.
	 */
	protected void yyPushLexerState (int newState)
	{
	}

	/**
	 * Push the current state onto lexer state onto stack and
	 * begin the new state specified by the user.
	 *
	 * @param	newState
	 * 			the new state.
	 */
	protected void yyPushLexerState (String newState)
	{
	}

	/**
	 * Restore the previous lexer state.
	 */
	protected void yyPopLexerState ()
	{
	}

	/**
	 * Call this function to start parsing.
	 *
	 * @return 0 if everything is okay, or 1 if an error occurred.
	 * @throws IOException in case of error
	 */
	public int yyParse () throws IOException
	{
		return 0;
	}

	protected void yyClearError ()
	{
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
		return null;
	}

	/**
	 * Set the object value for the current non-terminal being reduced.
	 *
	 * @param	value
	 * 			the object value for the current non-terminal.
	 */
	protected void yySetValue (Object value)
	{
	}

	/**
	 * Switch the current input to the old input on stack.  The current input
	 * and its buffered characters are all switch to the old ones.
	 */
	public void yyPopInput ()
	{
	}

	/**
	 * Obtain the number of input objects on the stack.
	 *
	 * @return	the number of input objects on the stack.
	 */
	public int yyInputStackSize ()
	{
		return 0;
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
		return true;
	}

	/**
	 * Set the current input.
	 *
	 * @param	reader
	 *			the new input.
	 */
	public void setInput (Reader reader)
	{
	}

	/**
	 * Obtain the current input.
	 *
	 * @return	the current input
	 */
	public Reader getInput ()
	{
		return null;
	}

	/**
	 * Switch the current input to the new input.  The old input and already
	 * buffered characters are pushed onto the stack.
	 *
	 * @param	reader
	 *			the new input
	 */
	public void yyPushInput (Reader reader)
	{
	}
}
