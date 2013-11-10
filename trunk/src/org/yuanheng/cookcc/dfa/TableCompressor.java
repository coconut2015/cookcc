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

import java.util.Arrays;
import java.util.TreeMap;
import java.util.Vector;

import org.yuanheng.cookcc.lexer.ECS;

/**
 * @author Heng Yuan
 * @version $Id$
 */
class TableCompressor
{
	private final static short SHORT_MIN = Short.MIN_VALUE;

	private static class ErrorVector implements Comparable<ErrorVector>
	{
		private final short m_defaultValue;
		private short[] m_error;

		public ErrorVector (DFARow row, short defaultValue)
		{
			m_error = row.getStates ().clone ();
			m_defaultValue = defaultValue;

			// We can't use SHORT_MIN as a flag since we could potentially
			// have values that are
			for (int i = 0; i < m_error.length; i++)
				m_error[i] = (m_error[i] == 0) ? 0 : (m_error[i] == defaultValue) ? defaultValue : 0;
		}

		public short[] getError ()
		{
			return m_error;
		}

		public void setError (short[] error)
		{
			m_error = error;
		}

		public short getDefaultValue ()
		{
			return m_defaultValue;
		}

		public int compareTo (ErrorVector other)
		{
			int size = m_error.length;
			for (int i = 0; i < size; ++i)
			{
				if (m_error[i] == other.m_error[i] ||
//					m_error[i] == SHORT_MIN ||
					other.m_error[i] == SHORT_MIN)
					continue;

				return m_error[i] - other.m_error[i];
			}
			return 0;
		}
	}

	private final DFATable m_dfa;
	private final DFATable m_dfaCopy;

	// the minimum repeat percentage a default value has to have, other
	// wise, we won't use the error state
	private final int MINREPEAT = 50;
	// the number is used to tip the balance between choosing the
	// state diff or using error state
	private final int BALANCE;
	private final int GOODREPEAT;
	private final int m_rowSize;

	private Vector<ErrorVector> m_errors = new Vector<ErrorVector> ();
	private TreeMap<ErrorVector, Short> m_errorMap = new TreeMap<ErrorVector, Short> ();
	private TreeMap<Integer, Vector<Short>> m_fillMap = new TreeMap<Integer, Vector<Short>> ();

	private short[] m_next;
	private short[] m_check;
	private short[] m_base;
	private short[] m_default;

	private ECS m_ecsError;

	private boolean m_useDefault = true;
	private boolean m_useMeta = true;
	private boolean m_useError = true;
	private boolean m_useStateDiff;

	public TableCompressor (DFATable dfa)
	{
		m_dfa = dfa;
		m_dfaCopy = dfa.clone ();

		m_rowSize = dfa.getRow (0).getStates ().length;

		BALANCE = m_rowSize / 10;
		GOODREPEAT = m_rowSize * MINREPEAT / 100;

		m_ecsError = new ECS (m_rowSize - 1);

		m_default = resize (null, m_dfaCopy.size (), SHORT_MIN);
		m_base = new short[m_dfaCopy.size ()];
	}

	static short[] resize (short[] src, int newSize, short fill)
	{
		if (src != null)
		{
			if (src.length == newSize)        // no need to do anything in this case.
				return src;
		}

		short[] newArray = new short[newSize];
		int start;
		if (src != null)
		{
			System.arraycopy (src, 0, newArray, 0, src.length);
			start = src.length;
		}
		else
			start = 0;
		for (int i = start; i < newSize; ++i)
			newArray[i] = fill;
		return newArray;
	}

	//
	// Statistics gathering routines
	//

	// find out the # of zeros (i.e. errors) in the state
	private int getErrorCount (int state)
	{
		int count = 0;
		short[] cols = m_dfa.getRow (state).getStates ();
		for (short c : cols)
			if (c == 0)
				++count;
		return count;
	}

	// find out the # of values that are neither the given
	// repeat value nor 0
	private int getNonDefaultDiff (int state, short repeatValue)
	{
		int diff = 0;
		short[] cols = m_dfa.getRow (state).getStates ();

		for (short c : cols)
			if (c != repeatValue && c != 0)
				++diff;

		return diff;
	}

	// get the difference between two states
	private int getStateDiff (int state1, int state2)
	{
		int diff = 0;
		short[] cols1 = m_dfa.getRow (state1).getStates ();
		short[] cols2 = m_dfa.getRow (state2).getStates ();

		for (int i = 0; i < cols1.length; ++i)
			if (cols1[i] != cols2[i])
				++diff;

		return diff;
	}

