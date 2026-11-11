/*
 * Copyright (c) 2026, Heng Yuan
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
package org.yuanheng.cookcc;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.yuanheng.cookcc.interfaces.OptionHandler;

/**
 * @author Heng Yuan
 */
public class OptionMapTest
{
	/** Records the values passed to it. */
	private static class RecordingHandler implements OptionHandler
	{
		final String m_option;
		final boolean m_requireArguments;
		final List<String> m_values = new ArrayList<String> ();

		RecordingHandler (String option, boolean requireArguments)
		{
			m_option = option;
			m_requireArguments = requireArguments;
		}

		public String getOption ()
		{
			return m_option;
		}

		public boolean requireArguments ()
		{
			return m_requireArguments;
		}

		public void handleOption (String value)
		{
			m_values.add (value);
		}

		public String toString ()
		{
			return m_option + "\thelp for " + m_option;
		}
	}

	@Test
	public void testFlagOption () throws Exception
	{
		OptionMap map = new OptionMap ();
		RecordingHandler handler = new RecordingHandler ("-flag", false);
		map.registerOptionHandler (handler);

		Assert.assertEquals (1, map.handleOption (new String[] { "-flag", "file" }, 0));
		Assert.assertTrue (map.hasOption ("-flag"));
		Assert.assertNull (map.getArgument ("-flag"));
		Assert.assertEquals (1, handler.m_values.size ());
	}

	@Test
	public void testOptionWithArgument () throws Exception
	{
		OptionMap map = new OptionMap ();
		RecordingHandler handler = new RecordingHandler ("-d", true);
		map.registerOptionHandler (handler);

		Assert.assertEquals (2, map.handleOption (new String[] { "-d", "out", "file" }, 0));
		Assert.assertEquals ("out", map.getArgument ("-d"));
		Assert.assertEquals ("out", handler.m_values.get (0));
	}

	@Test (expected = IllegalArgumentException.class)
	public void testMissingArgument () throws Exception
	{
		OptionMap map = new OptionMap ();
		map.registerOptionHandler (new RecordingHandler ("-d", true));
		map.handleOption (new String[] { "-d" }, 0);
	}

	@Test
	public void testUnknownOptionIsNotHandled () throws Exception
	{
		OptionMap map = new OptionMap ();
		Assert.assertEquals (0, map.handleOption (new String[] { "-unknown" }, 0));
		Assert.assertFalse (map.hasOption ("-unknown"));
	}

	@Test
	public void testAddOptionWithoutHandler () throws Exception
	{
		// Options without a handler are still recorded, which is how the
		// table options reach the code generators.
		OptionMap map = new OptionMap ();
		map.addOption ("-lexertable", "ecs");
		Assert.assertEquals ("ecs", map.getArgument ("-lexertable"));
	}

	@Test
	public void testRemoveOption () throws Exception
	{
		OptionMap map = new OptionMap ();
		map.addOption ("-x");
		map.removeOption ("-x");
		Assert.assertFalse (map.hasOption ("-x"));
	}

	@Test
	public void testHelpListsHandlersInOrder () throws Exception
	{
		OptionMap map = new OptionMap ();
		map.registerOptionHandler (new RecordingHandler ("-b", false));
		map.registerOptionHandler (new RecordingHandler ("-a", false));
		Assert.assertEquals ("-b\thelp for -b\n-a\thelp for -a\n", map.toString ());
		Assert.assertEquals (2, map.getAvailableOptions ().length);
	}
}
