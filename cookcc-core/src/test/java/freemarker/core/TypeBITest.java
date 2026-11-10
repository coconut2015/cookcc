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

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.yuanheng.cookcc.doc.RhsDoc;
import org.yuanheng.cookcc.parser.Production;

/**
 * Tests ?type, which templates/java/class.ftl uses to cast $$ / $n values in
 * parser actions to the type declared for the symbol.
 *
 * @author Heng Yuan
 */
public class TypeBITest
{
	private static final int EXPR = 300;
	private static final int NUMBER = 301;
	private static final int PLUS = '+';

	/**
	 * Builds the RhsDoc for "expr : expr '+' NUMBER" the way the Parser does:
	 * the Production is stored as the RhsDoc's "Production" property.
	 */
	private static RhsDoc createRhs () throws Exception
	{
		Production production = new Production (EXPR, (short)1);
		// setProduction is package-private in org.yuanheng.cookcc.parser.
		Method setProduction = Production.class.getDeclaredMethod ("setProduction", int[].class);
		setProduction.setAccessible (true);
		setProduction.invoke (production, new Object[] { new int[]{ EXPR, PLUS, NUMBER } });

		RhsDoc rhs = new RhsDoc ();
		rhs.setProperty ("Production", production);
		return rhs;
	}

	private static Map<Integer, MessageFormat> createFormats ()
	{
		Map<Integer, MessageFormat> formats = new HashMap<Integer, MessageFormat> ();
		formats.put (EXPR, new MessageFormat ("((Expr){0})"));
		formats.put (NUMBER, new MessageFormat ("((Integer){0})"));
		return formats;
	}

	private static String type (String term) throws Exception
	{
		// Same call as the "type" macro in class.ftl: a?type(b, c, d).  The term is a
		// template string, as it is there (an element of ?actioncode ()).
		String source = "${rhs?type(\"" + term + "\", formats, \"yyGetValue (" + term + ")\")}";
		return render (source, model ("rhs", createRhs (), "formats", createFormats ()));
	}

	@Test
	public void testDollarDollarUsesLhsType () throws Exception
	{
		Assert.assertEquals ("((Expr)yyGetValue ($))", type ("$"));
	}

	@Test
	public void testPositionalTermUsesItsType () throws Exception
	{
		Assert.assertEquals ("((Expr)yyGetValue (1))", type ("1"));
		Assert.assertEquals ("((Integer)yyGetValue (3))", type ("3"));
	}

	@Test
	public void testUntypedTermIsUnchanged () throws Exception
	{
		Assert.assertEquals ("yyGetValue (2)", type ("2"));
	}

	@Test
	public void testWithActionCode () throws Exception
	{
		// The combination class.ftl uses to generate a parser action.
		String source = "<#list action?actioncode() as a><#if a_index % 2 == 0>${a}<#elseif a == \"$\">_yyValue"
					  + "<#else>${rhs?type(a, formats, \"yyGetValue (\" + a + \")\")}</#if></#list>";
		String output = render (source, model ("rhs", createRhs (), "formats", createFormats (),
											   "action", "$$ = add ($1, $3);"));
		Assert.assertEquals ("_yyValue = add (((Expr)yyGetValue (1)), ((Integer)yyGetValue (3)));", output);
	}

	@Test
	public void testRejectsNonBean () throws Exception
	{
		// A string literal is a SimpleScalar, not a BeanModel.  (Beans-wrapped
		// numbers and strings are BeanModels, so they would pass this check.)
		String message = renderError ("${\"abc\"?type(\"$\", formats, \"x\")}", model ("formats", createFormats ()));
		assertContains ("bean", message);
	}

	@Test
	public void testParseActionCodeMatchesActionCodeBI ()
	{
		String[] inputs = { "abc$$def$1hij", "$$def$1hij", "", "abc", "$1$2", "f($10, $2)", "a$b" };
		for (String input : inputs)
			Assert.assertArrayEquals (input, ActionCodeBI.parseActionCode (input), TypeBI.parseActionCode (input));
	}
}
