/*
 * Copyright (c) 2008, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Heng Yuan nor the
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
package org.yuanheng.cookcc.lexer;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Heng Yuan
 * @version $Id: zz_CCL.java 46 2008-10-14 15:48:25Z superduperhengyuan $
 */
public class zz_PatternParser
{
	@Test
	public void testPattern () throws Exception
	{
		PatternParser parser = new PatternParser (CCL.getByteCCL ());
		LexerPattern pattern;
		pattern = parser.parse ("abcdefg");
		Assert.assertEquals ("(abcdefg)", pattern.toString ());
		pattern = parser.parse ("a[a-z]{-}[abc]{+}[c]b");
		Assert.assertEquals ("(a[c-z]b)", pattern.toString ());
		pattern = parser.parse ("a*a+a?a{1}a{1,}a{,1}a{1,2}");
		Assert.assertEquals ("(a*a+a?a{1}a{1,}a{,1}a{1,2})", pattern.toString ());
		pattern = parser.parse ("ca[t]|d(og)");
		Assert.assertEquals ("((ca[t])|(d(og)))", pattern.toString ());
		pattern = parser.parse ("a*b*|c*d+|e?f{2,3}");
		Assert.assertEquals ("(((a*b*)|(c*d+))|(e?f{2,3}))", pattern.toString ());
	}

	@Test
	public void testMergePattern () throws Exception
	{
		PatternParser parser = new PatternParser (CCL.getByteCCL ());
		LexerPattern pattern;
		pattern = parser.parse ("a|b|c|d");
		Assert.assertEquals ("[a-d]", pattern.toString ());
		pattern = parser.parse ("[a-d]|b|c|[x-z]");
		Assert.assertEquals ("[a-dx-z]", pattern.toString ());
	}

	@Test
	public void testCCLPattern () throws Exception
	{
		PatternParser parser = new PatternParser (CCL.getByteCCL ());
		LexerPattern pattern;
		pattern = parser.parse ("[a]|[b]");
		Assert.assertEquals ("[ab]", pattern.toString ());
		//pattern = parser.parse ("[[:alpha:]]");
		//Assert.assertEquals ("[A-Za-z]", pattern.toString ());
		pattern = parser.parse ("[a-zA-Z]");
		Assert.assertEquals ("[A-Za-z]", pattern.toString ());
	}

	@Test
	public void testQuotePattern () throws Exception
	{
		PatternParser parser = new PatternParser (CCL.getByteCCL ());
		LexerPattern pattern;
		pattern = parser.parse ("'a'");
		Assert.assertEquals ("a", pattern.toString ());
		pattern = parser.parse ("\"a\"");
		Assert.assertEquals ("a", pattern.toString ());
		pattern = parser.parse ("'ab'");
		Assert.assertEquals ("(ab)", pattern.toString ());
		pattern = parser.parse ("\"ab\"");
		Assert.assertEquals ("(ab)", pattern.toString ());
		pattern = parser.parse ("\"'ab'\"");
		Assert.assertEquals ("(\\'ab\\')", pattern.toString ());
		pattern = parser.parse ("'\"ab\"'");
		Assert.assertEquals ("(\\\"ab\\\")", pattern.toString ());
		pattern = parser.parse ("'\\s\\t'");
		Assert.assertEquals ("(\\s\\t)", pattern.toString ());
	}
}
