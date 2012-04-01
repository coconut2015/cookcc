package org.yuanheng.cookcc.lexer;

public class LexerPattern implements Pattern
{
	private final boolean m_bol;
	private final boolean m_eol;
	private final ChainPattern m_pattern;
	private final ChainPattern m_trailPattern;
	private int m_precedence;
	private String m_originalText;

	public LexerPattern (ChainPattern pattern, ChainPattern trailPattern, boolean bol, boolean eol)
	{
		m_pattern = pattern;
		m_trailPattern = trailPattern;
		m_bol = bol;
		m_eol = eol;
	}

	public boolean requiresPreprocessing ()
	{
		return m_eol || m_trailPattern != null || m_pattern.hasSubExpression ();
	}

	public int getTrailContext ()
	{
		if (m_trailPattern == null)
			return 0;
		if (m_pattern.getLength () > 0)
			return NFA.getTrailContext (m_pattern.getLength (), true, false);
		else
			return NFA.getTrailContext (m_trailPattern.getLength (), false, true);
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

	public boolean isEol ()
	{
		return m_eol;
	}

	public ChainPattern getPattern ()
	{
		return m_pattern;
	}

	@Override
	public String toString ()
	{
		StringBuffer buffer = new StringBuffer ();
		if (m_bol)
			buffer.append ('^');
		buffer.append (m_pattern);
		if (m_trailPattern != null)
		{
			buffer.append ('/').append (m_trailPattern);
		}
		if (m_eol)
			buffer.append ('$');
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
		start.trailContext = getTrailContext ();
		NFA end = m_pattern.constructNFA (factory, start);
		if (m_trailPattern == null)
			return end;
		return m_trailPattern.constructNFA (factory, end);
	}

	public boolean hasSubExpression ()
	{
		return m_pattern.hasSubExpression ();
	}
}
