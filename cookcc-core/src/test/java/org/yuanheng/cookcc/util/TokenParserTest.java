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
package org.yuanheng.cookcc.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Heng Yuan
 */
public class TokenParserTest
{
	@Test
	public void testActionParser () throws Exception
	{
		Assert.assertArrayEquals (new String[]{ "abc", "def", "'<'", "'>'", "123" }, TokenParser.parseString ("abc def '<' '>' 123 "));
		Assert.assertArrayEquals (new String[]{ "abc", "\\x12", "\\123" }, TokenParser.parseString ("abc \\x12 \\123 "));
	}
}
