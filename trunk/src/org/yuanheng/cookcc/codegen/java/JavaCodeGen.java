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
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.yuanheng.cookcc.codegen.options.ClassOption;
import org.yuanheng.cookcc.codegen.options.LexerTableOption;
import org.yuanheng.cookcc.codegen.options.OutputDirectoryOption;
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

	private static boolean m_public;

	private OutputDirectoryOption m_outputDirectoryOption = new OutputDirectoryOption ();

	private LexerTableOption m_lexerTableOption = new LexerTableOption ();

	private ClassOption m_classOption = new ClassOption ();

	private OptionParser m_publicOption = new OptionParser()
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

	private OptionParser[] m_options = new OptionParser[]
	{
			m_outputDirectoryOption,
			m_lexerTableOption,
			m_classOption,
			m_publicOption
	};

	private void generateLexerOutput (Document doc, File file) throws Exception
	{
		Lexer lexer = Lexer.getLexer (doc);
		if (lexer == null)
			return;

		Map<String, Object> map = new HashMap<String, Object> ();
		FileWriter fw = new FileWriter (file);
		for (Object key : Resources.defaults.keySet ())
			map.put (key.toString (), Resources.defaults.getProperty (key.toString ()));

		String cl = m_classOption.getClassOption ();

		if (cl != null && cl.length () > 0)
		{
			String packageName = getPackageName (cl);
			String className = getClassName (cl);
			map.put ("ccclass", className);
			if (packageName.length () > 0)
				map.put ("package", packageName);
		}
		if (m_public)
			map.put ("public", Boolean.TRUE);
		if (m_lexerTableOption.getLexerTable ()!= null)
			doc.getLexer ().setTable (m_lexerTableOption.getLexerTable ());
		setup (map, doc);
		Resources.template.process (map, fw);
		fw.close ();
	}

	public void generateOutput (Document doc) throws Exception
	{
		String cl = m_classOption.getClassOption ();
		String className = cl == null ? Resources.defaults.getProperty ("ccclass") : cl;
		String packageName = getPackageName (className);
		className = getClassName (className);

		// now we check if we can create the directories of the package name
		File dir = m_outputDirectoryOption.getOutputDirectory ();
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
					throw new IllegalArgumentException ("Unable to create directories for " + cl);
				dir = subDir;
			}
		}

		generateLexerOutput (doc, new File (dir, className + ".java"));
	}

	public OptionParser[] getOptions ()
	{
		return m_options;
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