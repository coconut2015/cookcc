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
package org.yuanheng.cookcc.codegen.options;

import org.yuanheng.cookcc.interfaces.OptionParser;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class LexerTableOption implements OptionParser
{
	public static String OPTION_LEXERTABLE = "-lexertable";

	private String m_lexerTable;

	public int handleOption (String[] args, int index) throws Exception
	{
		if (!OPTION_LEXERTABLE.equals (args[index]))
			return 0;
		String table = args[index + 1].toLowerCase ();
		if (!"ecs".equals (table) &&
			!"full".equals (table) &&
			!"compressed".equals (table))
			throw new IllegalArgumentException ("Invalid table choice: " + table);
		m_lexerTable = table;
		return 2;
	}

	public String toString ()
	{
		return OPTION_LEXERTABLE + "\t\t\t\tselect lexer DFA table format.\n" +
			   "\tAvailable formats:\t[ecs, full, compressed]";
	}

	public String getLexerTable ()
	{
		return m_lexerTable;
	}
}
