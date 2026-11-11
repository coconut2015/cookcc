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

/**
 * @author Heng Yuan
 */
public class PatternDoc extends TreeDoc
{
	private static int s_idCounter;

	private static int newId ()
	{
		return ++s_idCounter;
	}

	private String m_pattern;
	private boolean m_nocase;
	private boolean m_bol;
	private int m_caseValue = -1;            // special value indicating no case value set, since we do get case 0.
	private boolean m_internal;
	private int m_trailContext;
	private long m_lineNumber;
	private int m_precedence;

	public PatternDoc (boolean internal)
	{
		if (internal)
			m_precedence = Integer.MAX_VALUE;
		else
			m_precedence = newId ();
	}

	public int getPrecedence ()
	{
		return m_precedence;
	}

	public void setPattern (String pattern)
	{
		m_pattern = pattern;
	}

	public String getPattern ()
	{
		return m_pattern;
	}

	public boolean isNocase ()
	{
		return m_nocase;
	}

	public void setNocase (boolean nocase)
	{
		m_nocase = nocase;
	}

	public boolean isBOL ()
	{
		return m_bol;
	}

	public void setBOL (boolean bol)
	{
		m_bol = bol;
	}

	/**
	 * Obtain the case value in the lexer.
	 *
	 * @return the case value in the lexer.
	 */
	public int getCaseValue ()
	{
		return m_caseValue;
	}

	/**
	 * Set the case value in the lexer.  This function is for internal use.
	 *
	 * @param    caseValue Computed caes value in DFA.
	 */
	public void setCaseValue (int caseValue)
	{
		m_caseValue = caseValue;
	}

	/**
	 * Check if this pattern is internally generaeted.
	 *
	 * @return if the rule is internally generated.
	 */
	public boolean isInternal ()
	{
		return m_internal;
	}

	void setInternal ()
	{
		m_lineNumber = Integer.MAX_VALUE;
		m_internal = true;
	}

	/**
	 * This is an internal function which is called after processing the pattern
	 * to determine the trail context.
	 *
	 * @param    trailContext the trail context of the pattern.
	 */
	public void setTrailContext (int trailContext)
	{
		m_trailContext = trailContext;
	}

	/**
	 * Get the trail context of the pattern.
	 *
	 * @return the trail context of the pattern.
	 */
	public int getTrailContext ()
	{
		return m_trailContext;
	}

	public int getTrailLength ()
	{
		return m_trailContext >> 2;
	}

	public long getLineNumber ()
	{
		return m_lineNumber;
	}

	public void setLineNumber (long lineNumber)
	{
		m_lineNumber = lineNumber;
	}
}
