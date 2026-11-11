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
package org.yuanheng.cookcc.codegen.xml;

/**
 * @author Heng Yuan
 */
class Utils
{
	public static String translate (String inputStr)
	{
		if (inputStr == null)
			return "";
		if (inputStr.indexOf ('<') >= 0 ||
			inputStr.indexOf ("--") >= 0 ||
			inputStr.indexOf ('&') >= 0)
			return "<![CDATA[" + inputStr + "]]>";
		return inputStr;
	}
}
