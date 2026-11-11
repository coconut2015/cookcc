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
 */
public class JavaCodeGen extends TemplatedCodeGen implements CodeGen
{
	public final static String DEFAULTS_URI = "/templates/java/defaults.properties";
	public final static String TEMPLATE_URI = "templates/java/class.ftl";

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

	private final OutputDirectoryOption m_outputDirectoryOption = new OutputDirectoryOption ();

	private final ClassOption m_classOption = new ClassOption ();

	private final OptionHandler m_publicOption = new OptionHandler ()
	{
		@Override
		public String getOption ()
		{
			return OPTION_PUBLIC;
		}

		@Override
		public boolean requireArguments ()
		{
			return false;
		}

		@Override
		public void handleOption (String value) throws Exception
		{
		}

		@Override
		public String toString ()
		{
			return OPTION_PUBLIC + "\t\t\t\tSet class scope to public.";
		}
	};

	private final OptionHandler m_genericsOption = new OptionHandler ()
	{
		@Override
		public String getOption ()
		{
			return OPTION_GENERICS;
		}

		@Override
		public boolean requireArguments ()
		{
			return false;
		}

		@Override
		public void handleOption (String value) throws Exception
		{
		}

		@Override
		public String toString ()
		{
			return OPTION_GENERICS + "\t\t\tGenerate Java code with generics.";
		}
	};

	private final OptionHandler m_extendOption = new OptionHandler ()
	{
		@Override
		public String getOption ()
		{
			return OPTION_EXTEND;
		}

		@Override
		public boolean requireArguments ()
		{
			return true;
		}

		@Override
		public void handleOption (String value) throws Exception
		{
		}

		@Override
		public String toString ()
		{
			return OPTION_EXTEND + "\t\t\t\tSet the parent class of the generated class.";
		}
	};

	private final OptionHandler m_abstractOption = new AbstractOption ();

	private final OptionMap m_options = new OptionMap ();

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
			map.put ("main", Boolean.FALSE);        // force disable output main since we can't instantiate the class
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

	@Override
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

	@Override
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
