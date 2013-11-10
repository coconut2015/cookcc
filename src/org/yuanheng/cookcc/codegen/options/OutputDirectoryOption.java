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
package org.yuanheng.cookcc.codegen.options;

import java.io.File;

import org.yuanheng.cookcc.interfaces.OptionHandler;

/**
 * @author Heng Yuan
 * @version $Id$
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
