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
package org.yuanheng.cookcc.input.javaap;

import java.util.ArrayList;

import org.yuanheng.cookcc.*;
import org.yuanheng.cookcc.doc.*;

import com.sun.mirror.declaration.*;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.SourcePosition;

/**
 * @author Heng Yuan
 * @version $Id$
 */
class ClassVisitor implements DeclarationVisitor
{
	private final static String PROP_OUTPUT = "outputClass";
	private final static String PROP_INPUT = "inputClass";
	private final static String PROP_TOKEN = "tokenClass";
	private final static String PROP_PUBLIC = "publicClass";

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
		if (docComment == null)
			return "";
		String[] lines = docComment.split ("\n");
		StringBuffer buffer = new StringBuffer ();
		buffer.append ("/*\n");
		for (String line : lines)
			buffer.append (" *").append (line).append ("\n");
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

	/** the current document being worked on */
	private final Document m_doc = new Document ();
	private final CookCCVisitor m_parent;

	ClassVisitor (CookCCVisitor parent)
	{
		m_parent = parent;
	}

	private LexerDoc getLexer ()
	{
		LexerDoc lexer = m_doc.getLexer ();
		if (lexer == null)
		{
			lexer = new LexerDoc ();
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
			m_doc.setParser (parser);
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

	private void parseLexs (Lexs lexs, MethodDeclaration method)
	{
		if (lexs == null)
			return;

		for (Lex lex : lexs.patterns ())
			parseLex (lex, method);
	}

	private void parseLex (Lex lex, MethodDeclaration method)
	{
		if (lex == null)
			return;

		RuleDoc rule = new RuleDoc (getLexer ());

		SourcePosition pos = method.getPosition ();
		if (pos != null)
			rule.setLineNumber (pos.line ());

		rule.setAction (getLexAction (method, lex.token ()));

		PatternDoc pattern = new PatternDoc ();
		pattern.setPattern (lex.pattern ());

		rule.addPattern (pattern);
		rule.addStates (lex.state ());
	}

	private void parseRules (Rules rules, MethodDeclaration method)
	{
		if (rules == null)
			return;

		for (Rule rule : rules.rules ())
			parseRule (rule, method);
	}

	private void parseRule (Rule rule, MethodDeclaration method)
	{
		if (rule == null)
			return;

		GrammarDoc grammar = getParser ().getGrammar (rule.lhs ());
		RhsDoc rhs = new RhsDoc ();
		rhs.setTerms (rule.rhs ());
		String precedence = rule.precedence ().trim ();
		if (precedence.length () > 0)
			rhs.setPrecedence (precedence);
		String action = getParseAction (method, rule.args ());
		rhs.setAction (action);
		grammar.addRhs (rhs);
	}

	private String getLexAction (MethodDeclaration method, String token)
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
		boolean callSetValue = false;
		if (!"void".equals (returnType))
		{
			buffer.append ("yySetValue (");
			callSetValue = true;
		}

		buffer.append (THIS_STR).append (method.getSimpleName ()).append (" ()");
		if (callSetValue)
			buffer.append (")");
		buffer.append (";");

		if ("$".equals (token))
			buffer.append (" return 0;");
		else
			buffer.append (" return ").append (token).append (";");
		return buffer.toString ();
	}

	private String getParseAction (MethodDeclaration method, String args)
	{
		if (args == null)
			args = "";
		StringBuffer buffer = new StringBuffer ();

		String returnType = method.getReturnType ().toString ();
		boolean callSetValue = false;
		if ("int".equals (returnType))
			buffer.append ("return ");
		else if (!"void".equals (returnType))
		{
			buffer.append ("yySetValue (");
			callSetValue = true;
		}

		buffer.append (THIS_STR).append (method.getSimpleName ()).append (" (");

		ParameterDeclaration[] params = method.getParameters ().toArray (new ParameterDeclaration[method.getParameters ().size ()]);

		int[] argv = getArgs (args);
		for (int i = 0; i < argv.length; ++i)
		{
			if (i >= params.length)
				throw new IllegalArgumentException ("Method " + method  + " does not have so many arguments for " + args);
			if (i > 0)
				buffer.append (", ");
			int v = argv[i];
			String cl = params[i].getType ().toString ();
			buffer.append ("(").append (cl).append (")").append ("yyGetValue (").append (v).append (")");
		}
		buffer.append (")");

		if (callSetValue)
			buffer.append (")");

		buffer.append (";");
		return buffer.toString ();
	}

	public void visitDeclaration (Declaration declaration)
	{
	}

	public void visitPackageDeclaration (PackageDeclaration packageDeclaration)
	{
	}

	public void visitMemberDeclaration (MemberDeclaration memberDeclaration)
	{
	}

	public void visitTypeDeclaration (TypeDeclaration typeDeclaration)
	{
	}

	public void visitClassDeclaration (ClassDeclaration classDeclaration)
	{
		CookCCOption option = classDeclaration.getAnnotation (CookCCOption.class);
		if (option == null)
			return;

		m_doc.setMain (false);
		String inputClass = classDeclaration.getQualifiedName ();
		m_doc.setProperty (PROP_INPUT, inputClass);
		ClassDeclaration superClass = classDeclaration.getSuperclass ().getDeclaration ();
		m_doc.setProperty (PROP_OUTPUT, superClass.getQualifiedName ());

		if (superClass.getModifiers ().contains (Modifier.PUBLIC))
			m_doc.setProperty (PROP_PUBLIC, Boolean.TRUE);

		m_doc.setUnicode (option.unicode ());

		String tokenClass = option.tokenClass ();
		if (tokenClass != null && (tokenClass = tokenClass.trim ()).length () != 0)
			m_doc.setProperty (PROP_TOKEN, tokenClass);

//		m_doc.addCode ("fileheader", generateFileHeader (superClass.getPackage ().getDocComment ()));
		// try to get the file header and class header
		m_doc.addCode ("classheader", generateClassHeader (superClass.getDocComment ()));

		m_parent.addDocument (classDeclaration.getQualifiedName (), m_doc);
	}

	public void visitEnumDeclaration (EnumDeclaration enumDeclaration)
	{
	}

	public void visitInterfaceDeclaration (InterfaceDeclaration interfaceDeclaration)
	{
	}

	public void visitAnnotationTypeDeclaration (AnnotationTypeDeclaration annotationTypeDeclaration)
	{
	}

	public void visitFieldDeclaration (FieldDeclaration fieldDeclaration)
	{
	}

	public void visitEnumConstantDeclaration (EnumConstantDeclaration enumConstantDeclaration)
	{
	}

	public void visitExecutableDeclaration (ExecutableDeclaration executableDeclaration)
	{
	}

	public void visitConstructorDeclaration (ConstructorDeclaration constructorDeclaration)
	{
	}

	public void visitMethodDeclaration (MethodDeclaration method)
	{
		parseLexs (method.getAnnotation (Lexs.class), method);
		parseLex (method.getAnnotation (Lex.class), method);
		parseShortcuts (method.getAnnotation (Shortcuts.class));
		parseShortcut (method.getAnnotation (Shortcut.class));
		parseRules (method.getAnnotation (Rules.class), method);
		parseRule (method.getAnnotation (Rule.class), method);
	}

	public void visitAnnotationTypeElementDeclaration (AnnotationTypeElementDeclaration annotationTypeElementDeclaration)
	{
	}

	public void visitParameterDeclaration (ParameterDeclaration parameterDeclaration)
	{
	}

	public void visitTypeParameterDeclaration (TypeParameterDeclaration typeParameterDeclaration)
	{
	}
}