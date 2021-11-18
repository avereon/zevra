package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class DateUtilTest {

	@Test
	void testParse() {
		try {
			DateUtil.parse( "", null );
			fail( "Null format should cause a NullPointerException." );
		} catch( NullPointerException exception ) {
			// Intentionally ignore exception.
		}

		assertThat( DateUtil.parse( null, null ) ).isNull();
		assertThat( DateUtil.parse( null, "" ) ).isNull();
		assertThat( DateUtil.parse( "", "" ) ).isNull();

		assertThat( DateUtil.parse( "1970-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss" ) ).isEqualTo( new Date( 0 ) );
	}

	@Test
	void testParseWithTimeZone() {
		try {
			DateUtil.parse( "", null, (TimeZone)null );
			fail( "Null format should cause a NullPointerException." );
		} catch( NullPointerException exception ) {
			// Intentionally ignore exception.
		}

		try {
			DateUtil.parse( "", "", (TimeZone)null );
			fail( "Null time zone should cause a NullPointerException." );
		} catch( NullPointerException exception ) {
			// Intentionally ignore exception.
		}

		try {
			DateUtil.parse( "", null, (String)null );
			fail( "Null format should cause a NullPointerException." );
		} catch( NullPointerException exception ) {
			// Intentionally ignore exception.
		}

		try {
			DateUtil.parse( "", "", (String)null );
			fail( "Null time zone should cause a NullPointerException." );
		} catch( NullPointerException exception ) {
			// Intentionally ignore exception.
		}

		assertThat( DateUtil.parse( null, null, (TimeZone)null ) ).isNull();
		assertThat( DateUtil.parse( null, null, (String)null ) ).isNull();
		assertThat( DateUtil.parse( null, "", "" ) ).isNull();
		assertThat( DateUtil.parse( "", "", "" ) ).isNull();

		assertThat( DateUtil.parse( "1970-01-01 05:00:00", "yyyy-MM-dd HH:mm:ss", "GMT+05" ) ).isEqualTo( new Date( 0 ) );
	}

	@Test
	void testFormatDuration() {
		assertThat( DateUtil.formatDuration( 0L ) ).isEqualTo( "" );
		assertThat( DateUtil.formatDuration( 345L ) ).isEqualTo( "345ms" );
		assertThat( DateUtil.formatDuration( 1000L ) ).isEqualTo( "1s" );
		assertThat( DateUtil.formatDuration( 60000L ) ).isEqualTo( "1m" );
		assertThat( DateUtil.formatDuration( 3600000L ) ).isEqualTo( "1h" );
		assertThat( DateUtil.formatDuration( 3605000L ) ).isEqualTo( "1h 5s" );
		assertThat( DateUtil.formatDuration( 24 * 3600000L ) ).isEqualTo( "1d" );
		assertThat( DateUtil.formatDuration( 43673654L ) ).isEqualTo( "12h 7m 53s 654ms" );
		assertThat( DateUtil.formatDuration( 6443673321L ) ).isEqualTo( "74d 13h 54m 33s 321ms" );
	}

}
