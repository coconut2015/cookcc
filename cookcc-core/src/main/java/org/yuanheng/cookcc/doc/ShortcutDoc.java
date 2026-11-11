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

/**
 * @author Heng Yuan
 */
public class ShortcutDoc extends TreeDoc
{
	private String m_name;
	private String m_pattern;

	public String getName ()
	{
		return m_name;
	}

	public void setName (String name)
	{
		m_name = name;
	}

	public String getPattern ()
	{
		return m_pattern;
	}

	public void setPattern (String pattern)
	{
		m_pattern = pattern;
	}
}
