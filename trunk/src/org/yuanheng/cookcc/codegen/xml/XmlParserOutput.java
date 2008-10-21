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
package org.yuanheng.cookcc.codegen.xml;

import java.io.PrintWriter;

import org.yuanheng.cookcc.doc.GrammarDoc;
import org.yuanheng.cookcc.doc.ParserDoc;
import org.yuanheng.cookcc.doc.RhsDoc;

/**
 * @author Heng Yuan
 * @version $Id$
 */
class XmlParserOutput
{
	private void printRhs (RhsDoc rhs, PrintWriter p)
	{
		p.println ("\t\t\t<rhs>" + Utils.translate (rhs.getTerms ()) + "</rhs>");
		p.println ("\t\t\t<action>" + Utils.translate (rhs.getAction ()) + "</action>");
	}

	private void printGrammar (GrammarDoc grammar, PrintWriter p)
	{
		p.println ("\t\t<grammar term=\"" + grammar.getTerm () + "\">");
		for (RhsDoc rhs : grammar.getRhs ())
			printRhs (rhs, p);
		p.println ("\t\t</grammar>");
	}

	public void printParserDoc (ParserDoc doc, PrintWriter p)
	{
		if (doc == null)
			return;
		p.println ("\t<parser" + (doc.getStart () == null ? "" : ( " start=\"" + doc.getStart () + "\")")) + ">");

		for (GrammarDoc grammar : doc.getGrammars ())
			printGrammar (grammar, p);

		p.println ("\t</parser>");
	}
}
