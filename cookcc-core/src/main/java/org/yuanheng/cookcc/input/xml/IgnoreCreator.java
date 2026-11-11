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
import org.yuanheng.cookcc.doc.IgnoreDoc;

import cookxml.core.DecodeEngine;
import cookxml.core.interfaces.Creator;

/**
 * @author	Heng Yuan
 * @since	0.4
 */
class IgnoreCreator implements Creator
{
	@Override
	public Object create (String parentNS, String parentTag, Element elm, Object parentObj, DecodeEngine decodeEngine) throws Exception
	{
		IgnoreDoc ignoreDoc = new IgnoreDoc ();
		Integer lineNum = (Integer)elm.getUserData ("line");
		if (lineNum != null)
			ignoreDoc.setLineNumber (lineNum.intValue ());
		return ignoreDoc;
	}

	@Override
	public Object editFinished (String parentNS, String parentTag, Element elm, Object parentObj, Object obj, DecodeEngine decodeEngine) throws Exception
	{
		return obj;
	}
}
