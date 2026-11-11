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
public class InvalidRegExException extends ParserException
{
	private static final long serialVersionUID = 1824015010890844313L;

	public static MessageFormat ERROR_MSG = new MessageFormat ("invalid regular expression: {0}");

	private final String m_regex;

	public InvalidRegExException (long lineNumber, String msg, String input)
	{
		super (lineNumber, msg);
		m_regex = input;
	}

	public InvalidRegExException (long lineNumber, String input)
	{
		super (lineNumber, ERROR_MSG.format (new Object[]{input}));
		m_regex = input;
	}

	public String getRegex ()
	{
		return m_regex;
	}
}
