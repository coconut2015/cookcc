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
package org.yuanheng.cookcc.input.yacc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.yuanheng.cookcc.*;
import org.yuanheng.cookcc.doc.*;

/**
 * @author Heng Yuan
 * @version $Id$
 */
@CookCCOption (lexerTable = "ecs", parserTable = "ecs")
public class YaccParser extends YaccLexer
{
	@CookCCToken
	static enum Token
	{
		TOKENTYPE, TYPE, TYPEINFO, TOKEN, START, SEPARATOR, PREC, PARTIAL_ACTION, ACTION_CODE
	}

	private final Document m_doc = new Document ();
	private final ParserDoc m_parser = new ParserDoc ();
	private final TokensDoc m_plainTokens = new TokensDoc ();

	private int m_lineNum = 1;        // starts with line number 1

	private int m_braceLevel = 0;

	private StringBuffer m_currentBuffer;

	private StringBuffer m_section3Code;

	private YaccParser ()
	{
		m_doc.setParser (m_parser);
		m_doc.addTokens (m_plainTokens);
	}

	////////////////////////////////////////////////////////////////
	//
	// Lexer
	//
	////////////////////////////////////////////////////////////////

	@Shortcuts (shortcuts = {
		@Shortcut (name = "WS", pattern = "[ \\t]+"),
		@Shortcut (name = "OPTWS", pattern = "[ \\t]*"),
		@Shortcut (name = "NL", pattern = "\\r?\\n"),
		@Shortcut (name = "NAME", pattern = "[a-zA-Z_][a-zA-Z_0-9]*"),
		@Shortcut (name = "ESC", pattern = "[\\\\](.|(u[a-fA-F0-9]{4})|([0-9]{1,3})|(x[a-fA-F0-9]{1,2}))")
	})

	@Lex (pattern = "{WS}", state = "INITIAL, SECTION2")
	void ignoreWhiteSpace ()
	{
	}

	@Lex (pattern = "'//'.*", state = "INITIAL, SECTION2, ACTION")
	void lineComment ()
	{
		if (m_currentBuffer != null)
			m_currentBuffer.append (yyText ());
	}

	@Lex (pattern = "{NL}", state = "INITIAL, SECTION2, BLOCKCOMMENT, CODEINCLUDE, ACTION")
	void newLine ()
	{
		if (m_currentBuffer != null)
			m_currentBuffer.append (yyText ());
		++m_lineNum;
	}

	////////////// block comment //////////////////////////////////////////////////

	@Lex (pattern = "'/*'", state = "INITIAL, SECTION2, ACTION")
	void blockCommentStart ()
	{
		if (m_currentBuffer != null)
			m_currentBuffer.append (yyText ());
		yyPushLexerState ("BLOCKCOMMENT");
	}

	@Lex (pattern = "'*/'", state = "BLOCKCOMMENT")
	void blockCommentEnd ()
	{
		if (m_currentBuffer != null)
			m_currentBuffer.append (yyText ());
		yyPopLexerState ();
	}

	@Lexs (patterns = {
		@Lex (pattern = "[^*/\\n]+", state = "BLOCKCOMMENT"),
		@Lex (pattern = ".", state = "BLOCKCOMMENT")
	})
	void blockCommentContent ()
	{
		if (m_currentBuffer != null)
			m_currentBuffer.append (yyText ());
	}

	@Lex (pattern = "<<EOF>>", state = "BLOCKCOMMENT")
	void blockCommentEof ()
	{
		error ("unclosed block comment");
	}

	////////////// code include //////////////////////////////////////////////////

	@Lex (pattern = "'%{'", state = "INITIAL")
	void codeIncludeStart ()
	{
		m_currentBuffer = new StringBuffer ();
		begin ("CODEINCLUDE");
	}

