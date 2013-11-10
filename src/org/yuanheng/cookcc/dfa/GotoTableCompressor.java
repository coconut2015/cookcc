/*
 * Copyright (c) 2008-2013, Heng Yuan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *    Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    Neither the name of the Heng Yuan nor the
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
package org.yuanheng.cookcc.dfa;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author Heng Yuan
 * @version $Id$
 */
class GotoTableCompressor
{
	public final static short SHORT_MIN = Short.MIN_VALUE;

	private final Vector<short[]> m_dfa;
	private final Vector<short[]> m_dfaCopy;

	int m_baseAdd;
	private boolean m_useStateDiff;

	private short[] m_default;

	private short[] m_next;
	private short[] m_check;
	private short[] m_base;

	private Map<Integer, Vector<Short>> m_fillMap = new HashMap<Integer, Vector<Short>> ();

	public GotoTableCompressor (Vector<short[]> gotoTable)
	{
		m_dfa = gotoTable;
		m_dfaCopy = new Vector<short[]> ();
		for (short[] column : m_dfa)
			m_dfaCopy.add (column.clone ());
	}

	// find out the # of zeros (i.e. errors) in the state
	private int getErrorCount (int state)
	{
		int count = 0;
		short[] column = m_dfa.get (state);
		for (short c : column)
			if (c == 0)
				++count;
		return count;
	}

	private int getStateDiff (int thisState, int cmpState)
	{
		short[] column1 = m_dfa.get (thisState);
		short[] column2 = m_dfa.get (cmpState);

		int diff = 0;

		for (int i = 0; i < column1.length; ++i)
			if (column1[i] != column2[i])
				++diff;
		return diff;
	}

	private void cleanState (int thisState, int cmpState)
	{
		short[] column1 = m_dfaCopy.get (thisState);

		if (cmpState < 0)
		{
			for (int i = 0; i < column1.length; ++i)
				if (column1[i] == 0)
					column1[i] = SHORT_MIN;
		}
		else
		{
			short[] column2 = m_dfa.get (cmpState);
			m_useStateDiff = true;

			for (int i = 0; i < column1.length; ++i)
				if (column1[i] == column2[i])
					column1[i] = SHORT_MIN;
		}
	}

	private int getHoleSize (short thisState, int min, int max)
	{
		short[] column = m_dfaCopy.get (thisState);
		int holes = 0;
		for (int i = min; i <= max; ++i)
			if (column[i] == SHORT_MIN)
				++holes;
		return holes;
	}

	int getBlockSize (short row, int[] minMax)
	{
		short[] column = m_dfaCopy.get (row);
		int i;
		for (i = 0; i < column.length; ++i)
			if (column[i] != SHORT_MIN)
				break;
		minMax[0] = i;

		for (i = column.length - 1; i > 0; --i)
			if (column[i] != SHORT_MIN)
				break;
		minMax[1] = i;

		//DEBUGMSG ("row = " << row << ", min = " << min << ", max = " << max);
		return minMax[1] - minMax[0] + 1;
	}

	//
	// return if can fill the row at the position
	//
	boolean canFill (int row, int min, int max, int pos)
	{
		int bound = m_next.length;
		short[] column = m_dfaCopy.get (row);
		for (; pos < bound && min <= max; ++pos, ++min)
		{
			if (column[min] != SHORT_MIN &&
				m_next[pos] != SHORT_MIN)
				return false;
		}
		return true;
	}

	//
	// do the actual filling
	//
	void doFill (int row, int min, int max, int pos)
	{
		int bound = pos + max - min + 1;
		// allocate space if necessary
		if (bound > m_next.length)
		{
			//DEBUGMSG ("trying to resize to " << bound);
			m_next = TableCompressor.resize (m_next, bound, SHORT_MIN);
			m_check = TableCompressor.resize (m_check, bound, SHORT_MIN);
		}

		int rowIndex = row + m_baseAdd;

		m_base[rowIndex] = (short)(pos - min);

		// now do the fill
		short[] column = m_dfaCopy.get (row);
		for (; min <= max; ++pos, ++min)
		{
			if (column[min] != SHORT_MIN)
			{
				m_next[pos] = column[min];
				m_check[pos] = (short)rowIndex;
			}
		}
	}

	void doFillState (int row, int min, int max)
	{
		//DEBUGMSG ("trying to fill state " << row);

		// pretty dumb algorithm here
		//
		// just going through all the holes and see
		// if there is an available position

		int size = m_next.length;
		// we start from min because we don't want to have any
		// negative indices
		for (int i = min; i < size; ++i)
		{
			if (canFill (row, min, max, i))
			{
				doFill (row, min, max, i);
				return;
			}
		}
		doFill (row, min, max, m_next.length);
	}

