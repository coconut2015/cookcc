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
package org.yuanheng.cookcc.input.yacc;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;

/**
 * @author Heng Yuan
 * @version $Id$
 */
abstract class YaccLexer
{
	protected final static int TOKEN = 256;
	protected final static int PARTIAL_ACTION = 257;
	protected final static int ACTION_CODE = 258;

	protected final static int INITIAL = 0;
	protected final static int SECTION2 = 63;
	protected final static int ACTION = 81;
	protected final static int BLOCKCOMMENT = 94;
	protected final static int CODEINCLUDE = 104;
	protected final static int SECTION3 = 114;

	// an internal class for lazy initiation
	private final static class cc_lexer
	{
		private static char[] accept = ("\000\000\016\001\003\016\016\016\015\001\016\006\005\004\002\000\000\000\000\000\000\000\000\014\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\010\000\000\000\000\000\000\011\000\000\007\000\000\013\000\000\000\012\000\000\016\001\003\016\016\016\016\024\022\021\026\025\000\004\002\023\000\000\030\003\030\031\031\021\027\032\030\004\002\000\000\034\003\034\035\035\036\034\033\000\000\040\003\040\041\041\042\040\037\043\043\043\044").toCharArray ();
		private static char[] ecs = ("\000\000\000\000\000\000\000\000\000\001\002\000\000\003\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\000\000\000\000\004\000\005\000\000\006\000\000\000\000\007\010\010\010\010\010\010\010\010\010\010\011\011\000\000\000\000\000\012\012\012\012\012\012\012\012\012\012\012\012\012\012\012\012\012\012\012\012\012\012\012\012\012\012\000\000\000\000\012\000\013\012\014\012\015\016\017\020\021\012\022\023\012\024\025\012\012\026\027\030\012\012\012\012\012\012\031\011\032\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\033").toCharArray ();
		private static char[][] next = {("\002\003\004\005\006\002\002\007\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\010").toCharArray (),("\002\011\004\005\012\002\002\007\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\010").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\003\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\004\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\013\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\014\000\000").toCharArray (),("\000\000\000\000\000\000\015\016\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\011\000\000\017\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\013\000\000\000\000\000\020\020\020\020\020\020\020\020\020\021\022\020\023\024\025\014\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\016\016\000\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\020\020\020\020\020\020\020\020\020\021\022\020\023\024\025\000\000\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\030\020\020\020\020\020\020\020\020\020\020\020\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\020\031\020\020\020\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\032\020\020\020\020\020\020\020\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\020\020\020\020\033\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\020\034\020\020\020\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\035\020\020\020\020\020\020\020\020\020\020\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\036\020\020\020\020\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\037\020\020\020\020\020\020\020\020\020\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\040\020\020\020\020\020\020\020\020\020\020\020\020\020\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\041\020\020\020\020\020\020\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\020\020\020\020\042\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\043\020\020\020\020\020\020\020\020\020\020\020\020\020\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\020\044\020\020\020\020\020\020\020\020\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\020\020\045\020\020\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\046\020\020\020\020\020\020\020\020\020\020\020\026\026\000").toCharArray (),("\026\047\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\020\020\020\050\020\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\020\020\020\020\051\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\020\020\020\020\052\026\026\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\053\020\020\020\020\026\026\000").toCharArray (),("\054\047\055\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\020\020\020\056\020\026\026\000").toCharArray (),("\026\057\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\026\026\000").toCharArray (),("\026\060\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\026\026\000").toCharArray (),("\026\061\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\026\026\000").toCharArray (),("\054\054\055\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\054\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\020\062\020\020\020\026\026\000").toCharArray (),("\063\057\064\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\000").toCharArray (),("\026\060\027\026\026\026\026\026\026\026\065\065\065\065\065\065\065\065\065\065\065\065\065\065\065\026\026\000").toCharArray (),("\066\061\067\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\000").toCharArray (),("\026\026\027\026\026\026\026\026\020\026\020\020\070\020\020\020\020\020\020\020\020\020\020\020\020\026\026\000").toCharArray (),("\063\063\064\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\063\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\026\071\072\073\026\026\026\026\065\026\065\065\065\065\065\065\065\065\065\065\065\065\065\065\065\026\026\000").toCharArray (),("\066\066\067\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\066\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\026\074\027\026\026\026\026\026\020\026\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\026\026\000").toCharArray (),("\026\071\072\073\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\026\026\072\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\000").toCharArray (),("\075\074\076\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\000").toCharArray (),("\075\075\076\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\075\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\101\102\103\104\105\106\101\107\101\110\111\111\111\111\111\111\111\111\111\111\111\111\111\111\111\112\101\113").toCharArray (),("\101\102\103\104\105\106\101\107\101\110\111\111\111\111\111\111\111\111\111\111\111\111\111\111\111\112\101\113").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\102\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\103\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\114\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\115\115\115\115\115\000\115\115\115\115\115\115\115\115\115\115\115\115\115\115\115\115\115\115\115\115\115\000").toCharArray (),("\000\000\000\000\000\000\116\117\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\111\000\111\111\111\111\111\111\111\111\111\111\111\111\111\111\111\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\120\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\117\117\000\117\117\117\117\117\117\117\117\117\117\117\117\117\117\117\117\117\117\117\117\117\117\117\117\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\123\123\124\125\123\126\126\127\123\123\123\123\123\123\123\123\123\123\123\123\123\123\123\123\123\130\131\132").toCharArray (),("\123\123\124\125\123\126\126\127\123\123\123\123\123\123\123\123\123\123\123\123\123\123\123\123\123\130\131\132").toCharArray (),("\133\133\000\133\133\000\000\000\133\133\133\133\133\133\133\133\133\133\133\133\133\133\133\133\133\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\133\133\124\133\133\000\000\000\133\133\133\133\133\133\133\133\133\133\133\133\133\133\133\133\133\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\134\135\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\133\133\000\133\133\000\000\000\133\133\133\133\133\133\133\133\133\133\133\133\133\133\133\133\133\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\135\135\000\135\135\135\135\135\135\135\135\135\135\135\135\135\135\135\135\135\135\135\135\135\135\135\135\000").toCharArray (),("\140\140\141\142\140\140\143\144\140\140\140\140\140\140\140\140\140\140\140\140\140\140\140\140\140\140\140\145").toCharArray (),("\140\140\141\142\140\140\143\144\140\140\140\140\140\140\140\140\140\140\140\140\140\140\140\140\140\140\140\145").toCharArray (),("\146\146\000\146\146\146\000\000\146\146\146\146\146\146\146\146\146\146\146\146\146\146\146\146\146\146\146\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\146\146\141\146\146\146\000\000\146\146\146\146\146\146\146\146\146\146\146\146\146\146\146\146\146\146\146\000").toCharArray (),("\000\000\000\000\000\000\000\147\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\146\146\000\146\146\146\000\000\146\146\146\146\146\146\146\146\146\146\146\146\146\146\146\146\146\146\146\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\152\152\153\154\155\152\152\152\152\152\152\152\152\152\152\152\152\152\152\152\152\152\152\152\152\156\152\157").toCharArray (),("\152\152\153\154\155\152\152\152\152\152\152\152\152\152\152\152\152\152\152\152\152\152\152\152\152\156\152\157").toCharArray (),("\160\160\000\160\000\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\000\160\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\160\160\153\160\000\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\000\160\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\161\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\160\160\000\160\000\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\160\000\160\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\165").toCharArray (),("\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\165").toCharArray (),("\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\164\000").toCharArray (),("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray ()};
	}