	//
	// process the DFA state such that it can be used for compression
	// note, it destroys the DFA in the process!
	//
	private void cleanStateRepeat (int state, int repeatValue)
	{
		short[] cols = m_dfaCopy.getRow (state).getStates ();
		for (int i = 0; i < cols.length; ++i)
			if (cols[i] == repeatValue || cols[i] == 0)
				cols[i] = SHORT_MIN;
	}

	//
	// process the DFA state such that it can be used for compression
	// note, it destroys the DFA in the process!
	//
	private void cleanStateDiff (int state1, int state2)
	{
		short[] cols1 = m_dfaCopy.getRow (state1).getStates ();
		short[] cols2 = m_dfa.getRow (state2).getStates ();

		for (int i = 0; i < cols1.length; ++i)
			if (cols1[i] == cols2[i])
				cols1[i] = SHORT_MIN;
	}

	//
	// Get the difference between two states, and also retrieves
	// the block min and max index.
	//
	private int getStateDiffBlock (int state1, int state2, int[] minMax)
	{
		short[] cols1 = m_dfa.getRow (state1).getStates ();
		short[] cols2 = m_dfa.getRow (state2).getStates ();

		int size = cols1.length;
		int i;

		// scan from the left side
		for (i = 0; i < size; i++)
		{
			if (cols1[i] != cols2[i])
				break;
		}

		if (i == size)
		{
			minMax[0] = 0;
			minMax[1] = 0;
			return 0;
		}

		minMax[0] = i;

		// scan from the right side
		for (i = size - 1; i >= 0; i--)
		{
			if (cols1[i] != cols2[i])
				break;
		}
		minMax[1] = i;

		return minMax[1] - minMax[0] + 1;
	}

	//
	// obtain the min max of the processed DFA block
	// and return the block size
	//
	private int getBlockSize (int state, int[] minMax)
	{
		short[] cols = m_dfaCopy.getRow (state).getStates ();

		int size = cols.length;
		int i;

		// scan from left
		for (i = 0; i < size; ++i)
			if (cols[i] != SHORT_MIN)
				break;

		if (i == size)
		{
			minMax[0] = 0;
			minMax[1] = 0;
			return 0;
		}

		minMax[0] = i;

		// scan from right
		for (i = size - 1; i > 0; --i)
			if (cols[i] != SHORT_MIN)
				break;

		minMax[1] = i;

		return minMax[1] - minMax[0] + 1;
	}

	private int getHoleSize (int state, int min, int max)
	{
		int holes = 0;
		short[] cols = m_dfaCopy.getRow (state).getStates ();
		for (int i = min; i <= max; ++i)
			if (cols[i] == SHORT_MIN)
				++holes;
		return holes;
	}

	//
	// obtain the min max of the processed error block
	// and return the block size
	//
	private int getErrorBlockSize (int state, int[] minMax)
	{
		short[] cols = m_errors.get (state - m_dfaCopy.size ()).getError ();
		int size = cols.length;
		int i;
		for (i = 0; i < size; ++i)
			if (cols[i] != SHORT_MIN)
				break;

		if (i == size)
		{
			minMax[0] = 0;
			minMax[1] = 0;
			return 0;
		}

		minMax[0] = i;

		for (i = size - 1; i > 0; --i)
			if (cols[i] != SHORT_MIN)
				break;

		minMax[1] = i;

		return minMax[1] - minMax[0] + 1;
	}

	//
	// determine # of holes needs to be filled in thisState
	//
	private int getErrorHoleSize (int state, int min, int max)
	{
		int holes = 0;
		short[] cols = m_errors.get (state - m_dfaCopy.size ()).getError ();
		for (int i = min; i <= max; ++i)
			if (cols[i] == SHORT_MIN)
				++holes;
		return holes;
	}

	//
	// findRepeat finds the most repeated value in a DFA row other
	// than zero.  If all values are zeros, then just count of zero
	// is reported
	//
	private static int findRepeat (DFARow row, short[] repeatValue)
	{
		short lastRepeat;
		int i;
		int repeatCount;
		int lastRepeatCount;

		// first duplicate the column
		short[] cols = row.getStates ().clone ();
		Arrays.sort (cols);
		int size = cols.length;

		// then find the highest repeated value
		lastRepeat = 0;
		lastRepeatCount = 0;
		repeatCount = 1;

		// count the error states first
		for (i = 0; i < size; i++)
		{
			if (cols[i] != 0)
				break;
		}

		if (i == size) // hmm, all transitions are error transitions
		{
			repeatValue[0] = 0;
			return i;
		}

		i++;
		for (; i < size; i++)
		{
			if (cols[i] == cols[i - 1])
				repeatCount++;
			else
			{
				if (repeatCount > lastRepeatCount)
				{
					lastRepeatCount = repeatCount;
					lastRepeat = cols[i - 1];
				}
				repeatCount = 1;
			}
		}

		if (repeatCount > lastRepeatCount)
		{
			lastRepeatCount = repeatCount;
			lastRepeat = cols[i - 1];
		}

		repeatValue[0] = lastRepeat;
		return lastRepeatCount;
	}


