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
package org.yuanheng.cookcc.parser;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

import org.yuanheng.cookcc.Main;
import org.yuanheng.cookcc.OptionMap;
import org.yuanheng.cookcc.dfa.DFARow;
import org.yuanheng.cookcc.dfa.DFATable;
import org.yuanheng.cookcc.doc.*;
import org.yuanheng.cookcc.exception.ParserException;
import org.yuanheng.cookcc.parser.ast.*;
import org.yuanheng.cookcc.util.TerminalUtils;

/**
 * @author Heng Yuan
 */
public class Parser implements SymbolLibrary
{
	public static String WARN_UNUSED_TOKEN = "Following terminals are specified but not used:";

	private final static String PROP_PARSER = "Parser";
	private final static String PROP_PRODUCTION = "Production";

	public static String START = "@start";

	public static int FINISH = 0;
	public static int ERROR = 1;
	public static int UNHANDLED = 2;
	public static int IGNORE = 3;
	public static int CAPTURE = 4;

	//	private static Token s_epsilon = new Token ("{e}", 0, EPSILON, Token.NONASSOC);
	private static Token s_finish = new Token ("$", 0, FINISH, Token.NONASSOC);
	private static Token s_error = new Token ("error", 0, ERROR, Token.NONASSOC);
	private static Token s_unhandled = new Token ("@unhandled", 0, UNHANDLED, Token.NONASSOC);
	private static Token s_ignore = new Token ("@ignore", 0, IGNORE, Token.NONASSOC);
	private static Token s_capture = new Token ("@capture", 0, CAPTURE, Token.NONASSOC);

	public static Parser getParser (Document doc, OptionMap options) throws IOException
	{
		if (doc == null)
			return null;
		ParserDoc parserDoc = doc.getParser ();
		if (parserDoc == null)
			return null;
		Object obj = parserDoc.getProperty (PROP_PARSER);
		Parser parser;
		if (obj == null || !(obj instanceof Parser))
		{
			parser = new Parser (doc, options);
			parser.parse ();
			parserDoc.setProperty (PROP_PARSER, parser);
		}
		else
			parser = (Parser)obj;

		return parser;
	}

	int m_maxTerminal;

	private short m_productionIdCounter = 1;
	private final Document m_doc;
	private final OptionMap m_options;
	private int m_terminalCount;
	private int m_nonTerminalCount;
	private int m_usedTerminalCount;
	private int m_usedSymbolCount;
	/** symbols actually being used.  This is the ecs of the input */
	private int[] m_usedSymbols;
	/** look up the index of a terminal in m_usedSymbols */
	private int[] m_symbolGroups;

	private int[] m_ignoreList;
	private int[] m_captureList;

	private final Map<String, Token> m_terminals = new HashMap<String, Token> ();
	private final Map<Integer, Token> m_terminalMap = new HashMap<Integer, Token> ();
	private final Map<String, Integer> m_nonTerminals = new HashMap<String, Integer> ();
	private final ArrayList<Production> m_productions = new ArrayList<Production> ();
	private final Map<Integer, String> m_symbolMap = new TreeMap<Integer, String> ();
	// for a given symbol, its productions
	private final Map<Integer, Production[]> m_productionMap = new HashMap<Integer, Production[]> ();

	private final HashMap<Integer, int[]> m_firstSet = new HashMap<Integer, int[]> ();
	private final HashMap<Integer, TokenSet> m_firstSetVal = new HashMap<Integer, TokenSet> ();

	private final DFATable m_dfa = new DFATable ();
	private final ArrayList<short[]> m_goto = new ArrayList<short[]> ();

	private final ArrayList<Token> m_tokens = new ArrayList<Token> ();

	private final HashMap<Integer, MessageFormat> m_formats = new HashMap<Integer, MessageFormat> ();

	final ArrayList<ItemSet> _DFAStates = new ArrayList<ItemSet> ();
	final Map<ItemSet, Short> _DFASet = new TreeMap<ItemSet, Short> ();
	private final Set<Integer> m_mentionedSet = new HashSet<Integer> ();

	private int m_reduceConflict;
	private int m_shiftConflict;

	private PrintStream m_out;

	private int m_internalGrammarCount;

	// SymbolLibrary implementation
	private final Map<String, Symbol> m_symbols = new HashMap<String, Symbol> ();
	private final Map<Character, CharSymbol> m_charSymbols = new HashMap<Character, CharSymbol> ();
	private final ProductionCounter m_productionCounter = new ProductionCounter ();

	private Parser (Document doc, OptionMap options)
	{
		m_doc = doc;
		m_options = options;
	}

	private void verbose (String msg)
	{
		if (m_out == null)
			return;
		m_out.println (msg);
	}

	private void verboseSection (String section)
	{
		if (m_out == null)
			return;
		m_out.println ();
		m_out.println ("----------- " + section + " ----------");
	}

