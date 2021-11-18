package com.avereon.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadUtilTest {

	@Test
	void testPause() {
		long length = 100;
		long start = System.nanoTime();
		ThreadUtil.pause( length );
		long stop = System.nanoTime();

		// Compute the delta in nanoseconds
		long delta = stop - start;

		assertThat( delta ).isGreaterThanOrEqualTo( length * 1000 );
	}

	@Test
	void testCalledFrom() {
		assertThat( ThreadUtil.calledFrom( "ThreadUtilTest", "notHere" ) ).isFalse();
		assertThat( ThreadUtil.calledFrom( "ThreadUtilTest", "testCalledFrom" ) ).isTrue();
		assertThat( ThreadUtil.calledFrom( "com.parallelsymmetry.utility.ThreadUtilTest", "testCalledFrom" ) ).isTrue();
	}

	@Test
	void testAppendStackTraceWithNullSource() {
		Throwable target = new Throwable();
		StackTraceElement[] trace = target.getStackTrace();
		assertThat( ThreadUtil.appendStackTrace( (Throwable)null, target ).getStackTrace() ).containsSequence( trace );
	}

	@Test
	void testAppendStackTraceWithNullTarget() {
		Throwable source = new Throwable();
		StackTraceElement[] trace = source.getStackTrace();
		assertThat( ThreadUtil.appendStackTrace( source, null ).getStackTrace() ).containsSequence( trace );
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

		assertThat( ThreadUtil.appendStackTrace( source, target ).getStackTrace() ).containsSequence( elements );
	}

	@Test
	void testGetStackClasses() {
		Class<?>[] frame = ThreadUtil.getStackClasses();
		assertThat( frame[ 0 ] ).isEqualTo( ThreadUtilTest.class );
	}

}
