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
package org.yuanheng.cookcc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.yuanheng.cookcc.interfaces.OptionHandler;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class OptionMap
{
	private final Map<String, OptionHandler> m_handlerMap = new HashMap<String, OptionHandler> ();
	private final LinkedList<OptionHandler> m_handlerList = new LinkedList<OptionHandler> ();

	private final Map<String, String> m_options = new HashMap<String, String> ();

	public void registerOptionHandler (OptionHandler handler)
	{
		m_handlerMap.put (handler.getOption (), handler);
		m_handlerList.add (handler);
	}

	public void addOption (String option) throws Exception
	{
		addOption (option, null);
	}

	public void addOption (String option, String value) throws Exception
	{
		m_options.put (option, value);

		OptionHandler handler = m_handlerMap.get (option);
		if (handler == null)
			return;
		handler.handleOption (value);
	}

	/**
	 * Remove an option.  Depending on how option parser is implemented, it may not
	 * actually change the state of the option.  The CodeGen that handles the options
	 * should try to consult the OptionMap as much as possible.
	 *
	 * @param    option the option to be removed.
	 */
	public void removeOption (String option)
	{
		m_options.remove (option);
	}

	public int handleOption (String[] args, int index) throws Exception
	{
		OptionHandler handler = m_handlerMap.get (args[index]);
		if (handler == null)
			return 0;
		if (!handler.requireArguments ())
		{
			addOption (args[index]);
			return 1;
		}
		if ((index + 1) >= args.length)
			throw new IllegalArgumentException ("Missing argument for option " + args[index]);
		addOption (args[index], args[index + 1]);
		return 2;
	}

	public String toString ()
	{
		StringBuffer buffer = new StringBuffer ();
		for (OptionHandler handler : m_handlerList)
			buffer.append (handler.toString ()).append ('\n');
		return buffer.toString ();
	}

	public boolean hasOption (String option)
	{
		return m_options.containsKey (option);
	}

	public String getArgument (String option)
	{
		return m_options.get (option);
	}

	public String[] getAvailableOptions ()
	{
		return m_handlerMap.keySet ().toArray (new String[m_handlerMap.size ()]);
	}
}
