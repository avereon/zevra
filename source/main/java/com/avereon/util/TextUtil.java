package com.avereon.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

@SuppressWarnings( "WeakerAccess" )
public final class TextUtil {

	private static final System.Logger log = Log.get();

	public static final int LEFT = -1;

	public static final int CENTER = 0;

	public static final int RIGHT = 1;

	public static final String ENCODING = "UTF-8";

	public static final Charset CHARSET = Charset.forName( TextUtil.ENCODING );

	private static final char DEFAULT_PAD_CHAR = ' ';

	public static boolean isEmpty( String string ) {
		if( string == null ) return true;
		return string.trim().length() == 0;
	}

	public static boolean areEqual( String string1, String string2 ) {
		if( string1 == null && string2 == null ) return true;
		if( string1 == null ) return false;
		if( string2 == null ) return false;
		return string1.equals( string2 );
	}

	public static boolean areEqualIgnoreCase( String string1, String string2 ) {
		if( string1 == null && string2 == null ) return true;
		if( string1 == null ) return false;
		if( string2 == null ) return false;
		return string1.equalsIgnoreCase( string2 );
	}

	public static boolean areSame( String string1, String string2 ) {
		if( isEmpty( string1 ) && isEmpty( string2 ) ) return true;
		if( string1 == null ) return false;
		if( string2 == null ) return false;
		return string1.equals( string2 );
	}

	public static int compare( String string1, String string2 ) {
		if( string1 == null && string2 == null ) return 0;
		if( string1 == null ) return -1;
		if( string2 == null ) return 1;
		return string1.compareTo( string2 );
	}

	public static int compareIgnoreCase( String string1, String string2 ) {
		if( string1 == null && string2 == null ) return 0;
		if( string1 == null ) return -1;
		if( string2 == null ) return 1;
		return string1.compareToIgnoreCase( string2 );
	}

	public static String cleanNull( String string ) {
		if( string == null ) return null;
		string = string.trim();
		return "".equals( string ) ? null : string;
	}

	public static String cleanEmpty( String string ) {
		return string == null ? "" : string.trim();
	}

	/**
	 * Concatenate multiple objects together using a fast string building object.
	 *
	 * @param objects The objects to concatenate
	 * @return The concatenated string
	 */
	public static String concatenate( Object... objects ) {
		StringBuilder builder = new StringBuilder();

		for( Object object : objects ) {
			builder.append( object == null ? "null" : object.toString() );
		}

		return builder.toString();
	}

	public static String toString( Object[] array ) {
		return toString( Arrays.asList( array ) );
	}

	public static String toString( Object[] array, int offset ) {
		return toString( array, offset, array.length - offset );
	}

	public static String toString( Object[] array, int offset, int length ) {
		Object[] items = new Object[ length ];
		System.arraycopy( array, offset, items, 0, length );
		return toString( Arrays.asList( items ) );
	}

	public static String toString( Object[] array, String delimiter ) {
		return toString( Arrays.asList( array ), delimiter );
	}

	public static String toString( Object[] array, String delimiter, int offset ) {
		return toString( array, delimiter, offset, array.length - offset );
	}

	public static String toString( Object[] array, String delimiter, int offset, int length ) {
		Object[] items = new Object[ length ];
		System.arraycopy( array, offset, items, 0, length );
		return toString( Arrays.asList( items ), delimiter );
	}

	public static String toString( List<?> list ) {
		return toString( list, "[", "]" );
	}

	public static String toString( List<?> list, String delimiter ) {
		return toString( list, delimiter, null );
	}

	public static String toString( List<?> list, String prefix, String suffix ) {
		if( list == null ) return null;

		boolean delimiter = suffix == null;

		StringBuilder builder = new StringBuilder();

		for( Object object : list ) {
			if( !delimiter && builder.length() > 0 ) builder.append( " " );
			if( !delimiter || builder.length() > 0 ) builder.append( prefix );
			builder.append( object.toString() );
			if( !delimiter ) builder.append( suffix );
		}

		return builder.toString();
	}

	/**
	 * Returns a printable string representation of a byte by converting byte values less than 32 or greater than 126 to the integer value surrounded by brackets.
	 * <p>
	 * Example: An escape char (27) would be returned as: [27]
	 * <p>
	 * Example: The letter A would be returned as: A
	 *
	 * @param data The byte to convert.
	 * @return A printable string representation of the byte.
	 */
	public static String toPrintableString( byte data ) {
		String result;
		if( (int)data >= 32 && (int)data <= 126 ) {
			result = String.valueOf( (char)(int)data );
		} else {
			short value = (short)(int)data;
			result = "[" + String.valueOf( (value < 0 ? value + 256 : value) ) + "]";
			if( value == 13 ) result += "\n";
		}

		return result;
	}

