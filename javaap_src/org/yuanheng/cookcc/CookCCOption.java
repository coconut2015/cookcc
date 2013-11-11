/*
 * CookCC Copyright (c) 2008-2009, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <copyright holder> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <copyright holder> BE LIABLE FOR ANY
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
 * @author Heng Yuan
 * @version $Id$
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
}
