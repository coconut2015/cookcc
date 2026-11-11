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
package org.yuanheng.cookcc.codegen.xml;

import java.io.PrintWriter;

import org.yuanheng.cookcc.doc.GrammarDoc;
import org.yuanheng.cookcc.doc.ParserDoc;
import org.yuanheng.cookcc.doc.RhsDoc;
import org.yuanheng.cookcc.doc.TypeDoc;

/**
 * @author Heng Yuan
 */
class XmlParserOutput
{
	private void printRhs (RhsDoc rhs, PrintWriter p)
	{
		p.println ("\t\t\t<rhs>" + Utils.translate (rhs.getTerms ()) + "</rhs>");
		String action = rhs.getAction ();
		if (action != null && action.length () > 0)
			p.println ("\t\t\t<action>" + Utils.translate (rhs.getAction ()) + "</action>");
	}

	private void printGrammar (GrammarDoc grammar, PrintWriter p)
	{
		p.println ("\t\t<grammar rule=\"" + grammar.getRule () + "\">");
		for (RhsDoc rhs : grammar.getRhs ())
			printRhs (rhs, p);
		p.println ("\t\t</grammar>");
	}

	private void printType (TypeDoc type, PrintWriter p)
	{
		p.print ("\t\t<type format=\"" + type.getFormat ().toPattern () + "\"><![CDATA[");
		for (String t : type.getSymbols ())
			p.print (" " + t);
		p.println (" ]]></type>");
	}

	public void printParserDoc (ParserDoc doc, PrintWriter p)
	{
		if (doc == null)
			return;
		p.println ("\t<parser" +
			(doc.getStart () == null ? "" : (" start=\"" + doc.getStart () + "\"")) +
			(doc.getParseError () == true ? "" : " parseerror=\"false\"") +
			(doc.getRecovery () == true ? "" : " recovery=\"false\"") +
			">");

		for (TypeDoc type : doc.getTypes ())
			printType (type, p);

		for (GrammarDoc grammar : doc.getGrammars ())
			printGrammar (grammar, p);

		p.println ("\t</parser>");
	}
}
