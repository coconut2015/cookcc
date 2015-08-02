import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;

import org.yuanheng.cookcc.*;

@CookCCOption
public class PushInput extends Lexer
{
	@Shortcut (name="optws", pattern="[ \\t]*")
	@Lex (pattern="^{optws}#{optws}include{optws}", state="INITIAL")
	void matchInclude ()
	{
		begin ("INCLUDE");
	}

	@Lex (pattern=".|\\n", state="INITIAL")
	void matchChar ()
	{
		echo ();
	}

	@Lex (pattern="<<EOF>>", state="INITIAL")
	int matchEOF ()
	{
		System.out.println ("[[EOF]]");
		return 0;
	}

	@Lex (pattern="[^\\r\\n]+", state="INCLUDE")
	void doInclude () throws IOException
	{
		yyPushInput (new FileInputStream (yyText ()));
		begin ("INITIAL");
	}

	@Lexs (patterns = {
		@Lex (pattern=".|\\n", state="INCLUDE"),
		@Lex (pattern="<<EOF>>", state="INCLUDE")
	})
	void errInclude () throws IOException
	{
		throw new IOException ("missing include file name");
	}

	protected boolean yyWrap ()
	{
		if (yyInputStackSize () > 0)
		{
			yyPopInput ();
			return false;
		}
		return true;
	}

	public void parse (InputStream is) throws IOException
	{
		setInput (is);
		yyLex ();
	}

	public static void main (String[] args) throws IOException
	{
		PushInput pi = new PushInput ();
		if (args.length == 0)
			pi.parse (System.in);
		else
			pi.parse (new FileInputStream (new File (args[0])));
	}
}
