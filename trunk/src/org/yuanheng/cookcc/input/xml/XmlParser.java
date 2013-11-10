/*
 * Copyright (c) 2008-2013, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *    Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    Neither the name of the Heng Yuan nor the
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
package org.yuanheng.cookcc.input.xml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.yuanheng.cookcc.doc.*;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xerces.internal.xni.*;

import cookxml.core.CookXml;
import cookxml.core.adder.CallFunctionAdder;
import cookxml.core.adder.DefaultAdder;
import cookxml.core.converter.BooleanConverter;
import cookxml.core.creator.DefaultCreator;
import cookxml.core.setter.CallFunctionSetter;
import cookxml.core.setter.DefaultSetter;
import cookxml.core.taglibrary.InheritableTagLibrary;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class XmlParser
{
	private final static InheritableTagLibrary s_tagLibrary;

	static
	{
		// I normally put setters/adders next to their corresponding creators, but
		// for this tutorial purpose, I separate them into four steps instead.

		// Create a TagLibrary
		InheritableTagLibrary tagLibrary = new InheritableTagLibrary ();

		// setup default setter/adder
		tagLibrary.setSetter (null, null, DefaultSetter.getInstance ());
		tagLibrary.setAdder (null, DefaultAdder.getInstance ());

		// converters
		tagLibrary.setConverter (Boolean.class, BooleanConverter.getInstance ());
		tagLibrary.setConverter (boolean.class, BooleanConverter.getInstance ());

		// Tag Creators
		tagLibrary.setCreator ("cookcc", DefaultCreator.getCreator (Document.class));
		tagLibrary.setCreator ("code", new CodeCreator ());
		tagLibrary.setCreator ("tokens", new TokensCreator ());

		tagLibrary.setCreator ("lexer", DefaultCreator.getCreator (LexerDoc.class));
		tagLibrary.addAdder ("cookcc", new CallFunctionAdder ("setLexer", Document.class, LexerDoc.class));
		tagLibrary.addAdder ("lexer", new RuleDocAdder ());
		tagLibrary.addAdder ("lexer", new CallFunctionAdder ("addShortcut", LexerDoc.class, ShortcutDoc.class));
		tagLibrary.setCreator ("shortcut", new ShortcutCreator ());
		tagLibrary.setCreator ("state", new LexerStateCreator ());
		tagLibrary.setCreator ("rule", new RuleCreator ());
		tagLibrary.setSetter ("rule", "state", new CallFunctionSetter ("addStates", RuleDoc.class, String.class));
		tagLibrary.setCreator ("pattern", new PatternCreator ());
		tagLibrary.setCreator ("action", new ActionCreator ());

		tagLibrary.setCreator ("parser", DefaultCreator.getCreator (ParserDoc.class));
		tagLibrary.addAdder ("cookcc", new CallFunctionAdder ("setParser", Document.class, ParserDoc.class));
		tagLibrary.setCreator ("grammar", new GrammarCreator ());
		tagLibrary.setCreator ("type", new TypeCreator ());
		tagLibrary.setCreator ("rhs", new RhsCreator ());
		s_tagLibrary = tagLibrary;
	}

	private static class Parser extends DOMParser
	{
		private EntityResolver m_entityResolver = new EntityResolver ()
		{
			public InputSource resolveEntity (String publicId, String systemId) throws SAXException, IOException
			{
				return new InputSource (getClass ().getClassLoader ().getResourceAsStream ("resources/cookcc.dtd"));
			}
		};
		private XMLLocator m_locator;

		public Parser ()
		{
			try
			{
				setFeature ("http://apache.org/xml/features/dom/defer-node-expansion", false);
				setEntityResolver (m_entityResolver);
			}
			catch (Exception ex)
			{
				ex.printStackTrace ();
			}
		}

		public void startElement (QName element, XMLAttributes attributes, Augmentations augs) throws XNIException
		{
			super.startElement (element, attributes, augs);

			Node node = null;
			try
			{
				node = (Node)this.getProperty ("http://apache.org/xml/properties/dom/current-element-node");
			}
			catch (Exception ex)
			{
				ex.printStackTrace ();
			}
			if (node != null)
				node.setUserData ("line", new Integer (m_locator.getLineNumber ()), null);
		}

		public void startDocument (XMLLocator locator, String encoding, NamespaceContext namespaceContext, Augmentations augs) throws XNIException
		{
			m_locator = locator;
			super.startDocument (locator, encoding, namespaceContext, augs);
			Node node = null;
			try
			{
				node = (Node)this.getProperty ("http://apache.org/xml/properties/dom/current-element-node");
			}
			catch (Exception ex)
			{
			}

			if (node != null)
				node.setUserData ("line", new Integer (locator.getLineNumber ()), null);
		}
	}

	public static Document parse (File file)
	{
		try
		{
			CookXml cookXml = new CookXml (null, s_tagLibrary, (Object)null);
			Parser parser = new Parser ();
			parser.parse (new InputSource (new FileReader (file)));
			return (Document)cookXml.xmlDecode (parser.getDocument ());
		}
		catch (Exception ex)
		{
			ex.printStackTrace ();
		}
		return null;
	}
}
