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
package org.yuanheng.cookcc.lexer;

import java.text.MessageFormat;
import java.util.Set;

import org.yuanheng.cookcc.doc.*;
import org.yuanheng.cookcc.exception.ParserException;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class Lexer
{
	public static MessageFormat WARN_MSG = new MessageFormat ("Warning: {0}");
	public static MessageFormat WARN_NO_RULES = new MessageFormat ("no rules for state: {0}");

	private final Document m_doc;
	private NFAFactory m_nfaFactory;

	public Lexer (Document doc)
	{
		m_doc = doc;
		m_nfaFactory = doc.isUnicode () ? NFAFactory.getCharacterNFAFactory () : NFAFactory.getByteNFAFactory ();
	}

	public void warn (String msg)
	{
		System.out.println (WARN_MSG.format (new Object[]{ msg }));
	}

	public void parse ()
	{
		LexerDoc lexer = m_doc.getLexer ();
		if (lexer == null)
			return;
		if (lexer.getLexerState (LexerDoc.INITIAL_STATE) == null)
			throw new ParserException (0, "no initial states specified.");

		LexerStateDoc[] lexerStates = lexer.getLexerStates ();
		for (int i = 0; i < lexerStates.length; ++i)
		{
			RuleDoc[] rules = lexerStates[i].getRules ();
			if (rules.length == 0)
				warn (WARN_NO_RULES.format (new Object[]{ lexerStates[i].getName () }));

			Set<NFA> nfas = NFA.getSortedSet ();

			for (int j = 0; j < rules.length; ++j)
			{
				RuleDoc rule = rules[j];
				PatternDoc[] patterns = rule.getPatterns ();
				for (int k = 0; k < patterns.length; ++k)
				{
					PatternDoc pattern = patterns[k];
					NFA nfa = new RuleParser (m_nfaFactory, pattern.isNocase ()).parse (rule.getLineNumber (), pattern.getPattern ());
					pattern.setUserObject (nfa);
					nfas.add (nfa);
				}
			}

			lexerStates[i].setUserObject (nfas);
		}
	}
}
