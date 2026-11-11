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
import java.util.Arrays;

import org.yuanheng.cookcc.util.TokenParser;

/**
 * @author Heng Yuan
 * @since 0.4
 */
public class IgnoreDoc extends TreeDoc
{
	private String[] m_list;
	private String[] m_capture;
	private long m_lineNumber;

	public IgnoreDoc ()
	{
	}

	public String[] getList ()
	{
		return m_list;
	}

	public String[] getCapture ()
	{
		return m_capture;
	}

	public void setList (String token)
	{
		if (token == null || (token = token.trim ()).length () == 0)
		{
			throw new RuntimeException ("Ignore list cannot be empty.");
		}
		try
		{
			String[] tokensA = TokenParser.parseString (token);
			if (tokensA == null || tokensA.length == 0)
			{
				throw new RuntimeException ("Ignore list cannot be empty.");
			}

			if (m_list == null)
			{
				Arrays.sort (tokensA);
				m_list = tokensA;
				return;
			}
			else
			{
				throw new RuntimeException ("Multiple ignore lists.");
			}
		}
		catch (IOException ex)
		{
		}
	}

	public void setCapture (String token)
	{
		if (token == null || (token = token.trim ()).length () == 0)
		{
			return;
		}

		try
		{
			String[] tokensA = TokenParser.parseString (token);
			if (tokensA == null || tokensA.length == 0)
			{
				return;
			}

			if (m_capture == null)
			{
				Arrays.sort (tokensA);
				m_capture = tokensA;
				return;
			}
			else
			{
				throw new RuntimeException ("Multiple capture lists.");
			}
		}
		catch (IOException ex)
		{
		}
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
