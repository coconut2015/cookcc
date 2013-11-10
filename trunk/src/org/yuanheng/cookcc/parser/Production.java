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
package org.yuanheng.cookcc.parser;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class Production implements Comparable<Production>
{
	private final short m_id;
	private final int m_symbol;
	private int[] m_production;
	private Token m_precedence;
	private int m_lineNumber;

	public Production (int symbol, short id)
	{
		m_symbol = symbol;
		m_id = id;
		m_precedence = Token.DEFAULT;
	}

	public short getId ()
	{
		return m_id;
	}

	public int size ()
	{
		return m_production.length;
	}

	public int[] getProduction ()
	{
		return m_production;
	}

	void setProduction (int[] production)
	{
		m_production = production;
	}

	public int getSymbol ()
	{
		return m_symbol;
	}

	public Token getPrecedence ()
	{
		return m_precedence;
	}

	void setPrecedence (Token precedence)
	{
		if (precedence == null)
			precedence = Token.DEFAULT;
		m_precedence = precedence;
	}

	public int getLineNumber ()
	{
		return m_lineNumber;
	}

	void setLineNumber (int lineNumber)
	{
		m_lineNumber = lineNumber;
	}

	public int compareTo (Production o)
	{
		if (this == o)
			return 0;
		return m_id - o.m_id;
	}

	public boolean isErrorCorrecting ()
	{
		for (int i = 0; i < m_production.length; ++i)
			if (m_production[i] == 1)
				return true;
		return false;
	}
}
