/*
 * CookCC Copyright (c) 2008-2009, Heng Yuan
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

import java.util.HashMap;
import java.io.IOException;
import java.io.FileInputStream;

import org.yuanheng.cookcc.*;

/**
 * @author Heng Yuan
 */
@CookCCOption (start = "B")
public class Foo extends Parser
{
	@CookCCToken
	static enum Token
	{
		C;
	}

	@Lex (pattern="C", token="C")
	void scanC ()
	{
	}

	@Lex (pattern=".|\\n")
	void scanIgnore ()
	{
	}

	@Lex (pattern="<<EOF>>")
	int scanEof ()
	{
		return 0;
	}

	@Rule (lhs = "A", rhs = "B C")
	void parseA ()
	{
		System.out.println ("A");
	}

	@Rule (lhs = "B", rhs = "C")
	void parseB ()
	{
		System.out.println ("B");
	}

	public static void main (String[] args) throws IOException
	{
		Foo foo = new Foo ();
		if (args.length > 0)
			foo.setInput (new FileInputStream (args[0]));

		if (foo.yyParse () > 0)
		{
			System.err.println ("Calculator: errors in input.");
			System.exit (1);
		}
	}
}