	/**
	 * Returns a printable string representation of a character by converting char values less than or equal to 32 or greater than or equal to 126 to the integer
	 * value surrounded by brackets.
	 * <p>
	 * Example: An escape char (27) would be returned as: [27]
	 * <p>
	 * Example: The letter A would be returned as: A
	 *
	 * @param data The character to convert.
	 * @return A printable string representation of the character.
	 */
	public static String toPrintableString( char data ) {

		if( data >= 32 && data <= 126 ) {
			return String.valueOf( data );
		} else {
			short value = (short)data;
			return "[" + String.valueOf( (value < 0 ? value + 65536 : value) ) + "]";
		}
	}

	public static String toPrintableString( byte[] data ) {
		return toPrintableString( data, 0, data.length );
	}

	public static String toPrintableString( byte[] data, int offset, int length ) {
		if( data == null ) return null;
		StringBuilder builder = new StringBuilder();
		int count = offset + length;
		for( int index = offset; index < count; index++ ) {
			byte value = data[ index ];
			builder.append( toPrintableString( (char)(value < 0 ? value + 256 : value) ) );
		}
		return builder.toString();
	}

	public static String toPrintableString( char[] data ) {
		if( data == null ) return null;
		StringBuilder builder = new StringBuilder();
		for( char aData : data ) {
			builder.append( toPrintableString( aData ) );
		}
		return builder.toString();
	}

	public static String toPrintableString( String data ) {
		return toPrintableString( data.toCharArray() );
	}

	/**
	 * Convert an array of bytes to a HEX encoded string.
	 *
	 * @param bytes The bytes to convert to hex.
	 * @return A hex encoded string of the byte array.
	 */
	public static String toHexEncodedString( byte[] bytes ) {
		int value;
		String string;
		StringBuilder builder = new StringBuilder();
		for( byte aByte : bytes ) {
			value = aByte;
			string = Integer.toHexString( value < 0 ? value + 256 : value );
			if( string.length() == 1 ) builder.append( "0" );
			builder.append( string );
		}
		return builder.toString();
	}

	/**
	 * Encode a string to a hex string.
	 *
	 * @param string The string to encode
	 * @return The hex encoded string
	 */
	public static String hexEncode( String string ) {
		return secureHexEncode( string.toCharArray() );
	}

	/**
	 * Decode a hex string to a string.
	 *
	 * @param string The hex encoded string to decode
	 * @return The decoded string
	 */
	public static String hexDecode( String string ) {
		char[] value = secureHexDecode( string );
		return value == null ? null : new String( value );
	}

	public static String secureHexByteEncode( byte[] bytes ) {
		if( bytes == null ) return null;

		int value;
		String string;
		StringBuilder builder = new StringBuilder();
		for( byte aByte : bytes ) {
			value = aByte;
			string = Integer.toHexString( value < 0 ? value + 256 : value );
			if( string.length() == 1 ) builder.append( "0" );
			builder.append( string );
		}

		return builder.toString();
	}

	public static byte[] secureHexByteDecode( String string ) {
		if( string == null ) return null;

		int count = string.length();
		if( count % 2 != 0 ) throw new IllegalArgumentException( "Invalid string length: " + count );

		// Divide the count by two.
		count /= 2;

		byte[] bytes = new byte[ count ];
		for( int index = 0; index < count; index += 1 ) {
			bytes[ index ] = (byte)Integer.parseInt( string.substring( index * 2, index * 2 + 2 ), 16 );
		}

		return bytes;
	}

	/**
	 * <p>
	 * Encode the char[] into a hex string. Secure means that at no time is the char[] converted, in its entirety, to or from a String. See the security note
	 * below.
	 * <p>
	 * Security note: Objects of type String are immutable, meaning there are no methods defined that allow you to change (overwrite) or zero out the contents of
	 * a String after usage. This feature makes String objects unsuitable for storing
	 * security sensitive information, such as passwords. The String objects can easily be discovered using standard debugging tools or other methods that can
	 * inspect the JVM memory. Security sensitive information should always be collected
	 * and stored in a char array instead.
	 *
	 * @param chars The characters to hex encode
	 * @return The hex encoded string
	 */
	public static String secureHexEncode( char[] chars ) {
		if( chars == null ) return null;

		String string;
		StringBuilder builder = new StringBuilder();
		for( char aChar : chars ) {
			string = Integer.toHexString( aChar );
			builder.append( rightJustify( string, 4, '0' ) );
		}

		return builder.toString();
	}

