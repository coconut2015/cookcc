/*
 * Copyright (c) 2008, Heng Yuan
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
