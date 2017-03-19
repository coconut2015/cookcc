/*
 * CookCC Copyright (c) 2008-2009, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <copyright holder> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <copyright holder> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.yuanheng.cookcc.input.ap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;

import org.yuanheng.cookcc.*;
import org.yuanheng.cookcc.doc.*;
import org.yuanheng.cookcc.util.FileHeaderScanner;

/**
 * @author Heng Yuan
 */
class ClassVisitor
{
	private final static String PROP_OUTPUT = "outputClass";
	private final static String PROP_INPUT = "inputClass";
	private final static String PROP_TOKEN = "tokenClass";
	private final static String PROP_PUBLIC = "publicClass";
	private final static String PROP_SUPPRESSING_UNCHECK_WARNING = "SuppressUnCheckWarning";

	private static String THIS_STR = "m_this.";

	static String getOutputClass (Document doc)
	{
		return (String)doc.getProperty (PROP_OUTPUT);
	}

	static String getInputClass (Document doc)
	{
		return (String)doc.getProperty (PROP_INPUT);
	}

	static String getTokenClass (Document doc)
	{
		return (String)doc.getProperty (PROP_TOKEN);
	}

	static boolean isPublic (Document doc)
	{
		return doc.getProperty (PROP_PUBLIC) == Boolean.TRUE;
	}

	static int[] getArgs (String args)
	{
		String[] argv = args.split ("[ \\t,]");
		ArrayList<Integer> list = new ArrayList<Integer> ();
		for (String s : argv)
		{
			if (s == null || s.length () == 0)
				continue;
			list.add (Integer.parseInt (s));
		}
		int[] returnArgv = new int[list.size ()];
		for (int i = 0; i < returnArgv.length; ++i)
			returnArgv[i] = list.get (i);
		return returnArgv;
	}

	static String generateFileHeader (String docComment)
	{
		if (docComment == null || docComment.length () == 0)
			return "";
		String[] lines = docComment.split ("\n");
		if (lines.length == 0)
			return "";
		StringBuffer buffer = new StringBuffer ();
		boolean firstLine = true;
		for (String line : lines)
		{
			if (firstLine)
			{
				buffer.append ("/*");
				firstLine = false;
			}
			else
				buffer.append (" *");
			buffer.append (line).append ("\n");
		}
		buffer.append (" */");
		return buffer.toString ();
	}

	static String generateClassHeader (String docComment)
	{
		if (docComment == null)
			return "";
		String[] lines = docComment.split ("\n");
		StringBuffer buffer = new StringBuffer ();
		buffer.append ("/**\n");
		for (String line : lines)
			buffer.append (" *").append (line).append ("\n");
		buffer.append (" */");
		return buffer.toString ();
	}

	private long[] getAnnotationArrayLineNumbers (ExecutableElement method, String className, String attr)
	{
		for (AnnotationMirror mirror : method.getAnnotationMirrors ())
		{
			Map<? extends ExecutableElement, ? extends AnnotationValue> map = mirror.getElementValues ();
			for (ExecutableElement key : map.keySet ())
			{
				if (!attr.equals (key.getSimpleName ()))
					continue;
				@SuppressWarnings ("unchecked")
				List<? extends AnnotationValue> c = (List<? extends AnnotationValue>)map.get (key).getValue ();
				if (c == null)
					return null;
				long[] returnVal = new long[c.size ()];
				int i = 0;
				for (AnnotationValue v : c)
				{
					returnVal[i++] = m_parent.getLineNumber ((Element)v);
				}
				return returnVal;
			}
		}
		return null;
	}

	/** the current document being worked on */
	private final Document m_doc = new Document ();
	private String m_start;
	private final CookCCProcessor m_parent;
	private CookCCOption m_option;

	ClassVisitor (CookCCProcessor parent)
	{
		m_parent = parent;
	}

	private LexerDoc getLexer ()
	{
		LexerDoc lexer = m_doc.getLexer ();
		if (lexer == null)
		{
			lexer = new LexerDoc ();
			lexer.setLineMode (m_option.lineMode ());
			lexer.setTable (m_option.lexerTable ());
			lexer.setWarnBackup (m_option.warnBackup ());
			lexer.setYywrap (true);
			m_doc.setLexer (lexer);
		}
		return lexer;
	}