	@Lex (pattern = "'%}'", state = "CODEINCLUDE")
	void codeIncludeEnd ()
	{
		String str = m_doc.getCode ().get ("fileheader");
		if (str == null)
			str = m_currentBuffer.toString ();
		else
			str += m_currentBuffer.toString ();
		m_doc.getCode ().put ("fileheader", str);
		m_currentBuffer = null;
		begin ("INITIAL");
	}

	@Lexs (patterns = {
		@Lex (pattern = "[^%{\\n]+", state = "CODEINCLUDE"),
		@Lex (pattern = ".", state = "CODEINCLUDE")
	})
	void codeIncludeContent ()
	{
		if (m_currentBuffer != null)
			m_currentBuffer.append (yyText ());
	}

	@Lex (pattern = "<<EOF>>", state = "CODEINCLUDE")
	void codeIncludeEof ()
	{
		error ("unclosed code include.");
	}

	@Lex (pattern = "'{'", state = "SECTION2, ACTION")
	void actionLB ()
	{
		++m_braceLevel;
		if (m_braceLevel == 1)
		{
			m_currentBuffer = new StringBuffer ();
			begin ("ACTION");
		}
		else
			m_currentBuffer.append (yyText ());
	}

	/**
	 * This is a slightly complicated situation.  Since CookCC only allows
	 * a single type (either turn a value or do not return a value) for a
	 * lexer function, we will just have to use a hack.
	 * <p/>
	 * We will just directly return the value (since our code does not need
	 * to be compiled to pass through APT correctly).
	 *
	 * @return token
	 */
	@Lex (pattern = "'}'", state = "ACTION")
	int actionRB ()
	{
		--m_braceLevel;
		if (m_braceLevel == 0)
		{
			begin ("SECTION2");
			String action = m_currentBuffer.toString ();
			m_currentBuffer = null;
			yySetValue (action);
			return ACTION_CODE;
		}
		else
		{
			m_currentBuffer.append (yyText ());
			return PARTIAL_ACTION;
		}
	}

	@Lexs (patterns = {
		@Lex (pattern = "[^'/*{}\\n]+", state = "ACTION"),
		@Lex (pattern = ".", state = "ACTION")
	})
	void actionContent ()
	{
		m_currentBuffer.append (yyText ());
	}

	@Lex (pattern = "<<EOF>>", state = "ACTION")
	void actionEof ()
	{
		error ("unclosed action code.");
	}

	////////////// INITIAL STATE //////////////////////////////////////////////////

	@Lex (pattern = "'%%'", token = "SEPARATOR", state = "INITIAL")
	void startSection2 ()
	{
		begin ("SECTION2");
	}

	@Lexs (patterns = {
		@Lex (pattern = "^{OPTWS}%token", token = "TOKENTYPE", state = "INITIAL"),
		@Lex (pattern = "^{OPTWS}%left", token = "TOKENTYPE", state = "INITIAL"),
		@Lex (pattern = "^{OPTWS}%right", token = "TOKENTYPE", state = "INITIAL"),
		@Lex (pattern = "^{OPTWS}%nonassoc", token = "TOKENTYPE", state = "INITIAL")
	})
	String scanTokenDirective () throws IOException
	{
		String text = yyText ();
		return text.substring (text.indexOf ('%'));
	}

	@Lexs (patterns = {
		@Lex (pattern = "%start", token = "START", state = "INITIAL"),
		@Lex (pattern = "%type", token = "TYPE", state = "INITIAL")
	})
	void scanDirective ()
	{
	}

	@Lex (pattern = "[<][^>]*[>]", token = "TYPEINFO", state = "INITIAL")
	String scanTypeInfo ()
	{
		String type = yyText ();
		return type.substring (1, type.length () - 1);
	}

	@Lex (pattern = "^{OPTWS}%{NAME}")
	void unknownDirective ()
	{
		warn ("unknown directive: " + yyText ());
		++m_lineNum;
	}

	@Lex (pattern = "<<EOF>>", state = "INITIAL")
	void earlyEof ()
	{
		error ("grammar section not found.");
	}

