package org.yuanheng.cookcc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used to contain the grammar that needs to has its Abstract Syntax
 * Tree node classes automatically generated.
 * <p>
 * The grammar is specified as a JavaDoc for the annotation, since this is the simplest
 * way of specifying a large amount of multi-line text in Java without having to go through
 * complicated escaping and formatting.
 * <p>
 * There is no specific requirement of where this annotation is put.  Normally, it should
 * be either put after the first {@link Rule} annotation.  If not, then the start symbol should
 * be specified in {@link CookCCOption}.
 * <p>
 * In general, the naming convention of Non-terminals should following Java naming convention
 * with CamelCases.  The tree code generator would append AST to signify that it is an
 * AST node.
 * <p>
 *
 * @author Heng Yuan
 * @version $Id$
 * @since 0.4
 */
@Retention (value = RetentionPolicy.SOURCE)
public @interface TreeRule
{
}
