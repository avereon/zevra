package com.xeomar.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestUtil {

	private static Set<String> runners;

	private static Boolean test;

	static {
		runners = new HashSet<>();
		runners.add( "org.junit.runner.JUnitCore" );
		runners.add( "junit.framework.TestSuite" );
	}

	/**
	 * Check if the any thread is running in the JUnit test framework. This is
	 * done by searching the thread stack for the class junit.framework.TestSuite
	 * and caching the result. If the TestSuite class is found the method will
	 * return true.
	 *
	 * @return If any thread is being run by JUnit
	 */
	public static boolean isTest() {
		if( test != null ) return test;

		boolean result = false;
		Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
		for( Thread thread : stacks.keySet() ) {
			if( thread.getId() == 1 ) {
				StackTraceElement[] elements = stacks.get( thread );
				for( StackTraceElement element : elements ) {
					if( runners.contains( element.getClassName() ) ) {
						result = true;
						break;
					}
				}
			}
		}

		return test = result;
	}

}