	////////////// SECTION 2 //////////////////////////////////////////////////

	@Lexs (patterns = {
		@Lex (pattern = "{NAME}", token = "TOKEN", state = "INITIAL, SECTION2"),
		@Lex (pattern = "[']([^\\\\']|{ESC})[']", token = "TOKEN", state = "INITIAL, SECTION2")
	})
	String parseToken ()
	{
		return yyText ();
	}

	@Lex (pattern = "%prec", token = "PREC", state = "SECTION2")
	void scanPrec ()
	{
	}

	@Lex (pattern = "[:|;]", state = "SECTION2")
	int scanSymbol ()
	{
		yySetValue (m_lineNum);
		return yyText ().charAt (0);
	}

	@Lex (pattern = "'%%'", state = "SECTION2")
	void startSection3 ()
	{
		begin ("SECTION3");
		m_section3Code = new StringBuffer ();
	}

	@Lex (pattern = "<<EOF>>", state = "SECTION2")
	int eof ()
	{
		return 0;
	}

	////////////// SECTION 3 //////////////////////////////////////////////////

	@Lex (pattern = "(.|\\n)*", state = "SECTION3")
	void dumpSection3Code ()
	{
		m_section3Code.append (yyText ());
	}

	@Lex (pattern = "<<EOF>>", state = "SECTION3")
	int endSection3 ()
	{
		String code = m_section3Code.toString ();
		if (code.length () > 0)
			m_doc.addCode ("default", code);
		return 0;
	}

	////////////// MISC //////////////////////////////////////////////////

	@Lex (pattern = ".", state = "INITIAL, SECTION2")
	void invalidChar ()
	{
		warn ("invalid character: " + yyText ());
	}

	////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////

	@Rules (rules = {
		@Rule (lhs = "yacc", rhs = "section1 SEPARATOR section2"),
		@Rule (lhs = "section1", rhs = "section1 precedence"),
		@Rule (lhs = "section1", rhs = "section1 start"),
		@Rule (lhs = "section1", rhs = "section1 type"),
		@Rule (lhs = "section1", rhs = ""),
		@Rule (lhs = "optTypeInfo", rhs = ""),
		@Rule (lhs = "section2", rhs = "rules")
	})
	void parseYacc ()
	{
	}

	@Rule (lhs = "precedence", rhs = "TOKENTYPE optTypeInfo tokenList", args = "1 2 3")
	void parsePrecedence (String type, String dataType, String tokenList) throws IOException
	{
		if (tokenList == null)
			return;
		TokensDoc tokensDoc;
		if ("%token".equals (type))
			tokensDoc = m_plainTokens;
		else
		{
			tokensDoc = new TokensDoc ();
			if ("%left".equals (type))
				tokensDoc.setType ("left");
			else if ("%right".equals (type))
				tokensDoc.setType ("right");
			m_doc.addTokens (tokensDoc);
		}
		tokensDoc.addTokens (tokenList);
		if (dataType != null)
		{
			TypeDoc typeDoc = new TypeDoc ();
			typeDoc.setFormat ("((" + dataType + "){0})");
			typeDoc.setSymbols (tokenList);
			m_parser.addType (typeDoc);
		}
	}

	@Rule (lhs = "optTypeInfo", rhs = "TYPEINFO", args = "1")
	String parseTypeInfo (String type)
	{
		return type;
	}

	@Rule (lhs = "type", rhs = "TYPE TYPEINFO tokenList", args = "2 3")
	void parseType (String dataType, String list) throws IOException
	{
		TypeDoc typeDoc = new TypeDoc ();
		typeDoc.setFormat ("((" + dataType + "){0})");
		typeDoc.setSymbols (list);
		m_parser.addType (typeDoc);
	}

	@Rule (lhs = "tokenList", rhs = "tokenList TOKEN", args = "1 2")
	String parseTokenList (String list, String token)
	{
		return list + " " + token;
	}

