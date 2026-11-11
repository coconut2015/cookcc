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
package org.yuanheng.cookcc.lexer;

import org.junit.Assert;
import org.junit.Test;
import org.yuanheng.cookcc.exception.EscapeSequenceException;

/**
 * @author Heng Yuan
 */
public class CCLTest
{
	@Test
	public void escTest () throws EscapeSequenceException
	{
		Assert.assertEquals ('a', CCL.esc ("\\abc", new int[]{ 0 }));
		Assert.assertEquals ('\b', CCL.esc ("\\bc", new int[]{ 0 }));

		Assert.assertEquals ('\1', CCL.esc ("\\1", new int[]{ 0 }));
		Assert.assertEquals ('\12', CCL.esc ("\\12", new int[]{ 0 }));
		Assert.assertEquals ('\123', CCL.esc ("\\123", new int[]{ 0 }));
		Assert.assertEquals ('\123', CCL.esc ("\\1234", new int[]{ 0 }));
		Assert.assertEquals ('\123', CCL.esc ("\\123]", new int[]{ 0 }));

		Assert.assertEquals ('\b', CCL.esc ("\b", new int[]{ 0 }));

		Assert.assertEquals (1, CCL.esc ("\\x1", new int[]{ 0 }));
		Assert.assertEquals (18, CCL.esc ("\\x12", new int[]{ 0 }));

		Assert.assertEquals ('\u655f', CCL.esc ("\\u655f", new int[]{ 0 }));
		Assert.assertEquals ('\u1234', CCL.esc ("\\u12345", new int[]{ 0 }));
		Assert.assertEquals ('\u1234', CCL.esc ("\\u1234]", new int[]{ 0 }));
	}

	@Test
	public void testCCL () throws Exception
	{
		CCL ccl = CCL.getByteCCL ();
		Assert.assertEquals ("[x]", ccl.toString (ccl.parseCCL ("[x]")));
		Assert.assertEquals ("[Zabj-o]", ccl.toString (ccl.parseCCL ("[abj-oZ]")));
		Assert.assertEquals ("[^A-Z]", ccl.toString (ccl.parseCCL ("[^A-Z]")));
		Assert.assertEquals ("[^\\nA-Z]", ccl.toString (ccl.parseCCL ("[^A-Z\\n]")));
	}
}
