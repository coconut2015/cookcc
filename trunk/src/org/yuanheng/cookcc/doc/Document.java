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
package org.yuanheng.cookcc.doc;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * This is the parsed tree from cookcc input.  It is the internal structure which lexer/parser
 * used as input for computing state machines.  It is also the structure for converting input
 * format from one to another.
 *
 * @author Heng Yuan
 * @version $Id$
 */
public class Document extends TreeDoc
{
	private Boolean m_main;
	private boolean m_unicode;
	private final Map<String, String> m_code = new HashMap<String, String> ();
	private final Collection<TokensDoc> m_tokens = new LinkedList<TokensDoc> ();
	private LexerDoc m_lexer;
	private ParserDoc m_parser;

	/**
	 * Check if the user specifically asked for main function.
	 *
	 * @return if user wants to have a main function or not.  null indicates flag not set.
	 */
	public Boolean getMain ()
	{
		return m_main;
	}

	public void setMain (boolean main)
	{
		m_main = Boolean.valueOf (main);
	}

	public boolean isUnicode ()
	{
		return m_unicode;
	}

	public void setUnicode (boolean unicode)
	{
		m_unicode = unicode;
	}

	public void setLexer (LexerDoc lexerDoc)
	{
		m_lexer = lexerDoc;
	}

	public void setParser (ParserDoc parserDoc)
	{
		m_parser = parserDoc;
	}

	public Map<String, String> getCode ()
	{
		return m_code;
	}

	public void addCode (String key, String code)
	{
		m_code.put (key, code);
	}

	public LexerDoc getLexer ()
	{
		return m_lexer;
	}

	public ParserDoc getParser ()
	{
		return m_parser;
	}

	public void addTokens (TokensDoc tokens)
	{
		m_tokens.add (tokens);
	}

	public TokensDoc[] getTokens ()
	{
		return m_tokens.toArray (new TokensDoc[m_tokens.size ()]);
	}
}
