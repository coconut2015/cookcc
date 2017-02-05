package org.yuanheng.cookcc.parser.ast;

import java.util.ArrayList;
import java.util.Arrays;

import org.yuanheng.cookcc.doc.GrammarDoc;
import org.yuanheng.cookcc.doc.ParserDoc;

/**
 * @author	Heng Yuan
 */
public class StarSymbol extends InternalSymbol
{
	private final Symbol[] m_rhs;

	public StarSymbol (Symbol symbol, Symbol[] rhs)
	{
		super (symbol);
		m_rhs = rhs;
	}

	@Override
	public void addNewRules (ArrayList<SingleRule> rules, long lineNumber, ParserDoc parserDoc, ProductionCounter counter)
	{
		SingleRule r1 = new SingleRule (getSymbol (), new Symbol[0], "", lineNumber, counter, null);
		rules.add (r1);
		Symbol[] newRhs = new Symbol[m_rhs.length + 1];
		newRhs[0] = getSymbol ();
		for (int i = 0; i < m_rhs.length; ++i)
			newRhs[i + 1] = m_rhs[i];
		SingleRule r2 = new SingleRule (getSymbol (), newRhs, "", lineNumber, counter, null);
		rules.add (r2);

		GrammarDoc grammar = parserDoc.getGrammar (getSymbol ().getName ());
		grammar.setType ('*');
		grammar.addRhs (r1.getRhsDoc ());
		grammar.addRhs (r2.getRhsDoc ());
	}

	@Override
	public String toString ()
	{
		StringBuilder buffer = new StringBuilder ();
		if (m_rhs.length > 1)
		{
			buffer.append ('(');
			buffer.append (toString (Arrays.asList (m_rhs)));
			buffer.append (')');
		}
		else
		{
			buffer.append (m_rhs[0]);
		}
		buffer.append ('*');
		return buffer.toString ();
	}
}