	public void parse () throws IOException
	{
		File analysisFile = Main.getParserAnalysisFile (m_options);
		if (analysisFile != null)
			m_out = new PrintStream (new FileOutputStream (analysisFile));

		m_terminals.put (s_finish.name, s_finish);
		m_terminals.put (s_error.name, s_error);
		m_terminals.put (s_unhandled.name, s_unhandled);

		m_symbolMap.put (s_finish.value, s_finish.name);
		m_symbolMap.put (s_error.value, s_error.name);
		m_symbolMap.put (s_unhandled.value, s_unhandled.name);

		m_maxTerminal = TerminalUtils.parseTerminals (m_terminals,
													  m_terminalMap,
													  m_symbolMap,
													  m_tokens,
													  m_doc.getTokens ());
		getIgnoreList ();
		getCaptureList ();

		// add start condition
		Production startProduction = new Production (getNonterminal (START), m_productionIdCounter++);
		m_productions.add (startProduction);
		m_productionMap.put (m_nonTerminals.get (START), new Production[]{startProduction});

//		parseProductions ();
		generateProductions (parseGrammars ());

		// make sure all the non-terminals have its production
		for (int i = 0; i < m_nonTerminalCount; ++i)
		{
			if (m_productionMap.get (i + m_maxTerminal + 1) == null)
				throw new ParserException (0, "Missing production for non-terminal " + m_symbolMap.get (i + m_maxTerminal + 1));
		}


		ParserDoc parserDoc = m_doc.getParser ();
		Integer startNonTerminal = parserDoc.getStart () == null ? (m_productions.size () > 1 ? m_productions.get (1).getSymbol () : null) : m_nonTerminals.get (parserDoc.getStart ());
		if (startNonTerminal == null)
			throw new ParserException (0, "Unable to find the start symbol for the parser.");
		startProduction.setProduction (new int[]{startNonTerminal});

		// now we need to add the internal symbols

		// the computed used tokens can be smaller than the symbol map
		// if there are terminals that are declared but not used.
		//
		// other times, such as in case of Unary minus, a token is used
		// merely to specify the precedence
		m_terminalCount = computeUsedSymbols ();

		m_usedSymbolCount = m_usedSymbols.length;
		m_usedTerminalCount = m_usedSymbols.length - m_nonTerminalCount;

		verboseSection ("used symbols");
		for (int i = 0; i < m_usedSymbols.length; ++i)
			verbose (i + "\t:\t" + m_usedSymbols[i] + "\t:\t" + m_symbolMap.get (m_usedSymbols[i]));

		verboseSection ("statistics");
		verbose ("max terminal = " + m_maxTerminal);
		verbose ("non terminal count = " + m_nonTerminalCount);
		verbose ("terminal count = " + m_terminalCount);
		verbose ("used terminal count = " + m_usedTerminalCount);
		verbose ("used symbol count = " + m_usedSymbolCount);
//		verbose ("symbol map = " + m_symbolMap);

		verboseSection ("productions");
		for (Production p : m_productions)
			verbose (toString (p));

		computeFirstSet ();

		new LALR (this).build ();

		reduce ();

		if (m_out != null)
		{
			m_out.close ();
			m_out = null;
		}
	}

	private boolean isIgnored (int terminal)
	{
		if (m_ignoreList == null)
			return false;
		return Arrays.binarySearch (m_ignoreList, terminal) >= 0;
	}

	private boolean isCaptured (int terminal)
	{
		if (m_captureList == null)
			return false;
		return Arrays.binarySearch (m_captureList, terminal) >= 0;
	}

	public String getNewInternalNonTerminal ()
	{
		ParserDoc parserDoc = m_doc.getParser ();
		for (;;)
		{
			String term = "@" + (++m_internalGrammarCount);
			if (!parserDoc.hasGrammar (term))
				return term;
		}
	}

	private Token getPrecedence (String name, long lineNumber)
	{
		name = name.trim ();
		if (name.length () > 0)
		{
			int[] value = new int[1];
			checkTerminalName (lineNumber, name, value);
			Token tok;
			if (value[0] == 0)
				tok = m_terminals.get (name);
			else
				tok = m_terminalMap.get (value[0]);
			if (tok == null)
				throw new ParserException (lineNumber, "Invalid terminal '" + name + "' specified for %prec.");
			if (tok.getValue () > TerminalUtils.INIT_MAX_TERMINALS)
				m_mentionedSet.add (tok.getValue ());
			return tok;
		}
		return null;
	}

	private boolean hasInternalSymbols (ArrayList<Symbol> symbols)
	{
		for (Symbol s : symbols)
		{
			if (s.isInternal ())
				return true;
		}
		return false;
	}

	private ArrayList<SingleRule> parseGrammars () throws IOException
	{
		ParserDoc parserDoc = m_doc.getParser ();

		ArrayList<SingleRule> rules = new ArrayList<SingleRule> ();
		ArrayList<SingleRule> internalRules = new ArrayList<SingleRule> ();
		ProductionCounter productionCounter = m_productionCounter;
		productionCounter.newId ();	// skip the start

		ProductionParser productionParser = new ProductionParser ();
		int initialGrammarCount = parserDoc.getGrammarCount ();
		for (int grammarId = 0; grammarId < initialGrammarCount; ++grammarId)
		{
			GrammarDoc grammar = parserDoc.getGrammar (grammarId);
			Symbol lhs = getSymbol (grammar.getRule ());
			grammar.internalSetSymbol (lhs.getValue (this, 0));
			for (RhsDoc rhsDoc : grammar.getRhs ())
			{
				String rhs = rhsDoc.getTerms ();
//				System.out.println ("rhs = " + rhs);
				ArrayList<Symbol> symbols = productionParser.parse (this, rhsDoc.getLineNumber (), rhs);
				long lineNumber = rhsDoc.getLineNumber ();
				String precedence = rhsDoc.getPrecedence ();
				SingleRule singleRule = new SingleRule (lhs, symbols.toArray (new Symbol[symbols.size ()]), precedence, lineNumber, productionCounter, rhsDoc);
				rules.add (singleRule);
				if (hasInternalSymbols (symbols))
				{
					rhsDoc.internalSetTranslatedTerms (singleRule.getTerms ());
				}

				singleRule.addNewRules (internalRules, parserDoc, productionCounter);
				rhsDoc.setCaseValue (singleRule.caseValue);
			}
		}

		// recursively check if there are new rules to deal with
		for (int i = 0; i < internalRules.size (); ++i)
		{
			SingleRule singleRule = internalRules.get (i);
			singleRule.addNewRules (internalRules, parserDoc, productionCounter);
		}

		// now combine all the rules
		rules.addAll (internalRules);

		return rules;
	}

