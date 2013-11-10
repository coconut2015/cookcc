/*
 * Copyright (c) 2008-2013, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *    Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    Neither the name of the Heng Yuan nor the
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
package org.yuanheng.cookcc.codegen.xml;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.yuanheng.cookcc.OptionMap;
import org.yuanheng.cookcc.codegen.options.OutputOption;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.doc.TokensDoc;
import org.yuanheng.cookcc.interfaces.CodeGen;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class XmlCodeGen implements CodeGen
{
	private final OutputOption m_outputOption = new OutputOption ();

	private final OptionMap m_options = new OptionMap ();

	{
		m_options.registerOptionHandler (m_outputOption);
	}

	private void printTokens (Document doc, PrintWriter p)
	{
		for (TokensDoc tokens : doc.getTokens ())
		{
			String[] ts = tokens.getTokens ();
			if (ts == null)
				continue;
			p.print ("\t<tokens" + (tokens.getType () == null ? "" : " type=\"" + tokens.getType () + "\"") + "><![CDATA[");
			for (int i = 0; i < ts.length; ++i)
			{
				if ((i % 5) > 0)
					p.print (" ");
				else
				{
					p.println ();
					p.print ("\t\t");
				}
				p.print (ts[i]);
			}
			p.println ();
			p.println ("\t]]></tokens>");
		}
	}

	private void printDocument (Document doc, PrintWriter p)
	{
		p.println ("<?xml version = \"1.0\" encoding=\"UTF-8\"?>");
		p.println ("<!DOCTYPE cookcc PUBLIC \"-//CookCC//1.0\" \"http://code.google.com/p/cookcc/source/browse/trunk/src/resources/cookcc.dtd\">");
		p.println ("<cookcc>");

		Map<String, String> codeMap = doc.getCode ();

		for (String key : codeMap.keySet ())
		{
			String code = codeMap.get (key);
			if (code != null && code.length () > 0)
				p.println ("\t<code name=\"" + key + "\">" + Utils.translate (code) + "</code>");
		}

		printTokens (doc, p);
		new XmlLexerOutput ().printLexer (doc.getLexer (), p);
		new XmlParserOutput ().printParserDoc (doc.getParser (), p);
		p.println ("</cookcc>");
	}

	public void generateOutput (Document doc) throws Exception
	{
		PrintWriter p;
		StringWriter sw;
		if (m_outputOption.getOutput () == null)
		{
			p = new PrintWriter (System.out);
			sw = null;
		}
		else
		{
			sw = new StringWriter ();
			p = new PrintWriter (sw);
		}
		printDocument (doc, p);
		p.flush ();
		p.close ();
		if (sw != null)
		{
			FileWriter fw = new FileWriter (m_outputOption.getOutput ());
			fw.write (sw.toString ());
			fw.close ();
		}
	}

	public OptionMap getOptions ()
	{
		return m_options;
	}
}
