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

import java.util.List;

import freemarker.ext.beans.ArrayModel;
import freemarker.template.*;

/**
 * This code dumps a number array to Java character string.  This is function is
 * necessary because of the fact that Java cannot handle certain \\u00XX values
 * correctly.  Namely \r \n \" and their corresponding values.  Thus it is
 * necessary to use octal representation (which Java does handle correctly)
 * for numbers smaller than 128, and the rest using \\u notation.
 * <p>
 * This is the <code>?javastring</code> FreeMarker built-in.  The target must be
 * a beans-wrapped array of numbers, and the result is a Java expression that
 * creates the equivalent <code>char[]</code>, such as
 * <code>("\001ሴ").toCharArray ()</code>.  Long arrays are split into
 * several string literals joined with <code>+</code>, so that each stays within
 * the class file limit on constant length.
 * <p>
 * If {@link JavaStringResourceBI} already declared string fields for the same
 * array, the expression uses those fields instead of repeating the data.
 *
 * @author Heng Yuan
 */
public class JavaStringBI extends BuiltIn
{
	/** The maximum UTF-8 length, in bytes, of each string literal generated. */
	private final static int MAX_ARRAY_LEN = 16383;

	@SuppressWarnings ("unchecked")
	public static void init ()
	{
		BuiltIn.BUILT_INS_BY_NAME.put ("javastring", new JavaStringBI ());
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

	/**
	 * The method returned by <code>?javastring</code>.  Calling it, with no
	 * arguments, returns the Java expression for the array.
	 */
	private class JavaStringBuilder implements TemplateMethodModelEx
	{
		/** The array of numbers to convert. */
		private final ArrayModel m_seq;

		private JavaStringBuilder (ArrayModel seq)
		{
			super ();
			m_seq = seq;
		}

		@SuppressWarnings ("rawtypes")
		public TemplateModel exec (List args) throws TemplateModelException
		{
			String combinedString = JavaStringResourceBI.getStringResource (m_seq.getWrappedObject ());
			if (combinedString != null)
			{
				return new SimpleScalar ("(" + combinedString + ").toCharArray ()");
			}
			StringBuffer buffer = new StringBuffer ();
			buffer.append ("(\"");
//			buffer.append ('\"');
			int size = m_seq.size ();
			int utf8Size = 0;
			for (int i = 0; i < size; ++i)
			{
				TemplateNumberModel model = (TemplateNumberModel)m_seq.get (i);
				int value = model.getAsNumber ().shortValue () & 0xffff;
				utf8Size += getUTF8Length(value);
				if (utf8Size > MAX_ARRAY_LEN)
				{
					buffer.append ("\" + \"");
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

			buffer.append ("\").toCharArray ()");
//			buffer.append ("\".toCharArray ()");

			return new SimpleScalar (buffer.toString ());
		}
	}
}