	/**
	 * Generate production from a SingleRule
	 *
	 * @param	singleRule
	 * 			a single parsed SingleRule
	 * @return	corresponding production in integer values.
	 */
	private Production generateProduction (SingleRule singleRule)
	{
		long lineNumber = singleRule.lineNumber;
		int lhs = singleRule.lhs.getValue (this, lineNumber);
		Production production = new Production (lhs, singleRule.caseValue);
		singleRule.getRhsDoc ().setProperty (PROP_PRODUCTION, production);
		int[] prod = new int[singleRule.rhs.length];
		int i = 0;
		for (Symbol s : singleRule.rhs)
		{
			int v = s.getValue (this, lineNumber);
			prod[i++] = v;
			if (v <= m_maxTerminal)
			{
				production.setPrecedence (m_terminalMap.get (v));
				if (m_symbolMap.get (v) == null)
				{
					m_symbolMap.put (v, s.getName ());
				}
			}
		}
		production.setProduction (prod);

		if (singleRule.precedence != null)
		{
			Token tok = getPrecedence (singleRule.precedence, singleRule.lineNumber);
			if (tok != null)
			{
				production.setPrecedence (tok);
			}
		}

		return production;
	}

	/**
	 * Generate all the productions.
	 * It assumes that all the SingleRule are added in a way LHS are
	 * grouped together.
	 *
	 * @param	rules
	 * 			the parsed SingleRule rules
	 */
	private void generateProductions (ArrayList<SingleRule> rules)
	{
		ArrayList<Production> prods = new ArrayList<Production> ();
		int prevLhs = -1;

		for (SingleRule singleRule : rules)
		{
			Production production = generateProduction (singleRule);
			m_productions.add (production);

			int lhs = production.getSymbol ();

			// are we handling a new symbol?
			if (lhs != prevLhs &&
				prevLhs != -1)	// -1 is the initial case
			{
				m_productionMap.put (prevLhs, prods.toArray (new Production[prods.size ()]));
				prods.clear ();
			}

			prods.add (production);
			prevLhs = lhs;
		}

		if (prevLhs != -1)
		{
			m_productionMap.put (prevLhs, prods.toArray (new Production[prods.size ()]));
		}

		/*
		System.out.println ("------ production map ------------");
		for (Map.Entry<Integer, Production[]> entry : m_productionMap.entrySet ())
		{
			int lhs = entry.getKey ();
			if (lhs == getNonterminal (START))
				continue;
			Production[] productions = entry.getValue ();
			System.out.println ("-- " + m_symbolMap.get (lhs));
			for (Production p : productions)
			{
				System.out.println (toString (p));
			}
		}
		System.out.println ("------ end production map ------------");
		*/
	}

/*
	private void parseProductions ()
	{
		int[] pos = new int[1];
		char[] type = new char[1];
		ParserDoc parserDoc = m_doc.getParser ();
		// because iterator disallow modifying the list (even from the same thread)
		// have to use the index approach.
		for (int grammarId = 0; grammarId < parserDoc.getGrammarCount (); ++grammarId)
		{
			GrammarDoc grammar = parserDoc.getGrammar (grammarId);
			int lhs = getNonterminal (grammar.getRule ());
			ArrayList<Production> prods = new ArrayList<Production> ();
			for (RhsDoc rhs : grammar.getRhs ())
			{
				Production production = new Production (lhs, m_productionIdCounter++);
				ArrayList<Integer> symbolList = new ArrayList<Integer> ();
				String terms = rhs.getTerms ().trim ();
				long lineNumber = rhs.getLineNumber ();
				while (terms.length () > 0)
				{
					pos[0] = 0;
					int sym = parseTerm (lineNumber, terms, pos, type);
					if (isIgnored (sym))
						throw new ParserException (lineNumber, "An ignored terminal is used in the grammar");
					if (type[0] == '\0')
					{
						if (sym <= m_maxTerminal)
						{
							production.setPrecedence (m_terminalMap.get (sym));
						}
					}
					else
					{
						// we need a new symbol to handle the new grammar.
						String tmpSymbol = getNewInternalNonTerminal ();
						GrammarDoc tmpGrammar = parserDoc.getGrammar (tmpSymbol);
						tmpGrammar.setType (type[0]);

						if (type[0] == '?')
						{
							RhsDoc emptyRhs = new RhsDoc ();
							RhsDoc optRhs = new RhsDoc ();
							optRhs.setTerms (m_symbolMap.get (sym));
							tmpGrammar.addRhs (emptyRhs);
							tmpGrammar.addRhs (optRhs);
						}
						else if (type[0] == '*')
						{
							RhsDoc initRhs = new RhsDoc ();
							RhsDoc listRhs = new RhsDoc ();
							listRhs.setTerms (tmpSymbol + " " + m_symbolMap.get (sym));
							tmpGrammar.addRhs (initRhs);
							tmpGrammar.addRhs (listRhs);
						}
						else if (type[0] == '+')
						{
							RhsDoc initRhs = new RhsDoc ();
							initRhs.setTerms (m_symbolMap.get (sym));
							RhsDoc listRhs = new RhsDoc ();
							listRhs.setTerms (tmpSymbol + " " + m_symbolMap.get (sym));
							tmpGrammar.addRhs (initRhs);
							tmpGrammar.addRhs (listRhs);
						}
						sym = getNonterminal (tmpSymbol);
					}
					terms = terms.substring (pos[0]).trim ();
					symbolList.add (sym);
				}
				int[] prod = new int[symbolList.size ()];
				int i = 0;
				for (Integer s : symbolList)
					prod[i++] = s.intValue ();
				production.setProduction (prod);

				if (rhs.getPrecedence () != null)
				{
					String name = rhs.getPrecedence ().trim ();
					if (name.length () > 0)
					{
						int[] value = new int[1];
						checkTerminalName (lineNumber, name, value);
						Token tok;
						if (value[0] == 0)
							tok = m_terminals.get (name);
						else
							tok = m_terminalMap.get (value[0]);
						if (tok == null)
							throw new ParserException (lineNumber, "Invalid terminal '" + name + "' specified for %prec.");
						if (tok.getValue () > TerminalUtils.INIT_MAX_TERMINALS)
							m_mentionedSet.add (tok.getValue ());
						production.setPrecedence (tok);
					}
				}

				prods.add (production);
				rhs.setCaseValue (production.getId ());
				rhs.setProperty (PROP_PRODUCTION, production);
				m_productions.add (production);
			}
			m_productionMap.put (lhs, prods.toArray (new Production[prods.size ()]));
		}
	}

	private int parseTerm (long lineNumber, String terms, int[] pos, char[] type)
	{
		if (terms.startsWith ("'\\''"))
		{
			pos[0] = "'\\''".length ();
			if (terms.length () > pos[0] &&
				"+?*".indexOf (terms.charAt (pos[0])) >= 0)
			{
				type[0] = terms.charAt (pos[0]);
				++pos[0];
			}
			else
				pos[0] = '\0';
			return getSymbolValue ("'\\''", lineNumber);
		}
		if (terms.charAt (0) == '\'')
		{
			int index = terms.indexOf ('\'', 1);
			if (index > 1)
			{
				String name = terms.substring (0, index + 1);
				int symbol = getSymbolValue (name, lineNumber);
				pos[0] = index + 1;
				if (terms.length () > pos[0] &&
					"+?*".indexOf (terms.charAt (pos[0])) >= 0)
				{
					type[0] = terms.charAt (pos[0]);
					++pos[0];
				}
				else
					type[0] = '\0';
				return symbol;
			}
		}
		else
		{
			String name = terms;
			int index = name.indexOf (' ');
			if (index > 0)
				name = name.substring (0, index);
			index = terms.indexOf ('\t');
			if (index > 0)
				name = name.substring (0, index);
			index = terms.indexOf ('\r');
			if (index > 0)
				name = name.substring (0, index);
			index = terms.indexOf ('\n');
			if (index > 0)
				name = name.substring (0, index);
			index = name.length ();
			if (name.length () > 0 &&
				("+?*".indexOf (name.charAt (index - 1)) >= 0))
			{
				type[0] = name.charAt (index - 1);
				name = name.substring (0, index - 1);
			}
			else
				type[0] = '\0';
			int symbol = getSymbolValue (name, lineNumber);
			pos[0] = index;
			return symbol;
		}
		throw new ParserException (lineNumber, "Invalid symbol: " + terms);
	}
*/