	private ParserDoc getParser ()
	{
		ParserDoc parser = m_doc.getParser ();
		if (parser == null)
		{
			parser = new ParserDoc ();
			parser.setTable (m_option.parserTable ());
			m_doc.setParser (parser);
			parser.setStart (m_start);
		}
		return parser;
	}

	private void parseShortcuts (Shortcuts shortcuts)
	{
		if (shortcuts == null)
			return;

		for (Shortcut shortcut : shortcuts.shortcuts ())
			parseShortcut (shortcut);
	}

	private void parseShortcut (Shortcut shortcut)
	{
		if (shortcut == null)
			return;

		ShortcutDoc shortcutDoc = new ShortcutDoc ();
		shortcutDoc.setName (shortcut.name ());
		shortcutDoc.setPattern (shortcut.pattern ());
		getLexer ().addShortcut (shortcutDoc);
	}

	private void parseLexs (Lexs lexs, ExecutableElement method)
	{
		if (lexs == null)
			return;

		long[] pos = getAnnotationArrayLineNumbers (method, Lexs.class.getName (), "patterns");
		int i = 0;
		for (Lex lex : lexs.patterns ())
			parseLex (lex, method, pos == null ? 0 : pos[i++]);
	}

	private void parseLex (Lex lex, ExecutableElement method, long lineNumber)
	{
		if (lex == null)
			return;

		RuleDoc rule = new RuleDoc (getLexer ());

		rule.setAction (getLexAction (method, lex.token ()));

		PatternDoc pattern = new PatternDoc (false);
		pattern.setPattern (lex.pattern ());
		if (lineNumber < 0)
			pattern.setLineNumber (m_parent.getLineNumber (method));
		else
			pattern.setLineNumber (lineNumber);

		rule.addPattern (pattern);
		rule.addStates (lex.state ());
	}

	private void parseTreeRule (ExecutableElement method)
	{
		TreeRule treeRule = method.getAnnotation (TreeRule.class);
		if (treeRule == null)
			return;

		//String grammar = m_parent.getJavaDoc (method);
	}

	private void parseIgnore (Ignore ignore, ExecutableElement method)
	{
		if (ignore == null)
			return;

		IgnoreDoc ignoreDoc = new IgnoreDoc ();
		ignoreDoc.setList (ignore.list ());
		ignoreDoc.setLineNumber (m_parent.getLineNumber (method));
		if (ignore.capture () != null)
		{
			String capture = ignore.capture ().trim ();
			if (capture.length () > 0)
				ignoreDoc.setCapture (capture);
		}
		getParser ().add (ignoreDoc);
	}

	private void parseRules (Rules rules, ExecutableElement method)
	{
		if (rules == null)
			return;

		long[] pos = getAnnotationArrayLineNumbers (method, Rules.class.getName (), "rules");
		int i = 0;
		for (Rule rule : rules.rules ())
			parseRule (rule, method, pos == null ? 0 : pos[i++]);
	}

	private void parseRule (Rule rule, ExecutableElement method, long lineNumber)
	{
		if (rule == null)
			return;

		GrammarDoc grammar = getParser ().getGrammar (rule.lhs ());
		RhsDoc rhs = new RhsDoc ();
		rhs.setTerms (rule.rhs ());
		if (lineNumber < 0)
			rhs.setLineNumber (m_parent.getLineNumber (method));
		else
			rhs.setLineNumber (lineNumber);
		String precedence = rule.precedence ().trim ();
		if (precedence.length () > 0)
			rhs.setPrecedence (precedence);
		String action = getParseAction (method, rule.args ());
		rhs.setAction (action);
		grammar.addRhs (rhs);
	}

	private String getLexAction (ExecutableElement method, String token)
	{
		StringBuffer buffer = new StringBuffer ();

		if (token == null || (token = token.trim ()).length () == 0)
		{
			if (!"void".equals (method.getReturnType ().toString ()))
				buffer.append ("return ");
			buffer.append (THIS_STR).append (method.getSimpleName ()).append (" ();");
			return buffer.toString ();
		}

		String returnType = method.getReturnType ().toString ();
		if (!"void".equals (returnType))
			buffer.append ("_yyValue = ");

		buffer.append (THIS_STR).append (method.getSimpleName ()).append (" ()");
		buffer.append (";");

		if ("$".equals (token))
			buffer.append (" return 0;  // token = $");
		else if ("error".equals (token))
			buffer.append (" return 1;  // token = error");
		else
			buffer.append (" return ").append (token).append (";");
		return buffer.toString ();
	}

