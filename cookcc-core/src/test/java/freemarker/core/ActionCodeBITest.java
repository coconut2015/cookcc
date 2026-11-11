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
public class ActionCodeBITest
{
	@Test
	public void testActionParser () throws Exception
	{
		Assert.assertArrayEquals (new String[]{"abc", "$", "def", "1", "hij" }, ActionCodeBI.parseActionCode ("abc$$def$1hij"));
		Assert.assertArrayEquals (new String[]{ "", "$", "def", "1", "hij" }, ActionCodeBI.parseActionCode ("$$def$1hij"));
	}

	@Test
	public void testNoDelimiters ()
	{
		Assert.assertArrayEquals (new String[]{ "abc" }, ActionCodeBI.parseActionCode ("abc"));
		Assert.assertArrayEquals (new String[0], ActionCodeBI.parseActionCode (""));
	}

	@Test
	public void testDelimiterAtEnd ()
	{
		Assert.assertArrayEquals (new String[]{ "abc", "$" }, ActionCodeBI.parseActionCode ("abc$$"));
		Assert.assertArrayEquals (new String[]{ "x = ", "2" }, ActionCodeBI.parseActionCode ("x = $2"));
	}

	@Test
	public void testAdjacentDelimiters ()
	{
		// Even indexes are always code, odd indexes always delimiters, so an
		// empty code string separates adjacent delimiters.
		Assert.assertArrayEquals (new String[]{ "", "1", "", "2" }, ActionCodeBI.parseActionCode ("$1$2"));
		Assert.assertArrayEquals (new String[]{ "", "$", "", "1", ";" }, ActionCodeBI.parseActionCode ("$$$1;"));
	}

	@Test
	public void testMultiDigitIndex ()
	{
		Assert.assertArrayEquals (new String[]{ "f(", "10", ", ", "2", ")" }, ActionCodeBI.parseActionCode ("f($10, $2)"));
	}

	@Test
	public void testLoneDollarIsCode ()
	{
		Assert.assertArrayEquals (new String[]{ "a$b" }, ActionCodeBI.parseActionCode ("a$b"));
		Assert.assertArrayEquals (new String[]{ "$" }, ActionCodeBI.parseActionCode ("$"));
	}

	@Test
	public void testBuiltInInTemplate () throws Exception
	{
		// The same pattern that templates/java/class.ftl uses for parser actions.
		String source = "<#list action?actioncode() as a><#if a_index % 2 == 0>${a}<#elseif a == \"$\">VAL<#else>ARG(${a})</#if></#list>";
		Assert.assertEquals ("VAL = ARG(1) + ARG(3);", render (source, model ("action", "$$ = $1 + $3;")));
	}

	@Test
	public void testBuiltInReturnsSequence () throws Exception
	{
		Assert.assertEquals ("5", render ("${action?actioncode()?size}", model ("action", "abc$$def$1hij")));
	}

	@Test
	public void testBuiltInRejectsNonBeanString () throws Exception
	{
		// A string literal is a SimpleScalar, not a beans-wrapped StringModel.
		String message = renderError ("${\"abc\"?actioncode()?size}", model ());
		assertContains ("string", message);
	}

	@Test
	public void testBuiltInRejectsNumber () throws Exception
	{
		String message = renderError ("${n?actioncode()?size}", model ("n", Integer.valueOf (1)));
		assertContains ("string", message);
	}
}
