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

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class RhsDoc extends TreeDoc
{
	private String m_terms = "";
	private int m_caseValue = -1;            // special value indicating no case value set, since we do get case 0.
	private int m_lineNumber;
	private int m_actionLineNumber;
	private String m_action = "";
	private String m_precedence;

	public RhsDoc ()
	{
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

	/** Obtain the case value in the lexer. */
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

	public int getLineNumber ()
	{
		return m_lineNumber;
	}

	public void setLineNumber (int lineNumber)
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
}
