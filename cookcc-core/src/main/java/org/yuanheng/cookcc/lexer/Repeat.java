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
package org.yuanheng.cookcc.lexer;

/**
 * @author Heng Yuan
 */
class Repeat
{
	private final int m_min;
	private final int m_max;

	public Repeat (int min, int max)
	{
		m_min = min;
		m_max = max;
	}

	public int getMin ()
	{
		return m_min;
	}

	public int getMax ()
	{
		return m_max;
	}

	@Override
	public String toString ()
	{
		if (m_min == m_max)
			return "{" + m_min + "}";
		String text;
		if (m_min == 0)
			text = "{,";
		else
			text = "{" + m_min + ",";
		if (m_max == Integer.MAX_VALUE)
			text += "}";
		else
			text += m_max + "}";
		return text;
	}
}
