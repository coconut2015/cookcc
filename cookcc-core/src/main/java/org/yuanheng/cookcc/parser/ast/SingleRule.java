package org.yuanheng.cookcc.parser.ast;

import java.util.ArrayList;

import org.yuanheng.cookcc.doc.ParserDoc;
import org.yuanheng.cookcc.doc.RhsDoc;

/**
 * @author	Heng Yuan
 */
public class SingleRule
{
	public final Symbol lhs;
	public final Symbol[] rhs;
	public final String precedence;
	public final long lineNumber;
	public final short caseValue;

	private RhsDoc m_rhsDoc;

	public SingleRule (Symbol lhs, Symbol[] rhs, String precedence, long lineNumber, ProductionCounter counter, RhsDoc rhsDoc)
	{
		this.lhs = lhs;
		this.rhs = rhs;
		this.precedence = precedence;
		this.lineNumber = lineNumber;
		this.caseValue = counter.newId ();
		if (rhsDoc != null)
		{
			m_rhsDoc = rhsDoc;
		}
		else
		{
			m_rhsDoc = createRhsDoc (rhs);
		}
	}

	public void addNewRules (ArrayList<SingleRule> rules, SymbolLibrary library, ParserDoc parserDoc, ProductionCounter counter)
	{
		for (Symbol s: rhs)
		{
			s.addNewRules (rules, library, lineNumber, parserDoc, counter);
		}
	}

	public String getTerms ()
	{
		StringBuilder buffer = new StringBuilder ();
		boolean first = true;
		for (Symbol s : rhs)
		{
			if (first)
				first = false;
			else
				buffer.append (' ');
			buffer.append (s.getName ());
		}
		return buffer.toString ();
	}

	private RhsDoc createRhsDoc (Symbol[] rhs)
	{
		RhsDoc rhsDoc = new RhsDoc (rhs.length);
		rhsDoc.setTerms (getTerms ());
		rhsDoc.setLineNumber (lineNumber);
		rhsDoc.setCaseValue (caseValue);
		m_rhsDoc = rhsDoc;
		return rhsDoc;
	}

	@Override
	public String toString ()
	{
		StringBuilder buffer = new StringBuilder ();
		buffer.append (this.lhs).append (" :");
		for (Symbol s : this.rhs)
		{
			buffer.append (' ').append (s);
		}
		return buffer.toString ();
	}

	public RhsDoc getRhsDoc ()
	{
		return m_rhsDoc;
	}

	public void setRhsDoc (RhsDoc rhsDoc)
	{
		m_rhsDoc = rhsDoc;
	}
}
