package com.avereon.util;

import lombok.CustomLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Utility class for working with text strings.
 */
@CustomLog
@SuppressWarnings( "WeakerAccess" )
public final class TextUtil {

	/**
	 * The empty string.
	 */
	public static final String EMPTY = "";

	public static final int LEFT = -1;

	public static final int CENTER = 0;

	public static final int RIGHT = 1;

	public static final String ENCODING = "UTF-8";

	public static final Charset CHARSET = Charset.forName( TextUtil.ENCODING );

	private static final char DEFAULT_PAD_CHAR = ' ';

	/**
	 * Checks if a given string is empty.
	 *
	 * @param string the string to check for empty value (null or blank)
	 * @return true if the string is empty, false otherwise
	 */
	public static boolean isEmpty( String string ) {
		return string == null || string.isBlank();
	}

	/**
	 * Checks if a string is not empty.
	 *
	 * @param string the string to check
	 * @return {@code true} if the string is not empty, {@code false} otherwise
	 */
	public static boolean isNotEmpty( String string ) {
		return !isEmpty( string );
	}

	/**
	 * Returns an empty string if the input string is null, otherwise returns
	 * the input string itself.
	 *
	 * @param string the input string to be checked for null
	 * @return an empty string if the input string is null, otherwise returns the input string itself
	 */
	public static String nullToEmpty( String string ) {
		return string == null ? EMPTY : string;
	}

	/**
	 * Compares two strings for equality.
	 *
	 * @param string1 The first string to compare
	 * @param string2 The second string to compare
	 * @return {@code true} if the strings are equal, {@code false} otherwise
	 */
	public static boolean areEqual( String string1, String string2 ) {
		if( string1 == null && string2 == null ) return true;
		if( string1 == null ) return false;
		if( string2 == null ) return false;
		return string1.equals( string2 );
	}

	/**
	 * Checks if two strings are equal (ignoring case).
	 *
	 * @param string1 the first string to compare
	 * @param string2 the second string to compare
	 * @return {@code true} if the strings are equal, ignoring case, {@code false} otherwise
	 */
	public static boolean areEqualIgnoreCase( String string1, String string2 ) {
		if( string1 == null && string2 == null ) return true;
		if( string1 == null ) return false;
		if( string2 == null ) return false;
		return string1.equalsIgnoreCase( string2 );
	}

	/**
	 * Determines whether two strings are the same string object.
	 *
	 * @param string1 the first string
	 * @param string2 the second string
	 * @return {@code true} if the two strings are the same object, {@code false} otherwise
	 */
	public static boolean areSame( String string1, String string2 ) {
		if( isEmpty( string1 ) && isEmpty( string2 ) ) return true;
		if( string1 == null ) return false;
		if( string2 == null ) return false;
		// It is correct to use '==' here
		return string1 == string2;
	}

	/**
	 * Compares two strings lexicographically. Returns the value 0 if the
	 * strings are equal, a value less than 0 if string1 is lexicographically
	 * less than string2, and a value greater than 0 if string1 is
	 * lexicographically greater than string2.
	 *
	 * @param string1 the first string to compare
	 * @param string2 the second string to compare
	 * @return 0 if the strings are equal, a negative value if string1 is
	 * less than string2, and a positive value if string1 is greater
	 * than string2
	 */
	public static int compare( String string1, String string2 ) {
		if( string1 == null && string2 == null ) return 0;
		if( string1 == null ) return -1;
		if( string2 == null ) return 1;
		return string1.compareTo( string2 );
	}

	/**
	 * Compares two strings lexicographically ignoring case considerations.
	 *
	 * @param string1 the first string to compare
	 * @param string2 the second string to compare
	 * @return the value 0 if the two strings are equal (ignoring case),
	 * a value less than 0 if the first string is lexicographically less than the second string,
	 * a value greater than 0 if the first string is lexicographically greater than the second string.
	 */
	public static int compareIgnoreCase( String string1, String string2 ) {
		if( string1 == null && string2 == null ) return 0;
		if( string1 == null ) return -1;
		if( string2 == null ) return 1;
		return string1.compareToIgnoreCase( string2 );
	}

