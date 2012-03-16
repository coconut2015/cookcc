package org.yuanheng.cookcc.lexer;

public class LexerPattern implements Pattern
{
	private final boolean m_bol;
	private final ChainPattern m_pattern;
	private int m_precedence;
	private String m_originalText;

	public LexerPattern (ChainPattern pattern, boolean bol)
	{
		m_pattern = pattern;
		m_bol = bol;
	}

	public int getPrecedence ()
	{
		return m_precedence;
	}

	public void setPrecedence (int precedence)
	{
		m_precedence = precedence;
	}

	public boolean isBol ()
	{
		return m_bol;
	}

	public ChainPattern getPattern ()
	{
		return m_pattern;
	}

	@Override
	public String toString ()
	{
		if (m_bol)
			return "^" + m_pattern;
		return m_pattern.toString ();
	}

	public void setOriginalText (String originalText)
	{
		m_originalText = originalText;
	}

	public String getOriginalText ()
	{
		return m_originalText;
	}

	public int getLength ()
	{
		return m_pattern.getLength ();
	}

	public NFA constructNFA (NFAFactory factory, NFA start)
	{
		return m_pattern.constructNFA (factory, start);
	}
}
