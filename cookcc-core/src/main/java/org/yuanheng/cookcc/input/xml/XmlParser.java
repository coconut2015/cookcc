/*
 * Copyright (c) 2008-2013, Heng Yuan
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
package org.yuanheng.cookcc.input.xml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.*;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.yuanheng.cookcc.doc.*;

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
		tagLibrary.setCreator ("ignore", new IgnoreCreator ());
		tagLibrary.setCreator ("type", new TypeCreator ());
		tagLibrary.setCreator ("rhs", new RhsCreator ());
		s_tagLibrary = tagLibrary;
	}

	private static class Parser extends DOMParser
	{
		private final EntityResolver m_entityResolver = new EntityResolver ()
		{
			@Override
			public InputSource resolveEntity (String publicId, String systemId) throws SAXException, IOException
			{
				return new InputSource (getClass ().getClassLoader ().getResourceAsStream ("cookcc.dtd"));
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

		@Override
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

		@Override
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
