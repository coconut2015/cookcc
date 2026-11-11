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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Heng Yuan
 */
public class JavaStringBITest
{
	/** The maximum UTF-8 length of one string constant in a class file is 65535; CookCC splits at 16383. */
	private static final int MAX_LEN = 16383;

	private static String javaString (Object array) throws Exception
	{
		return render ("${a?javastring()}", model ("a", array));
	}

	private static String repeat (String s, int count)
	{
		StringBuilder builder = new StringBuilder ();
		for (int i = 0; i < count; ++i)
			builder.append (s);
		return builder.toString ();
	}

	@Test
	public void testAsciiUsesOctal () throws Exception
	{
		Assert.assertEquals ("(\"\\000\\012\\015\\042\\101\\177\").toCharArray ()",
							 javaString (new int[]{ 0, '\n', '\r', '"', 'A', 127 }));
	}

	@Test
	public void testNonAsciiUsesUnicodeEscape () throws Exception
	{
		Assert.assertEquals ("(\"\\u0080\\u07ff\\u1234\\uffff\").toCharArray ()",
							 javaString (new int[]{ 0x80, 0x7ff, 0x1234, 0xffff }));
	}

	@Test
	public void testValuesAreTruncatedToChar () throws Exception
	{
		// Values go through shortValue () & 0xffff.
		Assert.assertEquals ("(\"\\uffff\\000\\001\").toCharArray ()",
							 javaString (new int[]{ -1, 0x10000, 0x10001 }));
	}

	@Test
	public void testShortArray () throws Exception
	{
		Assert.assertEquals ("(\"\\001\\uffff\").toCharArray ()", javaString (new short[]{ 1, -1 }));
	}

	@Test
	public void testEmptyArray () throws Exception
	{
		Assert.assertEquals ("(\"\").toCharArray ()", javaString (new int[0]));
	}

	@Test
	public void testLongAsciiStringIsSplit () throws Exception
	{
		int[] array = new int[MAX_LEN + 1];
		Arrays.fill (array, 'a');
		String expected = "(\"" + repeat ("\\141", MAX_LEN) + "\" + \"\\141\").toCharArray ()";
		Assert.assertEquals (expected, javaString (array));
	}

	@Test
	public void testAsciiStringAtLimitIsNotSplit () throws Exception
	{
		int[] array = new int[MAX_LEN];
		Arrays.fill (array, 'a');
		Assert.assertFalse (javaString (array).contains ("\" + \""));
	}

	@Test
	public void testSplitCountsUtf8Length () throws Exception
	{
		// 0x80 takes 2 bytes in UTF-8: 8191 of them is 16382 bytes, which fits.
		int[] array = new int[MAX_LEN / 2];
		Arrays.fill (array, 0x80);
		Assert.assertFalse (javaString (array).contains ("\" + \""));

		// One more makes 16384 bytes, so the last character starts a new piece.
		array = new int[MAX_LEN / 2 + 1];
		Arrays.fill (array, 0x80);
		Assert.assertTrue (javaString (array).endsWith ("\" + \"\\u0080\").toCharArray ()"));

		// 0x800 takes 3 bytes: 5461 of them is 16383 bytes, which fits.
		array = new int[MAX_LEN / 3];
		Arrays.fill (array, 0x800);
		Assert.assertFalse (javaString (array).contains ("\" + \""));

		array = new int[MAX_LEN / 3 + 1];
		Arrays.fill (array, 0x800);
		Assert.assertTrue (javaString (array).endsWith ("\" + \"\\u0800\").toCharArray ()"));
	}

	@Test
	public void testRejectsNonArray () throws Exception
	{
		assertContains ("array", renderError ("${s?javastring()}", model ("s", "abc")));
	}
}
