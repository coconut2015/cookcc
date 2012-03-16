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
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import org.yuanheng.cookcc.OptionMap;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.doc.GrammarDoc;
import org.yuanheng.cookcc.doc.ParserDoc;
import org.yuanheng.cookcc.parser.Parser;
import org.yuanheng.cookcc.parser.Production;
import org.yuanheng.cookcc.parser.Token;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class ParserDFAInfo
{
	public static ParserDFAInfo getParserDFAInfo (Document doc, OptionMap options) throws IOException
	{
		return new ParserDFAInfo (doc.getParser (), Parser.getParser (doc, options));
	}

	private final ParserDoc m_parserDoc;
	private final Parser m_parser;
	private Object m_dfa;

	private int[] m_rules;

	private int[] m_lhs;
	private int[] m_ecs;
	private String[] m_symbols;

	private ParserDFAInfo (ParserDoc parserDoc, Parser Parser)
	{
		m_parserDoc = parserDoc;
		m_parser = Parser;
	}

	public boolean getRecovery ()
	{
		return m_parserDoc.getRecovery ();
	}

	public boolean getParseError ()
	{
		return m_parserDoc.getParseError ();
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

	public int[] getEcs ()
	{
		if (m_ecs != null)
			return m_ecs;
		int maxTerminal = m_parser.getMaxTerminal ();
		m_ecs = new int[maxTerminal + 1];
		int[] symbols = m_parser.getSymbolGroups ();
		System.arraycopy (symbols, 0, m_ecs, 0, maxTerminal + 1);
		return m_ecs;
	}

	public int getMaxTerminal ()
	{
		return m_parser.getMaxTerminal ();
	}

	public int getUsedTerminalCount ()
	{
		return m_parser.getUsedTerminalCount ();
	}

	public int getNonTerminalCount ()
	{
		return m_parser.getNonTerminalCount ();
	}

	public int[] getRules ()
	{
		if (m_rules != null)
			return m_rules;
		Vector<Production> rules = m_parser.getRules ();

		// plus 1 since our reduce states cannot be 0 (0 indicates no shifts or reduces)
		m_rules = new int[rules.size () + 1];
		for (int i = 0; i < rules.size (); ++i)
			m_rules[i + 1] = rules.get (i).size ();

		return m_rules;
	}

	public int[] getLhs ()
	{
		if (m_lhs != null)
			return m_lhs;

		Vector<Production> rules = m_parser.getRules ();
		int[] groups = m_parser.getSymbolGroups ();
		// plus 1 since our reduce states cannot be 0 (0 indicates no shifts or reduces)
		m_lhs = new int[rules.size () + 1];
		for (int i = 0; i < rules.size (); ++i)
			m_lhs[i + 1] = groups[rules.get (i).getSymbol ()];

		return m_lhs;
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

	public String[] getSymbols ()
	{
		if (m_symbols != null)
			return m_symbols;
		m_symbols = m_parser.getSymbols ();
		return m_symbols;
	}

	public LinkedList<Token> getTokens ()
	{
		return m_parser.getTokens ();
	}

	public Map<Integer, MessageFormat> getFormats ()
	{
		return m_parser.getFormats ();
	}

	public int getShiftConflict ()
	{
		return m_parser.getShiftConflict ();
	}

	public int getReduceConflict ()
	{
		return m_parser.getReduceConflict ();
	}

	public Object getProperty (String property)
	{
		return m_parserDoc.getProperty (property);
	}
}