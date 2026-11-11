/*
 * Copyright (c) 2026, Heng Yuan
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
