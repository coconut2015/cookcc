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
import java.text.MessageFormat;

import org.yuanheng.cookcc.util.TokenParser;

/**
 * @author Heng Yuan
 */
public class TypeDoc extends TreeDoc
{
	private MessageFormat m_format;
	private String[] m_symbols;

	public TypeDoc ()
	{
	}

	public void setFormat (String format)
	{
		m_format = new MessageFormat (format);
	}

	public MessageFormat getFormat ()
	{
		return m_format;
	}

	public void setSymbols (String symbols) throws IOException
	{
		m_symbols = TokenParser.parseString (symbols);
	}

	public String[] getSymbols ()
	{
		return m_symbols;
	}
}
