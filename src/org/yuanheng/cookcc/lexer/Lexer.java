/*
 * Copyright (c) 2008-2013, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *    Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    Neither the name of the Heng Yuan nor the
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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.yuanheng.cookcc.Main;
import org.yuanheng.cookcc.dfa.DFARow;
import org.yuanheng.cookcc.dfa.DFATable;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.doc.LexerDoc;
import org.yuanheng.cookcc.doc.LexerStateDoc;
import org.yuanheng.cookcc.doc.PatternDoc;
import org.yuanheng.cookcc.doc.RuleDoc;
import org.yuanheng.cookcc.exception.NoInitialStateException;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class Lexer
{
	public static MessageFormat WARN_MSG = new MessageFormat ("Warning: {0}");
	public static MessageFormat WARN_NO_RULES = new MessageFormat ("no rules for state: {0}");
	public static String WARN_INCOMPLETE_STATE = "Following states have do not have patterns that cover all character sets:";
	public static String WARN_CANNOT_MATCH = "Following patterns can never be matched:";
	public static String WARN_BACKUP = "Following patterns require backup:";

	private final static String PROP_LEXER = "Lexer";
	private final static String PROP_NFA = "NFA";

	private final static String PROP_START_SET = "START_SET";
	private final static String PROP_BOL_SET = "BOL_SET";

	public static Lexer getLexer (Document doc) throws IOException
	{
		if (doc == null)
			return null;
		LexerDoc lexerDoc = doc.getLexer ();
		if (lexerDoc == null)
			return null;
		Object obj = lexerDoc.getProperty (PROP_LEXER);
		Lexer lexer;
		if (obj == null || !(obj instanceof Lexer))
		{
			lexer = new Lexer (doc);
			lexer.parse ();
			lexerDoc.setProperty (PROP_LEXER, lexer);

			Set<LexerStateDoc> incompleteStates = lexer.getIncompleteStates ();
			if (incompleteStates != null)
			{
				Main.warn (WARN_INCOMPLETE_STATE);
				Set<String> names = new TreeSet<String> ();
				for (LexerStateDoc lexerState : incompleteStates)
					names.add (lexerState.getName ());
				Main.warn ("\t" + names);
			}
			Map<LexerStateDoc, Collection<PatternDoc>> unusedPatterns = lexer.getUnusedPatterns ();
			if (unusedPatterns != null)
			{
				Main.warn (WARN_CANNOT_MATCH);
				for (LexerStateDoc lexerStateDoc : unusedPatterns.keySet ())
				{
					for (PatternDoc patternDoc : unusedPatterns.get (lexerStateDoc))
						Main.warn ("\t<" + lexerStateDoc.getName () + ">" + patternDoc.getPattern ());
				}
			}
			if (lexer.hasBackup () && lexerDoc.isWarnBackup ())
			{
				Main.warn (WARN_BACKUP);
				Map<LexerStateDoc, Collection<PatternDoc>> backupPatterns = lexer.getBackupPatterns ();
				for (LexerStateDoc lexerStateDoc : backupPatterns.keySet ())
				{
					for (PatternDoc patternDoc : backupPatterns.get (lexerStateDoc))
						Main.warn ("\t<" + lexerStateDoc.getName () + ">" + patternDoc.getPattern ());
				}
			}
		}
		else
			lexer = (Lexer)obj;
		return lexer;
	}

	private final Document m_doc;
	private NFAFactory m_nfaFactory;
	private int m_caseCount;

	private boolean m_bolStates;
	private boolean m_backup;
	private boolean[] m_backupCases;

	private final DFATable m_dfa = new DFATable ();
	private Vector<ESet> m_dfaStates = new Vector<ESet> ();
	private LexerStateDoc[] m_lexerStates;
	private int[] m_beginLocations;

	private Map<LexerStateDoc, Collection<PatternDoc>> m_backupPatterns;
	private Map<LexerStateDoc, Collection<PatternDoc>> m_unusedPatterns;
	private Set<LexerStateDoc> m_incompleteStates;

	private final RuleDoc m_defaultRule;

	private final PatternParser m_patternParser;

	private Lexer (Document doc)
	{
		m_doc = doc;
		m_nfaFactory = doc.isUnicode () ? new NFAFactory (CCL.getCharacterCCL ()) : new NFAFactory (CCL.getByteCCL ());
		m_patternParser = new PatternParser (this, getCCL ());

		m_defaultRule = RuleDoc.createInternalRule (doc.getLexer ());
	}

	public Document getDocument ()
	{
		return m_doc;
	}

	public int getCaseCount ()
	{
		return m_caseCount;
	}

	int incCaseCounter ()
	{
		return ++m_caseCount;
	}

	public ECS getECS ()
	{
		return m_nfaFactory.getECS ();
	}

	public CCL getCCL ()
	{
		return m_nfaFactory.getCCL ();
	}

	NFAFactory getNFAFactory ()
	{
		return m_nfaFactory;
	}

	/**
	 * After all the patterns have parsed, call this function to check if the lexer
	 * has to be aware of beginning of the line condition.
	 *
	 * @return if any of the NFAs has BOL requirements
	 */
	public boolean hasBolStates ()
	{
		return m_bolStates;
	}

	/**
	 * After all the patterns have parsed, call this function to check if there are
	 * backups in the statement (i.e. not all states are accepting states).
	 *
	 * @return if backups are required in certain cases.
	 */
	public boolean hasBackup ()
	{
		return m_backup;
	}

	public DFATable getDFA ()
	{
		return m_dfa;
	}

	public LexerStateDoc[] getLexerStates ()
	{
		return m_lexerStates;
	}

	public int[] getBeginLocations ()
	{
		return m_beginLocations;
	}

	public void warn (String msg)
	{
		System.out.println (WARN_MSG.format (new Object[]{msg}));
	}

	public void parse () throws IOException
	{
		LexerDoc lexer = m_doc.getLexer ();
		if (lexer == null)
			return;
		if (lexer.getLexerState (LexerDoc.INITIAL_STATE) == null)
			throw new NoInitialStateException ();

		LexerStateDoc[] lexerStates = lexer.getLexerStates ();
		for (LexerStateDoc lexerState : lexerStates)
		{
			lexerState.addRule (m_defaultRule);

			RuleDoc[] rules = lexerState.getRules ();
			if (rules.length == 0)
				warn (WARN_NO_RULES.format (new Object[]{lexerState.getName ()}));

			ESet startSet = new ESet ();
			ESet bolSet = new ESet ();

			for (RuleDoc rule : rules)
			{
				for (PatternDoc pattern : rule.getPatterns ())
				{
					NFA nfa;
					if (pattern.getCaseValue () < 0)
					{
						int caseValue = incCaseCounter ();
						pattern.setCaseValue (caseValue);

						LexerPattern lp = m_patternParser.parse (pattern.getPrecedence (), pattern.getLineNumber (), pattern.getPattern ());
						nfa = lp.constructNFA (m_nfaFactory, caseValue, pattern.getLineNumber ());
						if (lp.isBol ())
							pattern.setBOL (true);
						if (nfa.trailContext != 0)
							pattern.setTrailContext (nfa.trailContext);
						pattern.setProperty (PROP_NFA, nfa);
					}
					else
						nfa = (NFA)pattern.getProperty (PROP_NFA);
					if (pattern.isBOL ())
					{
						m_bolStates = true;
						bolSet.add (nfa);
					}
					else
					{
						// NFA that do not have BOL status can start in normal
						// or BOL cases.
						bolSet.add (nfa);
						startSet.add (nfa);
					}
//					System.out.println ("case " + pattern.getCaseValue () + ": " + pattern.getPattern ());
//					System.out.println ("NFA:");
//					System.out.println (nfa);
				}
			}
			lexerState.setProperty (PROP_START_SET, startSet);
			lexerState.setProperty (PROP_BOL_SET, bolSet);
		}

		// swap INITIAL state to the front if possible
		LexerStateDoc lexerState = lexer.getLexerState (LexerDoc.INITIAL_STATE);
		for (int i = 0; i < lexerStates.length; ++i)
		{
			if (lexerStates[i] == lexerState)
			{
				LexerStateDoc tmp = lexerStates[0];
				lexerStates[0] = lexerState;
				lexerStates[i] = tmp;
				break;
			}
		}

		m_lexerStates = lexerStates;
		m_beginLocations = new int[lexerStates.length];

		m_backupCases = new boolean[m_caseCount + 1];

		for (int i = 0; i < lexerStates.length; ++i)
		{
			lexerState = lexerStates[i];
			ESet startSet = (ESet)lexerState.getProperty (PROP_START_SET);
			ESet bolSet = (ESet)lexerState.getProperty (PROP_BOL_SET);

			m_beginLocations[i] = m_dfaStates.size ();

			buildDFA (startSet, bolSet);

			// check shadowed patterns for each state
			// we do it here because some rules may be used in multiple states
			int[] accepts = m_dfa.getAccepts ();
			for (RuleDoc rule : lexerStates[i].getRules ())
			{
				for (PatternDoc pattern : rule.getPatterns ())
				{
					// check if the pattern is shadowed
					int caseValue = pattern.getCaseValue ();
					int a;
					for (a = m_beginLocations[i]; a < accepts.length; ++a)
						if (accepts[a] == caseValue)
							break;
					if (pattern.isInternal ())
					{
						if (a < accepts.length)
						{
							if (m_incompleteStates == null)
								m_incompleteStates = new HashSet<LexerStateDoc> ();
							m_incompleteStates.add (lexerStates[i]);
						}
					}
					else if (a >= accepts.length)
					{
						if (m_unusedPatterns == null)
							m_unusedPatterns = new HashMap<LexerStateDoc, Collection<PatternDoc>> ();
						Collection<PatternDoc> list = m_unusedPatterns.get (lexerStates[i]);
						if (list == null)
						{
							list = new LinkedList<PatternDoc> ();
							m_unusedPatterns.put (lexerStates[i], list);
						}
						list.add (pattern);
					}
				}
			}
		}

		if (m_backup)
		{
			m_backupPatterns = new HashMap<LexerStateDoc, Collection<PatternDoc>> ();
			for (int i = 0; i < lexerStates.length; ++i)
			{
				for (RuleDoc rule : lexerStates[i].getRules ())
				{
					for (PatternDoc pattern : rule.getPatterns ())
					{
						if (m_backupCases[pattern.getCaseValue ()])
						{
							Collection<PatternDoc> list = m_backupPatterns.get (lexerStates[i]);
							if (list == null)
							{
								list = new LinkedList<PatternDoc> ();
								m_backupPatterns.put (lexerStates[i], list);
							}
							list.add (pattern);
						}
					}
				}
			}
		}
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
			if (t.thisChar == NFA.EPSILON)
			{
				if ((u = t.next) != null)
				{
					// if u is not in e_closure (T)
					if (!(u.mark))
					{
						u.mark = true;
						stack.add (u);  // push u onto stack
					}
				}
				if ((u = t.next2) != null)
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

			if (t.thisChar == NFA.EPSILON)
			{
				//
				// case by case eliminate unimportant NFA
				//
				if (t.next != null)
					continue;

				if (lastAcceptNFA == null)
					lastAcceptNFA = t;
				else if (lastAcceptNFA == t)
					continue;
				else if (lastAcceptNFA.precedence < t.precedence && t.next2 == null)
					continue;
				else if (lastAcceptNFA.precedence > t.precedence)
				{
					if (lastAcceptNFA.next2 == null)
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
			if (n.thisChar == a ||
				(n.thisChar == NFA.ISCCL && n.charSet[a]))
			{
				if (n.next != null)
				{
					s.add (n.next);
				}
				if (n.next2 != null)
				{
					s.add (n.next2);
				}
			}
		}
		return s;
	}

	private int buildDFA (ESet startSet, ESet bolSet)
	{
		int j, a;

		TreeMap<ESet, Integer> statesSet = new TreeMap<ESet, Integer> ();

		ECS ecs = getECS ();
		int groupCount = ecs.getGroupCount ();
		int[] lookup = ecs.getLookup ();

		int Mark;

		ESet s0 = eClosure (startSet);
		ESet s1 = eClosure (bolSet);

		int dfaBase = m_dfa.size ();
		int esetBase = m_dfaStates.size ();

		// initially, e_closure (s0) is the only state in _Dstates and it is unmarked
		m_dfaStates.add (s0);

		if (m_bolStates)
		{
			m_dfaStates.add (s1);
		}

		// while there is an unmarked state T in _Dstates
		for (Mark = esetBase; Mark < m_dfaStates.size (); Mark++)
		{
			// mark T;
			ESet T = m_dfaStates.get (Mark);

			NFA n = T.isAccept ();

			DFARow row = new DFARow (groupCount);
			if (n != null) // set row accept conditions and values
			{
				row.setCaseValue (n.caseValue);
			}
			else
			{
				// this state is not an accept state
				//row.accept = 0;
				row.setCaseValue (0);

				// mark that there is a state that is non-accepting
				if (Mark > esetBase)
				{
					if (Mark > esetBase + 1 || !m_bolStates)
					{
						m_backup = true;

						// also mark the NFA states that lead to this non-accepting DFA state

						for (NFA nfa : T.getSet ())
						{
							NFA s = nfa.last ();
							m_backupCases[s.caseValue] = true;
						}
					}
				}
			}

			// for each input symbol a do
			for (j = 0; j < groupCount; ++j)
			{
				a = lookup[j];

				// U := e_closure (move (T, a));
				ESet U = eClosure (move (T, a));

				if (U.getSet ().size () == 0)
				{
					row.setState (j, 0);
					continue;
				}

				// if U is not in _Dstates
				// add U as an unmarked state to _Dstates
				int toState;
				Integer value = statesSet.get (U);
				if (value == null)
				{
					m_dfaStates.add (U);
					statesSet.put (U, new Integer (m_dfaStates.size () - 1));
					toState = m_dfaStates.size () - 1;
					U.setStateId (toState);
				}
				else
				{
					toState = value.intValue ();
				}

				// Dtran[T, a] := U
				row.setState (j, toState);
			}

			m_dfa.add (row);        // add to the DFA table
		}
		return dfaBase;
	}

	public Map<LexerStateDoc, Collection<PatternDoc>> getUnusedPatterns ()
	{
		return m_unusedPatterns;
	}

	public Map<LexerStateDoc, Collection<PatternDoc>> getBackupPatterns ()
	{
		return m_backupPatterns;
	}

	public Set<LexerStateDoc> getIncompleteStates ()
	{
		return m_incompleteStates;
	}
}
