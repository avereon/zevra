package com.avereon.util;

import java.util.Date;
import java.util.TimeZone;

/**
 * Represents an artifact release, which is a version and a timestamp.
 *
 * @author Mark Soderquist
 */
public class Release implements Comparable<Release> {

	private static final String ENCODE_DELIMITER = "  ";

	private Version version;

	private Date timestamp;

	public Release( String version ) {
		this( new Version( version ), null );
	}

	public Release( String version, Date timestamp ) {
		this( new Version( version ), timestamp );
	}

	public Release( Version version ) {
		this( version, null );
	}

	public Release( Version version, Date timestamp ) {
		if( version == null ) throw new NullPointerException( "Version cannot be null." );
		this.version = version;
		this.timestamp = timestamp;
	}

	public static Release create( String version, String timestamp ) {
		return new Release( new Version( version ), DateUtil.parse( timestamp, DateUtil.DEFAULT_DATE_FORMAT ) );
	}

	public Version getVersion() {
		return version;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getTimestampString() {
		return getTimestampString( DateUtil.DEFAULT_TIME_ZONE );
	}

	public String getTimestampString( TimeZone zone ) {
		if( timestamp == null ) return "";
		return DateUtil.format( timestamp, DateUtil.DEFAULT_DATE_FORMAT, zone );
	}

	@Override
	public String toString() {
		return format( version.toString() );
	}

	public String toHumanString() {
		return format( version.toHumanString() );
	}

	public String toHumanString( TimeZone zone ) {
		return format( version.toHumanString(), zone );
	}

	public static String encode( Release release ) {
		StringBuilder builder = new StringBuilder( release.version.toString() );
		if( release.timestamp != null ) {
			builder.append( ENCODE_DELIMITER );
			builder.append( release.timestamp.getTime() );
		}

		return builder.toString();
	}

	public static Release decode( String release ) {
		if( release == null ) return null;

		int index = release.indexOf( ENCODE_DELIMITER );
		if( index < 0 ) return new Release( new Version( release ) );

		Version version = new Version( release.substring( 0, index ) );
		Date timestamp = new Date( Long.parseLong( release.substring( index + ENCODE_DELIMITER.length() ) ) );
		return new Release( version, timestamp );
	}

	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof Release) ) return false;

		Release that = (Release)object;
		return this.compareTo( that ) == 0;
	}

	@Override
	public int hashCode() {
		return version.hashCode() ^ (timestamp == null ? 0 : timestamp.hashCode());
	}

	@Override
	public int compareTo( Release that ) {
		int result = this.getVersion().compareTo( that.getVersion() );
		if( result != 0 ) return result;

		if( this.timestamp == null || that.timestamp == null ) return 0;
		return this.timestamp.compareTo( that.timestamp );
	}

	private String format( String version ) {
		return format( version, DateUtil.DEFAULT_TIME_ZONE );
	}

	private String format( String version, TimeZone zone ) {
		StringBuilder buffer = new StringBuilder();

		buffer.append( version );
		if( timestamp != null ) {
			buffer.append( "  " );
			buffer.append( getTimestampString( zone ) );
		}

		return buffer.toString();
	}

}
