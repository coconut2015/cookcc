/*
 * Copyright (c) 2008-2013, Heng Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yuanheng.cookcc.doc;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Heng Yuan
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
