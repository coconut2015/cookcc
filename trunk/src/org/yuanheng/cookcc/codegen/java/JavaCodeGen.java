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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import org.antlr.stringtemplate.StringTemplate;
import org.yuanheng.cookcc.codegen.plain.TemplatedCodeGen;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.interfaces.CodeGen;
import org.yuanheng.cookcc.interfaces.OptionParser;
import org.yuanheng.cookcc.lexer.Lexer;

import cookxml.core.util.TextUtils;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class JavaCodeGen extends TemplatedCodeGen implements CodeGen
{
	public final static String DEFAULTS_URI = "/resources/templates/java/defaults.properties";
	public final static String TEMPLATE_URI = "resources/templates/java/class.txt";

	private static class Resources
	{
		private final static Properties defaults = new Properties ();
		private static String template;
		static
		{
			try
			{
				defaults.load (Resources.class.getResourceAsStream (DEFAULTS_URI));
				template = TextUtils.readText (TEMPLATE_URI, Resources.class.getClassLoader ());
			}
			catch (Exception ex)
			{
				ex.printStackTrace ();
			}
		}
	}

	private void printCharArray (char[] array, PrintWriter p)
	{
		for (int i = 0; i < array.length; ++i)
		{
			p.print ("\\u");
			p.print (Integer.toHexString (array[i]));
		}
	}

	private void generateLexerOutput (Document doc, PrintWriter p)
	{
		Lexer lexer = Lexer.getLexer (doc);
		if (lexer == null)
			return;

		StringTemplate st = new StringTemplate (Resources.template);
		st.setAttributes (Resources.defaults);
		setup (st, doc);

		p.println (st);
	}

	public void generateOutput (Document doc, OutputStream os)
	{
		PrintWriter p = new PrintWriter (os);
		generateLexerOutput (doc, p);
		p.flush ();
	}

	public OptionParser[] getOptionParsers ()
	{
		return new OptionParser[0];
	}
}