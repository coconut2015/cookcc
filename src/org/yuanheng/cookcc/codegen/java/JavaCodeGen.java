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
package org.yuanheng.cookcc.codegen.java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.yuanheng.cookcc.codegen.plain.TemplatedCodeGen;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.interfaces.CodeGen;
import org.yuanheng.cookcc.interfaces.OptionParser;
import org.yuanheng.cookcc.lexer.Lexer;

import freemarker.template.Template;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class JavaCodeGen extends TemplatedCodeGen implements CodeGen
{
	public final static String DEFAULTS_URI = "/resources/templates/java/defaults.properties";
	public final static String TEMPLATE_URI = "resources/templates/java/class.txt";

	public static String OPTION_OUTPUT_DIR = "-d";
	public static String OPTION_TABLE = "-table";
	public static String OPTION_CLASS = "-class";
	public static String OPTION_PUBLIC = "-public";

	private static class Resources
	{
		private final static Properties defaults = new Properties ();
		private static Template template;

		static
		{
			try
			{
				defaults.load (Resources.class.getResourceAsStream (DEFAULTS_URI));
				template = getTemplate (TEMPLATE_URI);
			}
			catch (Exception ex)
			{
				ex.printStackTrace ();
			}
		}
	}

	private static File m_outputDir = new File (".");
	private static String m_table;
	private static String m_class;
	private static boolean m_public;

	private OptionParser m_outputDirectoryParser = new OptionParser()
	{
		public int handleOption (String[] args, int index) throws Exception
		{
			if (!OPTION_OUTPUT_DIR.equals (args[index]))
				return 0;
			File file = new File (args[index + 1]);
			if (!file.isDirectory ())
				throw new IllegalArgumentException (args[index + 1] + " does not exist.");
			m_outputDir = file;
			return 2;
		}

		public String toString ()
		{
			return OPTION_OUTPUT_DIR + "\t\t\t\tselect output directory.";
		}
	};

	private OptionParser m_tableParser = new OptionParser()
	{
		public int handleOption (String[] args, int index) throws Exception
		{
			if (!OPTION_TABLE.equals (args[index]))
				return 0;
			String table = args[index + 1].toLowerCase ();
			if (!"ecs".equals (table) &&
				!"full".equals (table) &&
				!"compressed".equals (table))
				throw new IllegalArgumentException ("Invalid table choice: " + table);
			m_table = table;
			return 2;
		}

		public String toString ()
		{
			return OPTION_TABLE + "\t\t\t\tselect lexer DFA table format.\n" +
					"\tAvailable formats:\t[ecs, full, compressed]";
		}
	};

	private OptionParser m_classParser = new OptionParser()
	{
		public int handleOption (String[] args, int index) throws Exception
		{
			if (!OPTION_CLASS.equals (args[index]))
				return 0;
			m_class = args[index + 1];
			return 2;
		}

		public String toString ()
		{
			return OPTION_CLASS + "\t\t\t\tset class name.";
		}
	};

	private OptionParser m_publicParser = new OptionParser()
	{
		public int handleOption (String[] args, int index) throws Exception
		{
			if (!OPTION_PUBLIC.equals (args[index]))
				return 0;
			m_public = true;
			return 1;
		}

		public String toString ()
		{
			return OPTION_PUBLIC + "\t\t\t\tset class scope to public.";
		}
	};

	private OptionParser[] m_optionParsers = new OptionParser[]
	{
			m_outputDirectoryParser,
			m_tableParser,
			m_classParser,
			m_publicParser
	};

	private void generateLexerOutput (Document doc, PrintWriter p) throws Exception
	{
		Lexer lexer = Lexer.getLexer (doc);
		if (lexer == null)
			return;

		Map<String, Object> map = new HashMap<String, Object> ();
		StringWriter sw = new StringWriter ();
		for (Object key : Resources.defaults.keySet ())
			map.put (key.toString (), Resources.defaults.getProperty (key.toString ()));

		if (m_class != null && m_class.length () > 0)
		{
			String packageName = getPackageName (m_class);
			String className = getClassName (m_class);
			map.put ("ccclass", className);
			if (packageName.length () > 0)
				map.put ("package", packageName);
		}
		if (m_public)
			map.put ("public", Boolean.TRUE);
		if (m_table != null)
			doc.getLexer ().setTable (m_table);
		setup (map, doc);
		Resources.template.process (map, sw);
		p.println (sw);
	}

	public void generateOutput (Document doc) throws Exception
	{
		String className = m_class == null ? Resources.defaults.getProperty ("ccclass") : m_class;
		String packageName = getPackageName (className);
		className = getClassName (className);

		// now we check if we can create the directories of the package name
		File dir = m_outputDir;
		if (packageName.length () > 0)
		{
			String[] subDirs = packageName.split ("\\.");
			for (String d : subDirs)
			{
				File subDir = new File (dir, d);
				if (subDir.isDirectory () && subDir.exists ())
				{
					dir = subDir;
					continue;
				}
				if (!subDir.mkdir ())
					throw new IllegalArgumentException ("Unable to create directories for " + m_class);
				dir = subDir;
			}
		}

		PrintWriter p = new PrintWriter (new FileOutputStream (new File (dir, className + ".java")));
		generateLexerOutput (doc, p);
		p.flush ();
		p.close ();
	}

	public OptionParser[] getOptionParsers ()
	{
		return m_optionParsers;
	}

	private static String getClassName (String className)
	{
		int index = className.lastIndexOf ('.');
		if (index < 0)
			return className;
		else
			return className.substring (index + 1);
	}

	private static String getPackageName (String className)
	{
		int index = className.lastIndexOf ('.');
		if (index < 0)
			return "";
		else
			return className.substring (0, index);
	}
}