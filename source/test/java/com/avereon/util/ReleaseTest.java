package com.avereon.util;

import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class ReleaseTest {

	private String versionString = "1.2.3-u-04";

	private DateFormat timestampFormat = new SimpleDateFormat( DateUtil.DEFAULT_DATE_FORMAT );

	@Before
	public void setup() {
		timestampFormat.setTimeZone( DateUtil.DEFAULT_TIME_ZONE );
	}

	@Test
	public void testConstructorWithStringAndTimestamp() throws Exception {
		Release release = new Release( versionString, timestampFormat.parse( "1970-01-01 00:00:00" ) );
		assertEquals( versionString, release.getVersion().toString() );
		assertEquals( new Date( 0 ), release.getTimestamp() );
	}

	@Test
	public void testConstructorWithStringStringAndNullTimestamp() throws Exception {
		Release release = new Release( versionString, null );
		assertEquals( versionString, release.getVersion().toString() );
		assertEquals( null, release.getTimestamp() );
	}

	@Test
	public void testCreateWithBadTimestampString() throws Exception {
		Release release = Release.create( versionString, "bad date string" );
		assertEquals( versionString, release.getVersion().toString() );
		assertEquals( null, release.getTimestamp() );
	}

	@Test
	public void testGetVersion() {
		Release release = new Release( versionString );
		assertEquals( 0, new Version( "1.2.3-u-04" ).compareTo( release.getVersion() ) );
	}

	@Test
	public void testGetTimestamp() {
		assertNull( new Release( versionString ).getTimestamp() );
		assertEquals( new Date( 0 ), new Release( new Version( versionString ), new Date( 0 ) ).getTimestamp() );
	}

	@Test
	public void testGetTimestampString() {
		assertEquals( "", new Release( versionString ).getTimestampString() );
		assertEquals( "1970-01-01 00:00:00", new Release( versionString, new Date( 0 ) ).getTimestampString() );
	}

	@Test
	public void testGetTimestampStringWithTimeZone() {
		TimeZone zone = TimeZone.getTimeZone( "US/Pacific" );
		assertEquals( "", new Release( versionString ).getTimestampString( zone ) );
		assertEquals( "1969-12-31 16:00:00 PST", new Release( versionString, new Date( 0 ) ).getTimestampString( zone ) );
	}

	@Test
	public void testToString() {
		assertEquals( "1.2.3-u-04", new Release( versionString ).toString() );
		assertEquals( "1.2.3-u-04  1970-01-01 00:00:00", new Release( new Version( versionString ), new Date( 0 ) ).toString() );
	}

	@Test
	public void testToHumanString() {
		assertEquals( "1.2.3 Update 04", new Release( versionString ).toHumanString() );
		assertEquals( "1.2.3 Update 04  1970-01-01 00:00:00", new Release( new Version( versionString ), new Date( 0 ) ).toHumanString() );
	}

	@Test
	public void testToHumanStringWithTimeZone() {
		TimeZone zone = TimeZone.getTimeZone( "Antarctica/South_Pole" );
		assertEquals( "1.2.3 Update 04", new Release( versionString ).toHumanString( zone ) );
		assertEquals( "1.2.3 Update 04  1970-01-01 12:00:00 NZST", new Release( new Version( versionString ), new Date( 0 ) ).toHumanString( zone ) );
	}

	@Test
	public void testEncode() {
		assertEquals( "", Release.encode( new Release( "" ) ) );
		assertEquals( "1.2.3-u-04", Release.encode( new Release( versionString ) ) );
		assertEquals( "1.2.3-u-04  0", Release.encode( new Release( versionString, new Date( 0 ) ) ) );
	}

	@Test
	public void testDecode() {
		assertNull( Release.decode( null ) );
		assertEquals( new Release( "" ), Release.decode( "" ) );
		assertEquals( new Release( versionString ), Release.decode( "1.2.3-u-04" ) );
		assertEquals( new Release( versionString, new Date( 0 ) ), Release.decode( "1.2.3-u-04  0" ) );
	}

	@Test
	public void testEquals() {
		assertTrue( new Release( versionString ).equals( new Release( versionString ) ) );
		assertFalse( new Release( "1.2.3 Update 04" ).equals( new Release( versionString ) ) );

		assertTrue( new Release( new Version( "1" ) ).equals( new Release( "1" ) ) );
		assertFalse( new Release( new Version( "1" ) ).equals( new Release( "2" ) ) );
		assertTrue( new Release( new Version( "1" ), new Date( 0 ) ).equals( new Release( "1", new Date( 0 ) ) ) );
		assertFalse( new Release( new Version( "1" ), new Date( 0 ) ).equals( new Release( "1", new Date( 1 ) ) ) );
	}

	@Test
	public void testHashCode() {
		assertTrue( new Release( versionString ).hashCode() == new Release( versionString ).hashCode() );
		assertTrue( new Release( versionString, null ).hashCode() == new Release( versionString, null ).hashCode() );
		assertTrue( new Release( versionString, new Date( 0 ) ).hashCode() == new Release( versionString, new Date( 0 ) ).hashCode() );
	}

	@Test
	public void testCompareTo() {
		assertTrue( new Release( "1" ).compareTo( new Release( "1" ) ) == 0 );
		assertTrue( new Release( "1" ).compareTo( new Release( "2" ) ) < 0 );
		assertTrue( new Release( "2" ).compareTo( new Release( "1" ) ) > 0 );

		assertTrue( new Release( new Version( "1" ), new Date( 0 ) ).compareTo( new Release( new Version( "1" ), new Date( 0 ) ) ) == 0 );
		assertTrue( new Release( new Version( "1" ), new Date( -1 ) ).compareTo( new Release( new Version( "1" ), new Date( 0 ) ) ) < 0 );
		assertTrue( new Release( new Version( "1" ), new Date( 1 ) ).compareTo( new Release( new Version( "1" ), new Date( 0 ) ) ) > 0 );

		assertTrue( new Release( new Version( "1" ), null ).compareTo( new Release( new Version( "1" ), null ) ) == 0 );
		assertTrue( new Release( new Version( "1" ), null ).compareTo( new Release( new Version( "1" ), new Date( 0 ) ) ) == 0 );
		assertTrue( new Release( new Version( "1" ), new Date( 0 ) ).compareTo( new Release( new Version( "1" ), null ) ) == 0 );
	}

}
