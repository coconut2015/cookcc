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

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.yuanheng.cookcc.codegen.TemplatedCodeGen;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.doc.TokensDoc;

import com.sun.mirror.declaration.*;
import com.sun.mirror.util.DeclarationVisitor;

import freemarker.template.Template;

/**
 * @author Heng Yuan
 */
class CookCCVisitor implements DeclarationVisitor
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

	private final Map<String, Document> m_docs = new HashMap<String, Document> ();
	private final Map<String, Collection<TokensDoc>> m_tokens = new HashMap<String, Collection<TokensDoc>> ();
	private DeclarationVisitor m_visitor;

	void addDocument (String className, Document document)
	{
		m_docs.put (className, document);
	}

	Document getDocument (String className)
	{
		return m_docs.get (className);
	}

	Document[] getDocuments ()
	{
		return m_docs.values ().toArray (new Document[m_docs.size ()]);
	}

	void addTokenEnum (String className, Collection<TokensDoc> tokens)
	{
		m_tokens.put (className, tokens);
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

	void process ()
	{
		assignTokensDoc ();
		addDefaultCode ();
	}

	public void visitDeclaration (Declaration declaration)
	{
		if (m_visitor != null)
			m_visitor.visitDeclaration (declaration);
	}

	public void visitPackageDeclaration (PackageDeclaration packageDeclaration)
	{
		if (m_visitor != null)
			m_visitor.visitPackageDeclaration (packageDeclaration);
	}

	public void visitMemberDeclaration (MemberDeclaration memberDeclaration)
	{
		if (m_visitor != null)
			m_visitor.visitMemberDeclaration (memberDeclaration);
	}

	public void visitTypeDeclaration (TypeDeclaration typeDeclaration)
	{
		if (m_visitor != null)
			m_visitor.visitTypeDeclaration (typeDeclaration);
	}

	public void visitClassDeclaration (ClassDeclaration classDeclaration)
	{
		m_visitor = new ClassVisitor (this);
		m_visitor.visitClassDeclaration (classDeclaration);
	}

	public void visitEnumDeclaration (EnumDeclaration enumDeclaration)
	{
		m_visitor = new EnumVisitor (this);
		m_visitor.visitEnumDeclaration (enumDeclaration);
	}

	public void visitInterfaceDeclaration (InterfaceDeclaration interfaceDeclaration)
	{
		if (m_visitor != null)
			m_visitor.visitInterfaceDeclaration (interfaceDeclaration);
	}

	public void visitAnnotationTypeDeclaration (AnnotationTypeDeclaration annotationTypeDeclaration)
	{
		if (m_visitor != null)
			m_visitor.visitAnnotationTypeDeclaration (annotationTypeDeclaration);
	}

	public void visitFieldDeclaration (FieldDeclaration fieldDeclaration)
	{
		if (m_visitor != null)
			m_visitor.visitFieldDeclaration (fieldDeclaration);
	}

	public void visitEnumConstantDeclaration (EnumConstantDeclaration enumConstantDeclaration)
	{
		if (m_visitor != null)
			m_visitor.visitEnumConstantDeclaration (enumConstantDeclaration);
	}

	public void visitExecutableDeclaration (ExecutableDeclaration executableDeclaration)
	{
		if (m_visitor != null)
			m_visitor.visitExecutableDeclaration (executableDeclaration);
	}

	public void visitConstructorDeclaration (ConstructorDeclaration constructorDeclaration)
	{
		if (m_visitor != null)
			m_visitor.visitConstructorDeclaration (constructorDeclaration);
	}

	public void visitMethodDeclaration (MethodDeclaration methodDeclaration)
	{
		if (m_visitor != null)
			m_visitor.visitMethodDeclaration (methodDeclaration);
	}

	public void visitAnnotationTypeElementDeclaration (AnnotationTypeElementDeclaration annotationTypeElementDeclaration)
	{
		if (m_visitor != null)
			m_visitor.visitAnnotationTypeElementDeclaration (annotationTypeElementDeclaration);
	}

	public void visitParameterDeclaration (ParameterDeclaration parameterDeclaration)
	{
		if (m_visitor != null)
			m_visitor.visitParameterDeclaration (parameterDeclaration);
	}

	public void visitTypeParameterDeclaration (TypeParameterDeclaration typeParameterDeclaration)
	{
		if (m_visitor != null)
			m_visitor.visitTypeParameterDeclaration (typeParameterDeclaration);
	}
}