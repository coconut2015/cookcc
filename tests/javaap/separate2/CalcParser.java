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

import java.util.HashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

import org.yuanheng.cookcc.*;

/**
 * This calculator example is adapted from "A Compact Guide to Lex & Yacc"
 * (http://epaperpress.com/lexandyacc/).
 *
 * @author Heng Yuan
 * @version $Id$
 */
@CookCCOption (parserTable="compressed", tokenClass="Token")
public class CalcParser extends CalcParserGen
{
	private final HashMap<String, Node> m_varMap = new HashMap<String, Node> ();

	private final CalcLexer m_lexer = new CalcLexer ();

	public void setInput(InputStream is)
	{
		m_lexer.setInput (is);
	}

	public int yyLex () throws IOException
	{
		int token = m_lexer.yyLex ();
		return token;
	}

	protected Object yyValue ()
	{
		return m_lexer.yyValue ();
	}

	////////////////////////////////////////////////////////////////////////
	//
	// Parser section
	//
	////////////////////////////////////////////////////////////////////////

	/**
	 * If the return type of the function is int, it is considered as existing
	 * the parser with the exit value.
	 *
	 * @return	the exit value from the parser, with 0 meaning everything okay.
	 */
	@Rule (lhs = "program", rhs = "function")
	protected int parseProgram ()
	{
		return 0;
	}

	/**
	 * If the return type of the function is void, then _yyValue is null.
	 *
	 * @param	node
	 * 			the statement node
	 */
	@Rule (lhs = "function", rhs = "function stmt", args = "2")
	protected void parseFunction (Node node)
	{
		interpret (node);
	}

	@Rule (lhs = "function", rhs = "")
	protected void parseFunction ()
	{
	}

	/**
	 * If the return type of the function is an Object type, it is considered returning
	 * a value to be set as _yyValue.
	 *
	 * @return	the statement node
	 */
	@Rule (lhs = "stmt", rhs = "SEMICOLON")
	protected Node parseStmt ()
	{
		return new SemiColonNode ();
	}

	@Rule (lhs = "stmt", rhs = "expr SEMICOLON", args = "1")
	protected Node parseStmt (Node expr)
	{
		return expr;
	}

	@Rule (lhs = "stmt", rhs = "PRINT expr SEMICOLON", args = "2")
	protected Node parsePrintStmt (Node expr)
	{
		return new PrintNode (expr);
	}

	@Rule (lhs = "stmt", rhs = "VARIABLE ASSIGN expr SEMICOLON", args = "1 3")
	protected Node parseAssign (String var, Node expr)
	{
		return new AssignNode (var, expr);
	}

	@Rule (lhs = "stmt", rhs = "WHILE '(' expr ')' stmt", args = "3 5")
	protected Node parseWhile (Node expr, Node stmt)
	{
		return new WhileNode (expr, stmt);
	}

	@Rule (lhs = "stmt", rhs = "IF '(' expr ')' stmt", args = "3 5", precedence = "IFX")
	protected Node parseIf (Node expr, Node stmt)
	{
		return new IfNode (expr, stmt, null);
	}

	@Rule (lhs = "stmt", rhs = "IF '(' expr ')' stmt ELSE stmt", args = "3 5 7")
	protected Node parseIf (Node expr, Node stmt, Node elseStmt)
	{
		return new IfNode (expr, stmt, elseStmt);
	}

	@Rule (lhs = "stmt", rhs = "'{' stmt_list '}'", args = "2")
	protected Node parseBlock (Node stmtList)
	{
		return stmtList;
	}

	@Rule (lhs = "stmt_list", rhs = "stmt", args = "1")
	protected Node parseStmtList (Node stmt)
	{
		return stmt;
	}

	@Rule (lhs = "stmt_list", rhs = "stmt_list stmt", args = "1 2")
	protected Node parseStmtList (Node stmtList, Node stmt)
	{
		return new SemiColonNode (stmtList, stmt);
	}

	@Rule (lhs = "expr", rhs = "INTEGER", args = "1")
	protected Node parseExpr (Integer value)
	{
		return new ConstantNode (value);
	}

