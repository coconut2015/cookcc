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
package junit_tests;

import org.junit.Assert;
import org.junit.Test;
import org.yuanheng.cookcc.exception.EscapeSequenceException;
import org.yuanheng.cookcc.lexer.CCL;
import org.yuanheng.cookcc.lexer.NFAFactory;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class TestCCL
{
	@Test
	public void escTest () throws EscapeSequenceException
	{
		Assert.assertEquals ('a', CCL.esc ("\\abc".toCharArray (), new int[]{ 0 }));
		Assert.assertEquals ('\b', CCL.esc ("\\bc".toCharArray (), new int[]{ 0 }));

		Assert.assertEquals ('\1', CCL.esc ("\\1".toCharArray (), new int[]{ 0 }));
		Assert.assertEquals ('\12', CCL.esc ("\\12".toCharArray (), new int[]{ 0 }));
		Assert.assertEquals ('\123', CCL.esc ("\\123".toCharArray (), new int[]{ 0 }));
		Assert.assertEquals ('\123', CCL.esc ("\\1234".toCharArray (), new int[]{ 0 }));
		Assert.assertEquals ('\123', CCL.esc ("\\123]".toCharArray (), new int[]{ 0 }));

		Assert.assertEquals ('\b', CCL.esc ("\b".toCharArray (), new int[]{ 0 }));

		Assert.assertEquals (1, CCL.esc ("\\x1".toCharArray (), new int[]{ 0 }));
		Assert.assertEquals (18, CCL.esc ("\\x12".toCharArray (), new int[]{ 0 }));

		Assert.assertEquals ('\u655f', CCL.esc ("\\u655f".toCharArray (), new int[]{ 0 }));
		Assert.assertEquals ('\u1234', CCL.esc ("\\u12345".toCharArray (), new int[]{ 0 }));
		Assert.assertEquals ('\u1234', CCL.esc ("\\u1234]".toCharArray (), new int[]{ 0 }));
	}

	@Test
	public void testCCL () throws Exception
	{
		CCL ccl = NFAFactory.getByteNFAFactory ().getCCL ();
		Assert.assertEquals ("[x]", ccl.toString (ccl.parseCCL ("[x]")));
		Assert.assertEquals ("[Zabj-o]", ccl.toString (ccl.parseCCL ("[abj-oZ]")));
		Assert.assertEquals ("[^A-Z]", ccl.toString (ccl.parseCCL ("[^A-Z]")));
		Assert.assertEquals ("[^\\nA-Z]", ccl.toString (ccl.parseCCL ("[^A-Z\\n]")));
	}
}