	// an internal class for lazy initiation
	private final static class cc_parser
	{
		private static char[] rule = ("\000\001\001\002\001\002\001\003\004\003\002\000\002\000\002\000").toCharArray ();
		private static char[] ecs = ("\000\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\002\003\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\004\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\005\006\007").toCharArray ();
		private static char[][] next = {("\000\001\000\000\000\002\000\000\000\003\004\005\000\000\000\000").toCharArray (),("\ufffa\ufffa\ufffa\ufffa\ufffa\ufffa\ufffa\ufffa\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\006\000\000\000\000\000\000\000\000\000\007\000\000\000").toCharArray (),("\uffff\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\ufffe\010\000\000\000\002\000\000\000\000\000\011\000\000\000\000").toCharArray (),("\ufffc\ufffc\000\000\000\ufffc\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\ufff5\ufff5\ufff5\ufff5\ufff5\000\000\000\000\000\012\000\000").toCharArray (),("\000\000\000\013\014\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\ufffb\ufffb\ufffb\ufffb\ufffb\ufffb\ufffb\ufffb\000\000\000\000\000\000\000\000").toCharArray (),("\ufffd\ufffd\000\000\000\ufffd\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\ufff3\ufff3\015\ufff1\ufff1\000\000\000\000\000\000\016\017").toCharArray (),("\ufff9\ufff9\000\000\000\ufff9\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\ufff5\ufff5\ufff5\ufff5\ufff5\000\000\000\000\000\020\000\000").toCharArray (),("\000\000\000\ufff6\ufff6\ufff6\ufff6\ufff6\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\ufff7\ufff7\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\000\000\000\021\022\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\ufff3\ufff3\015\ufff1\ufff1\000\000\000\000\000\000\023\017").toCharArray (),("\000\000\000\000\000\000\ufff2\ufff2\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\ufff4\ufff4\000\000\000\000\000\000\000\000\000\000\000").toCharArray (),("\000\000\000\ufff8\ufff8\000\000\000\000\000\000\000\000\000\000\000").toCharArray ()};
		private static char[] lhs = ("\000\010\011\012\012\012\012\013\014\014\015\015\016\016\017\017").toCharArray ();
	}