	@Rule (lhs = "expr", rhs = "VARIABLE", args = "1")
	protected Node parseExpr (String var)
	{
		IdNode idNode = (IdNode)m_varMap.get (var);
		if (idNode == null)
		{
			idNode = new IdNode (var);
			m_varMap.put (var, idNode);
		}
		return idNode;
	}

	@Rule (lhs = "expr", rhs = "SUB expr", args = "2", precedence = "UMINUS")
	protected Node parseUminus (Node expr)
	{
		return new ExprNode (Token.UMINUS, expr, null);
	}

	@Rule (lhs = "expr", rhs = "expr ADD expr", args = "1 3")
	protected Node parseAdd (Node expr1, Node expr2)
	{
		return new ExprNode (Token.ADD, expr1, expr2);
	}

	@Rule (lhs = "expr", rhs = "expr SUB expr", args = "1 3")
	protected Node parseSub (Node expr1, Node expr2)
	{
		return new ExprNode (Token.SUB, expr1, expr2);
	}

	@Rule (lhs = "expr", rhs = "expr MUL expr", args = "1 3")
	protected Node parseMul (Node expr1, Node expr2)
	{
		return new ExprNode (Token.MUL, expr1, expr2);
	}

	@Rule (lhs = "expr", rhs = "expr DIV expr", args = "1 3")
	protected Node parseDiv (Node expr1, Node expr2)
	{
		return new ExprNode (Token.DIV, expr1, expr2);
	}

	@Rule (lhs = "expr", rhs = "expr LT expr", args = "1 3")
	protected Node parseLt (Node expr1, Node expr2)
	{
		return new ExprNode (Token.LT, expr1, expr2);
	}

	@Rule (lhs = "expr", rhs = "expr GT expr", args = "1 3")
	protected Node parseGt (Node expr1, Node expr2)
	{
		return new ExprNode (Token.GT, expr1, expr2);
	}

	@Rule (lhs = "expr", rhs = "expr LE expr", args = "1 3")
	protected Node parseLe (Node expr1, Node expr2)
	{
		return new ExprNode (Token.LE, expr1, expr2);
	}

	@Rule (lhs = "expr", rhs = "expr GE expr", args = "1 3")
	protected Node parseGe (Node expr1, Node expr2)
	{
		return new ExprNode (Token.GE, expr1, expr2);
	}

	@Rule (lhs = "expr", rhs = "expr NE expr", args = "1 3")
	protected Node parseNe (Node expr1, Node expr2)
	{
		return new ExprNode (Token.NE, expr1, expr2);
	}

	@Rule (lhs = "expr", rhs = "expr EQ expr", args = "1 3")
	protected Node parseEq (Node expr1, Node expr2)
	{
		return new ExprNode (Token.EQ, expr1, expr2);
	}

	@Rule (lhs = "expr", rhs = "'(' LT ')'", args = "2")
	protected Node parseParen (Node expr)
	{
		return expr;
	}

	////////////////////////////////////////////////////////////////////////
	//
	// Supporting classes and the interpreter
	//
	////////////////////////////////////////////////////////////////////////

	static class Node
	{
	}

	static class IdNode extends Node
	{
		String name;
		int value;

		public IdNode (String name)
		{
			this.name = name;
		}
	}

	static class ConstantNode extends Node
	{
		int value;

		public ConstantNode (Integer value)
		{
			this.value = value.intValue ();
		}
	}

	static class OpNode extends Node
	{
		final Token type;

		public OpNode (Token type)
		{
			this.type = type;
		}
	}

	static class SemiColonNode extends OpNode
	{
		Node stmt1;
		Node stmt2;

		public SemiColonNode ()
		{
			super (Token.SEMICOLON);
		}

		public SemiColonNode (Node stmt1, Node stmt2)
		{
			super (Token.SEMICOLON);
			this.stmt1 = stmt1;
			this.stmt2 = stmt2;
		}
	}

	static class PrintNode extends OpNode
	{
		Node expr;