	/**
	 * Cleans a string by removing leading/trailing whitespace and returning null
	 * if the string is empty or null.
	 *
	 * @param string the string to be cleaned
	 * @return the cleaned string, or null if the string is empty
	 */
	public static String cleanNull( String string ) {
		if( string == null ) return null;
		string = string.trim();
		return string.isEmpty() ? null : string;
	}

	/**
	 * Cleans the given string by removing leading and trailing whitespace.
	 * If the string is null, an empty string is returned.
	 *
	 * @param string the string to be cleaned
	 * @return the cleaned string
	 */
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

	/**
	 * Converts an array to a string representation.
	 *
	 * @param array the array to be converted
	 * @return the string representation of the array
	 */
	public static String toString( Object[] array ) {
		return toString( Arrays.asList( array ) );
	}

	/**
	 * Returns a string representation of the given array starting at the specified offset.
	 *
	 * @param array the array to create string representation of
	 * @param offset the starting offset of the array
	 * @return a string representation of the array starting at the specified offset
	 */
	public static String toString( Object[] array, int offset ) {
		return toString( array, offset, array.length - offset );
	}

	/**
	 * Converts the specified subarray of an Object array to a string representation.
	 *
	 * @param array the Object array from which the subarray is to be converted
	 * @param offset the starting index of the subarray (inclusive)
	 * @param length the length of the subarray
	 * @return a string representation of the specified subarray
	 */
	public static String toString( Object[] array, int offset, int length ) {
		Object[] items = new Object[ length ];
		System.arraycopy( array, offset, items, 0, length );
		return toString( Arrays.asList( items ) );
	}

	/**
	 * Returns a string representation of the given array, where each element is
	 * separated by the specified delimiter.
	 *
	 * @param array the array of objects to be converted to a string
	 * @param delimiter the delimiter to be used between elements in the resulting string
	 * @return a string representation of the array with elements separated by the delimiter
	 */
	public static String toString( Object[] array, String delimiter ) {
		return toString( Arrays.asList( array ), delimiter );
	}

	/**
	 * Returns a string representation of the specified array, starting from the
	 * specified offset, and using the specified delimiter to separate the elements.
	 *
	 * @param array the array to convert to a string
	 * @param delimiter the delimiter to use between array elements
	 * @param offset the starting offset in the array
	 * @return a string representation of the array
	 */
	public static String toString( Object[] array, String delimiter, int offset ) {
		return toString( array, delimiter, offset, array.length - offset );
	}

	/**
	 * Returns a string representation of the specified range of elements in the
	 * given array, delimited by the specified delimiter.
	 *
	 * @param array the array containing the elements
	 * @param delimiter the delimiter to be used between elements
	 * @param offset the starting offset of the range (inclusive)
	 * @param length the length of the range
	 * @return a string representation of the specified range of elements in the given array
	 */
	public static String toString( Object[] array, String delimiter, int offset, int length ) {
		Object[] items = new Object[ length ];
		System.arraycopy( array, offset, items, 0, length );
		return toString( Arrays.asList( items ), delimiter );
	}

	/**
	 * Returns a String representation of the given List.
	 *
	 * @param list the List to convert to a String
	 * @return a String representation of the given List
	 */
	public static String toString( List<?> list ) {
		return toString( list, "[", "]" );
	}

	/**
	 * Converts a List to a String representation using the specified delimiter.
	 *
	 * @param list the List to be converted to a String
	 * @param delimiter the delimiter to be used to separate the list elements
	 * @return the String representation of the List using the specified delimiter
	 */
	public static String toString( List<?> list, String delimiter ) {
		return toString( list, delimiter, null );
	}

