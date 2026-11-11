/*
 * Copyright (c) 2015, Heng Yuan
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