	//
	// figure out the error states
	//
	private short addErrorState (int thisState, short defaultValue)
	{
		ErrorVector ev = new ErrorVector (m_dfaCopy.getRow (thisState), defaultValue);

		//
		// Check if the state is all errors.  If so, we could simply
		// make the default state as SHRT_MIN
		//
		int i;
		short[] cols = ev.getError ();
		for (i = 0; i < cols.length; i++)
			if (cols[i] != 0 && cols[i] != SHORT_MIN)
				break;
		if (i == cols.length)
			return SHORT_MIN;

		//
		// then check if the error state is already in the error array
		//
		Short errorId = m_errorMap.get (ev);
		if (errorId != null)
			return errorId.shortValue ();

		//
		// so the error is new
		//
		m_errors.add (ev);

		/*
		//
		// give the insignificant values some value
		//
		for (i = 0; i < cols.length; ++i)
		{
			if (cols[i] == SHORT_MIN)
				cols[i] = defaultValue;
		}
		*/

		//
		// check if it creates new equivalent classes
		//
		m_ecsError.add (ev.getError ());

		//
		// then add the array iterator to the search set
		//
		errorId = new Short ((short)(m_dfaCopy.size () + m_errors.size () - 1));
		m_errorMap.put (ev, errorId);
		return errorId.shortValue ();
	}

	private void addBlock (short thisState)
	{
		int[] minMax = new int[2];
		int blockSize = getBlockSize (thisState, minMax);
		int min = minMax[0];
		int max = minMax[1];

		// don't have to do fill for block size of 0
		if (blockSize > 0)
		{
			Integer holes = new Integer (getHoleSize (thisState, min, max));
			Vector<Short> v = m_fillMap.get (holes);
			if (v == null)
			{
				v = new Vector<Short> ();
				m_fillMap.put (holes, v);
			}
			v.add (new Short (thisState));
		}
	}

	//
	//   create block/hole information of state with a repeatValue
	//
	private void processStateRepeat (short state, short repeatValue, int repeatCount)
	{
		//
		// check if there are enough repeats for the repeatValue.
		// if not, we will disregard it as a repeatValue and saves
		// us from storing the error state.  This step also greatly
		// reduces # of error state equivalent classes
		//

		if (repeatCount == 1 || repeatCount < GOODREPEAT)
		{
			repeatValue = 0;
			m_default[state] = SHORT_MIN;
		}
		else
		{
			// we calculate an error state as well
			m_default[state] = addErrorState (state, repeatValue);
		}

		cleanStateRepeat (state, repeatValue);
		addBlock (state);
	}

	//
	//   create block/hole information of thisState using cmpState as
	//   the template.
	//
	private void processStateDiff (short thisState, short cmpState)
	{
		m_useStateDiff = true;

		m_default[thisState] = cmpState;
		cleanStateDiff (thisState, cmpState);

		addBlock (thisState);
	}

	//
	//   shrink ErrorVector's in _errorArray and error vector's
	//   default
	//
	private void processErrorStates ()
	{
		int size = m_errors.size ();
		int[] minMax = new int[2];
		for (int i = 0; i < size; ++i)
		{
			ErrorVector ev = m_errors.get (i);
			int errorGroups = m_ecsError.getGroupCount ();
			short[] cols = ev.getError ();
			short[] newArray = new short[errorGroups];
			int[] groups = m_ecsError.getLookup ();
			for (int j = 0; j < errorGroups; ++j)
			{
				if (cols[groups[j]] == 0)
					newArray[j] = SHORT_MIN;
//					newArray[j] = 0;
				else
					newArray[j] = cols[groups[j]];
			}
			ev.setError (newArray);

			int stateNum = i + m_dfaCopy.size ();

			int blockSize = getErrorBlockSize (stateNum, minMax);
			// don't have to do fill for block size of 0
			if (blockSize > 0)
			{
				Integer holes = new Integer (getErrorHoleSize (stateNum, minMax[0], minMax[1]));

				Vector<Short> v = m_fillMap.get (holes);
				if (v == null)
				{
					v = new Vector<Short> ();
					m_fillMap.put (holes, v);
				}
				v.add (new Short ((short)stateNum));
			}
		}
	}

