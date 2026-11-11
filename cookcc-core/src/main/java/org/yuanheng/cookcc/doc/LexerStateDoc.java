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

import java.util.LinkedList;

/**
 * @author Heng Yuan
 */
public class LexerStateDoc extends TreeDoc
{
	private final String m_name;
	private final LinkedList<RuleDoc> m_rules = new LinkedList<RuleDoc> ();

	public LexerStateDoc (String name)
	{
		m_name = name;
	}

	public String getName ()
	{
		return m_name;
	}

	public void addRule (RuleDoc rule)
	{
		if (!m_rules.contains (rule))
			m_rules.add (rule);
	}

	public RuleDoc[] getRules ()
	{
		return m_rules.toArray (new RuleDoc[m_rules.size ()]);
	}
}
