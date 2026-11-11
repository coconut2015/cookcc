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
