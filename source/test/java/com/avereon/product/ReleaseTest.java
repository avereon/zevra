package com.avereon.product;

import com.avereon.util.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class ReleaseTest {

	private String versionString = "1.2.3-u-04";

	private DateFormat timestampFormat = new SimpleDateFormat( DateUtil.DEFAULT_DATE_FORMAT );

	@BeforeEach
	void setup() {
		timestampFormat.setTimeZone( DateUtil.DEFAULT_TIME_ZONE );
	}

	@Test
	void constructor() {
		assertThat( new Release().toHumanString() ).isEqualTo( "UNKNOWN" );
		assertThat( new Release( "" ).toHumanString() ).isEqualTo( "" );
		assertThat( new Release( "1" ).toString() ).isEqualTo( "1" );
	}

	@Test
	void testConstructorWithStringAndTimestamp() throws Exception {
		Release release = new Release( versionString, timestampFormat.parse( "1970-01-01 00:00:00" ) );
		assertThat( release.version().toString() ).isEqualTo( versionString );
		assertThat( release.timestamp() ).isEqualTo( new Date( 0 ) );
	}

	@Test
	void testConstructorWithStringStringAndNullTimestamp() {
		Release release = new Release( versionString, null );
		assertThat( release.version().toString() ).isEqualTo( versionString );
		assertThat( release.timestamp() ).isNull();
	}

	@Test
	void testCreateWithBadTimestampString() {
		Release release = Release.create( versionString, "bad date string" );
		assertThat( release.version().toString() ).isEqualTo( versionString );
		assertThat( release.timestamp() ).isNull();
	}

	@Test
	void testGetVersion() {
		Release release = new Release( versionString );
		assertThat( new Version( "1.2.3-u-04" ).compareTo( release.version() ) ).isEqualTo( 0 );
	}

	@Test
	void testGetTimestamp() {
		assertThat( new Release( versionString ).timestamp() ).isNull();
		assertThat( new Release( new Version( versionString ), new Date( 0 ) ).timestamp() ).isEqualTo( new Date( 0 ) );
	}

	@Test
	void testGetTimestampString() {
		assertThat( new Release( versionString ).getTimestampString() ).isEqualTo( "" );
		assertThat( new Release( versionString, new Date( 0 ) ).getTimestampString() ).isEqualTo( "1970-01-01 00:00:00" );
	}

	@Test
	void testGetTimestampStringWithTimeZone() {
		TimeZone zone = TimeZone.getTimeZone( "US/Pacific" );
		assertThat( new Release( versionString ).getTimestampString( zone ) ).isEqualTo( "" );
		assertThat( new Release( versionString, new Date( 0 ) ).getTimestampString( zone ) ).isEqualTo( "1969-12-31 16:00:00 PST" );
	}

	@Test
	void testToString() {
		assertThat( new Release( versionString ).toString() ).isEqualTo( "1.2.3-u-04" );
		assertThat( new Release( new Version( versionString ), new Date( 0 ) ).toString() ).isEqualTo( "1.2.3-u-04  1970-01-01 00:00:00" );
	}

	@Test
	void testToHumanString() {
		assertThat( new Release( versionString ).toHumanString() ).isEqualTo( "1.2.3 Update 04" );
		assertThat( new Release( new Version( versionString ), new Date( 0 ) ).toHumanString() ).isEqualTo( "1.2.3 Update 04  1970-01-01 00:00:00" );
	}

	@Test
	void testToHumanStringWithTimeZone() {
		TimeZone zone = TimeZone.getTimeZone( "Antarctica/South_Pole" );
		assertThat( new Release( versionString ).toHumanString( zone ) ).isEqualTo( "1.2.3 Update 04" );
		assertThat( new Release( new Version( versionString ), new Date( 0 ) ).toHumanString( zone ) ).isEqualTo( "1.2.3 Update 04  1970-01-01 12:00:00 NZST" );
	}

	@Test
	void testEncode() {
		assertThat( Release.encode( new Release( "" ) ) ).isEqualTo( "" );
		assertThat( Release.encode( new Release( versionString ) ) ).isEqualTo( "1.2.3-u-04" );
		assertThat( Release.encode( new Release( versionString, new Date( 0 ) ) ) ).isEqualTo( "1.2.3-u-04  0" );
	}

	@Test
	void testDecode() {
		assertThat( Release.decode( null ) ).isEqualTo( new Release() );
		assertThat( Release.decode( "" ) ).isEqualTo( new Release( "" ) );
		assertThat( Release.decode( "1.2.3-u-04" ) ).isEqualTo( new Release( versionString ) );
		assertThat( Release.decode( "1.2.3-u-04  0" ) ).isEqualTo( new Release( versionString, new Date( 0 ) ) );
	}

	@Test
	void testEquals() {
		assertThat( new Release( versionString ) ).isEqualTo( new Release( versionString ) );
		assertThat( new Release( versionString ) ).isNotEqualTo( new Release( "1.2.3 Update 04" ) );

		assertThat( new Release( "1" ) ).isEqualTo( new Release( new Version( "1" ) ) );
		assertThat( new Release( "2" ) ).isNotEqualTo( new Release( new Version( "1" ) ) );
		assertThat( new Release( "1", new Date( 0 ) ) ).isEqualTo( new Release( new Version( "1" ), new Date( 0 ) ) );
		assertThat( new Release( "1", new Date( 1 ) ) ).isNotEqualTo( new Release( new Version( "1" ), new Date( 0 ) ) );
	}

	@Test
	void testHashCode() {
		assertThat( new Release( versionString ).hashCode() ).isEqualTo( new Release( versionString ).hashCode() );
		assertThat( new Release( versionString, null ).hashCode() ).isEqualTo( new Release( versionString, null ).hashCode() );
		assertThat( new Release( versionString, new Date( 0 ) ).hashCode() ).isEqualTo( new Release( versionString, new Date( 0 ) ).hashCode() );
	}

	@Test
	void testCompareTo() {
		assertThat( new Release( "1" ).compareTo( new Release( "1" ) ) ).isEqualTo( 0 );
		assertThat( new Release( "1" ).compareTo( new Release( "2" ) ) ).isLessThan( 0 );
		assertThat( new Release( "2" ).compareTo( new Release( "1" ) ) ).isGreaterThan( 0 );

		assertThat( new Release( new Version( "1" ), new Date( 0 ) ).compareTo( new Release( new Version( "1" ), new Date( 0 ) ) ) ).isEqualTo( 0 );
		assertThat( new Release( new Version( "1" ), new Date( -1 ) ).compareTo( new Release( new Version( "1" ), new Date( 0 ) ) ) ).isLessThan( 0 );
		assertThat( new Release( new Version( "1" ), new Date( 1 ) ).compareTo( new Release( new Version( "1" ), new Date( 0 ) ) ) ).isGreaterThan( 0 );

		assertThat( new Release( new Version( "1" ), null ).compareTo( new Release( new Version( "1" ), null ) ) ).isEqualTo( 0 );
		assertThat( new Release( new Version( "1" ), null ).compareTo( new Release( new Version( "1" ), new Date( 0 ) ) ) ).isEqualTo( 0 );
		assertThat( new Release( new Version( "1" ), new Date( 0 ) ).compareTo( new Release( new Version( "1" ), null ) ) ).isEqualTo( 0 );
	}

}
