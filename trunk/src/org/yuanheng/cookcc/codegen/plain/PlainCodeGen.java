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

import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
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
 * @version $Id$
 */
public class PlainCodeGen extends TemplatedCodeGen implements CodeGen
{
	public final static String TEMPLATE_URI = "resources/templates/plain/plain.ftl";

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

	public OptionMap getOptions ()
	{
		return m_options;
	}
}