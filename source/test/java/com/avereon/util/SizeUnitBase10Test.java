package com.avereon.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SizeUnitBase10Test {

	@Test
	void testConstants() {
		assertThat( SizeUnitBase10.KB.getSize(), is( 1000L ) );
		assertThat( SizeUnitBase10.MB.getSize(), is( 1000000L ) );
		assertThat( SizeUnitBase10.GB.getSize(), is( 1000000000L ) );
		assertThat( SizeUnitBase10.TB.getSize(), is( 1000000000000L ) );
		assertThat( SizeUnitBase10.PB.getSize(), is( 1000000000000000L ) );
		assertThat( SizeUnitBase10.EB.getSize(), is( 1000000000000000000L ) );
	}

}
