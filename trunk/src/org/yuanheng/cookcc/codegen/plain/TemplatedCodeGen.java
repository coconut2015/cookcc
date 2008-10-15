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
package org.yuanheng.cookcc.codegen.plain;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.LinkedList;
import java.util.Map;

import org.yuanheng.cookcc.dfa.ECSTable;
import org.yuanheng.cookcc.dfa.FullTable;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.doc.LexerStateDoc;
import org.yuanheng.cookcc.doc.RuleDoc;
import org.yuanheng.cookcc.lexer.Lexer;

import freemarker.cache.TemplateLoader;
import freemarker.core.HexBI;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;

/**
 * A utility class for code generators.
 *
 * @author Heng Yuan
 * @version $Id$
 */
public abstract class TemplatedCodeGen
{
	private static Configuration s_configuration;

	static
	{
		HexBI.init ();

		Configuration cfg = new Configuration ();
		cfg.setTemplateLoader (new TemplateLoader()
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

	public void setup (Map<String, Object> map, Document doc)
	{
		Lexer lexer = Lexer.getLexer (doc);
		if (lexer == null)
			return;
		map.put ("bol", Boolean.valueOf (lexer.hasBOL ()));
		map.put ("backup", Boolean.valueOf (lexer.hasBackup ()));
		map.put ("header", doc.getHeader ());
		map.put ("cases", lexer.getCaseCount ());

		map.put ("statistics", lexer);

		map.put ("lexerCases", getLexerCases (doc));
		map.put ("accepts", lexer.getDFA ().getAccepts ());

		String table = doc.getLexer ().getTable ();
		map.put ("table", table);
		if ("ecs".equals (table))
		{
			ECSTable ecsTable = new ECSTable (lexer);
			ecsTable.setup (map);
		}
		else if ("full".equals (table))
		{
			FullTable fullTable = new FullTable (lexer);
			fullTable.setup (map);
		}
	}

	private RuleDoc[] getLexerCases (Document doc)
	{
		LinkedList<RuleDoc> rules = new LinkedList<RuleDoc> ();
		LexerStateDoc[] lexerStates = doc.getLexer ().getLexerStates ();
		for (int i = 0; i < lexerStates.length; ++i)
		{
			LexerStateDoc lexerState = lexerStates[i];
			for (RuleDoc rule : lexerState.getRules ())
				rules.add (rule);
		}
		return rules.toArray (new RuleDoc[rules.size ()]);
	}
}
