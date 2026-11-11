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

import java.util.HashMap;

/**
 * @author Heng Yuan
 */
public abstract class TreeDoc
{
	private HashMap<String, Object> m_properties;

	public void setProperty (String property, Object value)
	{
		if (property == null)
			return;
		if (m_properties == null)
			m_properties = new HashMap<String, Object> ();
		if (value == null)
			m_properties.remove (property);
		else
			m_properties.put (property, value);
	}

	public Object getProperty (String property)
	{
		if (m_properties == null)
			return null;
		return m_properties.get (property);
	}
}