	/**
	 * Decode a hex string into a char[]. Secure means that at no time is the char[] converted, in its entirety, to or from a String. See the security note
	 * below.
	 * <p>
	 * Security note: Objects of type String are immutable, meaning there are no methods defined that allow you to change (overwrite) or zero out the contents of
	 * a String after usage. This feature makes String objects unsuitable for storing
	 * security sensitive information, such as passwords. The String objects can easily be discovered using standard debugging tools or other methods that can
	 * inspect the JVM memory. Security sensitive information should always be collected
	 * and stored in a char array instead.
	 *
	 * @param string The hex encoded string to decode
	 * @return The decoded characters
	 */
	public static char[] secureHexDecode( String string ) {
		if( string == null ) return null;

		int count = string.length();
		if( count % 4 != 0 ) throw new IllegalArgumentException( "Invalid string length: " + count );

		// Divide the count by four.
		count /= 4;

		char[] chars = new char[ count ];
		for( int index = 0; index < count; index += 1 ) {
			chars[ index ] = (char)Integer.parseInt( string.substring( index * 4, index * 4 + 4 ), 16 );
		}

		return chars;
	}

	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	public static boolean isInteger( String text ) {
		if( text == null ) return false;

		try {
			Integer.parseInt( text );
		} catch( NumberFormatException exception ) {
			return false;
		}
		return true;
	}

	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	public static boolean isLong( String text ) {
		if( text == null ) return false;

		try {
			Long.parseLong( text );
		} catch( NumberFormatException exception ) {
			return false;
		}
		return true;
	}

	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	public static boolean isFloat( String text ) {
		if( text == null ) return false;

		try {
			Float.parseFloat( text );
		} catch( NumberFormatException exception ) {
			return false;
		}
		return true;
	}

	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	public static boolean isDouble( String text ) {
		if( text == null ) return false;

		try {
			Double.parseDouble( text );
		} catch( NumberFormatException exception ) {
			return false;
		}
		return true;
	}

	public static String capitalize( String string ) {
		if( string == null ) return null;
		char[] chars = string.toCharArray();
		if( chars.length > 0 ) chars[ 0 ] = Character.toUpperCase( chars[ 0 ] );
		return new String( chars );
	}

	public static String justify( int alignment, String text, int width ) {
		return justify( alignment, text, width, DEFAULT_PAD_CHAR );
	}

	public static String justify( int alignment, String text, int width, char chr ) {
		return justify( alignment, text, width, chr, 0 );
	}

	public static String justify( int alignment, String text, int width, char chr, int pad ) {
		switch( alignment ) {
			case CENTER:
				return centerJustify( text, width, chr, pad );
			case RIGHT:
				return rightJustify( text, width, chr, pad );
			default:
				return leftJustify( text, width, chr, pad );
		}
	}

	public static String pad( int width ) {
		return pad( width, DEFAULT_PAD_CHAR );
	}

	public static String pad( int width, char chr ) {
		if( width <= 0 ) return "";
		char[] pad = new char[ width ];
		Arrays.fill( pad, chr );
		return new String( pad );
	}

	public static String leftJustify( String text, int width ) {
		return leftJustify( text, width, DEFAULT_PAD_CHAR );
	}

	public static String leftJustify( String text, int width, char chr ) {
		return leftJustify( text, width, chr, 0 );
	}

	public static String leftJustify( String text, int width, char chr, int pad ) {
		if( text == null ) return pad( width );
		if( text.length() > width ) return text.substring( 0, width );

		int right = width - text.length();
		StringBuilder builder = new StringBuilder( width );
		builder.append( text );
		if( right <= pad ) {
			builder.append( pad( right ) );
		} else {
			builder.append( pad( pad ) );
			builder.append( pad( right - pad, chr ) );
		}
		return builder.toString();
	}

	public static String centerJustify( String text, int width ) {
		return centerJustify( text, width, DEFAULT_PAD_CHAR );
	}

	public static String centerJustify( String text, int width, char chr ) {
		return centerJustify( text, width, chr, 0 );
	}

	public static String centerJustify( String text, int width, char chr, int pad ) {
		if( text == null ) return pad( width );
		if( text.length() > width ) return text.substring( 0, width );

		int left = (width - text.length()) / 2;
		int right = (width - text.length()) - left;

		StringBuilder builder = new StringBuilder( width );
		if( left <= pad ) {
			builder.append( pad( left ) );
		} else {
			builder.append( pad( left - pad, chr ) );
			builder.append( pad( pad ) );
		}
		builder.append( text );
		if( right <= pad ) {
			builder.append( pad( right ) );
		} else {
			builder.append( pad( pad ) );
			builder.append( pad( right - pad, chr ) );
		}
		return builder.toString();
	}

