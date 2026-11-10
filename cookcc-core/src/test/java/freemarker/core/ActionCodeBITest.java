/*
 * Copyright (c) 2008-2013, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *    Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    Neither the name of the Heng Yuan nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY Heng Yuan ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Heng Yuan BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