	private final static class YYParserState	// internal tracking tool
	{
		int token;			// the current token type
		Object value;		// the current value associated with token
		int state;			// the current scan state

		YYParserState ()	// EOF token construction
		{
			this (0, null, 0);
		}
		YYParserState (int token)
		{
			this (token, null, 0);
		}
		YYParserState (int token, Object value)
		{
			this (token, value, 0);
		}
		YYParserState (int token, Object value, int state)
		{
			this.token = token;
			this.value = value;
			this.state = state;
		}
	}

	// lookahead stack for the parser
	private final LinkedList _yyLookaheadStack = new LinkedList ();
	// state stack for the parser
	private final Vector _yyStateStack = new Vector (512, 512);
	// flag that indicates error
	private boolean _yyInError;
	// internal track of the argument start
	private int _yyArgStart;
	// for passing value from lexer to parser
	private Object _yyValue;

	private InputStream _yyIs = System.in;
	private byte[] _yyBuffer;
	private int _yyBufferSize = 4096;
	private int _yyMatchStart;
	private int _yyBufferEnd;

	private int _yyBaseState;

	private int _yyTextStart;
	private int _yyLength;

	private Stack _yyLexerStack;

	// we need to track beginning of line (BOL) status
	private boolean _yyIsNextBOL = true;
	private boolean _yyBOL = true;

	public void setInput (InputStream is)
	{
		_yyIs = is;
	}

	public InputStream getInput ()
	{
		return _yyIs;
	}

	/**
	 * Check whether or not the current token at the beginning of the line.  This
	 * function is not accurate if the user does multi-line pattern matching or
	 * have trail contexts at the end of the line.
	 *
	 * @return	whether or not the current token is at the beginning of the line.
	 */
	public boolean isBOL ()
	{
		return _yyBOL;
	}

	/**
	 * Get the current token text.
	 * <p>
	 * Avoid calling this function unless it is absolutely necessary since it creates
	 * a copy of the token string.  The string length can be found by reading _yyLength
	 * or calling yyLength () function.
	 *
	 * @return	the current text token.
	 */
	public String yyText ()
	{
		if (_yyMatchStart == _yyTextStart)		// this is the case when we have EOF
			return null;
		return new String (_yyBuffer, _yyTextStart, _yyMatchStart - _yyTextStart);
	}

	/**
	 * Get the current text token's length.  Actions specified in the CookCC file
	 * can directly access the variable _yyLength.
	 *
	 * @return	the string token length
	 */
	public int yyLength ()
	{
		return _yyLength;
	}

