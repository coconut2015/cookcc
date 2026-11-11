/*
 * Copyright (c) 2008, Heng Yuan
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
package org.yuanheng.cookcc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Use this annotation to mark a function which is called when the pattern
 * specified is matched.
 *
 * @author Heng Yuan
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
