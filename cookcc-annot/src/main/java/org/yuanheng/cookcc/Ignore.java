package org.yuanheng.cookcc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used allow parser to ignore certain terminals generated by the lexer.
 * Optionally, it allows some of these ignored terminals to still be captured by the parser
 * without processing them syntactically.
 * <p/>
 * The main purpose of this annotation is to allow one common lexer to be used with multiple
 * parsers for the same language.  For example, one may use one parser for syntax checking,
 * one for formatting, and one for documentation.
 * <p/>
 * It should be noted that using the {@link Stream} interface of the CookCC lexer
 * (See {@link CookCCOption#stream}) and applying a filter predicate can be used equivalently
 * to ignore terminals, but it still lacks the capability of dealing
 *
 * @author Heng Yuan
 * @since 0.4
 */
@Retention (value = RetentionPolicy.SOURCE)
public @interface Ignore
{
	/**
	 * Ignore a set of terminals for grammar processing.
	 * <p>
	 * This ignore list allows the lexer to generate terminals such as spaces, comment,
	 * indentation etc that are not syntactically important to the parser.  Depending
	 * on the parser (such as code formatter), it may choose to process it as it sees
	 * fit.
	 * <p>
	 * Without this ignore list, one would have to write different lexers for the
	 * same language for different purposes, since typically these tokens would have
	 * to be ignore at the lexer level.
	 */
	String list () default "";

	/**
	 * A set of terminals that are in {@link #ignore}, but should be captured by
	 * the parser.  For the generated AST codes, a default handling is provided.
	 * Non-AST related code can call {@link CookCCChar#getCapturedTerminals}
	 * function manually to retrieve these captured terminals and call
	 * {@link CookCCChar#clearCapturedTerminals} function to clear the list.
	 * <p>
	 * The main point of this list is to allow certain things to be revisited later.
	 * For example, we may not need to deal with comments for a function in code
	 * generation, but we would value such comments for documentation generation.
	 * <p>
	 * Since there many cases, some of which ambiguous, of how comments should be
	 * handled, we basically group these ignored terminals with the next non-ignored
	 * terminal to be processed.
	 */
	String capture () default "";
}
