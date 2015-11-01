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
package org.yuanheng.cookcc.input.ap;

import java.io.File;
import java.io.StringWriter;
import java.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.yuanheng.cookcc.*;
import org.yuanheng.cookcc.codegen.TemplatedCodeGen;
import org.yuanheng.cookcc.codegen.java.JavaCodeGen;
import org.yuanheng.cookcc.codegen.options.AbstractOption;
import org.yuanheng.cookcc.codegen.options.ClassOption;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.doc.TokensDoc;
import org.yuanheng.cookcc.interfaces.CodeGen;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import freemarker.template.Template;

/**
 * @author Heng Yuan
 * @version $Id: CookCCProcessor.java 486 2008-11-09 15:09:57Z superduperhengyuan $
 */
public class CookCCProcessor implements Processor
{
	public final static String TEMPLATE_URI = "resources/templates/javaap/javaap.ftl";

	private static class Resources
	{
		private static Template template;

		static
		{
			try
			{
				template = TemplatedCodeGen.getTemplate (TEMPLATE_URI);
			}
			catch (Exception ex)
			{
				ex.printStackTrace ();
			}
		}
	}

	private static void addTokens (Document doc, Collection<TokensDoc> tokens)
	{
		if (doc == null || tokens == null)
			return;
		for (TokensDoc tokensDoc : tokens)
			doc.addTokens (tokensDoc);
	}

	static Map<String, String> convertOptions (Map<String, String> aptOptions)
	{
		HashMap<String, String> ccOptions = new HashMap<String, String> ();
		for (Map.Entry<String, String> entry: aptOptions.entrySet ())
		{
			String option = "-" + entry.getKey ();
			String value = entry.getValue ();
			ccOptions.put (option, value);
		}
		return ccOptions;
	}

	private ProcessingEnvironment m_env;
	private Types m_typeUtils;
	private Elements m_elementUtils;
	// private Filer m_filer;
	private Messager m_messager;
	private Trees m_trees;

	private final Map<String, Document> m_docs = new HashMap<String, Document> ();
	private final Map<String, Collection<TokensDoc>> m_tokens = new HashMap<String, Collection<TokensDoc>> ();

	public CookCCProcessor ()
	{
	}

	public void log (String message)
	{
		// m_messager.printMessage (Kind.NOTE, message);
	}

	public void warn (String message)
	{
		m_messager.printMessage (Kind.WARNING, message);
	}

	public void error (String message)
	{
		m_messager.printMessage (Kind.ERROR, message);
	}

	public Element getElement (TypeMirror t)
	{
		return m_typeUtils.asElement (t);
	}

	public String getJavaDoc (Element e)
	{
		return m_elementUtils.getDocComment (e);
	}

	public long getLineNumber (Element e)
	{
		if (m_trees == null)
			m_trees = Trees.instance (m_env);
		TreePath tp = m_trees.getPath (e);
		CompilationUnitTree cu = tp.getCompilationUnit ();
		long startOffset = m_trees.getSourcePositions ().getStartPosition (cu, tp.getLeaf ());
		return cu.getLineMap ().getLineNumber (startOffset);
	}

	public File getFile (Element e)
	{
		if (m_trees == null)
			m_trees = Trees.instance (m_env);
		TreePath tp = m_trees.getPath (e);
		CompilationUnitTree cu = tp.getCompilationUnit ();
		JavaFileObject src = cu.getSourceFile ();
		if (src.getKind () != JavaFileObject.Kind.SOURCE)
		{
			return null;
		}
		return new File (src.getName ());
	}

	Document getDocument (String className)
	{
		return m_docs.get (className);
	}

	Document[] getDocuments ()
	{
		return m_docs.values ().toArray (new Document[m_docs.size ()]);
	}

	Collection<TokensDoc> getTokenEnum (String className)
	{
		return m_tokens.get (className);
	}

	/**
	 * Assign tokens doc for parsers.
	 */
	private void assignTokensDoc ()
	{
		if (m_tokens.size () == 0)
			return;
		HashSet<String> usedTokenClass = new HashSet<String> ();
		for (Document doc : m_docs.values ())
		{
			if (doc.hasTokens ())
				continue;
			// check if the user specified a token class
			String cl = ClassVisitor.getTokenClass (doc);
			if (cl != null)
			{
				Collection<TokensDoc> tokensDocs = m_tokens.get (cl);
				if (tokensDocs == null)
					throw new IllegalArgumentException ("Unable to find the token class " + cl);
				addTokens (doc, tokensDocs);
				usedTokenClass.add (cl);

				continue;
			}
			// now try to look for nested class
			String match = ClassVisitor.getInputClass (doc) + ".";
			for (String tokenCl : m_tokens.keySet ())
			{
				if (tokenCl.startsWith (match) && !usedTokenClass.contains (tokenCl))
				{
					addTokens (doc, m_tokens.get (tokenCl));
					usedTokenClass.add (tokenCl);
					break;
				}
			}
		}
	}