	//
	//   1st parse each DFA states into _noCompressList, _fullCompressList
	//   and _blockListMap.  For states that contain holes, also put it into
	//   _holeSet.
	//
	//   2nd step basically fills all holes in the _holeSet
	//
	private void processDFAStates ()
	{
		short i;
		int repeatCount;
		short[] repeatValue = new short[1];

		//
		// process each state to see the repeats, holes, etc
		//
		int diff;
		short cmpState;

		for (i = 0; i < m_dfaCopy.size (); i++)
		{
			repeatCount = findRepeat (m_dfaCopy.getRow (i), repeatValue);

			int hardDiff = getErrorCount (i);
			diff = getNonDefaultDiff (i, repeatValue[0]);
			cmpState = i;

			if (repeatCount < GOODREPEAT)
				diff = hardDiff;

			// looking for a state that minimizes the
			// difference
			if (repeatCount != m_rowSize)
			{
				int stateDiff = m_rowSize;
				short stateCmp = 0;

				// find the minimal difference state
				for (short j = 0; j < i; ++j)
				{
					int d = getStateDiff (i, j);
					if (d < stateDiff)
					{
						stateCmp = j;
						stateDiff = d;
					}
					else if (d == stateDiff)
					{
						// for two same diff's, pick the smaller block size
						int[] minMax = new int[2];
						int b1, b2;
						b1 = getStateDiffBlock (i, stateCmp, minMax);
						b2 = getStateDiffBlock (i, j, minMax);
						if (b2 < b1)
						{
							stateCmp = j;
							stateDiff = d;
						}
					}
				}

				if (stateDiff < diff + BALANCE)
				{
					cmpState = stateCmp;
					diff = stateDiff;
				}
			}

			// process the DFA state and add it to error state where
			// applicable

			if (i == cmpState)
				processStateRepeat (i, repeatValue[0], repeatCount);
//				processStateRepeat (i, (short)0, 0);
			else
				processStateDiff (i, cmpState);
		}
	}

	private boolean canFill (int state, int min, int max, int pos)
	{
		if (state < m_dfaCopy.size ())
		{
			int bound = m_next.length;
			short[] column = m_dfaCopy.getRow (state).getStates ();
			for (pos = pos + min; pos < bound && min <= max; ++pos, ++min)
			{
				if (column[min] != SHORT_MIN &&
					m_next[pos] != SHORT_MIN)
					return false;
			}
			return true;
		}
		else
		{
			int bound = m_next.length;
			short[] column = m_errors.get (state - m_dfaCopy.size ()).getError ();
			for (pos = pos + min; pos < bound && min <= max; ++pos, ++min)
			{
				if (column[min] != SHORT_MIN &&
					m_next[pos] != SHORT_MIN)
					return false;
			}
			return true;
		}
	}

	//
	// do the actual filling
	//
	private void doFill (short state, int min, int max, int pos)
	{
		int bound = pos + max + 1;

		// allocate space if necessary
		if (bound > m_next.length)
		{
			m_next = resize (m_next, bound, SHORT_MIN);
			m_check = resize (m_check, bound, SHORT_MIN);
		}

		m_base[state] = (short)pos;

		// now do the fill
		if (state < m_dfaCopy.size ())
		{
			short[] column = m_dfaCopy.getRow (state).getStates ();
			for (pos = pos + min; min <= max; ++pos, ++min)
			{
				if (column[min] != SHORT_MIN)
				{
					m_next[pos] = column[min];
					m_check[pos] = state;
				}
			}
		}
		else
		{
			short[] column = m_errors.get (state - m_dfaCopy.size ()).getError ();
			for (pos = pos + min; min <= max; ++pos, ++min)
			{
				if (column[min] != SHORT_MIN)
				{
					m_next[pos] = column[min];
					m_check[pos] = state;
				}
			}
		}
	}

	private void doFillState (short state)
	{
		// pretty dumb algorithm here
		//
		// just going through all the holes and see
		// if there is an available position

		int[] minMax = new int[2];

		if (state < m_dfaCopy.size ())
			getBlockSize (state, minMax);
		else
			getErrorBlockSize (state, minMax);

		int size = m_next.length;

		int min = minMax[0];
		int max = minMax[1];

		for (int i = 0; i < size; ++i)
		{
			if (canFill (state, min, max, i))
			{
				doFill (state, min, max, i);
				return;
			}
		}
		doFill (state, min, max, size);
	}

