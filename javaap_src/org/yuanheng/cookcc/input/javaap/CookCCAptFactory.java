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
package org.yuanheng.cookcc.input.javaap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/**
 * @author Heng Yuan
 * @version $Id$
 */
public class CookCCAptFactory implements AnnotationProcessorFactory
{
	private final static Collection<String> s_supportedOptions = Collections.emptySet ();
	private final static Collection<String> s_supportedAnnotationTypes = Collections.unmodifiableCollection (Arrays.asList ("org.yuanheng.cookcc.*"));

//	static
//	{
//		// compute all possible options across all output languages
//
//		HashSet<String> options = new HashSet<String> ();
//
//		addOptions (options, Main.getOptions ());
//
//		for (String lang : Main.getLanguages ())
//		{
//			try
//			{
//				CodeGen codeGen = Main.getCodeGen (lang);
//				addOptions (options, codeGen.getOptions ());
//			}
//			catch (Throwable t)
//			{
//			}
//		}
//		s_supportedOptions = options;
//	}
//
//	private static void addOptions (HashSet<String> optionSet, OptionMap optionMap)
//	{
//		for (String option : optionMap.getAvailableOptions ())
//			optionSet.add ("-A" + option.substring (1));
//	}

	public Collection<String> supportedOptions ()
	{
		return s_supportedOptions;
	}

	public Collection<String> supportedAnnotationTypes ()
	{
		return s_supportedAnnotationTypes;
	}

	public AnnotationProcessor getProcessorFor (Set<AnnotationTypeDeclaration> annotationTypeDeclarations, AnnotationProcessorEnvironment env)
	{
		try
		{
			return new CookCCProcessor (env);
		}
		catch (Exception ex)
		{
			throw new IllegalArgumentException ("Invalid CookCC options");
		}
	}
}
