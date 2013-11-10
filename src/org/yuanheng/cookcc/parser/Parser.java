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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

import org.yuanheng.cookcc.Main;
import org.yuanheng.cookcc.OptionMap;
import org.yuanheng.cookcc.dfa.DFARow;
import org.yuanheng.cookcc.dfa.DFATable;
import org.yuanheng.cookcc.doc.*;
import org.yuanheng.cookcc.exception.ParserException;
import org.yuanheng.cookcc.lexer.CCL;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class Parser
{
	private final static String PROP_PARSER = "Parser";
	private final static String PROP_PRODUCTION = "Production";

	private static Pattern s_tokenNamePattern = Pattern.compile ("[a-zA-Z_][a-zA-Z_0-9]*");

	public static String START = "@start";

	public static int FINISH = 0;
	public static int ERROR = 1;

	//	private static Token s_epsilon = new Token ("{e}", 0, EPSILON, Token.NONASSOC);
	private static Token s_finish = new Token ("$", 0, FINISH, Token.NONASSOC);
	private static Token s_error = new Token ("error", 0, ERROR, Token.NONASSOC);

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
	private final Map<String, Token> m_terminals = new HashMap<String, Token> ();
	private final Map<Integer, Token> m_terminalMap = new HashMap<Integer, Token> ();
	private final Map<String, Integer> m_nonTerminals = new HashMap<String, Integer> ();
	private final Vector<Production> m_productions = new Vector<Production> ();
	private final Map<Integer, String> m_symbolMap = new TreeMap<Integer, String> ();
	// for a given symbol, its productions
	private final Map<Integer, Production[]> m_productionMap = new HashMap<Integer, Production[]> ();

	private final HashMap<Integer, int[]> m_firstSet = new HashMap<Integer, int[]> ();
	private final HashMap<Integer, TokenSet> m_firstSetVal = new HashMap<Integer, TokenSet> ();

	private final DFATable m_dfa = new DFATable ();
	private final Vector<short[]> m_goto = new Vector<short[]> ();

	private final LinkedList<Token> m_tokens = new LinkedList<Token> ();

	private final HashMap<Integer, MessageFormat> m_formats = new HashMap<Integer, MessageFormat> ();

	final Vector<ItemSet> _DFAStates = new Vector<ItemSet> ();
	final Map<ItemSet, Short> _DFASet = new TreeMap<ItemSet, Short> ();

	private int m_reduceConflict;
	private int m_shiftConflict;

	private PrintStream m_out;

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
		File analysisFile = Main.getAnalysisFile (m_options);
		if (analysisFile != null)
			m_out = new PrintStream (new FileOutputStream (analysisFile));

		m_terminals.put (s_finish.name, s_finish);
		m_terminals.put (s_error.name, s_error);

		m_symbolMap.put (s_finish.value, s_finish.name);
		m_symbolMap.put (s_error.value, s_error.name);

		m_maxTerminal = parseTerminals ();

		// add start condition
		Production startProduction = new Production (getNonterminal (START), m_productionIdCounter++);
		m_productions.add (startProduction);
		m_productionMap.put (m_nonTerminals.get (START), new Production[]{startProduction});

		parseProductions ();

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

	private int parseTerminals ()
	{
		int precedenceLevel = 0;
		TokensDoc[] tokensDocs = m_doc.getTokens ();
		int maxTerminalValue = 255;

		int[] checkValue = new int[1];
		for (TokensDoc tokensDoc : tokensDocs)
		{
			int level = precedenceLevel++;
			String[] names = tokensDoc.getTokens ();
			if (names == null)
				continue;
			for (String name : names)
			{
				if (m_terminals.containsKey (name))
					throw new ParserException (tokensDoc.getLineNumber (), "Duplicate token " + name + " specified.");

				checkValue[0] = 0;
				name = checkTerminalName (tokensDoc.getLineNumber (), name, checkValue, true);
				int v = checkValue[0];
				if (v == 0)
					v = ++maxTerminalValue;
				Token token = new Token (name, level, v, tokensDoc.getType ());
				m_terminals.put (name, token);
				m_terminalMap.put (v, token);

				if (m_symbolMap.get (v) == null)
					m_symbolMap.put (v, name);
				if (checkValue[0] == 0)
					m_tokens.add (token);
			}
		}
		return maxTerminalValue;
	}

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
			LinkedList<Production> prods = new LinkedList<Production> ();
			for (RhsDoc rhs : grammar.getRhs ())
			{
				Production production = new Production (lhs, m_productionIdCounter++);
				LinkedList<Integer> symbolList = new LinkedList<Integer> ();
				String terms = rhs.getTerms ().trim ();
				int lineNumber = rhs.getLineNumber ();
				while (terms.length () > 0)
				{
					pos[0] = 0;
					int sym = parseTerm (lineNumber, terms, pos, type);
					if (type[0] == '\0')
					{
						if (sym <= m_maxTerminal)
							production.setPrecedence (m_terminalMap.get (sym));
					}
					else
					{
						// we need a new symbol to handle the new grammar.
						String tmpSymbol = grammar.getRule ();
						GrammarDoc tmpGrammar;
						for (int i = 0; ; ++i)
						{
							if (parserDoc.hasGrammar (tmpSymbol + "_" + i))
								continue;
							tmpSymbol = tmpSymbol + "_" + i;
							tmpGrammar = parserDoc.getGrammar (tmpSymbol);
							tmpGrammar.setType (type[0]);
							break;
						}
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
							throw new ParserException (lineNumber, "Invalid terminal specified for %prec.");
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

	private int parseTerm (int lineNumber, String terms, int[] pos, char[] type)
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
			return getSymbol (lineNumber, "'\\''");
		}
		if (terms.charAt (0) == '\'')
		{
			int index = terms.indexOf ('\'', 1);
			if (index > 1)
			{
				String name = terms.substring (0, index + 1);
				int symbol = getSymbol (lineNumber, name);
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
			int symbol = getSymbol (lineNumber, name);
			pos[0] = index;
			return symbol;
		}
		throw new ParserException (lineNumber, "Invalid symbol: " + terms);
	}

	private String checkTerminalName (int lineNumber, String name, int[] value)
	{
		return checkTerminalName (lineNumber, name, value, false);
	}

	private String checkTerminalName (int lineNumber, String name, int[] value, boolean noInternal)
	{
		try
		{
			if (name.startsWith ("'"))
			{
				int[] pos = new int[1];
				pos[0] = 1;
				char ch = CCL.esc (name, pos);
				if (name.length () == (pos[0] + 1) && name.charAt (pos[0]) == '\'')
				{
					value[0] = ch;
					++pos[0];

					if (m_symbolMap.get ((int)ch) == null)
						m_symbolMap.put ((int)ch, name);
					return String.valueOf ((int)ch);
				}
			}
			else if (s_tokenNamePattern.matcher (name).matches ())
			{
				if (noInternal && "error".equals (name))
					throw new ParserException (0, "error token is built-in");
				return name;
			}
		}
		catch (ParserException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
		}
		throw new ParserException (lineNumber, "Invalid token name: " + name);
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

	private int getSymbol (int lineNumber, String name)
	{
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

		int[] vec = new int[m_maxTerminal + m_nonTerminalCount + 1];
		int count = 0;
		for (int i = 0; i <= m_maxTerminal; ++i)
		{
			if (used[i] == true)
				vec[count++] = i;
		}

		for (int i = 0; i < m_nonTerminalCount; ++i)
			vec[count++] = m_maxTerminal + 1 + i;
		m_usedSymbols = new int[count];
		System.arraycopy (vec, 0, m_usedSymbols, 0, count);

		m_symbolGroups = new int[m_maxTerminal + 1 + m_nonTerminalCount];
		for (int i = 0; i < count; ++i)
			m_symbolGroups[vec[i]] = i;
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
	public LinkedList<Token> getTokens ()
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

	Vector<Production> getProductions ()
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

	public Vector<short[]> getGoto ()
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

	public Vector<Production> getRules ()
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
		return m_productionIdCounter;
	}


	public int getMaxTerminal ()
	{
		return m_maxTerminal;
	}

	public String[] getSymbols ()
	{
		String[] symbols = new String[m_usedSymbolCount];
		for (int i = 0; i < symbols.length; ++i)
			symbols[i] = m_symbolMap.get (m_usedSymbols[i]);
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
}
