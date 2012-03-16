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
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.yuanheng.cookcc.OptionMap;
import org.yuanheng.cookcc.codegen.TemplatedCodeGen;
import org.yuanheng.cookcc.codegen.options.AbstractOption;
import org.yuanheng.cookcc.codegen.options.ClassOption;
import org.yuanheng.cookcc.codegen.options.OutputDirectoryOption;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.interfaces.CodeGen;
import org.yuanheng.cookcc.interfaces.OptionHandler;

import freemarker.template.Template;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class JavaCodeGen extends TemplatedCodeGen implements CodeGen
{
	public final static String DEFAULTS_URI = "/resources/templates/java/defaults.properties";
	public final static String TEMPLATE_URI = "resources/templates/java/class.ftl";

	public static String OPTION_PUBLIC = "-public";
	public static String OPTION_GENERICS = "-generics";
	public static String OPTION_EXTEND = "-extend";

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

	private OutputDirectoryOption m_outputDirectoryOption = new OutputDirectoryOption ();

	private ClassOption m_classOption = new ClassOption ();

	private OptionHandler m_publicOption = new OptionHandler()
	{
		public String getOption ()
		{
			return OPTION_PUBLIC;
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
			return OPTION_PUBLIC + "\t\t\t\tSet class scope to public.";
		}
	};

	private OptionHandler m_genericsOption = new OptionHandler()
	{
		public String getOption ()
		{
			return OPTION_GENERICS;
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
			return OPTION_GENERICS + "\t\t\tGenerate Java code with generics.";
		}
	};

	private OptionHandler m_extendOption = new OptionHandler()
	{
		public String getOption ()
		{
			return OPTION_EXTEND;
		}

		public boolean requireArguments ()
		{
			return true;
		}

		public void handleOption (String value) throws Exception
		{
		}

		public String toString ()
		{
			return OPTION_EXTEND + "\t\t\t\tSet the parent class of the generated class.";
		}
	};

	private OptionHandler m_abstractOption = new AbstractOption ();

	private OptionMap m_options = new OptionMap ();

	{
		m_options.registerOptionHandler (m_outputDirectoryOption);
		m_options.registerOptionHandler (m_classOption);
		m_options.registerOptionHandler (m_publicOption);
		m_options.registerOptionHandler (m_genericsOption);
		m_options.registerOptionHandler (m_abstractOption);
		m_options.registerOptionHandler (m_extendOption);
	}

	private void generateTemplateOutput (Document doc, File file) throws Exception
	{
		if (doc.getLexer () == null && doc.getParser () == null)
			return;

		Map<String, Object> map = new HashMap<String, Object> ();
		for (Object key : Resources.defaults.keySet ())
			map.put (key.toString (), Resources.defaults.getProperty (key.toString ()));

		// check if user specifically set the main function flag
		if (doc.getMain () != null)
			map.put ("main", doc.getMain ());

		String cl = m_classOption.getClassName ();

		if (cl != null && cl.length () > 0)
		{
			String packageName = getPackageName (cl);
			String className = getClassName (cl);
			map.put ("ccclass", className);
			if (packageName.length () > 0)
				map.put ("package", packageName);
		}
		if (m_options.hasOption (OPTION_PUBLIC))
			map.put ("public", Boolean.TRUE);
		if (m_options.hasOption (OPTION_GENERICS))
			map.put ("generics", Boolean.TRUE);
		if (m_options.hasOption (AbstractOption.OPTION_ABSTRACT))
		{
			map.put ("abstract", Boolean.TRUE);
			map.put ("main", Boolean.FALSE);		// force disable output main since we can't instantiate the class
		}
		if (m_options.hasOption (OPTION_EXTEND))
		{
			String parentClass = m_options.getArgument (OPTION_EXTEND);
			if (parentClass != null && (parentClass = parentClass.trim ()).length () > 0)
				map.put ("extend", parentClass);
		}
		setup (map, doc);
		StringWriter sw = new StringWriter ();
		Resources.template.process (map, sw);
		FileWriter fw = new FileWriter (file);
		fw.write (sw.toString ());
		fw.close ();
	}

	public void generateOutput (Document doc) throws Exception
	{
		String cl = m_classOption.getClassName ();
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

		generateTemplateOutput (doc, new File (dir, className + ".java"));
	}

	public OptionMap getOptions ()
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
