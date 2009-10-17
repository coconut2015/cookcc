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
package org.yuanheng.cookcc.ant;

import java.io.File;
import java.util.ArrayList;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.util.JavaEnvUtils;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class Task extends org.apache.tools.ant.Task
{
	public static class Option
	{
		private String m_name;
		private String m_value;
		public void setName (String name)
		{
			m_name = name;
		}
		public void setValue (String value)
		{
			m_value = value;
		}
	}

	private String m_lexerTable;
	private String m_parserTable;
	private boolean m_debug;
	private boolean m_analysis;
	private boolean m_defaultReduce;
	private String m_lang;
	private File m_destDir = null;
	private File m_srcDir = null;

	private ArrayList<Option> m_options = new ArrayList<Option> ();

	private ArrayList<String> m_aptFiles = new ArrayList<String> ();
	private ArrayList<String> m_xccFiles = new ArrayList<String> ();

	public void setSrcDir (File srcDir)
	{
		if (!srcDir.exists () || !srcDir.isDirectory ())
			throw new IllegalArgumentException ("Invalid destination directory.");
		m_srcDir = srcDir;
	}

	public void setDestDir (File destDir)
	{
		if (!destDir.exists () || !destDir.isDirectory ())
			throw new IllegalArgumentException ("Invalid destination directory.");
		m_destDir = destDir;
	}

	public void setLexerTable (String table)
	{
		if (!"ecs".equals (table) &&
			!"full".equals (table) &&
			!"compressed".equals (table))
			throw new IllegalArgumentException ("Unknown lexer table: " + table);
		m_lexerTable = table;
	}

	public void setParserTable (String table)
	{
		if (!"ecs".equals (table) &&
			!"compressed".equals (table))
			throw new IllegalArgumentException ("Unknown parser table: " + table);
		m_parserTable = table;
	}

	public void setLang (String lang)
	{
		m_lang = lang;
	}

	public void setDebug (boolean debug)
	{
		m_debug = debug;
	}

	public void setAnalysis (boolean analysis)
	{
		m_analysis = analysis;
	}

	public void setDefaultReduce (boolean defaultReduce)
	{
		m_defaultReduce = defaultReduce;
	}

	public void setSrc (String files)
	{
		String[] fileNames = files.split (" ");
		for (String fileName : fileNames)
		{
			if (fileName == null || fileName.length () == 0)
				continue;
			if (fileName.endsWith (".java"))
				m_aptFiles.add (fileName);
			else
				m_xccFiles.add (fileName);
		}
	}

	public void addConfiguredOption (Option option)
	{
		m_options.add (option);
	}

	public void execute ()
	{
		if (m_xccFiles.size () > 0)
			executeCookCC (m_xccFiles.toArray (new String[m_xccFiles.size ()]));
		if (m_aptFiles.size () > 0)
			executeApt (m_aptFiles.toArray (new String[m_aptFiles.size ()]));
	}

	protected void executeCookCC (String[] files)
	{
		Commandline cmd = new Commandline ();
		cmd.setExecutable (JavaEnvUtils.getJdkExecutable ("java"));
		cmd.createArgument ().setValue ("-cp");
		cmd.createArgument ().setValue (getCookCCPath ());
		cmd.createArgument ().setValue ("org.yuanheng.cookcc.Main");

		if (m_analysis)
			cmd.createArgument ().setValue ("-analysis");
		if (m_debug)
			cmd.createArgument ().setValue ("-debug");
		if (m_defaultReduce)
			cmd.createArgument ().setValue ("-defaultreduce");
		if (m_lexerTable != null)
		{
			cmd.createArgument ().setValue ("-lexertable");
			cmd.createArgument ().setValue (m_lexerTable);
		}
		if (m_parserTable != null)
		{
			cmd.createArgument ().setValue ("-parsertable");
			cmd.createArgument ().setValue (m_parserTable);
		}
		if (m_lang != null)
		{
			cmd.createArgument ().setValue ("-lang");
			cmd.createArgument ().setValue (m_lang);
		}
		for (Option option : m_options)
		{
			if (option.m_name == null || option.m_name.length () == 0)
				continue;
			cmd.createArgument ().setValue (option.m_name);
			if (option.m_value == null)
				continue;
			cmd.createArgument ().setValue (option.m_value);
		}

		for (String file : files)
		{
			Commandline newCmd = (Commandline)cmd.clone ();
			newCmd.createArgument ().setValue (file);
			if (executeCmd (newCmd))
				throw new RuntimeException ("Error executing CookCC");
		}
	}

	protected String getCookCCPath ()
	{
		ClassLoader cl = Task.class.getClassLoader ();
		if (cl instanceof AntClassLoader)
		{
			return ((AntClassLoader)cl).getClasspath ();
		}
		throw new RuntimeException ("Unable to determine the runtime path of CookCC.");
	}

	protected void executeApt (String[] classes)
	{
		Commandline cmd = new Commandline ();
		cmd.setExecutable (JavaEnvUtils.getJdkExecutable ("apt"));

		if (m_srcDir == null)
			throw new IllegalArgumentException ("Source directory is not specified.");

		cmd.createArgument ().setValue ("-cp");
		cmd.createArgument ().setValue (getCookCCPath () + File.pathSeparatorChar + m_srcDir.getPath ());

		cmd.createArgument ().setValue ("-s");
		cmd.createArgument ().setValue (m_srcDir.getPath ());

		cmd.createArgument ().setValue ("-nocompile");

		// always turn on generics since we are using Java 1.5+ anyways
		cmd.createArgument ().setValue ("-Agenerics");
		if (m_analysis)
			cmd.createArgument ().setValue ("-Aanalysis");
		if (m_debug)
			cmd.createArgument ().setValue ("-Adebug");
		if (m_defaultReduce)
			cmd.createArgument ().setValue ("-Adefaultreduce");
		if (m_lexerTable != null)
			cmd.createArgument ().setValue ("-Alexertable=" + m_lexerTable);
		if (m_parserTable != null)
			cmd.createArgument ().setValue ("-Aparsertable=" + m_parserTable);
		if (m_lang != null)
			cmd.createArgument ().setValue ("-Alang=" + m_lang);
		if (m_lang == null || "java".equals (m_lang))
		{
			if (m_destDir == null)
				m_destDir = m_srcDir;
			cmd.createArgument ().setValue ("-Ad=" + m_destDir.getPath ());
		}
		for (Option option : m_options)
		{
			if (option.m_name == null || option.m_name.length () == 0)
				continue;
			String arg = option.m_name;
			if (arg.startsWith ("-"))
				arg = arg.substring (1);
			arg = "-A" + arg;
			if (option.m_value != null)
				arg = arg + '=' + option.m_value;
			cmd.createArgument ().setValue (arg);
		}

		for (String className : classes)
		{
			File file = new File (m_srcDir, className);
			cmd.createArgument ().setValue (file.getPath ());
		}

		if (executeCmd (cmd))
			throw new RuntimeException ("Error executing CookCC using APT");
	}

	protected boolean executeCmd (Commandline cmd)
	{
		try
		{
			System.out.println (cmd.toString ());
			Execute exe = new Execute ();
			exe.setCommandline (cmd.getCommandline ());
			exe.execute ();
			if (exe.getExitValue () == 0)
				return false;
		}
		catch (Exception ex)
		{
		}
		return true;
	}
}
