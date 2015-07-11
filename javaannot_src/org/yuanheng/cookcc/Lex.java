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
package org.yuanheng.cookcc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Use this annotation to mark a function which is called when the pattern
 * specified is matched.
 *
 * @author Heng Yuan
 * @version $Id$
 * @since 0.3
 */
@Retention (value = RetentionPolicy.SOURCE)
public @interface Lex
{
	/**
	 * The lexical pattern to be matched.
	 *
	 * @return	the lexicial pattern
	 */
	String pattern ();
	/**
	 * A list of states where the pattern applies.  Multiple states
	 * can be separated by spaces.
	 * <p>
	 * Default is "INITIAL" which is the starting state for the lexer.
	 *
	 * @return	the state where the pattern applies.
	 */
	String state ()	default "INITIAL";

	/**
	 * Indicate that the pattern is case-insensitive.
	 * <p>
	 * Default is false
	 *
	 * @return	where or not the pattern is case-insensitive.
	 */
	boolean nocase () default false;

	/**
	 * Specify the token to be passed to the parser.  The string
	 * value should correspond to the Enum type marked with {@link CookCCToken}.
	 *
	 * @return	the name of the token.
	 */
	String token () default "";
}
