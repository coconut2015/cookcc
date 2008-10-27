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
package org.yuanheng.cookcc;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.Set;

import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.interfaces.CodeGen;
import org.yuanheng.cookcc.interfaces.OptionParser;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class Main
{
	public static String OPTION_HELP = "-help";
	public static String OPTION_QUIET = "-quiet";
	public static String OPTION_LANG = "-lang";
	public static String OPTION_DEBUG = "-debug";
	public static String OPTION_ANALYSIS = "-analysis";

	private static Properties s_codeGenDrivers = new Properties ();
	private static Properties s_inputParsers = new Properties ();

	static
	{
		try
		{
			s_codeGenDrivers.load (Main.class.getClassLoader ().getResourceAsStream ("resources/codegen.properties"));
			s_inputParsers.load (Main.class.getClassLoader ().getResourceAsStream ("resources/input.properties"));
		}
		catch (Exception ex)
		{
			ex.printStackTrace ();
		}
	}

	private static boolean s_printUsage;
	private static String s_lang = s_codeGenDrivers.getProperty ("default");
	private static CodeGen s_codeGen;
	private static boolean s_quiet;
	private static boolean s_debug;
	private static boolean s_analysis;

	private static OptionParser s_helpOptioni = new OptionParser ()
	{
		public int handleOption (String[] args, int index) throws Exception
		{
			if (!OPTION_HELP.equals (args[index]))
				return 0;
			s_printUsage = true;
			return 1;
		}

		public String toString ()
		{
			return OPTION_HELP + "\t\t\t\tPrint this help message.";
		}
	};

	private static OptionParser s_quietOption = new OptionParser ()
	{
		public int handleOption (String[] args, int index) throws Exception
		{
			if (!OPTION_QUIET.equals (args[index]))
				return 0;
			s_quiet = true;
			return 1;
		}

		public String toString ()
		{
			return OPTION_QUIET + "\t\t\t\tSuppress console messages.";
		}
	};

	private static OptionParser s_analysisOption = new OptionParser()
	{
		public int handleOption (String[] args, int index) throws Exception
		{
			if (!OPTION_ANALYSIS.equals (args[index]))
				return 0;
			s_analysis = true;
			return 1;
		}

		public String toString ()
		{
			return OPTION_ANALYSIS + "\t\t\tGenerate analysis output for the parser.";
		}
	};

	private static OptionParser s_langOption = new OptionParser()
	{
		public int handleOption (String[] args, int index) throws Exception
		{
			if (!OPTION_LANG.equals (args[index]))
				return 0;
			s_lang = args[index + 1];
			return 2;
		}

		public String toString ()
		{
			StringBuffer buffer = new StringBuffer ();
			buffer.append (OPTION_LANG + "\t\t\t\tSelect output language.  Default is ");
			buffer.append (s_codeGenDrivers.getProperty ("default"));
			buffer.append ("\t\tAvailable languages:\t");
			Set<Object> keys = s_codeGenDrivers.keySet ();
			keys.remove ("default");
			buffer.append (keys);
			return buffer.toString ();
		}
	};

	private static OptionParser s_debugOption = new OptionParser()
	{
		public int handleOption (String[] args, int index) throws Exception
		{
			if (!OPTION_DEBUG.equals (args[index]))
				return 0;
			s_debug = true;
			return 1;
		}

		public String toString ()
		{
			return OPTION_DEBUG + "\t\t\t\tGenerate debug code.";
		}
	};

	private static OptionParser[] s_options = new OptionParser[]
	{
		s_helpOptioni,
		s_langOption,
		s_quietOption,
		s_analysisOption,
		s_debugOption
	};

	private static int parseOptions (String[] args) throws Exception
	{
		OptionParser[] optionParsers = s_options;
		int i;
		for (i = 0; i < args.length;)
		{
			int j;
			for (j = 0; j < optionParsers.length; ++j)
			{
				int count = optionParsers[j].handleOption (args, i);
				if (count > 0)
				{
					i += count;
					break;
				}
			}
			if (j == optionParsers.length)
				break;
		}

		CodeGen codeGen = getCodeGen ();
		s_codeGen = codeGen;
		optionParsers = codeGen.getOptions ();
		for (; i < args.length;)
		{
			int j;
			for (j = 0; j < optionParsers.length; ++j)
			{
				int count = optionParsers[j].handleOption (args, i);
				if (count > 0)
				{
					i += count;
					break;
				}
			}
			if (j < optionParsers.length)
				continue;
			for (j = 0; j < s_options.length; ++j)
			{
				int count = s_options[j].handleOption (args, i);
				if (count > 0)
				{
					i += count;
					break;
				}
			}
			if (j == s_options.length)
				break;
		}

		if (s_printUsage || args.length == 0)
		{
			s_printUsage = false;

			Package p = Package.getPackage ("org.yuanheng.cookcc");

			System.out.println ("CookCC version " + p.getImplementationVersion ());
			System.out.println ("Usage: cookcc [cookcc options] [language options] file");
			for (int j = 0; j < s_options.length; ++j)
				System.out.println (s_options[j]);
			System.out.println ();
			System.out.println (s_lang + " options:");
			for (int j = 0; j < optionParsers.length; ++j)
				System.out.println (optionParsers[j]);
			return -1;
		}

		return i;
	}

	private static CodeGen getCodeGen () throws Exception
	{
		if (s_lang == null)
			throw new IllegalArgumentException ("output language not specified.");
		String codeGen = (String)s_codeGenDrivers.get (s_lang);
		if (codeGen == null)
			throw new IllegalArgumentException ("unknown output language: " + s_lang);
		Class codeGenClass = Class.forName (codeGen);
		Constructor ctor = codeGenClass.getConstructor (new Class[0]);
		if (ctor == null)
			throw new IllegalArgumentException ("default constructor not found in the doclet class.");
		return (CodeGen)ctor.newInstance (new Object[0]);
	}

	public static void main (String[] args) throws Exception
	{
		try
		{
			int fileIndex = parseOptions (args);

			if (fileIndex < 0 || s_codeGen == null)
				return;

			if (fileIndex >= args.length)
				error ("no input file specified.");

			File file = new File (args[fileIndex]);
			Class parserClass = getParser (getExtension (file.getName ()));
			if (parserClass == null)
				error ("Unknown file type: " + args[fileIndex]);
			Document doc = (Document)parserClass.getMethod ("parse", File.class).invoke (null, file);

			s_codeGen.generateOutput (doc);
		}
		catch (Exception ex)
		{
			error (ex);
		}
	}

	private static String getExtension (String fileName)
	{
		int index = fileName.lastIndexOf ('.');
		if (index < 0)
			return "";
		return fileName.substring (index);
	}

	public static boolean isDebug ()
	{
		return s_debug;
	}

	private static Class getParser (String extension)
	{
		String className = s_inputParsers.getProperty (extension);
		if (className == null)
			return null;
		try
		{
			return Class.forName (className);
		}
		catch (Throwable t)		// use throwable since sometimes fetal errors occur and they are not exceptions
		{
			return null;
		}
	}

	public static void error (Exception ex)
	{
		if (s_quiet)
			return;
		ex.printStackTrace (System.err);
		System.exit (1);
	}

	public static void error (String msg)
	{
		if (s_quiet)
			return;
		System.err.println (msg);
		System.exit (1);
	}

	public static void warn (String msg)
	{
		if (s_quiet)
			return;
		System.err.println (msg);
	}

	public static File getAnalysisFile ()
	{
		if (s_analysis)
			return new File ("cookcc.parser.txt");
		return null;
	}
}
