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
 * @version $Id$
 */
public class JavaStringBI extends BuiltIn
{
	private final static int MAX_ARRAY_LEN = 32768;

	@SuppressWarnings ("unchecked")
	public static void init ()
	{
		BuiltIn.builtins.put ("javastring", new JavaStringBI ());
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
			buffer.append ("(\"");
//			buffer.append ('\"');
			int size = m_seq.size ();
			for (int i = 0; i < size; ++i)
			{
				if (i % MAX_ARRAY_LEN == 0 && i > 0)
					buffer.append ("\" + \"");
				TemplateNumberModel model = (TemplateNumberModel)m_seq.get (i);
				int value = model.getAsNumber ().shortValue () & 0xffff;
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
