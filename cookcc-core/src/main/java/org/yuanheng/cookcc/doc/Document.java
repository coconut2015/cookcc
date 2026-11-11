/*
 * Copyright (c) 2008-2013, Heng Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yuanheng.cookcc.doc;

import java.util.*;

/**
 * This is the parsed tree from cookcc input.  It is the internal structure which lexer/parser
 * used as input for computing state machines.  It is also the structure for converting input
 * format from one to another.
 *
 * @author	Heng Yuan
 */
public class Document extends TreeDoc
{
	private Boolean m_main;
	private boolean m_unicode;
	private boolean m_rt;
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

	public boolean hasTokens ()
	{
		return m_tokens.size () > 0;
	}

	/**
	 * Checks if AST runtime should be used.
	 *
	 * @return	if AST runtime should be used.
	 * @since	0.4
	 */
	public boolean isRT ()
	{
		return m_rt;
	}

	/**
	 * Set if AST runtime should be used.
	 *
	 * @param	rt
	 * 			true or false
	 * @since	0.4
	 */
	public void setRT (boolean rt)
	{
		m_rt = rt;
	}
}
