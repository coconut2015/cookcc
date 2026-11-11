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
package org.yuanheng.cookcc.exception;

import java.text.MessageFormat;

/**
 * @author Heng Yuan
 */
public class CookCCException extends RuntimeException
{
	private static final long serialVersionUID = -1202462319594574689L;

	public static MessageFormat ERROR_MSG = new MessageFormat ("Error on line {0}: {1}.");

	private final long m_lineNumber;
	private final String m_msg;

	public CookCCException (long lineNumber, String msg)
	{
		m_lineNumber = lineNumber;
		m_msg = msg;
	}

	public long getLineNumber ()
	{
		return m_lineNumber;
	}

	@Override
	public String toString ()
	{
		Object[] objs = new Object[]{new Long (m_lineNumber), m_msg};
		return ERROR_MSG.format (objs);
	}
}
