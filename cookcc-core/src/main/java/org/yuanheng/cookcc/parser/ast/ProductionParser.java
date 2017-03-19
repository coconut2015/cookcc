package org.yuanheng.cookcc.parser.ast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.yuanheng.cookcc.*;
import org.yuanheng.cookcc.exception.ParserException;
import org.yuanheng.cookcc.lexer.CCL;

/**
 * This class scans a production and generates any internal generated
 * rules if necessary.
 *
 * @author	Heng Yuan
 */
@CookCCOption
public class ProductionParser extends ProductionScanner
{
	@CookCCToken
	static enum Token
	{
		SYMBOL,
		LPAREN,
		RPAREN,
		@TokenGroup (type = TokenType.LEFT)
		OR,
		@TokenGroup (type = TokenType.LEFT)
		QUESTION,
		STAR,
		PLUS
	}

	private SymbolLibrary m_lib;
	private long m_lineNumber;
	private ArrayList<Symbol> m_rule;

	public ArrayList<Symbol> parse (SymbolLibrary lib, long lineNumber, String rule) throws IOException
	{
		m_rule = null;
		reset ();

		m_lib = lib;
		m_lineNumber = lineNumber;

		setInput (new ByteArrayInputStream (rule.getBytes ()));
		yyParse ();

		return m_rule;
	}

	////////////////////////////////////////////////////////////////////////
	//
	// Lexer section
	//
	////////////////////////////////////////////////////////////////////////

	@Lexs (patterns = {
		@Lex (pattern = "'|'", token = "OR"),
		@Lex (pattern = "'('", token = "LPAREN"),
		@Lex (pattern = "')'", token = "RPAREN"),
		@Lex (pattern = "'?'", token = "QUESTION"),
		@Lex (pattern = "'*'", token = "STAR"),
		@Lex (pattern = "'+'", token = "PLUS")
	})
	void scanOperator ()
	{
	}

	@Lex (pattern = "[a-zA-Z_][a-zA-Z_0-9]*", token = "SYMBOL")
	Symbol scanSymbol ()
	{
		return m_lib.getSymbol (yyText ());
	}

	@Lexs (patterns = {
		@Lex (pattern = "['][^'\\\\\\r\\n][']", token = "SYMBOL"),
		@Lex (pattern = "[']\\\\.[']", token = "SYMBOL"),
		@Lex (pattern = "[']\\\\x([0-9a-fA-F]{1,2})[']", token = "SYMBOL"),
		@Lex (pattern = "[']\\\\([0-9]{1,3})[']", token = "SYMBOL")
	})
	Symbol scanTerminal () throws ParserException
	{
		String terminal = yyText ();
		String str = terminal.substring (1, terminal.length () - 1);
		// only a single character
		if (str.length () == 1)
		{
			return m_lib.getSymbol (str.charAt (0));
		}
		if (str.charAt (0) != '\\')
			throw new ParserException (m_lineNumber, "unknown terminal: " + terminal);
		try
		{
			int[] pos = new int[1];
			return m_lib.getSymbol (CCL.esc (str, pos));
		}
		catch (Exception ex)
		{
			throw new ParserException (m_lineNumber, ex.getMessage ());
		}
	}

	@Lex (pattern = "[ \\t\\f]+")
	void ignoreWhiteSpace ()
	{
	}

	@Lex (pattern = "\\n|\\r\\n")
	void scanNewLine ()
	{
		++m_lineNumber;
	}

	@Lex (pattern = ".")
	void scanError () throws ParserException
	{
		throw new ParserException (m_lineNumber, "Unexpected character: " + yyText ());
	}

	@Lex (pattern = "<<EOF>>")
	int scanEof () throws ParserException
	{
		return 0;
	}

	////////////////////////////////////////////////////////////////////////
	//
	// Parser section
	//
	////////////////////////////////////////////////////////////////////////

	@Rule (lhs = "start", rhs = "")
	void parseStart ()
	{
		m_rule = new ArrayList<Symbol> ();
	}

	@Rule (lhs = "start", rhs = "rule")
	void parseStart (ArrayList<Symbol> symbols)
	{
		m_rule = symbols;
	}

	@Rule (lhs = "start", rhs = "orRule")
	void parseStart (OrSymbol symbol)
	{
		m_rule = new ArrayList<Symbol> ();
		m_rule.add (symbol);
	}

