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
 * Use this annotation to create a lexical shortcut.  When a particular
 * <tt>{name}</tt> in any lexical patterns, CookCC would try to find the
 * shortcut with the given name and substitute it with its shortcut
 * pattern.
 * <p>
 * This annotation can be specified anywhere on any functions.  The
 * ordering of the shortcut does not matter, but care must be taken
 * not to create reference loops.
 *
 * @author Heng Yuan
 * @since 0.3
 */
@Retention (value = RetentionPolicy.SOURCE)
public @interface Shortcut
{
	/**
	 * The name of the shortcut.
	 *
	 * @return	the name of the shortcut.
	 */
	public abstract String name ();
	/**
	 * The regular expression pattern.
	 *
	 * @return the regular expression pattern this shortcut represents.
	 */
	public abstract String pattern ();
}
