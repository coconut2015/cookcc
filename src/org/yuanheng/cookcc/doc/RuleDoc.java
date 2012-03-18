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
package org.yuanheng.cookcc.doc;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class RuleDoc extends TreeDoc
{
	/**
	 * There are two internal rules:
	 * 		&lt;&lt;EOF&gt;&gt;
	 * for dealing with unexpected end of the file and
	 * 		.|\n
	 * that covers all characters.  This two internal rules ensures that
	 * all inputs have some meaningful
	 *
	 * @param	lexer
	 *			the parent LexerDoc
	 * @return	a rule that covers the internal patterns.
	 */
	public static RuleDoc createInternalRule (LexerDoc lexer)
	{
		RuleDoc rule = new RuleDoc (lexer);
		rule.setInternal ();
		PatternDoc pattern = new PatternDoc (true);
		pattern.setPattern (".|\\n");
		pattern.setInternal ();
		rule.addPattern (pattern);
		pattern = new PatternDoc (true);
		pattern.setPattern ("<<EOF>>");
		pattern.setInternal ();
		rule.addPattern (pattern);
		return rule;
	}

	private static int s_count = 0;

	private final LexerDoc m_lexer;
	private final int m_id;

	private final LinkedList<PatternDoc> m_patterns = new LinkedList<PatternDoc> ();

	private String m_action = "";
	private HashSet<LexerStateDoc> m_states = new HashSet<LexerStateDoc> ();

	private boolean m_internal;

	public RuleDoc (LexerDoc lexer)
	{
		m_lexer = lexer;
		m_id = s_count++;
	}

	/**
	 * The order of the rule is important, so it is necessary to have a sequential id
	 * that determines the order of the rules.
	 *
	 * @return	the sequential id of the rule.
	 */
	public int getId ()
	{
		return m_id;
	}

	public void addPattern (PatternDoc pattern)
	{
		m_patterns.add (pattern);
	}

	public PatternDoc[] getPatterns ()
	{
		return m_patterns.toArray (new PatternDoc[m_patterns.size ()]);
	}

	public void setAction (String action)
	{
		if (action == null)
			action = "";
		m_action = action;
	}

	public String getAction ()
	{
		return m_action;
	}

	/**
	 * Add a comma separated list of states that this pattern applies to.
	 *
	 * @param	states
	 *			a comma separated list of states
	 */
	public void addStates (String states)
	{
		StringTokenizer tokenizer = new StringTokenizer (states, ", ");
		while (tokenizer.hasMoreTokens ())
		{
			String tok = tokenizer.nextToken ().trim ();
			if (tok.length () == 0)
				continue;
			LexerStateDoc lexerState = m_lexer.getLexerState (tok);
			if (!m_states.contains (lexerState))
			{
				m_states.add (lexerState);
				lexerState.addRule (this);
			}
		}
	}

	public LexerStateDoc[] getStates ()
	{
		return m_states.toArray (new LexerStateDoc[m_states.size ()]);
	}

	public boolean getInternal ()
	{
		return m_internal;
	}

	private void setInternal ()
	{
		m_internal = true;
	}
}
