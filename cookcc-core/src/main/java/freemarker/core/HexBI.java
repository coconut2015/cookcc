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

import freemarker.template.*;

/**
 * The <code>?hex</code> FreeMarker built-in, which formats a number in
 * lowercase hexadecimal, padded with leading zeros to a minimum length.
 * For example, <code>${255?hex(4)}</code> produces <code>00ff</code>.
 * Negative numbers are shown in two's complement, as
 * {@link Integer#toHexString} does.
 * <p>
 * This piece of code is from http://osdir.com/ml/web.freemarker.devel/2004-04/msg00010.html
 * by Mark van de Veerdonk.
 *
 * @author Heng Yuan
 */
public class HexBI extends BuiltIn
{
	@SuppressWarnings ("unchecked")
	public static void init ()
	{
		BuiltIn.BUILT_INS_BY_NAME.put ("hex", new HexBI ());
	}

	@Override
	TemplateModel _eval (Environment env) throws TemplateException
	{
		TemplateModel model = target.eval (env);
		if (!(model instanceof TemplateNumberModel))
			throw new UnexpectedTypeException (target, model, "number", new Class[] { TemplateNumberModel.class }, env);
		TemplateNumberModel nModel = (TemplateNumberModel)model;
		return new Hexanizer (nModel.getAsNumber ().intValue ());
	}

	/**
	 * The method returned by <code>?hex</code>.  It takes one argument, the
	 * minimum number of digits, and returns the formatted string.
	 */
	@SuppressWarnings ("rawtypes")
	private class Hexanizer implements TemplateMethodModelEx
	{
		/** The number to format. */
		int value;

		Hexanizer (int value)
		{
			super ();
			this.value = value;
		}

		public TemplateModel exec (List args) throws TemplateModelException
		{
			if (args.size () != 1)
				throw new TemplateModelException ("number?hex(...) requires exactly 1 argument");

			Object obj = args.get (0);
			if (!(obj instanceof TemplateNumberModel))
				throw new TemplateModelException ("Parameter to ?hex(...) must be a number.");

			String strValue = Integer.toHexString (value);
			int minLength = ((TemplateNumberModel)obj).getAsNumber ().intValue ();
			int numLeadingZeros = minLength - strValue.length ();
			String prependStr = "";
			while (numLeadingZeros > 0)
			{
				prependStr += "0";
				numLeadingZeros--;
			}
			return new SimpleScalar (prependStr + strValue);
		}
	}
}