	@Rule (lhs = "rule", rhs = "symbol")
	ArrayList<Symbol> parseRule (Symbol symbol)
	{
		ArrayList<Symbol> symbols = new ArrayList<Symbol> ();
		symbols.add (symbol);
		return symbols;
	}

	@Rule (lhs = "rule", rhs = "parenRule")
	ArrayList<Symbol> parseRule (ArrayList<Symbol> symbols)
	{
		return symbols;
	}

	@Rule (lhs = "rule", rhs = "rule symbol")
	ArrayList<Symbol> parseRule (ArrayList<Symbol> symbols, Symbol symbol)
	{
		symbols.add (symbol);
		return symbols;
	}

	@Rule (lhs = "rule", rhs = "rule parenRule")
	ArrayList<Symbol> parseRule (ArrayList<Symbol> s1, ArrayList<Symbol> s2)
	{
		GroupSymbol symbol = new GroupSymbol (m_lib.createInternalSymbol (), s2.toArray (new Symbol[s2.size ()]));
		s1.add (symbol);
		return s1;
	}

	@Rule (lhs = "orRule", rhs = "rule OR rule", args = "1 3")
	OrSymbol parseOr (ArrayList<Symbol> s1, ArrayList<Symbol> s2)
	{
		OrSymbol symbol = new OrSymbol (m_lib.createInternalSymbol ());
		symbol.addRule (s1.toArray (new Symbol[s1.size ()]));
		symbol.addRule (s2.toArray (new Symbol[s2.size ()]));
		return symbol;
	}

	@Rule (lhs = "orRule", rhs = "orRule OR rule", args = "1 3")
	OrSymbol parseOr (OrSymbol orSymbol, ArrayList<Symbol> s2)
	{
		orSymbol.addRule (s2.toArray (new Symbol[s2.size ()]));
		return orSymbol;
	}

	@Rule (lhs = "parenRule", rhs = "LPAREN rule RPAREN", args = "2")
	ArrayList<Symbol> parseParenRule (ArrayList<Symbol> symbols)
	{
		return symbols;
	}

	@Rule (lhs = "parenRule", rhs = "LPAREN orRule RPAREN", args = "2")
	ArrayList<Symbol> parseParenRule (Symbol orSymbol)
	{
		ArrayList<Symbol> symbols = new ArrayList<Symbol> ();
		symbols.add (orSymbol);
		return symbols;
	}

	@Rule (lhs = "symbol", rhs = "SYMBOL")
	Symbol parseSymbol (Symbol symbol)
	{
		return symbol;
	}

	@Rule (lhs = "symbol", rhs = "SYMBOL PLUS", args = "1")
	Symbol parsePlus (Symbol symbol)
	{
		return new PlusSymbol (m_lib.createInternalSymbol (), new Symbol[]{ symbol });
	}

	@Rule (lhs = "symbol", rhs = "parenRule PLUS", args = "1")
	Symbol parsePlus (ArrayList<Symbol> symbols)
	{
		return new PlusSymbol (m_lib.createInternalSymbol (), symbols.toArray (new Symbol[symbols.size ()]));
	}

	@Rule (lhs = "symbol", rhs = "SYMBOL STAR", args = "1")
	Symbol parseStar (Symbol symbol)
	{
		return new StarSymbol (m_lib.createInternalSymbol (), new Symbol[]{ symbol });
	}

	@Rule (lhs = "symbol", rhs = "parenRule STAR", args = "1")
	Symbol parseStar (ArrayList<Symbol> symbols)
	{
		return new StarSymbol (m_lib.createInternalSymbol (), symbols.toArray (new Symbol[symbols.size ()]));
	}

	@Rule (lhs = "symbol", rhs = "SYMBOL QUESTION", args = "1")
	Symbol parseQuestion (Symbol symbol)
	{
		return new QSymbol (m_lib.createInternalSymbol (), new Symbol[]{ symbol });
	}

	@Rule (lhs = "symbol", rhs = "parenRule QUESTION", args = "1")
	Symbol parseQuestion (ArrayList<Symbol> symbols)
	{
		return new QSymbol (m_lib.createInternalSymbol (), symbols.toArray (new Symbol[symbols.size ()]));
	}
}
