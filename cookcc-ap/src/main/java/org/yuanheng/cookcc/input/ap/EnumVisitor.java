/*
 * Copyright (c) 2008, Heng Yuan
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
