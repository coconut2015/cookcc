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
package org.yuanheng.cookcc.codegen.options;

import java.io.File;

import org.yuanheng.cookcc.interfaces.OptionHandler;

/**
 * @author Heng Yuan
 */
public class OutputOption implements OptionHandler
{
	public static String OPTION_OUTPUT = "-o";

	private static File m_output;

	public String getOption ()
	{
		return OPTION_OUTPUT;
	}

	public boolean requireArguments ()
	{
		return true;
	}

	public void handleOption (String value) throws Exception
	{
		if (value == null)
			throw new IllegalArgumentException ("Output file was not specified.");
		File file = new File (value);
		if (file.exists () && file.isDirectory ())
			throw new IllegalArgumentException (value + " is a directory.");
		m_output = file;
	}

	public String toString ()
	{
		return OPTION_OUTPUT + "\t\t\t\tSelect output file.";
	}

	public File getOutput ()
	{
		return m_output;
	}

	public void setOutput (File output)
	{
		m_output = output;
	}
}
