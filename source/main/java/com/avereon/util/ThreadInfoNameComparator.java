package com.avereon.util;

import java.lang.management.ThreadInfo;
import java.util.Comparator;

/**
 * Comparator implementation for comparing ThreadInfo objects based on their thread name.
 */
public class ThreadInfoNameComparator implements Comparator<ThreadInfo> {

	/**
	 * Compares two {@link ThreadInfo} objects based on their thread name.
	 *
	 * @param threadInfo1 the first ThreadInfo object to compare
	 * @param threadInfo2 the second ThreadInfo object to compare
	 * @return 0 if both threadInfo1 and threadInfo2 are null or their thread names are equal (ignore case),
	 * a negative value if threadInfo1 is null or its thread name comes before threadInfo2's thread name (ignore case),
	 * a positive value if threadInfo2 is null or its thread name comes before threadInfo1's thread name (ignore case)
	 */
	@Override
	public int compare( ThreadInfo threadInfo1, ThreadInfo threadInfo2 ) {
		String name1 = threadInfo1 == null ? null : threadInfo1.getThreadName();
		String name2 = threadInfo2 == null ? null : threadInfo2.getThreadName();

		if( name1 == null && name2 == null ) return 0;
		if( name1 == null ) return -1;
		if( name2 == null ) return 1;

		return threadInfo1.getThreadName().compareToIgnoreCase( threadInfo2.getThreadName() );
	}

}