	/**
	 * Print the current string token to the standard output.
	 */
	public void echo ()
	{
		System.out.print (yyText ());
	}

	/**
	 * Put all but n characters back to the input stream.  Be aware that calling
	 * yyLess (0) is allowed, but be sure to change the state some how to avoid
	 * an endless loop.
	 *
	 * @param	n
	 * 			The number of characters.
	 */
	protected void yyLess (int n)
	{
		if (n < 0)
			throw new IllegalArgumentException ("yyLess function requires a non-zero value.");
		if (n > (_yyMatchStart - _yyTextStart))
			throw new IndexOutOfBoundsException ("yyLess function called with a too large index value " + n + ".");
		_yyMatchStart = _yyTextStart + n;
	}

	/**
	 * Set the lexer's current state.
	 *
	 * @param	baseState
	 *			the base state index
	 */
	protected void begin (int baseState)
	{
		_yyBaseState = baseState;
	}

	/**
	 * Push the current state onto lexer state onto stack and
	 * begin the new state specified by the user.
	 *
	 * @param	newState
	 *			the new state.
	 */
	protected void yyPushLexerState (int newState)
	{
		if (_yyLexerStack == null)
			_yyLexerStack = new Stack ();
		_yyLexerStack.push (new Integer (_yyBaseState));
		begin (newState);
	}

	/**
	 * Restore the previous lexer state.
	 */
	protected void yyPopLexerState ()
	{
		begin (((Integer)_yyLexerStack.pop ()).intValue ());
	}


	// read more data from the input
	protected boolean yyRefreshBuffer () throws IOException
	{
		if (_yyBuffer == null)
			_yyBuffer = new byte[_yyBufferSize];
		if (_yyMatchStart > 0)
		{
			if (_yyBufferEnd > _yyMatchStart)
			{
				System.arraycopy (_yyBuffer, _yyMatchStart, _yyBuffer, 0, _yyBufferEnd - _yyMatchStart);
				_yyBufferEnd -= _yyMatchStart;
				_yyMatchStart = 0;
			}
			else
			{
				_yyMatchStart = 0;
				_yyBufferEnd = 0;
			}
		}
		int readSize = _yyIs.read (_yyBuffer, _yyBufferEnd, _yyBufferSize - _yyBufferEnd);
		if (readSize > 0)
			_yyBufferEnd += readSize;
		return readSize >= 0;
	}

	/**
	 * Reset the internal buffer.
	 */
	public void yyResetBuffer ()
	{
		_yyMatchStart = 0;
		_yyBufferEnd = 0;
	}

	/**
	 * Set the internal buffer size.  This action can only be performed
	 * when the buffer is empty.  Having a large buffer is useful to read
	 * a whole file in to increase the performance sometimes.
	 *
	 * @param	bufferSize
	 *			the new buffer size.
	 */
	public void setBufferSize (int bufferSize)
	{
		if (_yyBufferEnd > _yyMatchStart)
			throw new IllegalArgumentException ("Cannot change lexer buffer size at this moment.");
		_yyBufferSize = bufferSize;
		_yyMatchStart = 0;
		_yyBufferEnd = 0;
		if (_yyBuffer != null && bufferSize != _yyBuffer.length)
			_yyBuffer = new byte[bufferSize];
	}

