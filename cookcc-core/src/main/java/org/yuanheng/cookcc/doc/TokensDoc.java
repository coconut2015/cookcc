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

import java.io.IOException;

import org.yuanheng.cookcc.util.TokenParser;

/**
 * @author Heng Yuan
 */
public class TokensDoc extends TreeDoc
{
	private String m_type;
	private String[] m_tokens;
	private int m_lineNumber;

	public TokensDoc ()
	{
	}

	public void setType (String type)
	{
		m_type = type;
	}

	public String getType ()
	{
		return m_type;
	}

	public String[] getTokens ()
	{
		return m_tokens;
	}

	public void setTokens (String tokens) throws IOException
	{
		m_tokens = TokenParser.parseString (tokens);
	}

	public void addTokens (String token)
	{
		if (token == null || (token = token.trim ()).length () == 0)
			return;
		try
		{
			String[] tokensA = TokenParser.parseString (token);
			if (tokensA == null || tokensA.length == 0)
				return;
			if (m_tokens == null)
			{
				m_tokens = tokensA;
				return;
			}
			String[] newA = new String[m_tokens.length + tokensA.length];
			System.arraycopy (m_tokens, 0, newA, 0, m_tokens.length);
			System.arraycopy (tokensA, 0, newA, m_tokens.length, tokensA.length);
			m_tokens = newA;
		}
		catch (IOException ex)
		{
		}
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
