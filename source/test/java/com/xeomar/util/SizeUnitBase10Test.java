package com.xeomar.util;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SizeUnitBase10Test {

	@Test
	public void testConstants() {
		assertThat( SizeUnitBase10.KB.getSize(), is( 1000L ) );
		assertThat( SizeUnitBase10.MB.getSize(), is( 1000000L ) );
		assertThat( SizeUnitBase10.GB.getSize(), is( 1000000000L ) );
		assertThat( SizeUnitBase10.TB.getSize(), is( 1000000000000L ) );
		assertThat( SizeUnitBase10.PB.getSize(), is( 1000000000000000L ) );
		assertThat( SizeUnitBase10.EB.getSize(), is( 1000000000000000000L ) );
	}

}
