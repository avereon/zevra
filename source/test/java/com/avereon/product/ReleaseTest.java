package com.avereon.product;

import com.avereon.util.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class ReleaseTest {

	private String versionString = "1.2.3-u-04";

	private DateFormat timestampFormat = new SimpleDateFormat( DateUtil.DEFAULT_DATE_FORMAT );

	@BeforeEach
	void setup() {
		timestampFormat.setTimeZone( DateUtil.DEFAULT_TIME_ZONE );
	}

	@Test
	void testConstructorWithStringAndTimestamp() throws Exception {
		Release release = new Release( versionString, timestampFormat.parse( "1970-01-01 00:00:00" ) );
		assertEquals( versionString, release.getVersion().toString() );
		assertEquals( new Date( 0 ), release.getTimestamp() );
	}

	@Test
	void testConstructorWithStringStringAndNullTimestamp() {
		Release release = new Release( versionString, null );
		assertThat( release.getVersion().toString(), is( versionString ) );
		assertThat( release.getTimestamp(), is( nullValue() ) );
	}

	@Test
	void testCreateWithBadTimestampString() {
		Release release = Release.create( versionString, "bad date string" );
		assertThat( release.getVersion().toString(), is( versionString ) );
		assertThat( release.getTimestamp(), is( nullValue() ) );
	}

	@Test
	void testGetVersion() {
		Release release = new Release( versionString );
		assertEquals( 0, new Version( "1.2.3-u-04" ).compareTo( release.getVersion() ) );
	}

	@Test
	void testGetTimestamp() {
		assertNull( new Release( versionString ).getTimestamp() );
		assertEquals( new Date( 0 ), new Release( new Version( versionString ), new Date( 0 ) ).getTimestamp() );
	}

	@Test
	void testGetTimestampString() {
		assertEquals( "", new Release( versionString ).getTimestampString() );
		assertEquals( "1970-01-01 00:00:00", new Release( versionString, new Date( 0 ) ).getTimestampString() );
	}

	@Test
	void testGetTimestampStringWithTimeZone() {
		TimeZone zone = TimeZone.getTimeZone( "US/Pacific" );
		assertEquals( "", new Release( versionString ).getTimestampString( zone ) );
		assertEquals( "1969-12-31 16:00:00 PST", new Release( versionString, new Date( 0 ) ).getTimestampString( zone ) );
	}

	@Test
	void testToString() {
		assertEquals( "1.2.3-u-04", new Release( versionString ).toString() );
		assertEquals( "1.2.3-u-04  1970-01-01 00:00:00", new Release( new Version( versionString ), new Date( 0 ) ).toString() );
	}

	@Test
	void testToHumanString() {
		assertEquals( "1.2.3 Update 04", new Release( versionString ).toHumanString() );
		assertEquals( "1.2.3 Update 04  1970-01-01 00:00:00", new Release( new Version( versionString ), new Date( 0 ) ).toHumanString() );
	}

	@Test
	void testToHumanStringWithTimeZone() {
		TimeZone zone = TimeZone.getTimeZone( "Antarctica/South_Pole" );
		assertEquals( "1.2.3 Update 04", new Release( versionString ).toHumanString( zone ) );
		assertEquals( "1.2.3 Update 04  1970-01-01 12:00:00 NZST", new Release( new Version( versionString ), new Date( 0 ) ).toHumanString( zone ) );
	}

	@Test
	void testEncode() {
		assertEquals( "", Release.encode( new Release( "" ) ) );
		assertEquals( "1.2.3-u-04", Release.encode( new Release( versionString ) ) );
		assertEquals( "1.2.3-u-04  0", Release.encode( new Release( versionString, new Date( 0 ) ) ) );
	}

	@Test
	void testDecode() {
		assertNull( Release.decode( null ) );
		assertEquals( new Release( "" ), Release.decode( "" ) );
		assertEquals( new Release( versionString ), Release.decode( "1.2.3-u-04" ) );
		assertEquals( new Release( versionString, new Date( 0 ) ), Release.decode( "1.2.3-u-04  0" ) );
	}

	@Test
	void testEquals() {
		assertThat( new Release( versionString ), is( new Release( versionString ) ) );
		assertThat( new Release( versionString ), is( not( new Release( "1.2.3 Update 04" ) ) ) );

		assertThat( new Release( "1" ), is( new Release( new Version( "1" ) ) ) );
		assertThat( new Release( "2" ), is( not( new Release( new Version( "1" ) ) ) ) );
		assertThat( new Release( "1", new Date( 0 ) ), is( new Release( new Version( "1" ), new Date( 0 ) ) ) );
		assertThat( new Release( "1", new Date( 1 ) ), is( not( new Release( new Version( "1" ), new Date( 0 ) ) ) ) );
	}

	@Test
	void testHashCode() {
		assertThat( new Release( versionString ).hashCode(), is( new Release( versionString ).hashCode() ) );
		assertThat( new Release( versionString, null ).hashCode(), is( new Release( versionString, null ).hashCode() ) );
		assertThat( new Release( versionString, new Date( 0 ) ).hashCode(), is( new Release( versionString, new Date( 0 ) ).hashCode() ) );
	}

	@Test
	void testCompareTo() {
		assertThat( new Release( "1" ).compareTo( new Release( "1" ) ), is( 0 ) );
		assertTrue( new Release( "1" ).compareTo( new Release( "2" ) ) < 0 );
		assertTrue( new Release( "2" ).compareTo( new Release( "1" ) ) > 0 );

		assertThat( new Release( new Version( "1" ), new Date( 0 ) ).compareTo( new Release( new Version( "1" ), new Date( 0 ) ) ), is( 0 ) );
		assertTrue( new Release( new Version( "1" ), new Date( -1 ) ).compareTo( new Release( new Version( "1" ), new Date( 0 ) ) ) < 0 );
		assertTrue( new Release( new Version( "1" ), new Date( 1 ) ).compareTo( new Release( new Version( "1" ), new Date( 0 ) ) ) > 0 );

		assertThat( new Release( new Version( "1" ), null ).compareTo( new Release( new Version( "1" ), null ) ), is( 0 ) );
		assertThat( new Release( new Version( "1" ), null ).compareTo( new Release( new Version( "1" ), new Date( 0 ) ) ), is( 0 ) );
		assertThat( new Release( new Version( "1" ), new Date( 0 ) ).compareTo( new Release( new Version( "1" ), null ) ), is( 0 ) );
	}

}
