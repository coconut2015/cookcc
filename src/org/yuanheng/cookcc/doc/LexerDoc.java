/*
 * Copyright (c) 2008, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Heng Yuan nor the
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

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class LexerDoc extends TreeDoc
{
	public final static String INITIAL_STATE = "INITIAL";
	private final HashMap<String, LexerStateDoc> m_stateMap = new HashMap<String, LexerStateDoc> ();
	private final HashMap<String, ShortcutDoc> m_shortcutMap = new HashMap<String, ShortcutDoc> ();

	public LexerStateDoc getLexerState (String stateName)
	{
		if (stateName == null || stateName.length () == 0)
			stateName = INITIAL_STATE;
		LexerStateDoc doc = m_stateMap.get (stateName);
		if (doc == null)
		{
			doc = new LexerStateDoc (stateName);
			m_stateMap.put (stateName, doc);
		}

		return doc;
	}

	public LexerStateDoc[] getLexerStates ()
	{
		return m_stateMap.values ().toArray (new LexerStateDoc[m_stateMap.size ()]);
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
}