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

import java.util.Collection;
import java.util.LinkedList;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import org.yuanheng.cookcc.TokenGroup;
import org.yuanheng.cookcc.doc.TokensDoc;

/**
 * @author Heng Yuan
 * @version $Id: EnumVisitor.java 486 2008-11-09 15:09:57Z superduperhengyuan $
 */
class EnumVisitor
{
	private final CookCCProcessor m_parent;
	private final Collection<TokensDoc> m_tokens = new LinkedList<TokensDoc> ();

	private TokensDoc m_doc;

	EnumVisitor (CookCCProcessor parent)
	{
		m_parent = parent;
	}

	public void visit (TypeElement typeElem)
	{
		m_parent.log ("Visiting enum type " + typeElem + " {");
		for (Element childElem : typeElem.getEnclosedElements ())
		{
			// ignore anything that are not enum constants
			// (there could be variables, constructors, etc)
			if (childElem.getKind () != ElementKind.ENUM_CONSTANT)
				continue;

			TokenGroup tg = childElem.getAnnotation (TokenGroup.class);
			m_parent.log ("\t" + (tg == null ? "" : tg.toString ()) + childElem + ",");
			if (m_doc == null || tg != null)
			{
				m_doc = new TokensDoc ();
				if (tg != null)
					m_doc.setType (tg.type ().toString ());
				m_tokens.add (m_doc);
			}
			m_doc.addTokens (childElem.getSimpleName ().toString ());
		}
		m_parent.log ("}");
	}

	public Collection<TokensDoc> getTokens ()
	{
		return m_tokens;
	}
}
