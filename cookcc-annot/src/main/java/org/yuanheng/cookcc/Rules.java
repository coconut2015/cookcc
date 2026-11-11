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
 * Use this annotation to chain multiple {@link Rule} together.
 *
 * @author Heng Yuan
 * @since 0.3
 */
@Retention (value = RetentionPolicy.SOURCE)
public @interface Rules
{
	/**
	 * A list of rules that share the same actions.
	 * <p>
	 * Typically, it is only used in cases of empty actions.
	 *
	 * @return	an array of {@link Rule}.
	 */
	Rule[]  rules ();
}
