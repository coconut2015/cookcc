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
import freemarker.core.*;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;

/**
 * A utility class for code generators.
 *
 * @author Heng Yuan
 */
public abstract class TemplatedCodeGen implements CodeGen
{
	private static Configuration s_configuration;

	static
	{
		HexBI.init ();
		JavaStringResourceBI.init ();
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
		map.put ("unicode", Boolean.valueOf (doc.isUnicode () || Main.isUnicode (options)));

		if (lexer != null)
			map.put ("lexer", LexerDFAInfo.getLexerDFAInfo (doc, options));
		if (parser != null)
			map.put ("parser", ParserDFAInfo.getParserDFAInfo (doc, options));
	}
}
