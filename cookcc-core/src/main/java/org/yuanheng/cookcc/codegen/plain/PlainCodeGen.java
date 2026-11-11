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
package org.yuanheng.cookcc.codegen.plain;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.yuanheng.cookcc.OptionMap;
import org.yuanheng.cookcc.codegen.TemplatedCodeGen;
import org.yuanheng.cookcc.codegen.options.OutputOption;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.interfaces.CodeGen;

import freemarker.template.Template;

/**
 * @author Heng Yuan
 */
public class PlainCodeGen extends TemplatedCodeGen implements CodeGen
{
	public final static String TEMPLATE_URI = "templates/plain/plain.ftl";

	private static class Resources
	{
		private static Template template;

		static
		{
			template = getTemplate (TEMPLATE_URI);
		}
	}

	private final OutputOption m_outputOption = new OutputOption ();

	private final OptionMap m_options = new OptionMap ();

	{
		m_options.registerOptionHandler (m_outputOption);
	}

	@Override
	public void generateOutput (Document doc) throws Exception
	{
		if (doc.getLexer () == null && doc.getParser () == null)
			return;

		Map<String, Object> map = new HashMap<String, Object> ();
		Writer writer;
		boolean bufferFirst;
		if (m_outputOption.getOutput () == null)
		{
			writer = new OutputStreamWriter (System.out);
			bufferFirst = false;
		}
		else
		{
			bufferFirst = true;
			writer = new StringWriter ();
		}
		setup (map, doc);
		Resources.template.process (map, writer);
		writer.close ();
		if (bufferFirst)
		{
			FileWriter fw = new FileWriter (m_outputOption.getOutput ());
			fw.write (writer.toString ());
			fw.close ();
		}
	}

	@Override
	public OptionMap getOptions ()
	{
		return m_options;
	}
}