	/**
	 * Call this function to start the scanning of the input.
	 *
	 * @return	a token or status value.
	 * @throws	IOException
	 *			in case of I/O error.
	 */
	protected int yyLex () throws IOException
	{

		char[] cc_ecs = cc_lexer.ecs;
		char[][] cc_next = cc_lexer.next;
		char[] cc_accept = cc_lexer.accept;

		byte[] buffer = _yyBuffer;

		while (true)
		{
			// initiate variables necessary for lookup
			_yyBOL = _yyIsNextBOL;
			_yyIsNextBOL = false;
			int cc_matchedState = _yyBaseState + (_yyBOL ? 1 : 0);

			int matchedLength = 0;

			int internalBufferEnd = _yyBufferEnd;
			int lookahead = _yyMatchStart;

			int cc_backupMatchedState = cc_matchedState;
			int cc_backupMatchedLength = 0;

			// the DFA lookup
			while (true)
			{
				// check buffer status
				if (lookahead < internalBufferEnd)
				{
					// now okay to process the character
					int cc_toState;
					cc_toState = cc_next[cc_matchedState][cc_ecs[buffer[lookahead] & 0xff]];

					if (cc_toState == 0)
					{
						cc_matchedState = cc_backupMatchedState;
						matchedLength = cc_backupMatchedLength;
						break;
					}

					cc_matchedState = cc_toState;
					++lookahead;
					++matchedLength;

					if (cc_accept[cc_matchedState] > 0)
					{
						cc_backupMatchedState = cc_toState;
						cc_backupMatchedLength = matchedLength;
					}
				}
				else
				{
					int lookPos = lookahead - _yyMatchStart;
					boolean refresh = yyRefreshBuffer ();
					buffer = _yyBuffer;
					internalBufferEnd = _yyBufferEnd;
					lookahead = _yyMatchStart + lookPos;
					if (! refresh)
					{
						// <<EOF>>
						int cc_toState;
						cc_toState = cc_next[cc_matchedState][cc_ecs[256]];
						if (cc_toState != 0)
							cc_matchedState = cc_toState;
						else
						{
							cc_matchedState = cc_backupMatchedState;
							matchedLength = cc_backupMatchedLength;
						}
						break;
					}
				}
			}

			_yyTextStart = _yyMatchStart;
			_yyMatchStart += matchedLength;
			_yyLength = matchedLength;


			switch (cc_accept[cc_matchedState])
			{
				case 1:	// {WS}
				{
					m_this.ignoreWhiteSpace ();
				}
				case 38: break;
				case 2:	// '//'.*
				{
					m_this.lineComment ();
				}
				case 39: break;
				case 3:	// {NL}
				{
					m_this.newLine ();
				}
				case 40: break;
				case 4:	// '/*'
				{
					m_this.blockCommentStart ();
				}
				case 41: break;
				case 27:	// '*/'
				{
					m_this.blockCommentEnd ();
				}
				case 64: break;
				case 28:	// [^*/\n]+
				{
					m_this.blockCommentContent ();
				}
				case 65: break;
				case 29:	// .
				{
					m_this.blockCommentContent ();
				}
				case 66: break;
				case 30:	// <<EOF>>
				{
					m_this.blockCommentEof ();
				}
				case 67: break;
				case 5:	// '%{'
				{
					m_this.codeIncludeStart ();
				}
				case 42: break;
				case 31:	// '%}'
				{
					m_this.codeIncludeEnd ();
				}
				case 68: break;
				case 32:	// [^%{\n]+
				{
					m_this.codeIncludeContent ();
				}
				case 69: break;
				case 33:	// .
				{
					m_this.codeIncludeContent ();
				}
				case 70: break;
				case 34:	// <<EOF>>
				{
					m_this.codeIncludeEof ();
				}
				case 71: break;
				case 17:	// '{'
				{
					m_this.actionLB ();
				}
				case 54: break;
				case 23:	// '}'
				{
					return m_this.actionRB ();
				}
				case 60: break;
				case 24:	// [^'/*{}\n]+
				{
					m_this.actionContent ();
				}
				case 61: break;
				case 25:	// .
				{
					m_this.actionContent ();
				}
				case 62: break;
				case 26:	// <<EOF>>
				{
					m_this.actionEof ();
				}
				case 63: break;
				case 6:	// '%%'
				{
					m_this.startSection2 ();
				}
				case 43: break;
				case 7:	// ^{OPTWS}'%token'{WS}.*\n
				{
					m_this.addTokens ();
				}
				case 44: break;
				case 8:	// ^{OPTWS}'%left'{WS}.*\n
				{
					m_this.addLeftTokens ();
				}
				case 45: break;
				case 9:	// ^{OPTWS}'%right'{WS}.*\n
				{
					m_this.addRightTokens ();
				}
				case 46: break;
				case 10:	// ^{OPTWS}'%nonassoc'{WS}.*\n
				{
					m_this.addNonAssocTokens ();
				}
				case 47: break;
				case 11:	// ^{OPTWS}'%start'{WS}{NAME}{OPTWS}{NL}
				{
					m_this.setStartToken ();
				}
				case 48: break;
				case 12:	// ^{OPTWS}%{NAME}.*\n
				{
					m_this.unknownDirective ();
				}
				case 49: break;
				case 13:	// <<EOF>>
				{
					m_this.earlyEof ();
				}
				case 50: break;
				case 18:	// {NAME}
				{
					_yyValue = m_this.parseToken (); return TOKEN;
				}
				case 55: break;
				case 19:	// ['][^\'][']
				{
					_yyValue = m_this.parseToken (); return TOKEN;
				}
				case 56: break;
				case 20:	// [:|;]
				{
					return m_this.scanSymbol ();
				}
				case 57: break;
				case 21:	// '%%'
				{
					m_this.startSection3 ();
				}
				case 58: break;
				case 22:	// <<EOF>>
				{
					return m_this.eof ();
				}
				case 59: break;
				case 35:	// (.|\n)*
				{
					m_this.dumpSection3Code ();
				}
				case 72: break;
				case 36:	// <<EOF>>
				{
					return m_this.endSection3 ();
				}
				case 73: break;
				case 14:	// .
				{
					m_this.invalidChar ();
				}
				case 51: break;
				case 15:	// .|\n
				{
					echo ();			// default character action
				}
				case 52: break;
				case 16:	// <<EOF>>
				{
					return 0;			// default EOF action
				}
				case 53: break;
				default:
					throw new IOException ("Internal error in YaccLexer lexer.");
			}

			// check BOL here since '\n' may be unput back into the stream buffer

			// specifically used _yyBuffer since it could be changed by user
			if (_yyMatchStart > 0 && _yyBuffer[_yyMatchStart - 1] == '\n')
				_yyIsNextBOL = true;
		}
	}