	private String getParseAction (ExecutableElement method, String args)
	{
		if (args == null)
			args = "";
		StringBuffer buffer = new StringBuffer ();

		String returnType = method.getReturnType ().toString ();
		if ("int".equals (returnType))
			buffer.append ("return ");
		else if (!"void".equals (returnType))
		buffer.append ("_yyValue = ");

		buffer.append (THIS_STR).append (method.getSimpleName ()).append (" (");

		VariableElement[] params = method.getParameters ().toArray (new VariableElement[method.getParameters ().size ()]);

		int[] argv = getArgs (args);
		// user did not specify any arguments, then just assume that the list is the same as the arguments, in order.
		if (argv.length == 0 && params.length > 0)
		{
			argv = new int[params.length];
			for (int i = 0; i < argv.length; ++i)
				argv[i] = i + 1;
		}

		if (argv.length != params.length)
			throw new IllegalArgumentException ("Method " + method + " does not have the same number of arguments (" + argv.length + " vs " + params.length + ") as specified.");

		for (int i = 0; i < argv.length; ++i)
		{
			if (i > 0)
				buffer.append (", ");
			int v = argv[i];
			String cl = params[i].asType ().toString ();
			if (!"java.lang.Object".equals (cl))
			{
				buffer.append ("(").append (cl).append (")");
				if (cl.indexOf ('<') > 0)
				{
					getParser ().setProperty (PROP_SUPPRESSING_UNCHECK_WARNING, Boolean.TRUE);
				}
			}
			buffer.append ("yyGetValue (").append (v).append (")");
		}
		buffer.append (")");

		buffer.append (";");
		return buffer.toString ();
	}

	public void visit (CookCCOption option, TypeElement typeElem)
	{
		m_option = option;
		m_parent.log (m_option + "\n" + typeElem + " {");

		m_doc.setMain (false);
		String inputClass = typeElem.getQualifiedName ().toString ();
		m_doc.setProperty (PROP_INPUT, inputClass);
		TypeElement superClass = (TypeElement)((DeclaredType)typeElem.getSuperclass ()).asElement ();
		m_doc.setProperty (PROP_OUTPUT, superClass.getQualifiedName ().toString ());

		if (superClass.getModifiers ().contains (Modifier.PUBLIC))
			m_doc.setProperty (PROP_PUBLIC, Boolean.TRUE);

		m_doc.setUnicode (option.unicode ());
		m_doc.setRT (option.rt ());
		m_start = option.start ();

		String tokenClass = option.tokenClass ();
		if (tokenClass != null && (tokenClass = tokenClass.trim ()).length () != 0)
			m_doc.setProperty (PROP_TOKEN, tokenClass);

		// try to get the file header
		try
		{
			File file = m_parent.getFile (superClass);
			m_doc.addCode ("fileheader", generateFileHeader (FileHeaderScanner.getFileHeader (file)));
		}
		catch (Exception ex)
		{
		}
		// try to get the class header
		m_doc.addCode ("classheader", generateClassHeader (m_parent.getJavaDoc (superClass)));

		// now we can scan the enclosing elements
		for (Element childElem : typeElem.getEnclosedElements ())
		{
			if (childElem.getKind () == ElementKind.ENUM)
			{
				if (m_doc.getProperty (PROP_TOKEN) == null)
				{
					m_parent.log ("Associating " + childElem + " with " + typeElem);
					m_doc.setProperty (PROP_TOKEN, ((TypeElement)childElem).getQualifiedName ().toString ());
					m_parent.visit (childElem);
				}
			}
			else if (childElem.getKind () == ElementKind.CLASS)
			{
				m_parent.visit (childElem);
			}
			if (childElem.getKind () == ElementKind.METHOD)
			{
				visitMethod ((ExecutableElement)childElem);
			}
		}
		m_parent.log ("}");
	}

	public void visitMethod (ExecutableElement method)
	{
		parseLexs (method.getAnnotation (Lexs.class), method);
		parseLex (method.getAnnotation (Lex.class), method, -1);
		parseShortcuts (method.getAnnotation (Shortcuts.class));
		parseShortcut (method.getAnnotation (Shortcut.class));
		parseTreeRule (method);
		parseIgnore (method.getAnnotation (Ignore.class), method);
		parseRules (method.getAnnotation (Rules.class), method);
		parseRule (method.getAnnotation (Rule.class), method, -1);
	}

	public Document getDoc ()
	{
		return m_doc;
	}
}
