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
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import org.yuanheng.cookcc.OptionMap;
import org.yuanheng.cookcc.doc.*;
import org.yuanheng.cookcc.lexer.Lexer;
import org.yuanheng.cookcc.parser.Token;

/**
 * @author Heng Yuan
 */
public class LexerDFAInfo
{
	private final static Comparator<RuleDoc> s_ruleComparator = new Comparator<RuleDoc> ()
	{
		@Override
		public int compare (RuleDoc o1, RuleDoc o2)
		{
			return o1.getId () - o2.getId ();
		}
	};

	public static LexerDFAInfo getLexerDFAInfo (Document doc, OptionMap options) throws IOException
	{
		return new LexerDFAInfo (doc.getLexer (), Lexer.getLexer (doc, options));
	}

	private final LexerDoc m_lexerDoc;
	private final Lexer m_lexer;
	private Object m_dfa;
	private int[] m_ecs;

	private LexerDFAInfo (LexerDoc lexerDoc, Lexer lexer)
	{
		m_lexerDoc = lexerDoc;
		m_lexer = lexer;
	}

	public int getEof ()
	{
		return m_lexer.getCCL ().EOF;
	}

	public boolean getYywrap ()
	{
		return m_lexerDoc.isYywrap ();
	}

	public int getMaxSymbol ()
	{
		return m_lexer.getCCL ().MAX_SYMBOL;
	}

	public boolean getBol ()
	{
		return m_lexerDoc.isBol () || m_lexer.hasBolStates ();
	}

	public boolean getBolStates ()
	{
		return m_lexer.hasBolStates ();
	}

	public int[] getAccept ()
	{
		return m_lexer.getDFA ().getAccepts ();
	}

	public int[] getEcs ()
	{
		if (m_ecs == null)
			m_ecs = m_lexer.getECS ().getGroups ().clone ();
		return m_ecs;
	}

	public int getEcsGroupCount ()
	{
		return m_lexer.getECS ().getGroupCount ();
	}

	public String[] getStates ()
	{
		LexerStateDoc[] lexerStates = m_lexer.getLexerStates ();
		String[] states = new String[lexerStates.length];
		for (int i = 0; i < lexerStates.length; ++i)
			states[i] = lexerStates[i].getName ();
		return states;
	}

	public int[] getBegins ()
	{
		return m_lexer.getBeginLocations ().clone ();
	}

	public int getCaseCount ()
	{
		return m_lexer.getCaseCount ();
	}

	public boolean isLineMode ()
	{
		return m_lexerDoc.isLineMode ();
	}

	public boolean getBackup ()
	{
		return m_lexer.hasBackup ();
	}

	public RuleDoc[] getCases ()
	{
		TreeSet<RuleDoc> rules = new TreeSet<RuleDoc> (s_ruleComparator);

		LexerStateDoc[] lexerStates = m_lexerDoc.getLexerStates ();
		for (LexerStateDoc lexerState : lexerStates)
		{
			for (RuleDoc rule : lexerState.getRules ())
				rules.add (rule);
		}
		return rules.toArray (new RuleDoc[rules.size ()]);
	}

	public String getTable ()
	{
		return m_lexerDoc.getTable ();
	}

	public Object getDfa ()
	{
		if (m_dfa != null)
			return m_dfa;
		String table = getTable ();
		if ("ecs".equals (table))
			m_dfa = new ECSTable (m_lexer);
		else if ("full".equals (table))
			m_dfa = new FullTable (m_lexer);
		else if ("compressed".equals (table))
			m_dfa = new CompressedTable (m_lexer);
		return m_dfa;
	}

	public Collection<Token> getTokens ()
	{
		return m_lexer.getTokens ();
	}
}
