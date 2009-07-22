import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;

import org.yuanheng.cookcc.*;

@CookCCOption
public class WC2 extends Lexer2
{
	private int m_cc;	// character count
	private int m_wc;	// word count
	private int m_lc;	// line count

	@Shortcuts (shortcuts = {
		@Shortcut (name = "nonws", pattern = "[^ \\t\\n]"),
		@Shortcut (name = "ws", pattern = "[ \\t]"),
		@Shortcut (name = "word", pattern = "{ws}*{nonws}+")
	})
	@Lex (pattern = "{word}{ws}*")
	void matchWord ()
	{
		m_cc += yyLength ();
		++m_wc;
	}

	@Lex (pattern = "{word}{ws}*\\n")
	void matchWordLine ()
	{
		m_cc += yyLength ();
		++m_wc;
		++m_lc;
	}

	/**
	 * Match white space characters.
	 */
	@Lex (pattern = "{ws}+")
	void matchSpace ()
	{
		m_cc += yyLength ();
	}

	@Lex (pattern = "\\n+")
	void matchEOL ()
	{
		m_cc += yyLength ();
		m_lc += yyLength ();
	}

	/**
	 * This function is called when the end of file character (artificially
	 * created) is matched.
	 * <p>
	 * CookCC detects that there is an int (has to be int in Java) return
	 * type and would return this value as the return value from the lexer.
	 *
	 * @return	0
	 */
	@Lex (pattern="<<EOF>>")
	int matchEOF ()
	{
		System.out.println (m_lc + ", " + m_wc + ", " + m_cc);
		return 0;
	}

	public void parse (InputStream is) throws IOException
	{
		setInput (is);
		yyLex ();
	}

	public static void main (String[] args) throws IOException
	{
		WC2 wc = new WC2 ();
		if (args.length == 0)
			wc.parse (System.in);
		else
			wc.parse (new FileInputStream (new File (args[0])));
	}
}