	@Rule (lhs = "tokenList", rhs = "TOKEN", args = "1")
	String parseTokenList (String token)
	{
		return token;
	}

	@Rule (lhs = "start", rhs = "START TOKEN", args = "2")
	void parseStart (String start)
	{
		m_parser.setStart (start);
	}

	@Rules (rules = {
		@Rule (lhs = "rules", rhs = "rules rule"),
		@Rule (lhs = "rules", rhs = "rule")
	})
	void parseRules ()
	{
	}

	@Rule (lhs = "rules", rhs = "rules error")
	void parseRuleError ()
	{
		error ("invalid grammar.");
	}

	@Rule (lhs = "rules", rhs = "error")
	void parseRulesError ()
	{
		error ("empty grammar.");
	}

	@Rule (lhs = "rule", rhs = "TOKEN rhsList ';'", args = "1 2")
	void parseRule (String lhs, ArrayList<RhsDoc> rhsDocs)
	{
		GrammarDoc grammar = m_parser.getGrammar (lhs);
		for (RhsDoc rhs : rhsDocs)
			grammar.addRhs (rhs);
	}

	@Rule (lhs = "rhs", rhs = "terms prec action", args = "1 2 3")
	RhsDoc parseRhsList (String terms, String precedence, String action)
	{
		RhsDoc rhs = new RhsDoc ();
		rhs.setTerms (terms);
		rhs.setAction (action);
		rhs.setPrecedence (precedence);
		return rhs;
	}

	@Rule (lhs = "rhsList", rhs = "rhsList '|' rhs", args = "1 2 3")
	ArrayList<RhsDoc> parseRhsList (ArrayList<RhsDoc> list, Integer lineNumber, RhsDoc rhs)
	{
		rhs.setLineNumber (lineNumber);
		list.add (rhs);
		return list;
	}

	@Rule (lhs = "rhsList", rhs = "':' rhs", args = "1 2")
	ArrayList<RhsDoc> parseRhsList (Integer lineNumber, RhsDoc rhs)
	{
		rhs.setLineNumber (lineNumber);
		ArrayList<RhsDoc> list = new ArrayList<RhsDoc> ();
		list.add (rhs);
		return list;
	}

	@Rule (lhs = "terms", rhs = "terms TOKEN", args = "1 2")
	String parseRHS (String terms, String token)
	{
		if (terms.length () == 0)
			return token;
		return terms + " " + token;
	}

	@Rule (lhs = "terms", rhs = "")
	String parseTerms ()
	{
		return "";
	}

	@Rule (lhs = "action", rhs = "complete_action ACTION_CODE", args = "2")
	String parseAction (String action)
	{
		return action;
	}

	@Rules (rules = {
		@Rule (lhs = "action", rhs = ""),
		@Rule (lhs = "complete_action", rhs = "complete_action PARTIAL_ACTION"),
		@Rule (lhs = "complete_action", rhs = "")
	})
	String parseAction ()
	{
		return null;
	}

	@Rule (lhs = "prec", rhs = "PREC TOKEN", args = "2")
	String parsePrec (String prec)
	{
		return prec;
	}

	@Rule (lhs = "prec", rhs = "")
	String parsePrec ()
	{
		return null;
	}

	void warn (String msg)
	{
		Main.warn ("Warning [" + m_lineNum + "]: " + msg);
	}

	void error (String msg)
	{
		Main.error ("Error [" + m_lineNum + "]: " + msg);
	}

	public static Document parse (File file) throws IOException
	{
		YaccParser parser = new YaccParser ();
		int fileSize = (int)file.length ();
		if (fileSize > 4096)
			parser.setBufferSize (fileSize);
		parser.setInput (new FileInputStream (file));
		if (parser.yyParse () > 0)
			Main.error ("errors in input");
		return parser.m_doc;
	}
}
