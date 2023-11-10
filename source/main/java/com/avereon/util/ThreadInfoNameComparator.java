package com.avereon.util;

import java.lang.management.ThreadInfo;
import java.util.Comparator;

public class ThreadInfoNameComparator implements Comparator<ThreadInfo> {

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
