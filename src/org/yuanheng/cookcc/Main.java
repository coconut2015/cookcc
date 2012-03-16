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
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.interfaces.CodeGen;
import org.yuanheng.cookcc.interfaces.OptionHandler;

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
	public static String OPTION_DEFAULTREDUCE = "-defaultreduce";

	public static String OPTION_LEXERTABLE = "-lexertable";
	public static String OPTION_PARSERTABLE = "-parsertable";

	public static String ANALYSIS_FILE = "cookcc_parser_analysis.txt";

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

	private static String s_lang = s_codeGenDrivers.getProperty ("default");
	private static CodeGen s_codeGen;
	private static boolean s_quiet;

	private static OptionHandler s_helpOption = new OptionHandler()
	{
		public String getOption ()
		{
			return OPTION_HELP;
		}

		public boolean requireArguments ()
		{
			return false;
		}

		public void handleOption (String value) throws Exception
		{
		}

		public String toString ()
		{
			return OPTION_HELP + "\t\t\t\tPrint this help message.";
		}

	};

	private static OptionHandler s_quietOption = new OptionHandler()
	{
		public String getOption ()
		{
			return OPTION_QUIET;
		}

		public boolean requireArguments ()
		{
			return false;
		}

		public void handleOption (String value) throws Exception
		{
			s_quiet = true;
		}

		public String toString ()
		{
			return OPTION_QUIET + "\t\t\t\tSuppress console messages.";
		}
	};

	private static OptionHandler s_analysisOption = new OptionHandler()
	{
		public String getOption ()
		{
			return OPTION_ANALYSIS;
		}

		public boolean requireArguments ()
		{
			return false;
		}

		public void handleOption (String value) throws Exception
		{
		}

		public String toString ()
		{
			return OPTION_ANALYSIS + "\t\t\tGenerate analysis output for the parser.";
		}
	};

	private static OptionHandler s_defaultReduceOption = new OptionHandler()
	{
		public String getOption ()
		{
			return OPTION_DEFAULTREDUCE;
		}

		public boolean requireArguments ()
		{
			return false;
		}

		public void handleOption (String value) throws Exception
		{
		}

		public String toString ()
		{
			return OPTION_DEFAULTREDUCE + "\t\t\tUse default reduce states for the parser.";
		}
	};

	private static OptionHandler s_lexerTableOption = new OptionHandler()
	{
		public String getOption ()
		{
			return OPTION_LEXERTABLE;
		}

		public boolean requireArguments ()
		{
			return true;
		}

		public void handleOption (String value) throws Exception
		{
			String table = value.toLowerCase ();
			if (!"ecs".equals (table) &&
				!"full".equals (table) &&
				!"compressed".equals (table))
				throw new IllegalArgumentException ("Invalid table choice: " + table);
		}

		public String toString ()
		{
			return OPTION_LEXERTABLE + "\t\t\tSelect lexer DFA table format.\n" +
				   "\tAvailable formats:\t\t[ecs, full, compressed]";
		}
	};

	private static OptionHandler s_parserTableOption = new OptionHandler()
	{
		public String getOption ()
		{
			return OPTION_PARSERTABLE;
		}

		public boolean requireArguments ()
		{
			return true;
		}

		public void handleOption (String value) throws Exception
		{
			String table = value.toLowerCase ();
			if (!"ecs".equals (table) &&
				!"compressed".equals (table))
				throw new IllegalArgumentException ("Invalid table choice: " + table);
		}

		public String toString ()
		{
			return OPTION_PARSERTABLE + "\t\t\tSelect parser DFA table format.\n" +
				   "\tAvailable formats:\t\t[ecs, compressed]";
		}
	};

	private static OptionHandler s_langOption = new OptionHandler()
	{
		public String getOption ()
		{
			return OPTION_LANG;
		}

		public boolean requireArguments ()
		{
			return true;
		}

		public void handleOption (String value) throws Exception
		{
			s_lang = value;
		}

		public String toString ()
		{
			StringBuffer buffer = new StringBuffer ();
			buffer.append (OPTION_LANG).append ("\t\t\t\tSelect output language.  Default is ");
			buffer.append (s_codeGenDrivers.getProperty ("default"));
			buffer.append ("\t\tAvailable languages:\t\t");
			Set<Object> keys = s_codeGenDrivers.keySet ();
			keys.remove ("default");
			buffer.append (keys);
			return buffer.toString ();
		}
	};

	private static OptionHandler s_debugOption = new OptionHandler()
	{
		public String getOption ()
		{
			return OPTION_DEBUG;
		}

		public boolean requireArguments ()
		{
			return false;
		}

		public void handleOption (String value) throws Exception
		{
		}

		public String toString ()
		{
			return OPTION_DEBUG + "\t\t\t\tGenerate debug code.";
		}
	};

	private static OptionMap s_options = new OptionMap ();

	static
	{
		s_options.registerOptionHandler (s_helpOption);
		s_options.registerOptionHandler (s_langOption);
		s_options.registerOptionHandler (s_quietOption);
		s_options.registerOptionHandler (s_analysisOption);
		s_options.registerOptionHandler (s_defaultReduceOption);
		s_options.registerOptionHandler (s_lexerTableOption);
		s_options.registerOptionHandler (s_parserTableOption);
		s_options.registerOptionHandler (s_debugOption);
	}

	private static int parseOptions (String[] args) throws Exception
	{
		OptionMap optionParsers = s_options;
		int i;
		for (i = 0; i < args.length;)
		{
			int count = optionParsers.handleOption (args, i);

			if (count > 0)
				i += count;
			else
				break;
		}

		CodeGen codeGen = getCodeGen ();
		optionParsers = codeGen.getOptions ();
		for (; i < args.length;)
		{
			int count = optionParsers.handleOption (args, i);
			if (count > 0)
			{
				i += count;
				continue;
			}

			count = s_options.handleOption (args, i);
			if (count > 0)
				i += count;
			else
				break;
		}

		if (s_options.hasOption (OPTION_HELP) || args.length == 0)
		{
			Package p = Package.getPackage ("org.yuanheng.cookcc");

			System.out.println ("CookCC version " + p.getImplementationVersion ());
			System.out.println ("Usage: cookcc [cookcc options] [language options] file");
			System.out.println (s_options);
			System.out.println ();
			System.out.println (s_lang + " options:");
			System.out.println (optionParsers);
			return -1;
		}

		return i;
	}

	public static void parseOptions (Map<String, String> options) throws Exception
	{
		String lang = options.get (OPTION_LANG);
		if (lang != null)
		{
			if (s_lang == null || !s_lang.equals (lang))
			{
				s_lang = lang;
				s_codeGen = null;
			}
		}

		CodeGen codeGen = getCodeGen ();
		Set<String> keySet = options.keySet ();
		keySet.remove (OPTION_LANG);
		OptionMap optionMap = codeGen.getOptions ();
		for (String option : keySet)
		{
			String value = options.get (option);
			optionMap.addOption (option, value);
		}
	}

	public static CodeGen getCodeGen (String lang) throws Exception
	{
		String codeGen = (String)s_codeGenDrivers.get (lang);
		if (codeGen == null)
			throw new IllegalArgumentException ("unknown output language: " + s_lang);
		Class<? extends CodeGen> codeGenClass = Class.forName (codeGen).asSubclass (CodeGen.class);
		Constructor<? extends CodeGen> ctor = codeGenClass.getConstructor (new Class[0]);
		if (ctor == null)
			throw new IllegalArgumentException ("default constructor not found in the doclet class.");
		return ctor.newInstance (new Object[0]);
	}

	public static CodeGen getCodeGen () throws Exception
	{
		if (s_codeGen != null)
			return s_codeGen;
		if (s_lang == null)
			throw new IllegalArgumentException ("output language not specified.");
		s_codeGen = getCodeGen (s_lang);
		return s_codeGen;
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
			Class<?> parserClass = getParser (getExtension (file.getName ()));
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

	private static Class<?> getParser (String extension)
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
		if (!s_quiet)
			ex.printStackTrace (System.err);
		System.exit (1);
	}

	public static void error (String msg)
	{
		if (!s_quiet)
			System.err.println (msg);
		System.exit (1);
	}

	public static void warn (String msg)
	{
		if (s_quiet)
			return;
		System.err.println (msg);
	}

	public static boolean isDebug (OptionMap options)
	{
		return s_options.hasOption (OPTION_DEBUG) || options.hasOption (OPTION_DEBUG);
	}

	public static File getAnalysisFile (OptionMap options)
	{
		if (s_options.hasOption (OPTION_ANALYSIS) || options.hasOption (OPTION_ANALYSIS))
			return new File (ANALYSIS_FILE);
		return null;
	}

	public static boolean getDefaultReduce (OptionMap options)
	{
		return s_options.hasOption (OPTION_DEFAULTREDUCE) || options.hasOption (OPTION_DEFAULTREDUCE);
	}

	public static String getLexerTable (OptionMap options)
	{
		String table = options.getArgument (OPTION_LEXERTABLE);
		if (table != null)
			return table;
		return s_options.getArgument (OPTION_LEXERTABLE);
	}

	public static String getParserTable (OptionMap options)
	{
		String table = options.getArgument (OPTION_PARSERTABLE);
		if (table != null)
			return table;
		return s_options.getArgument (OPTION_PARSERTABLE);
	}

	public static OptionMap getOptions ()
	{
		return s_options;
	}

	public static String[] getLanguages ()
	{
		Set<Object> keys = s_codeGenDrivers.keySet ();
		keys.remove ("default");
		return keys.toArray (new String[keys.size ()]);
	}
}
