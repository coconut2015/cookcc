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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * @author Heng Yuan
 */
public class RuleDoc extends TreeDoc
{
	/**
	 * There are two internal rules:
	 * &lt;&lt;EOF&gt;&gt;
	 * for dealing with unexpected end of the file and
	 * .|\n
	 * that covers all characters.  This two internal rules ensures that
	 * all inputs have some meaningful
	 *
	 * @param    lexer the parent LexerDoc
	 * @return a rule that covers the internal patterns.
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
	 * @return the sequential id of the rule.
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
	 * @param    states a comma separated list of states
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