	private void doFillStates ()
	{
		Integer[] holeSizes = m_fillMap.keySet ().toArray (new Integer[m_fillMap.size ()]);

		// fill the states from the biggest to the smallest
		for (int i = holeSizes.length - 1; i >= 0; --i)
		{
			Integer holeSize = holeSizes[i];
			for (Short state : m_fillMap.get (holeSize))
				doFillState (state.shortValue ());
		}
	}

	void compute ()
	{
		m_next = new short[0];
		m_check = new short[0];
		m_default = resize (m_default, m_dfaCopy.size (), SHORT_MIN);
		m_base = resize (m_base, m_dfaCopy.size (), (short)0);

		processDFAStates ();

		//
		// expand the base/default array to count error vectors
		//
		m_base = resize (m_base, m_dfaCopy.size () + m_errors.size (), (short)0);
		m_default = resize (m_default, m_dfaCopy.size () + m_errors.size (), SHORT_MIN);

		//
		// determine if _yy_meta is necessary
		//
		if (m_ecsError.getGroupCount () <= 1)
			m_useMeta = false;
		else
			m_useMeta = true;

		//
		// determine if we really needs error states
		//
		if (!m_useMeta && !m_useStateDiff)
			m_useError = false;
		else
			m_useError = true;

		//
		// process the error states
		//
		if (m_useError)
			processErrorStates ();

		//
		// fill the table
		//
		doFillStates ();

		//
		// check if we really needs m_default
		//
		m_useDefault = false;
		for (int i = 0; i < m_default.length; ++i)
			if (m_default[i] != SHORT_MIN)
			{
				m_useDefault = true;
				break;
			}
		if (!m_useDefault)
			m_default = new short[0];

		//
		// check if the indices at the end are all valid
		//
		int expand;
		if (m_useDefault)
			expand = m_ecsError.getGroupCount ();    // reserve space for default error state
		else
			expand = 0;

		for (int i = m_next.length - m_rowSize; i < m_next.length; ++i)
		{
			if (i < 0)
				i = 0;
			if (m_check[i] == SHORT_MIN)
				continue;

			if (m_check[i] < m_dfaCopy.size ())
			{
				if (m_base[m_check[i]] + m_rowSize > m_next.length + expand)
				{
					expand = m_base[m_check[i]] + m_rowSize - m_next.length;
				}
			}
			else
			{
				if (m_base[m_check[i]] + m_ecsError.getGroupCount () >
					m_next.length + expand)
				{
					expand = m_base[m_check[i]] + m_ecsError.getGroupCount () - m_next.length;
				}
			}
		}

		short defaultError = (short)m_base.length;

		if (m_useDefault)
		{
			m_default = resize (m_default, m_default.length + 1, defaultError);
			m_base = resize (m_base, m_base.length + 1, (short)m_next.length);
		}
		m_next = resize (m_next, m_next.length + expand, (short)0);
		m_check = resize (m_check, m_next.length, defaultError);

		//
		// now processm_default
		//
		if (m_useDefault)
		{
			if (m_useError)
				for (int i = 0; i < m_default.length; ++i)
				{
					if (m_default[i] == SHORT_MIN)
						m_default[i] = defaultError;
				}
			else
				for (int i = 0; i < m_default.length; ++i)
				{
					if (m_default[i] == SHORT_MIN)
						m_default[i] = 0;
					else
						m_default[i] = m_errors.get (m_default[i] - m_dfaCopy.size ()).getDefaultValue ();
				}
		}
		else
			m_default = null;

		//
		// process all SHRT_MIN in m_check and m_next to 0
		//
		for (int i = 0; i < m_check.length; ++i)
		{
			if (m_check[i] == SHORT_MIN)
				m_check[i] = defaultError;
			if (m_next[i] == SHORT_MIN)
				m_next[i] = 0;
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

	short[] getDefault ()
	{
		return m_default;
	}

	short[] getMeta ()
	{
		if (m_useDefault && m_useMeta)
		{
			int[] groups = m_ecsError.getGroups ();
			short[] meta = new short[groups.length];
			for (int i = 0; i < groups.length; ++i)
				meta[i] = (short)groups[i];
			return meta;
		}
		return null;
	}

	boolean getError ()
	{
		return m_useError;
	}
}
