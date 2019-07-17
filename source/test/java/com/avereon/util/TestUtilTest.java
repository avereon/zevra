package com.avereon.util;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestUtilTest {

	@Test
	public void testIsTest() {
		boolean isTest = TestUtil.isTest();
		if( !isTest ) new Throwable( "Unable to determine test runner from thread stack:" ).printStackTrace();
		assertTrue( "Unable to determine test runner from thread stack", TestUtil.isTest() );
	}

}
