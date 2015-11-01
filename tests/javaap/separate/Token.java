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

import org.yuanheng.cookcc.*;

/**
 * Specify the tokens shared by the lexer and parser.
 */
@CookCCToken
enum Token
{
	// TokenGroup is used to specify the token type and precedence.
	// By default, if the type of the token is not specified, it is
	// TokenGroup.NONASSOC.
	@TokenGroup
	VARIABLE, INTEGER, WHILE, IF, PRINT, ASSIGN, SEMICOLON,
	@TokenGroup
	IFX,
	@TokenGroup
	ELSE,

	// specify the left associativity.
	// Can use static import to avoid typing TokenType. part.
	@TokenGroup (type = TokenType.LEFT)
	GE, LE, EQ, NE, LT, GT,
	@TokenGroup (type = TokenType.LEFT)
	ADD, SUB,
	@TokenGroup (type = TokenType.LEFT)
	MUL, DIV,
	@TokenGroup (type = TokenType.LEFT)
	UMINUS
}
