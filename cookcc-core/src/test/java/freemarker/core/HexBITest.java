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
package freemarker.core;

import static freemarker.core.TemplateTestHelper.*;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Heng Yuan
 */
public class HexBITest
{
	private static String hex (int value, int minLength) throws Exception
	{
		return render ("${n?hex(" + minLength + ")}", model ("n", Integer.valueOf (value)));
	}

	@Test
	public void testPadding () throws Exception
	{
		Assert.assertEquals ("00ff", hex (255, 4));
		Assert.assertEquals ("0000", hex (0, 4));
		Assert.assertEquals ("1234", hex (0x1234, 4));
	}

	@Test
	public void testNoPaddingWhenLongEnough () throws Exception
	{
		Assert.assertEquals ("ff", hex (255, 1));
		Assert.assertEquals ("ff", hex (255, 0));
		Assert.assertEquals ("12345", hex (0x12345, 4));
	}

	@Test
	public void testLowerCase () throws Exception
	{
		Assert.assertEquals ("abcdef", hex (0xabcdef, 0));
	}

	@Test
	public void testNegativeIsTwosComplement () throws Exception
	{
		Assert.assertEquals ("ffffffff", hex (-1, 0));
	}

	@Test
	public void testNumberLiteral () throws Exception
	{
		Assert.assertEquals ("000a", render ("${10?hex(4)}", model ()));
	}

	@Test
	public void testFractionTruncated () throws Exception
	{
		Assert.assertEquals ("0f", render ("${n?hex(2)}", model ("n", Double.valueOf (15.9))));
	}

	@Test
	public void testRejectsNonNumberTarget () throws Exception
	{
		String message = renderError ("${s?hex(2)}", model ("s", "abc"));
		assertContains ("number", message);
	}

	@Test
	public void testRequiresOneArgument () throws Exception
	{
		assertContains ("exactly 1 argument", renderError ("${10?hex()}", model ()));
		assertContains ("exactly 1 argument", renderError ("${10?hex(1, 2)}", model ()));
	}

	@Test
	public void testArgumentMustBeNumber () throws Exception
	{
		assertContains ("must be a number", renderError ("${10?hex(\"4\")}", model ()));
	}
}
