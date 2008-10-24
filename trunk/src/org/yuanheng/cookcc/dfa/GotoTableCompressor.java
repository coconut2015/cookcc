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

	int _baseAdd;

	private short[] _defaultArray;

	private short[] _nextArray;
	private short[] _checkArray;
	private short[] _baseArray;

	private Map<Integer, Vector<Short>> _fillList = new HashMap<Integer, Vector<Short>> ();

	public GotoTableCompressor (Vector<short[]> gotoTable)
	{
		m_dfa = gotoTable;
		m_dfaCopy = new Vector<short[]> ();
		for (short[] column : m_dfa)
			m_dfaCopy.add (column.clone ());
	}

	private boolean isEmpty (int state)
	{
		short[] column = m_dfa.get (state);
		for (short c : column)
			if (c != 0)
				return false;
		return true;
	}

	private int getStateDiff (int thisState, int cmpState)
	{
		short[] column1 = m_dfa.get (thisState);
		short[] column2 = m_dfa.get (cmpState);

		int diff = 0;

		for (int i = 0; i < column1.length; ++i)
			if (column1[i] != 0 && column1[i] != column2[i])
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

			for (int i = 0; i < column1.length; ++i)
				if (column1[i] == 0 || column1[i] == column2[i])
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
		minMax[1]= i;

		//DEBUGMSG ("row = " << row << ", min = " << min << ", max = " << max);
		return minMax[1] - minMax[0] + 1;
	}

	//
	// return if can fill the row at the position
	//
	boolean canFill (int row, int min, int max, int pos)
	{
		int bound = _nextArray.length;
		short[] column = m_dfaCopy.get (row);
		for (; pos < bound && min <= max; ++pos, ++min)
		{
			if (column[min] != SHORT_MIN &&
				_nextArray[pos] != SHORT_MIN)
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
		if (bound > _nextArray.length)
		{
			//DEBUGMSG ("trying to resize to " << bound);
			_nextArray = TableCompressor.resize (_nextArray, bound, SHORT_MIN);
			_checkArray = TableCompressor.resize (_checkArray, bound, SHORT_MIN);
		}

		int rowIndex = row + _baseAdd;

		_baseArray[rowIndex] = (short)(pos - min);

		// now do the fill
		short[] column = m_dfaCopy.get (row);
		for (; min <= max; ++pos, ++min)
		{
			if (column[min] != SHORT_MIN)
			{
				_nextArray[pos] = column[min];
				_checkArray[pos] = (short)rowIndex;
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

		int size = _nextArray.length;
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
		doFill (row, min, max, _nextArray.length);
	}

	void doInsertState (short state, short cmpState)
	{
		_defaultArray[state] = cmpState;
		cleanState (state, cmpState);

		int[] minMax = new int[2];
		int blockSize = getBlockSize (state, minMax);

		if (blockSize == 0)
			return;

		int holeSize = getHoleSize (state, minMax[0], minMax[1]);

		Vector<Short> list = _fillList.get (holeSize);
		if (list == null)
		{
			list = new Vector<Short> ();
			_fillList.put (holeSize, list);
		}
		list.add (state);
	}

	void doFillStates ()
	{
		int[] minMax = new int[2];


		Integer[] holeSizes = _fillList.keySet ().toArray (new Integer[_fillList.size ()]);

		// fill the states from the biggest to the smallest
		for (int i = holeSizes.length - 1; i >= 0; --i)
		{
			Integer holeSize = holeSizes[i];
			for (Short state : _fillList.get (holeSize))
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
	void compute ()
	{
		_baseAdd = _baseArray.length;

		_baseArray = TableCompressor.resize (_baseArray, _baseArray.length + m_dfaCopy.size (), SHORT_MIN);
		_defaultArray = TableCompressor.resize (_defaultArray, m_dfaCopy.size (), SHORT_MIN);

		for (short i = 0; i < m_dfaCopy.size (); ++i)
		{
			// ignore those empty ones
			if (isEmpty (i))
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
			if (minState < i)
				doInsertState (i, minState);
			else
				doInsertState (i, SHORT_MIN);
		}

		doFillStates ();

		// shrink base and default array to save some space
		for (int i = _baseArray.length - 1; i > 0; --i)
		{
			if (_baseArray[i] != SHORT_MIN)
			{
//				_baseArray.resize (i + 1);
//				_defaultArray.resize (i + 1 - _baseAdd);
				break;
			}
		}

		//
		// process all SHORT_MIN in _checkArray and _nextArray to
		// some less high magnitude value
		//
		for (int i = 0; i < _checkArray.length; ++i)
		{
			if (_checkArray[i] == SHORT_MIN)
				_checkArray[i] = -1;
			if (_nextArray[i] == SHORT_MIN)
				_nextArray[i] = 0;
		}

		//
		// process all SHORT_MIN in _baseArray and _defaultArray
		// as well
		//
		for (int i = _baseAdd; i < _baseArray.length; ++i)
		{
			if (_baseArray[i] == SHORT_MIN)
				_baseArray[i] = 0;
		}
		for (int i = 0; i < _defaultArray.length; ++i)
		{
			if (_defaultArray[i] == SHORT_MIN)
				_defaultArray[i] = 0;
		}

		/*
		_osSource << sizeType << ' ' << prefix << "_goto_default[" << _defaultArray.size () << "] =" << outEndl
		<< _defaultArray << ";" << outEndl << outEndl;

		_osSource << sizeType << ' ' << prefix << "_base[" << _baseArray.size () << "] =" << outEndl
		<< _baseArray << ";" << outEndl << outEndl;
		_osSource << sizeType << ' ' << prefix << "_next[" << _nextArray.size () << "] =" << outEndl
		<< _nextArray << ";" << outEndl << outEndl;
		_osSource << sizeType << ' ' << prefix << "_check[" << _checkArray.size () << "] =" << outEndl
		<< _checkArray << ";" << outEndl << outEndl;

		_osSource << "/////////////////////////////////////////////////////////" << outEndl
		<< "//" << outEndl
		<< "// GOTO state lookup macro" << outEndl
		<< "//" << outEndl
		<< "/////////////////////////////////////////////////////////" << outEndl
		<< outEndl
		<< "#define " << macroName << "_BASEADD " << _baseAdd << outEndl
		<< "#define " << macroName << "(outstate,instate,a)\tregister int e = a; \\" << outEndl
		<< "\t\t\t\toutstate = instate + " << _baseAdd << "; \\" << outEndl
		<< "\t\t\t\twhile (" << prefix << "_check[e + " << prefix << "_base[outstate]] != outstate) \\" << outEndl
		<< "\t\t\t\t  outstate = " << prefix << "_goto_default[outstate]; \\" << outEndl
		<< "\t\t\t\toutstate = " << prefix << "_next[e + " << prefix << "_base[outstate]];" << outEndl
		<< outEndl;
		*/
	}
}
