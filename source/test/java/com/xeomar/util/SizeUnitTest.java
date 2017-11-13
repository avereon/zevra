package com.xeomar.util;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SizeUnitTest {

	@Test
	public void testConstants() throws Exception {
		assertThat( SizeUnit.KB.getSize(), is( 1000L ) );
		assertThat( SizeUnit.MB.getSize(), is( 1000000L ) );
		assertThat( SizeUnit.GB.getSize(), is( 1000000000L ) );
		assertThat( SizeUnit.TB.getSize(), is( 1000000000000L ) );
		assertThat( SizeUnit.PB.getSize(), is( 1000000000000000L ) );
		assertThat( SizeUnit.EB.getSize(), is( 1000000000000000000L ) );

		assertThat( SizeUnit.KiB.getSize(), is( 1024L ) );
		assertThat( SizeUnit.MiB.getSize(), is( 1048576L ) );
		assertThat( SizeUnit.GiB.getSize(), is( 1073741824L ) );
		assertThat( SizeUnit.TiB.getSize(), is( 1099511627776L ) );
		assertThat( SizeUnit.PiB.getSize(), is( 1125899906842624L ) );
		assertThat( SizeUnit.EiB.getSize(), is( 1152921504606846976L ) );
	}

}
