package com.xeomar.razor;

import com.xeomar.razor.SizeUnit;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class SizeUnitTest {

	@Test
	public void testConstants() throws Exception {
		assertEquals( 1000L, SizeUnit.KB.getSize() );
		assertEquals( 1000000L, SizeUnit.MB.getSize() );
		assertEquals( 1000000000L, SizeUnit.GB.getSize() );
		assertEquals( 1000000000000L, SizeUnit.TB.getSize() );
		assertEquals( 1000000000000000L, SizeUnit.PB.getSize() );
		assertEquals( 1000000000000000000L, SizeUnit.EB.getSize() );

		assertEquals( 1024L, SizeUnit.KiB.getSize() );
		assertEquals( 1048576L, SizeUnit.MiB.getSize() );
		assertEquals( 1073741824L, SizeUnit.GiB.getSize() );
		assertEquals( 1099511627776L, SizeUnit.TiB.getSize() );
		assertEquals( 1125899906842624L, SizeUnit.PiB.getSize() );
		assertEquals( 1152921504606846976L, SizeUnit.EiB.getSize() );
	}

}
