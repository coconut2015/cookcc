/*
 * Copyright (c) 2008-2013, Heng Yuan
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
package org.yuanheng.cookcc.interfaces;

/**
 * OptionParser is used in parsing user command line arguments.
 * It's toString () function is used to print usage.
 *
 * @author Heng Yuan
 */
public interface OptionHandler
{
	/**
	 * Indicates the option string
	 *
	 * @return the option string
	 */
	public String getOption ();

	/**
	 * Indicates whether or not this option requires an argument.
	 *
	 * @return whether or not this option requires an argument.
	 */
	public boolean requireArguments ();

	/**
	 * This function parses the argument at the current index.
	 * If this function recognizes the argument, it would return
	 * the number of arguments being read.  Otherwise, just return
	 * 0 to indicate that it was not handled.
	 *
	 * @param    value the argument for the option
	 * @throws Exception in case of error.
	 */
	public void handleOption (String value) throws Exception;
}
