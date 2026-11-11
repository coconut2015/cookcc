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
 * The <code>?actioncode</code> FreeMarker built-in, which splits a parser
 * action into code and value references so that the template can substitute
 * them.  For example, in a template:
 * <pre>
 * &lt;#list p.action?actioncode() as a&gt;&lt;#if a_index % 2 == 0&gt;${a}&lt;#else&gt;...&lt;/#if&gt;&lt;/#list&gt;
 * </pre>
 * Elements at even indexes are code; elements at odd indexes are the value
 * references with the <code>$</code> removed: <code>"$"</code> for
 * <code>$$</code>, or the position (<code>"1"</code>, <code>"2"</code>, ...)
 * for <code>$1</code>, <code>$2</code>, ....  See {@link #parseActionCode}.
 * <p>
 * The target must be a beans-wrapped string.
 *
 * @author Heng Yuan
 */
public class ActionCodeBI extends BuiltIn
{
	@SuppressWarnings ("unchecked")
	public static void init ()
	{
		BuiltIn.BUILT_INS_BY_NAME.put ("actioncode", new ActionCodeBI ());
	}

	@Override
	TemplateModel _eval (Environment env) throws TemplateException
	{
		TemplateModel model = target.eval (env);
		if (!(model instanceof StringModel))
			throw new UnexpectedTypeException (target, model, "string", new Class[] { StringModel.class }, env);
		StringModel seq = (StringModel)model;
		return new ActionCodeBuilder (seq);
	}

	/**
	 * The method returned by <code>?actioncode</code>.  Calling it, with no
	 * arguments, returns the split action code as a sequence of strings.
	 */
	private class ActionCodeBuilder implements TemplateMethodModelEx
	{
		/** The action code to split. */
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
	 * <p>
	 * The reason why the parser didn't do this step automatically is to allow
	 * customizable deliminator patterns.
	 * <p>
	 * An example:
	 * <p>
	 * parseAction ("abc$$def$1hij")
	 * <p>
	 * would return {"abc", "$", "def", "1", "hij"}.
	 * <p>
	 * This piece code was written by gloomyturkey on mitbbs.
	 *
	 * @param    input the user action code.
	 * @return the splitted strings
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
