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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests ?stringresource and how ?javastring uses the string resources it
 * declares, as in templates/java/class.ftl (longintarray / intarray macros).
 *
 * @author Heng Yuan
 */
public class JavaStringResourceBITest
{
	private static final int MAX_LEN = 16383;
	private static final String HEADER = "\t\tprivate final static String s_s";

	private static String stringResource (Object array) throws Exception
	{
		return render ("${a?stringresource()}", model ("a", array));
	}

	private static String repeat (String s, int count)
	{
		StringBuilder builder = new StringBuilder ();
		for (int i = 0; i < count; ++i)
			builder.append (s);
		return builder.toString ();
	}

	private static int[] filled (int length, int value)
	{
		int[] array = new int[length];
		Arrays.fill (array, value);
		return array;
	}

	@Before
	public void setUp ()
	{
		// JavaCodeGen resets before each generated class; do the same per test.
		JavaStringResourceBI.reset ();
	}

	@Test
	public void testShortArrayNeedsNoResource () throws Exception
	{
		int[] array = { 1, 2, 3 };
		Assert.assertEquals ("", stringResource (array));
		Assert.assertNull (JavaStringResourceBI.getStringResource (array));
		// ?javastring then falls back to an inline string.
		Assert.assertEquals ("(\"\\001\\002\\003\").toCharArray ()", render ("${a?javastring()}", model ("a", array)));
	}

	@Test
	public void testArrayAtLimitNeedsNoResource () throws Exception
	{
		int[] array = filled (MAX_LEN, 'a');
		Assert.assertEquals ("", stringResource (array));
		Assert.assertNull (JavaStringResourceBI.getStringResource (array));
	}

	@Test
	public void testLongArrayDeclaresResources () throws Exception
	{
		int[] array = filled (MAX_LEN + 1, 'a');
		String expected = HEADER + "1 = \"" + repeat ("\\141", MAX_LEN) + "\";\n"
						+ HEADER + "2 = \"\\141\";\n";
		Assert.assertEquals (expected, stringResource (array));
		Assert.assertEquals ("s_s1.toString () + s_s2.toString ()", JavaStringResourceBI.getStringResource (array));
	}

	@Test
	public void testThreePieces () throws Exception
	{
		int[] array = filled (MAX_LEN * 2 + 1, 'a');
		String output = stringResource (array);
		Assert.assertTrue (output.contains (HEADER + "3 = \"\\141\";\n"));
		Assert.assertEquals ("s_s1.toString () + s_s2.toString () + s_s3.toString ()",
							 JavaStringResourceBI.getStringResource (array));
	}

	@Test
	public void testSplitCountsUtf8Length () throws Exception
	{
		int[] array = filled (MAX_LEN / 2 + 1, 0x80);
		Assert.assertTrue (stringResource (array).endsWith (HEADER + "2 = \"\\u0080\";\n"));
	}

	@Test
	public void testJavaStringUsesDeclaredResource () throws Exception
	{
		int[] array = filled (MAX_LEN + 1, 'a');
		String output = render ("${a?stringresource()}|${a?javastring()}", model ("a", array));
		Assert.assertTrue (output.endsWith ("|(s_s1.toString () + s_s2.toString ()).toCharArray ()"));
	}

	@Test
	public void testTwoLongArraysUseDistinctFields () throws Exception
	{
		// Like several <@longintarray .../> calls in one generated class: the
		// second array must not declare s_s1 / s_s2 again.
		int[] first = filled (MAX_LEN + 1, 'a');
		int[] second = filled (MAX_LEN + 1, 'b');
		String output = render ("${a?stringresource()}${b?stringresource()}", model ("a", first, "b", second));
		for (int i = 1; i <= 4; ++i)
			Assert.assertEquals ("s_s" + i + " declarations", 1, output.split (HEADER + i + " = ", -1).length - 1);
		Assert.assertEquals ("s_s1.toString () + s_s2.toString ()", JavaStringResourceBI.getStringResource (first));
		Assert.assertEquals ("s_s3.toString () + s_s4.toString ()", JavaStringResourceBI.getStringResource (second));
	}

	@Test
	public void testShortArrayDoesNotAdvanceCounter () throws Exception
	{
		int[] small = { 1, 2, 3 };
		int[] large = filled (MAX_LEN + 1, 'a');
		render ("${s?stringresource()}${l?stringresource()}", model ("s", small, "l", large));
		Assert.assertEquals ("s_s1.toString () + s_s2.toString ()", JavaStringResourceBI.getStringResource (large));
	}

	@Test
	public void testResetRestartsNumbering () throws Exception
	{
		int[] first = filled (MAX_LEN + 1, 'a');
		stringResource (first);
		JavaStringResourceBI.reset ();
		Assert.assertNull (JavaStringResourceBI.getStringResource (first));

		int[] second = filled (MAX_LEN + 1, 'b');
		Assert.assertTrue (stringResource (second).startsWith (HEADER + "1 = "));
	}

	@Test
	public void testResourcesAreKeyedByIdentity () throws Exception
	{
		int[] array = filled (MAX_LEN + 1, 'a');
		stringResource (array);
		Assert.assertNull (JavaStringResourceBI.getStringResource (array.clone ()));
	}

	@Test
	public void testRejectsNonArray () throws Exception
	{
		assertContains ("array", renderError ("${s?stringresource()}", model ("s", "abc")));
	}
}
