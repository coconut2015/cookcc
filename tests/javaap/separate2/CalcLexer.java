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
import java.io.FileInputStream;

import org.yuanheng.cookcc.*;

/**
 * This calculator example is adapted from "A Compact Guide to Lex & Yacc"
 * (http://epaperpress.com/lexandyacc/).
 *
 * @author Heng Yuan
 */
@CookCCOption (lexerTable="compressed", tokenClass="Token")
class CalcLexer extends CalcLexerGen
{
	@Lex (pattern="[0-9]+", token="INTEGER")
	protected Integer parseInt ()
	{
		return Integer.parseInt (yyText ());
	}

	/**
	 * Parse operators.
	 * <p>
	 * Chain multiple lexical patterns using {@link Lexs}.
	 *
	 * @return	null.
	 */
	@Lexs (patterns = {
		@Lex (pattern = "[;]", token = "SEMICOLON"),
		@Lex (pattern = "[=]", token = "ASSIGN"),
		@Lex (pattern = "[+]", token = "ADD"),
		@Lex (pattern = "\\-", token = "SUB"),
		@Lex (pattern = "[*]", token = "MUL"),
		@Lex (pattern = "[/]", token = "DIV"),
		@Lex (pattern = "[<]", token = "LT"),
		@Lex (pattern = "[>]", token = "GT"),
		@Lex (pattern = ">=", token = "GE"),
		@Lex (pattern = "<=", token = "LE"),
		@Lex (pattern = "!=", token = "NE"),
		@Lex (pattern = "==", token = "EQ")
	})
	protected Object parseOp ()
	{
		return null;
	}

	/**
	 * Parsing the symbols.
	 * <p>
	 * The return value is the token value because
	 * 1) the token attribute of Lex annotation is not specified AND
	 * 2) the return type is int or a {@link CookCCToken} marked Enum.
	 * <p>
	 * If token attribute is not specified and the return type of the
	 * function is not int (or Enum), an error would be generated
	 * by CookCC.
	 *
	 * @return	the symbol
	 */
	@Lex (pattern="[(){}.]")
	protected int parseSymbol ()
	{
		return yyText ().charAt (0);
	}

	/**
	 * Parsing keywords.
	 * <p>
	 * We could have merged annotations with parseOp () and it won't
	 * make a difference.  The separation is done to logically group
	 * the patterns.
	 *
	 * @return	null.
	 */
	@Lexs (patterns = {
		@Lex (pattern = "while", token = "WHILE"),
		@Lex (pattern = "if", token = "IF"),
		@Lex (pattern = "else", token = "ELSE"),
		@Lex (pattern = "print", token = "PRINT")
	})
	protected Object parseKeyword ()
	{
		return null;
	}

	/**
	 * Parsing the variable.  This function has to be specified AFTER
	 * parseKeyword because its lexical pattern could potentially shadow
	 * the keywords.
	 */
	@Lex (pattern = "[a-z]+", token = "VARIABLE")
	protected String parseVariable ()
	{
		return yyText ();
	}

	/**
	 * Simply ignore the white spaces
	 */
	@Lex (pattern = "[ \\t\\r\\n]+")
	protected void ignoreWhiteSpace ()
	{
	}

	/**
	 * Any characters passed at this point is invalid.
	 */
	@Lex (pattern = ".")
	protected void invalidCharacter () throws IOException
	{
		throw new IOException ("Illegal character: " + yyText ());
	}

	/**
	 * This is the function called after reaching the end of file.
	 * $ indicates the artifiical EOF token.  Alternatively, this
	 * function could simply return 0.
	 */
	@Lex (pattern = "<<EOF>>", token = "$")
	protected void parseEOF ()
	{
	}
}