	private String checkTerminalName (long lineNumber, String name, int[] value)
	{
		return TerminalUtils.checkTerminalName (lineNumber, name, value, false, m_symbolMap);
	}

	private int getNonterminal (String name)
	{
		Integer value = m_nonTerminals.get (name);
		if (value == null)
		{
			value = new Integer (m_maxTerminal + (++m_nonTerminalCount));
			m_nonTerminals.put (name, value);
			m_symbolMap.put (value, name);
		}
		return value.intValue ();
	}

	@Override
	public int getSymbolValue (String name, long lineNumber)
	{
		if (name.startsWith ("@") &&
			name.length () > 1 &&
			Character.isDigit (name.charAt (1)))
		{
			Integer value = m_nonTerminals.get (name);
			if (value != null)
				return value;
			return getNonterminal (name);
		}
		int[] checkValue = new int[1];
		name = checkTerminalName (lineNumber, name, checkValue);
		if (checkValue[0] != 0)
			return checkValue[0];
		Token token = m_terminals.get (name);
		if (token != null)
			return token.value;
		return getNonterminal (name);
	}

	//
	// given a production alpha, return FIRST(alpha)
	//
	void computeFirst (int[] production, int begin, int end, TokenSet first)
	{
		// indicate first contain epsilon
		boolean epsilon = true;

		for (; begin < end; ++begin)
		{
			int sym = production[begin];

			if (sym <= m_maxTerminal)
			{
				first.addSymbol (sym);
				epsilon = false;
				break;
			}

			TokenSet val = m_firstSetVal.get (sym);

			first.or (val);

			if (val.hasEpsilon () == false)
			{
				epsilon = false;
				break;
			}
		}

		first.setEpsilon (epsilon);
	}

	//
	// This is a very expensive operation.  Need to find a way to optimize it
	// later.
	//
	private void computeFirstSet ()
	{
		for (int i = 0; i < m_nonTerminalCount; ++i)
			m_firstSetVal.put (m_maxTerminal + 1 + i, createTokenSet ());

		boolean changed;
		do
		{
			changed = false;
			for (Production production : m_productions)
			{
				int A = production.getSymbol ();

				TokenSet current = m_firstSetVal.get (A);
				TokenSet old = current.clone ();

				for (int sym : production.getProduction ())
				{
					if (sym <= m_maxTerminal)
					{
						// for a terminal, first (X) is {X}
						current.addSymbol (sym);
						break;
					}

					// for a non-terminal, do or operation
					TokenSet val = m_firstSetVal.get (sym);
					current.or (val);

					if (!val.hasEpsilon ())
						break;
				}
				if (production.size () == 0)
					current.setEpsilon (true);

				// determine if anything got changed
				if (old.compareTo (current) != 0)
					changed = true;
			}
		}
		while (changed);

		for (int i = 0; i < m_nonTerminalCount; ++i)
		{
			int sym = m_maxTerminal + 1 + i;
			TokenSet val = m_firstSetVal.get (sym);
			int[] vec = new int[m_usedTerminalCount];
			int count = 0;

			for (int k = 0; k < m_usedTerminalCount; ++k)
				if (val.hasSymbol (k))
					vec[count++] = k;
			int[] newVec = new int[count];
			System.arraycopy (vec, 0, newVec, 0, count);
			m_firstSet.put (sym, newVec);
		}

		if (m_out != null)
		{
			verboseSection ("First Sets");
			for (int i = 0; i < m_nonTerminalCount; ++i)
			{
//				m_out.print ("FIRST(" + m_symbolMap.get (m_usedSymbols[m_usedTerminalCount + i]) + ") = {");
//				for (int sym : m_firstSet.get (m_maxTerminal + 1 + i))
//					m_out.print (" " + m_symbolMap.get (m_usedSymbols[sym]));
//				m_out.println (" }");
				m_out.println ("FIRST(" + m_symbolMap.get (m_usedSymbols[m_usedTerminalCount + i]) + ") = " + toString (m_firstSetVal.get (m_maxTerminal + 1 + i)) + (m_firstSetVal.get (m_maxTerminal + 1 + i).hasEpsilon () ? ", epsilon" : ""));
			}
		}
	}

