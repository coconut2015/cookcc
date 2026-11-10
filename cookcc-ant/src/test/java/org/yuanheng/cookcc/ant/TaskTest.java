/*
 * Copyright (c) 2026, Heng Yuan
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
import java.util.Arrays;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.util.JavaEnvUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for the CookCC Ant task.
 * <p>
 * The task builds a command line and runs it as a separate process.  These
 * tests replace the two methods that touch the environment,
 * {@code executeCmd} and {@code getCookCCPath}, so that the command lines
 * can be checked without starting any process.
 *
 * @author Heng Yuan
 */
public class TaskTest
{
	private static final String COOKCC_PATH = "cookcc.jar";
	private static final String PROCESSOR = "org.yuanheng.cookcc.input.ap.CookCCProcessor";

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder ();

	/**
	 * A task that records the commands instead of executing them.
	 */
	private static class RecordingTask extends Task
	{
		final List<String> executables = new ArrayList<String> ();
		final List<List<String>> commands = new ArrayList<List<String>> ();
		boolean fail;

		@Override
		protected String getCookCCPath ()
		{
			return COOKCC_PATH;
		}

		@Override
		protected boolean executeCmd (Commandline cmd)
		{
			executables.add (new File (cmd.getExecutable ()).getName ());
			commands.add (Arrays.asList (cmd.getArguments ()));
			// executeCmd returns true on failure.
			return fail;
		}
	}

	/**
	 * A task that records the messages it logs.
	 */
	private static class LoggingTask extends Task
	{
		final List<String> errors = new ArrayList<String> ();
		final List<String> warnings = new ArrayList<String> ();

		@Override
		public void log (String msg, int msgLevel)
		{
			if (msgLevel == Project.MSG_ERR)
				errors.add (msg);
			else if (msgLevel == Project.MSG_WARN)
				warnings.add (msg);
		}
	}

	private static Task.Option option (String name, String value)
	{
		Task.Option option = new Task.Option ();
		option.setName (name);
		option.setValue (value);
		return option;
	}

	private static List<String> list (String... args)
	{
		return Arrays.asList (args);
	}

	//
	// Grammar files (.xcc) are processed by running org.yuanheng.cookcc.Main.
	//

	@Test
	public void testXccFile ()
	{
		RecordingTask task = new RecordingTask ();
		task.setSrc ("grammar.xcc");
		task.execute ();

		Assert.assertEquals (1, task.commands.size ());
		Assert.assertTrue (task.executables.get (0).matches ("java(\\.exe)?"));
		Assert.assertEquals (list ("-cp", COOKCC_PATH, "org.yuanheng.cookcc.Main", "grammar.xcc"),
							 task.commands.get (0));
	}

	@Test
	public void testXccAllSettings ()
	{
		RecordingTask task = new RecordingTask ();
		task.setAnalysis (true);
		task.setDebug (true);
		task.setDefaultReduce (true);
		task.setLexerTable ("compressed");
		task.setParserTable ("ecs");
		task.setLang ("plain");
		task.setSrc ("grammar.xcc");
		task.execute ();

		Assert.assertEquals (list ("-cp", COOKCC_PATH, "org.yuanheng.cookcc.Main",
								   "-analysis", "-debug", "-defaultreduce",
								   "-lexertable", "compressed",
								   "-parsertable", "ecs",
								   "-lang", "plain",
								   "grammar.xcc"),
							 task.commands.get (0));
	}

	@Test
	public void testXccOptions ()
	{
		RecordingTask task = new RecordingTask ();
		task.addConfiguredOption (option ("-class", "MyLexer"));
		task.addConfiguredOption (option ("-public", null));
		// options without a name are ignored
		task.addConfiguredOption (option (null, "ignored"));
		task.addConfiguredOption (option ("", "ignored"));
		task.setSrc ("grammar.xcc");
		task.execute ();

		Assert.assertEquals (list ("-cp", COOKCC_PATH, "org.yuanheng.cookcc.Main",
								   "-class", "MyLexer", "-public", "grammar.xcc"),
							 task.commands.get (0));
	}

	@Test
	public void testXccOneCommandPerFile ()
	{
		RecordingTask task = new RecordingTask ();
		task.setDebug (true);
		// files are separated by spaces; extra spaces are ignored
		task.setSrc (" a.xcc  b.xcc ");
		task.execute ();

		Assert.assertEquals (2, task.commands.size ());
		Assert.assertEquals (list ("-cp", COOKCC_PATH, "org.yuanheng.cookcc.Main", "-debug", "a.xcc"),
							 task.commands.get (0));
		Assert.assertEquals (list ("-cp", COOKCC_PATH, "org.yuanheng.cookcc.Main", "-debug", "b.xcc"),
							 task.commands.get (1));
	}