	/**
	 * Converts a list of objects into a string representation with a specified
	 * prefix and suffix.
	 *
	 * @param list the list of objects to be converted
	 * @param prefix the prefix to be added before each object in the resulting string
	 * @param suffix the suffix to be added after each object in the resulting string
	 * @return the string representation of the list of objects with the specified prefix and suffix
	 */
	public static String toString( List<?> list, String prefix, String suffix ) {
		if( list == null ) return null;

		boolean delimiter = suffix == null;

		StringBuilder builder = new StringBuilder();

		for( Object object : list ) {
			if( !delimiter && !builder.isEmpty() ) builder.append( " " );
			if( !delimiter || !builder.isEmpty() ) builder.append( prefix );
			builder.append( object.toString() );
			if( !delimiter ) builder.append( suffix );
		}

		return builder.toString();
	}

	/**
	 * Returns the {@link String#valueOf(Object)} value of the object or null.
	 * This is different from {@link String#valueOf(Object)} which returns the
	 * String "null" when the object is null.
	 *
	 * @param object Any object
	 * @return The object string value or null
	 */
	public static String toStringOrNull( Object object ) {
		if( object == null ) return null;
		return String.valueOf( object );
	}

	/**
	 * Returns a printable string representation of a byte by converting byte
	 * values less than 32 or greater than 126 to the integer value surrounded by
	 * brackets.
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
			result = "[" + (value < 0 ? value + 256 : value) + "]";
			if( value == 13 ) result += "\n";
		}

		return result;
	}

	/**
	 * Returns a printable string representation of a character by converting char
	 * values less than or equal to 32 or greater than or equal to 126 to the
	 * integer value surrounded by brackets.
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
			return "[" + (value < 0 ? value + 65536 : value) + "]";
		}
	}

	/**
	 * Converts a byte array to a printable string representation.
	 *
	 * @param data the byte array to convert
	 * @return the printable string representation of the byte array
	 */
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

	/**
	 * This method converts an array of characters into a printable string representation.
	 *
	 * @param data the array of characters to convert into a printable string
	 * @return a printable string representation of the given character array, or null if the input is null
	 */
	public static String toPrintableString( char[] data ) {
		if( data == null ) return null;
		StringBuilder builder = new StringBuilder();
		for( char aData : data ) {
			builder.append( toPrintableString( aData ) );
		}
		return builder.toString();
	}

	/**
	 * Converts the given string to a printable string representation by escaping special characters.
	 *
	 * @param data the string to be converted to a printable string
	 * @return the printable string representation of the given data
	 */
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

	/**
	 * Encodes an array of bytes into a secure hexadecimal representation.
	 *
	 * @param bytes the array of bytes to be encoded
	 * @return the secure hexadecimal representation of the given bytes as a string
	 */
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

	/**
	 * Decodes a hexadecimal encoded string into a byte array.
	 * Each pair of characters in the string represents a single byte.
	 * Throws an IllegalArgumentException if the string length is not even.
	 *
	 * @param string the hexadecimal encoded string to decode
	 * @return the decoded byte array
	 * @throws IllegalArgumentException if the string length is not even
	 */
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

	/**
	 * Checks if the given text can be parsed as an int.
	 *
	 * @param text the string to be checked
	 * @return true if the text can be parsed as an int, false otherwise
	 */
	public static boolean isInteger( String text ) {
		if( text == null ) return false;

		try {
			Integer.parseInt( text );
		} catch( NumberFormatException exception ) {
			return false;
		}

		return true;
	}

	/**
	 * Determines if the given string can be parsed as a long.
	 *
	 * @param text the string to be checked
	 * @return true if the string can be parsed as a long, false otherwise.
	 */
	public static boolean isLong( String text ) {
		if( text == null ) return false;

		try {
			Long.parseLong( text );
		} catch( NumberFormatException exception ) {
			return false;
		}

		return true;
	}

	/**
	 * Checks if the given string can be parsed as a float.
	 *
	 * @param text the string to be checked
	 * @return true if the string can be parsed as a float, false otherwise
	 */
	public static boolean isFloat( String text ) {
		if( text == null ) return false;

		try {
			Float.parseFloat( text );
		} catch( NumberFormatException exception ) {
			return false;
		}
		return true;
	}

