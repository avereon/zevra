package com.avereon.product;

import com.avereon.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Date;
import java.util.TimeZone;

/**
 * Represents an artifact release, which is a version and a timestamp.
 *
 * @author Mark Soderquist
 */
@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonIgnoreProperties( ignoreUnknown = true )
public record Release(Version version, Date timestamp) implements Comparable<Release> {

	private static final String ENCODE_DELIMITER = "  ";

	public Release( String version ) {
		this( new Version( version ), null );
	}

	public Release( String version, Date timestamp ) {
		this( new Version( version ), timestamp );
	}

	public Release( Version version ) {
		this( version, null );
	}

	public Release {
		if( version == null ) throw new NullPointerException( "Version cannot be null." );
	}

	public static Release create( String version, String timestamp ) {
		return new Release( new Version( version ), DateUtil.parse( timestamp, DateUtil.DEFAULT_DATE_FORMAT ) );
	}

	public String getTimestampString() {
		return getTimestampString( DateUtil.DEFAULT_TIME_ZONE );
	}

	public String getTimestampString( TimeZone zone ) {
		if( timestamp == null ) return "";
		return DateUtil.format( timestamp, DateUtil.DEFAULT_DATE_FORMAT, zone );
	}

	@NonNull
	@Override
	public String toString() {
		return format( version.toString() );
	}

	@NonNull
	public String toHumanString() {
		return format( version.toHumanString() );
	}

	@NonNull
	public String toHumanString( TimeZone zone ) {
		return format( version.toHumanString(), zone );
	}

	@NonNull
	public static String encode( Release release ) {
		StringBuilder builder = new StringBuilder( release.version.toString() );
		if( release.timestamp != null ) {
			builder.append( ENCODE_DELIMITER );
			builder.append( release.timestamp.getTime() );
		}

		return builder.toString();
	}

	@Nullable
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
		if( !(object instanceof Release that) ) return false;
		return this.compareTo( that ) == 0;
	}

	@Override
	public int hashCode() {
		return version.hashCode() ^ (timestamp == null ? 0 : timestamp.hashCode());
	}

	@Override
	public int compareTo( Release that ) {
		int result = this.version().compareTo( that.version() );
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
