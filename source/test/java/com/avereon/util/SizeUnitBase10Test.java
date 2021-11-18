package com.avereon.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SizeUnitBase10Test {

	@Test
	void testConstants() {
		assertThat( SizeUnitBase10.KB.getSize()).isEqualTo( 1000L );
		assertThat( SizeUnitBase10.MB.getSize()).isEqualTo( 1000000L );
		assertThat( SizeUnitBase10.GB.getSize()).isEqualTo( 1000000000L );
		assertThat( SizeUnitBase10.TB.getSize()).isEqualTo( 1000000000000L );
		assertThat( SizeUnitBase10.PB.getSize()).isEqualTo( 1000000000000000L );
		assertThat( SizeUnitBase10.EB.getSize()).isEqualTo( 1000000000000000000L );
	}

}