	/**
	 * Call this function to start parsing.
	 *
	 * @return	0 if everything is okay, or 1 if an error occurred.
	 * @throws	IOException
	 *			in case of error
	 */
	public int yyParse () throws IOException
	{
		char[] cc_ecs = cc_parser.ecs;
		char[][] cc_next = cc_parser.next;
		char[] cc_rule = cc_parser.rule;
		char[] cc_lhs = cc_parser.lhs;

		LinkedList cc_lookaheadStack = _yyLookaheadStack;
		Vector cc_stateStack = _yyStateStack;

		if (cc_stateStack.size () == 0)
			cc_stateStack.add (new YYParserState ());

		int cc_toState = 0;

		for (;;)
		{
			YYParserState cc_lookahead;

			int cc_fromState;
			char cc_ch;

			//
			// check if there are any lookahead tokens on stack
			// if not, then call yyLex ()
			//
			if (cc_lookaheadStack.size () == 0)
			{
				_yyValue = null;
				int val = yyLex ();
				cc_lookahead = new YYParserState (val, _yyValue);
				cc_lookaheadStack.add (cc_lookahead);
			}
			else
				cc_lookahead = (YYParserState)cc_lookaheadStack.getLast ();

			cc_ch = cc_ecs[cc_lookahead.token];
			cc_fromState = ((YYParserState)cc_stateStack.get (cc_stateStack.size () - 1)).state;
			cc_toState = (short)cc_next[cc_fromState][cc_ch];


			//
			// check the value of toState and determine what to do
			// with it
			//
			if (cc_toState > 0)
			{
				// shift
				cc_lookahead.state = cc_toState;
				cc_stateStack.add (cc_lookahead);
				cc_lookaheadStack.removeLast ();
				continue;
			}
			else if (cc_toState == 0)
			{
				// error
				if (_yyInError)
				{
					// first check if the error is at the lookahead
					if (cc_ch == 1)
					{
						// so we need to reduce the stack until a state with reduceable
						// action is found
						if (_yyStateStack.size () > 1)
							_yyStateStack.setSize (_yyStateStack.size () - 1);
						else
							return 1;	// can't do much we exit the parser
					}
					else
					{
						// this means that we need to dump the lookahead.
						if (cc_ch == 0)		// can't do much with EOF;
							return 1;
						cc_lookaheadStack.removeLast ();
					}
					continue;
				}
				else
				{
					if (yyParseError (cc_lookahead.token))
						return 1;
					_yyLookaheadStack.add (new YYParserState (1, _yyValue));
					_yyInError = true;
					continue;
				}
			}
			_yyInError = false;
			// now the reduce action
			int cc_ruleState = -cc_toState;

			_yyArgStart = cc_stateStack.size () - cc_rule[cc_ruleState] - 1;
			//
			// find the state that said need this non-terminal
			//
			cc_fromState = ((YYParserState)cc_stateStack.get (_yyArgStart)).state;

			//
			// find the state to goto after shifting the non-terminal
			// onto the stack.
			//
			if (cc_ruleState == 1)
				cc_toState = 0;			// reset the parser
			else
			{
				cc_toState = cc_next[cc_fromState][cc_lhs[cc_ruleState]];
			}

			_yyValue = null;

			switch (cc_ruleState)
			{
				case 1:					// accept
					return 0;
				case 2:	// section2 : rules
				{
					return m_this.parseSection2 ();
				}
				case 18: break;
				case 3:	// rules : rules rule
				{
					m_this.parseRules ();
				}
				case 19: break;
				case 4:	// rules : rule
				{
					m_this.parseRules ();
				}
				case 20: break;
				case 5:	// rules : rules error
				{
					m_this.parseRuleError ();
				}
				case 21: break;
				case 6:	// rules : error
				{
					m_this.parseRulesError ();
				}
				case 22: break;
				case 7:	// rule : TOKEN rhsList ';'
				{
					m_this.parseRule ((java.lang.String)yyGetValue (1), (java.util.ArrayList<org.yuanheng.cookcc.doc.RhsDoc>)yyGetValue (2));
				}
				case 23: break;
				case 8:	// rhsList : rhsList '|' terms action
				{
					_yyValue = m_this.parseRhsList ((java.util.ArrayList<org.yuanheng.cookcc.doc.RhsDoc>)yyGetValue (1), (java.lang.Integer)yyGetValue (2), (java.lang.String)yyGetValue (3), (java.lang.String)yyGetValue (4));
				}
				case 24: break;
				case 9:	// rhsList : ':' terms action
				{
					_yyValue = m_this.parseRhsList ((java.lang.Integer)yyGetValue (1), (java.lang.String)yyGetValue (2), (java.lang.String)yyGetValue (3));
				}
				case 25: break;
				case 10:	// terms : terms TOKEN
				{
					_yyValue = m_this.parseRHS ((java.lang.String)yyGetValue (1), (java.lang.String)yyGetValue (2));
				}
				case 26: break;
				case 11:	// terms : 
				{
					_yyValue = m_this.parseTerms ();
				}
				case 27: break;
				case 12:	// action : complete_action ACTION_CODE
				{
					_yyValue = m_this.parseAction ((java.lang.String)yyGetValue (2));
				}
				case 28: break;
				case 13:	// action : 
				{
					_yyValue = m_this.parseAction ();
				}
				case 29: break;
				case 14:	// complete_action : complete_action PARTIAL_ACTION
				{
					_yyValue = m_this.parseAction ();
				}
				case 30: break;
				case 15:	// complete_action : 
				{
					_yyValue = m_this.parseAction ();
				}
				case 31: break;
				default:
					throw new IOException ("Internal error in YaccLexer parser.");
			}

			YYParserState cc_reduced = new YYParserState (-cc_ruleState, _yyValue, cc_toState);
			_yyValue = null;
			cc_stateStack.setSize (_yyArgStart + 1);
			cc_stateStack.add (cc_reduced);
		}
	}