	private int computeUsedSymbols ()
	{
		boolean[] used = new boolean[m_maxTerminal + 1];

		for (Production production : m_productions)
		{
			for (int sym : production.getProduction ())
			{
				if (sym <= m_maxTerminal)
					used[sym] = true;
			}
		}

		used[FINISH] = true;
		used[ERROR] = true;
		used[UNHANDLED] = true;
		if (getIgnoreList () != null)
		{
			used[IGNORE] = true;
			m_terminals.put (s_ignore.name, s_ignore);
			m_symbolMap.put (s_ignore.value, s_ignore.name);
		}
		if (getCaptureList () != null)
		{
			used[CAPTURE] = true;
			m_terminals.put (s_capture.name, s_capture);
			m_symbolMap.put (s_capture.value, s_capture.name);
		}

		int[] vec = new int[m_maxTerminal + m_nonTerminalCount + 1];
		int count = 0;
		for (int i = 0; i <= m_maxTerminal; ++i)
		{
			if (used[i])
				vec[count++] = i;
		}

		for (int i = 0; i < m_nonTerminalCount; ++i)
			vec[count++] = m_maxTerminal + 1 + i;
		m_usedSymbols = new int[count];
		System.arraycopy (vec, 0, m_usedSymbols, 0, count);

		m_symbolGroups = new int[m_maxTerminal + 1 + m_nonTerminalCount];
		for (int i = 0; i < count; ++i)
			m_symbolGroups[vec[i]] = i;

		// now we deal with unhandled list
		ArrayList<Integer> unusedList = new ArrayList<Integer> ();
		for (int i = 0; i <= m_maxTerminal; ++i)
		{
			if (!used[i])
			{
				if (isIgnored (i))
				{
					if (isCaptured (i))
					{
						m_symbolGroups[i] = CAPTURE;
					}
					else
						m_symbolGroups[i] = IGNORE;
				}
				else
				{
					m_symbolGroups[i] = UNHANDLED;
					if (i > TerminalUtils.INIT_MAX_TERMINALS && !m_mentionedSet.contains (i))
						unusedList.add (i);
				}
			}
		}
		if (unusedList.size () > 0)
		{
			Main.warn (WARN_UNUSED_TOKEN);
			StringBuffer list = new StringBuffer ("\t");
			boolean first = true;
			for (Integer terminal : unusedList)
			{
				if (!first)
					list.append (" ");
				String name = m_symbolMap.get (terminal);
				if (name != null)
					list.append (name);
				first = false;
			}
			Main.warn (list.toString ());
		}
		return count;
	}

	Item createItem (Production production, int pos, TokenSet lookahead)
	{
		return new Item (production, pos, lookahead.clone (), createTokenSet ());
	}

	/**
	 * Dummy items are for searching purpose.
	 *
	 * @param    production the production to be used, can be null.
	 * @return a dummy Item with null lookahead and first
	 */
	Item createDummyItem (Production production)
	{
		return new Item (production, 0, null, null);
	}

	TokenSet createTokenSet ()
	{
		return new TokenSet (m_usedTerminalCount, m_symbolGroups, m_usedSymbols);
	}

	/**
	 * For output purpose.
	 *
	 * @return user defined tokens that needs value definitions.
	 */
	public Collection<Token> getTokens ()
	{
		return m_tokens;
	}

	//
	// simpler version of closure, without the need of doing
	// any first computations
	//
	void propagateClosure (ItemSet itemSet)
	{
		boolean changed;
		Item dummyItem = createDummyItem (null);
		do
		{
			changed = false;

			for (Item item : itemSet.getItems ())
			{
				if (!item.isChanged ())
					continue;

				item.setChanged (false);

				int[] production = item.getProduction ().getProduction ();
				int pos = item.getPosition ();

				if (pos >= production.length ||
					production[pos] <= m_maxTerminal)
					continue;

				int nextPos;
				for (nextPos = pos + 1; nextPos < production.length; ++nextPos)
				{
					if (production[nextPos] <= m_maxTerminal)
						break;
					if (!m_firstSetVal.get (production[nextPos]).hasEpsilon ())
						break;
				}
				if (nextPos < production.length)
					continue;

				changed = true;
				itemSet.setChanged (true);

				// okay a non-terminal is found,
				int nonTerminal = production[pos];

				// hmm, needs update, so do the update

				TokenSet lookahead = item.getLookahead ().clone ();
				lookahead.setEpsilon (false);

				Production[] table = m_productionMap.get (nonTerminal);
				for (Production k : table)
				{
					dummyItem.setProduction (k);
					Item subItem = itemSet.find (dummyItem);

					subItem.updateLookahead (lookahead);
				}
			}
		}
		while (changed);
	}

	//
	// does move operation
	//
	ItemSet move (Comparator<Item> kernelSorter, ItemSet src, int symbol)
	{
		ItemSet dest = null;
		for (Item item : src.getItems ())
		{
			int[] production = item.getProduction ().getProduction ();
			int pos = item.getPosition ();

			if (pos < production.length && production[pos] == symbol)
			{
				if (dest == null)
					dest = new ItemSet (kernelSorter);
				dest.insertKernelItem (new Item (item, pos + 1));
			}
		}
		return dest;
	}

