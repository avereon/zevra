package com.avereon.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SizeUnitBase2Test {

	@Test
	void testConstants() {
		assertThat( SizeUnitBase2.KiB.getSize(), is( 1024L ) );
		assertThat( SizeUnitBase2.MiB.getSize(), is( 1048576L ) );
		assertThat( SizeUnitBase2.GiB.getSize(), is( 1073741824L ) );
		assertThat( SizeUnitBase2.TiB.getSize(), is( 1099511627776L ) );
		assertThat( SizeUnitBase2.PiB.getSize(), is( 1125899906842624L ) );
		assertThat( SizeUnitBase2.EiB.getSize(), is( 1152921504606846976L ) );
	}

}
