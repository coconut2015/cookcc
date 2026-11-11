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
public class OutputDirectoryOption implements OptionHandler
{
	public static String OPTION_OUTPUT_DIR = "-d";

	private static File m_outputDir = new File (".");

	public String getOption ()
	{
		return OPTION_OUTPUT_DIR;
	}

	public boolean requireArguments ()
	{
		return true;
	}

	public void handleOption (String value) throws Exception
	{
		if (value == null)
			throw new IllegalArgumentException ("Output directory was not specified.");
		File file = new File (value);
		if (!file.isDirectory ())
			throw new IllegalArgumentException (value + " does not exist.");
		m_outputDir = file;
	}

	public String toString ()
	{
		return OPTION_OUTPUT_DIR + "\t\t\t\tSelect output directory.";
	}

	public File getOutputDirectory ()
	{
		return m_outputDir;
	}

	public void setOutputDirectory (File dir)
	{
		m_outputDir = dir;
	}
}
