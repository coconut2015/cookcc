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

import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.Set;

import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.input.xml.XmlParser;
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

	private static Properties s_properties = new Properties ();
	static
	{
		try
		{
			s_properties.load (Main.class.getClassLoader ().getResourceAsStream ("resources/codegen.properties"));
		}
		catch (Exception ex)
		{
			ex.printStackTrace ();
		}
	}

	private static boolean s_printUsage;
	private static String s_lang = s_properties.getProperty ("default");
	private static CodeGen s_codeGen;
	private static boolean s_quiet;

	private static OptionParser s_helpParser = new OptionParser ()
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

	private static OptionParser s_quietParser = new OptionParser ()
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

	private static OptionParser s_langParser = new OptionParser()
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
			buffer.append (s_properties.getProperty ("default"));
			buffer.append ("\t\tAvailable languages:\t");
			Set<Object> keys = s_properties.keySet ();
			keys.remove ("default");
			buffer.append (keys);
			return buffer.toString ();
		}
	};

	private static OptionParser[] s_optionParsers = new OptionParser[]
	{
		s_helpParser,
		s_langParser,
		s_quietParser
	};

	private static int parseOptions (String[] args) throws Exception
	{
		OptionParser[] optionParsers = s_optionParsers;
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
		optionParsers = codeGen.getOptionParsers ();
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
			if (j == optionParsers.length)
				break;
		}

		if (s_printUsage || args.length == 0)
		{
			s_printUsage = false;

			Package p = Package.getPackage ("org.yuanheng.cookcc");

			System.out.println ("CookCC version " + p.getImplementationVersion ());
			System.out.println ("Usage: cookcc [cookcc options] [language options] file");
			for (int j = 0; j < s_optionParsers.length; ++j)
				System.out.println (s_optionParsers[j]);
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
		String codeGen = (String)s_properties.get (s_lang);
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

			Document doc = XmlParser.parseXml (args[fileIndex]);
			s_codeGen.generateOutput (doc, System.out);
		}
		catch (Exception ex)
		{
			error (ex);
		}
	}

	public static void error (Exception ex)
	{
		if (s_quiet)
			return;
		ex.printStackTrace (System.out);
		System.exit (1);
	}

	public static void error (String msg)
	{
		if (s_quiet)
			return;
		System.out.println (msg);
		System.exit (1);
	}

	public static void warn (String msg)
	{
		if (s_quiet)
			return;
		System.out.println (msg);
	}
}
