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
package org.yuanheng.cookcc.dfa;

import java.io.IOException;
import java.util.Comparator;
import java.util.TreeSet;

import org.yuanheng.cookcc.OptionMap;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.doc.LexerDoc;
import org.yuanheng.cookcc.doc.LexerStateDoc;
import org.yuanheng.cookcc.doc.RuleDoc;
import org.yuanheng.cookcc.lexer.Lexer;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class LexerDFAInfo
{
	private final static Comparator<RuleDoc> s_ruleComparator = new Comparator<RuleDoc> ()
	{
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
		return m_lexer.getECS ().getGroups ().clone ();
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
}