	public static String rightJustify( String text, int width ) {
		return rightJustify( text, width, DEFAULT_PAD_CHAR );
	}

	public static String rightJustify( String text, int width, char chr ) {
		return rightJustify( text, width, chr, 0 );
	}

	public static String rightJustify( String text, int width, char chr, int pad ) {
		if( text == null ) return pad( width );
		if( text.length() > width ) return text.substring( 0, width );

		int left = width - text.length();
		StringBuilder builder = new StringBuilder( width );
		if( left <= pad ) {
			builder.append( pad( left ) );
		} else {
			builder.append( pad( left - pad, chr ) );
			builder.append( pad( pad ) );
		}
		builder.append( text );
		return builder.toString();
	}

	public static List<String> getLines( String text ) {
		if( text == null ) return null;

		String line;
		List<String> lines = new ArrayList<>();
		BufferedReader reader = new BufferedReader( new StringReader( text ) );
		try {
			while( (line = reader.readLine()) != null ) {
				lines.add( line );
			}
		} catch( IOException exception ) {
			log.log( Log.ERROR, "Error parsing text", exception );
		}

		return lines;
	}

	public static int getLineCount( String text ) {
		if( text == null ) return 0;

		int count = 0;
		BufferedReader reader = new BufferedReader( new StringReader( text ) );
		try {
			while( reader.readLine() != null ) {
				count++;
			}
		} catch( IOException exception ) {
			log.log( Log.ERROR, "Error parsing text", exception );
		}

		return count;
	}

	public static int countLines( List<String> lines, String pattern ) {
		int result = 0;
		Pattern p = Pattern.compile( pattern );

		for( String line : lines ) {
			if( p.matcher( line ).matches() ) result++;
		}

		return result;
	}

	public static String findLine( List<String> lines, String pattern ) {
		return findLine( lines, pattern, 0 );
	}

	public static String findLine( List<String> lines, String pattern, int start ) {
		Pattern p = Pattern.compile( pattern );

		int count = lines.size();
		for( int index = start; index < count; index++ ) {
			String line = lines.get( index );
			if( p.matcher( line ).matches() ) return line;
		}

		return null;
	}

	public static List<String> findLines( List<String> lines, String pattern ) {
		return findLines( lines, pattern, 0 );
	}

	public static List<String> findLines( List<String> lines, String pattern, int start ) {
		List<String> result = new ArrayList<>();

		Pattern p = Pattern.compile( pattern );

		int count = lines.size();
		for( int index = start; index < count; index++ ) {
			String line = lines.get( index );
			if( p.matcher( line ).matches() ) result.add( line );
		}

		return result;
	}

	public static int findLineIndex( List<String> lines, String pattern ) {
		return findLineIndex( lines, pattern, 0 );
	}

	public static int findLineIndex( List<String> lines, String pattern, int start ) {
		Pattern p = Pattern.compile( pattern );

		int count = lines.size();
		for( int index = start; index < count; index++ ) {
			if( p.matcher( lines.get( index ) ).matches() ) return index;
		}

		return -1;
	}

	public static String prepend( String content, String text ) {
		if( content == null ) return null;
		if( text == null || "".equals( text ) ) return content;

		String line;
		LineParser parser = new LineParser( content );
		StringBuilder result = new StringBuilder();

		while( (line = parser.next()) != null ) {
			result.append( text );
			result.append( line );
			result.append( parser.getTerminator() );
		}

		return result.toString();
	}

	public static String append( String content, String text ) {
		if( content == null ) return null;
		if( text == null || "".equals( text ) ) return content;

		String line;
		LineParser parser = new LineParser( content );
		StringBuilder result = new StringBuilder();

		while( (line = parser.next()) != null ) {
			result.append( line );
			result.append( text );
			result.append( parser.getTerminator() );
		}

		return result.toString();
	}

	public static String reline( String text, int width ) {
		if( text == null ) return null;

		StringBuilder line = new StringBuilder();
		StringBuilder result = new StringBuilder();
		StringTokenizer tokenizer = new StringTokenizer( text );

		while( tokenizer.hasMoreTokens() ) {
			String token = tokenizer.nextToken();
			int lineLength = line.length();
			int next = lineLength + token.length() + 1;

			if( next <= width ) {
				if( lineLength > 0 ) line.append( " " );
				line.append( token );
			}

			if( next > width || !tokenizer.hasMoreTokens() ) {
				if( result.length() > 0 ) result.append( "\n" );
				result.append( line.toString() );
				line.delete( 0, line.length() );
				line.append( token );
			}
		}

		return result.toString();
	}

	public static List<String> split( String string ) {
		return new SimpleTokenizer( string ).getTokens();
	}

}
