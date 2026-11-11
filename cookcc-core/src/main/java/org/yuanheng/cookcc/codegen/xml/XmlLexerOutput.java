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

import org.yuanheng.cookcc.doc.*;

/**
 * @author Heng Yuan
 */
class XmlLexerOutput
{
	private void printShortcut (ShortcutDoc shortcut, PrintWriter p)
	{
		p.println ("\t\t<shortcut name=\"" + shortcut.getName () + "\">" + Utils.translate (shortcut.getPattern ()) + "</shortcut>");
	}

	private void printRule (RuleDoc rule, PrintWriter p)
	{
		p.println ("\t\t\t<rule>");
		for (PatternDoc pattern : rule.getPatterns ())
		{
			p.print ("\t\t\t\t<pattern");
			if (pattern.isBOL ())
				p.print (" bol=\"true\"");
			if (pattern.isNocase ())
				p.print (" nocase=\"true\"");
			p.println (">" + Utils.translate (pattern.getPattern ()) + "</pattern>");
		}
		p.println ("\t\t\t\t<action>" + Utils.translate (rule.getAction ()) + "</action>");
		p.println ("\t\t\t</rule>");
	}

	private void printLexerState (LexerStateDoc doc, PrintWriter p)
	{
		p.println ("\t\t<state name=\"" + doc.getName () + "\">");
		RuleDoc[] patterns = doc.getRules ();
		for (int i = 0; i < patterns.length; ++i)
		{
			printRule (patterns[i], p);
		}

		p.println ("\t\t</state>");
	}

	public void printLexer (LexerDoc lexer, PrintWriter p)
	{
		if (lexer == null)
			return;
		p.println ("\t<lexer>");

		ShortcutDoc[] shortcuts = lexer.getShortcuts ();
		for (int i = 0; i < shortcuts.length; ++i)
			printShortcut (shortcuts[i], p);

		LexerStateDoc[] stateDocs = lexer.getLexerStates ();
		for (int i = 0; i < stateDocs.length; ++i)
			printLexerState (stateDocs[i], p);

		p.println ("\t</lexer>");
	}
}