	@Test
	public void testXccFailureStopsAtFirstFile ()
	{
		RecordingTask task = new RecordingTask ();
		task.fail = true;
		task.setSrc ("a.xcc b.xcc");
		try
		{
			task.execute ();
			Assert.fail ("expected the task to fail");
		}
		catch (RuntimeException ex)
		{
			Assert.assertEquals ("Error executing CookCC", ex.getMessage ());
		}
		Assert.assertEquals (1, task.commands.size ());
	}

	@Test
	public void testNoSourceDoesNothing ()
	{
		RecordingTask task = new RecordingTask ();
		task.execute ();
		Assert.assertTrue (task.commands.isEmpty ());

		task.setSrc ("   ");
		task.execute ();
		Assert.assertTrue (task.commands.isEmpty ());
	}

	//
	// Java files are processed by running javac with the CookCC annotation processor.
	//

	@Test
	public void testJavaFile () throws Exception
	{
		File srcDir = tmp.newFolder ("src");
		RecordingTask task = new RecordingTask ();
		task.setSrcDir (srcDir);
		task.setSrc ("pkg/Lexer.java");
		task.execute ();

		String src = srcDir.getPath ();
		Assert.assertEquals (1, task.commands.size ());
		Assert.assertTrue (task.executables.get (0).matches ("javac(\\.exe)?"));
		Assert.assertEquals (list ("-proc:only",
								   "-processor", PROCESSOR,
								   "-cp", COOKCC_PATH + File.pathSeparatorChar + src,
								   "-s", src,
								   "-Ad=" + src,
								   new File (srcDir, "pkg/Lexer.java").getPath ()),
							 task.commands.get (0));
	}

	@Test
	public void testJavaAllSettings () throws Exception
	{
		File srcDir = tmp.newFolder ("src");
		File destDir = tmp.newFolder ("dest");
		RecordingTask task = new RecordingTask ();
		task.setSrcDir (srcDir);
		task.setDestDir (destDir);
		task.setAnalysis (true);
		task.setDebug (true);
		task.setDefaultReduce (true);
		task.setLexerTable ("full");
		task.setParserTable ("compressed");
		task.setLang ("java");
		// a leading "-" is removed, and the value is appended with "="
		task.addConfiguredOption (option ("-generics", null));
		task.addConfiguredOption (option ("class", "MyParser"));
		task.setSrc ("A.java B.java");
		task.execute ();

		String src = srcDir.getPath ();
		Assert.assertEquals (1, task.commands.size ());
		Assert.assertEquals (list ("-proc:only",
								   "-processor", PROCESSOR,
								   "-cp", COOKCC_PATH + File.pathSeparatorChar + src,
								   "-s", src,
								   "-Aanalysis", "-Adebug", "-Adefaultreduce",
								   "-Alexertable=full",
								   "-Aparsertable=compressed",
								   "-Alang=java",
								   "-Ad=" + destDir.getPath (),
								   "-Agenerics", "-Aclass=MyParser",
								   new File (srcDir, "A.java").getPath (),
								   new File (srcDir, "B.java").getPath ()),
							 task.commands.get (0));
	}

	@Test
	public void testJavaNonJavaLangHasNoOutputDir () throws Exception
	{
		File srcDir = tmp.newFolder ("src");
		RecordingTask task = new RecordingTask ();
		task.setSrcDir (srcDir);
		task.setLang ("plain");
		task.setSrc ("A.java");
		task.execute ();

		List<String> args = task.commands.get (0);
		Assert.assertTrue (args.contains ("-Alang=plain"));
		for (String arg : args)
			Assert.assertFalse (arg, arg.startsWith ("-Ad="));
	}

	@Test
	public void testJavaRequiresSrcDir ()
	{
		RecordingTask task = new RecordingTask ();
		task.setSrc ("A.java");
		try
		{
			task.execute ();
			Assert.fail ("expected the task to fail");
		}
		catch (IllegalArgumentException ex)
		{
			Assert.assertEquals ("Source directory is not specified.", ex.getMessage ());
		}
		Assert.assertTrue (task.commands.isEmpty ());
	}

	@Test
	public void testJavaFailure () throws Exception
	{
		RecordingTask task = new RecordingTask ();
		task.fail = true;
		task.setSrcDir (tmp.newFolder ("src"));
		task.setSrc ("A.java");
		try
		{
			task.execute ();
			Assert.fail ("expected the task to fail");
		}
		catch (RuntimeException ex)
		{
			Assert.assertEquals ("Error executing CookCC using APT", ex.getMessage ());
		}
	}

	@Test
	public void testXccRunsBeforeJava () throws Exception
	{
		RecordingTask task = new RecordingTask ();
		task.setSrcDir (tmp.newFolder ("src"));
		task.setSrc ("A.java grammar.xcc");
		task.execute ();

		Assert.assertEquals (2, task.commands.size ());
		Assert.assertTrue (task.executables.get (0).matches ("java(\\.exe)?"));
		Assert.assertTrue (task.executables.get (1).matches ("javac(\\.exe)?"));
	}

