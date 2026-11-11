/*
 * Copyright (c) 2008, Heng Yuan
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
package org.yuanheng.cookcc;

/**
 * The associations of tokens.
 *
 * @author Heng Yuan
 * @since 0.3
 */
public enum TokenType
{
	/** left associative */
	LEFT ("left"),
	/** right associative */
	RIGHT ("right"),
	/** non-associative */
	NONASSOC ("nonassoc");

	private final String m_str;

	private TokenType (String str)
	{
		m_str = str;
	}

	public String toString ()
	{
		return m_str;
	}
}
