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
package org.yuanheng.cookcc.input.javaap;

import java.util.HashMap;
import java.util.Map;

import org.yuanheng.cookcc.Main;
import org.yuanheng.cookcc.OptionMap;
import org.yuanheng.cookcc.codegen.java.JavaCodeGen;
import org.yuanheng.cookcc.codegen.options.AbstractOption;
import org.yuanheng.cookcc.codegen.options.ClassOption;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.interfaces.CodeGen;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.util.DeclarationVisitors;

/**
 * @author Heng Yuan
 * @version $Id$
 */
class CookCCProcessor implements AnnotationProcessor
{
	static Map<String, String> convertOptions (Map<String, String> aptOptions)
	{
		HashMap<String, String> ccOptions = new HashMap<String, String> ();
		for (String option : aptOptions.keySet ())
		{
			if (!option.startsWith ("-A"))
				continue;
			option = "-" + option.substring (2);
			String value = null;
			int index = option.indexOf ('=');
			if (index >= 0)
			{
				value = option.substring (index + 1);
				option = option.substring (0, index);
			}
			ccOptions.put (option, value);
		}
		return ccOptions;
	}

	private final AnnotationProcessorEnvironment m_env;
	private final CodeGen m_codeGen;

	CookCCProcessor (AnnotationProcessorEnvironment env) throws Exception
	{
		m_env = env;
		Map<String, String> ccOptions = convertOptions (env.getOptions ());
		Main.parseOptions (ccOptions);
		m_codeGen = Main.getCodeGen ();
	}

	public void process ()
	{
		CookCCVisitor visitor = new CookCCVisitor ();
		for (TypeDeclaration typeDecl : m_env.getSpecifiedTypeDeclarations ())
			typeDecl.accept (DeclarationVisitors.getDeclarationScanner (visitor, DeclarationVisitors.NO_OP));
		visitor.process ();
		Document[] docs = visitor.getDocuments ();
		OptionMap options = m_codeGen.getOptions ();
		for (Document doc : docs)
		{
			try
			{
				// setup the code generators with some options that are geared toward
				// JavaCodeGen.  Other code generators would ignore them.

				options.addOption (ClassOption.OPTION_CLASS, ClassVisitor.getOutputClass (doc));
				// very important, this option should be consulted from OptionMap
				if (ClassVisitor.isPublic (doc))
					options.addOption (JavaCodeGen.OPTION_PUBLIC);
				else
					options.removeOption (JavaCodeGen.OPTION_PUBLIC);
				options.addOption (AbstractOption.OPTION_ABSTRACT);

				m_codeGen.generateOutput (doc);
			}
			catch (Exception ex)
			{
				ex.printStackTrace ();
			}
		}
	}
}
