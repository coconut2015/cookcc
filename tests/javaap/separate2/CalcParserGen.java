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

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Heng Yuan
 * @version $Id$
 */
abstract class CalcParserGen
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


	// an internal class for lazy initiation
	private final static class cc_parser
	{
		private final static char[] rule = ("\000\001\001\002\000\001\002\003\004\005\005\007\003\001\002\001\001\002\003\003\003\003\003\003\003\003\003\003\003").toCharArray ();
		private final static char[] ecs = ("\000\001\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\004\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\005\002\006\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\007\010\011\012\013\014\015\002\016\017\020\021\022\023\024\025\026\027\030\002").toCharArray ();
		private final static char[] base = ("\022\001\042\000\011\u0199\u00ee\002\007\000\104\000\062\u01a5\010\u00d4\011\000\000\000\002\003\u0103\126\000\000\000\000\000\000\000\000\000\000\u0118\150\u00e6\004\000\020\172\u012d\u0135\u013f\u0147\u0151\u0159\u0163\u016d\u0177\u018c\u008c\000\000\u009e\u00b0\000\u00c2\u01be\u01c1\u01d8\u01c1\u01d8\u01bc\u01d8\u01d8\u01d8\u01d8\u01bc\u01d8\u01c2\u01d8\u01d8\u01d8\u01d8\u01c4\u01c4\u01c5\u01c6\u01d8\u01d8\u01d8\u01d8\u01c7\u01c8\u01c9\u01ca\u01cb\u01cc\u01cd\u01ce\u01cf\u01d0\u01d8\u01d8\u01d8\u01d8\u01d8\u01d8\u01d8\u01d8\u01d8\u01d8\u01d8\u01d8\u01d8\u01d8\u01d8\u01d8\u01d8\u01d8\u01d2\u01d3\u01d8\u01d8\u01d4\u01d8\u01d8").toCharArray ();
		private final static char[] next = ("\000\uffff\000\003\064\022\ufff0\024\006\000\023\000\042\000\000\043\050\063\ufffc\016\065\ufffc\013\ufffc\000\ufffc\ufffc\ufffc\ufffc\ufffc\000\ufffc\000\000\ufffe\000\000\003\000\004\ufffc\005\006\007\010\011\000\012\000\000\ufffd\000\000\ufffd\000\ufffd\013\ufffd\ufffd\ufffd\ufffd\ufffd\000\ufffd\000\000\000\000\ufffb\000\000\ufffb\ufffd\ufffb\ufffb\ufffb\ufffb\ufffb\ufffb\ufffb\000\ufffb\ufffb\000\000\000\ufffa\000\000\ufffa\ufffb\ufffa\ufffa\ufffa\ufffa\ufffa\ufffa\ufffa\000\ufffa\ufffa\000\000\000\ufff4\000\000\ufff4\ufffa\ufff4\ufff4\ufff4\ufff4\ufff4\ufff4\ufff4\000\ufff4\ufff4\000\000\000\ufff9\000\000\ufff9\ufff4\ufff9\ufff9\ufff9\ufff9\ufff9\ufff9\ufff9\000\ufff9\ufff9\000\000\000\ufff8\000\000\ufff8\ufff9\ufff8\ufff8\ufff8\ufff8\ufff8\ufff8\ufff8\000\ufff8\ufff8\000\000\000\ufff7\000\000\ufff7\ufff8\ufff7\ufff7\ufff7\ufff7\ufff7\ufff7\ufff7\000\ufff7\ufff7\000\000\000\ufff6\000\000\ufff6\ufff7\ufff6\ufff6\ufff6\ufff6\ufff6\ufff6\ufff6\000\ufff6\070\000\000\000\ufff5\000\000\ufff5\ufff6\ufff5\ufff5\ufff5\ufff5\ufff5\ufff5\ufff5\000\ufff5\ufff5\000\000\000\000\000\000\ufff3\ufff5\ufff3\ufff3\ufff3\ufff3\ufff3\ufff3\ufff3\000\ufff3\000\000\000\000\000\000\000\ufff2\ufff3\ufff2\ufff2\ufff2\ufff2\ufff2\ufff2\ufff2\ufff1\ufff2\000\000\000\000\000\000\000\ufff1\ufff2\ufff1\ufff1\ufff1\ufff1\ufff1\ufff1\ufff1\ufff1\ufff1\ufff1\uffef\000\000\000\000\000\000\000\000\uffef\000\uffef\uffef\uffef\uffef\uffef\uffef\uffef\uffef\uffef\uffef\uffe4\000\000\000\000\000\000\000\000\uffe4\000\uffe4\uffe4\uffe4\uffe4\uffe4\uffe4\uffe4\uffe4\uffe4\uffe4\uffe7\000\000\000\000\000\000\000\uffe8\uffe7\000\uffe7\uffe7\uffe7\uffe7\uffe7\uffe7\uffe8\uffe5\uffe8\uffe8\uffe8\uffe8\uffe8\uffe8\000\uffe6\uffe5\000\uffe5\uffe5\uffe5\uffe5\uffe5\uffe5\uffe6\uffea\uffe6\uffe6\uffe6\uffe6\uffe6\uffe6\000\uffe9\uffea\000\uffea\uffea\uffea\uffea\uffea\uffea\uffe9\uffee\uffe9\uffe9\uffe9\uffe9\uffe9\uffe9\000\000\uffee\uffed\uffee\uffee\uffee\uffee\uffee\uffee\uffee\uffee\uffed\uffec\uffed\uffed\uffed\uffed\uffed\uffed\uffed\uffed\uffec\000\uffec\uffec\uffec\uffec\uffec\uffec\uffec\uffec\uffec\uffec\uffeb\000\000\000\000\000\000\000\000\uffeb\000\uffeb\uffeb\uffeb\uffeb\uffeb\uffeb\uffeb\uffeb\uffeb\uffeb\021\ufff0\000\ufff0\ufff0\ufff0\ufff0\ufff0\ufff0\ufff0\ufff0\ufff0\ufff0\027\000\030\031\032\033\034\035\036\037\040\041\000\017\025\020\001\002\014\015\026\044\045\046\047\051\052\053\054\055\056\057\060\061\062\066\067\071\000\000\000\000\000\000").toCharArray ();
		private final static char[] check = ("\003\001\007\011\046\007\024\011\011\004\010\016\016\046\024\020\025\045\000\003\047\000\011\000\072\000\000\000\000\000\072\000\072\072\002\072\072\002\072\002\000\002\002\002\002\002\072\002\072\072\014\072\072\014\072\014\002\014\014\014\014\014\072\014\072\072\072\072\012\072\072\012\014\012\012\012\012\012\012\012\072\012\012\072\072\072\027\072\072\027\012\027\027\027\027\027\027\027\072\027\027\072\072\072\043\072\072\043\027\043\043\043\043\043\043\043\072\043\043\072\072\072\050\072\072\050\043\050\050\050\050\050\050\050\072\050\050\072\072\072\063\072\072\063\050\063\063\063\063\063\063\063\072\063\063\072\072\072\066\072\072\066\063\066\066\066\066\066\066\066\072\066\066\072\072\072\067\072\072\067\066\067\067\067\067\067\067\067\072\067\067\072\072\072\071\072\072\071\067\071\071\071\071\071\071\071\072\071\071\072\072\072\072\072\072\017\071\017\017\017\017\017\017\017\072\017\072\072\072\072\072\072\072\044\017\044\044\044\044\044\044\044\006\044\072\072\072\072\072\072\072\006\044\006\006\006\006\006\006\006\006\006\006\026\072\072\072\072\072\072\072\072\026\072\026\026\026\026\026\026\026\026\026\026\042\072\072\072\072\072\072\072\072\042\072\042\042\042\042\042\042\042\042\042\042\051\072\072\072\072\072\072\072\052\051\072\051\051\051\051\051\051\052\053\052\052\052\052\052\052\072\054\053\072\053\053\053\053\053\053\054\055\054\054\054\054\054\054\072\056\055\072\055\055\055\055\055\055\056\057\056\056\056\056\056\056\072\072\057\060\057\057\057\057\057\057\057\057\060\061\060\060\060\060\060\060\060\060\061\072\061\061\061\061\061\061\061\061\061\061\062\072\072\072\072\072\072\072\072\062\072\062\062\062\062\062\062\062\062\062\062\005\005\072\005\005\005\005\005\005\005\005\005\005\015\072\015\015\015\015\015\015\015\015\015\015\072\077\104\077\073\073\075\075\106\113\114\115\116\123\124\125\126\127\130\131\132\133\134\157\160\163\165\165\165\165\165\165").toCharArray ();
		private final static char[] defaults = ("\072\072\000\001\002\003\003\001\007\007\000\011\000\003\007\004\004\011\011\011\005\015\003\000\011\011\011\011\011\011\011\011\011\011\003\000\004\015\015\046\000\015\015\015\015\015\015\015\015\003\003\000\004\004\000\000\004\000\072").toCharArray ();
		private final static char[] gotoDefault = ("\165\165\165\165\075\165\165\165\165\165\165\165\165\165\165\165\075\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\075\075\165\165\075\165\165").toCharArray ();
		private final static char[] lhs = ("\000\031\032\033\033\034\034\034\034\034\034\034\034\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035").toCharArray ();
	}

	private final static class cc_parser_symbol
	{
		private final static String[] symbols =
		{
			"VARIABLE","INTEGER","WHILE","IF","PRINT","ASSIGN","SEMICOLON","IFX","ELSE","GE","LE","EQ","NE","LT","GT","ADD","SUB","MUL","DIV","UMINUS","@start","program","function","stmt","expr","stmt_list"
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

	/**
	 * Return the object associate with the token.  This function is only generated
	 * when the lexer is not specified.
	 *
	 * @return	the object assoicated with the token.
	 */
	protected Object yyValue ()
	{
		return null;
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
	public int yyParse () throws IOException
	{
		char[] cc_ecs = cc_parser.ecs;
		char[] cc_next = cc_parser.next;
		char[] cc_check = cc_parser.check;
		char[] cc_base = cc_parser.base;
		char[] cc_default = cc_parser.defaults;
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
				int val = yyLex ();
				_yyValue = yyValue ();
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
				if (cc_toState >= 58)
					cc_symbol = 0;
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
				cc_toState = cc_fromState + 59;
				int cc_tmpCh = cc_lhs[cc_ruleState] - 25;
				while (cc_check[cc_tmpCh + cc_base[cc_toState]] != cc_toState)
					cc_toState = cc_gotoDefault[cc_toState - 59];
				cc_toState = cc_next[cc_tmpCh + cc_base[cc_toState]];
			}

			_yyValue = null;

			switch (cc_ruleState)
			{
				case 1:					// accept
					return 0;
				case 2:	// program : function
				{
					return m_this.parseProgram ();
				}
				case 31: break;
				case 3:	// function : function stmt
				{
					m_this.parseFunction ((CalcParser.Node)yyGetValue (2));
				}
				case 32: break;
				case 4:	// function : 
				{
					m_this.parseFunction ();
				}
				case 33: break;
				case 5:	// stmt : SEMICOLON
				{
					_yyValue = m_this.parseStmt ();
				}
				case 34: break;
				case 6:	// stmt : expr SEMICOLON
				{
					_yyValue = m_this.parseStmt ((CalcParser.Node)yyGetValue (1));
				}
				case 35: break;
				case 7:	// stmt : PRINT expr SEMICOLON
				{
					_yyValue = m_this.parsePrintStmt ((CalcParser.Node)yyGetValue (2));
				}
				case 36: break;
				case 8:	// stmt : VARIABLE ASSIGN expr SEMICOLON
				{
					_yyValue = m_this.parseAssign ((java.lang.String)yyGetValue (1), (CalcParser.Node)yyGetValue (3));
				}
				case 37: break;
				case 9:	// stmt : WHILE '(' expr ')' stmt
				{
					_yyValue = m_this.parseWhile ((CalcParser.Node)yyGetValue (3), (CalcParser.Node)yyGetValue (5));
				}
				case 38: break;
				case 10:	// stmt : IF '(' expr ')' stmt
				{
					_yyValue = m_this.parseIf ((CalcParser.Node)yyGetValue (3), (CalcParser.Node)yyGetValue (5));
				}
				case 39: break;
				case 11:	// stmt : IF '(' expr ')' stmt ELSE stmt
				{
					_yyValue = m_this.parseIf ((CalcParser.Node)yyGetValue (3), (CalcParser.Node)yyGetValue (5), (CalcParser.Node)yyGetValue (7));
				}
				case 40: break;
				case 12:	// stmt : '{' stmt_list '}'
				{
					_yyValue = m_this.parseBlock ((CalcParser.Node)yyGetValue (2));
				}
				case 41: break;
				case 13:	// stmt_list : stmt
				{
					_yyValue = m_this.parseStmtList ((CalcParser.Node)yyGetValue (1));
				}
				case 42: break;
				case 14:	// stmt_list : stmt_list stmt
				{
					_yyValue = m_this.parseStmtList ((CalcParser.Node)yyGetValue (1), (CalcParser.Node)yyGetValue (2));
				}
				case 43: break;
				case 15:	// expr : INTEGER
				{
					_yyValue = m_this.parseExpr ((java.lang.Integer)yyGetValue (1));
				}
				case 44: break;
				case 16:	// expr : VARIABLE
				{
					_yyValue = m_this.parseExpr ((java.lang.String)yyGetValue (1));
				}
				case 45: break;
				case 17:	// expr : SUB expr
				{
					_yyValue = m_this.parseUminus ((CalcParser.Node)yyGetValue (2));
				}
				case 46: break;
				case 18:	// expr : expr ADD expr
				{
					_yyValue = m_this.parseAdd ((CalcParser.Node)yyGetValue (1), (CalcParser.Node)yyGetValue (3));
				}
				case 47: break;
				case 19:	// expr : expr SUB expr
				{
					_yyValue = m_this.parseSub ((CalcParser.Node)yyGetValue (1), (CalcParser.Node)yyGetValue (3));
				}
				case 48: break;
				case 20:	// expr : expr MUL expr
				{
					_yyValue = m_this.parseMul ((CalcParser.Node)yyGetValue (1), (CalcParser.Node)yyGetValue (3));
				}
				case 49: break;
				case 21:	// expr : expr DIV expr
				{
					_yyValue = m_this.parseDiv ((CalcParser.Node)yyGetValue (1), (CalcParser.Node)yyGetValue (3));
				}
				case 50: break;
				case 22:	// expr : expr LT expr
				{
					_yyValue = m_this.parseLt ((CalcParser.Node)yyGetValue (1), (CalcParser.Node)yyGetValue (3));
				}
				case 51: break;
				case 23:	// expr : expr GT expr
				{
					_yyValue = m_this.parseGt ((CalcParser.Node)yyGetValue (1), (CalcParser.Node)yyGetValue (3));
				}
				case 52: break;
				case 24:	// expr : expr LE expr
				{
					_yyValue = m_this.parseLe ((CalcParser.Node)yyGetValue (1), (CalcParser.Node)yyGetValue (3));
				}
				case 53: break;
				case 25:	// expr : expr GE expr
				{
					_yyValue = m_this.parseGe ((CalcParser.Node)yyGetValue (1), (CalcParser.Node)yyGetValue (3));
				}
				case 54: break;
				case 26:	// expr : expr NE expr
				{
					_yyValue = m_this.parseNe ((CalcParser.Node)yyGetValue (1), (CalcParser.Node)yyGetValue (3));
				}
				case 55: break;
				case 27:	// expr : expr EQ expr
				{
					_yyValue = m_this.parseEq ((CalcParser.Node)yyGetValue (1), (CalcParser.Node)yyGetValue (3));
				}
				case 56: break;
				case 28:	// expr : '(' LT ')'
				{
					_yyValue = m_this.parseParen ((CalcParser.Node)yyGetValue (2));
				}
				case 57: break;
				default:
					throw new IOException ("Internal error in CalcParserGen parser.");
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
			_yySymbolArray = new Integer[275 + 6 + 1];
		if (symbol < 0 || symbol >= _yySymbolArray.length)
			return new Integer (symbol);
		if (_yySymbolArray[symbol] == null)
			_yySymbolArray[symbol] = new Integer (symbol);
		return _yySymbolArray[symbol];
	}

	private final CalcParser m_this = (CalcParser)this;



	protected static InputStream open (String file) throws IOException
	{
		return new FileInputStream (file);
	}


/*
 * lexer properties:
 * unicode = false
 *
 * parser properties:
 * symbols = 26
 * max terminal = 275
 * used terminals = 25
 * non-terminals = 6
 * rules = 28
 * shift/reduce conflicts = 0
 * reduce/reduce conflicts = 0
 *
 * memory usage:
 * ecs table = 2074
 * compressed table = 1468
 */
}
