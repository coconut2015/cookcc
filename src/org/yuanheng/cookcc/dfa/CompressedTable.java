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
package org.yuanheng.cookcc.dfa;

import org.yuanheng.cookcc.lexer.Lexer;

/**
 * Perform table compression
 *
 * @author Heng Yuan
 * @version $Id$
 */
public class CompressedTable
{
	private final Lexer m_lexer;
	private boolean m_computed;

	private short[] m_next;
	private short[] m_check;
	private short[] m_base;
	private short[] m_default;

	public CompressedTable (Lexer lexer)
	{
		m_lexer = lexer;
	}

	private void compute ()
	{
		if (m_computed)
			return;
		m_computed = true;
		TableCompressor compressor = new TableCompressor (m_lexer.getDFA ());

		compressor.compute ();

		m_next = compressor.getNext ();
		m_check = compressor.getCheck ();
		m_base = compressor.getBase ();
		m_default = compressor.getDefault ();
	}

	public int[] getECS ()
	{
		return m_lexer.getECS ().getGroups ().clone ();
	}

	public short[] getNext ()
	{
		compute ();
		return m_next;
	}

	public short[] getCheck ()
	{
		compute ();
		return m_check;
	}

	public short[] getBase ()
	{
		compute ();
		return m_base;
	}

	public short[] getDefault ()
	{
		compute ();
		return m_default;
	}
}
