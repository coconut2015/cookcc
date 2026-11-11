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
 * The <code>?stringresource</code> FreeMarker built-in, which declares string
 * fields holding the data of a large number array.  It works with
 * {@link JavaStringBI} (<code>?javastring</code>) for arrays too long to fit in
 * a single string literal.
 * <p>
 * If the array fits in one literal (at most 16383 bytes in UTF-8), the result
 * is empty and <code>?javastring</code> later generates an inline string.
 * Otherwise the array is split into pieces, and the result declares one field
 * per piece:
 * <pre>
 *		private final static String s_s1 = "...";
 *		private final static String s_s2 = "...";
 * </pre>
 * The expression that joins them (<code>s_s1.toString () + s_s2.toString ()</code>)
 * is remembered for the array, by identity, so that a later
 * <code>?javastring</code> on the same array refers to the fields.  Field
 * numbers continue across arrays, so each field name is unique within the
 * generated class; call {@link #reset} before generating the next class.
 * <p>
 * Characters are escaped the same way as in <code>?javastring</code>: octal for
 * values smaller than 128, since Java cannot handle certain \\u00XX values such
 * as \r \n \" correctly, and \\u notation for the rest.
 *
 * @author Heng Yuan
 */
public class JavaStringResourceBI extends BuiltIn
{
	/** Maps each array, by identity, to the expression that joins its string fields. */
	private final static IdentityHashMap<Object, String> s_stringResources = new IdentityHashMap<Object, String> ();
	/** The number of the last string field declared in the current class. */
	private static int s_stringCounter = 0;

	/** The start of each field declaration; the field number follows. */
	private final static String s_header = "\t\tprivate final static String s_s";
	/** The maximum UTF-8 length, in bytes, of each string field. */
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

	/**
	 * The method returned by <code>?stringresource</code>.  Calling it, with no
	 * arguments, returns the field declarations, or an empty string if none
	 * are needed.
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
			else
			{
				s_stringCounter = counter;
			}

			buffer.append ("\";\n");

			// we put the "s_s1 + s_s2" as the key
			s_stringResources.put (m_seq.getWrappedObject (), combinedString);

			return new SimpleScalar (buffer.toString ());
		}
	}

	/**
	 * Forgets the string resources declared so far and restarts the field
	 * numbering at s_s1.  Call this before generating each class, so that
	 * the output does not depend on what was generated earlier in the same
	 * JVM (for example, several classes in one annotation processing run).
	 */
	public static void reset ()
	{
		s_stringResources.clear ();
		s_stringCounter = 0;
	}

	/**
	 * Gets the expression that joins the string fields declared for an array.
	 *
	 * @param	key	the array, which must be the same instance passed to
	 *			<code>?stringresource</code>.
	 * @return	the expression, such as <code>s_s1.toString () + s_s2.toString ()</code>,
	 *			or null if no fields were declared for the array.
	 */
	public static String getStringResource (Object key)
	{
		return s_stringResources.get (key);
	}
}
