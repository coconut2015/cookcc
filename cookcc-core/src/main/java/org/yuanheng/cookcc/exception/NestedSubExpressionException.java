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
public class NestedSubExpressionException extends CookCCException
{
	private static final long serialVersionUID = -4336458045012222054L;

	public static MessageFormat ERROR_MSG = new MessageFormat ("Nested subexpression is not supported: {0}");

	private final String m_pattern;

	public NestedSubExpressionException (long lineNumber, String pattern)
	{
		super (lineNumber, ERROR_MSG.format (new Object[]{pattern}));
		m_pattern = pattern;
	}

	public String getPattern ()
	{
		return m_pattern;
	}
}