	/**
	 * Checks if the given text can be parsed as a double.
	 *
	 * @param text the string to be checked
	 * @return true if the text can be parsed as a double, otherwise false
	 */
	public static boolean isDouble( String text ) {
		if( text == null ) return false;

		try {
			Double.parseDouble( text );
		} catch( NumberFormatException exception ) {
			return false;
		}
		return true;
	}

	/**
	 * Capitalizes the first letter of a given string.
	 *
	 * @param string the string to capitalize
	 * @return the capitalized string, or null if the input string is null
	 */
	public static String capitalize( String string ) {
		if( string == null ) return null;
		char[] chars = string.toCharArray();
		if( chars.length > 0 ) chars[ 0 ] = Character.toUpperCase( chars[ 0 ] );
		return new String( chars );
	}

	/**
	 * Justifies the given text based on the desired alignment and width.
	 * Accepted values for the alignment are:
	 * <br>{@link #LEFT}: Justifies the text to the left.
	 * <br>{@link #CENTER}: Centers the text.
	 * <br>{@link #RIGHT}: Justifies the text to the right.
	 *
	 * @param alignment The desired alignment of the text.
	 * @param text The text to be justified.
	 * @param width The desired width of the justified text.
	 * @return The justified text.
	 */
	public static String justify( int alignment, String text, int width ) {
		return justify( alignment, text, width, DEFAULT_PAD_CHAR );
	}

	/**
	 * Justifies the given text within the specified width using the specified
	 * character as padding. Accepted values for the alignment are:
	 * <br>{@link #LEFT}: Justifies the text to the left.
	 * <br>{@link #CENTER}: Centers the text.
	 * <br>{@link #RIGHT}: Justifies the text to the right.
	 *
	 * @param alignment the alignment of the text.
	 * @param text the text to justify.
	 * @param width the width within which the text should be justified.
	 * @param chr the character to be used for padding.
	 * @return the justified text.
	 */
	public static String justify( int alignment, String text, int width, char chr ) {
		return justify( alignment, text, width, chr, 0 );
	}

	/**
	 * Justifies the given text based on the specified alignment.
	 * Accepted values for the alignment are:
	 * <br>{@link #LEFT}: Justifies the text to the left.
	 * <br>{@link #CENTER}: Centers the text.
	 * <br>{@link #RIGHT}: Justifies the text to the right.
	 *
	 * @param alignment the alignment type of the text
	 * @param text the text to be justified
	 * @param width the desired width of the justified text
	 * @param chr the character used for padding
	 * @param pad the number of characters used for padding
	 * @return the justified text based on the specified alignment
	 */
	public static String justify( int alignment, String text, int width, char chr, int pad ) {
		return switch( alignment ) {
			case CENTER -> centerJustify( text, width, chr, pad );
			case RIGHT -> rightJustify( text, width, chr, pad );
			default -> leftJustify( text, width, chr, pad );
		};
	}

	/**
	 * Pads a string with default padding character up to the specified width.
	 *
	 * @param width The desired width of the padded string.
	 * @return The padded string.
	 */
	public static String pad( int width ) {
		return pad( width, DEFAULT_PAD_CHAR );
	}

	/**
	 * Creates a string consisting of a specified width of repeated characters.
	 *
	 * @param width The width of the resulting string. Must be a positive integer.
	 * @param chr The character to be repeated in the resulting string.
	 * @return A string consisting of {@code width} times the character {@code chr}.
	 */
	public static String pad( int width, char chr ) {
		if( width <= 0 ) return "";
		char[] pad = new char[ width ];
		Arrays.fill( pad, chr );
		return new String( pad );
	}

	/**
	 * Left justifies the given text within the specified width by padding it with
	 * the {@link #DEFAULT_PAD_CHAR} character.
	 *
	 * @param text The text to be left justified.
	 * @param width The desired width of the justified text.
	 * @return The left justified text.
	 */
	public static String leftJustify( String text, int width ) {
		return leftJustify( text, width, DEFAULT_PAD_CHAR );
	}

