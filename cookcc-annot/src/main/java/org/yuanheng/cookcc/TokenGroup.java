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
package org.yuanheng.cookcc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is to mark a Enum value (and the values after it
 * until the next TokenGroup annotation) belongs to a particular
 * group, with same precedence and associations.
 * <p>
 * TokenGroups specified later have higher precedence.
 *
 * @author Heng Yuan
 * @since 0.3
 */
@Retention (value = RetentionPolicy.SOURCE)
public @interface TokenGroup
{
	/**
	 * The association of the token.
	 *
	 * @return	the association of the token.
	 */
	TokenType type () default TokenType.NONASSOC;
}