		public PrintNode (Node expr)
		{
			super (Token.PRINT);
			this.expr = expr;
		}
	}

	static class AssignNode extends OpNode
	{
		String var;
		Node expr;

		public AssignNode (String var, Node expr)
		{
			super (Token.ASSIGN);
			this.var = var;
			this.expr = expr;
		}
	}

	static class WhileNode extends OpNode
	{
		Node expr;
		Node stmt;

		public WhileNode (Node expr, Node stmt)
		{
			super (Token.WHILE);
			this.expr = expr;
			this.stmt = stmt;
		}
	}

	static class IfNode extends OpNode
	{
		Node expr;
		Node stmt1;
		Node stmt2;

		public IfNode (Node expr, Node stmt1, Node stmt2)
		{
			super (Token.IF);
			this.expr = expr;
			this.stmt1 = stmt1;
			this.stmt2 = stmt2;
		}
	}

	static class ExprNode extends OpNode
	{
		Node expr1;
		Node expr2;

		public ExprNode (Token type, Node expr1, Node expr2)
		{
			super (type);
			this.expr1 = expr1;
			this.expr2 = expr2;
		}
	}

	int interpret (Node p)
	{
		if (p == null)
			return 0;
		if (p instanceof ConstantNode)
			return ((ConstantNode)p).value;
		if (p instanceof IdNode)
			return ((IdNode)p).value;
		// should be all OpNodes from this point on

		switch (((OpNode)p).type)
		{
			case WHILE:
				while (interpret (((WhileNode)p).expr) > 0)
					interpret (((WhileNode)p).stmt);
				return 0;
			case IF:
				if (interpret (((IfNode)p).expr) > 0)
					interpret (((IfNode)p).stmt1);
				else
					interpret (((IfNode)p).stmt2);
				return 0;
			case PRINT:
				System.out.println (interpret (((PrintNode)p).expr));
				return 0;
			case SEMICOLON:
				interpret (((SemiColonNode)p).stmt1);
				return interpret (((SemiColonNode)p).stmt2);
			case ASSIGN:
			{
				String var = ((AssignNode)p).var;
				IdNode idNode = (IdNode)m_varMap.get (var);
				if (idNode == null)
				{
					idNode = new IdNode (var);
					m_varMap.put (var, idNode);
				}
				return idNode.value = interpret (((AssignNode)p).expr);
			}
			case UMINUS:
				return -interpret (((ExprNode)p).expr1);
			case ADD:
				return interpret (((ExprNode)p).expr1) + interpret (((ExprNode)p).expr2);
			case SUB:
				return interpret (((ExprNode)p).expr1) - interpret (((ExprNode)p).expr2);
			case MUL:
				return interpret (((ExprNode)p).expr1) * interpret (((ExprNode)p).expr2);
			case DIV:
				return interpret (((ExprNode)p).expr1) / interpret (((ExprNode)p).expr2);
			case LT:
				return interpret (((ExprNode)p).expr1) < interpret (((ExprNode)p).expr2) ? 1 : 0;
			case GT:
				return interpret (((ExprNode)p).expr1) > interpret (((ExprNode)p).expr2) ? 1 : 0;
			case GE:
				return interpret (((ExprNode)p).expr1) >= interpret (((ExprNode)p).expr2) ? 1 : 0;
			case LE:
				return interpret (((ExprNode)p).expr1) <= interpret (((ExprNode)p).expr2) ? 1 : 0;
			case NE:
				return interpret (((ExprNode)p).expr1) != interpret (((ExprNode)p).expr2) ? 1 : 0;
			case EQ:
				return interpret (((ExprNode)p).expr1) == interpret (((ExprNode)p).expr2) ? 1 : 0;
		}
		return 0;
	}

	public static void main (String[] args) throws IOException
	{
		CalcParser calc = new CalcParser ();
		if (args.length > 0)
			calc.setInput (new FileInputStream (args[0]));

		if (calc.yyParse () > 0)
		{
			System.err.println ("Calculator: errors in input.");
			System.exit (1);
		}
	}
}
