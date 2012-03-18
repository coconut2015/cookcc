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

import org.yuanheng.cookcc.doc.*;

/**
 * @author Heng Yuan
 * @version $Id$
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
