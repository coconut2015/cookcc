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

import freemarker.template.*;

/**
 * This piece of code is from http://osdir.com/ml/web.freemarker.devel/2004-04/msg00010.html
 * by Mark van de Veerdonk.
 *
 * @author Heng Yuan
 * @version $Id$
 */
public class HexBI extends BuiltIn
{
	@SuppressWarnings ("unchecked")
	public static void init ()
	{
		BuiltIn.builtins.put ("hex", new HexBI ());
	}

	@Override
	TemplateModel _getAsTemplateModel (Environment env) throws TemplateException
	{
		TemplateModel model = target.getAsTemplateModel (env);
		if (!(model instanceof TemplateNumberModel))
			throw invalidTypeException (model, target, env, "number");
		TemplateNumberModel nModel = (TemplateNumberModel)model;
		return new Hexanizer (nModel.getAsNumber ().intValue ());
	}

	@SuppressWarnings ("rawtypes")
	private class Hexanizer implements TemplateMethodModelEx
	{
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
