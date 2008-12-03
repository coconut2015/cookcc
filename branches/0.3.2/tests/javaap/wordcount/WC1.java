import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;

import org.yuanheng.cookcc.*;

@CookCCOption
public class WC1 extends Lexer1
{
	private int m_cc;	// character count
	private int m_wc;	// word count
	private int m_lc;	// line count

	@Shortcuts ( shortcuts = {
		@Shortcut (name="nonws", pattern="[^ \\t\\n]"),
		@Shortcut (name="ws", pattern="[ \\t]")
	})
	@Lex (pattern="{nonws}+", state="INITIAL")
	void matchWord ()
	{
		m_cc += yyLength ();
		++m_wc;
	}

	/**
	 * Match white space characters.
	 */
	@Lex (pattern="{ws}+")
	void matchSpace ()
	{
		m_cc += yyLength ();
	}

	@Lex (pattern = "\\n")
	void matchEOL ()
	{
		m_cc += yyLength ();
		++m_lc;
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
		WC1 wc = new WC1 ();
		if (args.length == 0)
			wc.parse (System.in);
		else
			wc.parse (new FileInputStream (new File (args[0])));
	}
}
