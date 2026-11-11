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
package org.yuanheng.cookcc.parser;

/**
 * @author Heng Yuan
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
