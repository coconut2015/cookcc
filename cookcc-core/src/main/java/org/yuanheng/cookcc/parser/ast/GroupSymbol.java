package org.yuanheng.cookcc.parser.ast;

import java.util.ArrayList;
import java.util.Arrays;

import org.yuanheng.cookcc.doc.GrammarDoc;
import org.yuanheng.cookcc.doc.ParserDoc;

/**
 * Simple grouping of symbols.
 *
 * @author	Heng Yuan
 */
public class GroupSymbol extends InternalSymbol
{
	private final Symbol[] m_rhs;

	public GroupSymbol (Symbol symbol, Symbol[] rhs)
	{
		super (symbol);
		m_rhs = rhs;
	}

	@Override
	public void addNewRules (ArrayList<SingleRule> rules, long lineNumber, ParserDoc parserDoc, ProductionCounter counter)
	{
		SingleRule r = new SingleRule (getSymbol (), m_rhs, "", lineNumber, counter, null);
		rules.add (r);

		GrammarDoc grammar = parserDoc.getGrammar (getSymbol ().getName ());
		grammar.internalSetType ('g');
		grammar.addRhs (r.getRhsDoc ());
	}

	@Override
	public String toString ()
	{
		StringBuilder buffer = new StringBuilder ();
		buffer.append ('(');
		buffer.append (toString (Arrays.asList (m_rhs)));
		buffer.append (')');
		return buffer.toString ();
	}
}
