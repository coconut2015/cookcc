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
