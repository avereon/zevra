package com.avereon.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestUtilTest {

	@Test
	void testIsTest() {
		boolean isTest = TestUtil.isTest();
		if( !isTest ) new Throwable( "Unable to determine test runner from thread stack:" ).printStackTrace();
		assertTrue( TestUtil.isTest(), "Unable to determine test runner from thread stack" );
	}

}
