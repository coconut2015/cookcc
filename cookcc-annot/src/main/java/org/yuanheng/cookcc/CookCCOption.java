/*
 * CookCC Copyright (c) 2008-2013, Heng Yuan
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
 * @author Heng Yuan
 * @since 0.3
 */
@Retention (value = RetentionPolicy.SOURCE)
public @interface CookCCOption
{
	/**
	 * Indicates whether or not the generated lexer/parser should support unicode.
	 *
	 * @return	whether or not unicode is supported for the generated lexer/parser.
	 */
	boolean unicode () default false;

	/**
	 * Whether or not uses line mode for the lexer.  Default is false.
	 *
	 * @return	Whether or not uses line mode for the lexer
	 */
	boolean lineMode () default false;

	/**
	 * The default lexer DFA table format.  Available options are "full", "ecs",
	 * and "compressed", in the order of decreasing table size, but increasing
	 * cost of performances.
	 *
	 * @return	the lexer dfa table format.
	 */
	String lexerTable () default "compressed";

	/**
	 * The default parser DFA table format.  Available options are "ecs",
	 * and "compressed", in the order of decreasing table size, but increasing
	 * cost of performances.
	 *
	 * @return	the parser dfa table format.
	 */
	String parserTable () default "compressed";

	/**
	 * The token enum class name shared between the lexer and parser.  If not
	 * specified.  The class name needs to be the full name.
	 * <p>
	 * Due to the specific APT behavior that would throw MirroredTypeException
	 * even when the class is located in the classpath, we have to use String
	 * instead of Class.  See
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5089128
	 *
	 * @return	the token enums shared between the lexer and parser.
	 */
	String tokenClass () default "";

	/**
	 * The starting grammar symbol.  By default the starting grammar symbol is
	 * the LHS of the first Rule annotation.  Use this attribute to set a
	 * different start symbol.
	 *
	 * @return	the starting grammar symbol
	 */
	String start () default "";

	/**
	 * Whether or not warn the backup states in the lexer.  Default is false.
	 *
	 * @return	whether or not warn the backup states in the lexer
	 */
	boolean warnBackup () default false;

	/**
	 * Whether or not CookCC runtime should be used for parser grammar such as
	 * |, *, +, , ? operators.
	 *
	 * @return	CookCC runtime should be used.
	 * @since	0.4
	 */
	boolean rt () default false;
}
