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
package org.yuanheng.cookcc.util;

import org.yuanheng.cookcc.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Heng Yuan
 */
@CookCCOption (lexerTable = "ecs")
public class FileHeaderScanner extends FileHeaderLexer
{
	private StringBuffer m_buffer = new StringBuffer ();

	@Shortcuts (shortcuts = {
		@Shortcut (name = "ws", pattern = "[ \\t]")
	})
	@Lexs (patterns = {
		@Lex (pattern = "{ws}+"),
		@Lex (pattern = "\\n+")
	})
	protected void ignoreWhiteSpace ()
	{
	}

	@Lex (pattern = "[/][/].*\\n")
	protected void scanLineComment ()
	{
		m_buffer.append (yyText ().substring (2));
	}

	@Lex (pattern = "[/][*]+")
	protected void startBlockComment ()
	{
		begin ("BLOCKCOMMENT");
	}

	@Lex (pattern = "^{ws}*[*]", state= "BLOCKCOMMENT")
	protected void ignoreLeadingStar ()
	{
	}

	@Lex (pattern = "{ws}*[*]+[/]", state = "BLOCKCOMMENT")
	protected void endBlockComment ()
	{
		begin ("INITIAL");
	}

	@Lexs (patterns = {
			@Lex (pattern = "{ws}+", state = "BLOCKCOMMENT"),
			@Lex (pattern = "[^ \\t*/]+", state = "BLOCKCOMMENT"),
			@Lex (pattern = ".", state = "BLOCKCOMMENT")
	})
	protected void scanBlockComment ()
	{
		m_buffer.append (yyText ());
	}

	@Lexs (patterns = {
		@Lex (pattern = "^{ws}*[^ \\t]"),
		@Lex (pattern = "."),
		@Lex (pattern = "<<EOF>>"),
		@Lex(pattern = "<<EOF>>", state = "BLOCKCOMMENT")
	})
	protected int doneScanning ()
	{
		return 0;
	}

	public static String getFileHeader (File file) throws IOException
	{
		FileHeaderScanner scanner = new FileHeaderScanner ();
		FileInputStream is = new FileInputStream (file);
		scanner.setInput (is);
		scanner.yyLex ();
		is.close ();
		return scanner.m_buffer.toString ();
	}
}
