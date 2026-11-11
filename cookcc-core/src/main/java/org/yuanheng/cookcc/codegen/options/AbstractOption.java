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
package org.yuanheng.cookcc.codegen.options;

import org.yuanheng.cookcc.interfaces.OptionHandler;

/**
 * @author Heng Yuan
 */
public class AbstractOption implements OptionHandler
{
	public static String OPTION_ABSTRACT = "-abstract";

	public String getOption ()
	{
		return OPTION_ABSTRACT;
	}

	public boolean requireArguments ()
	{
		return false;
	}

	public void handleOption (String value) throws Exception
	{
	}

	public String toString ()
	{
		return OPTION_ABSTRACT + "\t\t\tMake the output class abstract.";
	}
}