	private void addDefaultCode ()
	{
		for (Document doc : m_docs.values ())
		{
			try
			{
				Map<String, Object> map = new HashMap<String, Object> ();
				StringWriter sw = new StringWriter ();
				String inputClass = ClassVisitor.getInputClass (doc);
				map.put ("child", inputClass);
				if (doc.getLexer () != null)
					map.put ("states", doc.getLexer ().getLexerStates ());
				Resources.template.process (map, sw);
				doc.addCode ("default", sw.toString ());
			}
			catch (Exception ex)
			{
				ex.printStackTrace ();
			}
		}
	}

	private void addOptionMap (HashSet<String> set, OptionMap options)
	{
		for (String option : options.getAvailableOptions ())
		{
			option = option.substring (1);
			set.add (option);
		}
	}

	/**
	 * We here returns all possible options CookCC supports
	 *
	 * @return	a set of string options CookCC supports
	 */
	@Override
	public Set<String> getSupportedOptions ()
	{
		HashSet<String> set = new HashSet<String> ();
		// add the global options
		OptionMap options = Main.getOptions ();
		addOptionMap (set, options);

		// for each language, add its options
		for (String lang : Main.getLanguages ())
		{
			try
			{
				options = Main.getCodeGen (lang).getOptions ();
				addOptionMap (set, options);
			}
			catch (Exception ex)
			{
			}
		}
		return set;
	}

	@Override
	public Set<String> getSupportedAnnotationTypes ()
	{
		HashSet<String> set = new HashSet<String> ();
		set.add ("org.yuanheng.cookcc.*");
		return set;
	}

	@Override
	public SourceVersion getSupportedSourceVersion ()
	{
		return SourceVersion.latestSupported ();
	}

	@Override
	public void init (ProcessingEnvironment env)
	{
		m_env = env;
		m_typeUtils = env.getTypeUtils ();
		m_elementUtils = env.getElementUtils ();
		// m_filer = env.getFiler ();
		m_messager = env.getMessager ();

		log ("Initiating COOKCC APT");

		Map<String, String> ccOptions = convertOptions (env.getOptions ());
		try
		{
			Main.parseOptions (ccOptions);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void visit (Element elem)
	{
		if (elem.getKind () == ElementKind.ENUM)
		{
			TypeElement typeElem = (TypeElement)elem;
			String className = typeElem.getQualifiedName ().toString ();

			if (m_tokens.get (className) != null)
			{
				// already has it
				log ("Already visited enum " + typeElem + ".");
				return;
			}
			EnumVisitor enumVisitor = new EnumVisitor (this);
			enumVisitor.visit (typeElem);
			m_tokens.put (className, enumVisitor.getTokens ());
		}
		else if (elem.getKind () == ElementKind.CLASS)
		{
			TypeElement typeElem = (TypeElement)elem;
			String className = typeElem.getQualifiedName ().toString ();

			CookCCOption option = typeElem.getAnnotation (CookCCOption.class);
			if (option == null)
			{
				return;
			}

			if (m_docs.get (className) != null)
			{
				// already has it
				log ("Already visited class " + typeElem + ".");
				return;
			}

			ClassVisitor classVisitor = new ClassVisitor (this);
			classVisitor.visit (option, typeElem);
			m_docs.put (className, classVisitor.getDoc ());
		}
	}

	@Override
	public boolean process (Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		if (roundEnv.processingOver ())
			return false;
		try
		{
			// Scan CookCCToken classes
			for (Element elem : roundEnv.getElementsAnnotatedWith (CookCCToken.class))
			{
				visit (elem);
			}

			// Scan CookCCOption classes
			for (Element elem : roundEnv.getElementsAnnotatedWith (CookCCOption.class))
			{
				visit (elem);
			}

			assignTokensDoc ();
			addDefaultCode ();

			// Scan TokenDoc classes
			CodeGen codeGen = Main.getCodeGen ();
			OptionMap options = codeGen.getOptions ();
			for (Document doc : m_docs.values ())
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
					options.addOption (JavaCodeGen.OPTION_GENERICS);
					options.addOption (AbstractOption.OPTION_ABSTRACT);
					codeGen.generateOutput (doc);
				}
				catch (Exception ex)
				{
					error (ex.toString ());
					ex.printStackTrace ();
				}
			}
			return m_docs.size () > 0;
		}
		catch (Exception ex)
		{
			error (ex.getMessage ());
			//ex.printStackTrace ();
			return false;
		}
	}

	@Override
	public Iterable<? extends Completion> getCompletions (Element element, AnnotationMirror annotation, ExecutableElement member, String userText)
	{
		return null;
	}
}