	//
	// it only takes care of shifts and no reduces
	//
	// depending on _compareLA and the closureFunction,
	// the states built are quite different
	//
	void buildStates (Closure closureFunctor, Comparator<Item> kernelSorter)
	{
		// first build the first item,
		// it has $ (FINISH) as its lookahead

		TokenSet startLookahead = createTokenSet ();
		startLookahead.addSymbol (FINISH);
		Production startProduction = m_productionMap.get (m_nonTerminals.get (START))[0];
		Item startItem = createItem (startProduction, 0, startLookahead);

		// now build the first kernel ItemSet

		ItemSet startItemSet = new ItemSet (kernelSorter);
		startItemSet.insertKernelItem (startItem);

		//DEBUGMSG ("startItemSet: " << startItemSet);

		// do a closure operation
		closureFunctor.closure (startItemSet);

		//DEBUGMSG ("startItemSet: " << startItemSet);

		// okay, finally built the first DFA state, the start state
		_DFAStates.add (startItemSet);
		_DFASet.put (startItemSet, (short)0);

		// now the loops that build all DFA states

		for (int i = 0; i < _DFAStates.size (); ++i)
		{
			ItemSet srcSet = _DFAStates.get (i);

			DFARow currentDFA = new DFARow (m_usedTerminalCount);
			m_dfa.add (currentDFA);
			short[] currentGoto = new short[m_usedSymbols.length - m_usedTerminalCount];
			m_goto.add (currentGoto);

			//DEBUGMSG ("srcSet = " << srcSet);

			for (int j = 0; j < m_usedSymbolCount; ++j)
			{
				//DEBUGMSG ("move/closure on symbol " << _tokens[j]);
				ItemSet destSet = move (kernelSorter, srcSet, m_usedSymbols[j]);

				if (destSet == null)
					continue;

				//
				// manipulate the accept state in a special way to reduce
				// an extra call to yyLex ()
				//
				if (m_usedSymbols[j] == FINISH)
				{
					// the only state that shift on FINISH lookahead is accept
					// so just make it the accept state

					currentDFA.getStates ()[j] = -1;       // -1 is for case 1, which is accept
					continue;
				}

				Short state = _DFASet.get (destSet);

				if (state == null)
				{
					// ah, a new state
					closureFunctor.closure (destSet);

					if (j < m_usedTerminalCount)
						currentDFA.getStates ()[j] = (short)_DFAStates.size ();
					else
						currentGoto[j - m_usedTerminalCount] = (short)_DFAStates.size ();

					_DFAStates.add (destSet);
					_DFASet.put (destSet, (short)(_DFAStates.size () - 1));

				}
				else
				{
					// the state existed
					if (j < m_usedTerminalCount)
						currentDFA.getStates ()[j] = state.shortValue ();
					else
						currentGoto[j - m_usedTerminalCount] = state.shortValue ();
				}
			}
		}
	}

	//
	// check if the ItemSet is contain a reducing state or not
	// if all reducing states goes to one single production,
	// return that state, other wise, return 0;
	//
	Production hasDefaultReduce (ItemSet itemSet)
	{
		Production reduceState = null;
		// we check closure set as well since they may contain
		// epsilon transitions
		for (Item item : itemSet.getItems ())
		{
			if (item.getPosition () == item.getProduction ().getProduction ().length)
			{
				if (reduceState == null)
					reduceState = item.getProduction ();
				else if (item.getProduction ().getId () < reduceState.getId ())    // no problem in logic
					reduceState = item.getProduction ();                        // pick earlier rule to reduce
			}
		}
		return reduceState;
	}

	//
	// check if an item contain reduced item,
	// also check for reduce/reduce conflicts;
	//
	// A reduce/reduce conflict is if two reduce's share the same
	// lookahead
	//
	// A shift/reduce conflict is if the shift's name token
	// is the same as the reduce's lookahead
	//
	Production hasReduce (ItemSet itemSet, int symbol)
	{
		Set<Production> reduceSet = new TreeSet<Production> ();

		for (Item item : itemSet.getItems ())
		{
			if (item.getPosition () == item.getProduction ().getProduction ().length)
			{
				if (item.getLookahead ().hasSymbol (symbol))
					reduceSet.add (item.getProduction ());
			}
		}

		if (reduceSet.size () > 0)
		{
			if (reduceSet.size () > 1)
			{
				++m_reduceConflict;
				verbose ("\treduce/reduce conflict");
			}
			// pick the earlier rule to reduce
			//return*(reduceSet.begin ());
			return reduceSet.iterator ().next ();
		}
		return null;
	}

	public boolean getDefaultReduce ()
	{
		return m_doc.getParser ().getDefaultReduce ();
	}

	//
	// check for reduced states and print states information.
	//
	// the reason to combine them is to produce conflicts message
	//
	private void reduce ()
	{
		verboseSection ("DFA states: " + _DFAStates.size ());
		boolean defaultReduce = getDefaultReduce ();
		verbose ("default reduce = " + defaultReduce);

		for (int i = 0; i < _DFAStates.size (); ++i)
		{
			verbose ("");
			verbose ("State " + i + ":");
			verbose (toString (_DFAStates.get (i)));

			short[] column = m_dfa.getRow (i).getStates ();

			// force default reduce on any productions with error tokens
			Production defaultReduceState = hasDefaultReduce (_DFAStates.get (i));
			if (defaultReduceState != null && (defaultReduce || defaultReduceState.isErrorCorrecting ()))
			{
				short id = (short)-defaultReduceState.getId ();
				for (int j = 0; j < column.length; ++j)
					if (column[j] == 0)
						column[j] = id;
			}

			for (int j = 0; j < m_usedTerminalCount; ++j)
			{
				Production reduceState = hasReduce (_DFAStates.get (i), m_usedSymbols[j]);
				String reason = "";

				if (reduceState != null && column[j] > 0)
				{
					// possible shift reduce error, try to resolve

					Token shiftPrecedence = Token.DEFAULT;

					// we need to check the precedence of the rules of the destination
					// kernel (important!) set, not the how set.

					ItemSet destSet = _DFAStates.get (column[j]);

					for (Item item : destSet.getKernelItems ())
					{
						Token prec = item.getProduction ().getPrecedence ();
						if (shiftPrecedence.level < prec.level)
							shiftPrecedence = prec;
					}

					Token reducePrecedence = reduceState.getPrecedence ();

					if (shiftPrecedence.level < reducePrecedence.level)
					{
						// we go for the reduce
						reason = ", due to precedence";
					}
					else if (shiftPrecedence.level > reducePrecedence.level)
					{
						// we go for the shift
						reason = ", due to precedence";
						reduceState = null;
					}
					else
					{
						// now check associativity
						if (shiftPrecedence.type == Token.LEFT)
						{
							reason = ", due to left associativity";
						}
						else if (shiftPrecedence.type == Token.RIGHT)
						{
							// right associativity
							if (shiftPrecedence.level > 0)
							{
								reason = ", due to right associativity";
								reduceState = null;
							}
							else
							{
								reason = ", due to shift/reduce conflict";
								reduceState = null;
								++m_shiftConflict;
							}
						}
						else // NONASSOC
						{
							reason = ", due to shift/reduce conflict on non-associative terminal";
							++m_shiftConflict;
						}
					}
				}
				if (reduceState != null)
					column[j] = (short)-reduceState.getId ();

				if (m_out != null)
				{
					if (column[j] != 0)
					{
						m_out.print ('\t' + m_symbolMap.get (m_usedSymbols[j]));
						if (column[j] > 0)
							m_out.println ("\tshift, goto to state " + column[j] + reason);
						else if (column[j] < -1)
							m_out.println ("\treduce to rule " + (-column[j]) + reason);
						else if (column[j] == -1)
							m_out.println ("\tAccept");
					}
				}
			}

			if (m_out != null)
			{
				short[] gotoColumn = m_goto.get (i);
				for (int j = 0; j < gotoColumn.length; ++j)
				{
					if (gotoColumn[j] != 0)
						verbose ('\t' + m_symbolMap.get (m_usedSymbols[m_usedTerminalCount + j]) + "\tshift, goto to state " + gotoColumn[j]);
				}
				verbose ("");
			}
		}
		if (m_shiftConflict > 0 || m_reduceConflict > 0)
			Main.warn ("shift/reduce conflicts: " + m_shiftConflict + ", reduce/reduce conflicts: " + m_reduceConflict);
		verbose ("shift/reduce conflicts: " + m_shiftConflict + ", reduce/reduce conflicts: " + m_reduceConflict);
	}

