/*
 * Copyright (c) 2008-2013, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *    Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    Neither the name of the Heng Yuan nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY Heng Yuan ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Heng Yuan BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.yuanheng.cookcc.doc;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class LexerDoc extends TreeDoc
{
	public final static String INITIAL_STATE = "INITIAL";
	public final static String PROP_TABLE_TYPE = "table";
	public final static String DEFAULT_TABLE_TYPE = "ecs";

	// we use linked list to keep the state being used in order, may be useful.
	private final LinkedList<LexerStateDoc> m_stateList = new LinkedList<LexerStateDoc> ();
	private final HashMap<String, LexerStateDoc> m_stateMap = new HashMap<String, LexerStateDoc> ();
	private final HashMap<String, ShortcutDoc> m_shortcutMap = new HashMap<String, ShortcutDoc> ();

	private boolean m_lineMode;

	private boolean m_bol;

	private boolean m_warnBackup;

	private boolean m_yywrap;

	public LexerStateDoc getLexerState (String stateName)
	{
		if (stateName == null || stateName.length () == 0)
			stateName = INITIAL_STATE;
		LexerStateDoc lexerState = m_stateMap.get (stateName);
		if (lexerState == null)
		{
			lexerState = new LexerStateDoc (stateName);
			m_stateMap.put (stateName, lexerState);
			m_stateList.add (lexerState);
		}

		return lexerState;
	}

	public void setTable (String type)
	{
		setProperty (PROP_TABLE_TYPE, type);
	}

	public String getTable ()
	{
		String type = (String)getProperty (PROP_TABLE_TYPE);
		if (type == null)
			type = DEFAULT_TABLE_TYPE;
		return type;
	}

	public LexerStateDoc[] getLexerStates ()
	{
		return m_stateList.toArray (new LexerStateDoc[m_stateList.size ()]);
	}

	public void addShortcut (ShortcutDoc shortcut)
	{
		if (m_shortcutMap.get (shortcut.getName ()) != null)
			throw new RuntimeException ("Duplicate pattern name: " + shortcut);
		m_shortcutMap.put (shortcut.getName (), shortcut);
	}

	public ShortcutDoc[] getShortcuts ()
	{
		return m_shortcutMap.values ().toArray (new ShortcutDoc[m_shortcutMap.size ()]);
	}

	public ShortcutDoc getShortcut (String name)
	{
		return m_shortcutMap.get (name);
	}

	/**
	 * Check if the user wants to use the line mode in lexer.
	 *
	 * @return	whether or not to use line mode in lexer.
	 */
	public boolean isLineMode ()
	{
		return m_lineMode;
	}

	/**
	 * Whether or not to use line mode in lexer.
	 *
	 * @param	lineMode
	 * 			whether or not to use line mode in lexer.
	 */
	public void setLineMode (boolean lineMode)
	{
		m_lineMode = lineMode;
	}

	/**
	 * Check if the user set a global option that checks BOL state in the lexer.
	 *
	 * @return the global bol option
	 */
	public boolean isBol ()
	{
		return m_bol;
	}

	/**
	 * Set a global option that checks BOL state in the lexer.  If set to true,
	 * this option would force the generated code to maintain BOL status even
	 * if no patterns required the BOL information.
	 *
	 * @param    bol the global bol option
	 */
	public void setBol (boolean bol)
	{
		m_bol = bol;
	}


	/**
	 * Check if the lexer backup states should be warned.
	 *
	 * @return whether the lexer backup states should be warned.
	 */
	public boolean isWarnBackup ()
	{
		return m_warnBackup;
	}

	/**
	 * Set a global option that warns the occurance of backup lexer states.
	 * By default, this option is false.
	 *
	 * @param    warnBackup the warning option
	 */
	public void setWarnBackup (boolean warnBackup)
	{
		m_warnBackup = warnBackup;
	}

	/**
	 * Check if yywrap function should be called when EOF is encountered.
	 *
	 * @return if yywrap function should be called when EOF is encountered.
	 */
	public boolean isYywrap ()
	{
		return m_yywrap;
	}

	/**
	 * Set if yywrap function should be called when EOF is encountered.
	 *
	 * @param    yywrap if yywrap function should be called when EOF is encountered.
	 */
	public void setYywrap (boolean yywrap)
	{
		m_yywrap = yywrap;
	}
}
