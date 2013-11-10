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
	private int m_lineNumber;
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

	public int getLineNumber ()
	{
		return m_lineNumber;
	}

	public void setLineNumber (int lineNumber)
	{
		m_lineNumber = lineNumber;
	}
}
