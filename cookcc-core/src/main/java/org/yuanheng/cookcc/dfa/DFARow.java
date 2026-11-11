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
package org.yuanheng.cookcc.dfa;

/**
 * @author Heng Yuan
 */
public class DFARow
{
	private int m_caseValue;
	private final short[] m_states;

	public DFARow (int cols)
	{
		m_states = new short[cols];
	}

	private DFARow (DFARow other)
	{
		m_caseValue = other.m_caseValue;
		m_states = other.m_states.clone ();
	}

	public short[] getStates ()
	{
		return m_states;
	}

	public void setState (int col, int state)
	{
		m_states[col] = (short)state;
	}

	public void setCaseValue (int caseValue)
	{
		m_caseValue = caseValue;
	}

	public int getCaseValue ()
	{
		return m_caseValue;
	}

	public DFARow clone ()
	{
		return new DFARow (this);
	}
}
