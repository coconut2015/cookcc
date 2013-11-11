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
package org.yuanheng.cookcc.codegen;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Map;

import org.yuanheng.cookcc.Main;
import org.yuanheng.cookcc.OptionMap;
import org.yuanheng.cookcc.dfa.LexerDFAInfo;
import org.yuanheng.cookcc.dfa.ParserDFAInfo;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.doc.LexerDoc;
import org.yuanheng.cookcc.doc.ParserDoc;
import org.yuanheng.cookcc.interfaces.CodeGen;

import freemarker.cache.TemplateLoader;
import freemarker.core.ActionCodeBI;
import freemarker.core.HexBI;
import freemarker.core.JavaStringBI;
import freemarker.core.TypeBI;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;

/**
 * A utility class for code generators.
 *
 * @author Heng Yuan
 * @version $Id$
 */
public abstract class TemplatedCodeGen implements CodeGen
{
	private static Configuration s_configuration;

	static
	{
		HexBI.init ();
		JavaStringBI.init ();
		ActionCodeBI.init ();
		TypeBI.init ();

		Configuration cfg = new Configuration ();
		cfg.setTemplateLoader (new TemplateLoader ()
		{
			public Object findTemplateSource (String name) throws IOException
			{
				return getClass ().getClassLoader ().getResource (name);
			}

			public long getLastModified (Object templateSource)
			{
				return 0;
			}

			public Reader getReader (Object templateSource, String encoding) throws IOException
			{
				return new InputStreamReader (((URL)templateSource).openStream ());
			}

			public void closeTemplateSource (Object templateSource) throws IOException
			{
			}
		});
		cfg.setObjectWrapper (ObjectWrapper.BEANS_WRAPPER);
		s_configuration = cfg;
	}

	public static Template getTemplate (String source)
	{
		try
		{
			return s_configuration.getTemplate (source);
		}
		catch (IOException ex)
		{
			ex.printStackTrace ();
			return null;
		}
	}

	public void setup (Map<String, Object> map, Document doc) throws IOException
	{
		OptionMap options = getOptions ();

		LexerDoc lexer = doc.getLexer ();
		ParserDoc parser = doc.getParser ();

		if (lexer != null)
		{
			String table = Main.getLexerTable (options);
			if (table != null)
				lexer.setTable (table);
		}
		if (parser != null)
		{
			String table = Main.getParserTable (options);
			if (table != null)
				parser.setTable (table);
			if (Main.getDefaultReduce (options))
				parser.setDefaultReduce (true);
		}

		map.put ("debug", Main.isDebug (options));
		map.put ("tokens", doc.getTokens ());
		map.put ("code", doc.getCode ());
		map.put ("unicode", Boolean.valueOf (doc.isUnicode ()));

		if (lexer != null)
			map.put ("lexer", LexerDFAInfo.getLexerDFAInfo (doc, options));
		if (parser != null)
			map.put ("parser", ParserDFAInfo.getParserDFAInfo (doc, options));
	}
}