	ArrayList<Production> getProductions ()
	{
		return m_productions;
	}

	Map<Integer, Production[]> getProductionMap ()
	{
		return m_productionMap;
	}

	public DFATable getDFA ()
	{
		return m_dfa;
	}

	public ArrayList<short[]> getGoto ()
	{
		return m_goto;
	}

	public int[] getUsedSymbols ()
	{
		return m_usedSymbols;
	}

	public int[] getSymbolGroups ()
	{
		return m_symbolGroups;
	}

	public int[] getUsedTerminals ()
	{
		int[] usedTerminals = new int[m_usedTerminalCount];
		System.arraycopy (m_usedSymbols, 0, usedTerminals, 0, m_usedTerminalCount);
		return usedTerminals;
	}

	public int getTerminalCount ()
	{
		return m_terminalCount;
	}

	public int getNonTerminalCount ()
	{
		return m_nonTerminalCount;
	}

	public int getUsedTerminalCount ()
	{
		return m_usedTerminalCount;
	}

	int getUsedSymbolCount ()
	{
		return m_usedSymbolCount;
	}

	// debugging function
	String toString (Production production)
	{
		StringBuffer buffer = new StringBuffer ();
		buffer.append (production.getId ()).append ('\t');
		buffer.append (m_symbolMap.get (production.getSymbol ())).append ("\t:\t");
		for (int p : production.getProduction ())
			buffer.append (" ").append (m_symbolMap.get (p));
		return buffer.toString ();
	}

	String toString (TokenSet tokenSet)
	{
		StringBuffer buffer = new StringBuffer ();
		buffer.append ("[");
		boolean separator = false;
		for (int i = 0; i < m_usedTerminalCount; ++i)
		{
			int sym = m_usedSymbols[i];
			if (tokenSet.hasSymbol (sym))
			{
				if (separator)
					buffer.append (" ");
				separator = true;
				buffer.append (m_symbolMap.get (sym));
			}
		}
		buffer.append ("]");
		return buffer.toString ();
	}

	String toString (Item item)
	{
		StringBuffer buffer = new StringBuffer ();
		Production production = item.getProduction ();
		buffer.append (m_symbolMap.get (production.getSymbol ())).append ("\t:");
		int[] prods = production.getProduction ();
		for (int i = 0; i < prods.length; ++i)
		{
			if (i == item.getPosition ())
				buffer.append (" . ");
			else
				buffer.append (" ");
			buffer.append (m_symbolMap.get (prods[i]));
		}
		if (item.getPosition () == prods.length)
			buffer.append (" .");
		buffer.append (" , ").append (toString (item.getLookahead ()));
		return buffer.toString ();
	}

	String toString (ItemSet itemSet)
	{
		StringBuffer buffer = new StringBuffer ();
		for (Item item : itemSet.getItems ())
		{
			if (itemSet.isKernelItem (item))
				buffer.append (" *\t");
			else
				buffer.append (" -\t");
			buffer.append (toString (item)).append ("\n");
		}
		return buffer.toString ();
	}

	String toString (Token token)
	{
		return token.name + "[" + token.level + "]";
	}

	public ArrayList<Production> getRules ()
	{
		return m_productions;
	}

	public short[] getDefaultReduces ()
	{
		if (!getDefaultReduce ())
			return null;
		short[] reduceStates = new short[_DFAStates.size ()];
		for (int i = 0; i < _DFAStates.size (); ++i)
		{
			Production reduceState = hasDefaultReduce (_DFAStates.get (i));
			if (reduceState != null)
				reduceStates[i] = reduceState.getId ();
		}
		return reduceStates;
	}

	public int getCaseCount ()
	{
//		return m_productionIdCounter;
		return this.m_productionCounter.getCount ();
	}


	public int getMaxTerminal ()
	{
		return m_maxTerminal;
	}

	public String[] getSymbols ()
	{
		String[] symbols = new String[m_maxTerminal + m_nonTerminalCount - TerminalUtils.INIT_MAX_TERMINALS];
		for (int i = 0; i < (m_maxTerminal + m_nonTerminalCount - TerminalUtils.INIT_MAX_TERMINALS); ++i)
		{
			symbols[i] = m_symbolMap.get (i + TerminalUtils.INIT_MAX_TERMINALS + 1);
			if (symbols[i] == null)
				symbols[i] = ".";
		}
		return symbols;
	}

