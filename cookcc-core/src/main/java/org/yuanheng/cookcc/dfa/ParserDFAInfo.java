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
package org.yuanheng.cookcc.dfa;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.yuanheng.cookcc.OptionMap;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.doc.GrammarDoc;
import org.yuanheng.cookcc.doc.ParserDoc;
import org.yuanheng.cookcc.parser.Parser;
import org.yuanheng.cookcc.parser.Production;
import org.yuanheng.cookcc.parser.Token;

/**
 * @author Heng Yuan
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

	public int[] getReverse ()
	{
		return m_parser.getReverseECS ();
	}

	public boolean getIgnoreList ()
	{
		return m_parserDoc.hasIgnore ();
	}

	public boolean getCaptureList ()
	{
		return m_parser.getCaptureList () != null;
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
		ArrayList<Production> rules = m_parser.getRules ();

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

		ArrayList<Production> rules = m_parser.getRules ();
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

	public Collection<Token> getTokens ()
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
