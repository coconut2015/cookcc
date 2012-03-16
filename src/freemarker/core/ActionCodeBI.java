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
package freemarker.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class ActionCodeBI extends BuiltIn
{
	@SuppressWarnings ("unchecked")
	public static void init ()
	{
		BuiltIn.builtins.put ("actioncode", new ActionCodeBI ());
	}

	@Override
	TemplateModel _getAsTemplateModel (Environment env) throws TemplateException
	{
		TemplateModel model = target.getAsTemplateModel (env);
		if (!(model instanceof StringModel))
			throw invalidTypeException (model, target, env, "string");
		StringModel seq = (StringModel)model;
		return new ActionCodeBuilder (seq);
	}

	private class ActionCodeBuilder implements TemplateMethodModelEx
	{
		private final StringModel m_str;

		private ActionCodeBuilder (StringModel str)
		{
			super ();
			m_str = str;
		}

		@SuppressWarnings ("rawtypes")
		public TemplateModel exec (List args) throws TemplateModelException
		{
			return new StringArraySequence (parseActionCode (m_str.getAsString ()));
		}
	}

	/**
	 * A very simple utility function that splits user action code based on
	 * $$ and $[0-9]+ deliminators.  This is useful for substitution purposes
	 * in the generated code.
	 * <p/>
	 * The reason why the parser didn't do this step automatically is to allow
	 * customizable deliminator patterns.
	 * <p/>
	 * An example:
	 * <p/>
	 * parseAction ("abc$$def$1hij")
	 * <p/>
	 * would return {"abc", "$", "def", "1", "hij"}.
	 * <p/>
	 * This piece code was written by gloomyturkey on mitbbs.
	 *
	 * @param	input
	 * 			the user action code.
	 * @return	the splitted strings
	 */
	public static String[] parseActionCode (String input)
	{
		ArrayList<String> spliter = new ArrayList<String> ();
		ArrayList<String> content = new ArrayList<String> ();
		Pattern pattern = Pattern.compile ("([$][0-9]+|[$][$])");
		Matcher matcher = pattern.matcher (input);
		int index = 0;
		while (matcher.find ())
		{
			int start = matcher.start ();
			int end = matcher.end ();
			if (start >= index)
			{
				content.add (input.substring (index, start));
			}
			spliter.add (input.substring (start + 1, end));
			index = end;
		}
		if (index < input.length ())
			content.add (input.substring (index));
		String[] ret = new String[spliter.size () + content.size ()];
		for (int i = 0; i < ret.length; ++i)
		{
			if ((i % 2) == 0)
				ret[i] = content.get (i / 2);
			else
				ret[i] = spliter.get (i / 2);
		}
		return ret;
	}
}
