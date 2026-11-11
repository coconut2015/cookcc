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
public class RhsDoc extends TreeDoc
{
	private String m_terms = "";
	private int m_caseValue = -1;            // special value indicating no case value set, since we do get case 0.
	private long m_lineNumber;
	private int m_actionLineNumber;
	private String m_action = "";
	private String m_precedence;

	private final int m_termCount;
	private String m_transTerms;

	public RhsDoc ()
	{
		m_termCount = 0;
	}

	/**
	 * This constructor is used by internal rule generation.
	 *
	 * @param	termCount
	 * 			the number of terms on the rhs.
	 */
	public RhsDoc (int termCount)
	{
		m_termCount = termCount;
	}

	public void setTerms (String terms)
	{
		if (terms == null)
			terms = "";
		m_terms = terms;
	}

	public String getTerms ()
	{
		return m_terms;
	}

	/**
	 * Set the translated terms.  For example, for rule such as
	 *     A := B C+
	 * The translated term is
	 *     A := B @1
	 * where @1 is an internal symbol for handling C+.
	 *
	 * @param	terms
	 * 			the translated terms.
	 */
	public void internalSetTranslatedTerms (String terms)
	{
		m_transTerms = terms;
	}

	public String getTranslatedTerms ()
	{
		return m_transTerms;
	}

	/**
	 * Obtain the case value in the lexer.
	 *
	 * @return	the case value.
	 */
	public int getCaseValue ()
	{
		return m_caseValue;
	}

	/**
	 * Set the case value in the parser.  This function is for internal use.
	 *
	 * @param    caseValue Computed caes value in DFA.
	 */
	public void setCaseValue (int caseValue)
	{
		m_caseValue = caseValue;
	}

	public long getLineNumber ()
	{
		return m_lineNumber;
	}

	public void setLineNumber (long lineNumber)
	{
		m_lineNumber = lineNumber;
	}

	public int getActionLineNumber ()
	{
		return m_actionLineNumber;
	}

	public void setActionLineNumber (int actionLineNumber)
	{
		m_actionLineNumber = actionLineNumber;
	}

	public String getAction ()
	{
		return m_action;
	}

	public void setAction (String action)
	{
		if (action == null)
			action = "";
		m_action = action;
	}

	public String getPrecedence ()
	{
		return m_precedence;
	}

	public void setPrecedence (String precedence)
	{
		m_precedence = precedence;
	}

	public int getTermCount ()
	{
		return m_termCount;
	}
}
