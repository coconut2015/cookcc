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

import java.util.Collection;
import java.util.LinkedList;

import org.yuanheng.cookcc.CookCCToken;
import org.yuanheng.cookcc.TokenGroup;
import org.yuanheng.cookcc.doc.TokensDoc;

import com.sun.mirror.declaration.*;
import com.sun.mirror.util.DeclarationVisitor;

/**
 * @author Heng Yuan
 */
class EnumVisitor implements DeclarationVisitor
{
	private final CookCCVisitor m_parent;
	private final Collection<TokensDoc> m_tokens = new LinkedList<TokensDoc> ();

	private TokensDoc m_doc;

	EnumVisitor (CookCCVisitor parent)
	{
		m_parent = parent;
	}

	public void visitDeclaration (Declaration declaration)
	{
	}

	public void visitPackageDeclaration (PackageDeclaration packageDeclaration)
	{
	}

	public void visitMemberDeclaration (MemberDeclaration memberDeclaration)
	{
	}

	public void visitTypeDeclaration (TypeDeclaration typeDeclaration)
	{
	}

	public void visitClassDeclaration (ClassDeclaration classDeclaration)
	{
	}

	public void visitEnumDeclaration (EnumDeclaration enumDeclaration)
	{
		if (enumDeclaration.getAnnotation (CookCCToken.class) == null)
			return;
		m_parent.addTokenEnum (enumDeclaration.getQualifiedName (), m_tokens);
	}

	public void visitInterfaceDeclaration (InterfaceDeclaration interfaceDeclaration)
	{
	}

	public void visitAnnotationTypeDeclaration (AnnotationTypeDeclaration annotationTypeDeclaration)
	{
	}

	public void visitFieldDeclaration (FieldDeclaration fieldDeclaration)
	{
	}

	public void visitEnumConstantDeclaration (EnumConstantDeclaration enumConstantDeclaration)
	{
		TokenGroup tg = null;
		if (m_doc == null || (tg = enumConstantDeclaration.getAnnotation (TokenGroup.class)) != null)
		{
			m_doc = new TokensDoc ();
			if (tg != null)
				m_doc.setType (tg.type ().toString ());
			m_tokens.add (m_doc);
		}
		m_doc.addTokens (enumConstantDeclaration.getSimpleName ());
	}

	public void visitExecutableDeclaration (ExecutableDeclaration executableDeclaration)
	{
	}

	public void visitConstructorDeclaration (ConstructorDeclaration constructorDeclaration)
	{
	}

	public void visitMethodDeclaration (MethodDeclaration methodDeclaration)
	{
	}

	public void visitAnnotationTypeElementDeclaration (AnnotationTypeElementDeclaration annotationTypeElementDeclaration)
	{
	}

	public void visitParameterDeclaration (ParameterDeclaration parameterDeclaration)
	{
	}

	public void visitTypeParameterDeclaration (TypeParameterDeclaration typeParameterDeclaration)
	{
	}
}