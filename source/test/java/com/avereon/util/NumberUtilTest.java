package com.avereon.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NumberUtilTest {

	@Test
	void testParseInt() {
		assertThat( NumberUtil.parseInt( null, -1 ) ).isEqualTo( -1 );
		assertThat( NumberUtil.parseInt( "null", -1 ) ).isEqualTo( -1 );
		assertThat( NumberUtil.parseInt( "0", -1 ) ).isEqualTo( 0 );
	}

	@Test
	void testParseLong() {
		assertThat( NumberUtil.parseLong( null, -1L ) ).isEqualTo( -1L );
		assertThat( NumberUtil.parseLong( "null", -1L ) ).isEqualTo( -1L );
		assertThat( NumberUtil.parseLong( "0", -1L ) ).isEqualTo( 0L );
	}

	@Test
	void testParseFloat() {
		assertThat( NumberUtil.parseFloat( null, -1L ) ).isEqualTo( -1F );
		assertThat( NumberUtil.parseFloat( "null", -1L ) ).isEqualTo( -1F );
		assertThat( NumberUtil.parseFloat( "0", -1L ) ).isEqualTo( 0F );
	}

	@Test
	void testParseDouble() {
		assertThat( NumberUtil.parseDouble( null, -1L ) ).isEqualTo( -1.0 );
		assertThat( NumberUtil.parseDouble( "null", -1L ) ).isEqualTo( -1.0 );
		assertThat( NumberUtil.parseDouble( "0", -1L ) ).isEqualTo( 0.0 );
	}

}
