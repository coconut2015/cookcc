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
package org.yuanheng.cookcc.dfa;

import java.io.IOException;
import java.util.Vector;

import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.doc.GrammarDoc;
import org.yuanheng.cookcc.doc.ParserDoc;
import org.yuanheng.cookcc.parser.Parser;
import org.yuanheng.cookcc.parser.Production;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class ParserDFAInfo
{
	public static ParserDFAInfo getParserDFAInfo (Document doc) throws IOException
	{
		return new ParserDFAInfo (doc.getParser (), Parser.getParser (doc));
	}

	private final ParserDoc m_parserDoc;
	private final Parser m_parser;
	private Object m_dfa;

	private int[] m_rules;

	private short[] m_defaultReduce;

	private ParserDFAInfo (ParserDoc parserDoc, Parser Parser)
	{
		m_parserDoc = parserDoc;
		m_parser = Parser;
	}

	public String getTable ()
	{
		return m_parserDoc.getTable ();
	}

	public GrammarDoc[] getCases ()
	{
		return m_parserDoc.getGrammars ();
	}

	public int getCaseCount ()
	{
		return m_parser.getCaseCount ();
	}

	public int[] getRules ()
	{
		if (m_rules != null)
			return m_rules;
		Vector<Production> rules = m_parser.getRules ();
		m_rules = new int[rules.size ()];
		for (int i = 0; i < m_rules.length; ++i)
			m_rules[i] = rules.get (i).size ();

		return m_rules;
	}

	public Object getDfa ()
	{
		if (m_dfa != null)
			return m_dfa;
		String table = getTable ();
		if ("ecs".equals (table))
			m_dfa = new ECSParserTable (m_parser);
		else if ("compressed".equals (table))
			m_dfa = new CompressedParserTable (m_parser);
		return m_dfa;
	}

	public short[] getDefaultReduce ()
	{
		if (m_defaultReduce == null)
			m_defaultReduce = m_parser.getDefaultReduces ();
		return m_defaultReduce;
	}
}