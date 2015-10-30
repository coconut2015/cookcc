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

import java.io.IOException;
import java.util.Arrays;

import org.yuanheng.cookcc.util.TokenParser;

/**
 * @author Heng Yuan
 * @version $Id$
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