	/**
	 * Left justifies a given text within a specified width, padding it with a
	 * specified character.
	 *
	 * @param text The text to be left justified.
	 * @param width The width within which the text should be left justified.
	 * @param chr The character used for padding the text.
	 * @return The left justified text.
	 */
	public static String leftJustify( String text, int width, char chr ) {
		return leftJustify( text, width, chr, 0 );
	}

	/**
	 * Left justifies the given text within the specified width, using the
	 * provided padding character and pad length.
	 *
	 * @param text the text to be left justified
	 * @param width the desired width of the resultant string
	 * @param chr the padding character to be used
	 * @param pad the length of the padding
	 * @return the left justified string with the specified width, padding character, and pad length
	 */
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

	/**
	 * Centers the given text within a specified width, using the
	 * {@link #DEFAULT_PAD_CHAR} character.
	 *
	 * @param text The text to be centered.
	 * @param width The width within which the text is to be centered.
	 * @return The centered text within the specified width.
	 */
	public static String centerJustify( String text, int width ) {
		return centerJustify( text, width, DEFAULT_PAD_CHAR );
	}

	/**
	 * Centers a text within a specified width, padding it with a specified character.
	 *
	 * @param text The text to be centered.
	 * @param width The total width of the resulting string.
	 * @param chr The character used for padding.
	 * @return The centered text within the specified width.
	 */
	public static String centerJustify( String text, int width, char chr ) {
		return centerJustify( text, width, chr, 0 );
	}

	/**
	 * Centers the given text within a specified width, with optional padding on both sides.
	 *
	 * @param text The text to be centered.
	 * @param width The total width of the output string.
	 * @param chr The character used for padding.
	 * @param pad The padding size.
	 * @return The centered text string.
	 */
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

	/**
	 * Right justifies a given text within a specified width, using the
	 * {@link #DEFAULT_PAD_CHAR} character.
	 *
	 * @param text The text to be right justified.
	 * @param width The desired width for the right justified text.
	 * @return The right justified text.
	 */
	public static String rightJustify( String text, int width ) {
		return rightJustify( text, width, DEFAULT_PAD_CHAR );
	}

	/**
	 * Right justifies a given text within a specified width using a specified character.
	 *
	 * @param text the text to be right justified
	 * @param width the width within which the text should be right justified
	 * @param chr the character to use for right justification
	 * @return the right justified text
	 */
	public static String rightJustify( String text, int width, char chr ) {
		return rightJustify( text, width, chr, 0 );
	}

	/**
	 * Right justifies a given text within a specified width, using a specified character for padding.
	 * If the given text is null, it returns a string of the specified width filled with padding characters.
	 * If the given text length is greater than the width, it returns a substring of the text up to the width.
	 *
	 * @param text The text to right justify.
	 * @param width The width of the resulting right justified text.
	 * @param chr The character used for padding.
	 * @param pad The number of characters used for padding.
	 * @return The right justified text.
	 */
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

