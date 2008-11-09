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
</#if>
