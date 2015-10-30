package org.yuanheng.cookcc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This is a very simple annotation to tell CookCC APT Processor to scan a particular
 * interface for CookCC parser grammar.
 *
 * @author Heng Yuan
 * @version $Id$
 * @since 0.4
 */
@Retention (value = RetentionPolicy.SOURCE)
public @interface CookCCGrammar
{
}
