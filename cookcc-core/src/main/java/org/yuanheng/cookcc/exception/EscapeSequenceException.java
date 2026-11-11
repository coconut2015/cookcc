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
public class EscapeSequenceException extends RuntimeException
{
	private static final long serialVersionUID = -8522981046480956423L;

	public static MessageFormat ERROR_MSG = new MessageFormat ("invalid escape sequence {0}");

	private String m_esc;

	public EscapeSequenceException (String esc)
	{
		m_esc = esc;
	}

	@Override
	public String toString ()
	{
		return ERROR_MSG.format (new String[]{m_esc});
	}
}