	public Map<Integer, MessageFormat> getFormats ()
	{
		if (m_formats.size () > 0)
			return m_formats;

		int[] value = new int[1];
		for (TypeDoc typeDoc : m_doc.getParser ().getTypes ())
		{
			MessageFormat format = typeDoc.getFormat ();
			for (String name : typeDoc.getSymbols ())
			{
				checkTerminalName (0, name, value);
				if (value[0] > 0)
					m_formats.put (value[0], format);
				else
				{
					Token token = m_terminals.get (name);
					if (token != null)
						m_formats.put (token.value, format);
					else
					{
						Integer sym = m_nonTerminals.get (name);
						if (sym != null)
							m_formats.put (sym, format);
					}
				}
			}
		}
		return m_formats;
	}

	public int getShiftConflict ()
	{
		return m_shiftConflict;
	}

	public int getReduceConflict ()
	{
		return m_reduceConflict;
	}

	public int[] getIgnoreList ()
	{
		if (m_ignoreList != null)
			return m_ignoreList;
		IgnoreDoc ignoreDoc = m_doc.getParser ().getIgnore ();
		if (ignoreDoc == null)
			return null;
		String[] ignores = ignoreDoc.getList ();

		int[] checkValue = new int[1];

		m_ignoreList = new int[ignores.length];
		long lineNumber = ignoreDoc.getLineNumber ();
		int i = 0;
		for (String name : ignores)
		{
			checkValue[0] = 0;
			name = checkTerminalName (lineNumber, name, checkValue);
			if (checkValue[0] != 0)
				m_ignoreList[i] = checkValue[0];
			else
			{
				Token token = m_terminals.get (name);
				if (token == null)
					throw new ParserException (lineNumber, "Unknown terminal in ignore list: " + name);
				m_ignoreList[i] = token.value;
			}
			++i;
		}
		Arrays.sort (m_ignoreList);
		return m_ignoreList;
	}

	public int[] getCaptureList ()
	{
		if (m_captureList != null)
			return m_captureList;
		IgnoreDoc ignoreDoc = m_doc.getParser ().getIgnore ();
		if (ignoreDoc == null)
			return null;
		String[] ignores = ignoreDoc.getList ();
		String[] captures = ignoreDoc.getCapture ();
		if (captures == null || captures.length == 0)
			return null;

		int[] checkValue = new int[1];

		m_captureList = new int[captures.length];
		long lineNumber = ignoreDoc.getLineNumber ();
		int i = 0;
		for (String name : captures)
		{
			// now we need to make sure all elements of captureList must
			// be in the ignore list.
			if (Arrays.binarySearch (ignores, name) < 0)
				throw new ParserException (lineNumber, "Ignore list does not container the capture list terminal " + name);

			checkValue[0] = 0;
			name = checkTerminalName (lineNumber, name, checkValue);
			if (checkValue[0] != 0)
				m_captureList[i] = checkValue[0];
			else
			{
				Token token = m_terminals.get (name);
				if (token == null)
					throw new ParserException (lineNumber, "Unknown terminal in capture list: " + name);
				m_captureList[i] = token.value;
			}
		}
		Arrays.sort (m_captureList);

		return m_captureList;
	}

	@Override
	public Symbol getSymbol (String symbol)
	{
		Symbol n = m_symbols.get (symbol);
		if (n == null)
		{
			n = new StringSymbol (symbol, false);
			m_symbols.put (symbol, n);
		}
		return n;
	}

	@Override
	public Symbol getSymbol (char ch)
	{
		Character c = Character.valueOf (ch);
		CharSymbol n = m_charSymbols.get (c);
		if (n == null)
		{
			n = new CharSymbol (c);
			m_charSymbols.put (c, n);
		}
		return n;
	}

	@Override
	public Symbol createInternalRule (Symbol[] rhs)
	{
		ParserDoc parserDoc = m_doc.getParser ();
		// we need a new symbol to handle the new grammar.
		String newSymbol = getNewInternalNonTerminal ();
		GrammarDoc tmpGrammar = parserDoc.getGrammar (newSymbol);

		StringBuilder buffer = new StringBuilder ();
		for (Symbol sym : rhs)
		{
			if (buffer.length () > 0)
				buffer.append (' ');
			buffer.append (sym.getName ());
		}
		RhsDoc rhsDoc = new RhsDoc ();
		rhsDoc.setTerms (buffer.toString ());
		tmpGrammar.addRhs (rhsDoc);

		return getSymbol (newSymbol);
	}
/*
	public Symbol createInternalRule (char type, Symbol rhs)
	{
		ParserDoc parserDoc = m_doc.getParser ();
		// we need a new symbol to handle the new grammar.
		String newSymbol = getNewInternalNonTerminal ();
		GrammarDoc tmpGrammar = parserDoc.getGrammar (newSymbol);
		tmpGrammar.internalSetType (type);

		String sym = rhs.getName ();
		switch (type)
		{
			case '?':
			{
				RhsDoc emptyRhs = new RhsDoc ();
				RhsDoc optRhs = new RhsDoc ();
				optRhs.setTerms (sym);
				tmpGrammar.addRhs (emptyRhs);
				tmpGrammar.addRhs (optRhs);
				break;
			}
			case '*':
			{
				RhsDoc initRhs = new RhsDoc ();
				RhsDoc listRhs = new RhsDoc ();
				listRhs.setTerms (newSymbol + " " + sym);
				tmpGrammar.addRhs (initRhs);
				tmpGrammar.addRhs (listRhs);
				break;
			}
			case '+':
			{
				RhsDoc initRhs = new RhsDoc ();
				initRhs.setTerms (sym);
				RhsDoc listRhs = new RhsDoc ();
				listRhs.setTerms (newSymbol + " " + sym);
				tmpGrammar.addRhs (initRhs);
				tmpGrammar.addRhs (listRhs);
				break;
			}
		}
		return getSymbol (newSymbol);
	}
*/
	@Override
	public Symbol createInternalSymbol ()
	{
		String newSymbol = getNewInternalNonTerminal ();
		return getSymbol (newSymbol);
	}

	@Override
	public void addRule (Symbol lhs, Symbol[] rhs)
	{
	}
}
