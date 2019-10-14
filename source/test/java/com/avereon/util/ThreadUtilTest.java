package com.avereon.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

class ThreadUtilTest {

	@Test
	void testPause() {
		long length = 100;
		long start = System.nanoTime();
		ThreadUtil.pause( length );
		long stop = System.nanoTime();
		long delta = stop - start;
		assertTrue( delta >= length * 1000, "Delta: " + delta );
	}

	@Test
	void testCalledFrom() {
		assertFalse( ThreadUtil.calledFrom( "ThreadUtilTest", "notHere" ) );
		assertTrue( ThreadUtil.calledFrom( "ThreadUtilTest", "testCalledFrom" ) );
		assertTrue( ThreadUtil.calledFrom( "com.parallelsymmetry.utility.ThreadUtilTest", "testCalledFrom" ) );
	}

	@Test
	void testAppendStackTraceWithNullSource() {
		Throwable target = new Throwable();
		StackTraceElement[] trace = target.getStackTrace();
		assertArrayEquals( trace, ThreadUtil.appendStackTrace( (Throwable)null, target ).getStackTrace() );
	}

	@Test
	void testAppendStackTraceWithNullTarget() {
		Throwable source = new Throwable();
		StackTraceElement[] trace = source.getStackTrace();
		assertArrayEquals( trace, ThreadUtil.appendStackTrace( source, null ).getStackTrace() );
	}

	@Test
	void testAppendStackTrace() {
		Throwable source = new Throwable();
		Throwable target = new Throwable();

		StackTraceElement[] sourceTrace = source.getStackTrace();
		StackTraceElement[] targetTrace = target.getStackTrace();

		StackTraceElement[] elements = new StackTraceElement[ targetTrace.length + sourceTrace.length ];
		System.arraycopy( targetTrace, 0, elements, 0, targetTrace.length );
		System.arraycopy( sourceTrace, 0, elements, targetTrace.length, sourceTrace.length );

		assertArrayEquals( elements, ThreadUtil.appendStackTrace( source, target ).getStackTrace() );
	}

	@Test
	void testGetStackClasses() {
		Class<?>[] frame = ThreadUtil.getStackClasses();
		assertThat( frame[ 0 ], is( ThreadUtilTest.class ) );
	}

}
