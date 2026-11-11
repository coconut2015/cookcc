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
 * Use this interface to mark a function which is called when
 *
 * @author Heng Yuan
 * @since 0.3
 */
@Retention (value = RetentionPolicy.SOURCE)
public @interface Rule
{
	/**
	 * The left hand non-terminal of the rule.
	 *
	 * @return	the name of the non-terminal on the LHS.
	 */
	String lhs ();
	/**
	 * The production of the rule.
	 *
	 * @return	the production of the rule.
	 */
	String rhs ();

	/**
	 * Specify which RHS production would be passed to the function.
	 * The index values start from 1.
	 *
	 * @return	an array of indices separated by spaces.
	 */
	String args () default "";

	/**
	 * Specify the rule precedence using the precedence of a particular token.
	 *
	 * @return	the token
	 */
	String precedence () default "";
}
