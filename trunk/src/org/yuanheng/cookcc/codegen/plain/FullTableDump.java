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
package org.yuanheng.cookcc.codegen.plain;

import java.io.OutputStream;
import java.io.PrintWriter;

import org.yuanheng.cookcc.OptionParser;
import org.yuanheng.cookcc.codegen.interfaces.CodeGen;
import org.yuanheng.cookcc.dfa.DFATable;
import org.yuanheng.cookcc.doc.Document;
import org.yuanheng.cookcc.lexer.ECS;
import org.yuanheng.cookcc.lexer.Lexer;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class FullTableDump implements CodeGen
{
	private void printArray (char[] array, PrintWriter p)
	{
		p.print (" {");
		for (int i = 0; i < array.length; ++i)
		{
			if ((i % 10) == 0 && i > 0)
				p.print ("\n  ");
			p.printf ("%6d", (int)array[i]);
			//p.print ("\t" + (int)array[i]);
		}
		p.print (" }");
	}

	private void generateLexerOutput (Document doc, PrintWriter p)
	{
		Lexer lexer = Lexer.getLexer (doc);
		if (lexer == null)
			return;

		DFATable dfa = lexer.getDFA ();
		ECS ecs = lexer.getECS ();
		int[] groups = ecs.getGroups ();

		int size = dfa.size ();
		p.println ("DFA states: " + size);

		char[] array = new char[lexer.getCCL ().MAX_SYMBOL + 1];
		p.println ("dfa = ");
		p.println ("{");
		for (int i = 0; i < size; ++i)
		{
			char[] states = dfa.getRow (i).getStates ();
			for (int j = 0; j < array.length; ++j)
				array[j] = states[groups[j]];
			if (i > 0)
				p.println (",");
			printArray (array, p);
		}
		p.println ();
		p.println ("}");

		p.println ();
		p.println (lexer);
	}

	public void generateOutput (Document doc, OutputStream os)
	{
		PrintWriter p = new PrintWriter (os);
		generateLexerOutput (doc, p);
		p.flush ();
	}

	public OptionParser[] getOptionParsers ()
	{
		return new OptionParser[0];
	}
}
