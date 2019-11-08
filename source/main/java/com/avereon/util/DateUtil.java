package com.avereon.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@SuppressWarnings( "WeakerAccess" )
public class DateUtil {

	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone( "UTC" );

	public static final TimeZone LOCAL_TIME_ZONE = TimeZone.getDefault();

	/**
	 * Convenience method to get the current year for the UTC time zone.
	 *
	 * @return The current year for the UTC time zone
	 */
	public static int getCurrentYear() {
		return getCurrentYear( DEFAULT_TIME_ZONE );
	}

	/**
	 * Convenience method to get the current year based on time zone.
	 *
	 * @param timeZone The time zone for which to get the current year
	 * @return The current year for the specified time zone
	 */
	public static int getCurrentYear( String timeZone ) {
		return getCurrentYear( TimeZone.getTimeZone( timeZone ) );
	}

	/**
	 * Convenience method to get the current year based on time zone.
	 *
	 * @param timeZone The time zone for which to get the current year
	 * @return The current year for the specified time zone
	 */
	public static int getCurrentYear( TimeZone timeZone ) {
		return Calendar.getInstance( timeZone ).get( Calendar.YEAR );
	}

	/**
	 * Parse a date string with the default format using the standard time zone.
	 *
	 * @param data
	 * @return
	 */
	public static Date parse( String data ) {
		return parse( data, DEFAULT_DATE_FORMAT, DEFAULT_TIME_ZONE );
	}

	/**
	 * Parse a date string with the given format using the standard time zone.
	 *
	 * @param data
	 * @param format
	 * @return
	 */
	public static Date parse( String data, String format ) {
		return parse( data, format, DEFAULT_TIME_ZONE );
	}

	/**
	 * Parse a date string with the given format and time zone.
	 *
	 * @param data
	 * @param format
	 * @param timeZone
	 * @return
	 */
	public static Date parse( String data, String format, String timeZone ) {
		return parse( data, format, timeZone == null ? null : TimeZone.getTimeZone( timeZone ) );
	}

	/**
	 * Parse a date string with the given format and time zone.
	 *
	 * @param data The date string to parse
	 * @param format The date format of the data
	 * @param timeZone The timezone of the data
	 * @return The parsed date or null if there is a parsing error
	 */
	public static Date parse( String data, String format, TimeZone timeZone ) {
		if( data == null ) return null;

		SimpleDateFormat formatter = new SimpleDateFormat( format );
		formatter.setTimeZone( timeZone );

		try {
			return formatter.parse( data );
		} catch( ParseException exception ) {
			return null;
		}
	}

	/**
	 * Format a date with the given format using the standard time zone.
	 *
	 * @param date
	 * @return
	 */
	public static String format( Date date ) {
		return format( date, DEFAULT_DATE_FORMAT, DEFAULT_TIME_ZONE );
	}

	/**
	 * Format a date with the given format using the standard time zone.
	 *
	 * @param date
	 * @param format
	 * @return
	 */
	public static String format( Date date, String format ) {
		return format( date, format, DEFAULT_TIME_ZONE );
	}

	/**
	 * Format a date with the given format and time zone.
	 *
	 * @param date
	 * @param format
	 * @param timeZone
	 * @return
	 */
	public static String format( Date date, String format, String timeZone ) {
		return format( date, format, TimeZone.getTimeZone( timeZone ) );
	}

	/**
	 * Format a date with the given format and time zone.
	 *
	 * @param date
	 * @param format
	 * @param zone
	 * @return
	 */
	public static String format( Date date, String format, TimeZone zone ) {
		SimpleDateFormat formatter = new SimpleDateFormat( format );
		formatter.setTimeZone( zone );

		StringBuilder builder = new StringBuilder( formatter.format( date ) );
		if( !zone.equals( DateUtil.DEFAULT_TIME_ZONE ) ) {
			builder.append( " " );
			builder.append( zone.getDisplayName( zone.inDaylightTime( date ), TimeZone.SHORT ) );
		}

		return builder.toString();

	}

	/**
	 * Format a duration in milliseconds to day/hour/minute/second/millisecond
	 * format. For example: 6443673321L formats to "74d 13h 54m 33s 321ms".
	 *
	 * @param time The duration in milliseconds
	 * @return A string in day/hour/minute/second/millisecond format
	 */
	public static String formatDuration( long time ) {
		long millis = time % 1000;
		time /= 1000;
		long seconds = time % 60;
		time /= 60;
		long minutes = time % 60;
		time /= 60;
		long hours = time % 24;
		time /= 24;

		StringBuilder builder = new StringBuilder();
		if( time != 0 ) {
			builder.append( " " );
			builder.append( time );
			builder.append( "d" );
		}

		if( hours != 0 ) {
			builder.append( " " );
			builder.append( hours );
			builder.append( "h" );
		}

		if( minutes != 0 ) {
			builder.append( " " );
			builder.append( minutes );
			builder.append( "m" );
		}

		if( seconds != 0 ) {
			builder.append( " " );
			builder.append( seconds );
			builder.append( "s" );
		}

		if( millis != 0 ) {
			builder.append( " " );
			builder.append( millis );
			builder.append( "ms" );
		}

		return builder.toString().trim();
	}

}