	/**
	 * This function is used by the error handling grammars to check the immediate
	 * lookahead token on the stack.
	 *
	 * @return	the top of lookahead stack.
	 */
	protected YYParserState yyPeekLookahead ()
	{
		return (YYParserState)_yyLookaheadStack.getLast ();
	}

	/**
	 * This function is used by the error handling grammars to pop an unwantted
	 * token from the lookahead stack.
	 */
	protected void yyPopLookahead ()
	{
		_yyLookaheadStack.removeLast ();
	}

	/**
	 * Clear the error flag.  If this flag is present and the parser again sees
	 * another error transition, it would immediately calls yyParseError, which
	 * would by default exit the parser.
	 * <p>
	 * This function is used in error recovery.
	 */
	protected void yyClearError ()
	{
		_yyInError = false;
	}

	/**
	 * This function reports error and return true if critical error occurred, or
	 * false if the error has been successfully recovered.  IOException is an optional
	 * choice of reporting error.
	 *
	 * @param	terminal
	 *			the terminal that caused the error.
	 * @return	true if irrecoverable error occurred.  Or simply throw an IOException.
	 *			false if the parsing can be continued to check for specific
	 *			error tokens.
	 * @throws	IOException
	 *			in case of error.
	 */
	protected boolean yyParseError (int terminal) throws IOException
	{
		return false;
	}

