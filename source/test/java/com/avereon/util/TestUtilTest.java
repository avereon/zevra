package com.avereon.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestUtilTest {

	@Test
	void testIsTest() {
		boolean isTest = TestUtil.isTest();
		if( !isTest ) new Throwable( "Unable to determine test runner from thread stack:" ).printStackTrace();
		assertThat( TestUtil.isTest() ).withFailMessage( "Unable to determine test runner from thread stack" ).isTrue();
	}

}
