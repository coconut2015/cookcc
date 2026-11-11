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
		BuiltIn.BUILT_INS_BY_NAME.put ("stringresource", new JavaStringResourceBI ());
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
	TemplateModel _eval (Environment env) throws TemplateException
	{
		TemplateModel model = target.eval (env);
		if (!(model instanceof ArrayModel))
			throw new UnexpectedTypeException (target, model, "array", new Class[] { ArrayModel.class }, env);
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
