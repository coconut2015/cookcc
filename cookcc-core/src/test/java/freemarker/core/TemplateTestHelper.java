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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Renders small templates with the CookCC built-ins registered, using the
 * same object wrapper as {@link org.yuanheng.cookcc.codegen.TemplatedCodeGen}.
 * The beans wrapper matters: the built-ins check for its model classes
 * (StringModel, ArrayModel, BeanModel, MapModel).
 *
 * @author Heng Yuan
 */
class TemplateTestHelper
{
	private static final Configuration s_configuration;

	static
	{
		HexBI.init ();
		JavaStringResourceBI.init ();
		JavaStringBI.init ();
		ActionCodeBI.init ();
		TypeBI.init ();

		@SuppressWarnings ("deprecation")
		Configuration cfg = new Configuration ();
		cfg.setObjectWrapper (ObjectWrapper.BEANS_WRAPPER);
		cfg.setTemplateExceptionHandler (TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions (false);
		s_configuration = cfg;
	}

	private TemplateTestHelper ()
	{
	}

	/**
	 * Builds a data model from alternating name / value pairs.
	 */
	static Map<String, Object> model (Object... nameValues)
	{
		Map<String, Object> map = new HashMap<String, Object> ();
		for (int i = 0; i < nameValues.length; i += 2)
			map.put ((String)nameValues[i], nameValues[i + 1]);
		return map;
	}

	static String render (String source, Map<String, Object> model) throws Exception
	{
		Template template = new Template ("test", new StringReader (source), s_configuration);
		StringWriter writer = new StringWriter ();
		template.process (model, writer);
		return writer.toString ();
	}

	/**
	 * Renders the template, expecting it to fail, and returns the error message.
	 */
	static String renderError (String source, Map<String, Object> model) throws Exception
	{
		try
		{
			render (source, model);
		}
		catch (TemplateException ex)
		{
			return ex.getMessage ();
		}
		Assert.fail ("Expected the template to fail: " + source);
		return null;
	}

	static void assertContains (String expected, String actual)
	{
		Assert.assertTrue ("Expected \"" + expected + "\" in: " + actual, actual.contains (expected));
	}
}
