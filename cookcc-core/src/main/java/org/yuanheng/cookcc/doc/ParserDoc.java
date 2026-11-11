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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Heng Yuan
 */
public class ParserDoc extends TreeDoc
{
	public final static String PROP_TABLE_TYPE = "table";
	public final static String DEFAULT_TABLE_TYPE = "ecs";

	// we use linked list to keep the state being used in order, may be useful.
	private final ArrayList<GrammarDoc> m_grammarList = new ArrayList<GrammarDoc> ();
	private final HashMap<String, GrammarDoc> m_grammarMap = new HashMap<String, GrammarDoc> ();

	private final LinkedList<TypeDoc> m_types = new LinkedList<TypeDoc> ();

	private String m_start;
	private boolean m_defaultReduce;
	private boolean m_recovery = true;
	private boolean m_parseError = true;
	private IgnoreDoc m_ignore;

	public ParserDoc ()
	{
	}

	public String getStart ()
	{
		return m_start;
	}

	public void setStart (String start)
	{
		if (start != null)
		{
			start = start.trim ();
			if (start.length () == 0)
				start = null;
		}
		m_start = start;
	}

	public void setDefaultReduce (boolean defaultReduce)
	{
		m_defaultReduce = defaultReduce;
	}

	public boolean getDefaultReduce ()
	{
		return m_defaultReduce;
	}

	public boolean getRecovery ()
	{
		return m_recovery;
	}

	public void setRecovery (boolean recovery)
	{
		m_recovery = recovery;
	}

	public boolean getParseError ()
	{
		return m_parseError;
	}

	public void setParseError (boolean parseerror)
	{
		m_parseError = parseerror;
	}

	public void add (IgnoreDoc ignore)
	{
		if (ignore.getList () == null)
		{
			throw new RuntimeException ("Ignore list cannot be empty.");
		}
		if (m_ignore == null)
		{
			m_ignore = ignore;
		}
		else
		{
			throw new RuntimeException ("Parser already has an ignore list.");
		}
	}

	public boolean hasIgnore ()
	{
		return m_ignore != null;
	}

	public IgnoreDoc getIgnore ()
	{
		return m_ignore;
	}

	public boolean hasGrammar (String term)
	{
		return m_grammarMap.containsKey (term);
	}

	public GrammarDoc getGrammar (String term)
	{
		if (term == null || term.length () == 0)
			throw new IllegalArgumentException ("term must not be empty.");
		GrammarDoc grammarDoc = m_grammarMap.get (term);
		if (grammarDoc == null)
		{
			grammarDoc = new GrammarDoc (term);
			m_grammarMap.put (term, grammarDoc);
			m_grammarList.add (grammarDoc);
		}

		return grammarDoc;
	}

	public void addType (TypeDoc type)
	{
		m_types.add (type);
	}

	public void setTable (String type)
	{
		setProperty (PROP_TABLE_TYPE, type);
	}

	public String getTable ()
	{
		String type = (String)getProperty (PROP_TABLE_TYPE);
		if (type == null)
			type = DEFAULT_TABLE_TYPE;
		return type;
	}

	public GrammarDoc[] getGrammars ()
	{
		return m_grammarList.toArray (new GrammarDoc[m_grammarList.size ()]);
	}

	public int getGrammarCount ()
	{
		return m_grammarList.size ();
	}

	public GrammarDoc getGrammar (int index)
	{
		return m_grammarList.get (index);
	}

	public TypeDoc[] getTypes ()
	{
		return m_types.toArray (new TypeDoc[m_types.size ()]);
	}
}
