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
package org.yuanheng.cookcc.doc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Heng Yuan
 * @version $Id$
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
