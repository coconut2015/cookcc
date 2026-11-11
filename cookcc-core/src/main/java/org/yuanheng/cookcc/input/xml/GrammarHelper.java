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
package org.yuanheng.cookcc.input.xml;

import org.yuanheng.cookcc.doc.GrammarDoc;
import org.yuanheng.cookcc.doc.ParserDoc;
import org.yuanheng.cookcc.doc.RhsDoc;

import cookxml.core.interfaces.NoAdd;

/**
 * @author Heng Yuan
 */
public class GrammarHelper implements NoAdd
{
	private final ParserDoc m_parser;
	private GrammarDoc m_grammar;

	public GrammarHelper (ParserDoc lexer)
	{
		m_parser = lexer;
	}

	public void setRule (String rule)
	{
		m_grammar = m_parser.getGrammar (rule);
	}

	public void add (RhsDoc rhs)
	{
		m_grammar.addRhs (rhs);
	}

	public GrammarDoc getGrammar ()
	{
		return m_grammar;
	}
}
