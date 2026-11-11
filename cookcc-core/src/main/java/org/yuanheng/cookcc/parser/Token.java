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
package org.yuanheng.cookcc.parser;

/**
 * @author Heng Yuan
 */
public class Token
{
	public final static int LEFT = 1;
	public final static int RIGHT = 2;
	public final static int NONASSOC = 3;

	public final static Token DEFAULT = new Token ("@default", 0, 0, Token.NONASSOC);

	public final String name;
	public final int level;
	public final int value;
	public final int type;

	public Token (String name, int level, int value, int type)
	{
		this.name = name;
		this.level = level;
		this.value = value;
		this.type = type;
	}

	public Token (String name, int level, int value, String type)
	{
		this.name = name;
		this.level = level;
		this.value = value;
		if (type == null || "nonassoc".equals (type))
			this.type = NONASSOC;
		else if ("left".equals (type))
			this.type = LEFT;
		else
			this.type = RIGHT;
	}

	public String getName ()
	{
		return name;
	}

	public int getValue ()
	{
		return value;
	}
}
