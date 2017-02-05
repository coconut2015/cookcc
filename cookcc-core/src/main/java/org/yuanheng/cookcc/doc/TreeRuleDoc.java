package org.yuanheng.cookcc.doc;

/**
 * @author	Heng Yuan
 */
public class TreeRuleDoc extends TreeDoc
{
	private long m_lineNumber;
	private String m_grammar = "";

	public long getLineNumber ()
	{
		return m_lineNumber;
	}

	public void setLineNumber (long lineNumber)
	{
		m_lineNumber = lineNumber;
	}

	public String getGrammar ()
	{
		return m_grammar;
	}

	public void setGrammar (String grammar)
	{
		if (grammar == null)
			grammar = "";
		m_grammar = grammar;
	}
}
