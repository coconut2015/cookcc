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
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import org.yuanheng.cookcc.dfa.DFARow;
import org.yuanheng.cookcc.dfa.DFATable;
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
	private int m_caseCount;

	private boolean m_bol;
	private boolean m_backup;
	private boolean[] m_backupCases;

	private final DFATable m_dfa = new DFATable ();
	private Vector<ESet> _Dstates = new Vector<ESet> ();
	private HashMap<ESet, Integer> _DstatesSet = new HashMap<ESet, Integer> ();

	public Lexer (Document doc)
	{
		m_doc = doc;
		m_nfaFactory = doc.isUnicode () ? new NFAFactory (this, CCL.getCharacterCCL ()) : new NFAFactory (this, CCL.getByteCCL ());
	}

	public int getCaseCount ()
	{
		return m_caseCount;
	}

	int incCaseCounter ()
	{
		return m_caseCount++;
	}

	NFAFactory getNFAFactory ()
	{
		return m_nfaFactory;
	}

	/**
	 * After all the patterns have parsed, call this function to check if the lexer
	 * has to be aware of beginning of the line condition.
	 *
	 * @return	if any of the NFAs has BOL requirements
	 */
	public boolean hasBOL ()
	{
		return m_bol;
	}

	/**
	 * After all the patterns have parsed, call this function to check if there are
	 * backups in the statement (i.e. not all states are accepting states).
	 *
	 * @return	if backups are required in certain cases.
	 */
	public boolean hasBackup ()
	{
		return m_backup;
	}

	public DFATable getDFA ()
	{
		return m_dfa;
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

			ESet nfas = new ESet ();

			for (int j = 0; j < rules.length; ++j)
			{
				RuleDoc rule = rules[j];
				PatternDoc[] patterns = rule.getPatterns ();
				for (int k = 0; k < patterns.length; ++k)
				{
					PatternDoc pattern = patterns[k];
					RuleParser parser = new RuleParser (this, m_nfaFactory, pattern.isNocase ());
					NFA nfa = parser.parse (rule.getLineNumber (), pattern.getPattern ());
					if (parser.isBOL ())
						pattern.setBOL (true);
					pattern.setUserObject (nfa);
					nfas.add (nfa);
					if (pattern.isBOL ())
						m_bol = true;
				}
			}

			lexerStates[i].setUserObject (nfas);
		}
	}

	public void buildDFA ()
	{
	}

	// just unmark a list of nfa states
	private void unmarknfa (Collection<NFA> col)
	{
		for (NFA nfa : col)
			nfa.mark = false;
	}

	// e_closure computation according to Aho's Dragon book, p119.
	// comments are the algorithm in the book.
	private ESet eClosure (ESet T)
	{
		Vector<NFA> stack = new Vector<NFA> ();
		stack.clear ();

		int Mark;  // Mark is to label nfa states that popped
		int j;

		// 1. push all states in T onto stack
		// 2. initialize e_closure (T) to T;
		stack.addAll (T.getSet ());

		// 3. while stack is not empty
		NFA u, t;
		for (Mark = 0; Mark < stack.size (); Mark++)
		{
			// pop t, the top element, off of stack;
			t = stack.get (Mark);
			// for each state u with an edge from t to u labeled _e_ do
			if (t.m_char == NFA.EPSILON)
			{
				if ((u = t.m_next) != null)
				{
					// if u is not in e_closure (T)
					if (!(u.mark))
					{
						u.mark = true;
						stack.add (u);  // push u onto stack
					}
				}
				if ((u = t.m_next2) != null)
				{
					// if u is not in e_closure (T)
					if (!(u.mark))
					{
						u.mark = true;
						stack.add (u);  // push u onto stack
					}
				}
			}
		}

		ESet st = new ESet ();

		// take only the important states, other states are really useless
		// it is also the important states that make dfa states different
		//
		// for accept states, only store the first one that is the accept
		// state unless that other accept state has outgoing edges.
		NFA lastAcceptNFA = null;
		for (j = 0; j < stack.size (); j++)
		{
			t = stack.get (j);
			//t->mark = false;

			if (t.m_char == NFA.EPSILON)
			{
				//
				// case by case eliminate unimportant NFA
				//
				if (t.m_next != null)
					continue;

				if (lastAcceptNFA == null)
					lastAcceptNFA = t;
				else if (lastAcceptNFA == t)
					continue;
				else if (lastAcceptNFA.m_id < t.m_id && t.m_next2 == null)
					continue;
				else if (lastAcceptNFA.m_id > t.m_id)
				{
					if (lastAcceptNFA.m_next2 == null)
						st.getSet ().remove (lastAcceptNFA);
					lastAcceptNFA = t;
				}
			}
			st.add (t);
		}
		unmarknfa (stack);

		return st;
	}

	// move pass e_stack to e_closure
	// a few changes to make it run faster
	private ESet move (ESet T, int a)
	{
		ESet s = new ESet ();

		for (NFA n : T.getSet ())
		{
			if (n.m_char == a ||
				(n.m_char == NFA.ISCCL && n.m_ccl[a]))
			{
				if (n.m_next != null)
				{
					s.add (n.m_next);
				}
				if (n.m_next2 != null)
				{
					s.add (n.m_next2);
				}
			}
		}
		return s;
	}

	private int builddfa (DFATable dfa, Collection<NFA> nfalist, Collection<NFA> bollist)
	{
		ESet start = new ESet ();
		ESet bolstart = new ESet ();

		if (m_bol)
			bolstart.getSet ().addAll (bollist);

		start.getSet ().addAll (nfalist);
		bolstart.getSet ().addAll (nfalist);

		return builddfa (dfa, start, bolstart);
	}

	int builddfa (DFATable dfa, ESet start, ESet bolstart)
	{
		m_backupCases = new boolean[m_caseCount];

		int j, a;

		ECS ecs = m_nfaFactory.getECS ();
		int groupCount = ecs.getGroupCount ();

		DFARow row = new DFARow (new char[groupCount]);
		int Mark;

		ESet s0 = eClosure (start);
		ESet s1 = eClosure (bolstart);

		ESet U = new ESet ();

		int dfaBase = dfa.size ();
		int esetBase = _Dstates.size ();

		// initially, e_closure (s0) is the only state in _Dstates and it is unmarked
		_Dstates.add (s0);
		_DstatesSet.put (s0, new Integer (_Dstates.size () - 1));

		if (m_bol)
		{
			_Dstates.add (s1);
			_DstatesSet.put (s1, new Integer (_Dstates.size () - 1));
		}

		// while there is an unmarked state T in _Dstates
		for (Mark = esetBase; Mark < _Dstates.size (); Mark++)
		{
			// mark T;
			ESet T =_Dstates.get (Mark);

			NFA n = T.isAccept ();

			if (n != null) // set row accept conditions and values
			{
				row.setCaseValue (n.m_caseValue);
			}
			else
			{
				// this state is not an accept state
				//row.accept = 0;
				row.setCaseValue (0);

				// mark that there is a state that is non-accepting
				if (Mark > esetBase)
				{
					if (Mark > esetBase + 1 || !m_bol)
					{
						m_backup = true;

						// also mark the NFA states that lead to this non-accepting DFA state

						for (NFA nfa : T.getSet ())
						{
							NFA s = nfa.last ();
							m_backupCases[s.m_caseValue] = true;
						}
					}
				}
			}

			// for each input symbol a do
			for (j = 0; j < groupCount; j++)
			{
				a = ecs.getGroup (j);

				// U := e_closure (move (T, a));
				U = eClosure (move (T, a));

				if (U.getSet ().size () == 0)
				{
					row.setState (j, 0);
					continue;
				}


				// if U is not in _Dstates
				// add U as an unmarked state to _Dstates
				int toState;
				Integer value = _DstatesSet.get (U);
				if (value == null)
				{
					_Dstates.add (U);
					_DstatesSet.put (U, new Integer (_Dstates.size () - 1));
					toState = _Dstates.size () - 1;
					U.setStateId (toState);
				}
				else
				{
					toState = value.intValue ();
				}

				// Dtran[T, a] := U
				row.setState (j, toState);
			}

			dfa.add (row);		// add to the DFA table
		}
		return dfaBase;
	}
}
