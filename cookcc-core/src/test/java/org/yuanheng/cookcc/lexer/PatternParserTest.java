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
package org.yuanheng.cookcc.lexer;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Heng Yuan
 */
public class PatternParserTest
{
	@Test
	public void testPattern () throws Exception
	{
		PatternParser parser = new PatternParser (null, CCL.getByteCCL ());
		LexerPattern pattern;
		pattern = parser.parse (0, 1, ".");
		Assert.assertEquals (".", pattern.toString ());
		pattern = parser.parse (0, 1, ".|a");
		Assert.assertEquals (".", pattern.toString ());
		pattern = parser.parse (0, 1, ".|\n");
		Assert.assertEquals ("(.|\\n)", pattern.toString ());
		pattern = parser.parse (0, 1, "a*b*c*");
		Assert.assertEquals ("(a*b*c*)", pattern.toString ());
		pattern = parser.parse (0, 1, "abcdefg");
		Assert.assertEquals ("(abcdefg)", pattern.toString ());
		pattern = parser.parse (0, 1, "a[a-z]{-}[abc]{+}[c]b");
		Assert.assertEquals ("(a[c-z]b)", pattern.toString ());
		pattern = parser.parse (0, 1, "a*a+a?a{1}a{1,}a{,1}a{1,2}");
		Assert.assertEquals ("(a*a+a?a{1}a{1,}a{,1}a{1,2})", pattern.toString ());
		pattern = parser.parse (0, 1, "cat|dog");
		Assert.assertEquals ("((cat)|(dog))", pattern.toString ());
		pattern = parser.parse (0, 1, "cat|d(og)");
		Assert.assertEquals ("((cat)|(d(og)))", pattern.toString ());
		pattern = parser.parse (0, 1, "ca[t]|d(og)");
		Assert.assertEquals ("((ca[t])|(d(og)))", pattern.toString ());
		pattern = parser.parse (0, 1, "a*b*|c*d+|e?f{2,3}");
		Assert.assertEquals ("(((a*b*)|(c*d+))|(e?f{2,3}))", pattern.toString ());
	}

	@Test
	public void testMergePattern () throws Exception
	{
		PatternParser parser = new PatternParser (null, CCL.getByteCCL ());
		LexerPattern pattern;
		pattern = parser.parse (0, 1, "a|b|c|d");
		Assert.assertEquals ("[a-d]", pattern.toString ());
		pattern = parser.parse (0, 1, "[a-d]|b|c|[x-z]");
		Assert.assertEquals ("[a-dx-z]", pattern.toString ());
	}

	@Test
	public void testCCLPattern () throws Exception
	{
		PatternParser parser = new PatternParser (null, CCL.getByteCCL ());
		LexerPattern pattern;
		pattern = parser.parse (0, 1, "[a]|[b]");
		Assert.assertEquals ("[ab]", pattern.toString ());
		//pattern = parser.parse ("[[:alpha:]]");
		//Assert.assertEquals ("[A-Za-z]", pattern.toString ());
		pattern = parser.parse (0, 1, "[a-zA-Z]");
		Assert.assertEquals ("[A-Za-z]", pattern.toString ());
	}

	@Test
	public void testQuotePattern () throws Exception
	{
		PatternParser parser = new PatternParser (null, CCL.getByteCCL ());
		LexerPattern pattern;
		pattern = parser.parse (0, 1, "'a'");
		Assert.assertEquals ("a", pattern.toString ());
		pattern = parser.parse (0, 1, "\"a\"");
		Assert.assertEquals ("a", pattern.toString ());
		pattern = parser.parse (0, 1, "'ab'");
		Assert.assertEquals ("(ab)", pattern.toString ());
		pattern = parser.parse (0, 1, "\"ab\"");
		Assert.assertEquals ("(ab)", pattern.toString ());
		pattern = parser.parse (0, 1, "\"'ab'\"");
		Assert.assertEquals ("(\\'ab\\')", pattern.toString ());
		pattern = parser.parse (0, 1, "'\"ab\"'");
		Assert.assertEquals ("(\\\"ab\\\")", pattern.toString ());
		pattern = parser.parse (0, 1, "'\\s\\t'");
		Assert.assertEquals ("(\\s\\t)", pattern.toString ());
	}
}