	void doInsertState (short state, short cmpState)
	{
		if (cmpState >= 0)
			m_default[state] = (short)(cmpState + m_baseAdd);
		else
			m_default[state] = cmpState;
		cleanState (state, cmpState);

		int[] minMax = new int[2];
		int blockSize = getBlockSize (state, minMax);

		if (blockSize == 0)
			return;

		int holeSize = getHoleSize (state, minMax[0], minMax[1]);

		Vector<Short> list = m_fillMap.get (holeSize);
		if (list == null)
		{
			list = new Vector<Short> ();
			m_fillMap.put (holeSize, list);
		}
		list.add (state);
	}

	void doFillStates ()
	{
		int[] minMax = new int[2];


		Integer[] holeSizes = m_fillMap.keySet ().toArray (new Integer[m_fillMap.size ()]);

		// fill the states from the biggest to the smallest
		for (int i = holeSizes.length - 1; i >= 0; --i)
		{
			Integer holeSize = holeSizes[i];
			for (Short state : m_fillMap.get (holeSize))
			{
				getBlockSize (state, minMax);
				//DEBUGMSG ("min = " << min << ", max = " << max);
				doFillState (state, minMax[0], minMax[1]);
			}
		}
	}

	//
	// There are some properties of GOTO table that can be taken advantaged
	// of:
	//
	// 1. 0 values are never ever referenced, so no need to worry about index
	// out of the bound
	//
	//
	//
	void compute (short[] base, short[] next, short[] check)
	{
		m_base = base;
		m_next = next;
		m_check = check;

		m_baseAdd = m_base.length;

		m_base = TableCompressor.resize (m_base, m_base.length + m_dfaCopy.size () + 1, SHORT_MIN);
		m_default = TableCompressor.resize (m_default, m_dfaCopy.size () + 1, SHORT_MIN);

		for (short i = 0; i < m_dfaCopy.size (); ++i)
		{
			// ignore those empty ones
			int errorCount = getErrorCount (i);
			int rowLength = m_dfa.get (i).length;
			if (errorCount == rowLength)        // ok, this state is full of 0's
				continue;

			// search the DFA for a nearest state
			int minDiff = Short.MAX_VALUE;
			short minState = i;
			for (short j = 0; j < i; ++j)
			{
				int diff = getStateDiff (i, j);
				if (diff < minDiff)
				{
					minDiff = diff;
					minState = j;

					if (diff == 0)
						break;
				}
			}

			// if found a state like that
			if (minState < i && minDiff < (rowLength - errorCount))
				doInsertState (i, minState);
			else
				doInsertState (i, SHORT_MIN);
		}

		doFillStates ();

		// Add an empty state at the end to prevent index out of bound etc
		// and deal with states with all 0's
		short defaultState = (short)(m_baseAdd + m_dfa.size ());
		int end;
		for (end = m_next.length - 1; end > 0; --end)
		{
			if (m_check[end] != SHORT_MIN)
				break;
		}
		++end;
		m_next = TableCompressor.resize (m_next, end + m_dfa.get (0).length, (short)0);
		m_check = TableCompressor.resize (m_check, end + m_dfa.get (0).length, defaultState);
		for (int i = end; i < m_check.length; ++i)
		{
			m_check[i] = defaultState;
			m_next[i] = (short)0;
		}
		m_base[m_base.length - 1] = (short)end;


		//
		// process all SHORT_MIN in _checkArray and _nextArray to
		// some less high magnitude value
		//
		for (int i = 0; i < m_check.length; ++i)
		{
			if (m_check[i] == SHORT_MIN)
				m_check[i] = defaultState;
			if (m_next[i] == SHORT_MIN)
				m_next[i] = 0;
		}

		//
		// process all SHORT_MIN in _baseArray and _defaultArray
		// as well
		//
		for (int i = m_baseAdd; i < m_base.length; ++i)
		{
			if (m_base[i] == SHORT_MIN)
				m_base[i] = (short)end;
		}
		for (int i = 0; i < m_default.length; ++i)
		{
			if (m_default[i] == SHORT_MIN)
				m_default[i] = defaultState;
		}
	}

	short[] getNext ()
	{
		return m_next;
	}

	short[] getCheck ()
	{
		return m_check;
	}

	short[] getBase ()
	{
		return m_base;
	}

	int getBaseAdd ()
	{
		return m_baseAdd;
	}

	short[] getDefault ()
	{
		if (m_useStateDiff)
			return m_default;
		return null;
	}
}
