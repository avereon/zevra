package com.avereon.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class NumberUtilTest {

	@Test
	void testParseInt() {
		assertThat( NumberUtil.parseInt( null, -1 ), is( -1 ) );
		assertThat( NumberUtil.parseInt( "null", -1 ), is( -1 ) );
		assertThat( NumberUtil.parseInt( "0", -1 ), is( 0 ) );
	}

	@Test
	void testParseLong() {
		assertThat( NumberUtil.parseLong( null, -1L ), is( -1L ) );
		assertThat( NumberUtil.parseLong( "null", -1L ), is( -1L ) );
		assertThat( NumberUtil.parseLong( "0", -1L ), is( 0L ) );
	}

	@Test
	void testParseFloat() {
		assertThat( NumberUtil.parseFloat( null, -1L ), is( -1F ) );
		assertThat( NumberUtil.parseFloat( "null", -1L ), is( -1F ) );
		assertThat( NumberUtil.parseFloat( "0", -1L ), is( 0F ) );
	}

	@Test
	void testParseDouble() {
		assertThat( NumberUtil.parseDouble( null, -1L ), is( -1.0 ) );
		assertThat( NumberUtil.parseDouble( "null", -1L ), is( -1.0 ) );
		assertThat( NumberUtil.parseDouble( "0", -1L ), is( 0.0 ) );
	}

}
