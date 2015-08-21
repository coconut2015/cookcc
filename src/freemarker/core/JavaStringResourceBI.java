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
package freemarker.core;

import java.util.IdentityHashMap;
import java.util.List;

import freemarker.ext.beans.ArrayModel;
import freemarker.template.*;

/**
 * This code dumps a number array to Java character string.  This is function is
 * necessary because of the fact that Java cannot handle certain \\u00XX values
 * correctly.  Namely \r \n \" and their corresponding values.  Thus it is
 * necessary to use octal representation (which Java does handle correctly)
 * for numbers smaller than 128, and the rest using \\u notation.
 *
 * @author Heng Yuan
 * @version $Id: JavaStringBI.java 750 2013-11-10 01:00:02Z superduperhengyuan@gmail.com $
 */
public class JavaStringResourceBI extends BuiltIn
{
	private final static IdentityHashMap<Object, String> s_stringResources = new IdentityHashMap<Object, String> ();
	private static int s_stringCounter = 0;

	private final static String s_header = "\t\tprivate final static String s_s";
	private final static int MAX_ARRAY_LEN = 16383;

	@SuppressWarnings ("unchecked")
	public static void init ()
	{
		BuiltIn.builtins.put ("stringresource", new JavaStringResourceBI ());
	}

	private static int getUTF8Length (int value)
	{
		if (value < 0x80)
			return 1;
		if (value < 0x800)
			return 2;
		return 3;
	}

	@Override
	TemplateModel _getAsTemplateModel (Environment env) throws TemplateException
	{
		TemplateModel model = target.getAsTemplateModel (env);
		if (!(model instanceof ArrayModel))
			throw invalidTypeException (model, target, env, "array");
		ArrayModel seq = (ArrayModel)model;
		return new JavaStringBuilder (seq);
	}

	private class JavaStringBuilder implements TemplateMethodModelEx
	{
		private final ArrayModel m_seq;

		private JavaStringBuilder (ArrayModel seq)
		{
			super ();
			m_seq = seq;
		}

		@SuppressWarnings ("rawtypes")
		public TemplateModel exec (List args) throws TemplateModelException
		{
			StringBuffer buffer = new StringBuffer ();
			String combinedString = null;
			int counter = s_stringCounter;
			buffer.append (s_header + (++counter) + " = \"");
			int size = m_seq.size ();
			int utf8Size = 0;
			for (int i = 0; i < size; ++i)
			{
				TemplateNumberModel model = (TemplateNumberModel)m_seq.get (i);
				int value = model.getAsNumber ().shortValue () & 0xffff;
				utf8Size += getUTF8Length(value);
				if (utf8Size > MAX_ARRAY_LEN)
				{
					if (combinedString == null)
						combinedString = "s_s" + counter + ".toString ()";
					buffer.append ("\";\n");
					buffer.append (s_header + (++counter) + " = \"");
					combinedString += " + s_s" + counter + ".toString ()";
					utf8Size = getUTF8Length(value);
				}
				if (value < 128)
				{
					buffer.append ('\\');
					String oct = Integer.toOctalString (value);
					if (oct.length () < 3)
						buffer.append ("000".substring (oct.length ()));
					buffer.append (oct);
				}
				else
				{
					buffer.append ("\\u");
					String hex = Integer.toHexString (value);
					if (hex.length () < 4)
						buffer.append ("0000".substring (hex.length ()));
					buffer.append (hex);
				}
			}

			if ((counter - s_stringCounter) == 1)
			{
				// no need to generate the string resource;
				return new SimpleScalar ("");
			}

			buffer.append ("\";\n");

			// we put the "s_s1 + s_s2" as the key
			s_stringResources.put (m_seq.getWrappedObject (), combinedString);

			return new SimpleScalar (buffer.toString ());
		}
	}

	public static String getStringResource (Object key)
	{
		return s_stringResources.get (key);
	}
}
