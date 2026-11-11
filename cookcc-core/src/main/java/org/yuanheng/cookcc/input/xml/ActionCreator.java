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
package org.yuanheng.cookcc.input.xml;

import org.w3c.dom.Element;
import org.yuanheng.cookcc.doc.RuleDoc;

import cookxml.core.DecodeEngine;
import cookxml.core.interfaces.Creator;
import cookxml.core.util.TextUtils;

/**
 * @author Heng Yuan
 */
class ActionCreator implements Creator
{
	public Object create (String parentNS, String parentTag, Element elm, Object parentObj, DecodeEngine decodeEngine) throws Exception
	{
		decodeEngine.setDoAdd (false);
		return TextUtils.getText (elm);
	}

	public Object editFinished (String parentNS, String parentTag, Element elm, Object parentObj, Object obj, DecodeEngine decodeEngine) throws Exception
	{
		if (parentObj instanceof RuleDoc)
		{
			((RuleDoc)parentObj).setAction ((String)obj);
		}
		else if (parentObj instanceof GrammarHelper)
		{
			((GrammarHelper)parentObj).getGrammar ().getLastRhs ().setAction ((String)obj);
		}
		return obj;
	}
}
