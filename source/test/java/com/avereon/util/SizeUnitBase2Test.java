package com.avereon.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SizeUnitBase2Test {

	@Test
	void testConstants() {
		assertThat( SizeUnitBase2.KiB.getSize()).isEqualTo( 1024L );
		assertThat( SizeUnitBase2.MiB.getSize()).isEqualTo( 1048576L );
		assertThat( SizeUnitBase2.GiB.getSize()).isEqualTo( 1073741824L );
		assertThat( SizeUnitBase2.TiB.getSize()).isEqualTo( 1099511627776L );
		assertThat( SizeUnitBase2.PiB.getSize()).isEqualTo( 1125899906842624L );
		assertThat( SizeUnitBase2.EiB.getSize()).isEqualTo( 1152921504606846976L );
	}

}
