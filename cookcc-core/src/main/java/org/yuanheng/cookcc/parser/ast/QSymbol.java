package org.yuanheng.cookcc.parser.ast;

import java.util.ArrayList;
import java.util.Arrays;

import org.yuanheng.cookcc.doc.GrammarDoc;
import org.yuanheng.cookcc.doc.ParserDoc;

/**
 * @author	Heng Yuan
 */
public class QSymbol extends InternalSymbol
{
	private final Symbol[] m_rhs;

	public QSymbol (Symbol symbol, Symbol[] rhs)
	{
		super (symbol);
		m_rhs = rhs;
	}

	@Override
	public void addNewRules (ArrayList<SingleRule> rules, SymbolLibrary library, long lineNumber, ParserDoc parserDoc, ProductionCounter counter)
	{
		SingleRule r1 = new SingleRule (getSymbol (), new Symbol[0], "", lineNumber, counter, null);
		rules.add (r1);
		SingleRule r2 = new SingleRule (getSymbol (), m_rhs, "", lineNumber, counter, null);
		rules.add (r2);

		GrammarDoc grammar = parserDoc.getGrammar (getSymbol ().getName ());
		grammar.internalSetSymbol (getSymbol ().getValue (library, lineNumber));
		grammar.internalSetType ('?');
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
		buffer.append ('?');
		return buffer.toString ();
	}
}