	/**
	 * Gets the object value associated with the symbol at the argument's position.
	 *
	 * @param	arg
	 *			the symbol position starting from 1.
	 * @return	the object value associated with symbol.
	 */
	protected Object yyGetValue (int arg)
	{
		return ((YYParserState)_yyStateStack.get (_yyArgStart + arg)).value;
	}

	/**
	 * Set the object value for the current non-terminal being reduced.
	 *
	 * @param	value
	 * 			the object value for the current non-terminal.
	 */
	protected void yySetValue (Object value)
	{
		_yyValue = value;
	}




	private final org.yuanheng.cookcc.input.yacc.YaccParser m_this = (org.yuanheng.cookcc.input.yacc.YaccParser)this;

	/**
	 * This function is used to change the initial state for the lexer.
	 *
	 * @param	state
	 *			the name of the state
	 */
	protected void begin (String state)
	{
		if ("INITIAL".equals (state))
		{
			begin (INITIAL);
			return;
		}
		if ("SECTION2".equals (state))
		{
			begin (SECTION2);
			return;
		}
		if ("ACTION".equals (state))
		{
			begin (ACTION);
			return;
		}
		if ("BLOCKCOMMENT".equals (state))
		{
			begin (BLOCKCOMMENT);
			return;
		}
		if ("CODEINCLUDE".equals (state))
		{
			begin (CODEINCLUDE);
			return;
		}
		if ("SECTION3".equals (state))
		{
			begin (SECTION3);
			return;
		}
		throw new IllegalArgumentException ("Unknown lexer state: " + state);
	}

	/**
	 * Push the current state onto lexer state onto stack and
	 * begin the new state specified by the user.
	 *
	 * @param	state
	 *			the new state.
	 */
	protected void yyPushLexerState (String state)
	{
		if ("INITIAL".equals (state))
		{
			yyPushLexerState (INITIAL);
			return;
		}
		if ("SECTION2".equals (state))
		{
			yyPushLexerState (SECTION2);
			return;
		}
		if ("ACTION".equals (state))
		{
			yyPushLexerState (ACTION);
			return;
		}
		if ("BLOCKCOMMENT".equals (state))
		{
			yyPushLexerState (BLOCKCOMMENT);
			return;
		}
		if ("CODEINCLUDE".equals (state))
		{
			yyPushLexerState (CODEINCLUDE);
			return;
		}
		if ("SECTION3".equals (state))
		{
			yyPushLexerState (SECTION3);
			return;
		}
		throw new IllegalArgumentException ("Unknown lexer state: " + state);
	}


/*
 * lexer properties:
 * unicode = false
 * bol = true
 * backup = true
 * cases = 36
 * table = ecs
 * ecs = 28
 * states = 118
 * max symbol value = 256
 *
 * memory usage:
 * full table = 30326
 * ecs table = 3561
 *
 * parser properties:
 * symbols = 16
 * max terminal = 258
 * used terminals = 8
 * non-terminals = 8
 * rules = 15
 * shift/reduce conflicts = 0
 * reduct/reduce conflicts = 0
 *
 * memory usage:
 * ecs table = 579
 */
}
