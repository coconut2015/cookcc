	private final ${child} m_this = (${child})this;

<#if states?has_content>
	/**
	 * This function is used to change the initial state for the lexer.
	 *
	 * @param	state
	 *			the name of the state
	 */
	protected void begin (String state)
	{
	<#list states as i>
		if ("${i.name}".equals (state))
		{
			begin (${i.name});
			return;
		}
	</#list>
		throw new IllegalArgumentException ("Unknown lexer state: " + state);
	}

	/**
	 * Push the current state onto lexer state onto stack and
	 * begin the new state specified by the user.
	 *
	 * @param	state
	 *			the new state.
	 */
	protected void yyPushLexerState (String state)
	{
	<#list states as i>
		if ("${i.name}".equals (state))
		{
			yyPushLexerState (${i.name});
			return;
		}
	</#list>
		throw new IllegalArgumentException ("Unknown lexer state: " + state);
	}

	/**
	 * Check if there are more inputs.  This function is called when EOF is
	 * encountered.
	 *
	 * @return	true to indicate no more inputs.
	 * @throws	IOException
	 * 			in case of an IO error
	 */
	protected boolean yyWrap () throws IOException
	{
		if (yyInputStackSize () > 0)
		{
			yyPopInput ();
			return false;
		}
		return true;
	}
</#if>
