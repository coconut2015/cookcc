/*
 * Copyright (c) 2015, Heng Yuan
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
 * This annotation is used to contain the grammar that needs to has its Abstract Syntax
 * Tree node classes automatically generated.
 * <p>
 * The grammar is specified as a JavaDoc for the method the annotation is applied to, since
 * this is the simplest way of specifying a large amount of multi-line text in Java without
 * having to go through complicated escaping and formatting.
 * <p>
 * There is no specific requirement of where this annotation is put.  Normally, it should
 * be either put after the first {@link Rule} annotation.  If not, then the start symbol should
 * be specified in {@link CookCCOption}.
 * <p>
 * In general, the naming convention of Non-terminals should following Java naming convention
 * with CamelCases.  The tree code generator would append AST to signify that it is an
 * AST node.
 * <p>
 * Additionally, 1) Line comments starts with ##.
 * 2) Arguments to be saved are specified after a single # character.
 * <p>
 * For example:
 * <pre>
 * /**
 * Expr : Expr '+' Expr              ## a line comment.
 *     | Expr '-' Expr
 *     | Expr '*' Expr
 *     | Expr '/' Expr
 *     | NotExpr
 *     ;
 *
 * NotExpr : '!' Expr    # 2         ## we only need to store the second expression
 *     ;
 * *&#47;
 * String toString ();
 * </pre>
 *
 * @author	Heng Yuan
 * @since	0.4
 */
@Retention (value = RetentionPolicy.SOURCE)
public @interface TreeRule
{
}