	/**
	 * This method takes a string as input and splits it into separate lines.
	 * Each line is added to a list and the list is returned.
	 *
	 * @param text the input string to be split into lines
	 * @return a list of strings representing the lines in the input string,
	 * or null if the input string is null
	 */
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
			log.atSevere().withCause( exception ).log( "Error parsing text" );
		}

		return lines;
	}

	/**
	 * Returns the number of lines in the given text.
	 *
	 * @param text the text to be parsed
	 * @return the number of lines in the text, or 0 if the text is null
	 */
	public static int getLineCount( String text ) {
		if( text == null ) return 0;

		int count = 0;
		BufferedReader reader = new BufferedReader( new StringReader( text ) );
		try {
			while( reader.readLine() != null ) {
				count++;
			}
		} catch( IOException exception ) {
			log.atSevere().withCause( exception ).log( "Error parsing text" );
		}

		return count;
	}

	/**
	 * Counts the number of lines in a given list that match a specified pattern.
	 *
	 * @param lines the list of lines to be checked
	 * @param pattern the pattern to be used for matching lines
	 * @return the number of lines that match the specified pattern
	 */
	public static int countLines( List<String> lines, String pattern ) {
		int result = 0;
		Pattern p = Pattern.compile( pattern );

		for( String line : lines ) {
			if( p.matcher( line ).matches() ) result++;
		}

		return result;
	}

	/**
	 * Finds the first occurrence of the specified pattern in a list of strings.
	 *
	 * @param lines the list of strings to search
	 * @param pattern the pattern to find
	 * @return the first line that contains the pattern, or null if no line is found
	 */
	public static String findLine( List<String> lines, String pattern ) {
		return findLine( lines, pattern, 0 );
	}

	/**
	 * Finds the first line in a list of strings that matches the given pattern.
	 *
	 * @param lines a list of strings to search through
	 * @param pattern the regular expression pattern to match against each line
	 * @param start the index to start searching from in the list (inclusive)
	 * @return the first line that matches the pattern, or null if no match is found
	 */
	public static String findLine( List<String> lines, String pattern, int start ) {
		Pattern p = Pattern.compile( pattern );

		int count = lines.size();
		for( int index = start; index < count; index++ ) {
			String line = lines.get( index );
			if( p.matcher( line ).matches() ) return line;
		}

		return null;
	}

	/**
	 * Finds all lines in the given list that match the specified pattern.
	 *
	 * @param lines the list of strings to search for lines
	 * @param pattern the regular expression pattern to match against each line
	 * @return a list of lines that match the pattern
	 */
	public static List<String> findLines( List<String> lines, String pattern ) {
		return findLines( lines, pattern, 0 );
	}

	/**
	 * Finds the lines that match a given pattern starting from a specified index.
	 *
	 * @param lines the list of lines to search
	 * @param pattern the regular expression pattern to match
	 * @param start the index to start searching from
	 * @return a list of lines that match the given pattern
	 */
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

	/**
	 * Finds the index of the first line in the given list of lines that matches the specified pattern.
	 *
	 * @param lines the list of lines to search in
	 * @param pattern the pattern to match against each line
	 * @return the index of the first line that matches the pattern, or -1 if no such line is found
	 */
	public static int findLineIndex( List<String> lines, String pattern ) {
		return findLineIndex( lines, pattern, 0 );
	}

	/**
	 * Finds the index of the first line in the given list of strings that matches the specified pattern,
	 * starting from the specified start index.
	 *
	 * @param lines the list of strings to search through
	 * @param pattern the regular expression pattern to match
	 * @param start the index at which to start searching
	 * @return the index of the first line that matches the pattern, or -1 if no match is found
	 */
	public static int findLineIndex( List<String> lines, String pattern, int start ) {
		Pattern p = Pattern.compile( pattern );

		int count = lines.size();
		for( int index = start; index < count; index++ ) {
			if( p.matcher( lines.get( index ) ).matches() ) return index;
		}

		return -1;
	}

	/**
	 * Prepends the given text to each line of the content.
	 *
	 * @param content the content to prepend the text to (not null)
	 * @param text the text to be prepended (not null or empty)
	 * @return a new string with the text prepended to each line of the content
	 */
	public static String prepend( String content, String text ) {
		if( content == null ) return null;
		if( isEmpty( text ) ) return content;

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

	/**
	 * Appends the given text to each line of the content and returns the modified content.
	 *
	 * @param content the content to append the text to (can be null)
	 * @param text the text to append to each line of the content
	 * @return the modified content with the text appended to each line, or null if the content is null
	 */
	public static String append( String content, String text ) {
		if( content == null ) return null;
		if( isEmpty( text ) ) return content;

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

	/**
	 * Splits the given text into multiple lines, with each line having a maximum width.
	 *
	 * @param text  The input text to be split into lines.
	 * @param width The maximum width of each line.
	 * @return The input text split into multiple lines with the specified width.
	 */
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
				if( !result.isEmpty() ) result.append( "\n" );
				result.append( line );
				line.delete( 0, line.length() );
				line.append( token );
			}
		}

		return result.toString();
	}

	/**
	 * Splits the given string into a list of tokens.
	 *
	 * @param string the string to be split
	 * @return a list of tokens
	 */
	public static List<String> split( String string ) {
		return new SimpleTokenizer( string ).getTokens();
	}

}