	//
	// Attribute validation.
	//

	@Test
	public void testLexerTableValues ()
	{
		Task task = new Task ();
		task.setLexerTable ("ecs");
		task.setLexerTable ("full");
		task.setLexerTable ("compressed");
		try
		{
			task.setLexerTable ("unknown");
			Assert.fail ("expected an invalid lexer table to be rejected");
		}
		catch (IllegalArgumentException ex)
		{
			Assert.assertEquals ("Unknown lexer table: unknown", ex.getMessage ());
		}
	}

	@Test
	public void testParserTableValues ()
	{
		Task task = new Task ();
		task.setParserTable ("ecs");
		task.setParserTable ("compressed");
		try
		{
			// "full" is valid for the lexer, but not for the parser
			task.setParserTable ("full");
			Assert.fail ("expected an invalid parser table to be rejected");
		}
		catch (IllegalArgumentException ex)
		{
			Assert.assertEquals ("Unknown parser table: full", ex.getMessage ());
		}
	}

	@Test (expected = IllegalArgumentException.class)
	public void testSrcDirMustExist ()
	{
		new Task ().setSrcDir (new File (tmp.getRoot (), "missing"));
	}

	@Test (expected = IllegalArgumentException.class)
	public void testDestDirMustBeDirectory () throws Exception
	{
		new Task ().setDestDir (tmp.newFile ("file.txt"));
	}

	//
	// Environment checks.
	//

	@Test
	public void testRequiresJava8 ()
	{
		RecordingTask task = new RecordingTask ()
		{
			@Override
			protected boolean isJava8OrLater ()
			{
				return false;
			}
		};
		task.setSrc ("grammar.xcc");
		try
		{
			task.execute ();
			Assert.fail ("expected the task to fail");
		}
		catch (BuildException ex)
		{
			Assert.assertTrue (ex.getMessage (), ex.getMessage ().startsWith ("CookCC requires Java 8 or later"));
		}
		Assert.assertTrue (task.commands.isEmpty ());
	}

	@Test
	public void testCurrentJavaIsAccepted ()
	{
		Assert.assertTrue (new Task ().isJava8OrLater ());
	}

	@Test
	public void testJavacFoundInJdk ()
	{
		// the tests run on a JDK, so javac is found without a warning
		LoggingTask task = new LoggingTask ();
		String javac = task.getJavacExecutable ();
		Assert.assertTrue (javac, new File (javac).isFile ());
		Assert.assertTrue (task.warnings.isEmpty ());
	}

	//
	// Running commands.
	//

	@Test
	public void testExecuteCmdSuccess ()
	{
		LoggingTask task = new LoggingTask ();
		Commandline cmd = new Commandline ();
		cmd.setExecutable (JavaEnvUtils.getJreExecutable ("java"));
		cmd.createArgument ().setValue ("-version");

		Assert.assertFalse (task.executeCmd (cmd));
		Assert.assertTrue (task.errors.isEmpty ());
	}

	@Test
	public void testExecuteCmdReportsExitCode ()
	{
		LoggingTask task = new LoggingTask ();
		Commandline cmd = new Commandline ();
		cmd.setExecutable (JavaEnvUtils.getJreExecutable ("java"));
		cmd.createArgument ().setValue ("-no-such-option");

		Assert.assertTrue (task.executeCmd (cmd));
		Assert.assertEquals (1, task.errors.size ());
		Assert.assertTrue (task.errors.get (0), task.errors.get (0).contains ("exited with code"));
	}

	@Test
	public void testExecuteCmdReportsStartFailure ()
	{
		LoggingTask task = new LoggingTask ();
		Commandline cmd = new Commandline ();
		cmd.setExecutable ("cookcc-no-such-program");

		Assert.assertTrue (task.executeCmd (cmd));
		Assert.assertEquals (1, task.errors.size ());
		Assert.assertTrue (task.errors.get (0), task.errors.get (0).startsWith ("Unable to run cookcc-no-such-program: "));
	}

	//
	// Classpath lookup.
	//

	/**
	 * The task can only find the CookCC classpath when Ant loads it through an
	 * AntClassLoader, i.e. through &lt;taskdef classpath="..."&gt;.  Loaded any
	 * other way (here: the plain test classpath), it fails before running anything.
	 */
	@Test
	public void testCookCCPathRequiresAntClassLoader ()
	{
		final List<Commandline> executed = new ArrayList<Commandline> ();
		Task task = new Task ()
		{
			@Override
			protected boolean executeCmd (Commandline cmd)
			{
				executed.add (cmd);
				return false;
			}
		};
		task.setSrc ("grammar.xcc");
		try
		{
			task.execute ();
			Assert.fail ("expected the task to fail");
		}
		catch (RuntimeException ex)
		{
			Assert.assertEquals ("Unable to determine the runtime path of CookCC.", ex.getMessage ());
		}
		Assert.assertTrue (executed.isEmpty ());
	}
}
