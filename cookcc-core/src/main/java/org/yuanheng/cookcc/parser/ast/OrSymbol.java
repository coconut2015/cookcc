package org.yuanheng.cookcc.parser.ast;

import java.util.ArrayList;
import java.util.Arrays;

import org.yuanheng.cookcc.doc.GrammarDoc;
import org.yuanheng.cookcc.doc.ParserDoc;

/**
 * @author	Heng Yuan
 */
public class OrSymbol extends InternalSymbol
{
	private final ArrayList<Symbol[]> m_rules = new ArrayList<Symbol[]> ();

	OrSymbol (Symbol symbol)
	{
		super (symbol);
	}

	public void addRule (Symbol[] rhs)
	{
		m_rules.add (rhs);
	}

	@Override
	public void addNewRules (ArrayList<SingleRule> rules, SymbolLibrary library, long lineNumber, ParserDoc parserDoc, ProductionCounter counter)
	{
		GrammarDoc grammar = parserDoc.getGrammar (getSymbol ().getName ());
		grammar.internalSetSymbol (getSymbol ().getValue (library, lineNumber));
		grammar.internalSetType ('|');
		for (Symbol[] rhs : m_rules)
		{
			SingleRule singleRule = new SingleRule (getSymbol (), rhs, "", lineNumber, counter, null);
			rules.add (singleRule);

			grammar.addRhs (singleRule.getRhsDoc ());
		}
	}

	@Override
	public String toString ()
	{
		StringBuilder buffer = new StringBuilder ();
		boolean first = true;
		buffer.append ('(');
		for (Symbol[] symbols : m_rules)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				buffer.append (" | ");
			}
			buffer.append (toString (Arrays.asList (symbols)));
		}
		buffer.append (')');
		return buffer.toString ();
	}
}
